/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service;

import java.io.File;
import java.io.OutputStream;

import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.jira.core.model.Attachment;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.CustomField;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraAction;
import org.eclipse.mylyn.internal.jira.core.model.JiraConfiguration;
import org.eclipse.mylyn.internal.jira.core.model.JiraField;
import org.eclipse.mylyn.internal.jira.core.model.JiraFilter;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.JiraVersion;
import org.eclipse.mylyn.internal.jira.core.model.NamedFilter;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.ServerInfo;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.model.WebServerInfo;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueCollector;
import org.eclipse.mylyn.internal.jira.core.model.filter.SingleIssueCollector;
import org.eclipse.mylyn.internal.jira.core.model.filter.TextFilter;
import org.eclipse.mylyn.internal.jira.core.service.soap.JiraSoapClient;
import org.eclipse.mylyn.internal.jira.core.service.web.JiraWebClient;
import org.eclipse.mylyn.internal.jira.core.service.web.rss.JiraRssClient;

/**
 * JIRA server implementation that caches information that is unlikely to change during the session. This server uses a
 * {@link JiraClientData} object to persist the repository configuration. It has life-cycle methods to allow data in the
 * cache to be reloaded.
 * 
 * This interface exposes the full set of services available from a Jira installation. It provides a unified inferface
 * for the SOAP and Web/RSS services available.
 * 
 * TODO this class needs to be populated using the SOAP or JAX-RPC interfaces. Once this is done it should be cached on
 * disk somewhere so we don't have to query the server each time a client loads. It should be possible to reload and
 * restore the cache information. We also need to store the images in a cache somewhere since we will ue them a lot.
 * 
 * TODO explain this is an attempt to enrich the jira service layer
 * 
 * TODO move all of the assignee stuff somewhere else.
 * 
 * @author Brock Janiczak
 * @author Steffen Pingel
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

	public final static String DEFAULT_CHARSET = "UTF-8";

	private boolean attemptedToDetermineCharacterEncoding;

	private final String baseUrl;

	private final JiraClientCache cache;

	private final JiraRssClient rssClient;

	private final JiraWebClient webClient;

	private final AbstractWebLocation location;

	private final JiraSoapClient soapClient;

	private final JiraConfiguration configuration;

	public JiraClient(AbstractWebLocation location, JiraConfiguration configuration) {
		Assert.isNotNull(location);
		Assert.isNotNull(configuration);
		this.baseUrl = location.getUrl();
		this.location = location;
		this.configuration = configuration;

		this.cache = new JiraClientCache(this);
		this.rssClient = new JiraRssClient(this);
		this.webClient = new JiraWebClient(this);
		this.soapClient = new JiraSoapClient(this);
	}

	public JiraClient(AbstractWebLocation location) {
		this(location, new JiraConfiguration());
	}

	public void addCommentToIssue(JiraIssue issue, String comment, IProgressMonitor monitor) throws JiraException {
		webClient.addCommentToIssue(issue, comment, monitor);
	}

	public void advanceIssueWorkflow(JiraIssue issue, String actionKey, String comment, IProgressMonitor monitor)
			throws JiraException {
		String[] fields = getActionFields(issue.getKey(), actionKey, monitor);
		webClient.advanceIssueWorkflow(issue, actionKey, comment, fields, monitor);
	}

	public void assignIssueTo(JiraIssue issue, int assigneeType, String user, String comment, IProgressMonitor monitor)
			throws JiraException {
		webClient.assignIssueTo(issue, assigneeType, user, comment, monitor);
	}

	public void addAttachment(JiraIssue issue, String comment, PartSource partSource, String contentType,
			IProgressMonitor monitor) throws JiraException {
		webClient.attachFile(issue, comment, partSource, contentType, monitor);
	}

	public void addAttachment(JiraIssue issue, String comment, String filename, byte[] contents, String contentType,
			IProgressMonitor monitor) throws JiraException {
		webClient.attachFile(issue, comment, filename, contents, contentType, monitor);
	}

	public void addAttachment(JiraIssue issue, String comment, String filename, File file, String contentType,
			IProgressMonitor monitor) throws JiraException {
		webClient.attachFile(issue, comment, filename, file, contentType, monitor);
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
	 * <li>Assigee (If sufficient permissions)</li>
	 * <li>Reporter (If sufficient permissions)</li>
	 * </ul>
	 * All other fields other fields are not settable at this time
	 * 
	 * @param issue
	 *            Prototype issue used to create the new issue
	 * @return A fully populated {@link org.eclipse.mylyn.internal.jira.core.model.JiraIssue} containing the details of
	 *         the new issue
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
		webClient.deleteIssue(issue, monitor);
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

	public String getCharacterEncoding() throws JiraException {
		if (configuration.getCharacterEncoding() == null) {
			String serverEncoding = getCache().getServerInfo().getCharacterEncoding();
			if (serverEncoding != null) {
				return serverEncoding;
			} else if (!attemptedToDetermineCharacterEncoding) {
				getCache().refreshServerInfo(new NullProgressMonitor());
				serverEncoding = getCache().getServerInfo().getCharacterEncoding();
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

	public JiraConfiguration getConfiguration() {
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
	public JiraField[] getEditableAttributes(final String issueKey, IProgressMonitor monitor) throws JiraException {
		// work around for bug 205015
		String version = getCache().getServerInfo().getVersion();
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
			throw new JiraException("Repository returned an unknown project for issue '"
					+ collector.getIssue().getKey() + "'");
		}
		return collector.getIssue();
	}

	public IssueType[] getIssueTypes(IProgressMonitor monitor) throws JiraException {
		return soapClient.getIssueTypes(monitor);
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
		String version = getCache().getServerInfo().getVersion();
		if (new JiraVersion(version).compareTo(JiraVersion.JIRA_3_4) >= 0) {
			return soapClient.getProjectsNoSchemes(monitor);
		} else {
			return soapClient.getProjects(monitor);
		}
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

	public String getUserName() {
		AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
		return (credentials != null) ? credentials.getUserName() : "";
	}

	public Version[] getVersions(String key, IProgressMonitor monitor) throws JiraException {
		return soapClient.getVersions(key, monitor);
	}

	@Override
	public int hashCode() {
		return getBaseUrl().hashCode();
	}

	/**
	 * Force a login to the remote repository.
	 * 
	 * There is no need to call this method as all services should automatically login when the session is about to
	 * expire. If you need to check if the credentials are valid, call
	 * {@link org.eclipse.mylyn.internal.jira.core.JiraClientManager#testConnection(String, String, String)}
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

	public byte[] getAttachment(JiraIssue issue, Attachment attachment, IProgressMonitor monitor) throws JiraException {
		byte[] result = new byte[(int) attachment.getSize()];
		webClient.retrieveFile(issue, attachment, result, monitor);
		return result;
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
			throw new IllegalArgumentException("Unknown query type: " + query.getClass());
		}
	}

	@Override
	public String toString() {
		return getBaseUrl();
	}

	/**
	 * Revoke vote for <code>issue</code>. Issues can only be voted on if the issue was not raied by the current user
	 * and is not resolved. Before calling this method, ensure it is valid to vote by calling
	 * {@link org.eclipse.mylyn.internal.jira.core.model.JiraIssue#canUserVote(String)}. If it is not valid for the user
	 * to vote for an issue this method will do nothing.
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

	public boolean useCompression() {
		return configuration.isCompressionEnabled();
	}

	/**
	 * Vote for <code>issue</code>. Issues can only be voted on if the issue was not raied by the current user and is
	 * not resolved. Before calling this method, ensure it is valid to vote by calling
	 * {@link org.eclipse.mylyn.internal.jira.core.model.JiraIssue#canUserVote(String)}. If it is not valid for the user
	 * to vote for an issue this method will do nothing.
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

}
