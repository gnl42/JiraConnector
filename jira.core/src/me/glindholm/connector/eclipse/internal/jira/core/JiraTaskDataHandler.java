/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Eugene Kuleshov - improvements
 *     Pawel Niewiadomski - fixes for bug 288347,
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.RepositoryResponse.ResponseKind;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskOperation;
import org.eclipse.osgi.util.NLS;

import me.glindholm.connector.eclipse.internal.jira.core.html.HTML2TextReader;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraAction;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraAllowedValue;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraAttachment;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraComment;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraComponent;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraCustomField;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssue;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueField;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueLink;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueType;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraPriority;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProject;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProjectRole;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraResolution;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraSecurityLevel;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraServerVersion;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraStatus;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraSubtask;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraUser;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraVersion;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClient;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraException;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraTimeFormat;
import me.glindholm.connector.eclipse.internal.jira.core.util.JiraUtil;
import me.glindholm.jira.rest.client.api.domain.BasicUser;
import me.glindholm.jira.rest.client.api.domain.Remotelink;
import me.glindholm.jira.rest.client.api.domain.Watchers;

/**
 * @author Mik Kersten
 * @author Rob Elves
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
 * @author Jacek Jaroczynski
 * @since 3.0
 */
public class JiraTaskDataHandler extends AbstractTaskDataHandler {

    /**
     * Public for testing
     */
    public static final class CommentDateComparator implements Comparator<JiraComment> {
        @Override
        public int compare(final JiraComment o1, final JiraComment o2) {
            if (o1 != null && o2 != null) {
                if (o1.getCreated() != null && o2.getCreated() != null) {
                    return o1.getCreated().compareTo(o2.getCreated());
                }
            }
            return 0;
        }
    }

    private static final String CONTEXT_ATTACHEMENT_FILENAME = "mylyn-context.zip"; //$NON-NLS-1$

    private static final String CONTEXT_ATTACHEMENT_FILENAME_LEGACY = "mylar-context.zip"; //$NON-NLS-1$

    private static final String CONTEXT_ATTACHMENT_DESCRIPTION = "mylyn/context/zip"; //$NON-NLS-1$

    private static final String CONTEXT_ATTACHMENT_DESCRIPTION_LEGACY = "mylar/context/zip"; //$NON-NLS-1$

    private static final boolean TRACE_ENABLED = Boolean.parseBoolean(Platform.getDebugOption("me.glindholm.connector.eclipse.jira.core/debug/dataHandler")); //$NON-NLS-1$

    private static final String REASSIGN_OPERATION = "reassign"; //$NON-NLS-1$

    public static final String STOP_PROGRESS_OPERATION = "301"; //$NON-NLS-1$

    public static final String START_PROGRESS_OPERATION = "4"; //$NON-NLS-1$

    public static final Object IN_PROGRESS_STATUS = "3"; //$NON-NLS-1$

    public static final Object OPEN_STATUS = "1"; //$NON-NLS-1$

    public static final Object REOPEN_STATUS = "4"; //$NON-NLS-1$

    private static final String LEAVE_OPERATION = "leave"; //$NON-NLS-1$

    private static final JiraServerVersion TASK_DATA_VERSION_1_0 = new JiraServerVersion("1.0"); //$NON-NLS-1$

    private static final JiraServerVersion TASK_DATA_VERSION_2_0 = new JiraServerVersion("2.0"); //$NON-NLS-1$

    private static final JiraServerVersion TASK_DATA_VERSION_2_2 = new JiraServerVersion("2.2"); //$NON-NLS-1$

    private static final JiraServerVersion TASK_DATA_VERSION_CURRENT = new JiraServerVersion("3.0"); //$NON-NLS-1$

    private final IJiraClientFactory clientFactory;

    public JiraTaskDataHandler(final IJiraClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public TaskData getTaskData(final TaskRepository repository, final String taskId, IProgressMonitor monitor) throws CoreException {
        monitor = Policy.monitorFor(monitor);
        try {
            monitor.beginTask(Messages.JiraTaskDataHandler_Getting_task, IProgressMonitor.UNKNOWN);

            final JiraClient client = clientFactory.getJiraClient(repository);
            if (!client.getCache().hasDetails()) {
                client.getCache().refreshDetails(monitor);
            }
            final JiraIssue jiraIssue = getJiraIssue(client, taskId, repository.getRepositoryUrl(), monitor);
            if (jiraIssue != null) {
                return createTaskData(repository, client, jiraIssue, null, monitor);
            }
            throw new CoreException(
                    new org.eclipse.core.runtime.Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN, IStatus.OK, "JIRA ticket not found: " + taskId, null)); //$NON-NLS-1$

        } catch (final JiraException e) {
            final IStatus status = JiraCorePlugin.toStatus(repository, e);
            trace(status);
            throw new CoreException(status);
        } finally {
            monitor.done();
        }
    }

    private JiraIssue getJiraIssue(final JiraClient client, final String taskId, final String repositoryUrl, final IProgressMonitor monitor) //
            throws JiraException {
        // try {
        // int id = Integer.parseInt(taskId);
        // TODO consider keeping a cache of id -> key in the JIRA core plug-in
        // ITask task = TasksUiPlugin.getTaskList().getTask(repositoryUrl, taskId);

        // if (task != null) {
        // return client.getIssueByKey(task.getTaskKey(), monitor);
        // } else {
        return client.getIssueById(taskId, monitor);
        // }
        // } catch (NumberFormatException e) {
        // return client.getIssueByKey(taskId, monitor);
        // }
    }

    public TaskData createTaskData(final TaskRepository repository, final JiraClient client, final JiraIssue jiraIssue, final TaskData oldTaskData,
            final IProgressMonitor monitor) throws JiraException {
        return createTaskData(repository, client, jiraIssue, oldTaskData, false, monitor);
    }

    public TaskData createTaskData(final TaskRepository repository, final JiraClient client, final JiraIssue jiraIssue, final TaskData oldTaskData,
            final boolean forceCache, final IProgressMonitor monitor) throws JiraException {
        final TaskData data = new TaskData(getAttributeMapper(repository), JiraCorePlugin.CONNECTOR_KIND, repository.getRepositoryUrl(), jiraIssue.getId());
        initializeTaskData(repository, data, client, jiraIssue.getProject(), monitor);
        updateTaskData(data, jiraIssue, client, oldTaskData, forceCache, monitor);
        addOperations(data, jiraIssue, client, oldTaskData, forceCache, monitor);
        return data;
    }

    private JiraProject ensureProjectHasDetails(final JiraClient client, final TaskRepository repository, final JiraProject project,
            final IProgressMonitor monitor) throws JiraException {
        if (!project.hasDetails()) {
            client.getCache().refreshProjectDetails(project.getId(), monitor);
            return client.getCache().getProjectById(project.getId());
        }
        return project;
    }

    public void initializeTaskData(final TaskRepository repository, final TaskData data, final JiraClient client, JiraProject project,
            final IProgressMonitor monitor) throws JiraException {
        project = ensureProjectHasDetails(client, repository, project, monitor);

        data.setVersion(TASK_DATA_VERSION_CURRENT.toString());

        createAttribute(data, JiraAttribute.CREATION_DATE);
        final TaskAttribute summaryAttribute = createAttribute(data, JiraAttribute.SUMMARY);
        summaryAttribute.getMetaData().setType(TaskAttribute.TYPE_SHORT_RICH_TEXT);
        final TaskAttribute descriptionAttribute = createAttribute(data, JiraAttribute.DESCRIPTION);
        descriptionAttribute.getMetaData().setType(TaskAttribute.TYPE_LONG_RICH_TEXT);
        createAttribute(data, JiraAttribute.STATUS);
        createAttribute(data, JiraAttribute.ISSUE_KEY);
        createAttribute(data, JiraAttribute.TASK_URL);
        createAttribute(data, JiraAttribute.USER_ASSIGNED);
        createAttribute(data, JiraAttribute.USER_REPORTER);
        createAttribute(data, JiraAttribute.MODIFICATION_DATE);

        final TaskAttribute projectAttribute = createAttribute(data, JiraAttribute.PROJECT);
        final JiraProject[] jiraProjects = client.getCache().getProjects();
        for (final JiraProject jiraProject : jiraProjects) {
            projectAttribute.putOption(jiraProject.getId(), jiraProject.getName());
        }
        projectAttribute.setValue(project.getId());

        final TaskAttribute resolutions = createAttribute(data, JiraAttribute.RESOLUTION);
        final JiraResolution[] jiraResolutions = client.getCache().getResolutions();
        if (jiraResolutions.length > 0) {
            for (final JiraResolution resolution : jiraResolutions) {
                resolutions.putOption(resolution.getId(), resolution.getName());
            }
        } else {
            resolutions.putOption(JiraResolution.FIXED_ID, "Fixed"); //$NON-NLS-1$
            resolutions.putOption(JiraResolution.WONT_FIX_ID, "Won't Fix"); //$NON-NLS-1$
            resolutions.putOption(JiraResolution.DUPLICATE_ID, "Duplicate"); //$NON-NLS-1$
            resolutions.putOption(JiraResolution.INCOMPLETE_ID, "Incomplete"); //$NON-NLS-1$
            resolutions.putOption(JiraResolution.CANNOT_REPRODUCE_ID, "Cannot Reproduce"); //$NON-NLS-1$
        }

        final TaskAttribute priorities = createAttribute(data, JiraAttribute.PRIORITY);
        final JiraPriority[] jiraPriorities = client.getCache().getPriorities();
        for (int i = 0; i < jiraPriorities.length; i++) {
            final JiraPriority priority = jiraPriorities[i];
            priorities.putOption(priority.getId(), priority.getName());
            if (i == jiraPriorities.length / 2) {
                priorities.setValue(priority.getId());
            }
        }

        createAttribute(data, JiraAttribute.RANK);

        final TaskAttribute types = createAttribute(data, JiraAttribute.TYPE);
        JiraIssueType[] jiraIssueTypes = project.getIssueTypes();
        if (jiraIssueTypes == null || jiraIssueTypes.length == 0) {
            jiraIssueTypes = client.getCache().getIssueTypes();
        }
        for (int i = 0; i < jiraIssueTypes.length; i++) {
            final JiraIssueType type = jiraIssueTypes[i];
            if (!type.isSubTaskType()) {
                types.putOption(type.getId(), type.getName());
                // set first as default
                if (i == 0) {
                    types.setValue(type.getId());
                }
            }
        }

        createAttribute(data, JiraAttribute.PARENT_KEY);
        createAttribute(data, JiraAttribute.PARENT_ID);

        createAttribute(data, JiraAttribute.DUE_DATE);
        createAttribute(data, JiraAttribute.ESTIMATE);
        if (!data.isNew()) {
            createAttribute(data, JiraAttribute.ACTUAL);
            createAttribute(data, JiraAttribute.INITIAL_ESTIMATE);
        }

        final TaskAttribute affectsVersions = createAttribute(data, JiraAttribute.AFFECTSVERSIONS);
        for (final JiraVersion version : project.getVersions()) {
            affectsVersions.putOption(version.getId(), version.getName());
        }

        final TaskAttribute components = createAttribute(data, JiraAttribute.COMPONENTS);
        for (final JiraComponent component : project.getComponents()) {
            components.putOption(component.getId(), component.getName());
        }

        final TaskAttribute fixVersions = createAttribute(data, JiraAttribute.FIXVERSIONS);
        for (final JiraVersion version : project.getVersions()) {
            fixVersions.putOption(version.getId(), version.getName());
        }

        final TaskAttribute env = createAttribute(data, JiraAttribute.ENVIRONMENT);
        env.getMetaData().setType(TaskAttribute.TYPE_LONG_RICH_TEXT);

        if (!data.isNew()) {
            final TaskAttribute commentAttribute = createAttribute(data, JiraAttribute.COMMENT_NEW);
            commentAttribute.getMetaData().setType(TaskAttribute.TYPE_LONG_RICH_TEXT);
        }

        final JiraSecurityLevel[] securityLevels = project.getSecurityLevels();
        if (securityLevels != null) {
            final TaskAttribute securityLevelAttribute = createAttribute(data, JiraAttribute.SECURITY_LEVEL);
            for (final JiraSecurityLevel securityLevel : securityLevels) {
                securityLevelAttribute.putOption(securityLevel.getId(), securityLevel.getName());
            }
            securityLevelAttribute.setValue(JiraSecurityLevel.NONE.getId());
        }

        data.getRoot().createAttribute(WorkLogConverter.ATTRIBUTE_WORKLOG_NEW);
        createAttribute(data, JiraAttribute.LABELS);

        // data.getRoot().createAttribute(WorkLogConverter.ATTRIBUTE_WORKLOG_MYLYN_ACTIVITY_DELTA);

        final TaskAttribute projectRoles = createAttribute(data, JiraAttribute.PROJECT_ROLES);
        projectRoles.putOption(JiraConstants.NEW_COMMENT_VIEWABLE_BY_ALL, JiraConstants.NEW_COMMENT_VIEWABLE_BY_ALL);
        final JiraProjectRole[] roles = client.getCache().getProjectRoles();
        if (roles != null) {
            for (final JiraProjectRole projectRole : roles) {
                projectRoles.putOption(projectRole.getName(), projectRole.getName());
            }
        }

        createAttribute(data, JiraAttribute.VOTES);

        createAttribute(data, JiraAttribute.WATCHERS);

        createAttribute(data, JiraAttribute.REMOTELINKS);
    }

    public TaskAttribute createAttribute(final TaskData data, final JiraAttribute key) {
        final TaskAttribute attribute = data.getRoot().createAttribute(key.id());
        attribute.getMetaData().defaults() //
                .setReadOnly(key.isReadOnly()).setKind(key.getKind()).setLabel(key.getName()).setType(key.getType().getTaskType())
                .putValue(JiraConstants.META_TYPE, key.getType().getKey());
        return attribute;
    }

    private void updateTaskData(final TaskData data, final JiraIssue jiraIssue, final JiraClient client, final TaskData oldTaskData, final boolean forceCache,
            final IProgressMonitor monitor) throws JiraException {
        final String parentKey = jiraIssue.getParentKey();
        if (parentKey != null) {
            setAttributeValue(data, JiraAttribute.PARENT_KEY, parentKey);
        } else {
            removeAttribute(data, JiraAttribute.PARENT_KEY);
        }

        final String parentId = jiraIssue.getParentId();
        if (parentId != null) {
            setAttributeValue(data, JiraAttribute.PARENT_ID, parentId);
        } else {
            removeAttribute(data, JiraAttribute.PARENT_ID);
        }

        final JiraSubtask[] subtasks = jiraIssue.getSubtasks();
        if (subtasks != null && subtasks.length > 0) {
            createAttribute(data, JiraAttribute.SUBTASK_IDS);
            createAttribute(data, JiraAttribute.SUBTASK_KEYS);
            for (final JiraSubtask subtask : subtasks) {
                addAttributeValue(data, JiraAttribute.SUBTASK_IDS, subtask.getIssueId());
                addAttributeValue(data, JiraAttribute.SUBTASK_KEYS, subtask.getIssueKey());
            }
        }

        final JiraIssueLink[] issueLinks = jiraIssue.getIssueLinks();
        if (issueLinks != null && issueLinks.length > 0) {
            final HashMap<String, TaskAttribute> links = new HashMap<>();
            for (final JiraIssueLink link : issueLinks) {
                String key;
                String desc;
                if (link.getInwardDescription() == null) {
                    key = link.getLinkTypeId() + "outward"; //$NON-NLS-1$
                    desc = link.getOutwardDescription();
                } else {
                    key = link.getLinkTypeId() + "inward"; //$NON-NLS-1$
                    desc = link.getInwardDescription();
                }
                final String label = capitalize(desc) + ":"; //$NON-NLS-1$
                TaskAttribute attribute = links.get(key);
                if (attribute == null) {
                    attribute = data.getRoot().createAttribute(JiraConstants.ATTRIBUTE_LINK_PREFIX + key);
                    attribute.getMetaData() //
                            .setKind(TaskAttribute.KIND_DEFAULT).setLabel(label).setType(JiraFieldType.ISSUELINKS.getTaskType())
                            .putValue(JiraConstants.META_TYPE, JiraFieldType.ISSUELINKS.getKey());
                    links.put(key, attribute);
                }
                attribute.addValue(link.getIssueKey());

                if (link.getInwardDescription() != null) {
                    attribute = data.getRoot().getMappedAttribute(JiraAttribute.LINKED_IDS.id());
                    if (attribute == null) {
                        attribute = createAttribute(data, JiraAttribute.LINKED_IDS);
                    }
                    addAttributeValue(data, JiraAttribute.LINKED_IDS, link.getIssueId());
                }
            }
        }

        setAttributeValue(data, JiraAttribute.CREATION_DATE, JiraUtil.dateToString(jiraIssue.getCreated()));
        setAttributeValue(data, JiraAttribute.SUMMARY, jiraIssue.getSummary());
        setAttributeValue(data, JiraAttribute.DESCRIPTION, jiraIssue.getDescription());
        setAttributeValue(data, JiraAttribute.ISSUE_KEY, jiraIssue.getKey());
        setAttributeValue(data, JiraAttribute.TASK_URL, jiraIssue.getUrl());
        setAttributeValue(data, JiraAttribute.RESOLUTION, //
                jiraIssue.getResolution() == null ? "" : jiraIssue.getResolution().getId()); //$NON-NLS-1$
        setAttributeValue(data, JiraAttribute.MODIFICATION_DATE, JiraUtil.dateToString(jiraIssue.getUpdated()));
        final IRepositoryPerson assignee = getPersonForUpdate(data, jiraIssue.getAssignee());

        if (assignee.getPersonId() != null) {
            setAttributeValue(data, JiraAttribute.USER_ASSIGNED, assignee);
        }
        final IRepositoryPerson reporter = getPersonForUpdate(data, jiraIssue.getReporter());
        if (reporter.getPersonId() != null) {
            setAttributeValue(data, JiraAttribute.USER_REPORTER, reporter);
        }

        if (jiraIssue.getWatchers() != null) {
            setAttributeWatchers(data, JiraAttribute.WATCHERS, jiraIssue.getWatchers());
        }

        if (jiraIssue.getRemotelinks() != null) {
            setAttributeRemotelinks(data, JiraAttribute.REMOTELINKS, jiraIssue.getRemotelinks());
        }

        setAttributeValue(data, JiraAttribute.PROJECT, jiraIssue.getProject().getId());

        if (jiraIssue.getStatus() != null) {
            final TaskAttribute attribute = data.getRoot().getAttribute(JiraAttribute.STATUS.id());
            attribute.putOption(jiraIssue.getStatus().getId(), jiraIssue.getStatus().getName());
            attribute.setValue(jiraIssue.getStatus().getId());
        }

        if (jiraIssue.getPriority() != null) {
            setAttributeValue(data, JiraAttribute.PRIORITY, jiraIssue.getPriority().getId());
        } else {
            removeAttribute(data, JiraAttribute.PRIORITY);
        }

        if (jiraIssue.getRank() != null) {
            setAttributeValue(data, JiraAttribute.RANK, jiraIssue.getRank().toString());
        }

        final JiraSecurityLevel securityLevel = jiraIssue.getSecurityLevel();
        if (securityLevel != null) {
            TaskAttribute attribute = data.getRoot().getAttribute(JiraAttribute.SECURITY_LEVEL.id());
            if (attribute == null) {
                // the repository configuration may not have information about available
                // security
                // information for older JIRA versions
                attribute = createAttribute(data, JiraAttribute.SECURITY_LEVEL);
                attribute.getMetaData().setReadOnly(true);
            }
            if (!attribute.getOptions().containsKey(securityLevel.getId())) {
                attribute.putOption(securityLevel.getId(), securityLevel.getName());
            }
            attribute.setValue(securityLevel.getId());
        }

        final JiraIssueType issueType = jiraIssue.getType();
        if (issueType != null) {
            final TaskAttribute attribute = setAttributeValue(data, JiraAttribute.TYPE, issueType.getId());
            if (issueType.isSubTaskType()) {
                attribute.getMetaData() //
                        .setReadOnly(true).putValue(JiraConstants.META_SUB_TASK_TYPE, Boolean.toString(true));
                attribute.clearOptions();
                attribute.putOption(issueType.getId(), issueType.getName());
            }
        } else {
            removeAttribute(data, JiraAttribute.TYPE);
        }

        // if no time was logged initial estimate and estimate should be the same value
        // (but does not have to)
        // show initial estimate if it is not equal to remaining estimate
        if (jiraIssue.getInitialEstimate() != null && !jiraIssue.getInitialEstimate().equals(jiraIssue.getEstimate())) {
            setAttributeValue(data, JiraAttribute.INITIAL_ESTIMATE, jiraIssue.getInitialEstimate() + ""); //$NON-NLS-1$
        } else {
            removeAttribute(data, JiraAttribute.INITIAL_ESTIMATE);
        }
        setAttributeValue(data, JiraAttribute.ESTIMATE, jiraIssue.getEstimate() + ""); //$NON-NLS-1$
        setAttributeValue(data, JiraAttribute.ACTUAL, jiraIssue.getActual() + ""); //$NON-NLS-1$

        if (jiraIssue.getDue() != null) {
            setAttributeValue(data, JiraAttribute.DUE_DATE, JiraUtil.dateToString(jiraIssue.getDue()));
        } else if (!jiraIssue.hasDueDate()) {
            removeAttribute(data, JiraAttribute.DUE_DATE);
        }

        if (jiraIssue.getComponents() != null) {
            for (final JiraComponent component : jiraIssue.getComponents()) {
                addAttributeValue(data, JiraAttribute.COMPONENTS, component.getId());
            }
        }

        if (jiraIssue.getReportedVersions() != null) {
            for (final JiraVersion version : jiraIssue.getReportedVersions()) {
                addAttributeValue(data, JiraAttribute.AFFECTSVERSIONS, version.getId());
            }
        }

        if (jiraIssue.getFixVersions() != null) {
            for (final JiraVersion version : jiraIssue.getFixVersions()) {
                addAttributeValue(data, JiraAttribute.FIXVERSIONS, version.getId());
            }
        }

        if (jiraIssue.getEnvironment() != null) {
            setAttributeValue(data, JiraAttribute.ENVIRONMENT, jiraIssue.getEnvironment());
        } else {
            removeAttribute(data, JiraAttribute.ENVIRONMENT);
        }

        if (jiraIssue.getVotes() > 0) {
            setAttributeValue(data, JiraAttribute.VOTES, Integer.toString(jiraIssue.getVotes()));
        } else {
            removeAttribute(data, JiraAttribute.VOTES);
        }

        if (jiraIssue.getLabels().length > 0) {
            setAttributeValue(data, JiraAttribute.LABELS, StringUtils.join(jiraIssue.getLabels(), " ")); //$NON-NLS-1$
        }

        addAttributeValue(data, JiraAttribute.PROJECT_ROLES, JiraConstants.NEW_COMMENT_VIEWABLE_BY_ALL);

        addComments(data, jiraIssue, client);
        addAttachments(data, jiraIssue, client);

        addEditableCustomFields(data, jiraIssue);

        addCustomFieldsValues(data, jiraIssue);

        addWorklog(data, jiraIssue, client, oldTaskData, forceCache, monitor);

        final Map<String, List<JiraAllowedValue>> editableKeys = getEditableKeys(data, jiraIssue, client, oldTaskData, forceCache, monitor);
        updateProperties(data, editableKeys);

    }

    private void addEditableCustomFields(final TaskData data, final JiraIssue jiraIssue) {
        final JiraIssueField[] editableAttributes = jiraIssue.getEditableFields();
        if (editableAttributes != null) {
            for (final JiraIssueField field : editableAttributes) {
                if (field.getId().startsWith("customfield")) { //$NON-NLS-1$
                    final String mappedKey = mapCommonAttributeKey(field.getId());

                    if (!data.getRoot().getAttributes().containsKey(mappedKey)) {

                        final String name = field.getName() + ":"; //$NON-NLS-1$
                        final String kind = JiraAttribute.valueById(mappedKey).getKind();
                        final String type = field.getType();
                        String taskType = JiraFieldType.fromKey(type).getTaskType();
                        if (taskType == null && type != null && type.startsWith(JiraConstants.JIRA_TOOLKIT_PREFIX)) {
                            taskType = TaskAttribute.TYPE_SHORT_TEXT;

                        }

                        final TaskAttribute attribute = data.getRoot().createAttribute(mappedKey);
                        attribute.getMetaData().defaults() //
                                .setKind(kind).setLabel(name).setReadOnly(false).setType(taskType).putValue(JiraConstants.META_TYPE, type);
                    }
                }
            }
        }
    }

    /**
     * Stores user in cache if <code>fullName</code> is provided. Otherwise user is
     * retrieved from cache.
     */
    public static IRepositoryPerson getPerson(final TaskData data, final JiraClient client, String userId, final String fullName) {
        if (userId == null || JiraRepositoryConnector.UNASSIGNED_USER.equals(userId)) {
            userId = ""; //$NON-NLS-1$
        }
        JiraUser user;
        if (fullName != null) {
            user = client.getCache().putUser(userId, fullName);
        } else {
            user = client.getCache().getUser(userId);
        }
        final IRepositoryPerson person = data.getAttributeMapper().getTaskRepository().createPerson(userId);
        if (user != null) {
            person.setName(user.getFullName());
        }
        return person;
    }

    private IRepositoryPerson getPersonForUpdate(final TaskData data, BasicUser user) {
        if (user == null) {
            user = BasicUser.UNASSIGNED_USER;
        }
        final ITaskAttributeMapper2 jiraMapper = (ITaskAttributeMapper2) data.getAttributeMapper();
        return jiraMapper.createPerson(user);
    }

    private void addComments(final TaskData data, final JiraIssue jiraIssue, final JiraClient client) {
        int i = 1;

        // ensure that the comments are in the correct order for display in the task
        // editor
        // fix fpr PLE-953
        final List<JiraComment> comments = new ArrayList<>(Arrays.asList(jiraIssue.getComments()));
        Collections.sort(comments, new CommentDateComparator());

        for (final JiraComment comment : comments) {
            final TaskAttribute attribute = data.getRoot().createAttribute(TaskAttribute.PREFIX_COMMENT + i);
            final TaskCommentMapper taskComment = TaskCommentMapper.createFrom(attribute);
            taskComment.setAuthor(getPerson(data, comment.getAuthor()));
            taskComment.setNumber(i);
            String commentText = comment.getComment();
            if (comment.isMarkupDetected()) {
                commentText = stripTags(commentText);
            }
            taskComment.setText(commentText);
            taskComment.setCreationDate(Date.from(comment.getCreated()));
            // TODO taskComment.setUrl()
            taskComment.applyTo(attribute);

            // add level attribute
            if (comment.getRoleLevel() != null) {
                final TaskAttribute level = attribute.createAttribute(JiraConstants.COMMENT_SECURITY_LEVEL);
                level.setValue(comment.getRoleLevel());
            }
            i++;
        }
    }

    private void addAttachments(final TaskData data, final JiraIssue jiraIssue, final JiraClient client) {
        int i = 1;
        for (final JiraAttachment attachment : jiraIssue.getAttachments()) {
            final TaskAttribute attribute = data.getRoot().createAttribute(TaskAttribute.PREFIX_ATTACHMENT + i);
            final TaskAttachmentMapper taskAttachment = TaskAttachmentMapper.createFrom(attribute);
            taskAttachment.setAttachmentId(attachment.getId());
            taskAttachment.setAuthor(getPerson(data, attachment.getAuthor()));
            taskAttachment.setFileName(attachment.getName());
            if (CONTEXT_ATTACHEMENT_FILENAME.equals(attachment.getName())) {
                taskAttachment.setDescription(CONTEXT_ATTACHMENT_DESCRIPTION);
            } else if (CONTEXT_ATTACHEMENT_FILENAME_LEGACY.equals(attachment.getName())) {
                taskAttachment.setDescription(CONTEXT_ATTACHMENT_DESCRIPTION_LEGACY);
            } else {
                taskAttachment.setDescription(attachment.getName());
            }
            taskAttachment.setLength(attachment.getSize());
            taskAttachment.setCreationDate(Date.from(attachment.getCreated()));
            // taskAttachment.setUrl(client.getBaseUrl() + "/secure/attachment/" +
            // attachment.getId() + "/" //$NON-NLS-1$ //$NON-NLS-2$
            // + attachment.getName());
            taskAttachment.setUrl(attachment.getContent().toString());
            taskAttachment.applyTo(attribute);
            i++;
        }
    }

    private IRepositoryPerson getPerson(final TaskData data, final BasicUser user) {
        if (user != null) {
            final TaskAttributeMapper mapper = data.getAttributeMapper();
            final ITaskAttributeMapper2 jiraMapper = (ITaskAttributeMapper2) mapper;
            final IRepositoryPerson person = jiraMapper.createPerson(user);
            person.setName(user.getDisplayName());
            return person;
        } else {
            return data.getAttributeMapper().getTaskRepository().createPerson("");
        }
    }

    private void addCustomFieldsValues(final TaskData data, final JiraIssue jiraIssue) {
        for (final JiraCustomField field : jiraIssue.getCustomFields()) {
            final String mappedKey = mapCommonAttributeKey(field.getId());
            // String name = field.getName() + ":"; //$NON-NLS-1$
            // String kind = JiraAttribute.valueById(mappedKey).getKind();
            // String type = field.getKey();
            // String taskType = JiraFieldType.fromKey(type).getTaskType();

            // if (taskType == null && type != null &&
            // type.startsWith(IJiraConstants.JIRA_TOOLKIT_PREFIX)) {
            // taskType = TaskAttribute.TYPE_SHORT_TEXT;
            //
            // }

            // TaskAttribute attribute = data.getRoot().createAttribute(mappedKey);

            final TaskAttribute attribute = data.getRoot().getAttributes().get(mappedKey);

            // attribute.getMetaData().defaults() //
            // .setKind(kind)
            // .setLabel(name)
            // .setReadOnly(field.isReadOnly())
            // .setType(taskType)
            // .putValue(IJiraConstants.META_TYPE, type);
            if (attribute != null) {
                for (final String value : field.getValues()) {
                    attribute.addValue(value);
                }
            }
        }
    }

    private Map<String, List<JiraAllowedValue>> getEditableKeys(final TaskData data, final JiraIssue jiraIssue, final JiraClient client,
            final TaskData oldTaskData, final boolean forceCache, final IProgressMonitor monitor) throws JiraException {
        final Map<String, List<JiraAllowedValue>> editableKeys = new HashMap<>();
        if (!JiraRepositoryConnector.isClosed(jiraIssue)) {
            if (useCachedInformation(jiraIssue, oldTaskData, forceCache)) {
                if (oldTaskData == null) {
                    // caching forced but no information available
                    data.setPartial(true);
                    return editableKeys;
                }

                // avoid server round-trips
                for (final TaskAttribute attribute : oldTaskData.getRoot().getAttributes().values()) {
                    if (!attribute.getMetaData().isReadOnly()) {
                        editableKeys.put(attribute.getId(), Collections.<JiraAllowedValue>emptyList());
                    }
                }

                final TaskAttribute attribute = oldTaskData.getRoot().getAttribute(JiraConstants.ATTRIBUTE_READ_ONLY);
                if (attribute != null) {
                    data.getRoot().deepAddCopy(attribute);
                }
            } else {
                // try {
                // IssueField[] editableAttributes =
                // client.getEditableAttributes(jiraIssue.getKey(), monitor);
                final JiraIssueField[] editableAttributes = jiraIssue.getEditableFields();
                if (editableAttributes != null && editableAttributes.length > 0) {
                    for (final JiraIssueField field : editableAttributes) {
                        // if (!field.getId().startsWith("customfield")) {
                        editableKeys.put(mapCommonAttributeKey(field.getId()), field.getAlloweValues());
                    }
                } else {
                    // flag as read-only to avoid calling getEditableAttributes() on each sync
                    data.getRoot().createAttribute(JiraConstants.ATTRIBUTE_READ_ONLY);
                }
                // }
                // } catch (JiraInsufficientPermissionException e) {
                // trace(e);
                // // flag as read-only to avoid calling getEditableAttributes() on each sync
                // data.getRoot().createAttribute(IJiraConstants.ATTRIBUTE_READ_ONLY);
                // }
            }
        }
        return editableKeys;
    }

    private void updateProperties(final TaskData data, final Map<String, List<JiraAllowedValue>> editableKeys) {
        for (final TaskAttribute attribute : data.getRoot().getAttributes().values()) {
            final TaskAttributeMetaData properties = attribute.getMetaData();
            final boolean editable = editableKeys.containsKey(attribute.getId().toLowerCase());
            if (editable && (attribute.getId().startsWith(JiraConstants.ATTRIBUTE_CUSTOM_PREFIX) //
                    || !JiraAttribute.valueById(attribute.getId()).isHidden())) {
                properties.setKind(TaskAttribute.KIND_DEFAULT);
            }

            if (TaskAttribute.COMMENT_NEW.equals(attribute.getId()) || TaskAttribute.RESOLUTION.equals(attribute.getId())
                    || TaskAttribute.USER_ASSIGNED.equals(attribute.getId()) || JiraAttribute.PROJECT_ROLES.id().equals(attribute.getId())) {
                properties.setReadOnly(false);
            } else {

                if (editable && attribute.getId().startsWith(JiraConstants.ATTRIBUTE_CUSTOM_PREFIX)) {
                    final List<JiraAllowedValue> allowedValues = editableKeys.get(attribute.getId().toLowerCase());

                    if (allowedValues != null && allowedValues.size() > 0) {
                        // attribute.getMetaData().getValue("type");
                        if (TaskAttribute.TYPE_SINGLE_SELECT.equals(attribute.getMetaData().getType())) {
                            // add "None" option for select and radio buttons
                            attribute.putOption(JiraCustomField.NONE_ALLOWED_VALUE, "None"); //$NON-NLS-1$
                            // set "None" as selected option if there is no value selected
                            if (attribute.getValues().size() == 0) {
                                attribute.addValue(JiraCustomField.NONE_ALLOWED_VALUE);
                            }
                        }

                        for (final JiraAllowedValue allowedValue : allowedValues) {
                            attribute.putOption(allowedValue.getValue(), allowedValue.getValue());
                        }
                    }
                }

                // make attributes read-only if can't find editing options
                final String key = properties.getValue(JiraConstants.META_TYPE);
                final Map<String, String> options = attribute.getOptions();
                if (JiraFieldType.SELECT.getKey().equals(key) && (options.isEmpty() || properties.isReadOnly())) {
                    properties.setReadOnly(true);
                } else if (JiraFieldType.MULTISELECT.getKey().equals(key) && options.isEmpty()) {
                    properties.setReadOnly(true);
                } else if (properties.isReadOnly()) {
                    properties.setReadOnly(true);
                } else {
                    properties.setReadOnly(!editable);
                }
            }
        }
    }

    private void addAttributeValue(final TaskData data, final JiraAttribute key, final String value) {
        data.getRoot().getAttribute(key.id()).addValue(value);
    }

    private TaskAttribute setAttributeValue(final TaskData data, final JiraAttribute key, final String value) {
        final TaskAttribute attribute = data.getRoot().getAttribute(key.id());
        // XXX a null value might indicate an invalid issue
        if (value != null) {
            attribute.setValue(value);
        }
        return attribute;
    }

    private TaskAttribute setAttributeValue(final TaskData data, final JiraAttribute key, final IRepositoryPerson person) {
        final TaskAttribute attribute = data.getRoot().getAttribute(key.id());
        data.getAttributeMapper().setRepositoryPerson(attribute, person);
        return attribute;
    }

    private TaskAttribute setAttributeWatchers(final TaskData data, final JiraAttribute key, final Watchers watchers) {
        final TaskAttribute attribute = data.getRoot().getAttribute(key.id());
        final List<String> watchersList = new ArrayList<>(watchers.getNumWatchers());
        for (final BasicUser watcher : watchers.getUsers()) {
            watchersList.add(watcher.getDisplayName());
        }

        data.getAttributeMapper().setValue(attribute, String.join("\n", watchersList));
        return attribute;
    }

    private void setAttributeRemotelinks(final TaskData data, final JiraAttribute key, final Map<String, List<Remotelink>> remotelinks) {
        final TaskAttribute attribute = data.getRoot().getAttribute(JiraAttribute.REMOTELINKS.id());
        int i = 0;
        for (final Map.Entry<String, List<Remotelink>> link : remotelinks.entrySet()) {
//            final TaskAttribute child = attribute.createAttribute(TaskAttribute.TYPE_LABEL + ++i);
//            final TaskAttributeMetaData defaults = child.getMetaData().defaults();
//            defaults.setType(TaskAttribute.TYPE_LABEL);
//            defaults.setLabel(link.getKey());
//            child.getMetaData().setKind(TaskAttribute.TYPE_LABEL);
            for (final Remotelink remotelink : link.getValue()) {
                final TaskAttribute childAttr = attribute.createAttribute(JiraAttribute.REMOTELINK.id() + ++i);
                final TaskAttributeMetaData childDefaults = childAttr.getMetaData().defaults();
                childDefaults.setType(TaskAttribute.TYPE_URL);
                childDefaults.setLabel(remotelink.getObject().getTitle());
                childAttr.setValue(remotelink.getObject().getUrl().toString());
            }
        }
    }

    private TaskAttribute setAttributeComponents(final TaskData data, final JiraAttribute key, final JiraComponent[] components) {
        final TaskAttribute attribute = data.getRoot().getAttribute(key.id());
        final List<String> componentsList = new ArrayList<>(components.length);
        for (final JiraComponent component : components) {
            componentsList.add(component.getName());
        }

        data.getAttributeMapper().setValue(attribute, String.join("\n", componentsList));
        return attribute;
    }

    private boolean useCachedInformation(final JiraIssue issue, final TaskData oldTaskData, final boolean forceCache) {
        if (forceCache) {
            return true;
        }
        if (oldTaskData != null && issue.getStatus() != null) {
            final TaskAttribute attribute = oldTaskData.getRoot().getMappedAttribute(TaskAttribute.STATUS);
            if (attribute != null) {
                return attribute.getValue().equals(issue.getStatus().getId());
            }
        }
        return false;
    }

// private void removeAttributes(TaskData data, String keyPrefix) {
// List<TaskAttribute> attributes = new
// ArrayList<TaskAttribute>(data.getRoot().getAttributes().values());
// for (TaskAttribute attribute : attributes) {
// if (attribute.getId().startsWith(keyPrefix)) {
// removeAttribute(data, attribute.getId());
// }
// }
// }

    private void removeAttribute(final TaskData data, final JiraAttribute key) {
        data.getRoot().removeAttribute(key.id());
    }

    /**
     * Removes attribute values without removing attribute to preserve order of
     * attributes
     */
// private void removeAttributeValues(TaskData data, String attributeId) {
// data.getRoot().getAttribute(attributeId).clearValues();
// }
    private String capitalize(final String s) {
        if (s.length() > 1) {
            final char c = s.charAt(0);
            final char uc = Character.toUpperCase(c);
            if (uc != c) {
                return uc + s.substring(1);
            }
        }
        return s;
    }

    public static String stripTags(final String text) {
        if (text == null || text.length() == 0) {
            return ""; //$NON-NLS-1$
        }
        final StringReader stringReader = new StringReader(text);
        try (HTML2TextReader html2TextReader = new HTML2TextReader(stringReader)) {
            final char[] chars = new char[text.length()];
            final int len = html2TextReader.read(chars, 0, text.length());
            if (len == -1) {
                return ""; //$NON-NLS-1$
            }
            return new String(chars, 0, len);
        } catch (final IOException e) {
            return text;
        }
    }

    private boolean useCachedData(final JiraIssue jiraIssue, final TaskData oldTaskData, final boolean forceCache) {
        if (forceCache) {
            return true;
        }
        if (jiraIssue.getUpdated() != null && oldTaskData != null) {
            if (jiraIssue.getUpdated().equals(getDateValue(oldTaskData, JiraAttribute.MODIFICATION_DATE))) {
                return true;
            }
        }
        return false;
    }

    private void addWorklog(final TaskData data, final JiraIssue jiraIssue, final JiraClient client, final TaskData oldTaskData, final boolean forceCache,
            final IProgressMonitor monitor) throws JiraException {
        if (useCachedData(jiraIssue, oldTaskData, forceCache)) {
            if (useCachedInformation(jiraIssue, oldTaskData, forceCache)) {
                if (oldTaskData == null) {
                    // caching forced but no information available
                    data.setPartial(true);
                    return;
                }
                final List<TaskAttribute> attributes = oldTaskData.getAttributeMapper().getAttributesByType(oldTaskData, WorkLogConverter.TYPE_WORKLOG);
                for (final TaskAttribute taskAttribute : attributes) {
                    data.getRoot().deepAddCopy(taskAttribute);
                }
                final TaskAttribute attribute = oldTaskData.getRoot().getAttribute(JiraConstants.ATTRIBUTE_WORKLOG_NOT_SUPPORTED);
                if (attribute != null) {
                    data.getRoot().deepAddCopy(attribute);
                }
                return;
            }
        }
        // try {
        // JiraWorkLog[] remoteWorklogs = client.getWorklogs(jiraIssue.getKey(),
        // monitor);
        final JiraWorkLog[] remoteWorklogs = jiraIssue.getWorklogs();
        if (remoteWorklogs != null) {
            int i = 1;
            for (final JiraWorkLog remoteWorklog : remoteWorklogs) {
                final String attributeId = WorkLogConverter.PREFIX_WORKLOG + "-" + i; //$NON-NLS-1$
                final TaskAttribute attribute = data.getRoot().createAttribute(attributeId);
                attribute.getMetaData().setType(WorkLogConverter.TYPE_WORKLOG);
                new WorkLogConverter().applyTo(remoteWorklog, attribute);
                i++;
            }
        } else {
            data.getRoot().createAttribute(JiraConstants.ATTRIBUTE_WORKLOG_NOT_SUPPORTED);
        }
        // } catch (JiraInsufficientPermissionException e) {
        // // ignore
        // trace(e);
        // data.getRoot().createAttribute(IJiraConstants.ATTRIBUTE_WORKLOG_NOT_SUPPORTED);
        // }
    }

    public void addOperations(final TaskData data, final JiraIssue issue, final JiraClient client, final TaskData oldTaskData, final boolean forceCache,
            final IProgressMonitor monitor) throws JiraException {
        // avoid server round-trips
        if (useCachedInformation(issue, oldTaskData, forceCache)) {
            if (oldTaskData == null) {
                // caching forced but no information available
                data.setPartial(true);
                return;
            }

            final List<TaskAttribute> attributes = oldTaskData.getAttributeMapper().getAttributesByType(oldTaskData, TaskAttribute.TYPE_OPERATION);
            for (final TaskAttribute taskAttribute : attributes) {
                data.getRoot().deepAddCopy(taskAttribute);
            }
            return;
        }

        final JiraStatus status = issue.getStatus();
        String label;
        if (status != null) {
            label = NLS.bind(Messages.JiraTaskDataHandler_Leave_as_X, status.getName());
        } else {
            label = Messages.JiraTaskDataHandler_Leave;
        }
        TaskAttribute attribute = data.getRoot().createAttribute(TaskAttribute.PREFIX_OPERATION + LEAVE_OPERATION);
        TaskOperation.applyTo(attribute, LEAVE_OPERATION, label);
        // set as default
        attribute = data.getRoot().createAttribute(TaskAttribute.OPERATION);
        TaskOperation.applyTo(attribute, LEAVE_OPERATION, label);

        // TODO need more accurate status matching
        // if (!JiraRepositoryConnector.isCompleted(data)) {
        // attribute = operationContainer.createAttribute(REASSIGN_OPERATION);
        // operation = TaskOperation.createFrom(attribute);
        // operation.setLabel("Reassign to");
        // operation.applyTo(attribute);
        //
        // String attributeId = REASSIGN_OPERATION + "::" +
        // JiraAttribute.USER_ASSIGNED.getParamName();
        // TaskAttribute associatedAttribute = createAttribute(data, attributeId);
        // associatedAttribute.setValue(client.getUserName());
        // attribute.putMetaDataValue(TaskAttribute.META_ASSOCIATED_ATTRIBUTE_ID,
        // attributeId);
        // }

        final List<JiraAction> availableActions = client.getAvailableActions(issue.getKey(), monitor);
        if (availableActions != null) {
            for (final JiraAction action : availableActions) {
                attribute = data.getRoot().createAttribute(TaskAttribute.PREFIX_OPERATION + action.getId());
                TaskOperation.applyTo(attribute, action.getId(), action.getName());

                // String[] fields = client.getActionFields(issue.getKey(), action.getId(),
                // monitor);
                final List<JiraIssueField> fields = action.getFields();
                for (final JiraIssueField field : fields) {
                    if (TaskAttribute.RESOLUTION.equals(mapCommonAttributeKey(field.getId()))) {
                        attribute.getMetaData().putValue(TaskAttribute.META_ASSOCIATED_ATTRIBUTE_ID, TaskAttribute.RESOLUTION);
                    }
                    // TODO handle other action fields
                }
            }
        }
    }

    @Override
    public RepositoryResponse postTaskData(final TaskRepository repository, final TaskData taskData, final Set<TaskAttribute> changedAttributes,
            IProgressMonitor monitor) throws CoreException {
        monitor = Policy.monitorFor(monitor);
        try {
            monitor.beginTask(Messages.JiraTaskDataHandler_Sending_task, IProgressMonitor.UNKNOWN);
            final JiraClient client = clientFactory.getJiraClient(repository);
            if (client == null) {
                throw new CoreException(
                        new org.eclipse.core.runtime.Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN, IStatus.ERROR, "Unable to create Jira client", null)); //$NON-NLS-1$
            }
            try {
                if (!client.getCache().hasDetails()) {
                    client.getCache().refreshDetails(monitor);
                }

                JiraIssue issue = buildJiraIssue(taskData);
                if (taskData.isNew()) {
                    // if (issue.getType().isSubTaskType() && issue.getParentId() != null) {
                    // issue = client.createSubTask(issue, monitor);
                    // } else {
                    issue = client.createIssue(issue, monitor);
                    // }
                    if (issue == null) {
                        throw new CoreException(
                                new org.eclipse.core.runtime.Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN, IStatus.OK, "Could not create issue.", null)); //$NON-NLS-1$
                    }

                    if (taskData.getRoot().getAttribute(JiraConstants.ATTRIBUTE_WORKLOG_NOT_SUPPORTED) == null) {
                        postWorkLog(repository, client, taskData, issue, monitor);
                    }

                    // this is severely broken: should return id instead
                    // return issue.getKey();
                    return new RepositoryResponse(ResponseKind.TASK_CREATED, issue.getId());
                } else {
                    final String operationId = getOperationId(taskData);
                    String newComment = getNewComment(taskData);

                    final Set<String> changeIds = new HashSet<>();
                    if (changedAttributes != null) {
                        for (final TaskAttribute ta : changedAttributes) {
                            changeIds.add(ta.getId());
                        }
                    }

                    // check if the visibility of the comment needs to be set
                    JiraComment soapComment = null;
                    final TaskAttribute commentVisibilityAttribute = taskData.getRoot().getMappedAttribute(JiraAttribute.PROJECT_ROLES.id());
                    if (commentVisibilityAttribute != null) {
                        final String commentVisibility = commentVisibilityAttribute.getValue();
                        if (!JiraConstants.NEW_COMMENT_VIEWABLE_BY_ALL.equals(commentVisibility)) {
                            // not relevant for later processing
                            changeIds.remove(JiraAttribute.PROJECT_ROLES.id());

                            if (newComment != null && newComment.length() > 0) {
                                soapComment = new JiraComment();
                                soapComment.setComment(newComment);
                                soapComment.setRoleLevel(commentVisibility);

                                newComment = null;
                            }
                        }
                    }

                    boolean handled = false;
                    boolean advWorkflowHandled = false;

                    if (!handled && changeIds.contains(JiraConstants.WORKLOG_NEW)) {
                        postWorkLog(repository, client, taskData, issue, monitor);

                        changeIds.remove(JiraConstants.WORKLOG_NEW);

                        if (changeIds.size() == 0) {
                            handled = true;
                        }
                    }

                    // if only reassigning do not do the workflow
                    if (!handled && changeIds.contains(TaskAttribute.USER_ASSIGNED)) {
                        final Set<String> anythingElse = new HashSet<>(changeIds);
                        anythingElse.removeAll(Arrays.asList(TaskAttribute.USER_ASSIGNED, TaskAttribute.COMMENT_NEW));
                        if (anythingElse.size() == 0) {
                            // no more changes, so that's a re-assign operation (we can't count on
                            // operationId == REASSIGN_OPERATION)
                            client.assignIssueTo(issue, issue.getAssignee().getId(), newComment, monitor);

                            handled = true;
                        }
                    }

                    // if only adv workflow do not do the standard workflow
                    if (!handled && changeIds.contains(TaskAttribute.OPERATION) && !LEAVE_OPERATION.equals(operationId)) {
                        final Set<String> anythingElse = new HashSet<>(changeIds);
                        anythingElse.removeAll(Arrays.asList(TaskAttribute.OPERATION, TaskAttribute.COMMENT_NEW, TaskAttribute.RESOLUTION));
                        if (anythingElse.size() == 0) {
                            // no more changes, so that's a adv workflow operation
                            client.advanceIssueWorkflow(issue, operationId, null, monitor);
                            if (newComment != null && newComment.length() > 0) {
                                client.addCommentToIssue(issue.getKey(), newComment, monitor);
                            }
                            handled = true;
                            advWorkflowHandled = true;
                        }
                    }

                    // stop progress must be run before issue is updated because assignee can be
                    // changed on update and this will cause stop progress to fail
                    if (!handled && STOP_PROGRESS_OPERATION.equals(operationId)) {
                        client.advanceIssueWorkflow(issue, operationId, null, monitor); // comment will be updated in the normal workflow, so don't post it here
                        advWorkflowHandled = true;
                    }

                    // if only comment was modified do not do the workflow
                    if (!handled //
                            && !JiraRepositoryConnector.isClosed(issue) && taskData.getRoot().getMappedAttribute(JiraConstants.ATTRIBUTE_READ_ONLY) == null
                            && !changeIds.equals(Collections.singleton(TaskAttribute.COMMENT_NEW))
                            && (!STOP_PROGRESS_OPERATION.equals(operationId) || !changeIds.equals(Collections.singleton(TaskAttribute.OPERATION)))) {
                        client.updateIssue(issue, newComment, changeIds.contains(JiraAttribute.ESTIMATE.id()), monitor);
                        handled = true;
                    }

                    // try to at least post the comment (if everything else failed)
                    if (!handled && newComment != null && newComment.length() > 0) {
                        client.addCommentToIssue(issue.getKey(), newComment, monitor);
                        handled = true;
                    } else if (soapComment != null) {
                        // no handling of comments visibility now
                        // client.addCommentToIssue(issue.getKey(), soapComment, monitor);
                        client.addCommentToIssue(issue.getKey(), soapComment.getComment(), monitor);
                        handled = true;
                    }

                    // postWorkLog(repository, client, taskData, issue, monitor);

                    // and do advanced workflow if necessary
                    if (!advWorkflowHandled && !LEAVE_OPERATION.equals(operationId) && !REASSIGN_OPERATION.equals(operationId)
                            && !STOP_PROGRESS_OPERATION.equals(operationId)) {
                        client.advanceIssueWorkflow(issue, operationId, null, monitor); // comment gets updated in the normal workflow already, so don"t post it
                        // a second time
                    }
                    return new RepositoryResponse(ResponseKind.TASK_UPDATED, issue.getId());
                }
            } catch (final JiraException e) {
                final IStatus status = JiraCorePlugin.toStatus(repository, e);
                StatusHandler.log(status);
                throw new CoreException(status);
            }
        } finally {
            monitor.done();
        }
    }

    private void postWorkLog(final TaskRepository repository, final JiraClient client, final TaskData taskData, final JiraIssue issue,
            final IProgressMonitor monitor) throws JiraException {
        final TaskAttribute attribute = taskData.getRoot().getMappedAttribute(WorkLogConverter.ATTRIBUTE_WORKLOG_NEW);
        if (attribute != null) {
            final TaskAttribute submitFlagAttribute = attribute.getAttribute(WorkLogConverter.ATTRIBUTE_WORKLOG_NEW_SUBMIT_FLAG);
            // if flag is set and true, submit
            if (submitFlagAttribute != null && submitFlagAttribute.getValue().equals(String.valueOf(true))) {
                final JiraWorkLog log = new WorkLogConverter().createFrom(attribute);
                client.addWorkLog(issue.getKey(), log, monitor);
            }
        }
    }

    private String getNewComment(final TaskData taskData) {
        String newComment = ""; //$NON-NLS-1$
        final TaskAttribute attribute = taskData.getRoot().getMappedAttribute(TaskAttribute.COMMENT_NEW);
        if (attribute != null) {
            newComment = taskData.getAttributeMapper().getValue(attribute);
        }
        return newComment;
    }

    private String getAssignee(final TaskData taskData) {
        String asignee = ""; //$NON-NLS-1$
        final TaskAttribute attribute = taskData.getRoot().getMappedAttribute(TaskAttribute.USER_ASSIGNED);
        if (attribute != null) {
            asignee = taskData.getAttributeMapper().getValue(attribute);
        }
        return asignee;
    }

    private String getOperationId(final TaskData taskData) {
        String operationId = ""; //$NON-NLS-1$
        final TaskAttribute attribute = taskData.getRoot().getMappedAttribute(TaskAttribute.OPERATION);
        if (attribute != null) {
            operationId = taskData.getAttributeMapper().getValue(attribute);
        }
        if (operationId.length() == 0) {
            operationId = LEAVE_OPERATION;
        }
        return operationId;
    }

    @Override
    public boolean initializeTaskData(final TaskRepository repository, final TaskData data, final ITaskMapping initializationData,
            final IProgressMonitor monitor) throws CoreException {
        if (initializationData == null) {
            return false;
        }
        final String product = initializationData.getProduct();
        if (product == null) {
            return false;
        }
        final JiraClient client = clientFactory.getJiraClient(repository);
        if (!client.getCache().hasDetails()) {
            try {
                client.getCache().refreshDetails(monitor);
            } catch (final JiraException ex) {
                final IStatus status = JiraCorePlugin.toStatus(repository, ex);
                trace(status);
                throw new CoreException(status);
            }
        }
        final JiraProject project = getProject(client, product);
        if (project == null) {
            return false;
        }
        if (!project.hasDetails()) {
            try {
                client.getCache().refreshProjectDetails(project.getId(), monitor);
            } catch (final JiraException e) {
                final IStatus status = JiraCorePlugin.toStatus(repository, e);
                trace(status);
                throw new CoreException(status);
            }
        }

        try {
            initializeTaskData(repository, data, client, project, monitor);
        } catch (final JiraException e) {
            final IStatus status = JiraCorePlugin.toStatus(repository, e);
            trace(status);
            throw new CoreException(status);
        }
        return true;
    }

    private JiraProject getProject(final JiraClient client, final String product) {
        final JiraProject[] projects = client.getCache().getProjects();
        for (final JiraProject project : projects) {
            if (product.equals(project.getName()) || product.equals(project.getKey())) {
                return project;
            }
        }
        return null;
    }

    @Override
    public boolean initializeSubTaskData(final TaskRepository repository, final TaskData taskData, final TaskData parentTaskData,
            final IProgressMonitor monitor) throws CoreException {
        try {
            monitor.beginTask(Messages.JiraTaskDataHandler_Creating_subtask, IProgressMonitor.UNKNOWN);

            final JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
            if (!client.getCache().hasDetails()) {
                client.getCache().refreshDetails(SubMonitor.convert(monitor, 1));
            }

            final TaskAttribute projectAttribute = parentTaskData.getRoot().getAttribute(TaskAttribute.PRODUCT);
            if (projectAttribute == null) {
                throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN, IStatus.OK,
                        "The parent task does not have a valid project.", null)); //$NON-NLS-1$
            }

            final JiraProject project = client.getCache().getProjectById(projectAttribute.getValue());
            initializeTaskData(repository, taskData, client, project, monitor);

            new JiraTaskMapper(taskData).merge(new JiraTaskMapper(parentTaskData));
            taskData.getRoot().getAttribute(JiraAttribute.PROJECT.id()).setValue(project.getId());
            taskData.getRoot().getAttribute(JiraAttribute.DESCRIPTION.id()).setValue(""); //$NON-NLS-1$
            taskData.getRoot().getAttribute(JiraAttribute.SUMMARY.id()).setValue(""); //$NON-NLS-1$

            // set subtask type
            final TaskAttribute typeAttribute = taskData.getRoot().getAttribute(JiraAttribute.TYPE.id());
            typeAttribute.clearOptions();
            final JiraIssueType[] jiraIssueTypes = client.getCache().getIssueTypes();
            for (final JiraIssueType type : jiraIssueTypes) {
                if (type.isSubTaskType()) {
                    typeAttribute.putOption(type.getId(), type.getName());
                }
            }
            final Map<String, String> options = typeAttribute.getOptions();
            if (options.size() == 0) {
                throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN, IStatus.OK,
                        "The repository does not support subtasks.", null)); //$NON-NLS-1$
            } else if (options.size() == 1) {
                typeAttribute.getMetaData().setReadOnly(true);
            }
            typeAttribute.setValue(options.keySet().iterator().next());
            typeAttribute.getMetaData().putValue(JiraConstants.META_SUB_TASK_TYPE, Boolean.TRUE.toString());

            // set parent id
            TaskAttribute attribute = taskData.getRoot().getAttribute(JiraAttribute.PARENT_ID.id());
            attribute.setValue(parentTaskData.getTaskId());
            attribute = taskData.getRoot().getAttribute(JiraAttribute.PARENT_KEY.id());
            attribute.setValue(parentTaskData.getRoot().getAttribute(JiraAttribute.ISSUE_KEY.id()).getValue());

            return true;
        } catch (final JiraException e) {
            final IStatus status = JiraCorePlugin.toStatus(repository, e);
            trace(status);
            throw new CoreException(status);
        } finally {
            monitor.done();
        }
    }

    @Override
    public boolean canInitializeSubTaskData(final TaskRepository taskRepository, final ITask task) {
        // for backwards compatibility with earlier versions that did not set the
        // subtask flag in
        // JiraRepositoryConnector.updateTaskFromTaskData() return true as a fall-back
        final String value = task.getAttribute(JiraConstants.META_SUB_TASK_TYPE);
        return value == null ? true : !Boolean.parseBoolean(value);
    }

    public static JiraIssue buildJiraIssue(final TaskData taskData) {
        final JiraIssue issue = new JiraIssue();
        issue.setId(taskData.getTaskId());
        issue.setKey(getAttributeValue(taskData, JiraAttribute.ISSUE_KEY));
        issue.setSummary(getAttributeValue(taskData, JiraAttribute.SUMMARY));
        issue.setDescription(getAttributeValue(taskData, JiraAttribute.DESCRIPTION));

        // TODO sync due date between jira and local planning
        issue.setDue(getDateValue(taskData, JiraAttribute.DUE_DATE));

        final String parentId = getAttributeValue(taskData, JiraAttribute.PARENT_ID);
        if (parentId != null) {
            issue.setParentId(parentId);
        }

        final String parentKey = getAttributeValue(taskData, JiraAttribute.PARENT_KEY);
        if (parentKey != null) {
            issue.setParentKey(parentKey);
        }

        final String securityLevelId = getAttributeValue(taskData, JiraAttribute.SECURITY_LEVEL);
        if (securityLevelId != null) {
            issue.setSecurityLevel(new JiraSecurityLevel(securityLevelId));
        }

        String estimate = getAttributeValue(taskData, JiraAttribute.ESTIMATE);
        if (estimate != null) {
            try {
                issue.setEstimate(Long.parseLong(estimate));
            } catch (final NumberFormatException e) {
            }
        }

        estimate = getAttributeValue(taskData, JiraAttribute.INITIAL_ESTIMATE);
        if (estimate != null) {
            try {
                issue.setInitialEstimate(Long.parseLong(estimate));
            } catch (final NumberFormatException e) {
            }
        }

        issue.setProject(new JiraProject(getAttributeValue(taskData, JiraAttribute.PROJECT)));

        final TaskAttribute typeAttribute = getAttribute(taskData, JiraAttribute.TYPE);
        final boolean subTaskType = typeAttribute != null ? hasSubTaskType(typeAttribute) : false;
        final String typeId = typeAttribute.getValue();
        final String typeName = typeAttribute.getOption(typeId);
        final JiraIssueType issueType = new JiraIssueType(typeId, typeName, subTaskType);
        issue.setType(issueType);

        issue.setStatus(new JiraStatus(getAttributeValue(taskData, JiraAttribute.STATUS)));

        final TaskAttribute componentsAttribute = taskData.getRoot().getMappedAttribute(JiraConstants.ATTRIBUTE_COMPONENTS);
        if (componentsAttribute != null) {
            final ArrayList<JiraComponent> components = new ArrayList<>();
            for (final String value : componentsAttribute.getValues()) {
                final JiraComponent component = new JiraComponent(value);
                component.setName(componentsAttribute.getOption(value));
                components.add(component);
            }
            issue.setComponents(components.toArray(new JiraComponent[components.size()]));
        }

        final TaskAttribute fixVersionAttr = taskData.getRoot().getMappedAttribute(JiraConstants.ATTRIBUTE_FIXVERSIONS);
        if (fixVersionAttr != null) {
            final ArrayList<JiraVersion> fixVersions = new ArrayList<>();
            for (final String value : fixVersionAttr.getValues()) {
                final JiraVersion version = new JiraVersion(value, fixVersionAttr.getOption(value));
                fixVersions.add(version);
            }
            issue.setFixVersions(fixVersions.toArray(new JiraVersion[fixVersions.size()]));
        }

        final TaskAttribute affectsVersionAttr = taskData.getRoot().getMappedAttribute(JiraConstants.ATTRIBUTE_AFFECTSVERSIONS);
        if (affectsVersionAttr != null) {
            final ArrayList<JiraVersion> affectsVersions = new ArrayList<>();
            for (final String value : affectsVersionAttr.getValues()) {
                final JiraVersion version = new JiraVersion(value, affectsVersionAttr.getOption(value));
                affectsVersions.add(version);
            }
            issue.setReportedVersions(affectsVersions.toArray(new JiraVersion[affectsVersions.size()]));
        }

        final ITaskAttributeMapper2 jiraMapper = (ITaskAttributeMapper2) taskData.getAttributeMapper();
        final String reporter = getAttributeValue(taskData, JiraAttribute.USER_REPORTER);
        final BasicUser rep = new BasicUser(null, reporter, "");
        issue.setReporter(rep);

        final String externalId = getAttributeValue(taskData, JiraAttribute.USER_ASSIGNED);
        final TaskAttribute assigneeAttr = taskData.getRoot().getAttribute(JiraAttribute.USER_ASSIGNED.id());
        final BasicUser assignee = getUser(taskData, assigneeAttr, externalId);
        final BasicUser ass = BasicUser.UNASSIGNED_USER;
        // JiraRepositoryConnector.getAssigneeFromAttribute(assignee)
        issue.setAssignee(assignee);

        issue.setEnvironment(getAttributeValue(taskData, JiraAttribute.ENVIRONMENT));
        final String priorityId = getAttributeValue(taskData, JiraAttribute.PRIORITY);
        if (priorityId != null) {
            issue.setPriority(new JiraPriority(priorityId));
        }

        final ArrayList<JiraCustomField> customFields = new ArrayList<>();
        for (final TaskAttribute attribute : taskData.getRoot().getAttributes().values()) {
            if (attribute.getId().startsWith(JiraConstants.ATTRIBUTE_CUSTOM_PREFIX)) {
                final String id = attribute.getId().substring(JiraConstants.ATTRIBUTE_CUSTOM_PREFIX.length());
                final String type = attribute.getMetaData().getValue(JiraConstants.META_TYPE);
                final String name = attribute.getMetaData().getLabel().substring(0, attribute.getMetaData().getLabel().length() - 1);
                final JiraCustomField field = new JiraCustomField(id, type, name, attribute.getValues());
                customFields.add(field);
            }
        }
        issue.setCustomFields(customFields.toArray(new JiraCustomField[customFields.size()]));

        final String resolutionId = getAttributeValue(taskData, JiraAttribute.RESOLUTION);
        if (resolutionId != null) {
            issue.setResolution(new JiraResolution(resolutionId, resolutionId));
        }

        final String labels = getAttributeValue(taskData, JiraAttribute.LABELS);
        if (labels != null) {
            issue.setLabels(StringUtils.split(labels));
        }

        return issue;
    }

    public static boolean hasSubTaskType(final TaskAttribute typeAttribute) {
        return Boolean.parseBoolean(typeAttribute.getMetaData().getValue(JiraConstants.META_SUB_TASK_TYPE));
    }

    private static TaskAttribute getAttribute(final TaskData taskData, final JiraAttribute key) {
        return taskData.getRoot().getAttribute(key.id());
    }

    private static String getAttributeValue(final TaskData taskData, final JiraAttribute key) {
        final TaskAttribute attribute = taskData.getRoot().getAttribute(key.id());
        return attribute != null ? attribute.getValue() : null;
    }

    private static Instant getDateValue(final TaskData data, final JiraAttribute key) {
        final TaskAttribute attribute = data.getRoot().getAttribute(key.id());
        return attribute != null && data.getAttributeMapper().getDateValue(attribute) != null ? data.getAttributeMapper().getDateValue(attribute).toInstant()
                : null;
    }

    private static BasicUser getUser(final TaskData data, final TaskAttribute Attribute, final String displayName) {
        final ITaskAttributeMapper2 jiraMapper = (ITaskAttributeMapper2) data.getAttributeMapper();
        return jiraMapper.lookupExternalId(Attribute, displayName);
    }

    private static void trace(final IStatus status) {
        if (TRACE_ENABLED) {
            StatusHandler.log(status);
        }
    }

    private static void trace(final Exception e) {
        if (TRACE_ENABLED) {
            StatusHandler.log(new Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN, "Error receiving infromation from JIRA", e)); //$NON-NLS-1$
        }
    }

    @Override
    public TaskAttributeMapper getAttributeMapper(final TaskRepository taskRepository) {
        final JiraClient client = clientFactory.getJiraClient(taskRepository);
        return new JiraAttributeMapper(taskRepository, client);
    }

    @Override
    public void migrateTaskData(final TaskRepository taskRepository, final TaskData taskData) {
        final String taskDataVersion = taskData.getVersion();
        final JiraServerVersion version = new JiraServerVersion(taskDataVersion != null ? taskDataVersion : "0.0"); //$NON-NLS-1$
        // 1.0: the value was stored in the attribute rather than the key
        if (version.isLessThanOrEquals(TASK_DATA_VERSION_1_0)) {
            for (final TaskAttribute attribute : taskData.getRoot().getAttributes().values()) {
                if (TaskAttribute.PRODUCT.equals(attribute.getId())) {
                    final String projectName = attribute.getValue();
                    final Map<String, String> options = taskData.getAttributeMapper().getOptions(attribute);
                    for (final String key : options.keySet()) {
                        final String value = options.get(key);
                        if (projectName.equals(value)) {
                            attribute.setValue(key);
                        }
                        attribute.putOption(key, value);
                    }
                } else if (TaskAttribute.USER_ASSIGNED.equals(attribute.getId())) {
                    attribute.getMetaData().setReadOnly(false);
                } else {
                    final JiraFieldType type = JiraFieldType.fromKey(attribute.getMetaData().getValue(JiraConstants.META_TYPE));
                    if ((JiraFieldType.SELECT == type || JiraFieldType.MULTISELECT == type) && !attribute.getOptions().isEmpty()) {
                        // convert option values to keys: version 1.0 stored value whereas 2.0 stores
                        // keys
                        final Set<String> values = new HashSet<>(attribute.getValues());
                        attribute.clearValues();
                        final Map<String, String> options = attribute.getOptions();
                        for (final String key : options.keySet()) {
                            if (values.contains(options.get(key))) {
                                attribute.addValue(key);
                            }
                        }
                    }
                }
            }
        }
        // 2.0: the type was not always set
        if (version.isLessThanOrEquals(TASK_DATA_VERSION_2_0)) {
            final Collection<TaskAttribute> attributes = new ArrayList<>(taskData.getRoot().getAttributes().values());
            for (final TaskAttribute attribute : attributes) {
                if (attribute.getId().startsWith(TaskAttribute.PREFIX_OPERATION)) {
                    if (REASSIGN_OPERATION.equals(attribute.getValue())) {
                        taskData.getRoot().removeAttribute(attribute.getId());
                        continue;
                    } else {
                        final TaskAttribute associatedAttribute = taskData.getAttributeMapper().getAssoctiatedAttribute(attribute);
                        if (associatedAttribute != null && "resolution".equals(associatedAttribute.getId())) { //$NON-NLS-1$
                            final TaskAttribute resolutionAttribute = taskData.getRoot().getAttribute(JiraAttribute.RESOLUTION.id());
                            if (resolutionAttribute != null) {
                                final Map<String, String> options = associatedAttribute.getOptions();
                                for (final String key : options.keySet()) {
                                    resolutionAttribute.putOption(key, options.get(key));
                                }
                                resolutionAttribute.getMetaData().setType(TaskAttribute.TYPE_SINGLE_SELECT);
                                resolutionAttribute.getMetaData().setReadOnly(false);
                            }
                            attribute.getMetaData().putValue(TaskAttribute.META_ASSOCIATED_ATTRIBUTE_ID, JiraAttribute.RESOLUTION.id());
                            attribute.removeAttribute(associatedAttribute.getId());
                        }
                    }
                } else if (attribute.getId().equals(JiraAttribute.TYPE.id())) {
                    if (attribute.getOptions().isEmpty()) {
                        // sub task
                        final JiraClient client = clientFactory.getJiraClient(taskRepository);
                        final JiraIssueType[] jiraIssueTypes = client.getCache().getIssueTypes();
                        for (final JiraIssueType type : jiraIssueTypes) {
                            if (attribute.getValue().equals(type.getName())) {
                                attribute.putOption(type.getId(), type.getName());
                                attribute.setValue(type.getId());
                                break;
                            }
                        }
                    }
                }
                attribute.getMetaData().setType(getType(attribute));
            }
        }
        // migration for v2.1 is now handled in the framework
        // store long values instead of formatted time spans
        if (version.isLessThanOrEquals(TASK_DATA_VERSION_2_2)) {
            for (final TaskAttribute attribute : taskData.getRoot().getAttributes().values()) {
                final JiraTimeFormat format = new JiraTimeFormat();
                if (isTimeSpanAttribute(attribute)) {
                    final String value = attribute.getValue();
                    if (value.length() > 0) {
                        try {
                            Long.parseLong(value);
                        } catch (final NumberFormatException e) {
                            try {
                                attribute.setValue(String.valueOf(format.parse(value)));
                            } catch (final ParseException e1) {
                                attribute.setValue(""); //$NON-NLS-1$
                            }
                        }
                    }
                }
            }
        }
        if (version.isLessThanOrEquals(TASK_DATA_VERSION_CURRENT)) {
            taskData.setVersion(TASK_DATA_VERSION_CURRENT.toString());
        }
    }

    public static boolean isTimeSpanAttribute(final TaskAttribute attribute) {
        return JiraAttribute.INITIAL_ESTIMATE.id().equals(attribute.getId()) || JiraAttribute.ESTIMATE.id().equals(attribute.getId())
                || JiraAttribute.ACTUAL.id().equals(attribute.getId());
    }

    private String getType(final TaskAttribute taskAttribute) {
        if (JiraAttribute.DESCRIPTION.id().equals(taskAttribute.getId()) || JiraAttribute.COMMENT_NEW.id().equals(taskAttribute.getId())) {
            return TaskAttribute.TYPE_LONG_RICH_TEXT;
        }
        if (JiraAttribute.SUMMARY.id().equals(taskAttribute.getId())) {
            return TaskAttribute.TYPE_SHORT_RICH_TEXT;
        }
        if (TaskAttribute.OPERATION.equals(taskAttribute.getId()) || taskAttribute.getId().startsWith(TaskAttribute.PREFIX_OPERATION)) {
            return TaskAttribute.TYPE_OPERATION;
        }
        if (taskAttribute.getId().startsWith(TaskAttribute.PREFIX_COMMENT)) {
            return TaskAttribute.TYPE_COMMENT;
        }
        if (taskAttribute.getId().startsWith(TaskAttribute.PREFIX_ATTACHMENT)) {
            return TaskAttribute.TYPE_ATTACHMENT;
        }
        JiraFieldType fieldType = null;
        if (JiraAttribute.CREATION_DATE.id().equals(taskAttribute.getId()) || JiraAttribute.DUE_DATE.id().equals(taskAttribute.getId())
                || JiraAttribute.MODIFICATION_DATE.id().equals(taskAttribute.getId())) {
            fieldType = JiraFieldType.DATE;
            taskAttribute.getMetaData().putValue(JiraConstants.META_TYPE, fieldType.getKey());
        }
        if (fieldType == null) {
            fieldType = JiraFieldType.fromKey(taskAttribute.getMetaData().getValue(JiraConstants.META_TYPE));
        }
        if (fieldType.getTaskType() != null) {
            return fieldType.getTaskType();
        }
        fieldType = JiraAttribute.valueById(taskAttribute.getId()).getType();
        if (fieldType.getTaskType() != null) {
            return fieldType.getTaskType();
        }
        final String existingType = taskAttribute.getMetaData().getType();
        if (existingType != null) {
            return existingType;
        }
        return TaskAttribute.TYPE_SHORT_TEXT;
    }

    /**
     * Translates JIRA attributes to Mylyn values
     *
     * @param key
     * @return
     */
    public String mapCommonAttributeKey(final String key) {
        if ("summary".equals(key)) { //$NON-NLS-1$
            return JiraAttribute.SUMMARY.id();
        } else if ("description".equals(key)) { //$NON-NLS-1$
            return JiraAttribute.DESCRIPTION.id();
        } else if ("priority".equals(key)) { //$NON-NLS-1$
            return JiraAttribute.PRIORITY.id();
        } else if ("resolution".equals(key)) { //$NON-NLS-1$
            return JiraAttribute.RESOLUTION.id();
        } else if ("assignee".equals(key)) { //$NON-NLS-1$
            return JiraAttribute.USER_ASSIGNED.id();
        } else if ("environment".equals(key)) { //$NON-NLS-1$
            return JiraAttribute.ENVIRONMENT.id();
        } else if ("issuetype".equals(key)) { //$NON-NLS-1$
            return JiraAttribute.TYPE.id();
        } else if ("components".equals(key)) { //$NON-NLS-1$
            return JiraAttribute.COMPONENTS.id();
        } else if ("versions".equals(key)) { //$NON-NLS-1$
            return JiraAttribute.AFFECTSVERSIONS.id();
        } else if ("fixVersions".equals(key)) { //$NON-NLS-1$
            return JiraAttribute.FIXVERSIONS.id();
        } else if ("timetracking".equals(key)) { //$NON-NLS-1$
            return JiraAttribute.ESTIMATE.id();
        } else if ("duedate".equals(key)) { //$NON-NLS-1$
            return JiraAttribute.DUE_DATE.id();
        } else if ("labels".equals(key)) {
            return JiraAttribute.LABELS.id();
        }
        if (key.startsWith("issueLink")) { //$NON-NLS-1$
            return JiraConstants.ATTRIBUTE_LINK_PREFIX + key;
        }
        if (key.startsWith("customfield")) { //$NON-NLS-1$
            return JiraConstants.ATTRIBUTE_CUSTOM_PREFIX + key;
        }
        if ("security".equals(key)) { //$NON-NLS-1$
            return JiraAttribute.SECURITY_LEVEL.id();
        }
        return key;
    }
}