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

package com.atlassian.connector.eclipse.internal.jira.core.service.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.osgi.util.NLS;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.atlassian.connector.eclipse.internal.jira.core.JiraAttribute;
import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;
import com.atlassian.connector.eclipse.internal.jira.core.JiraFieldType;
import com.atlassian.connector.eclipse.internal.jira.core.model.AllowedValue;
import com.atlassian.connector.eclipse.internal.jira.core.model.Attachment;
import com.atlassian.connector.eclipse.internal.jira.core.model.Comment;
import com.atlassian.connector.eclipse.internal.jira.core.model.Component;
import com.atlassian.connector.eclipse.internal.jira.core.model.CustomField;
import com.atlassian.connector.eclipse.internal.jira.core.model.IssueField;
import com.atlassian.connector.eclipse.internal.jira.core.model.IssueLink;
import com.atlassian.connector.eclipse.internal.jira.core.model.IssueType;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraAction;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import com.atlassian.connector.eclipse.internal.jira.core.model.NamedFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.Priority;
import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.model.Resolution;
import com.atlassian.connector.eclipse.internal.jira.core.model.SecurityLevel;
import com.atlassian.connector.eclipse.internal.jira.core.model.ServerInfo;
import com.atlassian.connector.eclipse.internal.jira.core.model.Subtask;
import com.atlassian.connector.eclipse.internal.jira.core.model.Version;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClientCache;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.domain.BasicComponent;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.BasicIssueType;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.FavouriteFilter;
import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.Status;
import com.atlassian.jira.rest.client.domain.Transition;
import com.atlassian.jira.rest.client.domain.Visibility;
import com.atlassian.jira.rest.client.domain.Worklog;
import com.atlassian.jira.rest.client.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.atlassian.jira.rest.client.domain.input.WorklogInput;
import com.atlassian.jira.rest.client.domain.input.WorklogInputBuilder;
import com.atlassian.jira.rest.client.internal.json.JsonParseUtil;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * @author Jacek Jaroczynski
 */
public class JiraRestConverter {

	private JiraRestConverter() throws Exception {
		throw new Exception("Utility class"); //$NON-NLS-1$
	}

	public static Project[] convertProjects(Iterable<BasicProject> allProjects) {
		List<Project> projects = new ArrayList<Project>();
		for (BasicProject basicProject : allProjects) {
			projects.add(convert(basicProject));
		}
		return projects.toArray(new Project[projects.size()]);
	}

	private static Project convert(BasicProject basicProject) {
		Project project = new Project();

		project.setName(basicProject.getName());
		project.setKey(basicProject.getKey());
		project.setId(basicProject.getId().toString());

		return project;
	}

	public static Resolution[] convertResolutions(
			Iterable<com.atlassian.jira.rest.client.domain.Resolution> allResolutions) {
		List<Resolution> resolutions = new ArrayList<Resolution>();

		for (com.atlassian.jira.rest.client.domain.Resolution resolution : allResolutions) {
			resolutions.add(convert(resolution));
		}

		return resolutions.toArray(new Resolution[resolutions.size()]);
	}

	private static Resolution convert(com.atlassian.jira.rest.client.domain.Resolution resolution) {
		return new Resolution(resolution.getId().toString(), resolution.getName(), resolution.getDescription(), null);
	}

	public static Priority[] convertPriorities(Iterable<com.atlassian.jira.rest.client.domain.Priority> allPriorities) {
		List<Priority> priorities = new ArrayList<Priority>();

		for (com.atlassian.jira.rest.client.domain.Priority priority : allPriorities) {
			priorities.add(convert(priority));
		}

		return priorities.toArray(new Priority[priorities.size()]);
	}

	private static Priority convert(com.atlassian.jira.rest.client.domain.Priority priority) {
		Priority outPriority = new Priority(priority.getId().toString());

		outPriority.setName(priority.getName());
		outPriority.setDescription(priority.getDescription());
		outPriority.setColour(priority.getStatusColor());
		outPriority.setIcon(priority.getIconUri().toString());
		outPriority.setSelf(priority.getSelf());

		return outPriority;
	}

	public static JiraIssue convertIssue(Issue issue, JiraClientCache cache, String url, IProgressMonitor monitor)
			throws JiraException {
		JiraIssue jiraIssue = new JiraIssue();

		jiraIssue.setRawIssue(issue);

		jiraIssue.setCustomFields(getCustomFieldsFromIssue(issue));
		jiraIssue.setEditableFields(getEditableFieldsFromIssue(issue));

//		setAllowValuesForCustomFields(jiraIssue.getCustomFields(), jiraIssue.getEditableFields());

		Project project = cache.getProjectByKey(issue.getProject().getKey());
		jiraIssue.setProject(project);
		if (project != null && !project.hasDetails()) {
			cache.refreshProjectDetails(project, monitor);
		} else if (project == null) {
			throw new JiraException(NLS.bind(
					"Project with key {0} not found in local cache. Please refresh repository configuration.", //$NON-NLS-1$
					issue.getProject().getKey()));
		}

		jiraIssue.setId(issue.getId().toString());
		jiraIssue.setSelf(issue.getSelf());
		jiraIssue.setKey(issue.getKey());
		jiraIssue.setSummary(issue.getSummary());
		jiraIssue.setDescription(issue.getDescription());

		if (issue.getIssueType().isSubtask()) {
			Object parent = issue.getField(JiraRestFields.PARENT).getValue();
			if (parent instanceof JSONObject) {
				jiraIssue.setParentKey(JsonParseUtil.getOptionalString((JSONObject) parent, JiraRestFields.KEY));
				jiraIssue.setParentId(JsonParseUtil.getOptionalString((JSONObject) parent, JiraRestFields.ID));
			}

		}

		if (issue.getPriority() != null) {
			jiraIssue.setPriority(cache.getPriorityById(issue.getPriority().getId().toString()));
		} else if (cache.getPriorities().length > 0) {
			jiraIssue.setPriority(cache.getPriorities()[0]);
		} else {
			jiraIssue.setPriority(null);
			StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN, NLS.bind(
					"Found issue with empty priority: {0}", issue.getKey())));
		}

		jiraIssue.setStatus(cache.getStatusById(issue.getStatus().getId().toString()));

		BasicUser assignee = issue.getAssignee();
		if (assignee != null) {
			jiraIssue.setAssignee(assignee.getName());
			jiraIssue.setAssigneeName(assignee.getDisplayName());
		}

		if (issue.getReporter() != null) {
			jiraIssue.setReporter(issue.getReporter().getName());
			jiraIssue.setReporterName(issue.getReporter().getDisplayName());
		}

		jiraIssue.setResolution(issue.getResolution() == null ? null : cache.getResolutionById(issue.getResolution()
				.getId()
				.toString()));

		if (issue.getTimeTracking() != null) {
			if (issue.getTimeTracking().getOriginalEstimateMinutes() != null) {
				jiraIssue.setInitialEstimate(issue.getTimeTracking().getOriginalEstimateMinutes() * 60);
			}
			if (issue.getTimeTracking().getRemainingEstimateMinutes() != null) {
				jiraIssue.setEstimate(issue.getTimeTracking().getRemainingEstimateMinutes() * 60);
			}
			if (issue.getTimeTracking().getTimeSpentMinutes() != null) {
				jiraIssue.setActual(issue.getTimeTracking().getTimeSpentMinutes() * 60);
			}
		}

		Field security = issue.getField(JiraRestFields.SECURITY);
		if (security != null && security.getValue() != null && security.getValue() instanceof JSONObject) {
			JSONObject json = (JSONObject) security.getValue();

			try {
				String id = json.getString(JiraRestFields.ID);
				String name = json.getString(JiraRestFields.NAME);

				SecurityLevel securityLevel = new SecurityLevel(id, name);

				jiraIssue.setSecurityLevel(securityLevel);
			} catch (JSONException e) {
				throw new JiraException(e);
			}
		}

		jiraIssue.setCreated(issue.getCreationDate().toDate());
		jiraIssue.setUpdated(issue.getUpdateDate().toDate());

		if (project != null && project.getIssueTypeById(issue.getIssueType().getId().toString()) != null) {
			jiraIssue.setType(project.getIssueTypeById(issue.getIssueType().getId().toString()));
		} else {
			jiraIssue.setType(cache.getIssueTypeById(issue.getIssueType().getId().toString()));
		}

		if (issue.getSubtasks() != null) {
			jiraIssue.setSubtasks(convert(issue.getSubtasks()));
		}

		jiraIssue.setType(convert(issue.getIssueType()));
		jiraIssue.setUrl(url + "/browse/" + issue.getKey()); //$NON-NLS-1$

		if (issue.getComponents() != null) {
			jiraIssue.setComponents(convertComponents(issue.getComponents()));
		}

		Field env = issue.getField(JiraRestFields.ENVIRONMENT);
		if (env != null && env.getValue() != null) {
			jiraIssue.setEnvironment(env.getValue().toString());
		} else {
			// hack: empty value is necessary to display environment field in the issue editor
			jiraIssue.setEnvironment(""); //$NON-NLS-1$
		}

		if (issue.getAffectedVersions() != null) {
			jiraIssue.setReportedVersions(convertVersions(issue.getAffectedVersions()));
		}
		if (issue.getFixVersions() != null) {
			jiraIssue.setFixVersions(convertVersions(issue.getFixVersions()));
		}

		DateTime dueDate = issue.getDueDate();
		if (dueDate != null) {
			jiraIssue.setDue(dueDate.toDate());
		} else {
			jiraIssue.setDue(null);
		}

		if (issue.getIssueLinks() != null) {
			jiraIssue.setIssueLinks(convertIssueLinks(issue.getIssueLinks()));
		}

		if (issue.getComments() != null) {
			jiraIssue.setComments(convertComments(issue.getComments()));
		}

		if (issue.getAttachments() != null) {
			jiraIssue.setAttachments(convertAttachments(issue.getAttachments()));
		}

		if (issue.getWorklogs() != null) {
			jiraIssue.setWorklogs(convertWorklogs(issue.getWorklogs()));
		}

		jiraIssue.setRank(getRankFromIssue(issue));

		if (issue.getLabels() != null) {
			jiraIssue.setLabels(issue.getLabels().toArray(new String[issue.getLabels().size()]));
		}

		return jiraIssue;
	}

//	private static void setAllowValuesForCustomFields(CustomField[] customFields, IssueField[] editableFields) {
//		Map<String, IssueField> editableFieldsMap = new HashMap<String, IssueField>(editableFields.length + 1, 1);
//
//		// transform editable fields into HasMap
//		for (IssueField editableField : editableFields) {
//			editableFieldsMap.put(editableField.getId(), editableField);
//		}
//
//		for (CustomField customField : customFields) {
//			customField.setAllowedValues(editableFieldsMap.get(customField.getId()).getAlloweValues());
//		}
//	}

	private static CustomField[] getCustomFieldsFromIssue(Issue issue) {
		JSONObject editmeta = JsonParseUtil.getOptionalJsonObject(issue.getRawObject(), "editmeta");

		if (editmeta != null) {

			JSONObject fieldsFromEditMeta = JsonParseUtil.getOptionalJsonObject(editmeta, "fields");

			if (fieldsFromEditMeta != null) {

				List<CustomField> customFields = new ArrayList<CustomField>();

				for (Field field : issue.getFields()) {
					if (field.getId().startsWith("customfield") && field.getValue() != null) { //$NON-NLS-1$

						JSONObject jsonFieldFromEditMeta = JsonParseUtil.getOptionalJsonObject(fieldsFromEditMeta,
								field.getId());

						if (jsonFieldFromEditMeta != null) {
							try {
								JSONObject schema = (JSONObject) jsonFieldFromEditMeta.get("schema");

								if (schema != null) {

									String longType = JsonParseUtil.getOptionalString(schema, "custom");

									if (longType != null) {
										CustomField customField = generateCustomField(field, longType);
										if (customField != null) {
											customFields.add(customField);
										}
									} else {
										StatusHandler.log(new org.eclipse.core.runtime.Status(
												IStatus.WARNING,
												JiraCorePlugin.ID_PLUGIN,
												NLS.bind(
														"Unable to parse type information (edit meta) for field [{0}].", field.getId()))); //$NON-NLS-1$
									}
								} else {
									StatusHandler.log(new org.eclipse.core.runtime.Status(
											IStatus.WARNING,
											JiraCorePlugin.ID_PLUGIN,
											NLS.bind(
													"Unable to parse type information (edit meta) for field [{0}].", field.getId()))); //$NON-NLS-1$
								}

							} catch (JSONException e) {
								StatusHandler.log(new org.eclipse.core.runtime.Status(
										IStatus.WARNING,
										JiraCorePlugin.ID_PLUGIN,
										NLS.bind(
												"Unable to parse type information (edit meta) for field [{0}].", field.getId()))); //$NON-NLS-1$
							}
						} else {
							// skip this (it is common to have not visible custom fields like GH Rank)
//							StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING,
//									JiraCorePlugin.ID_PLUGIN, NLS.bind(
//											"Type information (edit meta) for field [{0}] not found.", field.getId()))); //$NON-NLS-1$
						}
					}
				}

				return customFields.toArray(new CustomField[customFields.size()]);
			} else {
				StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN,
						"Unable to retrieve fields' type information (edit meta). Skipping custom fields parsing.")); //$NON-NLS-1$
			}

		} else {
			StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN,
					"Unable to retrieve fields' type information (edit meta). Skipping custom fields parsing.")); //$NON-NLS-1$
		}

		return new CustomField[0];
	}

	private static IssueField[] getEditableFieldsFromIssue(Issue issue) {

		List<IssueField> editableFields = new ArrayList<IssueField>();

		JSONObject editmeta = JsonParseUtil.getOptionalJsonObject(issue.getRawObject(), "editmeta");

		if (editmeta != null) {

			try {
				JSONObject fieldsFromEditMeta = JsonParseUtil.getNestedObject(editmeta, "fields");

				if (fieldsFromEditMeta != null) {

					@SuppressWarnings("rawtypes")
					Iterator keys = fieldsFromEditMeta.keys();

					while (keys.hasNext()) {
						String key = (String) keys.next();
						JSONObject jsonFieldFromEditMeta = fieldsFromEditMeta.getJSONObject(key);
//
						if (jsonFieldFromEditMeta != null) {
							boolean required = jsonFieldFromEditMeta.getBoolean("required");
							String name = jsonFieldFromEditMeta.getString(JiraRestFields.NAME);

							IssueField editableField = new IssueField(key, name);
							editableField.setRequired(required);

							Optional<JSONArray> allowedValuesObject = JsonParseUtil.getOptionalArray(
									jsonFieldFromEditMeta, "allowedValues"); //$NON-NLS-1$

							if (allowedValuesObject != null
									&& !Optional.<JSONArray> absent().equals(allowedValuesObject)) {
								List<AllowedValue> allowedValues = new ArrayList<AllowedValue>();

								JSONArray alloweValuesArray = allowedValuesObject.get();
								for (int i = 0; i < alloweValuesArray.length(); ++i) {
									JSONObject allowedValue = alloweValuesArray.getJSONObject(i);
									String optionalId = JsonParseUtil.getOptionalString(allowedValue, "id");
									String optionalValue = JsonParseUtil.getOptionalString(allowedValue, "value");
									if (optionalValue != null && optionalId != null) {
										allowedValues.add(new AllowedValue(optionalId, optionalValue));

									}
								}

								editableField.setAllowedValues(allowedValues);
							}

							JSONObject schema = (JSONObject) jsonFieldFromEditMeta.get("schema");

							if (schema != null) {

								String longTypeCustom = JsonParseUtil.getOptionalString(schema, "custom");
								String longTypeSystem = JsonParseUtil.getOptionalString(schema, "system");

								if (longTypeCustom != null) {
									editableField.setType(longTypeCustom);
								} else if (longTypeSystem != null) {
									editableField.setType(longTypeSystem);
								}
							}
							editableFields.add(editableField);

						} else {
							StatusHandler.log(new org.eclipse.core.runtime.Status(
									IStatus.WARNING,
									JiraCorePlugin.ID_PLUGIN,
									NLS.bind(
											"Unable to retrieve field' type information (edit meta) for [{0}]. Skipping this one.", key))); //$NON-NLS-1$
						}
					}
				} else {
					StatusHandler.log(new org.eclipse.core.runtime.Status(
							IStatus.WARNING,
							JiraCorePlugin.ID_PLUGIN,
							NLS.bind(
									"Unable to retrieve 'fields' information (edit meta) for issue [{0}]. Skipping editable fields parsing.", issue.getKey()))); //$NON-NLS-1$
				}
			} catch (JSONException e) {
				StatusHandler.log(new org.eclipse.core.runtime.Status(
						IStatus.WARNING,
						JiraCorePlugin.ID_PLUGIN,
						NLS.bind(
								"Unable to parse type information (edit meta) for issue [{0}]. Skipping editable fields parsing.", issue.getKey()))); //$NON-NLS-1$
			}
		} else {
			StatusHandler.log(new org.eclipse.core.runtime.Status(
					IStatus.WARNING,
					JiraCorePlugin.ID_PLUGIN,
					NLS.bind(
							"Unable to retrieve 'editmeta' information for issue [{0}]. Skipping editable fields parsing.", issue.getKey()))); //$NON-NLS-1$
		}

		if (editableFields.size() > 0) {
			return editableFields.toArray(new IssueField[editableFields.size()]);
		}

		return new IssueField[0];
	}

	private static CustomField generateCustomField(Field field, String longType) {

		boolean readonly = false;

		try {

			JiraFieldType fieldType = JiraFieldType.fromKey(longType);

			List<String> values = null;

			switch (fieldType) {
			case TEXTFIELD:
			case TEXTAREA:
			case URL:
			case EPIC_LABEL:
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
//			values = JiraRestCustomFieldsParser.parseMultiUserPicker(field);
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
				values = ImmutableList.of(StringUtils.join(JiraRestCustomFieldsParser.parseMultiGroupPicker(field),
						", ")); //$NON-NLS-1$
				break;
			default:
				// not supported custom field
			}

			if (values != null && !values.isEmpty()) {

				CustomField customField = new CustomField(field.getId(), longType, field.getName(), values);
				customField.setReadOnly(readonly);

				return customField;
			}
		} catch (JSONException e) {
			StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN,
					e.getMessage()));
		}

		return null;
	}

	private static Integer getRankFromIssue(Issue issue) {
		JSONObject schemaFields = JsonParseUtil.getOptionalJsonObject(issue.getRawObject(),
				IssueRestClient.Expandos.SCHEMA.getFieldName());

		if (schemaFields != null) {

			for (Field field : issue.getFields()) {
				if (field.getId().startsWith("customfield") && field.getValue() != null) { //$NON-NLS-1$

					JSONObject jsonFieldFromSchema = JsonParseUtil.getOptionalJsonObject(schemaFields, field.getId());

					if (jsonFieldFromSchema != null) {
						String longType = JsonParseUtil.getOptionalString(jsonFieldFromSchema, "custom"); //$NON-NLS-1$

						if (JiraAttribute.RANK.getType().getKey().equals(longType)) {
							try {
								return Integer.valueOf(field.getValue().toString());
							} catch (NumberFormatException e) {
								StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING,
										JiraCorePlugin.ID_PLUGIN, NLS.bind(
												"Unable to parse Rank value [{0}].", field.getValue().toString()))); //$NON-NLS-1$
							}
						}
					}
				}
			}
		} else {
			StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN,
					"Unable to retrieve fields' type information (schema). Skipping searching for Rank.")); //$NON-NLS-1$
		}

		return null;
	}

	private static JiraWorkLog[] convertWorklogs(Iterable<Worklog> worklogs) {
		List<JiraWorkLog> outWorkLogs = new ArrayList<JiraWorkLog>();

		for (Worklog worklog : worklogs) {
			outWorkLogs.add(convert(worklog));
		}

		return outWorkLogs.toArray(new JiraWorkLog[outWorkLogs.size()]);

	}

	private static JiraWorkLog convert(Worklog worklog) {
		JiraWorkLog outWorklog = new JiraWorkLog();

//		outWorklog.setAdjustEstimate(worklog.get);
		outWorklog.setAuthor(worklog.getAuthor().getDisplayName());
		outWorklog.setComment(worklog.getComment());
		outWorklog.setCreated(worklog.getCreationDate().toDate());
//		outWorklog.setGroupLevel(worklog.get)
//		outWorklog.setId(worklog.get)
//		outWorklog.setNewRemainingEstimate(worklog.get);
//		outWorklog.setRoleLevelId(worklog.get);
		outWorklog.setStartDate(worklog.getStartDate().toDate());
		outWorklog.setTimeSpent(worklog.getMinutesSpent() * 60);
		outWorklog.setUpdateAuthor(worklog.getUpdateAuthor().getDisplayName());
		outWorklog.setUpdated(worklog.getUpdateDate().toDate());

		return outWorklog;
	}

	private static Attachment[] convertAttachments(
			Iterable<com.atlassian.jira.rest.client.domain.Attachment> attachments) {

		List<Attachment> outAttachments = new ArrayList<Attachment>();

		for (com.atlassian.jira.rest.client.domain.Attachment attachment : attachments) {
			outAttachments.add(convert(attachment));
		}

		return outAttachments.toArray(new Attachment[outAttachments.size()]);
	}

	private static Attachment convert(com.atlassian.jira.rest.client.domain.Attachment attachment) {
		Attachment outAttachment = new Attachment();

		outAttachment.setId(attachment.getId().toString());

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

	private static Comment[] convertComments(Iterable<com.atlassian.jira.rest.client.domain.Comment> comments) {
		List<Comment> outComments = new ArrayList<Comment>();

		for (com.atlassian.jira.rest.client.domain.Comment comment : comments) {
			outComments.add(convert(comment));
		}

		return outComments.toArray(new Comment[outComments.size()]);
	}

	private static Comment convert(com.atlassian.jira.rest.client.domain.Comment comment) {
		Comment outComment = new Comment();

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

	private static IssueLink[] convertIssueLinks(Iterable<com.atlassian.jira.rest.client.domain.IssueLink> issueLinks) {

		List<IssueLink> outIssueLinks = new ArrayList<IssueLink>();

		for (com.atlassian.jira.rest.client.domain.IssueLink issueLink : issueLinks) {
			outIssueLinks.add(convert(issueLink));
		}

		return outIssueLinks.toArray(new IssueLink[outIssueLinks.size()]);
	}

	private static IssueLink convert(com.atlassian.jira.rest.client.domain.IssueLink issueLink) {
		IssueLink outIssueLink = new IssueLink(issueLink.getTargetIssueId().toString(), issueLink.getTargetIssueKey(),
				issueLink.getIssueLinkType().getName(), issueLink.getIssueLinkType().getName(),
				issueLink.getIssueLinkType().getDescription(), ""); //$NON-NLS-1$

		return outIssueLink;

	}

	static Version[] convertVersions(Iterable<com.atlassian.jira.rest.client.domain.Version> versions) {
		List<Version> outVersions = new ArrayList<Version>();

		for (com.atlassian.jira.rest.client.domain.Version version : versions) {
//			if (!version.isArchived()) {
			outVersions.add(convert(version));
//			}
		}

		Collections.reverse(outVersions);

		return outVersions.toArray(new Version[outVersions.size()]);
	}

	private static Version convert(com.atlassian.jira.rest.client.domain.Version version) {
		Version outVersion = new Version(version.getId().toString(), version.getName());

		DateTime releaseDate = version.getReleaseDate();
		if (releaseDate != null) {
			outVersion.setReleaseDate(releaseDate.toDate());
		}

		outVersion.setArchived(version.isArchived());
		outVersion.setReleased(version.isReleased());

		return outVersion;
	}

	static Component[] convertComponents(Iterable<BasicComponent> components) {

		List<Component> outComponents = new ArrayList<Component>();

		for (BasicComponent component : components) {
			outComponents.add(convert(component));
		}

		return outComponents.toArray(new Component[outComponents.size()]);
	}

	private static Component convert(BasicComponent component) {
		Component outComponent = new Component(component.getId().toString());

		outComponent.setName(component.getName());

		return outComponent;
	}

	private static IssueType convert(BasicIssueType issueType) {
		IssueType outIssueType = new IssueType(issueType.getId().toString(), issueType.getName(), issueType.isSubtask());

		return outIssueType;
	}

	private static Subtask[] convert(Iterable<com.atlassian.jira.rest.client.domain.Subtask> allSubtasks) {
		List<Subtask> subtasks = new ArrayList<Subtask>();

		for (com.atlassian.jira.rest.client.domain.Subtask subtask : allSubtasks) {
			subtasks.add(convert(subtask));
		}

		return subtasks.toArray(new Subtask[subtasks.size()]);
	}

	private static Subtask convert(com.atlassian.jira.rest.client.domain.Subtask subtask) {
		return new Subtask(subtask.getId().toString(), subtask.getIssueKey());
	}

//	private static String generateIssueId(String uri, String issueKey) {
//		return uri + "_" + issueKey.replace('-', '*');
//	}

	public static IssueType[] convertIssueTypes(Iterable<com.atlassian.jira.rest.client.domain.IssueType> allIssueTypes) {
		List<IssueType> issueTypes = new ArrayList<IssueType>();

		for (com.atlassian.jira.rest.client.domain.IssueType issueType : allIssueTypes) {
			issueTypes.add(convert(issueType));
		}

		return issueTypes.toArray(new IssueType[issueTypes.size()]);
	}

	private static IssueType convert(com.atlassian.jira.rest.client.domain.IssueType issueType) {
		IssueType outIssueType = new IssueType(issueType.getId().toString(), issueType.getName(), issueType.isSubtask());

		outIssueType.setDescription(issueType.getDescription());
		outIssueType.setIcon(issueType.getIconUri().toString());

		return outIssueType;
	}

	public static List<JiraIssue> convertIssues(Iterable<? extends BasicIssue> issues) {
		List<JiraIssue> outIssues = new ArrayList<JiraIssue>();

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
			worklogInputBuilder.setAdjustEstimateNew((jiraWorklog.getNewRemainingEstimate() / 60) + "m"); //$NON-NLS-1$
			break;
		case REDUCE:
			worklogInputBuilder.setAdjustEstimateManual((jiraWorklog.getNewRemainingEstimate() / 60) + "m"); //$NON-NLS-1$
			break;
		}

		worklogInputBuilder.setComment(jiraWorklog.getComment());
		worklogInputBuilder.setStartDate(new DateTime(jiraWorklog.getStartDate()));
//		worklogInputBuilder.setMinutesSpent(new Long(jiraWorklog.getTimeSpent() / 60).intValue());
		worklogInputBuilder.setTimeSpent(String.valueOf(jiraWorklog.getTimeSpent() / 60) + "m"); //$NON-NLS-1$
//		worklogInputBuilder.setAuthor(new )

		return worklogInputBuilder.build();
	}

	public static ServerInfo convert(com.atlassian.jira.rest.client.domain.ServerInfo serverInfo) {
		ServerInfo serverInfoOut = new ServerInfo();

		serverInfoOut.setBaseUrl(serverInfo.getBaseUri().toString());
		serverInfoOut.setWebBaseUrl(serverInfo.getBaseUri().toString());
		serverInfoOut.setBuildDate(serverInfo.getBuildDate().toDate());
		serverInfoOut.setBuildNumber(Integer.valueOf(serverInfo.getBuildNumber()).toString());
		serverInfoOut.setVersion(serverInfo.getVersion());

		return serverInfoOut;
	}

	public static Iterable<JiraAction> convertTransitions(Iterable<Transition> transitions) {
		List<JiraAction> actions = new ArrayList<JiraAction>();

		for (Transition transition : transitions) {
			actions.add(convert(transition));
		}

		return actions;
	}

	private static JiraAction convert(Transition transition) {
		JiraAction action = new JiraAction(Integer.toString(transition.getId()), transition.getName());

		for (com.atlassian.jira.rest.client.domain.Transition.Field field : transition.getFields()) {

			// TODO rest set field name once available https://studio.atlassian.com/browse/JRJC-113
			IssueField outField = new IssueField(field.getId(), field.getId());
			outField.setType(field.getType());
			outField.setRequired(field.isRequired());

			action.getFields().add(outField);
		}

		return action;
	}

	public static Iterable<com.atlassian.jira.rest.client.domain.Version> convert(Version[] reportedVersions) {
		List<com.atlassian.jira.rest.client.domain.Version> outReportedVersions = new ArrayList<com.atlassian.jira.rest.client.domain.Version>();

		if (reportedVersions != null) {
			for (Version version : reportedVersions) {
				outReportedVersions.add(convert(version));
			}
		}

		return outReportedVersions;
	}

	private static com.atlassian.jira.rest.client.domain.Version convert(Version version) {
		com.atlassian.jira.rest.client.domain.Version outVersion = new com.atlassian.jira.rest.client.domain.Version(
				null, Long.valueOf(version.getId()), version.getName(), null, false, false, null);
		return outVersion;
	}

	public static Iterable<BasicComponent> convert(Component[] components) {
		List<BasicComponent> outComponents = new ArrayList<BasicComponent>();

		if (components != null) {
			for (Component component : components) {
				outComponents.add(convert(component));
			}
		}

		return outComponents;
	}

	private static BasicComponent convert(Component component) {
		return new BasicComponent(null, Long.valueOf(component.getId()), component.getName(), null);
	}

	public static NamedFilter[] convertNamedFilters(Iterable<FavouriteFilter> favouriteFilters) {
		List<NamedFilter> outFilters = new ArrayList<NamedFilter>();

		for (FavouriteFilter filter : favouriteFilters) {
			outFilters.add(convert(filter));
		}

		return outFilters.toArray(new NamedFilter[outFilters.size()]);
	}

	private static NamedFilter convert(FavouriteFilter filter) {
		NamedFilter outFilter = new NamedFilter();

		outFilter.setId(filter.getId().toString());
		outFilter.setName(filter.getName());
		outFilter.setJql(filter.getJql());
		outFilter.setViewUrl(filter.getViewUrl().toString());

		return outFilter;
	}

	public static JiraStatus[] convertStatuses(Iterable<Status> statuses) {
		List<JiraStatus> outStatuses = new ArrayList<JiraStatus>();

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

	public static FieldInput convert(CustomField customField) {

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

			if (customField.getValues().size() > 0 && customField.getValues().get(0) != null
					&& customField.getValues().get(0).length() > 0) {
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

			if (customField.getValues().size() > 0 && customField.getValues().get(0) != null
					&& customField.getValues().get(0).length() > 0) {
				String date = null;

				try {
					date = new DateTime(customField.getValues().get(0)).toString(JiraRestFields.DATE_TIME_FORMAT);
				} catch (IllegalArgumentException e) {

					date = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss") //$NON-NLS-1$
							.withLocale(Locale.ENGLISH)
							.parseDateTime(customField.getValues().get(0))
							.toString(JiraRestFields.DATE_TIME_FORMAT);
				}

				return new FieldInput(customField.getId(), date);
			}

			break;

		case FLOATFIELD:
			if (customField.getValues().size() > 0 && customField.getValues().get(0) != null
					&& customField.getValues().get(0).length() > 0) {
				return new FieldInput(customField.getId(), Float.parseFloat(customField.getValues().get(0)));
			}
			break;
		case MULTIUSERPICKER:
		case MULTIGROUPPICKER:
			if (customField.getValues().size() > 0 && customField.getValues().get(0) != null) {

				List<ComplexIssueInputFieldValue> fields = new ArrayList<ComplexIssueInputFieldValue>();

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
				return new FieldInput(customField.getId(), ComplexIssueInputFieldValue.with(JiraRestFields.NAME,
						customField.getValues().get(0)));
			}
			break;
		case SELECT:
		case RADIOBUTTONS:
			if (customField.getValues().size() > 0) {
				if (CustomField.NONE_ALLOWED_VALUE.equals(customField.getValues().get(0))) {
					return new FieldInput(customField.getId(), ComplexIssueInputFieldValue.with("id", "-1"));
				}
				String value = customField.getValues().get(0);
				return new FieldInput(customField.getId(), ComplexIssueInputFieldValue.with("value", value));
			}
			break;
		case MULTISELECT:
		case MULTICHECKBOXES:

//			if (customField.getValues().size() > 0) {
			List<ComplexIssueInputFieldValue> values = new ArrayList<ComplexIssueInputFieldValue>();
			for (String value : customField.getValues()) {
				values.add(ComplexIssueInputFieldValue.with("value", value)); //$NON-NLS-1$
			}

			return new FieldInput(customField.getId(), values);
//			}

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
