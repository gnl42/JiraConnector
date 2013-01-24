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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.osgi.util.NLS;
import org.joda.time.DateTime;

import com.atlassian.connector.eclipse.internal.jira.core.JiraAttribute;
import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;
import com.atlassian.connector.eclipse.internal.jira.core.JiraFieldType;
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
import com.atlassian.jira.rest.client.domain.input.WorklogInput;
import com.atlassian.jira.rest.client.domain.input.WorklogInputBuilder;
import com.atlassian.jira.rest.client.internal.json.JsonParseUtil;
import com.google.common.collect.ImmutableList;

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
		// TODO rest change first argument to real ID if available
		return new Resolution(resolution.getName(), resolution.getName(), resolution.getDescription(), null);
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

		jiraIssue.setCustomFields(getCustomFieldsFromIssue(issue));

		Project project = cache.getProjectByKey(issue.getProject().getKey());
		jiraIssue.setProject(project);
		if (project != null && !project.hasDetails()) {
			cache.refreshProjectDetails(project, monitor);
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
			jiraIssue.setPriority(cache.getPriorityByName(issue.getPriority().getName()));
		} else if (cache.getPriorities().length > 0) {
			jiraIssue.setPriority(cache.getPriorities()[0]);
		} else {
			jiraIssue.setPriority(null);
			StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN, NLS.bind(
					"Found issue with empty priority: {0}", issue.getKey())));
		}

		jiraIssue.setStatus(cache.getStatusByName(issue.getStatus().getName()));

		BasicUser assignee = issue.getAssignee();
		if (assignee != null) {
			jiraIssue.setAssignee(assignee.getName());
			jiraIssue.setAssigneeName(assignee.getDisplayName());
		}

		jiraIssue.setReporter(issue.getReporter().getName());
		jiraIssue.setReporterName(issue.getReporter().getDisplayName());
		jiraIssue.setResolution(issue.getResolution() == null ? null : cache.getResolutionByName(issue.getResolution()
				.getName()));
		if (issue.getTimeTracking().getOriginalEstimateMinutes() != null) {
			jiraIssue.setInitialEstimate(issue.getTimeTracking().getOriginalEstimateMinutes() * 60);
		}
		if (issue.getTimeTracking().getRemainingEstimateMinutes() != null) {
			jiraIssue.setEstimate(issue.getTimeTracking().getRemainingEstimateMinutes() * 60);
		}
		if (issue.getTimeTracking().getTimeSpentMinutes() != null) {
			jiraIssue.setActual(issue.getTimeTracking().getTimeSpentMinutes() * 60);
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

		jiraIssue.setSubtasks(convert(issue.getSubtasks()));
		jiraIssue.setType(convert(issue.getIssueType()));
		jiraIssue.setUrl(url + "/browse/" + issue.getKey()); //$NON-NLS-1$
		jiraIssue.setComponents(convertComponents(issue.getComponents()));

		Object env = issue.getField(JiraRestFields.ENVIRONMENT).getValue();
		if (env != null) {
			jiraIssue.setEnvironment(env.toString());
		} else {
			// hack: empty value is necessary to display environment field in the issue editor
			jiraIssue.setEnvironment(""); //$NON-NLS-1$
		}

		jiraIssue.setReportedVersions(convertVersions(issue.getAffectedVersions()));
		jiraIssue.setFixVersions(convertVersions(issue.getFixVersions()));

		DateTime dueDate = issue.getDueDate();
		if (dueDate != null) {
			jiraIssue.setDue(dueDate.toDate());
		} else {
			jiraIssue.setDue(null);
		}

		jiraIssue.setIssueLinks(convertIssueLinks(issue.getIssueLinks()));
		jiraIssue.setComments(convertComments(issue.getComments()));

		jiraIssue.setAttachments(convertAttachments(issue.getAttachments()));

		jiraIssue.setWorklogs(convertWorklogs(issue.getWorklogs()));

		jiraIssue.setRank(getRankFromIssue(issue));

		return jiraIssue;
	}

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

	private static CustomField generateCustomField(Field field, String longType) {

		try {

			JiraFieldType fieldType = JiraFieldType.fromKey(longType);

			List<String> values = null;

			switch (fieldType) {
			case TEXTFIELD:
			case TEXTAREA:
			case URL:
				values = ImmutableList.of(field.getValue().toString());
				break;
			case DATE:
			case DATETIME:
				values = ImmutableList.of(field.getValue().toString());
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
				values = ImmutableList.of(StringUtils.join(JiraRestCustomFieldsParser.parseLabels(field), ", ")); //$NON-NLS-1$
				break;
			case GROUPPICKER:
				values = ImmutableList.of(JiraRestCustomFieldsParser.parseGroupPicker(field));
				break;
			case MULTIGROUPPICKER:
				values = JiraRestCustomFieldsParser.parseMultiGroupPicker(field);
				break;
			default:
				// not supported custom field
			}

			if (values != null && !values.isEmpty()) {

				CustomField customField = new CustomField(field.getId(), longType, field.getName(), values);
				customField.setReadOnly(true);

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
		outAttachment.setAuthor(attachment.getAuthor().getName());
		outAttachment.setAuthorDisplayName(attachment.getAuthor().getDisplayName());
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

		outComment.setAuthor(comment.getAuthor().getName());
		outComment.setAuthorDisplayName(comment.getAuthor().getDisplayName());
		outComment.setComment(comment.getBody());
		outComment.setCreated(comment.getCreationDate().toDate());
		outComment.setMarkupDetected(true);

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
			outVersions.add(convert(version));
		}

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
		worklogInputBuilder.setMinutesSpent(new Long(jiraWorklog.getTimeSpent() / 60).intValue());
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
}
