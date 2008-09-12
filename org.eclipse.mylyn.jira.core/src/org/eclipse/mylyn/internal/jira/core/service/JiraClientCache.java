/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service;

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.JiraVersion;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.ServerInfo;

/**
 * @author Steffen Pingel
 */
public class JiraClientCache {

	private volatile JiraClientData data;

	private final JiraClient jiraClient;

	public JiraClientCache(JiraClient jiraClient) {
		this.jiraClient = jiraClient;
		this.data = new JiraClientData();
	}

	public JiraStatus getStatusById(String id) {
		return data.statusesById.get(id);
	}

	public JiraStatus[] getStatuses() {
		return data.statuses;
	}

	public IssueType getIssueTypeById(String id) {
		return data.issueTypesById.get(id);
	}

	public IssueType[] getIssueTypes() {
		return data.issueTypes;
	}

	public boolean hasDetails() {
		return data.lastUpdate != 0;
	}

	private void initializeProjects(JiraClientData data, IProgressMonitor monitor) throws JiraException {
		data.projects = jiraClient.getProjects(monitor);

		data.projectsById = new HashMap<String, Project>(data.projects.length);
		data.projectsByKey = new HashMap<String, Project>(data.projects.length);

		for (Project project : data.projects) {
			project.setComponents(jiraClient.getComponents(project.getKey(), monitor));
			project.setVersions(jiraClient.getVersions(project.getKey(), monitor));

			data.projectsById.put(project.getId(), project);
			data.projectsByKey.put(project.getKey(), project);
		}
	}

	public Project getProjectById(String id) {
		return data.projectsById.get(id);
	}

	public Project getProjectByKey(String key) {
		return data.projectsByKey.get(key);
	}

	public Project[] getProjects() {
		return data.projects;
	}

	private void initializePriorities(JiraClientData data, IProgressMonitor monitor) throws JiraException {
		data.priorities = jiraClient.getPriorities(monitor);
		data.prioritiesById = new HashMap<String, Priority>(data.priorities.length);
		for (Priority priority : data.priorities) {
			data.prioritiesById.put(priority.getId(), priority);
		}
	}

	public Priority getPriorityById(String id) {
		return data.prioritiesById.get(id);
	}

	public Priority[] getPriorities() {
		return data.priorities;
	}

	private void initializeIssueTypes(JiraClientData data, IProgressMonitor monitor) throws JiraException {
		IssueType[] issueTypes = jiraClient.getIssueTypes(monitor);
		IssueType[] subTaskIssueTypes;
		String version = data.serverInfo.getVersion();
		if (new JiraVersion(version).compareTo(JiraVersion.JIRA_3_3) >= 0) {
			subTaskIssueTypes = jiraClient.getSubTaskIssueTypes(monitor);
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

	private void initializeStatuses(JiraClientData data, IProgressMonitor monitor) throws JiraException {
		data.statuses = jiraClient.getStatuses(monitor);
		data.statusesById = new HashMap<String, JiraStatus>(data.statuses.length);
		for (JiraStatus status : data.statuses) {
			data.statusesById.put(status.getId(), status);
		}
	}

	private void initializeResolutions(JiraClientData data, IProgressMonitor monitor) throws JiraException {
		data.resolutions = jiraClient.getResolutions(monitor);
		data.resolutionsById = new HashMap<String, Resolution>(data.resolutions.length);
		for (Resolution resolution : data.resolutions) {
			data.resolutionsById.put(resolution.getId(), resolution);
		}
	}

	public Resolution getResolutionById(String id) {
		return data.resolutionsById.get(id);
	}

	public Resolution[] getResolutions() {
		return data.resolutions;
	}

	public void setData(JiraClientData data) {
		this.data = data;
	}

	public JiraClientData getData() {
		return data;
	}

	public synchronized void refreshDetails(IProgressMonitor monitor) throws JiraException {
		try {
			monitor = Policy.monitorFor(monitor);

			// use UNKNOWN since some of the update operations block for a long time
			// TODO use InfiniteSubProgressMonitor
			monitor.beginTask("Updating repository configuration", IProgressMonitor.UNKNOWN);

			JiraClientData newData = new JiraClientData();

			data.serverInfo = newData.serverInfo = jiraClient.getServerInfo(monitor);

			Policy.advance(monitor, 1);
			initializeProjects(newData, monitor);
			Policy.advance(monitor, 1);
			initializePriorities(newData, monitor);
			Policy.advance(monitor, 1);
			initializeIssueTypes(newData, monitor);
			Policy.advance(monitor, 1);
			initializeResolutions(newData, monitor);
			Policy.advance(monitor, 1);
			initializeStatuses(newData, monitor);
			Policy.advance(monitor, 1);

			newData.lastUpdate = System.currentTimeMillis();
			this.data = newData;
		} finally {
			monitor.done();
		}
	}

	/**
	 * Refresh any cached information with the latest values from the remote server. This operation may take a long time
	 * to complete and should not be called from a UI thread.
	 */
	public synchronized void refreshServerInfo(IProgressMonitor monitor) throws JiraException {
		try {
			monitor.beginTask("Getting server information", IProgressMonitor.UNKNOWN);
			data.serverInfo = jiraClient.getServerInfo(monitor);
		} finally {
			monitor.done();
		}
	}

	public ServerInfo getServerInfo() throws JiraException {
		ServerInfo info = data.serverInfo;
		if (info == null) {
			info = jiraClient.getServerInfo(null);
		}
		data.serverInfo = info;
		return info;
	}

}
