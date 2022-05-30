/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Atlassian - improvements
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core.service;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueType;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraPriority;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProject;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProjectRole;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraResolution;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraSecurityLevel;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraServerInfo;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraServerVersion;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraStatus;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraUser;
import me.glindholm.jira.rest.client.api.domain.CimFieldInfo;
import me.glindholm.jira.rest.client.api.domain.Field;

/**
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
 * @author Pawel Niewiadomski
 * @author Jacek Jaroczynski
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

    public JiraIssueType getIssueTypeById(String id) {
        return data.issueTypesById.get(id);
    }

    public JiraIssueType[] getIssueTypes() {
        return data.issueTypes;
    }

    public boolean hasDetails() {
        return data.lastUpdate != 0;
    }

    private void initializeProjects(JiraClientData data, IProgressMonitor monitor) throws JiraException {
        SubMonitor submonitor = SubMonitor.convert(monitor, Messages.JiraClientCache_getting_projects, 10);
        data.projects = jiraClient.getProjects(submonitor.newChild(1));

        data.projectsById = new HashMap<>(data.projects.length);
        data.projectsByKey = new HashMap<>(data.projects.length);

        submonitor.setWorkRemaining(data.projects.length);
        for (JiraProject project : data.projects) {
            data.projectsById.put(project.getId(), project);
            data.projectsByKey.put(project.getKey(), project);
        }
    }

    private void initializeProject(JiraProject project, IProgressMonitor monitor) throws JiraException {
        SubMonitor subMonitor = SubMonitor.convert(monitor,
                NLS.bind(Messages.JiraClientCache_project_details_for, project.getKey()), 5);

        synchronized (project) {
            final JiraServerVersion version = new JiraServerVersion(data.serverInfo.getVersion());

            jiraClient.getProjectDetails(project);

            if (version.compareTo(JiraServerVersion.JIRA_3_13) >= 0) {
                try {
                    JiraSecurityLevel[] securityLevels = jiraClient.getAvailableSecurityLevels(project.getKey(),
                            subMonitor.newChild(1));
                    if (securityLevels.length > 0) {
                        JiraSecurityLevel[] projectSecurityLevels = new JiraSecurityLevel[securityLevels.length + 1];
                        projectSecurityLevels[0] = JiraSecurityLevel.NONE;
                        System.arraycopy(securityLevels, 0, projectSecurityLevels, 1, securityLevels.length);
                        project.setSecurityLevels(projectSecurityLevels);
                    }
                } catch (JiraInsufficientPermissionException e) {
                    // security levels are only support on JIRA enterprise
                    project.setSecurityLevels(null);
                }
            }

            project.setDetails(true);
        }
    }

    public JiraProject getProjectById(String id) {
        return data.projectsById.get(id);
    }

    public JiraProject getProjectById(String id, IProgressMonitor monitor) throws JiraException {
        JiraProject project = data.projectsById.get(id);
        if (project == null || project.getfieldMetadata() == null) {
            refreshProjectDetails(id, monitor);
            project = data.projectsById.get(id);
        }
        return project;
    }

    public JiraProject getProjectByKey(String key) {
        return data.projectsByKey.get(key);
    }

    public JiraProject[] getProjects() {
        return data.projects;
    }

    public JiraProjectRole[] getProjectRoles() {
        return data.projectRoles;
    }

    public Map<String, CimFieldInfo> getFieldMetadata(Long projectId, Long issueType) throws JiraException {
        final JiraProject project = getProjectById(projectId + "", new NullProgressMonitor());
        final Map<Long, Map<String, CimFieldInfo>> fieldMetadata = project.getfieldMetadata();
        return fieldMetadata.get(issueType);
    }

    public Field getMetadata(String fieldId) {
        return data.metadata.get(fieldId);
    }

    private void initializePriorities(JiraClientData data, IProgressMonitor monitor) throws JiraException {
        monitor = SubMonitor.convert(monitor, Messages.JiraClientCache_getting_priorities, 1);

        data.priorities = jiraClient.getPriorities(monitor);
        data.prioritiesById = new HashMap<>(data.priorities.length);
        for (JiraPriority priority : data.priorities) {
            data.prioritiesById.put(priority.getId(), priority);
        }
    }

    public JiraPriority getPriorityById(String id) {
        return data.prioritiesById.get(id);
    }

    public JiraPriority[] getPriorities() {
        return data.priorities;
    }

    private void initializeMetadata(JiraClientData data, IProgressMonitor monitor) throws JiraException {
        SubMonitor submonitor = SubMonitor.convert(monitor, Messages.JiraClientCache_getting_issue_types, 2);

        Iterable<Field> metadata = jiraClient.getMetadata(submonitor);
        Map<String, Field> fieldMetadata = new HashMap<>();
        for (Field field : metadata) {
            fieldMetadata.put(field.getId(), field);
        }

        data.metadata = fieldMetadata;
    }

    private void initializeIssueTypes(JiraClientData data, IProgressMonitor monitor) throws JiraException {
        SubMonitor submonitor = SubMonitor.convert(monitor, Messages.JiraClientCache_getting_issue_types, 2);

        JiraIssueType[] issueTypes = jiraClient.getIssueTypes(submonitor.newChild(1));
        //		IssueType[] subTaskIssueTypes = jiraClient.getSubTaskIssueTypes(submonitor.newChild(1));

        //		data.issueTypesById = new HashMap<String, IssueType>(issueTypes.length + subTaskIssueTypes.length);
        data.issueTypesById = new HashMap<>(issueTypes.length);

        for (JiraIssueType issueType : issueTypes) {
            data.issueTypesById.put(issueType.getId(), issueType);
        }

        //		for (IssueType issueType : subTaskIssueTypes) {
        //			issueType.setSubTaskType(true);
        //			data.issueTypesById.put(issueType.getId(), issueType);
        //		}

        //		data.issueTypes = new IssueType[issueTypes.length + subTaskIssueTypes.length];
        data.issueTypes = new JiraIssueType[issueTypes.length];
        System.arraycopy(issueTypes, 0, data.issueTypes, 0, issueTypes.length);
        //		System.arraycopy(subTaskIssueTypes, 0, data.issueTypes, issueTypes.length, subTaskIssueTypes.length);
    }

    private void initializeStatuses(JiraClientData data, IProgressMonitor monitor) throws JiraException {
        SubMonitor submonitor = SubMonitor.convert(monitor, Messages.JiraClientCache_getting_statuses, 1);

        data.statuses = jiraClient.getStatuses(submonitor.newChild(1));
        data.statusesById = new HashMap<>(data.statuses.length);
        data.statusesByName = new HashMap<>(data.statuses.length);
        for (JiraStatus status : data.statuses) {
            data.statusesById.put(status.getId(), status);
            data.statusesByName.put(status.getName(), status);
        }
    }

    private void initializeProjectRoles(JiraClientData data, IProgressMonitor monitor) throws JiraException {
        SubMonitor submonitor = SubMonitor.convert(monitor, Messages.JiraClientCache_getting_project_roles, 1);

        JiraProjectRole[] projectRoles = jiraClient.getProjectRoles(submonitor.newChild(1));
        if (projectRoles == null) {
            projectRoles = new JiraProjectRole[0];
        }
        data.projectRoles = projectRoles;
    }

    private void initializeResolutions(JiraClientData data, IProgressMonitor monitor) throws JiraException {
        SubMonitor submonitor = SubMonitor.convert(monitor, Messages.JiraClientCache_getting_resolutions, 1);

        data.resolutions = jiraClient.getResolutions(submonitor.newChild(0));
        data.resolutionsById = new HashMap<>(data.resolutions.length);
        data.resolutionsByName = new HashMap<>(data.resolutions.length);
        for (JiraResolution resolution : data.resolutions) {
            data.resolutionsById.put(resolution.getId(), resolution);
            data.resolutionsByName.put(resolution.getName(), resolution);
        }
    }

    public JiraResolution getResolutionById(String id) {
        return data.resolutionsById.get(id);
    }

    /**
     * For tests only
     *
     * @deprecated
     * @param name
     * @return
     */
    @Deprecated
    public JiraResolution getResolutionByName(String name) {
        return data.resolutionsByName.get(name);
    }

    public JiraResolution[] getResolutions() {
        return data.resolutions;
    }

    //	public JiraConfiguration getConfiguration() {
    //		return data.configuration;
    //	}

    /**
     * Requires Administration Privileges on the JIRA side
     *
     * @param data
     * @param monitor
     * @throws JiraException
     */
    //	private void initializeConfiguration(JiraClientData data, IProgressMonitor monitor) throws JiraException {
    //		SubMonitor submonitor = SubMonitor.convert(monitor, Messages.JiraClientCache_getting_configuration, 1);
    //		data.configuration = jiraClient.getConfiguration(submonitor);
    //	}

    public void setData(JiraClientData data) {
        this.data = data;
    }

    public JiraClientData getData() {
        return data;
    }

    public synchronized JiraProject refreshProjectDetails(String projectId, IProgressMonitor monitor) throws JiraException {
        JiraProject project = getProjectById(projectId);
        if (project != null) {
            initializeProject(project, monitor);
        } else {
            refreshDetails(monitor);
            project = getProjectById(projectId);
            if (project == null) {
                throw new JiraException(NLS.bind(
                        "A project with id ''{0}'' does not exist on the repository", projectId)); //$NON-NLS-1$
            }
            initializeProject(project, monitor);
        }
        return project;
    }

    public synchronized JiraProject refreshProjectDetails(JiraProject project, IProgressMonitor monitor) throws JiraException {
        if (project == null) {
            throw new JiraException("Project does not exist"); //$NON-NLS-1$
        }
        initializeProject(project, monitor);

        return project;
    }

    public synchronized void refreshDetails(IProgressMonitor monitor) throws JiraException {
        SubMonitor subMonitor = SubMonitor.convert(monitor, Messages.JiraClientCache_Updating_repository_configuration,
                8);

        final JiraClientData newData = new JiraClientData();

        refreshServerInfo(newData, subMonitor.newChild(1, SubMonitor.SUPPRESS_NONE));
        data.serverInfo = newData.serverInfo;

        initializePriorities(newData, subMonitor.newChild(1, SubMonitor.SUPPRESS_NONE));
        initializeIssueTypes(newData, subMonitor.newChild(1, SubMonitor.SUPPRESS_NONE));
        initializeResolutions(newData, subMonitor.newChild(1, SubMonitor.SUPPRESS_NONE));
        initializeStatuses(newData, subMonitor.newChild(1, SubMonitor.SUPPRESS_NONE));
        initializeProjectRoles(newData, subMonitor.newChild(1, SubMonitor.SUPPRESS_NONE));
        initializeProjects(newData, subMonitor.newChild(1, SubMonitor.SUPPRESS_NONE));
        // initializeMetadata(newData, subMonitor.newChild(1, SubMonitor.SUPPRESS_NONE));
        //		initializeConfiguration(newData, subMonitor.newChild(1, SubMonitor.SUPPRESS_NONE));

        newData.lastUpdate = System.currentTimeMillis();

        this.data = newData;
    }

    /**
     * Requires Administration Privileges on the JIRA side
     *
     * @param data
     * @param monitor
     * @throws JiraException
     */
    //	public void refreshConfiguration(IProgressMonitor monitor) throws JiraException {
    //		initializeConfiguration(data, monitor);
    //	}

    /**
     * Refresh any cached information with the latest values from the remote server. This operation may take a long time
     * to complete and should not be called from a UI thread.
     */
    public synchronized void refreshServerInfo(JiraClientData data, IProgressMonitor monitor) throws JiraException {
        SubMonitor submonitor = SubMonitor.convert(monitor, Messages.JiraClientCache_Getting_server_information, 1);
        data.serverInfo = jiraClient.getServerInfo(submonitor.newChild(1));
    }

    public synchronized void refreshServerInfo(IProgressMonitor monitor) throws JiraException {
        refreshServerInfo(data, monitor);
    }

    /**
     * Returns cached ServerInfo if available.
     *
     * @return null, if no server info is available
     */
    public JiraServerInfo getServerInfo() {
        return data.serverInfo;
    }

    /**
     * Returns cached ServerInfo if available, updated from the repository
     * otherwise.
     *
     * @param monitor
     * @throws JiraException
     */
    public JiraServerInfo getServerInfo(IProgressMonitor monitor) throws JiraException {
        JiraServerInfo info = data.serverInfo;
        if (info == null) {
            info = jiraClient.getServerInfo(monitor);
        }
        data.serverInfo = info;
        return info;
    }

    public JiraUser getUser(String name) {
        synchronized (this.data.usersByName) {
            return this.data.usersByName.get(name);
        }
    }

    public JiraUser putUser(String name, String fullName) {
        JiraUser user = new JiraUser();
        user.setName(name);
        user.setFullName(fullName);
        synchronized (this.data.usersByName) {
            this.data.usersByName.put(name, user);
        }
        return user;
    }

}
