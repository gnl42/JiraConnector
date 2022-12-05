/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core.service.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.osgi.util.NLS;

import me.glindholm.connector.eclipse.internal.jira.core.JiraAttribute;
import me.glindholm.connector.eclipse.internal.jira.core.JiraCorePlugin;
import me.glindholm.connector.eclipse.internal.jira.core.JiraFieldType;
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
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraNamedFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraPriority;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProject;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraResolution;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraSecurityLevel;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraServerInfo;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraServerVersion;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraStatus;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraSubtask;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraVersion;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClientCache;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraException;
import me.glindholm.jira.rest.client.api.JiraRestClient;
import me.glindholm.jira.rest.client.api.domain.Attachment;
import me.glindholm.jira.rest.client.api.domain.BasicComponent;
import me.glindholm.jira.rest.client.api.domain.BasicIssue;
import me.glindholm.jira.rest.client.api.domain.BasicPriority;
import me.glindholm.jira.rest.client.api.domain.BasicProject;
import me.glindholm.jira.rest.client.api.domain.BasicUser;
import me.glindholm.jira.rest.client.api.domain.BasicWatchers;
import me.glindholm.jira.rest.client.api.domain.CimFieldInfo;
import me.glindholm.jira.rest.client.api.domain.Comment;
import me.glindholm.jira.rest.client.api.domain.CustomFieldOption;
import me.glindholm.jira.rest.client.api.domain.Filter;
import me.glindholm.jira.rest.client.api.domain.Issue;
import me.glindholm.jira.rest.client.api.domain.IssueField;
import me.glindholm.jira.rest.client.api.domain.IssueLink;
import me.glindholm.jira.rest.client.api.domain.IssueType;
import me.glindholm.jira.rest.client.api.domain.Priority;
import me.glindholm.jira.rest.client.api.domain.Resolution;
import me.glindholm.jira.rest.client.api.domain.SecurityLevel;
import me.glindholm.jira.rest.client.api.domain.ServerInfo;
import me.glindholm.jira.rest.client.api.domain.Status;
import me.glindholm.jira.rest.client.api.domain.Subtask;
import me.glindholm.jira.rest.client.api.domain.Transition;
import me.glindholm.jira.rest.client.api.domain.Version;
import me.glindholm.jira.rest.client.api.domain.Visibility;
import me.glindholm.jira.rest.client.api.domain.Watchers;
import me.glindholm.jira.rest.client.api.domain.Worklog;
import me.glindholm.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import me.glindholm.jira.rest.client.api.domain.input.FieldInput;
import me.glindholm.jira.rest.client.api.domain.input.WorklogInput;
import me.glindholm.jira.rest.client.api.domain.input.WorklogInputBuilder;

/**
 * @author Jacek Jaroczynski
 */
public class JiraRestConverter {

    private JiraRestConverter() throws Exception {
        throw new Exception("Utility class"); //$NON-NLS-1$
    }

    public static JiraProject[] convertProjects(final List<BasicProject> allProjects) {
        final List<JiraProject> projects = new ArrayList<>();
        for (final BasicProject basicProject : allProjects) {
            projects.add(convert(basicProject));
        }
        return projects.toArray(new JiraProject[projects.size()]);
    }

    private static JiraProject convert(final BasicProject basicProject) {
        final JiraProject project = new JiraProject();

        project.setName(basicProject.getName());
        project.setKey(basicProject.getKey());
        project.setId(basicProject.getId().toString());

        return project;
    }

    public static JiraResolution[] convertResolutions(final List<Resolution> allResolutions) {
        final List<JiraResolution> resolutions = new ArrayList<>();

        for (final Resolution resolution : allResolutions) {
            resolutions.add(convert(resolution));
        }

        return resolutions.toArray(new JiraResolution[resolutions.size()]);
    }

    private static JiraResolution convert(final Resolution resolution) {
        return new JiraResolution(resolution.getId().toString(), resolution.getName(), resolution.getDescription(), null);
    }

    public static JiraPriority[] convertPriorities(final List<Priority> allPriorities) {
        final List<JiraPriority> priorities = new ArrayList<>();

        for (final Priority priority : allPriorities) {
            priorities.add(convert(priority));
        }

        return priorities.toArray(new JiraPriority[priorities.size()]);
    }

    private static JiraPriority convert(final Priority priority) {
        final JiraPriority outPriority = new JiraPriority(priority.getId().toString());

        outPriority.setName(priority.getName());
        outPriority.setDescription(priority.getDescription());
        outPriority.setColour(priority.getStatusColor());
        outPriority.setIcon(priority.getIconUri().toString());
        outPriority.setSelf(priority.getSelf());

        return outPriority;
    }

    public static JiraIssue convertIssue(final JiraRestClient restClient, final Issue rawIssue, final JiraClientCache cache, final String url,
            final IProgressMonitor monitor) throws JiraException {
        final JiraIssue issue = convert(rawIssue);

        issue.setRawIssue(rawIssue);
        issue.setCustomFields(getCustomFieldsFromIssue(rawIssue));
        issue.setEditableFields(getEditableFieldsFromIssue(rawIssue));

        // setAllowValuesForCustomFields(jiraIssue.getCustomFields(),
        // jiraIssue.getEditableFields());

        final JiraProject project = cache.getProjectByKey(rawIssue.getProject().getKey());
        issue.setProject(project);
        if (project != null && !project.hasDetails()) {
            cache.refreshProjectDetails(project, monitor);
        } else if (project == null) {
            throw new JiraException(NLS.bind("Project with key {0} not found in local cache. Please refresh repository configuration.", //$NON-NLS-1$
                    rawIssue.getProject().getKey()));
        }

        issue.setId(rawIssue.getId().toString());
        issue.setSelf(rawIssue.getSelf());
        issue.setKey(rawIssue.getKey());
        issue.setSummary(rawIssue.getSummary());
        issue.setDescription(rawIssue.getDescription());

        if (rawIssue.getIssueType().isSubtask()) {
            issue.setParentKey(rawIssue.getParent().getKey());
            issue.setParentId(rawIssue.getParent().getId() + "");
        }

        if (rawIssue.getPriority() != null) {
            issue.setPriority(cache.getPriorityById(rawIssue.getPriority().getId().toString()));
        } else if (cache.getPriorities().length > 0) {
            issue.setPriority(cache.getPriorities()[0]);
        } else {
            issue.setPriority(null);
            StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN,
                    NLS.bind("Found issue with empty priority: {0}", rawIssue.getKey())));
        }

        issue.setStatus(cache.getStatusById(rawIssue.getStatus().getId().toString()));

        final BasicUser assignee = rawIssue.getAssignee();
        if (assignee != null) {
            issue.setAssignee(assignee);
        }

        final BasicUser reprter = rawIssue.getReporter();
        if (reprter != null) {
            issue.setReporter(reprter);
        }

        issue.setResolution(rawIssue.getResolution() == null ? null : cache.getResolutionById(rawIssue.getResolution().getId().toString()));

        if (rawIssue.getTimeTracking() != null) {
            if (rawIssue.getTimeTracking().getOriginalEstimateMinutes() != null) {
                issue.setInitialEstimate(rawIssue.getTimeTracking().getOriginalEstimateMinutes() * 60);
            }
            if (rawIssue.getTimeTracking().getRemainingEstimateMinutes() != null) {
                issue.setEstimate(rawIssue.getTimeTracking().getRemainingEstimateMinutes() * 60);
            }
            if (rawIssue.getTimeTracking().getTimeSpentMinutes() != null) {
                issue.setActual(rawIssue.getTimeTracking().getTimeSpentMinutes() * 60);
            }
        }

        // TODO Find some way to check this
        final IssueField security = rawIssue.getField(JiraRestFields.SECURITY);
        if (security != null && security.getValue() != null && security.getValue() instanceof JSONObject) {
            final JSONObject json = (JSONObject) security.getValue();

            try {
                final String id = json.getString(JiraRestFields.ID);
                final String name = json.getString(JiraRestFields.NAME);

                final JiraSecurityLevel securityLevel = new JiraSecurityLevel(id, name);

                issue.setSecurityLevel(securityLevel);
            } catch (final JSONException e) {
                throw new JiraException(e);
            }
        }

        issue.setCreated(rawIssue.getCreationDate().toInstant());
        issue.setUpdated(rawIssue.getUpdateDate().toInstant());

        if (project != null && project.getIssueTypeById(rawIssue.getIssueType().getId().toString()) != null) {
            issue.setType(project.getIssueTypeById(rawIssue.getIssueType().getId().toString()));
        } else {
            issue.setType(cache.getIssueTypeById(rawIssue.getIssueType().getId().toString()));
        }

        if (rawIssue.getSubtasks() != null) {
            issue.setSubtasks(convert(rawIssue.getSubtasks()));
        }

        issue.setType(convert(rawIssue.getIssueType()));
        issue.setUrl(url + "/browse/" + rawIssue.getKey()); //$NON-NLS-1$

        if (rawIssue.getComponents() != null) {
            issue.setComponents(convertComponents(rawIssue.getComponents()));
        }

        final IssueField env = rawIssue.getField(JiraRestFields.ENVIRONMENT);
        if (env != null && env.getValue() != null) {
            issue.setEnvironment(env.getValue().toString());
        } else {
            // hack: empty value is necessary to display environment field in the issue
            // editor
            issue.setEnvironment(""); //$NON-NLS-1$
        }

        if (rawIssue.getAffectedVersions() != null) {
            issue.setReportedVersions(convertVersions(rawIssue.getAffectedVersions()));
        }
        if (rawIssue.getFixVersions() != null) {
            issue.setFixVersions(convertVersions(rawIssue.getFixVersions()));
        }

        if (isVersionMissingInProjectCache(rawIssue.getAffectedVersions(), rawIssue.getFixVersions(), cache.getProjectByKey(rawIssue.getProject().getKey()),
                monitor)) {
            cache.refreshProjectDetails(issue.getProject(), monitor);
        }

        final OffsetDateTime dueDate = rawIssue.getDueDate();
        if (dueDate != null) {
            issue.setDue(dueDate.toInstant());
        } else {
            issue.setDue(null);
        }

        if (rawIssue.getIssueLinks() != null) {
            issue.setIssueLinks(convertIssueLinks(rawIssue.getIssueLinks()));
        }

        if (rawIssue.getComments() != null) {
            issue.setComments(convertComments(rawIssue.getComments()));
        }

        if (rawIssue.getAttachments() != null) {
            issue.setAttachments(convertAttachments(rawIssue.getAttachments()));
        }

        if (rawIssue.getWorklogs() != null) {
            issue.setWorklogs(convertWorklogs(rawIssue.getWorklogs()));
        }

        issue.setRank(getRankFromIssue(rawIssue));

        if (rawIssue.getLabels() != null) {
            issue.setLabels(rawIssue.getLabels().toArray(new String[rawIssue.getLabels().size()]));
        }

        if (rawIssue.getVotes() != null) {
            issue.setVotes(rawIssue.getVotes().getVotes());
        }

        final BasicWatchers watched = rawIssue.getWatched();
        issue.setWatched(watched.isWatching());

        if (issue.isWatched()) {
            try {
                final Watchers watchers = restClient.getIssueClient().getWatchers(watched.getSelf()).get();
                issue.setWatchers(watchers);
            } catch (InterruptedException | ExecutionException e) {
                throw new JiraException(e);
            }
        }
        return issue;
    }

    // private static void setAllowValuesForCustomFields(CustomField[] customFields,
    // IssueField[] editableFields) {
    // Map<String, IssueField> editableFieldsMap = new HashMap<String,
    // IssueField>(editableFields.length + 1, 1);
    //
    // // transform editable fields into HasMap
    // for (IssueField editableField : editableFields) {
    // editableFieldsMap.put(editableField.getId(), editableField);
    // }
    //
    // for (CustomField customField : customFields) {
    // customField.setAllowedValues(editableFieldsMap.get(customField.getId()).getAlloweValues());
    // }
    // }

    private static boolean isVersionMissingInProjectCache(final List<Version> affectedVersions, final List<Version> fixVersions, final JiraProject project,
            final IProgressMonitor monitor) {

        if (affectedVersions != null) {
            for (final Version affectedVersion : affectedVersions) {
                if (project.getVersion(affectedVersion.getName()) == null) {
                    return true;
                }
            }
        }

        if (fixVersions != null) {
            for (final Version fixVersion : fixVersions) {
                if (project.getVersion(fixVersion.getName()) == null) {
                    return true;
                }
            }
        }

        return false;

    }

    private static JiraCustomField[] getCustomFieldsFromIssue(final Issue issue) throws JiraException {
        final List<JiraCustomField> customFields = new ArrayList<>(issue.getFields().size());
        for (final IssueField issueField : issue.getFields()) {

            final String fieldId = issueField.getId();
            if (fieldId.startsWith("customfield") && issueField.getValue() != null) { //$NON-NLS-1$
                final Map<String, CimFieldInfo> metadata = issue.getMetadata();
                final CimFieldInfo cim = metadata.get(fieldId);

                if (cim != null) {
                    final String longType = cim.getSchema().getCustom();
                    if (longType != null) {
                        final JiraCustomField customField = generateCustomField(issueField, longType);
                        if (customField != null) {
                            customFields.add(customField);
                        }
                    } else {
                        StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN,
                                NLS.bind("Unable to parse type information (edit meta) for field [{0}:{1}:{2}].", //$NON-NLS-1$
                                        new Object[] { fieldId, issueField.getName(), longType })));
                    }
                } else {
                    final int i = 0;
                }
            } else {
                final int i = 0;
            }
        }
        return customFields.toArray(new JiraCustomField[0]);
    }

    private static JiraIssueField[] getEditableFieldsFromIssue(final Issue issue) throws JiraException {

        final List<JiraIssueField> editableFields = new ArrayList<>(issue.getFields().size());
        for (final IssueField issueField : issue.getFields()) {
            final String fieldId = issueField.getId();
            final CimFieldInfo cim = issue.getMetadata().get(fieldId);
            if (cim != null) {
                final boolean required = cim.isRequired();
                final String name = issueField.getName();
                final JiraIssueField editableField = new JiraIssueField(fieldId, name);
                editableField.setRequired(required);
                if (cim.getAllowedValues() != null) {
                    final List<JiraAllowedValue> allowedValues = new ArrayList<>();
                    for (final Object allowedValue : cim.getAllowedValues()) {
                        if (allowedValue instanceof CustomFieldOption) {
                            final CustomFieldOption value = (CustomFieldOption) allowedValue;
                            final String optionalId = value.getId() + "";
                            final String optionalValue = value.getValue();
                            if (optionalValue != null && optionalId != null) {
                                allowedValues.add(new JiraAllowedValue(optionalId, optionalValue));
                            }
                        } else if (allowedValue instanceof IssueType) {
                            final IssueType issueType = (IssueType) allowedValue;
                            allowedValues.add(new JiraAllowedValue(issueType.getId() + "", issueType.getName()));
                        } else if (allowedValue instanceof BasicComponent) {
                            final BasicComponent basicComponent = (BasicComponent) allowedValue;
                            allowedValues.add(new JiraAllowedValue(basicComponent.getId() + "", basicComponent.getName()));
                        } else if (allowedValue instanceof BasicIssue) {
                            final BasicIssue basicIssue = (BasicIssue) allowedValue;
                            allowedValues.add(new JiraAllowedValue(basicIssue.getId() + "", basicIssue.getKey()));
                        } else if (allowedValue instanceof BasicPriority) {
                            final BasicPriority priority2 = (BasicPriority) allowedValue;
                            allowedValues.add(new JiraAllowedValue(priority2.getId() + "", priority2.getName()));
                        } else if (allowedValue instanceof BasicProject) {
                            final BasicProject basicProject = (BasicProject) allowedValue;
                            allowedValues.add(new JiraAllowedValue(basicProject.getId() + "", basicProject.getName()));
                        } else if (allowedValue instanceof Resolution) {
                            final Resolution resolution = (Resolution) allowedValue;
                            allowedValues.add(new JiraAllowedValue(resolution.getId() + "", resolution.getName()));
                        } else if (allowedValue instanceof Version) {
                            final Version version = (Version) allowedValue;
                            allowedValues.add(new JiraAllowedValue(version.getId() + "", version.getName()));
                        } else if (allowedValue instanceof SecurityLevel) {
                            final SecurityLevel securityLevel = (SecurityLevel) allowedValue;
                            allowedValues.add(new JiraAllowedValue(securityLevel.getId() + "", securityLevel.getName()));
                        } else {
                            StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN,
                                    NLS.bind("Unable extract value for field [{0}:{1}:{2}].", //$NON-NLS-1$
                                            new Object[] { fieldId, issueField.getName(), allowedValue })));
                        }
                    }
                    editableField.setAllowedValues(allowedValues);
                }

                final String longTypeCustom = cim.getSchema().getCustom();
                final String longTypeSystem = cim.getSchema().getSystem();

                if (longTypeCustom != null) {
                    editableField.setType(longTypeCustom);
                } else if (longTypeSystem != null) {
                    editableField.setType(longTypeSystem);
                }
                editableFields.add(editableField);
            } else {
                // final String name = issueField.getName();
                // StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING,
                // JiraCorePlugin.ID_PLUGIN,
                // NLS.bind("Unable to find metadata information for field [{0}:{1}].",
                // issueField.getId(), name))); //$NON-NLS-1$
            }
        }
        return editableFields.toArray(new JiraIssueField[0]);
    }

    private static JiraCustomField generateCustomField(final IssueField field, final String longType) {

        boolean readonly = false;

        try {

            final JiraFieldType fieldType = JiraFieldType.fromKey(longType);

            List<String> values = null;

            switch (fieldType) {
            case TEXTFIELD:
            case TEXTAREA:
            case URL:
            case EPIC_LABEL:
            case EPIC_LINK:
                values = List.of(field.getValue().toString());
                break;
            case DATE:
            case DATETIME:
                values = List.of(field.getValue().toString());
                break;
            case FLOATFIELD:
                values = List.of(field.getValue().toString());
                break;
            case MULTIUSERPICKER:
                // no support for multi users on the Mylyn side
                // values = JiraRestCustomFieldsParser.parseMultiUserPicker(field);
                final List<String> userPicks = JiraRestCustomFieldsParser.parseMultiUserPicker(field);
                values = List.of(String.join(", ", userPicks)); //$NON-NLS-1$
                break;
            case USERPICKER:
                final String user = JiraRestCustomFieldsParser.parseUserPicker(field);
                if (user != null) {
                    values = List.of(user);
                }
                break;
            case SELECT:
            case RADIOBUTTONS:
                values = List.of(JiraRestCustomFieldsParser.parseSelect(field));
                break;
            case MULTISELECT:
            case MULTICHECKBOXES:
                values = JiraRestCustomFieldsParser.parseMultiSelect(field);
                break;
            case LABELSS:
                values = List.of(StringUtils.join(JiraRestCustomFieldsParser.parseLabels(field), " ")); //$NON-NLS-1$
                readonly = true;
                break;
            case GROUPPICKER:
                values = List.of(JiraRestCustomFieldsParser.parseGroupPicker(field));
                break;
            case MULTIGROUPPICKER:
                values = List.of(StringUtils.join(JiraRestCustomFieldsParser.parseMultiGroupPicker(field), ", ")); //$NON-NLS-1$
                break;
            default:
                if (fieldType.getTaskType() != null) { // Ignore fields we don't care about, or don't support yet
                    StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN,
                            NLS.bind("Unable extract value for custom field [{0}:{1}:{2}].", new Object[] { field.getId(), field.getName(), longType }))); //$NON-NLS-1$

                    // not supported custom field
                }
            }

            if (values != null && !values.isEmpty()) {

                final JiraCustomField customField = new JiraCustomField(field.getId(), longType, field.getName(), values);
                customField.setReadOnly(readonly);

                return customField;
            }
        } catch (JSONException | URISyntaxException e) {
            StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN, e.getMessage()));
        }

        return null;
    }

    private static Long getRankFromIssue(final Issue issue) throws JiraException {
        for (final IssueField issueField : issue.getFields()) {
            if (issueField.getId().startsWith("customfield") && issueField.getValue() != null) { //$NON-NLS-1$
                final CimFieldInfo cim = issue.getMetadata().get(issueField.getId());
                if (cim != null) {
                    if (JiraAttribute.RANK.getType().getKey().equals(cim.getSchema().getCustom())) {
                        try {
                            return Long.valueOf(issueField.getValue().toString());
                        } catch (final NumberFormatException e) {
                            StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN,
                                    NLS.bind("Unable to parse Rank value [{0}].", issueField.getValue().toString()))); //$NON-NLS-1$
                        }

                    }
                } else {
                    final int i = 0;
                }
            }
        }
        return null;

    }

    private static JiraWorkLog[] convertWorklogs(final List<Worklog> worklogs) {
        final List<JiraWorkLog> outWorkLogs = new ArrayList<>();

        for (final Worklog worklog : worklogs) {
            outWorkLogs.add(convert(worklog));
        }

        return outWorkLogs.toArray(new JiraWorkLog[outWorkLogs.size()]);

    }

    private static JiraWorkLog convert(final Worklog worklog) {
        final JiraWorkLog outWorklog = new JiraWorkLog();

        outWorklog.setAuthor(worklog.getAuthor());

        outWorklog.setComment(worklog.getComment());
        outWorklog.setCreated(worklog.getCreationDate().toInstant());
        // outWorklog.setGroupLevel(worklog.get)
        // outWorklog.setId(worklog.get)
        // outWorklog.setNewRemainingEstimate(worklog.get);
        // outWorklog.setRoleLevelId(worklog.get);
        outWorklog.setStartDate(worklog.getStartDate().toInstant());
        outWorklog.setTimeSpent(worklog.getMinutesSpent() * 60);

        outWorklog.setUpdateAuthor(worklog.getUpdateAuthor());

        outWorklog.setUpdated(worklog.getUpdateDate().toInstant());

        return outWorklog;
    }

    private static JiraAttachment[] convertAttachments(final List<Attachment> attachments) {

        final List<JiraAttachment> outAttachments = new ArrayList<>();

        for (final Attachment attachment : attachments) {
            outAttachments.add(convert(attachment));
        }

        return outAttachments.toArray(new JiraAttachment[outAttachments.size()]);
    }

    private static JiraAttachment convert(final Attachment attachment) {
        final JiraAttachment outAttachment = new JiraAttachment();

        outAttachment.setId(attachment.getSelf().toString()); // FIXME which field??? attachment.getId().toString())

        final BasicUser author = attachment.getAuthor();

        if (author != null) {
            outAttachment.setAuthor(author);
        } else {
            outAttachment.setAuthor(BasicUser.UNASSIGNED_USER); // $NON-NLS-1$
        }

//        if (author != null && author.getDisplayName() != null) {
//            outAttachment.setAuthorDisplayName(author.getDisplayName());
//        } else {
//            outAttachment.setAuthorDisplayName("Unknown"); //$NON-NLS-1$
//        }

        outAttachment.setCreated(attachment.getCreationDate().toInstant());
        outAttachment.setName(attachment.getFilename());
        outAttachment.setSize(attachment.getSize());
        outAttachment.setContent(attachment.getContentUri());

        return outAttachment;
    }

    private static JiraComment[] convertComments(final List<Comment> comments) {
        final List<JiraComment> outComments = new ArrayList<>();

        for (final Comment comment : comments) {
            outComments.add(convert(comment));
        }

        return outComments.toArray(new JiraComment[outComments.size()]);
    }

    private static JiraComment convert(final Comment comment) {
        final JiraComment outComment = new JiraComment();

        final BasicUser author = comment.getAuthor();

        if (author != null) {
            outComment.setAuthor(author);
        } else {
            outComment.setAuthor(BasicUser.UNASSIGNED_USER); // $NON-NLS-1$
        }

        outComment.setComment(comment.getBody());
        outComment.setCreated(comment.getCreationDate().toInstant());
        outComment.setMarkupDetected(false);

        final Visibility visibility = comment.getVisibility();
        if (visibility != null) {
            outComment.setRoleLevel(visibility.getValue());
        }

        return outComment;
    }

    private static JiraIssueLink[] convertIssueLinks(final List<IssueLink> issueLinks) {

        final List<JiraIssueLink> outIssueLinks = new ArrayList<>();

        for (final IssueLink issueLink : issueLinks) {
            outIssueLinks.add(convert(issueLink));
        }

        return outIssueLinks.toArray(new JiraIssueLink[outIssueLinks.size()]);
    }

    private static JiraIssueLink convert(final IssueLink issueLink) {
        final JiraIssueLink outIssueLink = new JiraIssueLink(issueLink.getTargetIssueKey(), // FIXME issueLink.getTargetIssueId().toString(),
                issueLink.getTargetIssueKey(), issueLink.getIssueLinkType().getName(), issueLink.getIssueLinkType().getName(),
                issueLink.getIssueLinkType().getDescription(), ""); //$NON-NLS-1$

        return outIssueLink;

    }

    static JiraVersion[] convertVersions(final List<Version> versions) {
        final List<JiraVersion> outVersions = new ArrayList<>();

        for (final Version version : versions) {
            // if (!version.isArchived()) {
            outVersions.add(convert(version));
            // }
        }

        Collections.reverse(outVersions);

        return outVersions.toArray(new JiraVersion[outVersions.size()]);
    }

    private static JiraVersion convert(final Version version) {
        final JiraVersion outVersion = new JiraVersion(version.getId().toString(), version.getName());

        final OffsetDateTime releaseDate = version.getReleaseDate();
        if (releaseDate != null) {
            outVersion.setReleaseDate(releaseDate.toInstant());
        }

        outVersion.setArchived(version.isArchived());
        outVersion.setReleased(version.isReleased());

        return outVersion;
    }

    static JiraComponent[] convertComponents(final List<BasicComponent> components) {

        final List<JiraComponent> outComponents = new ArrayList<>();

        for (final BasicComponent component : components) {
            outComponents.add(convert(component));
        }

        return outComponents.toArray(new JiraComponent[outComponents.size()]);
    }

    private static JiraComponent convert(final BasicComponent component) {
        final JiraComponent outComponent = new JiraComponent(component.getId().toString());

        outComponent.setName(component.getName());

        return outComponent;
    }

    private static JiraSubtask[] convert(final List<Subtask> allSubtasks) {
        final List<JiraSubtask> subtasks = new ArrayList<>();

        for (final Subtask subtask : allSubtasks) {
            subtasks.add(convert(subtask));
        }

        return subtasks.toArray(new JiraSubtask[subtasks.size()]);
    }

    private static JiraSubtask convert(final Subtask subtask) {
        return new JiraSubtask(subtask.getIssueKey() /* //FIXME subtask.getId().toString() */, subtask.getIssueKey());
    }

    // private static String generateIssueId(String uri, String issueKey) {
    // return uri + "_" + issueKey.replace('-', '*');
    // }

    public static JiraIssueType[] convertIssueTypes(final List<IssueType> allIssueTypes) {
        final List<JiraIssueType> issueTypes = new ArrayList<>();

        for (final IssueType issueType : allIssueTypes) {
            issueTypes.add(convert(issueType));
        }

        return issueTypes.toArray(new JiraIssueType[issueTypes.size()]);
    }

    private static JiraIssueType convert(final IssueType issueType) {
        final JiraIssueType outIssueType = new JiraIssueType(issueType.getId().toString(), issueType.getName(), issueType.isSubtask());

        outIssueType.setDescription(issueType.getDescription());
        outIssueType.setIcon(issueType.getIconUri().toString());

        return outIssueType;
    }

    public static List<JiraIssue> convertIssues(final List<? extends BasicIssue> issues, final IProgressMonitor monitor) throws JiraException {
        final List<JiraIssue> outIssues = new ArrayList<>(issues.size());

        for (final BasicIssue issue : issues) {
            outIssues.add(convert(issue));
        }
        return outIssues;
    }

    private static JiraIssue convert(final BasicIssue issue) {
        final JiraIssue outIssue = new JiraIssue();

        outIssue.setId(issue.getId().toString());
        outIssue.setKey(issue.getKey());
        outIssue.setSelf(issue.getSelf());

        return outIssue;
    }

    public static WorklogInput convert(final JiraWorkLog jiraWorklog, final URI uri) {
        final WorklogInputBuilder worklogInputBuilder = new WorklogInputBuilder(uri);

        switch (jiraWorklog.getAdjustEstimate()) {
        case AUTO:
            worklogInputBuilder.setAdjustEstimateAuto();
            break;
        case LEAVE:
            worklogInputBuilder.setAdjustEstimateLeave();
            break;
        case SET:
            worklogInputBuilder.setAdjustEstimateNew(jiraWorklog.getNewRemainingEstimate() / 60 + "m"); //$NON-NLS-1$
            break;
        case REDUCE:
            worklogInputBuilder.setAdjustEstimateManual(jiraWorklog.getNewRemainingEstimate() / 60 + "m"); //$NON-NLS-1$
            break;
        }

        worklogInputBuilder.setComment(jiraWorklog.getComment());
        worklogInputBuilder.setStartDate(OffsetDateTime.ofInstant(jiraWorklog.getStartDate(), ZoneId.systemDefault()));
        // worklogInputBuilder.setMinutesSpent(new Long(jiraWorklog.getTimeSpent() /
        // 60).intValue());
        worklogInputBuilder.setMinutesSpent((int) (jiraWorklog.getTimeSpent() / 60)); // $NON-NLS-1$
        // worklogInputBuilder.setAuthor(new )

        return worklogInputBuilder.build();
    }

    public static JiraServerInfo convert(final ServerInfo serverInfo) {
        final JiraServerInfo serverInfoOut = new JiraServerInfo();

        serverInfoOut.setBaseUrl(serverInfo.getBaseUri().toString());
        serverInfoOut.setWebBaseUrl(serverInfo.getBaseUri().toString());
        serverInfoOut.setBuildDate(serverInfo.getBuildDate().toInstant());
        serverInfoOut.setBuildNumber(Integer.toString(serverInfo.getBuildNumber()));
        serverInfoOut.setVersion(new JiraServerVersion(serverInfo.getVersion()));

        return serverInfoOut;
    }

    public static List<JiraAction> convertTransitions(final List<Transition> transitions) {
        final List<JiraAction> actions = new ArrayList<>();

        for (final Transition transition : transitions) {
            actions.add(convert(transition));
        }

        return actions;
    }

    private static JiraAction convert(final Transition transition) {
        final JiraAction action = new JiraAction(Integer.toString(transition.getId()), transition.getName());

        for (final Transition.Field field : transition.getFields()) {

            // TODO rest set field name once available
            // https://studio.atlassian.com/browse/JRJC-113
            final JiraIssueField outField = new JiraIssueField(field.getId(), field.getId());
            outField.setType(field.getType());
            outField.setRequired(field.isRequired());

            action.getFields().add(outField);
        }

        return action;
    }

    public static List<Version> convert(final JiraVersion[] reportedVersions) {
        final List<Version> outReportedVersions = new ArrayList<>();

        if (reportedVersions != null) {
            for (final JiraVersion version : reportedVersions) {
                outReportedVersions.add(convert(version));
            }
        }

        return outReportedVersions;
    }

    private static Version convert(final JiraVersion version) {
        final Version outVersion = new Version(null, Long.valueOf(version.getId()), version.getName(), null, false, false, null);
        return outVersion;
    }

    public static List<BasicComponent> convert(final JiraComponent[] components) {
        final List<BasicComponent> outComponents = new ArrayList<>();

        if (components != null) {
            for (final JiraComponent component : components) {
                outComponents.add(convert(component));
            }
        }

        return outComponents;
    }

    private static BasicComponent convert(final JiraComponent component) {
        return new BasicComponent(null, Long.valueOf(component.getId()), component.getName(), null);
    }

    public static JiraNamedFilter[] convertNamedFilters(final List<Filter> favouriteFilters) {
        final List<JiraNamedFilter> outFilters = new ArrayList<>();

        for (final Filter filter : favouriteFilters) {
            outFilters.add(convert(filter));
        }

        return outFilters.toArray(new JiraNamedFilter[outFilters.size()]);
    }

    private static JiraNamedFilter convert(final Filter filter) {
        final JiraNamedFilter outFilter = new JiraNamedFilter();

        outFilter.setId(filter.getId().toString());
        outFilter.setName(filter.getName());
        outFilter.setJql(filter.getJql());
        outFilter.setViewUrl(filter.getViewUrl().toString());

        return outFilter;
    }

    public static JiraStatus[] convertStatuses(final List<Status> statuses) {
        final List<JiraStatus> outStatuses = new ArrayList<>();

        for (final Status status : statuses) {
            outStatuses.add(convert(status));
        }

        return outStatuses.toArray(new JiraStatus[outStatuses.size()]);
    }

    private static JiraStatus convert(final Status status) {
        final JiraStatus outStatus = new JiraStatus(status.getId().toString());

        outStatus.setName(status.getName());
        outStatus.setDescription(status.getDescription());
        outStatus.setIcon(status.getIconUrl().toString());

        return outStatus;
    }

    public static FieldInput convert(final JiraCustomField customField) {

        final JiraFieldType fieldType = JiraFieldType.fromKey(customField.getKey());

        switch (fieldType) {
        case TEXTFIELD:
        case TEXTAREA:
        case URL:
        case EPIC_LABEL:
            if (customField.getValues().size() > 0 && customField.getValues().get(0) != null) {
                return new FieldInput(customField.getId(), customField.getValues().get(0));
            }
            break;
        case DATE:

            if (customField.getValues().size() > 0 && customField.getValues().get(0) != null && customField.getValues().get(0).length() > 0) {
                String date = null;
                final String dateValue = customField.getValues().get(0);
                try {

                    final long epochTime = Long.parseLong(dateValue);
                    date = LocalDate.ofInstant(Instant.ofEpochMilli(epochTime), ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern(JiraRestFields.DATE_FORMAT));
                } catch (final Exception e) {
                    date = LocalDate.parse(dateValue, DateTimeFormatter.ofPattern(JiraRestFields.DATE_FORMAT).withZone(ZoneId.systemDefault()))
                            .format(DateTimeFormatter.ofPattern(JiraRestFields.DATE_FORMAT));
                    // date = new OffsetDateTime(dateValue).toString(JiraRestFields.DATE_FORMAT);
                }

                return new FieldInput(customField.getId(), date);
            }
            break;
        case DATETIME:

            if (customField.getValues().size() > 0 && customField.getValues().get(0) != null && customField.getValues().get(0).length() > 0) {
                String date = null;
                final String dateValue = customField.getValues().get(0);
                try {
                    final OffsetDateTime localDateTime = LocalDateTime
                            .parse(dateValue, DateTimeFormatter.ofPattern(JiraRestFields.DATE_TIME_FORMAT).withZone(ZoneId.systemDefault()))
                            .atOffset(ZoneOffset.UTC);
                    date = localDateTime.format(DateTimeFormatter.ofPattern(JiraRestFields.DATE_TIME_FORMAT));
                } catch (final Exception e) {
                    date = LocalDateTime.parse(dateValue, DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss").localizedBy(Locale.ENGLISH))
                            .atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(JiraRestFields.DATE_TIME_FORMAT));
                    // date = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss") //$NON-NLS-1$
                    // .withLocale(Locale.ENGLISH).parseOffsetDateTime(dateValue).toString(JiraRestFields.DATE_TIME_FORMAT);
                }

                return new FieldInput(customField.getId(), date);
            }

            break;

        case FLOATFIELD:
            if (customField.getValues().size() > 0 && customField.getValues().get(0) != null && customField.getValues().get(0).length() > 0) {
                return new FieldInput(customField.getId(), Float.parseFloat(customField.getValues().get(0)));
            }
            break;
        case MULTIUSERPICKER:
        case MULTIGROUPPICKER:
            if (customField.getValues().size() > 0 && customField.getValues().get(0) != null) {

                final List<ComplexIssueInputFieldValue> fields = new ArrayList<>();

                final List<String> items = Arrays.asList(customField.getValues().get(0).split(",")); //$NON-NLS-1$

                for (final String item : items) {
                    fields.add(ComplexIssueInputFieldValue.with(JiraRestFields.NAME, StringUtils.strip(item)));
                }

                return new FieldInput(customField.getId(), fields);

            }
            break;
        case USERPICKER:
        case GROUPPICKER:
            if (customField.getValues().size() > 0 && customField.getValues().get(0) != null) {
                return new FieldInput(customField.getId(), ComplexIssueInputFieldValue.with(JiraRestFields.NAME, customField.getValues().get(0)));
            }
            break;
        case SELECT:
        case RADIOBUTTONS:
            if (customField.getValues().size() > 0) {
                if (JiraCustomField.NONE_ALLOWED_VALUE.equals(customField.getValues().get(0))) {
                    return new FieldInput(customField.getId(), ComplexIssueInputFieldValue.with("id", "-1"));
                }
                final String value = customField.getValues().get(0);
                return new FieldInput(customField.getId(), ComplexIssueInputFieldValue.with("value", value));
            }
            break;
        case MULTISELECT:
        case MULTICHECKBOXES:

            // if (customField.getValues().size() > 0) {
            final List<ComplexIssueInputFieldValue> values = new ArrayList<>();
            for (final String value : customField.getValues()) {
                values.add(ComplexIssueInputFieldValue.with("value", value)); //$NON-NLS-1$
            }

            return new FieldInput(customField.getId(), values);
        // }

        case LABELSS:
            if (customField.getValues().size() > 0) {
                return new FieldInput(customField.getId(), customField.getValues());
            }
            break;
        default:
            // not supported custom field
            return null;
        }

        // custom field with no value (send null to clear)
        return new FieldInput(customField.getId(), null);
    }
}
