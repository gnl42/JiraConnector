/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service;

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraVersion;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.ServerInfo;
import org.eclipse.mylyn.internal.jira.core.model.Status;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.web.core.AbstractWebLocation;
import org.eclipse.mylyn.web.core.WebCredentials;

/**
 * JIRA server implementation that caches information that is unlikely to change during the session. This server uses a
 * {@link JiraClientData} object to persist the repository configuration. It has life-cycle methods to allow data in the
 * cache to be reloaded.
 * 
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
public abstract class AbstractJiraClient implements JiraClient {

	private volatile JiraClientData data;

	private final String baseUrl;

	private final AbstractWebLocation location;

	private final boolean useCompression;

	private String characterEncoding;

	private boolean attemptedToDetermineCharacterEncoding; 
	
	public AbstractJiraClient(AbstractWebLocation location, boolean useCompression) {
		if (location == null) {
			throw new IllegalArgumentException("baseURL may not be null");
		}

		this.baseUrl = location.getUrl();
		this.location = location;
		this.useCompression = useCompression;

		this.data = new JiraClientData();
	}

	public synchronized void refreshDetails(IProgressMonitor monitor) throws JiraException {
		// use UNKNOWN since some of the update operations block for a long time
		monitor.beginTask("Updating repository configuration", IProgressMonitor.UNKNOWN);

		JiraClientData newData = new JiraClientData();

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
		try {
			monitor.beginTask("Getting server information", IProgressMonitor.UNKNOWN);
			
			initializeServerInfo(data);
		} finally {
			monitor.done();
		}
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

	public String getBaseUrl() {
		return baseUrl;
	}

	public String getUserName() {
		WebCredentials credentials = location.getCredentials(WebCredentials.Type.REPOSITORY);
		return (credentials != null) ? credentials.getUserName() : "";
	}

	public AbstractWebLocation getLocation() {
		return location;
	}
	
	private void initializeProjects(JiraClientData data) throws JiraException {
		String version = data.serverInfo.getVersion();
		if (new JiraVersion(version).compareTo(JiraVersion.JIRA_3_4) >= 0) {
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
		return data.projectsById.get(id);
	}

	public Project getProjectByKey(String key) {
		return data.projectsByKey.get(key);
	}

	public Project[] getProjects() {
		return data.projects;
	}

	private void initializePriorities(JiraClientData data) throws JiraException {
		data.priorities = getPrioritiesRemote();
		data.prioritiesById = new HashMap<String, Priority>(data.priorities.length);
		for (int i = 0; i < data.priorities.length; i++) {
			Priority priority = data.priorities[i];
			data.prioritiesById.put(priority.getId(), priority);
		}
	}

	public abstract Priority[] getPrioritiesRemote() throws JiraException;

	public Priority getPriorityById(String id) {
		return data.prioritiesById.get(id);
	}

	public Priority[] getPriorities() {
		return data.priorities;
	}

	private void initializeIssueTypes(JiraClientData data) throws JiraException {
		IssueType[] issueTypes = getIssueTypesRemote();
		IssueType[] subTaskIssueTypes;
		String version = data.serverInfo.getVersion();
		if (new JiraVersion(version).compareTo(JiraVersion.JIRA_3_3) >= 0) {
			subTaskIssueTypes = getSubTaskIssueTypesRemote();
		} else {
			subTaskIssueTypes = new IssueType[0];
		}

		data.issueTypesById = new HashMap<String, IssueType>(issueTypes.length + subTaskIssueTypes.length);

		for (IssueType issueType : issueTypes) {
			data.issueTypesById.put(issueType.getId(), issueType);
		}

		for (IssueType issueType : subTaskIssueTypes) {
			issueType.setSubTaskType(true);
			data.issueTypesById.put(issueType.getId(), issueType);
		}

		data.issueTypes = new IssueType[issueTypes.length + subTaskIssueTypes.length];
		System.arraycopy(issueTypes, 0, data.issueTypes, 0, issueTypes.length);
		System.arraycopy(subTaskIssueTypes, 0, data.issueTypes, issueTypes.length, subTaskIssueTypes.length);
	}

	public abstract IssueType[] getIssueTypesRemote() throws JiraException;

	public abstract IssueType[] getSubTaskIssueTypesRemote() throws JiraException;

	public IssueType getIssueTypeById(String id) {
		return data.issueTypesById.get(id);
	}

	public IssueType[] getIssueTypes() {
		return data.issueTypes;
	}

	private void initializeStatuses(JiraClientData data) throws JiraException {
		data.statuses = getStatusesRemote();
		data.statusesById = new HashMap<String, Status>(data.statuses.length);
		for (int i = 0; i < data.statuses.length; i++) {
			Status status = data.statuses[i];
			data.statusesById.put(status.getId(), status);
		}
	}

	public abstract Status[] getStatusesRemote() throws JiraException;

	public Status getStatusById(String id) {
		return data.statusesById.get(id);
	}

	public Status[] getStatuses() {
		return data.statuses;
	}

	private void initializeResolutions(JiraClientData data) throws JiraException {
		data.resolutions = getResolutionsRemote();
		data.resolutionsById = new HashMap<String, Resolution>(data.resolutions.length);
		for (int i = 0; i < data.resolutions.length; i++) {
			Resolution resolution = data.resolutions[i];
			data.resolutionsById.put(resolution.getId(), resolution);
		}
	}

	public abstract Resolution[] getResolutionsRemote() throws JiraException;

	public Resolution getResolutionById(String id) {
		return data.resolutionsById.get(id);
	}

	public Resolution[] getResolutions() {
		return data.resolutions;
	}

	private void initializeServerInfo(JiraClientData data) throws JiraException {
		data.serverInfo = getServerInfoRemote();
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

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AbstractJiraClient) {
			return getBaseUrl().equals(((AbstractJiraClient) obj).getBaseUrl());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getBaseUrl().hashCode();
	}

	@Override
	public String toString() {
		return getBaseUrl();
	}

	public boolean useCompression() {
		return useCompression;
	}

	public void setData(JiraClientData data) {
		this.data = data;
	}

	public JiraClientData getData() {
		return data;
	}

	public String getCharacterEncoding() throws JiraException {
		if (this.characterEncoding == null) {
			String serverEncoding = getServerInfo().getCharacterEncoding();
			if (serverEncoding != null) {
				return serverEncoding;
			} else if (!attemptedToDetermineCharacterEncoding) {
				refreshServerInfo(new NullProgressMonitor());
				serverEncoding = getServerInfo().getCharacterEncoding();
				if (serverEncoding != null) {
					return serverEncoding;
				}
			}
			// fallback
			return DEFAULT_CHARSET;
		}
		return this.characterEncoding;
	}
	
	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}
	
}
