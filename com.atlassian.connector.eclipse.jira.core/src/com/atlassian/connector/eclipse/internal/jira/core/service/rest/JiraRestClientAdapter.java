/*******************************************************************************

 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.core.service.rest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.commons.net.AuthenticatedProxy;
import org.eclipse.osgi.util.NLS;
import org.joda.time.DateTime;

import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;
import com.atlassian.connector.eclipse.internal.jira.core.model.Component;
import com.atlassian.connector.eclipse.internal.jira.core.model.IssueField;
import com.atlassian.connector.eclipse.internal.jira.core.model.IssueType;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraAction;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import com.atlassian.connector.eclipse.internal.jira.core.model.NamedFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.Priority;
import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.model.Resolution;
import com.atlassian.connector.eclipse.internal.jira.core.model.ServerInfo;
import com.atlassian.connector.eclipse.internal.jira.core.model.Version;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraAuthenticationException;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClientCache;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraServiceUnavailableException;
import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.domain.BasicPriority;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.atlassian.jira.rest.client.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;

public class JiraRestClientAdapter {

	private static final SimpleDateFormat REST_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$

	private static final SimpleDateFormat REST_DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ"); //$NON-NLS-1$

	private static final String HTTP_401 = "Client response status: 401"; //$NON-NLS-1$

	private static final String HTTP_403 = "Client response status: 403"; //$NON-NLS-1$

	public static final String HTTP_404 = "Client response status: 404"; //$NON-NLS-1$

	private static final String HTTP_302 = "Client response status: 302"; //$NON-NLS-1$

	private static final String CONNECTION_REFUSED = "java.net.ConnectException: Connection refused: connect"; //$NON-NLS-1$

	private static final String UNKNOWN_HOST_EXCEPTION = "java.net.UnknownHostException:"; //$NON-NLS-1$

	private static final String ILLEGAL_ARGUMENT_EXCEPTION = "java.lang.IllegalArgumentException:"; //$NON-NLS-1$

	private JiraRestClient restClient;

	private final JiraClientCache cache;

	private final String url;

	public JiraRestClientAdapter(String url, JiraClientCache cache) {
		this.url = url;
		this.cache = cache;
	}

	public JiraRestClientAdapter(String url, String userName, String password, final Proxy proxy, JiraClientCache cache) {
		this(url, cache);

//		JerseyJiraRestClientFactory restFactory = new JerseyJiraRestClientFactory();
//		this.restClient = restFactory.createWithBasicHttpAuthentication(new URI(url), userName, password);
		try {
			restClient = new JerseyJiraRestClientFactory().create(new URI(url), new BasicHttpAuthenticationHandler(
					userName, password) {
				@Override
				public void configure(ApacheHttpClientConfig config) {
					super.configure(config);
					if (proxy != null) {
						InetSocketAddress address = (InetSocketAddress) proxy.address();
						if (proxy instanceof AuthenticatedProxy) {
							AuthenticatedProxy authProxy = (AuthenticatedProxy) proxy;

							config.getState().setProxyCredentials(AuthScope.ANY_REALM, address.getHostName(),
									address.getPort(), authProxy.getUserName(), authProxy.getPassword());
						}

					}
				}
			});

			if (proxy != null) {
				final InetSocketAddress address = (InetSocketAddress) proxy.address();
				restClient.getTransportClient()
						.getProperties()
						.put(ApacheHttpClientConfig.PROPERTY_PROXY_URI,
								"http://" + address.getHostName() + ":" + address.getPort()); //$NON-NLS-1$ //$NON-NLS-2$
			}

		} catch (URISyntaxException e) {
			// we should never get here as Mylyn constructs URI first and fails if it is incorrect
			StatusHandler.log(new Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN, e.getMessage()));
		}
	}

	public void addComment(final String issueKey, final String comment) throws JiraException {

		call(new Callable<Void>() {
			public Void call() throws Exception {
				restClient.getIssueClient().addComment(new NullProgressMonitor(), getIssue(issueKey).getCommentsUri(),
						Comment.valueOf(comment));

				return null;
			}
		});
	}

	private Issue getIssue(final String issueKeyOrId) throws JiraException {
		return call(new Callable<Issue>() {
			public Issue call() {
				return restClient.getIssueClient().getIssue(issueKeyOrId,
						ImmutableList.of(IssueRestClient.Expandos.EDITMETA), new NullProgressMonitor());
			}
		});
	}

	public void addAttachment(String issueKey, byte[] content, String filename) throws JiraException {
		restClient.getIssueClient().addAttachment(new NullProgressMonitor(), getIssue(issueKey).getAttachmentsUri(),
				new ByteArrayInputStream(content), filename);
	}

	public InputStream getAttachment(URI attachmentUri) {
		return restClient.getIssueClient().getAttachment(new NullProgressMonitor(), attachmentUri);
	}

	public Project[] getProjects() {
		Iterable<BasicProject> allProjects = restClient.getProjectClient().getAllProjects(new NullProgressMonitor());

		return JiraRestConverter.convertProjects(allProjects);
	}

	public NamedFilter[] getFavouriteFilters() {

		return JiraRestConverter.convertNamedFilters(restClient.getSearchClient().getFavouriteFilters(
				new NullProgressMonitor()));
	}

	public Resolution[] getResolutions() {
		return JiraRestConverter.convertResolutions(restClient.getMetadataClient().getResolutions(
				new NullProgressMonitor()));
	}

	public Priority[] getPriorities() {
		return JiraRestConverter.convertPriorities(restClient.getMetadataClient().getPriorities(
				new NullProgressMonitor()));
	}

	public JiraIssue getIssueByKeyOrId(String issueKeyOrId, IProgressMonitor monitor) throws JiraException {
		return JiraRestConverter.convertIssue(getIssue(issueKeyOrId), cache, url, monitor);
	}

	public JiraStatus[] getStatuses() {
		return JiraRestConverter.convertStatuses(restClient.getMetadataClient().getStatuses(new NullProgressMonitor()));
	}

	public IssueType[] getIssueTypes() {
		return JiraRestConverter.convertIssueTypes(restClient.getMetadataClient().getIssueTypes(
				new NullProgressMonitor()));
	}

	public IssueType[] getIssueTypes(String projectKey) {
		return JiraRestConverter.convertIssueTypes(restClient.getProjectClient()
				.getProject(projectKey, new NullProgressMonitor())
				.getIssueTypes());
	}

	public List<JiraIssue> getIssues(final String jql, final int maxSearchResult, final IProgressMonitor monitor)
			throws JiraException {

		return call(new Callable<List<JiraIssue>>() {

			public List<JiraIssue> call() throws Exception {
				List<JiraIssue> issues = JiraRestConverter.convertIssues(restClient.getSearchClient()
						.searchJql(jql, maxSearchResult, 0, new NullProgressMonitor())
						.getIssues());

				List<JiraIssue> fullIssues = new ArrayList<JiraIssue>();

				for (JiraIssue issue : issues) {
					fullIssues.add(JiraRestConverter.convertIssue(getIssue(issue.getKey()), cache, url, monitor));
				}

				return fullIssues;
			}

		});

	}

//	public Component[] getComponents(String projectKey) {
//		return JiraRestConverter.convertComponents(restClient.getProjectClient()
//				.getProject(projectKey, new NullProgressMonitor())
//				.getComponents());
//	}
//
//	public Version[] getVersions(String projectKey) {
//		return JiraRestConverter.convertVersions(restClient.getProjectClient()
//				.getProject(projectKey, new NullProgressMonitor())
//				.getVersions());
//	}

	public void getProjectDetails(Project project) {

		com.atlassian.jira.rest.client.domain.Project projectWithDetails = restClient.getProjectClient().getProject(
				project.getKey(), new NullProgressMonitor());

		project.setComponents(JiraRestConverter.convertComponents(projectWithDetails.getComponents()));
		project.setVersions(JiraRestConverter.convertVersions(projectWithDetails.getVersions()));
		project.setIssueTypes(JiraRestConverter.convertIssueTypes(projectWithDetails.getIssueTypes()));
	}

	public void addWorklog(String issueKey, JiraWorkLog jiraWorklog) throws JiraException {
		Issue issue = getIssue(issueKey);
		restClient.getIssueClient().addWorklog(issue.getWorklogUri(),
				JiraRestConverter.convert(jiraWorklog, issue.getSelf()), new NullProgressMonitor());
	}

	public ServerInfo getServerInfo() throws JiraException {
		return call(new Callable<ServerInfo>() {
			public ServerInfo call() {
				return JiraRestConverter.convert(restClient.getMetadataClient()
						.getServerInfo(new NullProgressMonitor()));
			}
		});
	}

	public Iterable<JiraAction> getTransitions(String issueKey) throws JiraException {

		return JiraRestConverter.convertTransitions(restClient.getIssueClient().getTransitions(getIssue(issueKey),
				new NullProgressMonitor()));
	}

	public void transitionIssue(JiraIssue issue, String transitionKey, String comment,
			Iterable<IssueField> transitionFields) throws JiraException {

		Comment outComment = (StringUtils.isEmpty(comment) ? null : Comment.valueOf(comment));

		List<FieldInput> fields = new ArrayList<FieldInput>();
		for (IssueField transitionField : transitionFields) {
			if (transitionField.isRequired()) {
				String[] values = issue.getFieldValues(transitionField.getName());
				if (values.length > 0 && transitionField.getName().equals(JiraRestFields.SUMMARY)) {
					fields.add(new FieldInput(JiraRestFields.SUMMARY, values[0]));
				} else if (values.length > 0) {
					fields.add(new FieldInput(transitionField.getName(), ComplexIssueInputFieldValue.with(
							JiraRestFields.NAME, values[0])));
				} else {
					throw new JiraException(NLS.bind("Field {0} is required for transition {1}",
							transitionField.getName(), transitionKey));
				}
			}
		}

//		fields.add(new FieldInput("resolution", ComplexIssueInputFieldValue.with("name", "Duplicate")));
//		fields.add(new FieldInput("resolution", new com.atlassian.jira.rest.client.domain.Resolution(null, "Duplicate",
//				null)));

		TransitionInput transitionInput = new TransitionInput(Integer.parseInt(transitionKey), fields, outComment);

		restClient.getIssueClient().transition(getIssue(issue.getKey()), transitionInput, new NullProgressMonitor());

	}

	public void assignIssue(String issueKey, String user, String comment) throws JiraException {
		Issue issue = getIssue(issueKey);

		ImmutableList<FieldInput> fields = ImmutableList.<FieldInput> of(new FieldInput(JiraRestFields.ASSIGNEE,
				ComplexIssueInputFieldValue.with(JiraRestFields.NAME, user)));

		restClient.getIssueClient().update(issue, fields, new NullProgressMonitor());

	}

	/**
	 * @param issue
	 * @return issue key
	 * @throws JiraException
	 */
	public String createIssue(JiraIssue issue) throws JiraException {

//		GetCreateIssueMetadataOptionsBuilder builder = new GetCreateIssueMetadataOptionsBuilder();
//		builder.withExpandedIssueTypesFields().withProjectKeys("TEST");
//
//		Iterable<CimProject> createIssueMetadata = restClient.getIssueClient().getCreateIssueMetadata(builder.build(),
//				new NullProgressMonitor());

		final IssueInputBuilder issueInputBuilder = new IssueInputBuilder(issue.getProject().getKey(),
				Long.parseLong(issue.getType().getId()), issue.getSummary());

		issueInputBuilder.setAffectedVersions(JiraRestConverter.convert(issue.getReportedVersions()))
				.setComponents(JiraRestConverter.convert(issue.getComponents()))
				.setDescription(issue.getDescription());

		if (issue.getAssignee() != null) {
			issueInputBuilder.setAssignee(new BasicUser(null, issue.getAssignee(), null));
		}

		if (issue.getDue() != null) {
			issueInputBuilder.setDueDate(new DateTime(issue.getDue()));
		}

		if (issue.getFixVersions() != null) {
			issueInputBuilder.setFixVersions(JiraRestConverter.convert(issue.getFixVersions()));
		}

		if (issue.getPriority() == null || StringUtils.isEmpty(issue.getPriority().getId())) {
			throw new JiraException("Priority not set");
		}
		issueInputBuilder.setPriority(new BasicPriority(null, Long.valueOf(issue.getPriority().getId()),
				issue.getPriority().getName()));

		issueInputBuilder.setFieldInput(new FieldInput(JiraRestFields.ENVIRONMENT, issue.getEnvironment()));

		Map<String, Object> map = ImmutableMap.<String, Object> builder()
				.put(JiraRestFields.ORIGINAL_ESTIMATE, String.valueOf(issue.getEstimate() / 60))
				.put(JiraRestFields.REMAINING_ESTIMATE, String.valueOf(issue.getEstimate() / 60))
				.build();
		issueInputBuilder.setFieldInput(new FieldInput(JiraRestFields.TIMETRACKING,
				new ComplexIssueInputFieldValue(map)));

		if (issue.getSecurityLevel() != null) {
			issueInputBuilder.setFieldValue(JiraRestFields.SECURITY,
					ComplexIssueInputFieldValue.with(JiraRestFields.ID, issue.getSecurityLevel().getId()));
		}

		if (!StringUtils.isEmpty(issue.getParentKey())) {
			issueInputBuilder.setFieldInput(new FieldInput(JiraRestFields.PARENT, ComplexIssueInputFieldValue.with(
					JiraRestFields.KEY, issue.getParentKey())));
		} else if (!StringUtils.isEmpty(issue.getParentId())) {
			issueInputBuilder.setFieldInput(new FieldInput(JiraRestFields.PARENT, ComplexIssueInputFieldValue.with(
					JiraRestFields.ID, issue.getParentId())));
		}

		return call(new Callable<String>() {

			public String call() throws Exception {
				return restClient.getIssueClient()
						.createIssue(issueInputBuilder.build(), new NullProgressMonitor())
						.getKey();
			}
		});
	}

	public void updateIssue(JiraIssue changedIssue) throws JiraException {
		Issue issue = getIssue(changedIssue.getKey());

		List<FieldInput> fields = new ArrayList<FieldInput>();

		fields.add(new FieldInput(JiraRestFields.ISSUETYPE, ComplexIssueInputFieldValue.with(JiraRestFields.ID,
				changedIssue.getType().getId())));
		fields.add(new FieldInput(JiraRestFields.PRIORITY, ComplexIssueInputFieldValue.with(JiraRestFields.ID,
				changedIssue.getPriority().getId())));

		// TODO rest: how to clear due date?
		String date = new DateTime(changedIssue.getDue()).toString(JiraRestFields.DATE_FORMAT);
		if (changedIssue.getDue() == null) {
			date = null;
		}
		fields.add(new FieldInput(JiraRestFields.DUEDATE, date));

		// we must set original estimate explicitly otherwise it is overwritten by remaining estimate (REST bug) 
		long originalEstimate = changedIssue.getEstimate() / 60;
		if (issue.getTimeTracking().getOriginalEstimateMinutes() != null) {
			originalEstimate = issue.getTimeTracking().getOriginalEstimateMinutes();
		}

		Map<String, Object> map = ImmutableMap.<String, Object> builder()
				.put(JiraRestFields.ORIGINAL_ESTIMATE, String.valueOf(originalEstimate))
				.put(JiraRestFields.REMAINING_ESTIMATE, String.valueOf(changedIssue.getEstimate() / 60))
				.build();

		fields.add(new FieldInput(JiraRestFields.TIMETRACKING, new ComplexIssueInputFieldValue(map)));

		List<ComplexIssueInputFieldValue> reportedVersions = new ArrayList<ComplexIssueInputFieldValue>();
		for (Version version : changedIssue.getReportedVersions()) {
			reportedVersions.add(ComplexIssueInputFieldValue.with(JiraRestFields.ID, version.getId()));
		}
		fields.add(new FieldInput(JiraRestFields.VERSIONS, reportedVersions));

		List<ComplexIssueInputFieldValue> fixVersions = new ArrayList<ComplexIssueInputFieldValue>();
		for (Version version : changedIssue.getFixVersions()) {
			fixVersions.add(ComplexIssueInputFieldValue.with(JiraRestFields.ID, version.getId()));
		}
		fields.add(new FieldInput(JiraRestFields.FIX_VERSIONS, fixVersions));

		List<ComplexIssueInputFieldValue> components = new ArrayList<ComplexIssueInputFieldValue>();
		for (Component component : changedIssue.getComponents()) {
			components.add(ComplexIssueInputFieldValue.with(JiraRestFields.ID, component.getId()));
		}
		fields.add(new FieldInput(JiraRestFields.COMPONENTS, components));

		if (changedIssue.getSecurityLevel() != null) {
			// security level value "-1" clears security level
			fields.add(new FieldInput(JiraRestFields.SECURITY, ComplexIssueInputFieldValue.with(JiraRestFields.ID,
					changedIssue.getSecurityLevel().getId())));
		} else {
			// do not clear security level as it might be not available on the screen
		}

		fields.add(new FieldInput(JiraRestFields.ENVIRONMENT, changedIssue.getEnvironment()));
		fields.add(new FieldInput(JiraRestFields.SUMMARY, changedIssue.getSummary()));
		fields.add(new FieldInput(JiraRestFields.DESCRIPTION, changedIssue.getDescription()));

		fields.add(new FieldInput(JiraRestFields.ASSIGNEE, ComplexIssueInputFieldValue.with(JiraRestFields.NAME,
				changedIssue.getAssignee())));

		restClient.getIssueClient().update(issue, fields, new NullProgressMonitor());

	}

	private <V> V call(Callable<V> callable) throws JiraException {

		try {
			return callable.call();
		} catch (RestClientException e) {
			if (e.getMessage().contains(HTTP_401)) {
				throw new JiraAuthenticationException(HTTP_401);
			} else if (e.getMessage().contains(HTTP_403)) {
				throw new JiraException(HTTP_403 + ". Captcha might be required. Please try to log in via browser."); //$NON-NLS-1$
			} else if (e.getMessage().contains(CONNECTION_REFUSED)) {
				throw new JiraException(CONNECTION_REFUSED, e);
			} else if (e.getMessage().contains(UNKNOWN_HOST_EXCEPTION)) {
				int index = e.getMessage().indexOf(UNKNOWN_HOST_EXCEPTION);
				throw new JiraServiceUnavailableException(e.getMessage().substring(index));
			} else if (e.getMessage().contains(ILLEGAL_ARGUMENT_EXCEPTION)) {
				int index = e.getMessage().indexOf(ILLEGAL_ARGUMENT_EXCEPTION);
				throw new JiraException(e.getMessage().substring(index), e);
			} else if (e.getMessage().contains(HTTP_302)) {
				int index = e.getMessage().indexOf(HTTP_302);
				throw new JiraException(e.getMessage().substring(index) + ". Https might be required."); //$NON-NLS-1$
			} else if (e.getMessage().contains(HTTP_404)) {
				throw new JiraServiceUnavailableException(e);
			} else {
				// use "e.getMessage()" as an argument instead of "e" so it fits error window (mainly TaskRepository dialog) 
				throw new JiraException(e.getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new JiraException(e);
		}

	}

	public SimpleDateFormat getDateTimeFormat() {
		return REST_DATETIME_FORMAT;
	}

	public SimpleDateFormat getDateFormat() {
		return REST_DATE_FORMAT;
	}
}
