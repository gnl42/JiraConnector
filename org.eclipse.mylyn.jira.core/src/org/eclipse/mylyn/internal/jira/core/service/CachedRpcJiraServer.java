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
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.mylar.internal.jira.core.model.Issue;
import org.eclipse.mylar.internal.jira.core.model.IssueType;
import org.eclipse.mylar.internal.jira.core.model.NamedFilter;
import org.eclipse.mylar.internal.jira.core.model.Priority;
import org.eclipse.mylar.internal.jira.core.model.Project;
import org.eclipse.mylar.internal.jira.core.model.Query;
import org.eclipse.mylar.internal.jira.core.model.Resolution;
import org.eclipse.mylar.internal.jira.core.model.ServerInfo;
import org.eclipse.mylar.internal.jira.core.model.Status;
import org.eclipse.mylar.internal.jira.core.model.Version;
import org.eclipse.mylar.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylar.internal.jira.core.model.filter.IssueCollector;
import org.eclipse.mylar.internal.jira.core.model.filter.SmartQuery;
import org.eclipse.mylar.internal.jira.core.service.soap.SoapJiraService;

/**
 * JIRA server implementation that caches information that is unlikely to change
 * during the session. This server uses a {@link JiraServerData} object to
 * persist the repository configuration. It has life-cycle methods to allow data
 * in the cache to be reloaded.
 * 
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
public class CachedRpcJiraServer implements JiraServer {

	private volatile JiraServerData data;

	private final JiraService serviceDelegate;

	private final String name;
	
	private final String baseURL;

	private final String username;

	private final String password;

	private final boolean useCompression;

	private final Proxy proxy;

	private final String httpUser;

	private final String httpPassword;

	public CachedRpcJiraServer(String name, String baseURL, boolean useCompression, String username, String password,
			Proxy proxy, String httpUser, String httpPassword) {
		this.name = name;
		this.baseURL = baseURL;
		this.useCompression = useCompression;
		this.username = username;
		this.password = password;

		this.proxy = proxy;
		this.httpUser = httpUser;
		this.httpPassword = httpPassword;

		this.data = new JiraServerData();

		this.serviceDelegate = new SoapJiraService(this);
	}

	public synchronized void refreshDetails(IProgressMonitor monitor) {
		monitor.beginTask("Updating repository configuration", 6);
		
		JiraServerData newData = new JiraServerData();

		initializeServerInfo(newData);
		advance(monitor, 1);
		initializeProjects(newData);
		advance(monitor, 1);
		initializePriorities(newData);
		advance(monitor, 1);
		initializeIssueTypes(newData);
		advance(monitor, 1);
		initializeResolutions(newData);
		advance(monitor, 1);
		initializeStatuses(newData);
		advance(monitor, 1);

		newData.lastUpdate = System.currentTimeMillis();
		this.data = newData;
		
		monitor.done();
	}

	public synchronized void refreshServerInfo(IProgressMonitor monitor) {
		monitor.beginTask("Getting server information", 1);
		
		initializeServerInfo(data);
		advance(monitor, 1);		
	}
	
	private void advance(IProgressMonitor monitor, int worked) {
		monitor.worked(1);
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}
	
	public boolean hasDetails() {
		return data.lastUpdate != 0;
	}
	
	public String getName() {
		return name;
	}

	public void login() {
		serviceDelegate.login(username, password);
	}

	public void logout() {
		serviceDelegate.logout();
	}

	// public void setBaseURL(String baseURL) {
	// this.baseURL = baseURL;
	// }

	public String getBaseURL() {
		return baseURL;
	}

	// public void setCurrentUserName(String username) {
	// this.username = username;
	// }

	public String getCurrentUserName() {
		return this.username;
	}

	// public void setCurrentPassword(String password) {
	// this.password = password;
	// }

	public String getCurrentUserPassword() {
		return this.password;
	}

	private void initializeProjects(JiraServerData data) {
		if (data.serverInfo.getVersion().compareTo("3.4") >= 0) {
			data.projects = this.serviceDelegate.getProjectsNoSchemes();
		} else {
			data.projects = this.serviceDelegate.getProjects();
		}

		data.projectsById = new HashMap<String, Project>(data.projects.length);
		data.projectsByKey = new HashMap<String, Project>(data.projects.length);

		for (int i = 0; i < data.projects.length; i++) {
			Project project = data.projects[i];
			project.setComponents(this.serviceDelegate.getComponents(project.getKey()));
			project.setVersions(this.serviceDelegate.getVersions(project.getKey()));

			data.projectsById.put(project.getId(), project);
			data.projectsByKey.put(project.getKey(), project);
		}
	}

	public Project getProjectById(String id) {
		Project project = data.projectsById.get(id);
		if (project != null) {
			return project;
		}
		return Project.MISSING_PROJECT;
	}

	public Project getProject(String key) {
		Project project = data.projectsByKey.get(key);
		if (project != null) {
			return project;
		}
		return Project.MISSING_PROJECT;
	}

	public Project[] getProjects() {
		return data.projects;
	}

	private void initializePriorities(JiraServerData data) {
		data.priorities = this.serviceDelegate.getPriorities();
		data.prioritiesById = new HashMap<String, Priority>(data.priorities.length);
		for (int i = 0; i < data.priorities.length; i++) {
			Priority priority = data.priorities[i];
			data.prioritiesById.put(priority.getId(), priority);
		}
	}

	public Priority getPriorityById(String id) {
		Priority priority = data.prioritiesById.get(id);
		if (priority != null) {
			return priority;
		}
		return Priority.MISSING_PRIORITY;
	}

	public Priority[] getPriorities() {
		return data.priorities;
	}

	private void initializeIssueTypes(JiraServerData data) {
		IssueType[] issueTypes = this.serviceDelegate.getIssueTypes();
		IssueType[] subTaskIssueTypes; 
		if (data.serverInfo.getVersion().compareTo("3.2") < 0) {
			subTaskIssueTypes = new IssueType[0];
		} else {
			subTaskIssueTypes = this.serviceDelegate.getSubTaskIssueTypes();
		}

		data.issueTypesById = new HashMap<String, IssueType>(issueTypes.length + subTaskIssueTypes.length);

		for (int i = 0; i < issueTypes.length; i++) {
			IssueType issueType = issueTypes[i];
			data.issueTypesById.put(issueType.getId(), issueType);
		}

		for (int i = 0; i < subTaskIssueTypes.length; i++) {
			IssueType issueType = subTaskIssueTypes[i];
			data.issueTypesById.put(issueType.getId(), issueType);
		}

		data.issueTypes = new IssueType[issueTypes.length + subTaskIssueTypes.length];
		System.arraycopy(issueTypes, 0, data.issueTypes, 0, issueTypes.length);
		System.arraycopy(subTaskIssueTypes, 0, data.issueTypes, issueTypes.length, subTaskIssueTypes.length);
	}

	public IssueType getIssueTypeById(String id) {
		IssueType issueType = data.issueTypesById.get(id);
		if (issueType != null) {
			return issueType;
		}
		return IssueType.MISSING_ISSUE_TYPE;
	}

	public IssueType[] getIssueTypes() {
		return data.issueTypes;
	}

	private void initializeStatuses(JiraServerData data) {
		data.statuses = this.serviceDelegate.getStatuses();
		data.statusesById = new HashMap<String, Status>(data.statuses.length);
		for (int i = 0; i < data.statuses.length; i++) {
			Status status = data.statuses[i];
			data.statusesById.put(status.getId(), status);
		}
	}

	public Status getStatusById(String id) {
		Status status = data.statusesById.get(id);
		if (status != null) {
			return status;
		}
		return Status.MISSING_STATUS;
	}

	public Status[] getStatuses() {
		return data.statuses;
	}

	private void initializeResolutions(JiraServerData data) {
		data.resolutions = this.serviceDelegate.getResolutions();
		data.resolutionsById = new HashMap<String, Resolution>(data.resolutions.length);
		for (int i = 0; i < data.resolutions.length; i++) {
			Resolution resolution = data.resolutions[i];
			data.resolutionsById.put(resolution.getId(), resolution);
		}
	}

	public Resolution getResolutionById(String id) {
		Resolution resolution = data.resolutionsById.get(id);
		if (resolution != null) {
			return resolution;
		}
		return Resolution.UNKNOWN_RESOLUTION;
	}

	public Resolution[] getResolutions() {
		return data.resolutions;
	}

	// private void initializeGroups(JiraServerData data) {
	// // TODO will this group exist everywhere?
	// data.groups = new Group[] { this.serviceDelegate.getGroup("jira-users")
	// };
	// ArrayList<User> allUsers = new ArrayList<User>();
	// for (int i = 0; i < data.groups[0].getUsers().length; i++) {
	// User user = data.groups[0].getUsers()[i];
	// data.usersByName.put(user.getName(), user);
	// allUsers.add(user);
	// }
	// data.users = allUsers.toArray(new User[allUsers.size()]);
	// }
	//
	// public Group[] getGroups() {
	// return data.groups;
	// }
	//
	// public User getUserByName(String name) {
	// return data.usersByName.get(name);
	// }
	//
	// public User[] getUsers() {
	// return data.users;
	// }

	private void initializeServerInfo(JiraServerData data) {
		data.serverInfo = this.serviceDelegate.getServerInfo();
	}

	public Issue getIssue(String issueKey) {
		return serviceDelegate.getIssue(issueKey);
	}

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
		ServerInfo info = data.serverInfo;
		if (info == null) {
			info = this.serviceDelegate.getServerInfo();
		}
		data.serverInfo = info;
		return info;
	}

	// public void addLocalFilter(FilterDefinition filter) {
	// data.localFilters.put(filter.getName(), filter);
	// }
	//
	// public void removeLocalFilter(String filterName) {
	// data.localFilters.remove(filterName);
	// }
	//
	// public FilterDefinition[] getLocalFilters() {
	// return data.localFilters.values().toArray(new
	// FilterDefinition[data.localFilters.size()]);
	// }

	public NamedFilter[] getNamedFilters() {
		NamedFilter[] namedFilters = this.serviceDelegate.getSavedFilters();
		// for (int i = 0; i < namedFilters.length; i++) {
		// NamedFilter namedFilter = namedFilters[i];
		// }

		return namedFilters;
	}

	public void addCommentToIssue(Issue issue, String comment) {
		serviceDelegate.addCommentToIssue(issue, comment);
	}

	public void updateIssue(Issue issue, String comment) {
		serviceDelegate.updateIssue(issue, comment);
	}

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

	public void attachFile(Issue issue, String comment, String filename, byte[] contents, String contentType) {
		serviceDelegate.attachFile(issue, comment, filename, contents, contentType);
	}

	public void attachFile(Issue issue, String comment, File file, String contentType) {
		serviceDelegate.attachFile(issue, comment, file, contentType);
	}

	public Issue createIssue(Issue issue) {
		return serviceDelegate.createIssue(issue);
	}

	public void watchIssue(Issue issue) {
		serviceDelegate.watchIssue(issue);
	}

	public void unwatchIssue(Issue issue) {
		serviceDelegate.unwatchIssue(issue);
	}

	public void voteIssue(Issue issue) {
		serviceDelegate.voteIssue(issue);
	}

	public void unvoteIssue(Issue issue) {
		serviceDelegate.unvoteIssue(issue);
	}

	public boolean equals(Object obj) {
		if (obj instanceof CachedRpcJiraServer)  {
			return getName().equals(((CachedRpcJiraServer) obj).getName());
		}
		return false;
	}

	public int hashCode() {
		return getName().hashCode();
	}

	public String toString() {
		return getName();
	}

	public String getHttpPassword() {
		return httpPassword;
	}

	public String getHttpUser() {
		return httpUser;
	}

	public Proxy getProxy() {
		return proxy;
	}

	public boolean useCompression() {
		return useCompression;
	}

	public void setData(JiraServerData data) {
		this.data = data;
	}
	
	public JiraServerData getData() {
		return data;
	}
	
}
