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

import java.net.Proxy;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.mylar.internal.jira.core.model.Component;
import org.eclipse.mylar.internal.jira.core.model.IssueType;
import org.eclipse.mylar.internal.jira.core.model.Priority;
import org.eclipse.mylar.internal.jira.core.model.Project;
import org.eclipse.mylar.internal.jira.core.model.Resolution;
import org.eclipse.mylar.internal.jira.core.model.ServerInfo;
import org.eclipse.mylar.internal.jira.core.model.Status;
import org.eclipse.mylar.internal.jira.core.model.Version;

/**
 * JIRA server implementation that caches information that is unlikely to change
 * during the session. This server uses a {@link JiraServerData} object to
 * persist the repository configuration. It has life-cycle methods to allow data
 * in the cache to be reloaded.
 * 
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
public abstract class AbstractJiraServer implements JiraServer {

	private volatile JiraServerData data;

	private final String baseUrl;

	private final String username;

	private final String password;

	private final boolean useCompression;

	private final Proxy proxy;

	private final String httpUser;

	private final String httpPassword;

	public AbstractJiraServer(String baseUrl, boolean useCompression, String username, String password,
			Proxy proxy, String httpUser, String httpPassword) {
		if (baseUrl == null) {
			throw new IllegalArgumentException("baseURL may not be null");
		}
		
		this.baseUrl = baseUrl;
		this.useCompression = useCompression;
		this.username = username;
		this.password = password;

		this.proxy = proxy;
		this.httpUser = httpUser;
		this.httpPassword = httpPassword;

		this.data = new JiraServerData();
	}

	public synchronized void refreshDetails(IProgressMonitor monitor)  throws JiraException {
		// use UNKNOWN since some of the update operations block for a long time
		monitor.beginTask("Updating repository configuration", IProgressMonitor.UNKNOWN);

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

	public synchronized void refreshServerInfo(IProgressMonitor monitor) throws JiraException {
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
	
	public String getBaseURL() {
		return baseUrl;
	}

	public String getUserName() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

	private void initializeProjects(JiraServerData data) throws JiraException {
		if (data.serverInfo.getVersion().compareTo("3.4") >= 0) {
			data.projects = getProjectsRemoteNoSchemes();
		} else {
			data.projects = getProjectsRemote();
		}

		data.projectsById = new HashMap<String, Project>(data.projects.length);
		data.projectsByKey = new HashMap<String, Project>(data.projects.length);

		for (int i = 0; i < data.projects.length; i++) {
			Project project = data.projects[i];
			project.setComponents(getComponentsRemote(project.getKey()));
			project.setVersions(getVersionsRemote(project.getKey()));

			data.projectsById.put(project.getId(), project);
			data.projectsByKey.put(project.getKey(), project);
		}
	}

	public abstract Project[] getProjectsRemote() throws JiraException;
	
	public abstract Project[] getProjectsRemoteNoSchemes() throws JiraException;

	public abstract Version[] getVersionsRemote(String key) throws JiraException;

	public abstract Component[] getComponentsRemote(String key) throws JiraException;

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

	private void initializePriorities(JiraServerData data) throws JiraException {
		data.priorities = getPrioritiesRemote();
		data.prioritiesById = new HashMap<String, Priority>(data.priorities.length);
		for (int i = 0; i < data.priorities.length; i++) {
			Priority priority = data.priorities[i];
			data.prioritiesById.put(priority.getId(), priority);
		}
	}

	public abstract Priority[] getPrioritiesRemote() throws JiraException;

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

	private void initializeIssueTypes(JiraServerData data) throws JiraException {
		IssueType[] issueTypes = getIssueTypesRemote();
		IssueType[] subTaskIssueTypes; 
		if (data.serverInfo.getVersion().compareTo("3.2") < 0) {
			subTaskIssueTypes = new IssueType[0];
		} else {
			subTaskIssueTypes = getSubTaskIssueTypesRemote();
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

	public abstract IssueType[] getIssueTypesRemote() throws JiraException;
	
	public abstract IssueType[] getSubTaskIssueTypesRemote() throws JiraException;

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

	private void initializeStatuses(JiraServerData data) throws JiraException {
		data.statuses = getStatusesRemote();
		data.statusesById = new HashMap<String, Status>(data.statuses.length);
		for (int i = 0; i < data.statuses.length; i++) {
			Status status = data.statuses[i];
			data.statusesById.put(status.getId(), status);
		}
	}

	public abstract Status[] getStatusesRemote() throws JiraException;

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

	private void initializeResolutions(JiraServerData data) throws JiraException {
		data.resolutions = getResolutionsRemote();
		data.resolutionsById = new HashMap<String, Resolution>(data.resolutions.length);
		for (int i = 0; i < data.resolutions.length; i++) {
			Resolution resolution = data.resolutions[i];
			data.resolutionsById.put(resolution.getId(), resolution);
		}
	}

	public abstract Resolution[] getResolutionsRemote() throws JiraException;

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

	private void initializeServerInfo(JiraServerData data) throws JiraException {
		data.serverInfo = getServerInfo();
	}

	public ServerInfo getServerInfo() throws JiraException {
		ServerInfo info = data.serverInfo;
		if (info == null) {
			info = getServerInfoRemote();
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

	public abstract ServerInfo getServerInfoRemote() throws JiraException;

	public boolean equals(Object obj) {
		if (obj instanceof AbstractJiraServer)  {
			return getBaseURL().equals(((AbstractJiraServer) obj).getBaseURL());
		}
		return false;
	}

	public int hashCode() {
		return getBaseURL().hashCode();
	}

	public String toString() {
		return getBaseURL();
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
