/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Pawel Niewiadomski - fixes for bug 290490
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.JiraVersion;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.SecurityLevel;
import org.eclipse.mylyn.internal.jira.core.model.ServerInfo;
import org.eclipse.mylyn.internal.jira.core.model.User;
import org.eclipse.osgi.util.NLS;

/**
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
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
		SubMonitor submonitor = SubMonitor.convert(monitor, Messages.JiraClientCache_getting_project_details, 10);
		data.projects = jiraClient.getProjects(submonitor.newChild(1));

		data.projectsById = new HashMap<String, Project>(data.projects.length);
		data.projectsByKey = new HashMap<String, Project>(data.projects.length);

		submonitor.setWorkRemaining(data.projects.length);
		for (Project project : data.projects) {
			initializeProject(project, submonitor.newChild(1, SubMonitor.SUPPRESS_NONE));

			data.projectsById.put(project.getId(), project);
			data.projectsByKey.put(project.getKey(), project);
		}
	}

	private void initializeProject(Project project, IProgressMonitor monitor) throws JiraException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, NLS.bind(Messages.JiraClientCache_project_details_for,
				project.getKey()), 5);

		synchronized (project) {
			final String version = data.serverInfo.getVersion();

			project.setComponents(jiraClient.getComponents(project.getKey(), subMonitor.newChild(1)));
			project.setVersions(jiraClient.getVersions(project.getKey(), subMonitor.newChild(1)));

			if (supportsPerProjectIssueTypes(version) >= 0) {
				IssueType[] issueTypes = jiraClient.getIssueTypes(project.getId(), subMonitor.newChild(1));
				IssueType[] subTaskIssueTypes = jiraClient.getSubTaskIssueTypes(project.getId(), subMonitor.newChild(1));
				for (IssueType issueType : subTaskIssueTypes) {
					issueType.setSubTaskType(true);
				}

				IssueType[] projectIssueTypes = new IssueType[issueTypes.length + subTaskIssueTypes.length];
				System.arraycopy(issueTypes, 0, projectIssueTypes, 0, issueTypes.length);
				System.arraycopy(subTaskIssueTypes, 0, projectIssueTypes, issueTypes.length, subTaskIssueTypes.length);

				project.setIssueTypes(projectIssueTypes);
			}
			if (new JiraVersion(version).compareTo(JiraVersion.JIRA_3_13) >= 0) {
				try {
					SecurityLevel[] securityLevels = jiraClient.getAvailableSecurityLevels(project.getKey(),
							subMonitor.newChild(1));
					if (securityLevels.length > 0) {
						SecurityLevel[] projectSecurityLevels = new SecurityLevel[securityLevels.length + 1];
						projectSecurityLevels[0] = SecurityLevel.NONE;
						System.arraycopy(securityLevels, 0, projectSecurityLevels, 1, securityLevels.length);
						project.setSecurityLevels(projectSecurityLevels);
					}
				} catch (JiraInsufficientPermissionException e) {
					// security levels are only support on JIRA enterprise
					project.setSecurityLevels(null);
				}
			}
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
		monitor = SubMonitor.convert(monitor, Messages.JiraClientCache_getting_priorities, 1);

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
		SubMonitor submonitor = SubMonitor.convert(monitor, Messages.JiraClientCache_getting_issue_types, 2);

		String version = data.serverInfo.getVersion();
		if (supportsPerProjectIssueTypes(version) >= 0) {
			// collect issue types from all projects to avoid additional SOAP request
			Set<IssueType> issueTypes = new HashSet<IssueType>();
			for (Project project : data.projects) {
				IssueType[] projectIssueTypes = project.getIssueTypes();
				if (projectIssueTypes != null) {
					issueTypes.addAll(Arrays.asList(projectIssueTypes));
				}
			}

			data.issueTypes = issueTypes.toArray(new IssueType[0]);
			data.issueTypesById = new HashMap<String, IssueType>(data.issueTypes.length);
			for (IssueType issueType : data.issueTypes) {
				data.issueTypesById.put(issueType.getId(), issueType);
			}
		} else {
			IssueType[] issueTypes = jiraClient.getIssueTypes(submonitor.newChild(1));
			IssueType[] subTaskIssueTypes;
			if (new JiraVersion(version).compareTo(JiraVersion.JIRA_3_3) >= 0) {
				subTaskIssueTypes = jiraClient.getSubTaskIssueTypes(submonitor.newChild(1));
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
	}

	private void initializeStatuses(JiraClientData data, IProgressMonitor monitor) throws JiraException {
		SubMonitor submonitor = SubMonitor.convert(monitor, Messages.JiraClientCache_getting_statuses, 1);

		data.statuses = jiraClient.getStatuses(submonitor.newChild(1));
		data.statusesById = new HashMap<String, JiraStatus>(data.statuses.length);
		for (JiraStatus status : data.statuses) {
			data.statusesById.put(status.getId(), status);
		}
	}

	private void initializeResolutions(JiraClientData data, IProgressMonitor monitor) throws JiraException {
		SubMonitor submonitor = SubMonitor.convert(monitor, Messages.JiraClientCache_getting_resolutions, 1);

		data.resolutions = jiraClient.getResolutions(submonitor.newChild(0));
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

	public synchronized void refreshProjectDetails(String projectId, IProgressMonitor monitor) throws JiraException {
		Project project = getProjectById(projectId);
		if (project != null) {
			initializeProject(project, monitor);
		} else {
			refreshDetails(monitor);
		}
	}

	public synchronized void refreshDetails(IProgressMonitor monitor) throws JiraException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, Messages.JiraClientCache_Updating_repository_configuration,
				20);

		final JiraClientData newData = new JiraClientData();

		refreshServerInfo(newData, subMonitor.newChild(1, SubMonitor.SUPPRESS_NONE));
		data.serverInfo = newData.serverInfo;

		initializeProjects(newData, subMonitor.newChild(15, SubMonitor.SUPPRESS_NONE));
		initializePriorities(newData, subMonitor.newChild(1, SubMonitor.SUPPRESS_NONE));
		initializeIssueTypes(newData, subMonitor.newChild(1, SubMonitor.SUPPRESS_NONE));
		initializeResolutions(newData, subMonitor.newChild(1, SubMonitor.SUPPRESS_NONE));
		initializeStatuses(newData, subMonitor.newChild(1, SubMonitor.SUPPRESS_NONE));

		newData.lastUpdate = System.currentTimeMillis();
		this.data = newData;
	}

	/**
	 * Refresh any cached information with the latest values from the remote server. This operation may take a long time
	 * to complete and should not be called from a UI thread.
	 */
	public synchronized void refreshServerInfo(JiraClientData data, IProgressMonitor monitor) throws JiraException {
		monitor = SubMonitor.convert(monitor, Messages.JiraClientCache_Getting_server_information, 1);
		data.serverInfo = jiraClient.getServerInfo(monitor);
	}

	public synchronized void refreshServerInfo(IProgressMonitor monitor) throws JiraException {
		refreshServerInfo(data, monitor);
	}

	/**
	 * Returns cached ServerInfo if available.
	 * 
	 * @return null, if no server info is available
	 */
	public ServerInfo getServerInfo() {
		return data.serverInfo;
	}

	/**
	 * Returns cached ServerInfo if available, updated from the repository otherwise.
	 * 
	 * @param monitor
	 * @throws JiraException
	 */
	public ServerInfo getServerInfo(IProgressMonitor monitor) throws JiraException {
		ServerInfo info = data.serverInfo;
		if (info == null) {
			info = jiraClient.getServerInfo(monitor);
		}
		data.serverInfo = info;
		return info;
	}

	public User getUser(String name) {
		synchronized (this.data.usersByName) {
			return this.data.usersByName.get(name);
		}
	}

	public User putUser(String name, String fullName) {
		User user = new User();
		user.setName(name);
		user.setFullName(fullName);
		synchronized (this.data.usersByName) {
			this.data.usersByName.put(name, user);
		}
		return user;
	}

	private int supportsPerProjectIssueTypes(String version) {
		return new JiraVersion(version).compareTo(JiraVersion.JIRA_3_12);
	}

}
