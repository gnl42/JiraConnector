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

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraComponent;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueType;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraPriority;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProject;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraResolution;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraVersion;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClient;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraException;
import me.glindholm.connector.eclipse.internal.jira.core.util.JiraUtil;
import me.glindholm.jira.rest.client.api.domain.BasicUser;
import me.glindholm.jira.rest.client.api.domain.User;

/**
 * @author Steffen Pingel
 */
public class JiraAttributeMapper extends TaskAttributeMapper implements ITaskAttributeMapper2 {

    private static final String PERSON_DISPLAY_NAME = "displayName";
    private static final String PERSON_SELF = "self";
    private static final String PERSON_ACCOUNT_ID = "accountId";
    private static final String PERSON_NAME = "name";
    private static final String PERSON_EXTERNAL_ID = "externalId";
    private static final String PERSON_EMAIL = "email";
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
    public BasicUser getRepositoryUser(final TaskAttribute attribute) {
        final String id = attribute.getValue();
        URI self = null;
        final TaskAttribute attrValue = attribute.getAttribute(PERSON_SELF);
        if (attrValue != null && attrValue.getValue() != null) {
            try {
                self = new URI(attrValue.getValue());
            } catch (final URISyntaxException e) {
            }
        }
        final BasicUser user = new BasicUser(self, getNullableValue(attribute.getAttribute(PERSON_NAME)),
                getNullableValue(attribute.getAttribute(PERSON_DISPLAY_NAME)), getNullableValue(attribute.getAttribute(PERSON_ACCOUNT_ID)),
                getNullableValue(attribute.getAttribute(PERSON_EMAIL)), true);
        return user;
    }

    @Override
    public IRepositoryPerson createPerson(final BasicUser user) {
        final IRepositoryPerson person = getTaskRepository().createPerson(user.getExternalId());
        person.setName(user.getDisplayName());
        person.setAttribute(PERSON_EMAIL, user.getEmailAddress());
        person.setAttribute(PERSON_ACCOUNT_ID, user.getAccountId());
        person.setAttribute(PERSON_NAME, user.getName());

        return person;
    }

    private static String getNullableValue(@Nullable final TaskAttribute attribute) {
        if (attribute != null) {
            return attribute.getValue();
        } else {
            return null;
        }
    }

    @Override
    public void setRepositoryUser(@NonNull final TaskAttribute taskAttribute, @NonNull final BasicUser user) {
        setValue(taskAttribute, user.getId());

        setValue(taskAttribute, TaskAttribute.PERSON_NAME, user.getDisplayName());
        setValue(taskAttribute, PERSON_DISPLAY_NAME, user.getDisplayName());
        setValue(taskAttribute, PERSON_EMAIL, user.getEmailAddress());
        setValue(taskAttribute, PERSON_NAME, user.getName());
        setValue(taskAttribute, PERSON_ACCOUNT_ID, user.getAccountId());
        setValue(taskAttribute, PERSON_SELF, user.getSelf());
    }

    public void setValue(@NonNull final TaskAttribute parent, @NonNull final String attributeName, @Nullable final String value) {
        if (value != null) {
            parent.createAttribute(attributeName).setValue(value);
        }
    }

    public void setValue(@NonNull final TaskAttribute parent, @NonNull final String attributeName, @Nullable final URI value) {
        if (value != null) {
            parent.createAttribute(attributeName).setValue(value.toString());
        }
    }

    @Override
    public IRepositoryPerson getRepositoryPerson(@NonNull final TaskAttribute taskAttribute) {
        return super.getRepositoryPerson(taskAttribute);
    }

    @Override
    public void setRepositoryPerson(@NonNull final TaskAttribute taskAttribute, @NonNull final IRepositoryPerson person) {
        super.setRepositoryPerson(taskAttribute, person);
    }

    @Override
    public Map<String, String> getOptions(final TaskAttribute attribute) {
        Map<String, String> options = null;
        if (client.getCache().hasDetails()) {
            if (JiraAttribute.USER_ASSIGNED.id().equals(attribute.getId())) {
                final JiraProject project = findProject(attribute);

                Map<String, BasicUser> users = project.getAssignables();
                if (users == null || users.isEmpty()) {
                    try {
                        users = project.setAssignables(client.getProjectAssignables(project.getKey()));
                    } catch (final JiraException e) {
                        return Collections.EMPTY_MAP;
                    }
                }

                try {
                    final List<User> currentUsers = client.getProjectAssignables(project.getKey());
                    options = new LinkedHashMap<>();
                    project.addAssignables(currentUsers);
                    for (final BasicUser user : users.values()) {
                        options.put(user.getExternalId(), user.getDisplayName());
                    }

                    return options;
                } catch (final JiraException e) {
                    return Collections.EMPTY_MAP;
                }

            } else {
                options = getRepositoryOptions(attribute);
            }

        }
        return options != null ? options : super.getOptions(attribute);

    }

    private JiraProject findProject(final TaskAttribute attribute) {
        final TaskAttribute projectAttribute = attribute.getTaskData().getRoot().getMappedAttribute(JiraAttribute.PROJECT.id());
        final JiraProject project = client.getCache().getProjectById(projectAttribute.getValue());
        return project;
    }

    @Override
    public BasicUser lookupExternalId(final TaskAttribute attribute, final String externalId) {
        final JiraProject project = findProject(attribute);
        for (final BasicUser user : project.getAssignables().values()) {
            if (user.getExternalId().equals(externalId)) {
                return user;
            }
        }
        return BasicUser.UNASSIGNED_USER;
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

    @Override
    public JiraClient getClient() {
        return client;
    }

}