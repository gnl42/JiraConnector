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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.osgi.util.NLS;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.atlassian.jira.rest.client.api.domain.Attachment;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.atlassian.jira.rest.client.api.domain.BasicWatchers;
import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.CustomFieldOption;
import com.atlassian.jira.rest.client.api.domain.Filter;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.api.domain.ServerInfo;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.Subtask;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.Visibility;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.WorklogInput;
import com.atlassian.jira.rest.client.api.domain.input.WorklogInputBuilder;
import com.google.common.collect.ImmutableList;

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
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraStatus;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraSubtask;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraVersion;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClientCache;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraException;

/**
 * @author Jacek Jaroczynski
 */
public class JiraRestConverter {

    private JiraRestConverter() throws Exception {
        throw new Exception("Utility class"); //$NON-NLS-1$
    }

    public static JiraProject[] convertProjects(Iterable<BasicProject> allProjects) {
        List<JiraProject> projects = new ArrayList<>();
        for (BasicProject basicProject : allProjects) {
            projects.add(convert(basicProject));
        }
        return projects.toArray(new JiraProject[projects.size()]);
    }

    private static JiraProject convert(BasicProject basicProject) {
        JiraProject project = new JiraProject();

        project.setName(basicProject.getName());
        project.setKey(basicProject.getKey());
        project.setId(basicProject.getId().toString());

        return project;
    }

    public static JiraResolution[] convertResolutions(Iterable<Resolution> allResolutions) {
        List<JiraResolution> resolutions = new ArrayList<>();

        for (Resolution resolution : allResolutions) {
            resolutions.add(convert(resolution));
        }

        return resolutions.toArray(new JiraResolution[resolutions.size()]);
    }

    private static JiraResolution convert(Resolution resolution) {
        return new JiraResolution(resolution.getId().toString(), resolution.getName(), resolution.getDescription(), null);
    }

    public static JiraPriority[] convertPriorities(Iterable<Priority> allPriorities) {
        List<JiraPriority> priorities = new ArrayList<>();

        for (Priority priority : allPriorities) {
            priorities.add(convert(priority));
        }

        return priorities.toArray(new JiraPriority[priorities.size()]);
    }

    private static JiraPriority convert(Priority priority) {
        JiraPriority outPriority = new JiraPriority(priority.getId().toString());

        outPriority.setName(priority.getName());
        outPriority.setDescription(priority.getDescription());
        outPriority.setColour(priority.getStatusColor());
        outPriority.setIcon(priority.getIconUri().toString());
        outPriority.setSelf(priority.getSelf());

        return outPriority;
    }

    public static JiraIssue convertIssue(Issue rawIssue, JiraClientCache cache, String url, IProgressMonitor monitor) throws JiraException {
        JiraIssue issue = new JiraIssue();

        issue.setRawIssue(rawIssue);
        issue.setCustomFields(getCustomFieldsFromIssue(rawIssue, cache));
        issue.setEditableFields(getEditableFieldsFromIssue(rawIssue, cache));

        // setAllowValuesForCustomFields(jiraIssue.getCustomFields(),
        // jiraIssue.getEditableFields());

        JiraProject project = cache.getProjectByKey(rawIssue.getProject().getKey());
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
            // TODO do we care?
            // Object parent = rawIssue.getField(JiraRestFields.PARENT).getValue();
            // if (parent instanceof JSONObject) {
            // issue.setParentKey(JsonParseUtil.getOptionalString((JSONObject) parent,
            // JiraRestFields.KEY));
            // issue.setParentId(JsonParseUtil.getOptionalString((JSONObject) parent,
            // JiraRestFields.ID));
            // }

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

        BasicUser assignee = rawIssue.getAssignee();
        if (assignee != null) {
            issue.setAssignee(assignee.getName());
            issue.setAssigneeName(assignee.getDisplayName());
        }

        if (rawIssue.getReporter() != null) {
            issue.setReporter(rawIssue.getReporter().getName());
            issue.setReporterName(rawIssue.getReporter().getDisplayName());
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
        IssueField security = rawIssue.getField(JiraRestFields.SECURITY);
        if (security != null && security.getValue() != null && security.getValue() instanceof JSONObject) {
            JSONObject json = (JSONObject) security.getValue();

            try {
                String id = json.getString(JiraRestFields.ID);
                String name = json.getString(JiraRestFields.NAME);

                JiraSecurityLevel securityLevel = new JiraSecurityLevel(id, name);

                issue.setSecurityLevel(securityLevel);
            } catch (JSONException e) {
                throw new JiraException(e);
            }
        }

        issue.setCreated(rawIssue.getCreationDate().toDate());
        issue.setUpdated(rawIssue.getUpdateDate().toDate());

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

        IssueField env = rawIssue.getField(JiraRestFields.ENVIRONMENT);
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

        DateTime dueDate = rawIssue.getDueDate();
        if (dueDate != null) {
            issue.setDue(dueDate.toDate());
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

        issue.setRank(getRankFromIssue(rawIssue, cache));

        if (rawIssue.getLabels() != null) {
            issue.setLabels(rawIssue.getLabels().toArray(new String[rawIssue.getLabels().size()]));
        }

        issue.setVotes(rawIssue.getVotes().getVotes());

        BasicWatchers watchers = rawIssue.getWatchers();
        issue.setWatched(watchers.isWatching());

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

    private static boolean isVersionMissingInProjectCache(Iterable<Version> affectedVersions, Iterable<Version> fixVersions, JiraProject project,
            IProgressMonitor monitor) {

        if (affectedVersions != null) {
            for (Version affectedVersion : affectedVersions) {
                if (project.getVersion(affectedVersion.getName()) == null) {
                    return true;
                }
            }
        }

        if (fixVersions != null) {
            for (Version fixVersion : fixVersions) {
                if (project.getVersion(fixVersion.getName()) == null) {
                    return true;
                }
            }
        }

        return false;

    }

    private static final List<String> internalNames = List.of("Development", "Rank", "Flagged"); // internal fields to the jira server

    private static JiraCustomField[] getCustomFieldsFromIssue(Issue issue, JiraClientCache cache) throws JiraException {
        List<JiraCustomField> customFields = new ArrayList<>();
        Map<String, CimFieldInfo> cimFieldInfoForProject = cache.getFieldMetadata(issue.getProject().getId(), issue.getIssueType().getId());

        for (IssueField issueField : issue.getFields()) {

            if (issueField.getId().startsWith("customfield") && issueField.getValue() != null) { //$NON-NLS-1$
                CimFieldInfo cim = cimFieldInfoForProject.get(issueField.getId());
                if (cim != null) {
                    String longType = cim.getSchema().getCustom();
                    if (longType != null) {
                        JiraCustomField customField = generateCustomField(issueField, longType);
                        if (customField != null) {
                            customFields.add(customField);
                        }
                    } else {
                        StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN,
                                NLS.bind("Unable to parse type information (edit meta) for field [{0}:{1}:{2}].", //$NON-NLS-1$
                                        new Object[] { issueField.getId(), issueField.getName(), longType })));
                    }
                } else {
                    //                    StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.INFO, JiraCorePlugin.ID_PLUGIN,
                    //                            NLS.bind("Unable to find meta information for field [{0}:{1}].", issueField.getId(), issueField.getName()))); //$NON-NLS-1$
                }
                //
                // } catch (JSONException e) {
                // StatusHandler.log(new org.eclipse.core.runtime.Status(
                // IStatus.WARNING,
                // JiraCorePlugin.ID_PLUGIN,
                // NLS.bind(
                // "Unable to parse type information (edit meta) for field [{0}].",
                // field.getId()))); //$NON-NLS-1$
                // }
                // } else {
                // // skip this (it is common to have not visible custom fields like GH Rank)
                // // StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING,
                // // JiraCorePlugin.ID_PLUGIN, NLS.bind(
                // // "Type information (edit meta) for field [{0}] not found.",
                // field.getId()))); //$NON-NLS-1$
                // }
                // }
            }
        }
        return customFields.toArray(new JiraCustomField[0]);
    }

    private static JiraIssueField[] getEditableFieldsFromIssue(Issue issue, JiraClientCache cache) throws JiraException {

        List<JiraIssueField> editableFields = new ArrayList<>();
        Map<String, CimFieldInfo> cimFieldInfoForProject = cache.getFieldMetadata(issue.getProject().getId(), issue.getIssueType().getId());

        for (IssueField issueField : issue.getFields()) {
            if (issueField.getId().startsWith("customfield") && issueField.getValue() != null && !internalNames.contains(issueField.getName())) { //$NON-NLS-1$
                CimFieldInfo cim = cimFieldInfoForProject.get(issueField.getId());
                if (cim != null) {
                    boolean required = cim.isRequired();
                    String name = issueField.getName();
                    JiraIssueField editableField = new JiraIssueField(issueField.getId(), name);
                    editableField.setRequired(required);
                    if (cim.getAllowedValues() != null) {
                        List<JiraAllowedValue> allowedValues = new ArrayList<>();
                        for (Object allowedValue : cim.getAllowedValues()) {
                            CustomFieldOption value = (CustomFieldOption) allowedValue;
                            String optionalId = value.getId() + "";
                            String optionalValue = value.getValue();
                            if (optionalValue != null && optionalId != null) {
                                allowedValues.add(new JiraAllowedValue(optionalId, optionalValue));
                            }
                        }
                        editableField.setAllowedValues(allowedValues);
                    }

                    String longTypeCustom = cim.getSchema().getCustom();
                    String longTypeSystem = cim.getSchema().getSystem();

                    if (longTypeCustom != null) {
                        editableField.setType(longTypeCustom);
                    } else if (longTypeSystem != null) {
                        editableField.setType(longTypeSystem);
                    }
                    editableFields.add(editableField);
                } else {
                    //                    final String name = issueField.getName();
                    //                    StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN,
                    //                            NLS.bind("Unable to find metadata information  for field [{0}:{1}].", issueField.getId(), name))); //$NON-NLS-1$
                }
            }

        }

        return editableFields.toArray(new JiraIssueField[0]);
    }

    private static JiraCustomField generateCustomField(IssueField field, String longType) {

        boolean readonly = false;

        try {

            JiraFieldType fieldType = JiraFieldType.fromKey(longType);

            List<String> values = null;

            switch (fieldType) {
            case TEXTFIELD:
            case TEXTAREA:
            case URL:
            case EPIC_LABEL:
            case EPIC_LINK:
                values = ImmutableList.of(field.getValue().toString());
                break;
            case DATE:
            case DATETIME:
                values = ImmutableList.of(field.getValue().toString());
                break;
            case FLOATFIELD:
                values = ImmutableList.of(field.getValue().toString());
                break;
            case MULTIUSERPICKER:
                // no support for multi users on the Mylyn side
                // values = JiraRestCustomFieldsParser.parseMultiUserPicker(field);
                values = ImmutableList.of(StringUtils.join(JiraRestCustomFieldsParser.parseMultiUserPicker(field), ", ")); //$NON-NLS-1$
                break;
            case USERPICKER:
                values = ImmutableList.of(JiraRestCustomFieldsParser.parseUserPicker(field));
                break;
            case SELECT:
            case RADIOBUTTONS:
                values = ImmutableList.of(JiraRestCustomFieldsParser.parseSelect(field));
                break;
            case MULTISELECT:
            case MULTICHECKBOXES:
                values = JiraRestCustomFieldsParser.parseMultiSelect(field);
                break;
            case LABELSS:
                values = ImmutableList.of(StringUtils.join(JiraRestCustomFieldsParser.parseLabels(field), " ")); //$NON-NLS-1$
                readonly = true;
                break;
            case GROUPPICKER:
                values = ImmutableList.of(JiraRestCustomFieldsParser.parseGroupPicker(field));
                break;
            case MULTIGROUPPICKER:
                values = ImmutableList.of(StringUtils.join(JiraRestCustomFieldsParser.parseMultiGroupPicker(field), ", ")); //$NON-NLS-1$
                break;
            default:
                StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN,
                        NLS.bind("Unable extract value for field [{0}:{1}].", new Object[] { field.getId(), field.getName(), longType }))); //$NON-NLS-1$

                // not supported custom field
            }

            if (values != null && !values.isEmpty()) {

                JiraCustomField customField = new JiraCustomField(field.getId(), longType, field.getName(), values);
                customField.setReadOnly(readonly);

                return customField;
            }
        } catch (JSONException e) {
            StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN, e.getMessage()));
        }

        return null;
    }

    private static Long getRankFromIssue(Issue issue, JiraClientCache cache) throws JiraException {
        Map<String, CimFieldInfo> cimFieldInfoForProject = cache.getFieldMetadata(issue.getProject().getId(), issue.getIssueType().getId());
        for (IssueField issueField : issue.getFields()) {
            if (issueField.getId().startsWith("customfield") && issueField.getValue() != null && !internalNames.contains(issueField.getName())) { //$NON-NLS-1$
                CimFieldInfo cim = cimFieldInfoForProject.get(issueField.getId());
                if (cim != null) {
                    if (JiraAttribute.RANK.getType().getKey().equals(cim.getSchema().getCustom())) {
                        try {
                            return Long.valueOf(issueField.getValue().toString());
                        } catch (NumberFormatException e) {
                            StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN,
                                    NLS.bind("Unable to parse Rank value [{0}].", issueField.getValue().toString()))); //$NON-NLS-1$
                        }

                    }
                }
            }
        }
        // JSONObject schemaFields =
        // JsonParseUtil.getOptionalJsonObject(issue.getRawObject(),
        // IssueRestClient.Expandos.SCHEMA.getFieldName());
        //
        // if (schemaFields != null) {
        //
        // for (IssueField field : issue.getFields()) {
        // if (field.getId().startsWith("customfield") && field.getValue() != null) {
        // //$NON-NLS-1$
        //
        // JSONObject jsonFieldFromSchema =
        // JsonParseUtil.getOptionalJsonObject(schemaFields, field.getId());
        //
        // if (jsonFieldFromSchema != null) {
        // String longType = JsonParseUtil.getOptionalString(jsonFieldFromSchema,
        // "custom"); //$NON-NLS-1$
        //
        // if (JiraAttribute.RANK.getType().getKey().equals(longType)) {
        // try {
        // return Long.valueOf(field.getValue().toString());
        // } catch (NumberFormatException e) {
        // StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING,
        // JiraCorePlugin.ID_PLUGIN, NLS.bind(
        // "Unable to parse Rank value [{0}].", field.getValue().toString())));
        // //$NON-NLS-1$
        // }
        // }
        // }
        // }
        // }
        // } else {
        // StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING,
        // JiraCorePlugin.ID_PLUGIN,
        // "Unable to retrieve fields' type information (schema). Skipping searching for
        // Rank.")); //$NON-NLS-1$
        // }
        //
        return null;
    }

    private static JiraWorkLog[] convertWorklogs(Iterable<Worklog> worklogs) {
        List<JiraWorkLog> outWorkLogs = new ArrayList<>();

        for (Worklog worklog : worklogs) {
            outWorkLogs.add(convert(worklog));
        }

        return outWorkLogs.toArray(new JiraWorkLog[outWorkLogs.size()]);

    }

    private static JiraWorkLog convert(Worklog worklog) {
        JiraWorkLog outWorklog = new JiraWorkLog();

        if (worklog.getAuthor() != null) {
            outWorklog.setAuthor(worklog.getAuthor().getDisplayName());
        }
        outWorklog.setComment(worklog.getComment());
        outWorklog.setCreated(worklog.getCreationDate().toDate());
        // outWorklog.setGroupLevel(worklog.get)
        // outWorklog.setId(worklog.get)
        // outWorklog.setNewRemainingEstimate(worklog.get);
        // outWorklog.setRoleLevelId(worklog.get);
        outWorklog.setStartDate(worklog.getStartDate().toDate());
        outWorklog.setTimeSpent(worklog.getMinutesSpent() * 60);
        if (worklog.getUpdateAuthor() != null) {
            outWorklog.setUpdateAuthor(worklog.getUpdateAuthor().getDisplayName());
        }
        outWorklog.setUpdated(worklog.getUpdateDate().toDate());

        return outWorklog;
    }

    private static JiraAttachment[] convertAttachments(Iterable<Attachment> attachments) {

        List<JiraAttachment> outAttachments = new ArrayList<>();

        for (Attachment attachment : attachments) {
            outAttachments.add(convert(attachment));
        }

        return outAttachments.toArray(new JiraAttachment[outAttachments.size()]);
    }

    private static JiraAttachment convert(Attachment attachment) {
        JiraAttachment outAttachment = new JiraAttachment();

        outAttachment.setId(attachment.getSelf().toString()); // FIXME which field??? attachment.getId().toString())

        BasicUser author = attachment.getAuthor();

        if (author != null && author.getName() != null) {
            outAttachment.setAuthor(author.getName());
        } else {
            outAttachment.setAuthor("unknown"); //$NON-NLS-1$
        }

        if (author != null && author.getDisplayName() != null) {
            outAttachment.setAuthorDisplayName(author.getDisplayName());
        } else {
            outAttachment.setAuthorDisplayName("Unknown"); //$NON-NLS-1$
        }

        outAttachment.setCreated(attachment.getCreationDate().toDate());
        outAttachment.setName(attachment.getFilename());
        outAttachment.setSize(attachment.getSize());
        outAttachment.setContent(attachment.getContentUri());

        return outAttachment;
    }

    private static JiraComment[] convertComments(Iterable<Comment> comments) {
        List<JiraComment> outComments = new ArrayList<>();

        for (Comment comment : comments) {
            outComments.add(convert(comment));
        }

        return outComments.toArray(new JiraComment[outComments.size()]);
    }

    private static JiraComment convert(Comment comment) {
        JiraComment outComment = new JiraComment();

        BasicUser author = comment.getAuthor();

        if (author != null && author.getName() != null) {
            outComment.setAuthor(author.getName());
        } else {
            outComment.setAuthor("unknown"); //$NON-NLS-1$
        }

        if (author != null && author.getDisplayName() != null) {
            outComment.setAuthorDisplayName(author.getDisplayName());
        } else {
            outComment.setAuthorDisplayName("Unknown"); //$NON-NLS-1$
        }

        outComment.setComment(comment.getBody());
        outComment.setCreated(comment.getCreationDate().toDate());
        outComment.setMarkupDetected(false);

        Visibility visibility = comment.getVisibility();
        if (visibility != null) {
            outComment.setRoleLevel(visibility.getValue());
        }

        return outComment;
    }

    private static JiraIssueLink[] convertIssueLinks(Iterable<IssueLink> issueLinks) {

        List<JiraIssueLink> outIssueLinks = new ArrayList<>();

        for (IssueLink issueLink : issueLinks) {
            outIssueLinks.add(convert(issueLink));
        }

        return outIssueLinks.toArray(new JiraIssueLink[outIssueLinks.size()]);
    }

    private static JiraIssueLink convert(IssueLink issueLink) {
        JiraIssueLink outIssueLink = new JiraIssueLink(issueLink.getTargetIssueKey(), // FIXME issueLink.getTargetIssueId().toString(),
                issueLink.getTargetIssueKey(), issueLink.getIssueLinkType().getName(), issueLink.getIssueLinkType().getName(),
                issueLink.getIssueLinkType().getDescription(), ""); //$NON-NLS-1$

        return outIssueLink;

    }

    static JiraVersion[] convertVersions(Iterable<Version> versions) {
        List<JiraVersion> outVersions = new ArrayList<>();

        for (Version version : versions) {
            // if (!version.isArchived()) {
            outVersions.add(convert(version));
            // }
        }

        Collections.reverse(outVersions);

        return outVersions.toArray(new JiraVersion[outVersions.size()]);
    }

    private static JiraVersion convert(Version version) {
        JiraVersion outVersion = new JiraVersion(version.getId().toString(), version.getName());

        DateTime releaseDate = version.getReleaseDate();
        if (releaseDate != null) {
            outVersion.setReleaseDate(releaseDate.toDate());
        }

        outVersion.setArchived(version.isArchived());
        outVersion.setReleased(version.isReleased());

        return outVersion;
    }

    static JiraComponent[] convertComponents(Iterable<BasicComponent> components) {

        List<JiraComponent> outComponents = new ArrayList<>();

        for (BasicComponent component : components) {
            outComponents.add(convert(component));
        }

        return outComponents.toArray(new JiraComponent[outComponents.size()]);
    }

    private static JiraComponent convert(BasicComponent component) {
        JiraComponent outComponent = new JiraComponent(component.getId().toString());

        outComponent.setName(component.getName());

        return outComponent;
    }

    private static JiraSubtask[] convert(Iterable<Subtask> allSubtasks) {
        List<JiraSubtask> subtasks = new ArrayList<>();

        for (Subtask subtask : allSubtasks) {
            subtasks.add(convert(subtask));
        }

        return subtasks.toArray(new JiraSubtask[subtasks.size()]);
    }

    private static JiraSubtask convert(Subtask subtask) {
        return new JiraSubtask(subtask.getIssueKey() /* //FIXME subtask.getId().toString() */, subtask.getIssueKey());
    }

    // private static String generateIssueId(String uri, String issueKey) {
    // return uri + "_" + issueKey.replace('-', '*');
    // }

    public static JiraIssueType[] convertIssueTypes(Iterable<IssueType> allIssueTypes) {
        List<JiraIssueType> issueTypes = new ArrayList<>();

        for (IssueType issueType : allIssueTypes) {
            issueTypes.add(convert(issueType));
        }

        return issueTypes.toArray(new JiraIssueType[issueTypes.size()]);
    }

    private static JiraIssueType convert(IssueType issueType) {
        JiraIssueType outIssueType = new JiraIssueType(issueType.getId().toString(), issueType.getName(), issueType.isSubtask());

        outIssueType.setDescription(issueType.getDescription());
        outIssueType.setIcon(issueType.getIconUri().toString());

        return outIssueType;
    }

    public static List<JiraIssue> convertIssues(Iterable<? extends BasicIssue> issues) {
        List<JiraIssue> outIssues = new ArrayList<>();

        for (BasicIssue issue : issues) {
            outIssues.add(convert(issue));
        }

        return outIssues;
    }

    private static JiraIssue convert(BasicIssue issue) {
        JiraIssue outIssue = new JiraIssue();

        outIssue.setId(issue.getId().toString());
        outIssue.setKey(issue.getKey());
        outIssue.setSelf(issue.getSelf());

        return outIssue;
    }

    public static WorklogInput convert(JiraWorkLog jiraWorklog, URI uri) {
        WorklogInputBuilder worklogInputBuilder = new WorklogInputBuilder(uri);

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
        worklogInputBuilder.setStartDate(new DateTime(jiraWorklog.getStartDate()));
        // worklogInputBuilder.setMinutesSpent(new Long(jiraWorklog.getTimeSpent() /
        // 60).intValue());
        worklogInputBuilder.setMinutesSpent((int) (jiraWorklog.getTimeSpent() / 60)); // $NON-NLS-1$
        // worklogInputBuilder.setAuthor(new )

        return worklogInputBuilder.build();
    }

    public static JiraServerInfo convert(ServerInfo serverInfo) {
        JiraServerInfo serverInfoOut = new JiraServerInfo();

        serverInfoOut.setBaseUrl(serverInfo.getBaseUri().toString());
        serverInfoOut.setWebBaseUrl(serverInfo.getBaseUri().toString());
        serverInfoOut.setBuildDate(serverInfo.getBuildDate().toDate());
        serverInfoOut.setBuildNumber(Integer.toString(serverInfo.getBuildNumber()));
        serverInfoOut.setVersion(serverInfo.getVersion());

        return serverInfoOut;
    }

    public static Iterable<JiraAction> convertTransitions(Iterable<Transition> transitions) {
        List<JiraAction> actions = new ArrayList<>();

        for (Transition transition : transitions) {
            actions.add(convert(transition));
        }

        return actions;
    }

    private static JiraAction convert(Transition transition) {
        JiraAction action = new JiraAction(Integer.toString(transition.getId()), transition.getName());

        for (Transition.Field field : transition.getFields()) {

            // TODO rest set field name once available
            // https://studio.atlassian.com/browse/JRJC-113
            JiraIssueField outField = new JiraIssueField(field.getId(), field.getId());
            outField.setType(field.getType());
            outField.setRequired(field.isRequired());

            action.getFields().add(outField);
        }

        return action;
    }

    public static Iterable<Version> convert(JiraVersion[] reportedVersions) {
        List<Version> outReportedVersions = new ArrayList<>();

        if (reportedVersions != null) {
            for (JiraVersion version : reportedVersions) {
                outReportedVersions.add(convert(version));
            }
        }

        return outReportedVersions;
    }

    private static Version convert(JiraVersion version) {
        Version outVersion = new Version(null, Long.valueOf(version.getId()), version.getName(), null, false, false, null);
        return outVersion;
    }

    public static Iterable<BasicComponent> convert(JiraComponent[] components) {
        List<BasicComponent> outComponents = new ArrayList<>();

        if (components != null) {
            for (JiraComponent component : components) {
                outComponents.add(convert(component));
            }
        }

        return outComponents;
    }

    private static BasicComponent convert(JiraComponent component) {
        return new BasicComponent(null, Long.valueOf(component.getId()), component.getName(), null);
    }

    public static JiraNamedFilter[] convertNamedFilters(Iterable<Filter> favouriteFilters) {
        List<JiraNamedFilter> outFilters = new ArrayList<>();

        for (Filter filter : favouriteFilters) {
            outFilters.add(convert(filter));
        }

        return outFilters.toArray(new JiraNamedFilter[outFilters.size()]);
    }

    private static JiraNamedFilter convert(Filter filter) {
        JiraNamedFilter outFilter = new JiraNamedFilter();

        outFilter.setId(filter.getId().toString());
        outFilter.setName(filter.getName());
        outFilter.setJql(filter.getJql());
        outFilter.setViewUrl(filter.getViewUrl().toString());

        return outFilter;
    }

    public static JiraStatus[] convertStatuses(Iterable<Status> statuses) {
        List<JiraStatus> outStatuses = new ArrayList<>();

        for (Status status : statuses) {
            outStatuses.add(convert(status));
        }

        return outStatuses.toArray(new JiraStatus[outStatuses.size()]);
    }

    private static JiraStatus convert(Status status) {
        JiraStatus outStatus = new JiraStatus(status.getId().toString());

        outStatus.setName(status.getName());
        outStatus.setDescription(status.getDescription());
        outStatus.setIcon(status.getIconUrl().toString());

        return outStatus;
    }

    public static FieldInput convert(JiraCustomField customField) {

        JiraFieldType fieldType = JiraFieldType.fromKey(customField.getKey());

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

                try {
                    date = new DateTime(Long.valueOf(customField.getValues().get(0))).toString(JiraRestFields.DATE_FORMAT);
                } catch (IllegalArgumentException e) {
                    date = new DateTime(customField.getValues().get(0)).toString(JiraRestFields.DATE_FORMAT);
                }

                return new FieldInput(customField.getId(), date);
            }
            break;
        case DATETIME:

            if (customField.getValues().size() > 0 && customField.getValues().get(0) != null && customField.getValues().get(0).length() > 0) {
                String date = null;

                try {
                    date = new DateTime(customField.getValues().get(0)).toString(JiraRestFields.DATE_TIME_FORMAT);
                } catch (IllegalArgumentException e) {

                    date = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss") //$NON-NLS-1$
                            .withLocale(Locale.ENGLISH).parseDateTime(customField.getValues().get(0)).toString(JiraRestFields.DATE_TIME_FORMAT);
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

                List<ComplexIssueInputFieldValue> fields = new ArrayList<>();

                List<String> items = Arrays.asList(customField.getValues().get(0).split(",")); //$NON-NLS-1$

                for (String item : items) {
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
                String value = customField.getValues().get(0);
                return new FieldInput(customField.getId(), ComplexIssueInputFieldValue.with("value", value));
            }
            break;
        case MULTISELECT:
        case MULTICHECKBOXES:

            // if (customField.getValues().size() > 0) {
            List<ComplexIssueInputFieldValue> values = new ArrayList<>();
            for (String value : customField.getValues()) {
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
