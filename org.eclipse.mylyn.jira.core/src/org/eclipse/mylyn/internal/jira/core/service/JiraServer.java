/*******************************************************************************
 * Copyright (c) 2007 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.core.service;

import java.io.File;
import java.net.Proxy;

import org.eclipse.mylar.internal.jira.core.model.Issue;
import org.eclipse.mylar.internal.jira.core.model.IssueType;
import org.eclipse.mylar.internal.jira.core.model.NamedFilter;
import org.eclipse.mylar.internal.jira.core.model.Priority;
import org.eclipse.mylar.internal.jira.core.model.Project;
import org.eclipse.mylar.internal.jira.core.model.Query;
import org.eclipse.mylar.internal.jira.core.model.Resolution;
import org.eclipse.mylar.internal.jira.core.model.ServerInfo;
import org.eclipse.mylar.internal.jira.core.model.Status;
import org.eclipse.mylar.internal.jira.core.model.User;
import org.eclipse.mylar.internal.jira.core.model.Version;
import org.eclipse.mylar.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylar.internal.jira.core.model.filter.IssueCollector;

/**
 * TODO this class needs to be populated using the SOAP or JAX-RPC interfaces.
 * Once this is done it should be cached on disk somewhere so we don't have to
 * query the server each time a client loads. It should be possible to reload
 * and restore the cache information. We also need to store the images in a
 * cache somewhere since we will ue them a lot.
 * 
 * TODO explain this is an attempt to enrich the jira service layer
 * 
 * TODO move all of the assignee stuff somewhere else.
 * 
 * @author Brock Janiczak
 */
public interface JiraServer {
	/**
	 * Assign to the default user
	 */
	public static final int ASSIGNEE_DEFAULT = 1;

	/**
	 * Leave the assignee field as is (this does not apply when performing an
	 * assign to action)
	 */
	public static final int ASSIGNEE_CURRENT = 2;

	/**
	 * Assign to nobody
	 */
	public static final int ASSIGNEE_NONE = 3;

	/**
	 * Assign to a specific user. To get the name of the assignee call
	 * {@link #getAssignee()}
	 */
	public static final int ASSIGNEE_USER = 4;

	/**
	 * Assign to the current user
	 */
	public static final int ASSIGNEE_SELF = 5;

	public abstract boolean hasSlowConnection();

	public abstract String getName();

	public abstract String getBaseURL();

	public abstract String getCurrentUserName();

	public abstract String getCurrentUserPassword();

	/**
	 * Force a login to the remote repository.
	 * 
	 * @deprecated There is no need to call this method as all services should
	 *             automatically login when the session is about to expire. If
	 *             you need to check if the credentials are valid, call
	 *             {@link org.eclipse.mylar.internal.jira.core.ServerManager#testConnection(String, String, String)}
	 */
	public abstract void login();

	/**
	 * Force the current session to be closed. This method should only be called
	 * during application shutdown and then only out of courtesy to the server.
	 * Jira will automatically expire sessions after a set amount of time.
	 */
	public abstract void logout();

	public abstract Project getProjectById(String id);

	public abstract Project getProject(String key);

	public abstract Project[] getProjects();

	public abstract Priority getPriorityById(String id);

	public abstract Priority[] getPriorities();

	public abstract IssueType getIssueTypeById(String id);

	public abstract IssueType[] getIssueTypes();

	public abstract Status getStatusById(String id);

	public abstract Status[] getStatuses();

	public abstract Resolution getResolutionById(String id);

	public abstract Resolution[] getResolutions();

	public abstract User getUserByName(String name);

	public abstract User[] getUsers();

	/**
	 * Finds all issues matching <code>searchString</code> using server
	 * defined matching rules. This query supports smart tags in the expression
	 * 
	 * @deprecated Use {@link #search(Query, IssueCollector) instead
	 * @param searchString
	 *            Value to search for
	 * @param collector
	 *            Colelctor that will process the matching issues
	 */
	public abstract void quickSearch(String searchString, IssueCollector collector);

	/**
	 * Finds issues given a user defined query string
	 * 
	 * @deprecated Use {@link #search(Query, IssueCollector) instead
	 * @param filter
	 *            Custom query to be executed
	 * @param collector
	 *            Reciever for the matching issues
	 */
	public abstract void findIssues(FilterDefinition filter, IssueCollector collector);

	/**
	 * @deprecated Use {@link #search(Query, IssueCollector) instead
	 * @param filter
	 *            Server defined query to execute
	 * @param collector
	 *            Reciever for the matching issues
	 */
	public abstract void executeNamedFilter(NamedFilter filter, IssueCollector collector);

	/**
	 * Retrieve an issue using its unique key
	 * 
	 * @param issueKey
	 *            Unique key of the issue to find
	 * @return Matching issue or <code>null</code> if no matching issue could
	 *         be found
	 */
	public abstract Issue getIssue(String issueKey);

	/**
	 * @param query
	 *            Query to be executed
	 * @param collector
	 *            Reciever for the matching issues
	 */
	public abstract void search(Query query, IssueCollector collector);

	public abstract ServerInfo getServerInfo();

	/**
	 * Retrieve all locally defined filter definitions. These filters are not
	 * konwn to the server and can be seen as 'quick' filters.
	 * 
	 * @return List of all locally defined filters for this server
	 */
	public abstract FilterDefinition[] getLocalFilters();

	public abstract void addLocalFilter(FilterDefinition filter);

	public abstract void removeLocalFilter(String filterName);

	/**
	 * Retrieves all filters that are stored and run on the server. The client
	 * will never be aware of the definition for the filter, only its name and
	 * description
	 * 
	 * @return List of all filters taht are stored and executed on the server
	 */
	public abstract NamedFilter[] getNamedFilters();

	public abstract void addCommentToIssue(Issue issue, String comment);

	public abstract void updateIssue(Issue issue, String comment);

	public abstract void assignIssueTo(Issue issue, int assigneeType, String user, String comment);

	public abstract void startIssue(Issue issue, String comment, String user);

	public abstract void stopIssue(Issue issue, String comment, String user);

	public abstract void resolveIssue(Issue issue, Resolution resolution, Version[] fixVersions, String comment,
			int assigneeType, String user);

	public abstract void closeIssue(Issue issue, Resolution resolution, Version[] fixVersions, String comment,
			int assigneeType, String user);

	public abstract void reopenIssue(Issue issue, String comment, int assigneeType, String user);

	public abstract void attachFile(Issue issue, String comment, String filename, byte[] contents, String contentType);

	public abstract void attachFile(Issue issue, String comment, File file, String contentType);

	/**
	 * Creates an issue with the details specified in <code>issue</code>. The
	 * following fields are mandatory:
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
	 * @return A fully populated {@link org.eclipse.mylar.internal.jira.core.model.Issue}
	 *         containing the details of the new issue
	 */
	public abstract Issue createIssue(Issue issue);

	/**
	 * Begin watching <code>issue</code>. Nothing will happen if the user is
	 * already watching the issue.
	 * 
	 * @param issue
	 *            Issue to begin watching
	 */
	public abstract void watchIssue(Issue issue);

	/**
	 * Stop watching <code>issue</code>. Nothing will happen if the user is
	 * not currently watching the issue.
	 * 
	 * @param issue
	 *            Issue to stop watching
	 */
	public abstract void unwatchIssue(Issue issue);

	/**
	 * Vote for <code>issue</code>. Issues can only be voted on if the issue
	 * was not raied by the current user and is not resolved. Before calling
	 * this method, ensure it is valid to vote by calling
	 * {@link org.eclipse.mylar.internal.jira.core.model.Issue#canUserVote(String)}. If it is
	 * not valid for the user to vote for an issue this method will do nothing.
	 * 
	 * @param issue
	 *            Issue to vote for
	 */
	public abstract void voteIssue(Issue issue);

	/**
	 * Revoke vote for <code>issue</code>. Issues can only be voted on if the
	 * issue was not raied by the current user and is not resolved. Before
	 * calling this method, ensure it is valid to vote by calling
	 * {@link org.eclipse.mylar.internal.jira.core.model.Issue#canUserVote(String)}. If it is
	 * not valid for the user to vote for an issue this method will do nothing.
	 * 
	 * @param issue
	 *            Issue to remove vote from
	 */
	public abstract void unvoteIssue(Issue issue);

	/**
	 * Refresh any cached information with the latest values from the remote
	 * server. This operation may take a long time to complete and should not be
	 * called from a UI thread.
	 */
	public abstract void refreshDetails();

	public abstract Proxy getProxy();

	public abstract String getHttpUser();

	public abstract String getHttpPassword();
	
	public abstract void setProxy(Proxy proxy);
	
}
