/*******************************************************************************
 * Copyright (c) 2005 Jira Dashboard project.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylar.jira.core.internal.service;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.mylar.jira.core.internal.model.Group;
import org.eclipse.mylar.jira.core.internal.model.Issue;
import org.eclipse.mylar.jira.core.internal.model.IssueType;
import org.eclipse.mylar.jira.core.internal.model.NamedFilter;
import org.eclipse.mylar.jira.core.internal.model.Priority;
import org.eclipse.mylar.jira.core.internal.model.Project;
import org.eclipse.mylar.jira.core.internal.model.Query;
import org.eclipse.mylar.jira.core.internal.model.Resolution;
import org.eclipse.mylar.jira.core.internal.model.ServerInfo;
import org.eclipse.mylar.jira.core.internal.model.Status;
import org.eclipse.mylar.jira.core.internal.model.User;
import org.eclipse.mylar.jira.core.internal.model.Version;
import org.eclipse.mylar.jira.core.internal.model.filter.FilterDefinition;
import org.eclipse.mylar.jira.core.internal.model.filter.IssueCollector;
import org.eclipse.mylar.jira.core.internal.model.filter.SmartQuery;
import org.eclipse.mylar.jira.core.internal.service.JiraServer;
import org.eclipse.mylar.jira.core.internal.service.JiraService;
import org.eclipse.mylar.jira.core.internal.service.ServiceManager;

/**
 * Jira server implementation that caches information that is unlikey to change
 * during the session. This server could be persisted to disk and re-loaded. It
 * has lifecycle methods to allow data in the cache to be reloaded.
 * 
 * TODO it is assumed that it will be backed by a standad jira service layer
 */
public class CachedRpcJiraServer implements JiraServer, Serializable {
	private static final long serialVersionUID = 1L;

	private transient JiraService serviceDelegate;

	private Map localFilters = new HashMap();

	private transient Object projectLock = new Object();

	private Project[] projects;

	private Map projectsById;

	private Map projectsByKey;

	private transient Object prioritiesLock = new Object();

	private Priority[] priorities;

	private Map prioritiesById;

	private transient Object issueTypesLock = new Object();

	private IssueType[] issueTypes;

	private Map issueTypesById;

	private transient Object resolutionsLock = new Object();

	private Resolution[] resolutions;

	private Map resolutionsById;

	private transient Object statusesLock = new Object();

	private Status[] statuses;

	private Map statusesById;

	private transient Object groupsLock = new Object();

	private Group[] groups;

	private User[] users;

	private Map usersByName;

	private transient Object serverInfoLock = new Object();

	private ServerInfo serverInfo;

	private String baseURL;

	private String username;

	private String password;

	private String name;

	private boolean hasSlowConnection;

	public CachedRpcJiraServer(String name, String baseURL, boolean hasSlowConnection, String username, String password) {
		this.name = name;
		this.baseURL = baseURL;
		this.hasSlowConnection = hasSlowConnection;
		this.username = username;
		this.password = password;

		this.serviceDelegate = ServiceManager.getJiraService(this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.jira.core.internal.service.JiraServer#refreshDetails()
	 */
	public void refreshDetails() {
		projects = null;
		priorities = null;
		issueTypes = null;
		resolutions = null;
		statuses = null;
		groups = null;
		users = null;
		serverInfo = null;

		initializeProjects();
		initializePriorities();
		initializeIssueTypes();
		initializeResolutions();
		initializeStatuses();
		initializeServerInfo();

	}

	public void setSlowConnection(boolean hasSlowConnection) {
		this.hasSlowConnection = hasSlowConnection;
	}

	public boolean hasSlowConnection() {
		return this.hasSlowConnection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraServer#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraServer#login()
	 */
	public void login() {
		serviceDelegate.login(username, password);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraServer#logout()
	 */
	public void logout() {
		serviceDelegate.logout();
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraServer#getBaseURL()
	 */
	public String getBaseURL() {
		return baseURL;
	}

	public void setCurrentUserName(String username) {
		this.username = username;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraServer#getCurrentUserName()
	 */
	public String getCurrentUserName() {
		return this.username;
	}

	public void setCurrentPassword(String password) {
		this.password = password;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraServer#getCurrentUserPassword()
	 */
	public String getCurrentUserPassword() {
		return this.password;
	}

	private void initializeProjects() {
		synchronized (projectLock) {
			if (projects == null) {
				if (getServerInfo().getVersion().compareTo("3.4") >= 0) {
					projects = this.serviceDelegate.getProjectsNoSchemes();
				} else {
					projects = this.serviceDelegate.getProjects();
				}

				projectsById = new HashMap(projects.length);
				projectsByKey = new HashMap(projects.length);

				for (int i = 0; i < projects.length; i++) {
					Project project = projects[i];
					project.setComponents(this.serviceDelegate.getComponents(project.getKey()));
					project.setVersions(this.serviceDelegate.getVersions(project.getKey()));

					projectsById.put(project.getId(), project);
					projectsByKey.put(project.getKey(), project);
				}

			}
		}
	}

	public Project getProjectById(String id) {
		initializeProjects();
		Project project = (Project) projectsById.get(id);
		if (project != null) {
			return project;
		}
		return Project.MISSING_PROJECT;
	}

	public Project getProject(String key) {
		initializeProjects();
		Project project = (Project) projectsByKey.get(key);
		if (project != null) {
			return project;
		}

		return Project.MISSING_PROJECT;
	}

	public Project[] getProjects() {
		initializeProjects();
		return projects;
	}

	private void initializePriorities() {
		synchronized (prioritiesLock) {
			if (priorities == null) {
				;
				priorities = this.serviceDelegate.getPriorities();
				prioritiesById = new HashMap(priorities.length);
				for (int i = 0; i < priorities.length; i++) {
					Priority priority = priorities[i];
					prioritiesById.put(priority.getId(), priority);
				}
			}
		}
	}

	public Priority getPriorityById(String id) {
		initializePriorities();
		Priority priority = (Priority) prioritiesById.get(id);
		if (priority != null) {
			return priority;
		}

		return Priority.MISSING_PRIORITY;
	}

	public Priority[] getPriorities() {
		initializePriorities();
		return priorities;
	}

	private void initializeIssueTypes() {
		synchronized (issueTypesLock) {
			if (issueTypes == null) {
				IssueType[] issueTypes = this.serviceDelegate.getIssueTypes();
				IssueType[] subTaskIssueTypes = this.serviceDelegate.getSubTaskIssueTypes();

				issueTypesById = new HashMap(issueTypes.length + subTaskIssueTypes.length);

				for (int i = 0; i < issueTypes.length; i++) {
					IssueType issueType = issueTypes[i];
					issueTypesById.put(issueType.getId(), issueType);
				}

				for (int i = 0; i < subTaskIssueTypes.length; i++) {
					IssueType issueType = subTaskIssueTypes[i];
					issueTypesById.put(issueType.getId(), issueType);
				}

				this.issueTypes = new IssueType[issueTypes.length + subTaskIssueTypes.length];
				System.arraycopy(issueTypes, 0, this.issueTypes, 0, issueTypes.length);
				System.arraycopy(subTaskIssueTypes, 0, this.issueTypes, issueTypes.length, subTaskIssueTypes.length);

			}
		}
	}

	public IssueType getIssueTypeById(String id) {
		initializeIssueTypes();
		IssueType issueType = (IssueType) issueTypesById.get(id);
		if (issueType != null) {
			return issueType;
		}
		return IssueType.MISSING_ISSUE_TYPE;
	}

	public IssueType[] getIssueTypes() {
		initializeIssueTypes();
		return issueTypes;
	}

	private void initializeStatuses() {
		synchronized (statusesLock) {
			if (statuses == null) {
				;
				statuses = this.serviceDelegate.getStatuses();
				statusesById = new HashMap(statuses.length);
				for (int i = 0; i < statuses.length; i++) {
					Status status = statuses[i];
					statusesById.put(status.getId(), status);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraServer#getStatusById(java.lang.String)
	 */
	public Status getStatusById(String id) {
		initializeStatuses();
		Status status = (Status) statusesById.get(id);
		if (status != null) {
			return status;
		}
		return Status.MISSING_STATUS;
	}

	public Status[] getStatuses() {
		initializeStatuses();
		return statuses;
	}

	private void initializeResolutions() {
		synchronized (resolutionsLock) {
			if (resolutions == null) {
				;
				resolutions = this.serviceDelegate.getResolutions();
				resolutionsById = new HashMap(resolutions.length);
				for (int i = 0; i < resolutions.length; i++) {
					Resolution resolution = resolutions[i];
					resolutionsById.put(resolution.getId(), resolution);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraServer#getResolutionById(java.lang.String)
	 */
	public Resolution getResolutionById(String id) {
		initializeResolutions();
		Resolution resolution = (Resolution) resolutionsById.get(id);
		if (resolution != null) {
			return resolution;
		}
		return Resolution.UNKNOWN_RESOLUTION;
	}

	public Resolution[] getResolutions() {
		initializeResolutions();
		return resolutions;
	}

	private void initializeGroups() {
		synchronized (groupsLock) {
			if (groups == null) {
				;
				// TODO will this exist everywhere?
				groups = new Group[] { this.serviceDelegate.getGroup("jira-users") };
				ArrayList allUsers = new ArrayList();
				for (int i = 0; i < groups[0].getUsers().length; i++) {
					User user = groups[0].getUsers()[i];
					usersByName.put(user.getName(), user);
					allUsers.add(user);
				}
				users = (User[]) allUsers.toArray(new User[allUsers.size()]);
			}
		}
	}

	public Group[] getGroups() {
		initializeGroups();
		return groups;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraServer#getUserByName(java.lang.String)
	 */
	public User getUserByName(String name) {
		initializeGroups();
		return (User) usersByName.get(name);
	}

	public User[] getUsers() {
		initializeGroups();
		return users;
	}

	private void initializeServerInfo() {
		synchronized (serverInfoLock) {
			if (serverInfo == null) {
				serverInfo = this.serviceDelegate.getServerInfo();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.jira.core.internal.service.JiraServer#quickSearch(java.lang.String,
	 *      org.eclipse.mylar.jira.core.internal.model.filter.IssueCollector)
	 */
	public void quickSearch(String searchString, IssueCollector collector) {
		serviceDelegate.quickSearch(searchString, collector);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraServer#findIssues(com.gbst.jira.core.model.filter.FilterDefinition,
	 *      com.gbst.jira.core.model.filter.IssueCollector)
	 */
	public void findIssues(FilterDefinition filter, IssueCollector collector) {
		serviceDelegate.findIssues(filter, collector);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraServer#executeNamedFilter(com.gbst.jira.core.model.NamedFilter,
	 *      com.gbst.jira.core.model.filter.IssueCollector)
	 */
	public void executeNamedFilter(NamedFilter filter, IssueCollector collector) {
		serviceDelegate.executeNamedFilter(filter, collector);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.jira.core.internal.service.JiraServer#getIssue(java.lang.String)
	 */
	public Issue getIssue(String issueKey) {
		return serviceDelegate.getIssue(issueKey);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.jira.core.internal.service.JiraServer#search(org.eclipse.mylar.jira.core.internal.model.Query,
	 *      org.eclipse.mylar.jira.core.internal.model.filter.IssueCollector)
	 */
	public void search(Query query, IssueCollector collector) {
		if (query instanceof SmartQuery) {
			serviceDelegate.quickSearch(((SmartQuery) query).getKeywords(), collector);
		} else if (query instanceof FilterDefinition) {
			serviceDelegate.findIssues((FilterDefinition) query, collector);
		} else if (query instanceof NamedFilter) {
			serviceDelegate.executeNamedFilter((NamedFilter) query, collector);
		} else {
			throw new IllegalArgumentException("Unknown query type: " + query.getClass());
		}

	}

	public ServerInfo getServerInfo() {
		initializeServerInfo();
		return serverInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraServer#getMaximumNumberOfMatches()
	 */
	public int getMaximumNumberOfMatches() {
		return 100;
		// return JiraServer.NO_LIMIT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.jira.core.internal.service.JiraServer#addLocalFilter(org.eclipse.mylar.jira.core.internal.model.filter.FilterDefinition)
	 */
	public void addLocalFilter(FilterDefinition filter) {
		localFilters.put(filter.getName(), filter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.jira.core.internal.service.JiraServer#removeLocalFilter(java.lang.String)
	 */
	public void removeLocalFilter(String filterName) {
		localFilters.remove(filterName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraServer#getLocalFilters()
	 */
	public FilterDefinition[] getLocalFilters() {
		return (FilterDefinition[]) localFilters.values().toArray(new FilterDefinition[localFilters.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraServer#getStoredFilters()
	 */
	public NamedFilter[] getNamedFilters() {
		NamedFilter[] namedFilters = this.serviceDelegate.getSavedFilters();
		for (int i = 0; i < namedFilters.length; i++) {
			NamedFilter namedFilter = namedFilters[i];
		}

		return namedFilters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraServer#addCommentToIssue(com.gbst.jira.core.model.Issue,
	 *      java.lang.String)
	 */
	public void addCommentToIssue(Issue issue, String comment) {
		serviceDelegate.addCommentToIssue(issue, comment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraServer#updateIssue(com.gbst.jira.core.model.Issue,
	 *      java.lang.String)
	 */
	public void updateIssue(Issue issue, String comment) {
		serviceDelegate.updateIssue(issue, comment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraServer#assignIssueTo(com.gbst.jira.core.model.Issue,
	 *      com.gbst.jira.core.model.User)
	 */
	public void assignIssueTo(Issue issue, int assigneeType, String user, String comment) {
		serviceDelegate.assignIssueTo(issue, assigneeType, user, comment);

	}

	public void startIssue(Issue issue, String comment, String user) {
		serviceDelegate.startIssue(issue, comment, user);
	}

	public void stopIssue(Issue issue, String comment, String user) {
		serviceDelegate.stopIssue(issue, comment, user);
	}

	public void resolveIssue(Issue issue, Resolution resolution, Version[] fixVersions, String comment,
			int assigneeType, String user) {
		serviceDelegate.resolveIssue(issue, resolution, fixVersions, comment, assigneeType, user);
	}

	public void closeIssue(Issue issue, Resolution resolution, Version[] fixVersions, String comment, int assigneeType,
			String user) {
		serviceDelegate.closeIssue(issue, resolution, fixVersions, comment, assigneeType, user);
	}

	public void reopenIssue(Issue issue, String comment, int assigneeType, String user) {
		serviceDelegate.reopenIssue(issue, comment, assigneeType, user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.jira.core.internal.service.JiraServer#attachFile(org.eclipse.mylar.jira.core.internal.model.Issue,
	 *      java.lang.String, java.lang.String, byte[])
	 */
	public void attachFile(Issue issue, String comment, String filename, byte[] contents, String contentType) {
		serviceDelegate.attachFile(issue, comment, filename, contents, contentType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.jira.core.internal.service.JiraServer#attachFile(org.eclipse.mylar.jira.core.internal.model.Issue,
	 *      java.lang.String, java.io.File)
	 */
	public void attachFile(Issue issue, String comment, File file, String contentType) {
		serviceDelegate.attachFile(issue, comment, file, contentType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.jira.core.internal.service.JiraServer#createIssue(org.eclipse.mylar.jira.core.internal.model.Issue)
	 */
	public Issue createIssue(Issue issue) {
		return serviceDelegate.createIssue(issue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.jira.core.internal.service.JiraServer#watchIssue(org.eclipse.mylar.jira.core.internal.model.Issue)
	 */
	public void watchIssue(Issue issue) {
		serviceDelegate.watchIssue(issue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.jira.core.internal.service.JiraServer#unwatchIssue(org.eclipse.mylar.jira.core.internal.model.Issue)
	 */
	public void unwatchIssue(Issue issue) {
		serviceDelegate.unwatchIssue(issue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.jira.core.internal.service.JiraServer#voteIssue(org.eclipse.mylar.jira.core.internal.model.Issue)
	 */
	public void voteIssue(Issue issue) {
		serviceDelegate.voteIssue(issue);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.jira.core.internal.service.JiraServer#unvoteIssue(org.eclipse.mylar.jira.core.internal.model.Issue)
	 */
	public void unvoteIssue(Issue issue) {
		serviceDelegate.unvoteIssue(issue);
	}

	private void writeObject(ObjectOutputStream s) throws IOException {
		s.defaultWriteObject();
	}

	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		this.serviceDelegate = ServiceManager.getJiraService(this);

		projectLock = new Object();
		prioritiesLock = new Object();
		issueTypesLock = new Object();
		resolutionsLock = new Object();
		statusesLock = new Object();
		groupsLock = new Object();
		serverInfoLock = new Object();

		// TODO should have a way to read an older version of the file
		if (this.localFilters == null) {
			localFilters = new HashMap();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (!(obj instanceof CachedRpcJiraServer))
			return false;

		CachedRpcJiraServer that = (CachedRpcJiraServer) obj;

		return this.name.equals(that.name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return this.name.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.name;
	}
}
