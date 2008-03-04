/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service;

import java.io.File;
import java.io.OutputStream;

import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.jira.core.model.Attachment;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.CustomField;
import org.eclipse.mylyn.internal.jira.core.model.Issue;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraVersion;
import org.eclipse.mylyn.internal.jira.core.model.NamedFilter;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Query;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.ServerInfo;
import org.eclipse.mylyn.internal.jira.core.model.Status;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.model.WebServerInfo;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueCollector;
import org.eclipse.mylyn.internal.jira.core.model.filter.SingleIssueCollector;
import org.eclipse.mylyn.internal.jira.core.model.filter.SmartQuery;
import org.eclipse.mylyn.internal.jira.core.service.soap.JiraSoapClient;
import org.eclipse.mylyn.internal.jira.core.service.web.JiraWebClient;
import org.eclipse.mylyn.internal.jira.core.service.web.rss.JiraRssClient;
import org.eclipse.mylyn.tasks.core.RepositoryOperation;
import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylyn.web.core.AbstractWebLocation;
import org.eclipse.mylyn.web.core.AuthenticationCredentials;
import org.eclipse.mylyn.web.core.AuthenticationType;

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

	private String characterEncoding;

	private final JiraRssClient filterService;

	private final JiraWebClient issueService;

	private final AbstractWebLocation location;

	private final JiraSoapClient soapService;

	private final boolean useCompression;

	public JiraClient(AbstractWebLocation location, boolean useCompression) {
		Assert.isNotNull(location);

		this.baseUrl = location.getUrl();
		this.location = location;
		this.useCompression = useCompression;

		this.cache = new JiraClientCache(this);

		this.filterService = new JiraRssClient(this);
		this.issueService = new JiraWebClient(this);
		this.soapService = new JiraSoapClient(this);
	}

	public void addCommentToIssue(Issue issue, String comment) throws JiraException {
		issueService.addCommentToIssue(issue, comment);
	}

	public void advanceIssueWorkflow(Issue issue, String actionKey, String comment) throws JiraException {
		String[] fields = getActionFields(issue.getKey(), actionKey);
		issueService.advanceIssueWorkflow(issue, actionKey, comment, fields);
	}

	public void assignIssueTo(Issue issue, int assigneeType, String user, String comment) throws JiraException {
		issueService.assignIssueTo(issue, assigneeType, user, comment);
	}

	public void attachFile(Issue issue, String comment, PartSource partSource, String contentType) throws JiraException {
		issueService.attachFile(issue, comment, partSource, contentType);
	}

	public void attachFile(Issue issue, String comment, String filename, byte[] contents, String contentType)
			throws JiraException {
		issueService.attachFile(issue, comment, filename, contents, contentType);
	}

	public void attachFile(Issue issue, String comment, String filename, File file, String contentType)
			throws JiraException {
		issueService.attachFile(issue, comment, filename, file, contentType);
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
	 * @return A fully populated {@link org.eclipse.mylyn.internal.jira.core.model.Issue} containing the details of the
	 *         new issue
	 */
	public Issue createIssue(Issue issue) throws JiraException {
		String issueKey = issueService.createIssue(issue);
		return getIssueByKey(issueKey);
	}

	/**
	 * See {@link #createIssue(Issue)} for mandatory attributes of <code>issue</code>. Additionally the
	 * <code>parentIssueId</code> must be set.
	 */
	public Issue createSubTask(Issue issue) throws JiraException {
		String issueKey = issueService.createSubTask(issue);
		return getIssueByKey(issueKey);
	}

	public void deleteIssue(Issue issue) throws JiraException {
		issueService.deleteIssue(issue);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof JiraClient) {
			return getBaseUrl().equals(((JiraClient) obj).getBaseUrl());
		}
		return false;
	}

	public void executeNamedFilter(NamedFilter filter, IssueCollector collector) throws JiraException {
		filterService.executeNamedFilter(filter, collector);
	}

	public void findIssues(FilterDefinition filterDefinition, IssueCollector collector) throws JiraException {
		filterService.findIssues(filterDefinition, collector);
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
	public String[] getActionFields(final String issueKey, final String actionId) throws JiraException {
		return soapService.getActionFields(issueKey, actionId);
	}

	/**
	 * Returns available operations for <code>issueKey</code>
	 * 
	 * @param issueKey
	 *            Unique key of the issue to find
	 * @return corresponding array of <code>RepositoryOperation</code> objects or <code>null</code>.
	 */
	public RepositoryOperation[] getAvailableOperations(final String issueKey) throws JiraException {
		return soapService.getAvailableOperations(issueKey);
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public JiraClientCache getCache() {
		return cache;
	}

	public String getCharacterEncoding() throws JiraException {
		if (this.characterEncoding == null) {
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
		return this.characterEncoding;
	}

	public Component[] getComponents(String projectKey) throws JiraException {
		return soapService.getComponents(projectKey);
	}

	public CustomField[] getCustomAttributes() throws JiraException {
		return soapService.getCustomAttributes();
	}

	/**
	 * Returns editable attributes for <code>issueKey</code>
	 * 
	 * @param issueKey
	 *            Unique key of the issue to find
	 * @return corresponding array of <code>RepositoryTaskAttribute</code> objects or <code>null</code>.
	 */
	public RepositoryTaskAttribute[] getEditableAttributes(final String issueKey) throws JiraException {
		// work around for bug 205015
		String version = getCache().getServerInfo().getVersion();
		boolean workAround = (new JiraVersion(version).compareTo(JiraVersion.JIRA_3_12) < 0);
		return soapService.getEditableAttributes(issueKey, workAround);
	}

	/**
	 * Retrieve an issue using its unique key
	 * 
	 * @param issueKey
	 *            Unique key of the issue to find
	 * @return Matching issue or <code>null</code> if no matching issue could be found
	 */
	public Issue getIssueByKey(String issueKey) throws JiraException {
		SingleIssueCollector collector = new SingleIssueCollector();
		filterService.getIssueByKey(issueKey, collector);
		if (collector.getIssue() != null && collector.getIssue().getProject() == null) {
			throw new JiraException("Repository returned an unknown project for issue '"
					+ collector.getIssue().getKey() + "'");
		}
		return collector.getIssue();
	}

	public IssueType[] getIssueTypes() throws JiraException {
		return soapService.getIssueTypes();
	}

	/**
	 * Returns the corresponding key for <code>issueId</code>.
	 * 
	 * @param issueId
	 *            unique id of the issue
	 * @return corresponding key or <code>null</code> if the id was not found
	 */
	public String getKeyFromId(final String issueId) throws JiraException {
		return soapService.getKeyFromId(issueId);
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
	public NamedFilter[] getNamedFilters() throws JiraException {
		return soapService.getNamedFilters();
	}

	public Priority[] getPriorities() throws JiraException {
		return soapService.getPriorities();
	}

	public Project[] getProjects() throws JiraException {
		String version = getCache().getServerInfo().getVersion();
		if (new JiraVersion(version).compareTo(JiraVersion.JIRA_3_4) >= 0) {
			return soapService.getProjectsNoSchemes();
		} else {
			return soapService.getProjects();
		}
	}

	public Resolution[] getResolutions() throws JiraException {
		return soapService.getResolutions();
	}

	public ServerInfo getServerInfo(IProgressMonitor monitor) throws JiraException {
		// get server information through SOAP
		ServerInfo serverInfo = soapService.getServerInfo(monitor);

		// get character encoding through web
		WebServerInfo webServerInfo = issueService.getWebServerInfo();
		serverInfo.setCharacterEncoding(webServerInfo.getCharacterEncoding());
		serverInfo.setWebBaseUrl(webServerInfo.getBaseUrl());

		return serverInfo;
	}

	public JiraSoapClient getSoapClient() {
		return soapService;
	}

	public Status[] getStatuses() throws JiraException {
		return soapService.getStatuses();
	}

	public IssueType[] getSubTaskIssueTypes() throws JiraException {
		return soapService.getSubTaskIssueTypes();
	}

	public String getUserName() {
		AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
		return (credentials != null) ? credentials.getUserName() : "";
	}

	public Version[] getVersions(String key) throws JiraException {
		return soapService.getVersions(key);
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
	public void login() throws JiraException {
		soapService.login();
	}

	/**
	 * Force the current session to be closed. This method should only be called during application shutdown and then
	 * only out of courtesy to the server. Jira will automatically expire sessions after a set amount of time.
	 */
	public void logout() {
		soapService.logout();
	}

	public void quickSearch(String searchString, IssueCollector collector) throws JiraException {
		filterService.quickSearch(searchString, collector);

	}

	public byte[] retrieveFile(Issue issue, Attachment attachment) throws JiraException {
		byte[] result = new byte[(int) attachment.getSize()];
		issueService.retrieveFile(issue, attachment, result);
		return result;
	}

	public void retrieveFile(Issue issue, Attachment attachment, OutputStream out) throws JiraException {
		issueService.retrieveFile(issue, attachment, out);
	}

	/**
	 * @param query
	 *            Query to be executed
	 * @param collector
	 *            Reciever for the matching issues
	 */
	public void search(Query query, IssueCollector collector) throws JiraException {
		if (query instanceof SmartQuery) {
			quickSearch(((SmartQuery) query).getKeywords(), collector);
		} else if (query instanceof FilterDefinition) {
			findIssues((FilterDefinition) query, collector);
		} else if (query instanceof NamedFilter) {
			executeNamedFilter((NamedFilter) query, collector);
		} else {
			throw new IllegalArgumentException("Unknown query type: " + query.getClass());
		}
	}

	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	@Override
	public String toString() {
		return getBaseUrl();
	}

	/**
	 * Revoke vote for <code>issue</code>. Issues can only be voted on if the issue was not raied by the current user
	 * and is not resolved. Before calling this method, ensure it is valid to vote by calling
	 * {@link org.eclipse.mylyn.internal.jira.core.model.Issue#canUserVote(String)}. If it is not valid for the user to
	 * vote for an issue this method will do nothing.
	 * 
	 * @param issue
	 *            Issue to remove vote from
	 */
	public void unvoteIssue(Issue issue) throws JiraException {
		issueService.unvoteIssue(issue);
	}

	/**
	 * Stop watching <code>issue</code>. Nothing will happen if the user is not currently watching the issue.
	 * 
	 * @param issue
	 *            Issue to stop watching
	 */
	public void unwatchIssue(Issue issue) throws JiraException {
		issueService.unwatchIssue(issue);
	}

	public void updateIssue(Issue issue, String comment) throws JiraException {
		issueService.updateIssue(issue, comment);
	}

	public boolean useCompression() {
		return useCompression;
	}

	/**
	 * Vote for <code>issue</code>. Issues can only be voted on if the issue was not raied by the current user and is
	 * not resolved. Before calling this method, ensure it is valid to vote by calling
	 * {@link org.eclipse.mylyn.internal.jira.core.model.Issue#canUserVote(String)}. If it is not valid for the user to
	 * vote for an issue this method will do nothing.
	 * 
	 * @param issue
	 *            Issue to vote for
	 */
	public void voteIssue(Issue issue) throws JiraException {
		issueService.voteIssue(issue);
	}

	/**
	 * Begin watching <code>issue</code>. Nothing will happen if the user is already watching the issue.
	 * 
	 * @param issue
	 *            Issue to begin watching
	 */
	public void watchIssue(Issue issue) throws JiraException {
		issueService.watchIssue(issue);
	}

}
