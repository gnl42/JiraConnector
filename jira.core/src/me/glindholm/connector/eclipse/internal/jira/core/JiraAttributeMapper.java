/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraComponent;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueType;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraPriority;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProject;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraResolution;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraVersion;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClient;
import me.glindholm.connector.eclipse.internal.jira.core.util.JiraUtil;
import me.glindholm.jira.rest.client.api.domain.BasicUser;

/**
 * @author Steffen Pingel
 */
public class JiraAttributeMapper extends TaskAttributeMapper implements ITaskAttributeMapper2 {

    private final JiraClient client;

    public JiraAttributeMapper(final TaskRepository taskRepository, final JiraClient client) {
        super(taskRepository);
        this.client = client;
    }

    @Override
    public Date getDateValue(final TaskAttribute attribute) {
        if (JiraUtil.isCustomDateTimeAttribute(attribute)) {
            try {
                // return JiraRssHandler.getOffsetDateTimeFormat().parse(attribute.getValue());
                return client.getDateTimeFormat().parse(attribute.getValue());
            } catch (final ParseException e) {
                return null;
            }
        } else if (JiraUtil.isCustomDateAttribute(attribute)) {
            try {
                // return JiraRssHandler.getOffsetDateTimeFormat().parse(attribute.getValue());
                return client.getDateFormat().parse(attribute.getValue());
            } catch (final ParseException e) {
                return null;
            }
        } else {
            return super.getDateValue(attribute);
        }
    }

    @Override
    public void setDateValue(final TaskAttribute attribute, final Date date) {
        if (JiraUtil.isCustomDateTimeAttribute(attribute)) {
            attribute.setValue(date != null ? new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss", Locale.US).format(date) : "");
        } else {
            super.setDateValue(attribute, date);
        }
    }

    @Override
    public String getValueLabel(final TaskAttribute taskAttribute) {
        if (JiraTaskDataHandler.isTimeSpanAttribute(taskAttribute)) {
            return JiraUtil.getTimeFormat(getTaskRepository()).format(getLongValue(taskAttribute));
        }
        return super.getValueLabel(taskAttribute);
    }

    @Override
    public List<String> getValueLabels(final TaskAttribute taskAttribute) {
        if (JiraTaskDataHandler.isTimeSpanAttribute(taskAttribute)) {
            return Collections.singletonList(JiraUtil.getTimeFormat(getTaskRepository()).format(getLongValue(taskAttribute)));
        }
        return super.getValueLabels(taskAttribute);
    }

    @Override
    public String mapToRepositoryKey(final TaskAttribute parent, final String key) {
        if (TaskAttribute.COMPONENT.equals(key)) {
            return JiraAttribute.COMPONENTS.id();
        } else if (TaskAttribute.TASK_KIND.equals(key)) {
            return JiraAttribute.TYPE.id();
        } else if (TaskAttribute.DATE_DUE.equals(key)) {
            return JiraAttribute.DUE_DATE.id();
        } else if (TaskAttribute.VERSION.equals(key)) {
            return JiraAttribute.AFFECTSVERSIONS.id();
        }
        return super.mapToRepositoryKey(parent, key);
    }

    @Override
    public Map<String, String> getOptions(final TaskAttribute attribute) {
        Map<String, String> options = null;
        if (client.getCache().hasDetails()) {
            if (JiraAttribute.USER_ASSIGNED.id().equals(attribute.getId())) {
                final TaskAttribute projectAttribute = attribute.getTaskData().getRoot().getMappedAttribute(JiraAttribute.PROJECT.id());
                final JiraProject project = client.getCache().getProjectById(projectAttribute.getValue());
                final Map<String, BasicUser> users = project.getAssignables();
                if (users == null) {
                    final String msg = NLS.bind("Project with key {0} needs refreshing. Please refresh repository configuration.", //$NON-NLS-1$
                            project.getKey());
                    PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            MessageDialog.openError(WorkbenchUtil.getShell(), "JiraConnector JIRA Connector", msg);
                            StatusHandler.log(new Status(IStatus.ERROR, "me.glindholm.connector.eclipse.jira.ui", msg));
                        }
                    });
                    return Collections.EMPTY_MAP;
                } else {
                    options = new LinkedHashMap<>();
                    for (final BasicUser user : users.values()) {
                        options.put(user.getId(), user.getDisplayName());
                    }

                    return options;
                }
            } else {
                options = getRepositoryOptions(attribute);
            }

        }
        return options != null ? options : super.getOptions(attribute);

    }

    @Override
    public Map<String, String> getRepositoryOptions(final TaskAttribute attribute) {
        if (client.getCache().hasDetails()) {
            final Map<String, String> options = new LinkedHashMap<>();
            if (JiraAttribute.PROJECT.id().equals(attribute.getId())) {
                final JiraProject[] jiraProjects = client.getCache().getProjects();
                for (final JiraProject jiraProject : jiraProjects) {
                    options.put(jiraProject.getId(), jiraProject.getName());
                }
                return options;
            } else if (JiraAttribute.RESOLUTION.id().equals(attribute.getId())) {
                final JiraResolution[] jiraResolutions = client.getCache().getResolutions();
                for (final JiraResolution resolution : jiraResolutions) {
                    options.put(resolution.getId(), resolution.getName());
                }
                return options;
            } else if (JiraAttribute.PRIORITY.id().equals(attribute.getId())) {
                final JiraPriority[] jiraPriorities = client.getCache().getPriorities();
                for (final JiraPriority priority : jiraPriorities) {
                    options.put(priority.getId(), priority.getName());
                }
                return options;
            } else {
                final TaskAttribute projectAttribute = attribute.getTaskData().getRoot().getMappedAttribute(JiraAttribute.PROJECT.id());
                if (projectAttribute != null) {
                    final JiraProject project = client.getCache().getProjectById(projectAttribute.getValue());
                    if (project != null && project.hasDetails()) {
                        if (JiraAttribute.COMPONENTS.id().equals(attribute.getId())) {
                            for (final JiraComponent component : project.getComponents()) {
                                options.put(component.getId(), component.getName());
                            }
                            return options;
                        } else if (JiraAttribute.AFFECTSVERSIONS.id().equals(attribute.getId())) {
                            for (final JiraVersion version : project.getVersions()) {
                                if (!version.isArchived() || attribute.getValues().contains(version.getId())) {
                                    options.put(version.getId(), version.getName());
                                }
                            }
                            return options;
                        } else if (JiraAttribute.FIXVERSIONS.id().equals(attribute.getId())) {
                            for (final JiraVersion version : project.getVersions()) {
                                if (!version.isArchived() || attribute.getValues().contains(version.getId())) {
                                    options.put(version.getId(), version.getName());
                                }
                            }
                            return options;
                        } else if (JiraAttribute.TYPE.id().equals(attribute.getId())) {
                            final boolean isSubTask = JiraTaskDataHandler.hasSubTaskType(attribute);
                            JiraIssueType[] jiraIssueTypes = project.getIssueTypes();
                            if (jiraIssueTypes == null) {
                                jiraIssueTypes = client.getCache().getIssueTypes();
                            }
                            for (final JiraIssueType issueType : jiraIssueTypes) {
                                if (!isSubTask || issueType.isSubTaskType()) {
                                    options.put(issueType.getId(), issueType.getName());
                                }
                            }
                            return options;
                        }
                    }
                }
            }
        }
        return null;
    }

}