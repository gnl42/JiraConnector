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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.internal.jira.core.model.Attachment;
import org.eclipse.mylyn.internal.jira.core.model.CustomField;
import org.eclipse.mylyn.internal.jira.core.model.Issue;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.NamedFilter;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Query;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.ServerInfo;
import org.eclipse.mylyn.internal.jira.core.model.Status;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueCollector;
import org.eclipse.mylyn.tasks.core.RepositoryOperation;
import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylyn.web.core.AbstractWebLocation;

/**
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
public interface JiraClient {

	String DEFAULT_CHARSET = "UTF-8";
	
	/**
	 * Assign to the default user
	 */
	int ASSIGNEE_DEFAULT = 1;

	/**
	 * Leave the assignee field as is (this does not apply when performing an assign to action)
	 */
	int ASSIGNEE_CURRENT = 2;

	/**
	 * Assign to nobody
	 */
	int ASSIGNEE_NONE = 3;

	/**
	 * Assign to a specific user. To get the name of the assignee call {@link #getAssignee()}
	 */
	int ASSIGNEE_USER = 4;

	/**
	 * Assign to the current user
	 */
	int ASSIGNEE_SELF = 5;

	boolean useCompression();

	String getBaseUrl();

	String getUserName();

	/**
	 * Force a login to the remote repository.
	 * 
	 * @deprecated There is no need to call this method as all services should automatically login when the session is
	 *             about to expire. If you need to check if the credentials are valid, call
	 *             {@link org.eclipse.mylyn.internal.jira.core.JiraClientManager#testConnection(String, String, String)}
	 */
	@Deprecated
	void login() throws JiraException;

	/**
	 * Force the current session to be closed. This method should only be called during application shutdown and then
	 * only out of courtesy to the server. Jira will automatically expire sessions after a set amount of time.
	 */
	void logout();

	Project getProjectById(String id);

	Project getProjectByKey(String key);

	Project[] getProjects();

	Priority getPriorityById(String id);

	Priority[] getPriorities();

	IssueType getIssueTypeById(String id);

	IssueType[] getIssueTypes();

	Status getStatusById(String id);

	Status[] getStatuses();

	Resolution getResolutionById(String id);

	Resolution[] getResolutions();

	// disabled since these methods will not return correct results
//	User getUserByName(String name);
//
//	User[] getUsers();

	// disabled deprecated API
//	/**
//	 * Finds all issues matching <code>searchString</code> using server
//	 * defined matching rules. This query supports smart tags in the expression
//	 * 
//	 * @deprecated Use {@link #search(Query, IssueCollector) instead
//	 * @param searchString
//	 *            Value to search for
//	 * @param collector
//	 *            Colelctor that will process the matching issues
//	 */
//	void quickSearch(String searchString, IssueCollector collector);
//
//	/**
//	 * Finds issues given a user defined query string
//	 * 
//	 * @deprecated Use {@link #search(Query, IssueCollector) instead
//	 * @param filter
//	 *            Custom query to be executed
//	 * @param collector
//	 *            Reciever for the matching issues
//	 */
//	void findIssues(FilterDefinition filter, IssueCollector collector);
//
//	/**
//	 * @deprecated Use {@link #search(Query, IssueCollector) instead
//	 * @param filter
//	 *            Server defined query to execute
//	 * @param collector
//	 *            Reciever for the matching issues
//	 */
//	void executeNamedFilter(NamedFilter filter, IssueCollector collector);

	/**
	 * Retrieve an issue using its unique key
	 * 
	 * @param issueKey
	 *            Unique key of the issue to find
	 * @return Matching issue or <code>null</code> if no matching issue could be found
	 */
	Issue getIssueByKey(String issueKey) throws JiraException;

	/**
	 * Retrieve an issue using its unique id
	 * 
	 * @param issueKey
	 *            Unique id of the issue to find
	 * @return Matching issue or <code>null</code> if no matching issue could be found
	 */
	Issue getIssueById(String issue) throws JiraException;

	/**
	 * Returns the corresponding key for <code>issueId</code>.
	 * 
	 * @param issueId
	 *            unique id of the issue
	 * @return corresponding key or <code>null</code> if the id was not found
	 */
	String getKeyFromId(final String issueId) throws JiraException;

	/**
	 * Returns available operations for <code>issueKey</code>
	 * 
	 * @param issueKey
	 *            Unique key of the issue to find
	 * @return corresponding array of <code>RepositoryOperation</code> objects or <code>null</code>.
	 */
	RepositoryOperation[] getAvailableOperations(final String issueKey) throws JiraException;

	/**
	 * Returns fields for given action id
	 * 
	 * @param issueKey
	 *            Unique key of the issue to find
	 * @param actionId
	 *            Unique id for action to get fields for
	 * @return array of field ids for given actionId
	 */
	String[] getActionFields(final String issueKey, final String actionId) throws JiraException;

	/**
	 * Returns editable attributes for <code>issueKey</code>
	 * 
	 * @param issueKey
	 *            Unique key of the issue to find
	 * @return corresponding array of <code>RepositoryTaskAttribute</code> objects or <code>null</code>.
	 */
	RepositoryTaskAttribute[] getEditableAttributes(final String issueKey) throws JiraException;

	CustomField[] getCustomAttributes() throws JiraException;

	/**
	 * @param query
	 *            Query to be executed
	 * @param collector
	 *            Reciever for the matching issues
	 */
	void search(Query query, IssueCollector collector) throws JiraException;

	ServerInfo getServerInfo() throws JiraException;

	/**
	 * Retrieve all locally defined filter definitions. These filters are not konwn to the server and can be seen as
	 * 'quick' filters.
	 * 
	 * @return List of all locally defined filters for this server
	 */
//	FilterDefinition[] getLocalFilters();
//
//	void addLocalFilter(FilterDefinition filter);
//
//	void removeLocalFilter(String filterName);
	/**
	 * Retrieves all filters that are stored and run on the server. The client will never be aware of the definition for
	 * the filter, only its name and description
	 * 
	 * @return List of all filters taht are stored and executed on the server
	 */
	NamedFilter[] getNamedFilters() throws JiraException;

	void addCommentToIssue(Issue issue, String comment) throws JiraException;

	void updateIssue(Issue issue, String comment) throws JiraException;

	void assignIssueTo(Issue issue, int assigneeType, String user, String comment) throws JiraException;

	void advanceIssueWorkflow(Issue issue, String actionKey, String comment) throws JiraException;

//    @Deprecated
//	void startIssue(Issue issue) throws JiraException;
//
//    @Deprecated
//	void stopIssue(Issue issue) throws JiraException;
//
//    @Deprecated
//	void resolveIssue(Issue issue, Resolution resolution, Version[] fixVersions, String comment,
//			int assigneeType, String user) throws JiraException;
//
//	@Deprecated
//	void closeIssue(Issue issue, Resolution resolution, Version[] fixVersions, String comment,
//			int assigneeType, String user) throws JiraException;
//
//	@Deprecated
//	void reopenIssue(Issue issue, String comment, int assigneeType, String user) throws JiraException;

	void attachFile(Issue issue, String comment, PartSource partSource, String contentType) throws JiraException;

	void attachFile(Issue issue, String comment, String filename, byte[] contents, String contentType)
			throws JiraException;

	void attachFile(Issue issue, String comment, String filename, File file, String contentType) throws JiraException;

	byte[] retrieveFile(Issue issue, Attachment attachment) throws JiraException;

	void retrieveFile(Issue issue, Attachment attachment, OutputStream out) throws JiraException;

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
	Issue createIssue(Issue issue) throws JiraException;

	/**
	 * See {@link #createIssue(Issue)} for mandatory attributes of <code>issue</code>. Additionally the
	 * <code>parentIssueId</code> must be set.
	 */
	Issue createSubTask(Issue issue) throws JiraException;
	
	/**
	 * Begin watching <code>issue</code>. Nothing will happen if the user is already watching the issue.
	 * 
	 * @param issue
	 *            Issue to begin watching
	 */
	void watchIssue(Issue issue) throws JiraException;

	/**
	 * Stop watching <code>issue</code>. Nothing will happen if the user is not currently watching the issue.
	 * 
	 * @param issue
	 *            Issue to stop watching
	 */
	void unwatchIssue(Issue issue) throws JiraException;

	/**
	 * Vote for <code>issue</code>. Issues can only be voted on if the issue was not raied by the current user and is
	 * not resolved. Before calling this method, ensure it is valid to vote by calling
	 * {@link org.eclipse.mylyn.internal.jira.core.model.Issue#canUserVote(String)}. If it is not valid for the user to
	 * vote for an issue this method will do nothing.
	 * 
	 * @param issue
	 *            Issue to vote for
	 */
	void voteIssue(Issue issue) throws JiraException;

	/**
	 * Revoke vote for <code>issue</code>. Issues can only be voted on if the issue was not raied by the current user
	 * and is not resolved. Before calling this method, ensure it is valid to vote by calling
	 * {@link org.eclipse.mylyn.internal.jira.core.model.Issue#canUserVote(String)}. If it is not valid for the user to
	 * vote for an issue this method will do nothing.
	 * 
	 * @param issue
	 *            Issue to remove vote from
	 */
	void unvoteIssue(Issue issue) throws JiraException;

	/**
	 * Refresh any cached information with the latest values from the remote server. This operation may take a long time
	 * to complete and should not be called from a UI thread.
	 */
	void refreshDetails(IProgressMonitor monitor) throws JiraException;

	void refreshServerInfo(IProgressMonitor monitor) throws JiraException;

	boolean hasDetails();

	String getCharacterEncoding() throws JiraException;

	AbstractWebLocation getLocation();
	
}
