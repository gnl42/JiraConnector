/*******************************************************************************
 * Copyright (c) 2004, 2009 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *     Jacek Jaroczynski - fixes for bug 233757
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.core.service;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;

import com.atlassian.connector.eclipse.internal.jira.core.model.Attachment;
import com.atlassian.connector.eclipse.internal.jira.core.model.Comment;
import com.atlassian.connector.eclipse.internal.jira.core.model.Component;
import com.atlassian.connector.eclipse.internal.jira.core.model.CustomField;
import com.atlassian.connector.eclipse.internal.jira.core.model.IssueField;
import com.atlassian.connector.eclipse.internal.jira.core.model.IssueType;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraAction;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraVersion;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import com.atlassian.connector.eclipse.internal.jira.core.model.NamedFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.Priority;
import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.model.ProjectRole;
import com.atlassian.connector.eclipse.internal.jira.core.model.Resolution;
import com.atlassian.connector.eclipse.internal.jira.core.model.SecurityLevel;
import com.atlassian.connector.eclipse.internal.jira.core.model.ServerInfo;
import com.atlassian.connector.eclipse.internal.jira.core.model.User;
import com.atlassian.connector.eclipse.internal.jira.core.model.Version;
import com.atlassian.connector.eclipse.internal.jira.core.model.WebServerInfo;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.IssueCollector;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.SingleIssueCollector;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.TextFilter;
import com.atlassian.connector.eclipse.internal.jira.core.service.soap.JiraSoapClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.web.JiraWebClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.web.JiraWebSession;
import com.atlassian.connector.eclipse.internal.jira.core.service.web.rss.JiraRssClient;

/**
 * JIRA server implementation that caches information that is unlikely to change during the session. This server uses a
 * {@link JiraClientData} object to persist the repository configuration. It has life-cycle methods to allow data in the
 * cache to be reloaded. This interface exposes the full set of services available from a Jira installation. It provides
 * a unified interface for the SOAP and Web/RSS services available.
 * 
 * @author Brock Janiczak
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
 */
public class JiraClient {

	/**
	 * Leave the assignee field as is (this does not apply when performing an assign to action)
	 */
	public final static int ASSIGNEE_CURRENT = 2;

	/**
	 * Assign to the default user
	 */
	public final static int ASSIGNEE_DEFAULT = 1;

	/**
	 * Assign to nobody
	 */
	public final static int ASSIGNEE_NONE = 3;

	/**
	 * Assign to the current user
	 */
	public final static int ASSIGNEE_SELF = 5;

	/**
	 * Assign to a specific user. To get the name of the assignee call {@link #getAssignee()}
	 */
	public final static int ASSIGNEE_USER = 4;

	public final static String DEFAULT_CHARSET = "UTF-8"; //$NON-NLS-1$

	private boolean attemptedToDetermineCharacterEncoding;

	private final String baseUrl;

	private final JiraClientCache cache;

	private final JiraRssClient rssClient;

	private final JiraWebClient webClient;

	private final AbstractWebLocation location;

	private final JiraSoapClient soapClient;

	private JiraConfiguration configuration;

	private final JiraWebSession webSession;

	public JiraClient(AbstractWebLocation location, JiraConfiguration configuration) {
		Assert.isNotNull(location);
		Assert.isNotNull(configuration);
		this.baseUrl = location.getUrl();
		this.location = location;
		this.configuration = configuration;

		this.cache = new JiraClientCache(this);
		this.webSession = new JiraWebSession(this);
		this.webClient = new JiraWebClient(this, webSession);
		this.rssClient = new JiraRssClient(this, webSession);
		this.soapClient = new JiraSoapClient(this);
	}

	public JiraClient(AbstractWebLocation location) {
		this(location, new JiraConfiguration());
	}

	public void addCommentToIssue(String issueKey, Comment comment, IProgressMonitor monitor) throws JiraException {
		soapClient.addComment(issueKey, comment, monitor);
	}

	public void addCommentToIssue(String issueKey, String comment, IProgressMonitor monitor) throws JiraException {
		Comment cmnt = new Comment();
		cmnt.setComment(comment);
		soapClient.addComment(issueKey, cmnt, monitor);
	}

	public void advanceIssueWorkflow(JiraIssue issue, String actionKey, String comment, IProgressMonitor monitor)
			throws JiraException {
		String[] fields = getActionFields(issue.getKey(), actionKey, monitor);
		webClient.advanceIssueWorkflow(issue, actionKey, comment, fields, monitor);
	}

	public void assignIssueTo(JiraIssue issue, int assigneeType, String user, String comment, IProgressMonitor monitor)
			throws JiraException {
		/*PLE-1188
		soapClient.assignIssueTo(issue.getKey(), getAssigneeParam(issue, assigneeType, user), monitor);

		if (!StringUtils.isEmpty(comment)) {
			addCommentToIssue(issue.getKey(), comment, monitor);
		}*/
		webClient.assignIssueTo(issue, assigneeType, user, comment, monitor);
	}

	public void addAttachment(JiraIssue issue, String comment, String filename, byte[] content, IProgressMonitor monitor)
			throws JiraException {
		soapClient.addAttachmentsToIssue(issue.getKey(), new String[] { filename }, new byte[][] { content }, monitor);

		if (!StringUtils.isEmpty(comment)) {
			addCommentToIssue(issue.getKey(), comment, monitor);
		}
	}

	/**
	 * Creates an issue with the details specified in <code>issue</code>. The following fields are mandatory:
	 * <ul>
	 * <li>Project</li>
	 * <li>Issue Type</li>
	 * <li>Summary</li>
	 * </ul>
	 * The following fields are optional:
	 * <ul>
	 * <li>Priority</li>
	 * <li>Components</li>
	 * <li>Affects Version</li>
	 * <li>Fix Version</li>
	 * <li>Environment</li>
	 * <li>Description</li>
	 * <li>Assignee (If sufficient permissions)</li>
	 * <li>Reporter (If sufficient permissions)</li>
	 * </ul>
	 * All other fields other fields are not settable at this time
	 * 
	 * @param issue
	 *            Prototype issue used to create the new issue
	 * @return A fully populated {@link com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue} containing
	 *         the details of the new issue
	 */
	public JiraIssue createIssue(JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		String issueKey = webClient.createIssue(issue, monitor);
		return getIssueByKey(issueKey, monitor);
	}

	/**
	 * See {@link #createIssue(JiraIssue)} for mandatory attributes of <code>issue</code>. Additionally the
	 * <code>parentIssueId</code> must be set.
	 */
	public JiraIssue createSubTask(JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		String issueKey = webClient.createSubTask(issue, monitor);
		return getIssueByKey(issueKey, monitor);
	}

	public void deleteIssue(JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		soapClient.deleteIssue(issue.getKey(), monitor);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof JiraClient) {
			return getBaseUrl().equals(((JiraClient) obj).getBaseUrl());
		}
		return false;
	}

	public void executeNamedFilter(NamedFilter filter, IssueCollector collector, IProgressMonitor monitor)
			throws JiraException {
		rssClient.executeNamedFilter(filter, collector, monitor);
	}

	public void findIssues(FilterDefinition filterDefinition, IssueCollector collector, IProgressMonitor monitor)
			throws JiraException {
		rssClient.findIssues(filterDefinition, collector, monitor);
	}

	/**
	 * Returns fields for given action id
	 * 
	 * @param issueKey
	 *            Unique key of the issue to find
	 * @param actionId
	 *            Unique id for action to get fields for
	 * @return array of field ids for given actionId
	 */
	public String[] getActionFields(final String issueKey, final String actionId, IProgressMonitor monitor)
			throws JiraException {
		return soapClient.getActionFields(issueKey, actionId, monitor);
	}

	/**
	 * Returns available operations for <code>issueKey</code>
	 * 
	 * @param issueKey
	 *            Unique key of the issue to find
	 * @return corresponding array of <code>RepositoryOperation</code> objects or <code>null</code>.
	 */
	public JiraAction[] getAvailableActions(final String issueKey, IProgressMonitor monitor) throws JiraException {
		return soapClient.getAvailableActions(issueKey, monitor);
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public JiraClientCache getCache() {
		return cache;
	}

	public String getCharacterEncoding(IProgressMonitor monitor) throws JiraException {
		if (configuration.getCharacterEncoding() == null) {
			String serverEncoding = getCache().getServerInfo(monitor).getCharacterEncoding();
			if (serverEncoding != null) {
				return serverEncoding;
			} else if (!attemptedToDetermineCharacterEncoding) {
				getCache().refreshServerInfo(monitor);
				serverEncoding = getCache().getServerInfo(monitor).getCharacterEncoding();
				if (serverEncoding != null) {
					return serverEncoding;
				}
			}
			// fallback
			return DEFAULT_CHARSET;
		}
		return configuration.getCharacterEncoding();
	}

	public Component[] getComponents(String projectKey, IProgressMonitor monitor) throws JiraException {
		return soapClient.getComponents(projectKey, monitor);
	}

	public synchronized JiraConfiguration getConfiguration() {
		return configuration;
	}

	public CustomField[] getCustomAttributes(IProgressMonitor monitor) throws JiraException {
		return soapClient.getCustomAttributes(monitor);
	}

	/**
	 * Returns editable attributes for <code>issueKey</code>
	 * 
	 * @param issueKey
	 *            Unique key of the issue to find
	 * @return corresponding array of <code>RepositoryTaskAttribute</code> objects or <code>null</code>.
	 */
	public IssueField[] getEditableAttributes(final String issueKey, IProgressMonitor monitor) throws JiraException {
		// work around for bug 205015
		String version = getCache().getServerInfo(monitor).getVersion();
		boolean workAround = (new JiraVersion(version).compareTo(JiraVersion.JIRA_3_12) < 0);
		return soapClient.getEditableAttributes(issueKey, workAround, monitor);
	}

	/**
	 * Retrieve an issue using its unique key
	 * 
	 * @param issueKey
	 *            Unique key of the issue to find
	 * @return Matching issue or <code>null</code> if no matching issue could be found
	 */
	public JiraIssue getIssueByKey(String issueKey, IProgressMonitor monitor) throws JiraException {
		SingleIssueCollector collector = new SingleIssueCollector();
		rssClient.getIssueByKey(issueKey, collector, monitor);
		if (collector.getIssue() != null && collector.getIssue().getProject() == null) {
			throw new JiraException("Repository returned an unknown project for issue '" //$NON-NLS-1$
					+ collector.getIssue().getKey() + "'"); //$NON-NLS-1$
		}
		return collector.getIssue();
	}

	public IssueType[] getIssueTypes(IProgressMonitor monitor) throws JiraException {
		return soapClient.getIssueTypes(monitor);
	}

	public IssueType[] getIssueTypes(String projectId, IProgressMonitor monitor) throws JiraException {
		return soapClient.getIssueTypes(projectId, monitor);
	}

	/**
	 * Returns the corresponding key for <code>issueId</code>.
	 * 
	 * @param issueId
	 *            unique id of the issue
	 * @return corresponding key or <code>null</code> if the id was not found
	 */
	public String getKeyFromId(final String issueId, IProgressMonitor monitor) throws JiraException {
		return soapClient.getKeyFromId(issueId, monitor);
	}

	public AbstractWebLocation getLocation() {
		return location;
	}

	/**
	 * Retrieves all filters that are stored and run on the server. The client will never be aware of the definition for
	 * the filter, only its name and description
	 * 
	 * @return List of all filters taht are stored and executed on the server
	 */
	public NamedFilter[] getNamedFilters(IProgressMonitor monitor) throws JiraException {
		return soapClient.getNamedFilters(monitor);
	}

	public Priority[] getPriorities(IProgressMonitor monitor) throws JiraException {
		return soapClient.getPriorities(monitor);
	}

	public Project[] getProjects(IProgressMonitor monitor) throws JiraException {
		return soapClient.getProjects(monitor);
	}

	public Resolution[] getResolutions(IProgressMonitor monitor) throws JiraException {
		return soapClient.getResolutions(monitor);
	}

	public ServerInfo getServerInfo(final IProgressMonitor monitor) throws JiraException {
		// get server information through SOAP
		ServerInfo serverInfo = soapClient.getServerInfo(monitor);

		// get character encoding through web
		WebServerInfo webServerInfo = webClient.getWebServerInfo(monitor);
		serverInfo.setCharacterEncoding(webServerInfo.getCharacterEncoding());
		serverInfo.setWebBaseUrl(webServerInfo.getBaseUrl());

		serverInfo.getStatistics().getStatus().addAll(webServerInfo.getStatistics().getStatus());

		return serverInfo;
	}

	public JiraSoapClient getSoapClient() {
		return soapClient;
	}

	public JiraStatus[] getStatuses(IProgressMonitor monitor) throws JiraException {
		return soapClient.getStatuses(monitor);
	}

	public IssueType[] getSubTaskIssueTypes(IProgressMonitor monitor) throws JiraException {
		return soapClient.getSubTaskIssueTypes(monitor);
	}

	public IssueType[] getSubTaskIssueTypes(String projectId, IProgressMonitor monitor) throws JiraException {
		return soapClient.getSubTaskIssueTypes(projectId, monitor);
	}

	public String getUserName() {
		AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
		return (credentials != null) ? credentials.getUserName() : ""; //$NON-NLS-1$
	}

	/**
	 * Returns a sorted list of versions for the specified project in descended order.
	 * 
	 * @param key
	 *            the project key
	 */
	public Version[] getVersions(String key, IProgressMonitor monitor) throws JiraException {
		List<Version> versions = Arrays.asList(soapClient.getVersions(key, monitor));
		Collections.sort(versions, new Comparator<Version>() {
			public int compare(Version o1, Version o2) {
				return o1.getSequence() > o2.getSequence() ? -1 : (o1.getSequence() == o2.getSequence() ? 0 : 1);
			}
		});
		return versions.toArray(new Version[0]);
	}

	@Override
	public int hashCode() {
		return getBaseUrl().hashCode();
	}

	/**
	 * Force a login to the remote repository. There is no need to call this method as all services should automatically
	 * login when the session is about to expire. If you need to check if the credentials are valid, call
	 * {@link com.atlassian.connector.eclipse.internal.jira.core.JiraClientManager#testConnection(String, String, String)}
	 */
	public void login(IProgressMonitor monitor) throws JiraException {
		soapClient.login(monitor);
	}

	/**
	 * Force the current session to be closed. This method should only be called during application shutdown and then
	 * only out of courtesy to the server. Jira will automatically expire sessions after a set amount of time.
	 */
	public void logout(IProgressMonitor monitor) throws JiraException {
		soapClient.logout(monitor);
	}

	public void quickSearch(String searchString, IssueCollector collector, IProgressMonitor monitor)
			throws JiraException {
		rssClient.quickSearch(searchString, collector, monitor);

	}

	public void getAttachment(JiraIssue issue, Attachment attachment, OutputStream out, IProgressMonitor monitor)
			throws JiraException {
		webClient.retrieveFile(issue, attachment, out, monitor);
	}

	/**
	 * @param query
	 *            Query to be executed
	 * @param collector
	 *            Reciever for the matching issues
	 */
	public void search(JiraFilter query, IssueCollector collector, IProgressMonitor monitor) throws JiraException {
		if (query instanceof TextFilter) {
			quickSearch(((TextFilter) query).getKeywords(), collector, monitor);
		} else if (query instanceof FilterDefinition) {
			findIssues((FilterDefinition) query, collector, monitor);
		} else if (query instanceof NamedFilter) {
			executeNamedFilter((NamedFilter) query, collector, monitor);
		} else {
			throw new IllegalArgumentException("Unknown query type: " + query.getClass()); //$NON-NLS-1$
		}
	}

	@Override
	public String toString() {
		return getBaseUrl();
	}

	/**
	 * Revoke vote for <code>issue</code>. Issues can only be voted on if the issue was not raied by the current user
	 * and is not resolved. Before calling this method, ensure it is valid to vote by calling
	 * {@link com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue#canUserVote(String)}. If it is not
	 * valid for the user to vote for an issue this method will do nothing.
	 * 
	 * @param issue
	 *            Issue to remove vote from
	 */
	public void unvoteIssue(JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		webClient.unvoteIssue(issue, monitor);
	}

	/**
	 * Stop watching <code>issue</code>. Nothing will happen if the user is not currently watching the issue.
	 * 
	 * @param issue
	 *            Issue to stop watching
	 */
	public void unwatchIssue(JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		webClient.unwatchIssue(issue, monitor);
	}

	public void updateIssue(JiraIssue issue, String comment, IProgressMonitor monitor) throws JiraException {
		webClient.updateIssue(issue, comment, monitor);
	}

	/**
	 * Vote for <code>issue</code>. Issues can only be voted on if the issue was not raied by the current user and is
	 * not resolved. Before calling this method, ensure it is valid to vote by calling
	 * {@link com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue#canUserVote(String)}. If it is not
	 * valid for the user to vote for an issue this method will do nothing.
	 * 
	 * @param issue
	 *            Issue to vote for
	 */
	public void voteIssue(JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		webClient.voteIssue(issue, monitor);
	}

	/**
	 * Begin watching <code>issue</code>. Nothing will happen if the user is already watching the issue.
	 * 
	 * @param issue
	 *            Issue to begin watching
	 */
	public void watchIssue(JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		webClient.watchIssue(issue, monitor);
	}

	public JiraWorkLog[] getWorklogs(String issueKey, IProgressMonitor monitor) throws JiraException {
		return soapClient.getWorkLogs(issueKey, monitor);
	}

	public SecurityLevel[] getAvailableSecurityLevels(String projectKey, IProgressMonitor monitor) throws JiraException {
		return soapClient.getAvailableSecurityLevels(projectKey, monitor);
	}

	public JiraWorkLog addWorkLog(String issueKey, JiraWorkLog log, IProgressMonitor monitor) throws JiraException {
		return soapClient.addWorkLog(issueKey, log, monitor);
	}

	public ProjectRole[] getProjectRoles(IProgressMonitor monitor) throws JiraException {
		return soapClient.getProjectRoles(monitor);
	}

	public User[] getProjectRoleUsers(Project project, ProjectRole projectRole, IProgressMonitor monitor)
			throws JiraException {
		return soapClient.getProjectRoleUsers(project, projectRole, monitor);
	}

	public synchronized void setConfiguration(JiraConfiguration configuration) {
		Assert.isNotNull(configuration);
		this.configuration = configuration;
	}

	public boolean isCompressionEnabled() {
		return getConfiguration().isCompressionEnabled();
	}

	public synchronized void purgeSession() {
		webSession.purgeSession();
		soapClient.purgeSession();
	}

	/**
	 * For testing only.
	 * 
	 * @return
	 */
	public JiraWebSession getWebSession() {
		return webSession;
	}

	public String getAssigneeParam(JiraIssue issue, int assigneeType, String user) {
		switch (assigneeType) {
		case JiraClient.ASSIGNEE_CURRENT:
			return issue.getAssignee();
		case JiraClient.ASSIGNEE_DEFAULT:
			return "-1"; //$NON-NLS-1$
		case JiraClient.ASSIGNEE_NONE:
			return ""; //$NON-NLS-1$
		case JiraClient.ASSIGNEE_SELF:
			return getUserName();
		case JiraClient.ASSIGNEE_USER:
			return user;
		default:
			return user;
		}
	}

}
