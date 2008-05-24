/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.internal.jira.core.html.HTML2TextReader;
import org.eclipse.mylyn.internal.jira.core.model.Attachment;
import org.eclipse.mylyn.internal.jira.core.model.Comment;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.CustomField;
import org.eclipse.mylyn.internal.jira.core.model.IssueLink;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraAction;
import org.eclipse.mylyn.internal.jira.core.model.JiraField;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.SecurityLevel;
import org.eclipse.mylyn.internal.jira.core.model.Subtask;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.service.JiraInsufficientPermissionException;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteCustomFieldValue;
import org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteIssue;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.RepositoryResponse.ResponseKind;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeProperties;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskOperation;
import org.eclipse.mylyn.tasks.core.data.TaskRelation;
import org.eclipse.mylyn.tasks.core.data.TaskRelation.Direction;

/**
 * @author Mik Kersten
 * @author Rob Elves
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 * @since 3.0
 */
public class JiraTaskDataHandler extends AbstractTaskDataHandler {

	private static final String CONTEXT_ATTACHEMENT_FILENAME = "mylyn-context.zip";

	private static final String CONTEXT_ATTACHEMENT_FILENAME_LEGACY = "mylar-context.zip";

	private static final String CONTEXT_ATTACHMENT_DESCRIPTION = "mylyn/context/zip";

	private static final String CONTEXT_ATTACHMENT_DESCRIPTION_LEGACY = "mylar/context/zip";

	private static final boolean TRACE_ENABLED = Boolean.valueOf(Platform.getDebugOption("org.eclipse.mylyn.internal.jira.ui/dataHandler"));

	private static final String REASSIGN_OPERATION = "reassign";

	private static final String LEAVE_OPERATION = "leave";

	private static final String TASK_DATA_VERSION_1_0 = "1.0";

	private static final String TASK_DATA_VERSION_2_0 = "2.0";

	private final IJiraClientFactory clientFactory;

	public JiraTaskDataHandler(IJiraClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	public TaskData getTaskData(TaskRepository repository, String taskId, IProgressMonitor monitor)
			throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask("Getting task", IProgressMonitor.UNKNOWN);

			JiraClient client = clientFactory.getJiraClient(repository);
			if (!client.getCache().hasDetails()) {
				client.getCache().refreshDetails(monitor);
			}
			JiraIssue jiraIssue = getJiraIssue(client, taskId, repository.getRepositoryUrl(), monitor);
			if (jiraIssue != null) {
				return createTaskData(repository, client, jiraIssue, null, monitor);
			}
			throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN,
					IStatus.OK, "JIRA ticket not found: " + taskId, null));

		} catch (JiraException e) {
			IStatus status = JiraCorePlugin.toStatus(repository, e);
			trace(status);
			throw new CoreException(status);
		} finally {
			monitor.done();
		}
	}

	private JiraIssue getJiraIssue(JiraClient client, String taskId, String repositoryUrl, IProgressMonitor monitor) //
			throws CoreException, JiraException {
		try {
			int id = Integer.parseInt(taskId);
			// TODO consider keeping a cache of id -> key in the JIRA core plug-in
//			AbstractTask task = TasksUiPlugin.getTaskList().getTask(repositoryUrl, "" + id);
//			if (task != null) {
//				return client.getIssueByKey(task.getTaskKey(), monitor);
//			} else {
			String issueKey = client.getKeyFromId(id + "", monitor);
			return client.getIssueByKey(issueKey, monitor);
//			}
		} catch (NumberFormatException e) {
			return client.getIssueByKey(taskId, monitor);
		}
	}

	public TaskData createTaskData(TaskRepository repository, JiraClient client, JiraIssue jiraIssue,
			TaskData oldTaskData, IProgressMonitor monitor) throws JiraException {
		TaskData data = new TaskData(getAttributeMapper(repository), JiraCorePlugin.CONNECTOR_KIND,
				repository.getRepositoryUrl(), jiraIssue.getId());
		data.setVersion(TASK_DATA_VERSION_2_0);
		initializeTaskData(data, client, jiraIssue.getProject());
		updateTaskData(data, jiraIssue, client, oldTaskData, monitor);
		addOperations(data, jiraIssue, client, oldTaskData, monitor);
		return data;
	}

	public void initializeTaskData(TaskData data, JiraClient client, Project project) {
		createAttribute(data, JiraAttribute.CREATION_DATE);
		TaskAttribute summaryAttribute = createAttribute(data, JiraAttribute.SUMMARY);
		TaskAttributeProperties.from(summaryAttribute).setType(TaskAttribute.TYPE_SHORT_RICH_TEXT).applyTo(
				summaryAttribute);
		createAttribute(data, JiraAttribute.DESCRIPTION);
		createAttribute(data, JiraAttribute.STATUS);
		createAttribute(data, JiraAttribute.TASK_KEY);
		createAttribute(data, JiraAttribute.TASK_URL);
		createAttribute(data, JiraAttribute.USER_ASSIGNED);
		createAttribute(data, JiraAttribute.USER_REPORTER);
		createAttribute(data, JiraAttribute.MODIFICATION_DATE);

		TaskAttribute projectAttribute = createAttribute(data, JiraAttribute.PROJECT);
		Project[] jiraProjects = client.getCache().getProjects();
		for (Project jiraProject : jiraProjects) {
			projectAttribute.putOption(jiraProject.getId(), jiraProject.getName());
		}
		projectAttribute.setValue(project.getId());

		TaskAttribute resolutions = createAttribute(data, JiraAttribute.RESOLUTION);
		Resolution[] jiraResolutions = client.getCache().getResolutions();
		if (jiraResolutions.length > 0) {
			for (Resolution resolution : jiraResolutions) {
				resolutions.putOption(resolution.getId(), resolution.getName());
			}
		} else {
			resolutions.putOption(Resolution.FIXED_ID, "Fixed");
			resolutions.putOption(Resolution.WONT_FIX_ID, "Won't Fix");
			resolutions.putOption(Resolution.DUPLICATE_ID, "Duplicate");
			resolutions.putOption(Resolution.INCOMPLETE_ID, "Incomplete");
			resolutions.putOption(Resolution.CANNOT_REPRODUCE_ID, "Cannot Reproduce");
		}

		TaskAttribute priorities = createAttribute(data, JiraAttribute.PRIORITY);
		Priority[] jiraPriorities = client.getCache().getPriorities();
		for (int i = 0; i < jiraPriorities.length; i++) {
			Priority priority = jiraPriorities[i];
			priorities.putOption(priority.getId(), priority.getName());
			if (i == (jiraPriorities.length / 2)) {
				priorities.setValue(priority.getName());
			}
		}

		TaskAttribute types = createAttribute(data, JiraAttribute.TYPE);
		IssueType[] jiraIssueTypes = client.getCache().getIssueTypes();
		for (int i = 0; i < jiraIssueTypes.length; i++) {
			IssueType type = jiraIssueTypes[i];
			if (!type.isSubTaskType()) {
				types.putOption(type.getId(), type.getName());
				if (i == 0) {
					types.setValue(type.getName());
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

		TaskAttribute affectsVersions = createAttribute(data, JiraAttribute.AFFECTSVERSIONS);
		for (Version version : project.getVersions()) {
			affectsVersions.putOption(version.getId(), version.getName());
		}

		TaskAttribute components = createAttribute(data, JiraAttribute.COMPONENTS);
		for (Component component : project.getComponents()) {
			components.putOption(component.getId(), component.getName());
		}

		TaskAttribute fixVersions = createAttribute(data, JiraAttribute.FIXVERSIONS);
		for (Version version : project.getVersions()) {
			fixVersions.putOption(version.getId(), version.getName());
		}

		createAttribute(data, JiraAttribute.ENVIRONMENT);

		createAttribute(data, JiraAttribute.COMMENT_NEW);
	}

	private TaskAttribute createAttribute(TaskData data, JiraAttribute key) {
		TaskAttribute attribute = data.getRoot().createAttribute(key.getId());
		TaskAttributeProperties.defaults()
				.setReadOnly(key.isReadOnly())
				.setKind(key.getKind())
				.setLabel(key.getName())
				.setType(key.getType().getTaskType())
				.applyTo(attribute);
		attribute.putMetaDataValue(IJiraConstants.META_TYPE, key.getType().getKey());
		return attribute;
	}

	private void updateTaskData(TaskData data, JiraIssue jiraIssue, JiraClient client, TaskData oldTaskData,
			IProgressMonitor monitor) throws JiraException {
		String parentKey = jiraIssue.getParentKey();
		if (parentKey != null) {
			setAttributeValue(data, JiraAttribute.PARENT_KEY, parentKey);
		} else {
			removeAttribute(data, JiraAttribute.PARENT_KEY);
		}

		String parentId = jiraIssue.getParentId();
		if (parentId != null) {
			setAttributeValue(data, JiraAttribute.PARENT_ID, parentId);
		} else {
			removeAttribute(data, JiraAttribute.PARENT_ID);
		}

		Subtask[] subtasks = jiraIssue.getSubtasks();
		if (subtasks != null && subtasks.length > 0) {
			createAttribute(data, JiraAttribute.SUBTASK_IDS);
			createAttribute(data, JiraAttribute.SUBTASK_KEYS);
			for (Subtask subtask : subtasks) {
				addAttributeValue(data, JiraAttribute.SUBTASK_IDS, subtask.getIssueId());
				addAttributeValue(data, JiraAttribute.SUBTASK_KEYS, subtask.getIssueKey());
			}
		}

		IssueLink[] issueLinks = jiraIssue.getIssueLinks();
		if (issueLinks != null && issueLinks.length > 0) {
			HashMap<String, TaskAttribute> links = new HashMap<String, TaskAttribute>();
			for (IssueLink link : issueLinks) {
				String key;
				String desc;
				if (link.getInwardDescription() == null) {
					key = link.getLinkTypeId() + "outward";
					desc = link.getOutwardDescription();
				} else {
					key = link.getLinkTypeId() + "inward";
					desc = link.getInwardDescription();
				}
				String label = capitalize(desc) + ":";
				TaskAttribute attribute = links.get(key);
				if (attribute == null) {
					attribute = data.getRoot().createAttribute(IJiraConstants.ATTRIBUTE_LINK_PREFIX + key);
					TaskAttributeProperties.defaults().setKind(TaskAttribute.KIND_DEFAULT).setLabel(label).setType(
							JiraFieldType.ISSUELINKS.getTaskType()).applyTo(attribute);
					attribute.putMetaDataValue(IJiraConstants.META_TYPE, JiraFieldType.ISSUELINKS.getKey());
					links.put(key, attribute);
				}
				attribute.addValue(link.getIssueKey());

				if (link.getInwardDescription() != null) {
					attribute = data.getMappedAttribute(JiraAttribute.LINKED_IDS.getId());
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
		setAttributeValue(data, JiraAttribute.STATUS, jiraIssue.getStatus().getName());
		setAttributeValue(data, JiraAttribute.TASK_KEY, jiraIssue.getKey());
		setAttributeValue(data, JiraAttribute.TASK_URL, jiraIssue.getUrl());
		setAttributeValue(data, JiraAttribute.RESOLUTION, //
				jiraIssue.getResolution() == null ? "" : jiraIssue.getResolution().getId());
		setAttributeValue(data, JiraAttribute.MODIFICATION_DATE, JiraUtil.dateToString(jiraIssue.getUpdated()));
		setAttributeValue(data, JiraAttribute.USER_ASSIGNED, getAssignee(jiraIssue));
		setAttributeValue(data, JiraAttribute.USER_REPORTER, jiraIssue.getReporter());
		setAttributeValue(data, JiraAttribute.PROJECT, jiraIssue.getProject().getId());

		if (jiraIssue.getPriority() != null) {
			setAttributeValue(data, JiraAttribute.PRIORITY, jiraIssue.getPriority().getId());
		} else {
			removeAttribute(data, JiraAttribute.PRIORITY);
		}

		SecurityLevel securityLevel = jiraIssue.getSecurityLevel();
		if (securityLevel != null) {
			TaskAttribute attribute = createAttribute(data, JiraAttribute.SECURITY_LEVEL);
			attribute.putOption(securityLevel.getId(), securityLevel.getName());
			attribute.setValue(securityLevel.getId());
		}

		IssueType issueType = jiraIssue.getType();
		if (issueType != null) {
			TaskAttribute attribute = setAttributeValue(data, JiraAttribute.TYPE, issueType.getId());
			if (issueType.isSubTaskType()) {
				TaskAttributeProperties.from(attribute).setReadOnly(true).applyTo(attribute);
				attribute.putMetaDataValue(IJiraConstants.META_SUB_TASK_TYPE, Boolean.toString(true));
				attribute.clearOptions();
			}
		} else {
			removeAttribute(data, JiraAttribute.TYPE);
		}

		JiraTimeFormat timeFormat = new JiraTimeFormat();
		if (jiraIssue.getActual() > 0) {
			setAttributeValue(data, JiraAttribute.INITIAL_ESTIMATE, timeFormat.format(jiraIssue.getInitialEstimate()));
		} else {
			removeAttribute(data, JiraAttribute.INITIAL_ESTIMATE);
		}
		setAttributeValue(data, JiraAttribute.ESTIMATE, timeFormat.format(jiraIssue.getEstimate()));
		setAttributeValue(data, JiraAttribute.ACTUAL, timeFormat.format(jiraIssue.getActual()));

		if (jiraIssue.getDue() != null) {
			setAttributeValue(data, JiraAttribute.DUE_DATE, JiraUtil.dateToString(jiraIssue.getDue()));
		} else {
			removeAttribute(data, JiraAttribute.DUE_DATE);
		}

		if (jiraIssue.getComponents() != null) {
			for (Component component : jiraIssue.getComponents()) {
				addAttributeValue(data, JiraAttribute.COMPONENTS, component.getId());
			}
		}

		if (jiraIssue.getReportedVersions() != null) {
			for (Version version : jiraIssue.getReportedVersions()) {
				addAttributeValue(data, JiraAttribute.AFFECTSVERSIONS, version.getId());
			}
		}

		if (jiraIssue.getFixVersions() != null) {
			for (Version version : jiraIssue.getFixVersions()) {
				addAttributeValue(data, JiraAttribute.FIXVERSIONS, version.getId());
			}
		}

		if (jiraIssue.getEnvironment() != null) {
			setAttributeValue(data, JiraAttribute.ENVIRONMENT, jiraIssue.getEnvironment());
		} else {
			removeAttribute(data, JiraAttribute.ENVIRONMENT);
		}

		addComments(data, jiraIssue);
		addAttachments(data, jiraIssue, client);
		addCustomFields(data, jiraIssue);

		updateMarkup(data, jiraIssue, client, oldTaskData, monitor);

		HashSet<String> editableKeys = getEditableKeys(data, jiraIssue, client, oldTaskData, monitor);
		updateProperties(data, editableKeys);
	}

	private void addComments(TaskData data, JiraIssue jiraIssue) {
		int i = 1;
		for (Comment comment : jiraIssue.getComments()) {
			TaskAttribute attribute = data.getRoot().createAttribute(TaskAttribute.PREFIX_COMMENT + i);
			TaskCommentMapper taskComment = TaskCommentMapper.createFrom(attribute);
			taskComment.setAuthor(data.getAttributeMapper().getTaskRepository().createPerson(comment.getAuthor()));
			taskComment.setNumber(i);
			String commentText = comment.getComment();
			if (comment.isMarkupDetected()) {
				commentText = stripTags(commentText);
			}
			taskComment.setText(commentText);
			taskComment.setCreationDate(comment.getCreated());
			// TODO taskComment.setUrl()
			taskComment.applyTo(attribute);
			i++;
		}
	}

	private void addAttachments(TaskData data, JiraIssue jiraIssue, JiraClient client) {
		int i = 1;
		for (Attachment attachment : jiraIssue.getAttachments()) {
			TaskAttribute attribute = data.getRoot().createAttribute(TaskAttribute.PREFIX_ATTACHMENT + i);
			TaskAttachmentMapper taskAttachment = TaskAttachmentMapper.createFrom(attribute);
			taskAttachment.setAuthor(data.getAttributeMapper().getTaskRepository().createPerson(attachment.getAuthor()));
			taskAttachment.setFileName(attachment.getName());
			if (CONTEXT_ATTACHEMENT_FILENAME.equals(attachment.getName())) {
				taskAttachment.setDescription(CONTEXT_ATTACHMENT_DESCRIPTION);
			} else if (CONTEXT_ATTACHEMENT_FILENAME_LEGACY.equals(attachment.getName())) {
				taskAttachment.setDescription(CONTEXT_ATTACHMENT_DESCRIPTION_LEGACY);
			} else {
				taskAttachment.setDescription(attachment.getName());
			}
			taskAttachment.setLength(attachment.getSize());
			taskAttachment.setCreationDate(attachment.getCreated());
			taskAttachment.setUrl(client.getBaseUrl() + "/secure/attachment/" + attachment.getId() + "/"
					+ attachment.getName());
			taskAttachment.applyTo(attribute);
			i++;
		}
	}

	private void addCustomFields(TaskData data, JiraIssue jiraIssue) {
		for (CustomField field : jiraIssue.getCustomFields()) {
			String mappedKey = mapCommonAttributeKey(field.getId());
			String name = field.getName() + ":";
			String kind = JiraAttribute.valueById(mappedKey).getKind();
			String type = field.getKey();
			String taskType = JiraFieldType.fromKey(type).getTaskType();

			TaskAttribute attribute = data.getRoot().createAttribute(mappedKey);
			TaskAttributeProperties.defaults().setKind(kind).setLabel(name).setReadOnly(field.isReadOnly()).setType(
					taskType).applyTo(attribute);
			attribute.putMetaDataValue(IJiraConstants.META_TYPE, type);
			for (String value : field.getValues()) {
				attribute.addValue(value);
			}
		}
	}

	private HashSet<String> getEditableKeys(TaskData data, JiraIssue jiraIssue, JiraClient client,
			TaskData oldTaskData, IProgressMonitor monitor) throws JiraException {
		HashSet<String> editableKeys = new HashSet<String>();
		if (!JiraRepositoryConnector.isClosed(jiraIssue)) {
			if (useCachedInformation(jiraIssue, oldTaskData)) {
				// avoid server round-trips
				for (TaskAttribute attribute : oldTaskData.getRoot().getAttributes().values()) {
					if (!TaskAttributeProperties.from(attribute).isReadOnly()) {
						editableKeys.add(attribute.getId());
					}
				}

				TaskAttribute attribute = oldTaskData.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_READ_ONLY);
				if (attribute != null) {
					data.getRoot().deepCopyFrom(attribute);
				}
			} else {
				try {
					JiraField[] editableAttributes = client.getEditableAttributes(jiraIssue.getKey(), monitor);
					if (editableAttributes != null) {
						for (JiraField field : editableAttributes) {
							editableKeys.add(mapCommonAttributeKey(field.getId()));
						}
					}
				} catch (JiraInsufficientPermissionException ex) {
					// flag as read-only to avoid calling getEditableAttributes() on each sync
					data.getRoot().createAttribute(IJiraConstants.ATTRIBUTE_READ_ONLY);
				}
			}
		}
		return editableKeys;
	}

	private void updateProperties(TaskData data, HashSet<String> editableKeys) {
		for (TaskAttribute attribute : data.getRoot().getAttributes().values()) {
			TaskAttributeProperties properties = TaskAttributeProperties.from(attribute);
			boolean editable = editableKeys.contains(attribute.getId().toLowerCase());
			if (editable && (attribute.getId().startsWith(IJiraConstants.ATTRIBUTE_CUSTOM_PREFIX) //
					|| !JiraAttribute.valueById(attribute.getId()).isHidden())) {
				properties.setKind(TaskAttribute.KIND_DEFAULT);
			}

			if (TaskAttribute.COMMENT_NEW.equals(attribute.getId())
					|| TaskAttribute.RESOLUTION.equals(attribute.getId())) {
				properties.setReadOnly(false);
			} else {
				// make attributes read-only if can't find editing options
				String key = attribute.getMetaData(IJiraConstants.META_TYPE);
				Map<String, String> options = attribute.getOptions();
				if (JiraFieldType.SELECT.getKey().equals(key) && (options.isEmpty() || properties.isReadOnly())) {
					properties.setReadOnly(true);
				} else if (JiraFieldType.MULTISELECT.getKey().equals(key) && options.isEmpty()) {
					properties.setReadOnly(true);
				} else {
					properties.setReadOnly(!editable);
				}
			}
			properties.applyTo(attribute);
		}
	}

	private void addAttributeValue(TaskData data, JiraAttribute key, String value) {
		data.getRoot().getAttribute(key.getId()).addValue(value);
	}

	private TaskAttribute setAttributeValue(TaskData data, JiraAttribute key, String value) {
		TaskAttribute attribute = data.getRoot().getAttribute(key.getId());
		attribute.setValue(value);
		return attribute;
	}

	private boolean useCachedInformation(JiraIssue issue, TaskData oldTaskData) {
		if (oldTaskData != null && issue.getStatus() != null) {
			TaskAttribute attribute = oldTaskData.getMappedAttribute(TaskAttribute.STATUS);
			if (attribute != null) {
				return attribute.getValue().equals(issue.getStatus().getName());
			}
		}
		return false;
	}

//	private void removeAttributes(TaskData data, String keyPrefix) {
//		List<TaskAttribute> attributes = new ArrayList<TaskAttribute>(data.getRoot().getAttributes().values());
//		for (TaskAttribute attribute : attributes) {
//			if (attribute.getId().startsWith(keyPrefix)) {
//				removeAttribute(data, attribute.getId());
//			}
//		}
//	}

	private void removeAttribute(TaskData data, JiraAttribute key) {
		data.getRoot().removeAttribute(key.getId());
	}

	/**
	 * Removes attribute values without removing attribute to preserve order of attributes
	 */
//	private void removeAttributeValues(TaskData data, String attributeId) {
//		data.getRoot().getAttribute(attributeId).clearValues();
//	}
	private String capitalize(String s) {
		if (s.length() > 1) {
			char c = s.charAt(0);
			char uc = Character.toUpperCase(c);
			if (uc != c) {
				return uc + s.substring(1);
			}
		}
		return s;
	}

	private String getAssignee(JiraIssue jiraIssue) {
		String assignee = jiraIssue.getAssignee();
		return assignee == null || JiraRepositoryConnector.UNASSIGNED_USER.equals(assignee) ? "" : assignee;
	}

	public static String stripTags(String text) {
		if (text == null || text.length() == 0) {
			return "";
		}
		StringReader stringReader = new StringReader(text);
		HTML2TextReader html2TextReader = new HTML2TextReader(stringReader);
		try {
			char[] chars = new char[text.length()];
			int len = html2TextReader.read(chars, 0, text.length());
			if (len == -1) {
				return "";
			}
			return new String(chars, 0, len);
		} catch (IOException e) {
			return text;
		}
	}

	/**
	 * Replaces the values in fields that are suspected to contain rendered markup with the source values retrieved
	 * through SOAP.
	 */
	private void updateMarkup(TaskData data, JiraIssue jiraIssue, JiraClient client, TaskData oldTaskData,
			IProgressMonitor monitor) throws JiraException {
		if (!jiraIssue.isMarkupDetected()) {
			return;
		}

		if (jiraIssue.getUpdated() != null && oldTaskData != null) {
			String value = getAttributeValue(oldTaskData, JiraAttribute.MODIFICATION_DATE);
			if (jiraIssue.getUpdated().equals(JiraUtil.stringToDate(value))) {
				// use cached information
				if (data.getRoot().getAttribute(TaskAttribute.DESCRIPTION) != null) {
					setAttributeValue(data, JiraAttribute.DESCRIPTION, getAttributeValue(oldTaskData,
							JiraAttribute.DESCRIPTION));
				}
				if (data.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_ENVIRONMENT) != null) {
					setAttributeValue(data, JiraAttribute.ENVIRONMENT, getAttributeValue(oldTaskData,
							JiraAttribute.ENVIRONMENT));
				}
				for (CustomField field : jiraIssue.getCustomFields()) {
					if (field.isMarkupDetected()) {
						TaskAttribute oldAttribute = oldTaskData.getRoot().getAttribute(field.getId());
						if (oldAttribute != null) {
							TaskAttribute attribute = data.getRoot().getAttribute(field.getId());
							attribute.setValues(oldAttribute.getValues());
						}
					}
				}
				return;
			}
		}

		// consider preserving HTML 
		RemoteIssue remoteIssue = client.getSoapClient().getIssueByKey(jiraIssue.getKey(), monitor);
		if (data.getRoot().getAttribute(TaskAttribute.DESCRIPTION) != null) {
			if (remoteIssue.getDescription() == null) {
				setAttributeValue(data, JiraAttribute.DESCRIPTION, "");
			} else {
				setAttributeValue(data, JiraAttribute.DESCRIPTION, remoteIssue.getDescription());
			}
		}
		if (data.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_ENVIRONMENT) != null) {
			if (remoteIssue.getEnvironment() == null) {
				setAttributeValue(data, JiraAttribute.ENVIRONMENT, "");
			} else {
				setAttributeValue(data, JiraAttribute.ENVIRONMENT, remoteIssue.getEnvironment());
			}
		}
		RemoteCustomFieldValue[] fields = remoteIssue.getCustomFieldValues();
		for (CustomField field : jiraIssue.getCustomFields()) {
			if (field.isMarkupDetected()) {
				innerLoop: for (RemoteCustomFieldValue remoteField : fields) {
					if (field.getId().equals(remoteField.getCustomfieldId())) {
						TaskAttribute attribute = data.getRoot().getAttribute(field.getId());
						if (attribute != null) {
							attribute.setValues(Arrays.asList(remoteField.getValues()));
						}
						break innerLoop;
					}
				}
			}
		}

	}

	public void addOperations(TaskData data, JiraIssue issue, JiraClient client, TaskData oldTaskData,
			IProgressMonitor monitor) throws JiraException {
		// avoid server round-trips
		if (useCachedInformation(issue, oldTaskData)) {
			TaskAttribute[] attributes = oldTaskData.getAttributeMapper().getAttributesByType(oldTaskData,
					TaskAttribute.TYPE_OPERATION);
			for (TaskAttribute taskAttribute : attributes) {
				data.getRoot().deepAddCopy(taskAttribute);
			}
			return;
		}

		String label = "Leave as " + issue.getStatus().getName();
		TaskAttribute attribute = data.getRoot().createAttribute(TaskAttribute.PREFIX_OPERATION + LEAVE_OPERATION);
		TaskOperation.applyTo(attribute, LEAVE_OPERATION, label);
		// set as default
		attribute = data.getRoot().createAttribute(TaskAttribute.OPERATION);
		TaskOperation.applyTo(attribute, LEAVE_OPERATION, label);

		// TODO need more accurate status matching
//		if (!JiraRepositoryConnector.isCompleted(data)) {
//			attribute = operationContainer.createAttribute(REASSIGN_OPERATION);
//			operation = TaskOperation.createFrom(attribute);
//			operation.setLabel("Reassign to");
//			operation.applyTo(attribute);
//
//			String attributeId = REASSIGN_OPERATION + "::" + JiraAttribute.USER_ASSIGNED.getParamName();
//			TaskAttribute associatedAttribute = createAttribute(data, attributeId);
//			associatedAttribute.setValue(client.getUserName());
//			attribute.putMetaDataValue(TaskAttribute.META_ASSOCIATED_ATTRIBUTE_ID, attributeId);
//		}

		JiraAction[] availableActions = client.getAvailableActions(issue.getKey(), monitor);
		if (availableActions != null) {
			for (JiraAction action : availableActions) {
				attribute = data.getRoot().createAttribute(TaskAttribute.PREFIX_OPERATION + action.getId());
				TaskOperation.applyTo(attribute, action.getId(), action.getName());

				String[] fields = client.getActionFields(issue.getKey(), action.getId(), monitor);
				for (String field : fields) {
					if (TaskAttribute.RESOLUTION.equals(mapCommonAttributeKey(field))) {
						attribute.putMetaDataValue(TaskAttribute.META_ASSOCIATED_ATTRIBUTE_ID, TaskAttribute.RESOLUTION);
					}
					// TODO handle other action fields
				}
			}
		}
	}

	@Override
	public RepositoryResponse postTaskData(TaskRepository repository, TaskData taskData,
			Set<TaskAttribute> changedAttributes, IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask("Sending task", IProgressMonitor.UNKNOWN);
			JiraClient client = clientFactory.getJiraClient(repository);
			if (client == null) {
				throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN,
						IStatus.ERROR, "Unable to create Jira client", null));
			}
			try {
				if (!client.getCache().hasDetails()) {
					client.getCache().refreshDetails(new NullProgressMonitor());
				}

				JiraIssue issue = buildJiraIssue(taskData, client);
				if (taskData.isNew()) {
					if (issue.getType().isSubTaskType() && issue.getParentId() != null) {
						issue = client.createSubTask(issue, monitor);
					} else {
						issue = client.createIssue(issue, monitor);
					}
					if (issue == null) {
						throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR,
								JiraCorePlugin.ID_PLUGIN, IStatus.OK, "Could not create issue.", null));
					}
					// this is severely broken: should return id instead
					//return issue.getKey();
					return new RepositoryResponse(ResponseKind.TASK_CREATED, issue.getId());
				} else {
					String operationId = getOperationId(taskData);
					String newComment = getNewComment(taskData);
					if (LEAVE_OPERATION.equals(operationId) || REASSIGN_OPERATION.equals(operationId)) {
						if (!JiraRepositoryConnector.isClosed(issue)
								&& taskData.getMappedAttribute(IJiraConstants.ATTRIBUTE_READ_ONLY) == null) {
							client.updateIssue(issue, newComment, monitor);
						} else if (newComment.length() > 0) {
							client.addCommentToIssue(issue, newComment, monitor);
						}
					} else {
						client.advanceIssueWorkflow(issue, operationId, newComment, monitor);
					}
					return new RepositoryResponse(ResponseKind.TASK_UPDATED, issue.getId());
				}
			} catch (JiraException e) {
				IStatus status = JiraCorePlugin.toStatus(repository, e);
				trace(status);
				throw new CoreException(status);
			}
		} finally {
			monitor.done();
		}
	}

	private String getNewComment(TaskData taskData) {
		String newComment = "";
		TaskAttribute attribute = taskData.getMappedAttribute(TaskAttribute.COMMENT_NEW);
		if (attribute != null) {
			newComment = taskData.getAttributeMapper().getValue(attribute);
		}
		return newComment;
	}

	private String getOperationId(TaskData taskData) {
		String operationId = "";
		TaskAttribute attribute = taskData.getMappedAttribute(TaskAttribute.OPERATION);
		if (attribute != null) {
			operationId = taskData.getAttributeMapper().getValue(attribute);
		}
		if (operationId.length() == 0) {
			operationId = LEAVE_OPERATION;
		}
		return operationId;
	}

	@Override
	public boolean initializeTaskData(TaskRepository repository, TaskData data, IProgressMonitor monitor)
			throws CoreException {
		TaskAttribute projectAttribute = data.getRoot().getAttribute(TaskAttribute.PRODUCT);
		if (projectAttribute == null) {
			return false;
		}

		JiraClient client = clientFactory.getJiraClient(repository);
		if (!client.getCache().hasDetails()) {
			try {
				client.getCache().refreshDetails(monitor);
			} catch (JiraException ex) {
				IStatus status = JiraCorePlugin.toStatus(repository, ex);
				trace(status);
				throw new CoreException(status);
			}
		}

		Project project = client.getCache().getProjectById(projectAttribute.getValue());
		if (project == null) {
			return false;
		}

		initializeTaskData(data, client, project);
		setAttributeValue(data, JiraAttribute.PROJECT, project.getId());
		return true;
	}

	@Override
	public boolean initializeSubTaskData(TaskRepository repository, TaskData taskData, TaskData parentTaskData,
			IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask("Creating subtask", IProgressMonitor.UNKNOWN);

			JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
			if (!client.getCache().hasDetails()) {
				client.getCache().refreshDetails(new SubProgressMonitor(monitor, 1));
			}

//			Project project = getProject(client, parentTaskData.getProduct());
//			if (project == null) {
//				throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN,
//						IStatus.OK, "The parent task does not have a valid project.", null));
//			}
//
//			initializeTaskData(taskData, client, project);
//			//cloneTaskData(parentTaskData, taskData);
//			taskData.setDescription("");
//			taskData.setSummary("");
//			setAttributeValue(taskdata, TaskAttribute.USER_ASSIGNED, parentTaskData.getAssignedTo());
//			setAttributeValue(taskdata, TaskAttribute.PRODUCT, project.getName());
//
//			// set subtask type
//			TaskAttribute typeAttribute = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_TYPE);
//			typeAttribute.clearOptions();
//
//			IssueType[] jiraIssueTypes = client.getCache().getIssueTypes();
//			for (IssueType type : jiraIssueTypes) {
//				if (type.isSubTaskType()) {
//					typeAttribute.putOption(type.getName(), type.getId());
//				}
//			}
//
//			List<String> options = typeAttribute.getOptions();
//			if (options.size() == 0) {
//				throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN,
//						IStatus.OK, "The repository does not support subtasks.", null));
//			} else if (options.size() == 1) {
//				setReadOnly(typeAttribute, true);
//			}
//			typeAttribute.setValue(options.get(0));
//
//			// set parent id
//			TaskAttribute attribute = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_ISSUE_PARENT_ID);
//			attribute.setValue(parentTaskData.getTaskId());
//
//			attribute = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_ISSUE_PARENT_KEY);
//			attribute.setValue(parentTaskData.getTaskKey());

			return true;
		} catch (JiraException e) {
			IStatus status = JiraCorePlugin.toStatus(repository, e);
			trace(status);
			throw new CoreException(status);
		} finally {
			monitor.done();
		}
	}

	@Override
	public boolean canInitializeSubTaskData(ITask task, TaskData parentTaskData) {
		return true;
	}

	private JiraIssue buildJiraIssue(TaskData taskData, JiraClient client) {
		JiraIssue issue = new JiraIssue();
		issue.setId(taskData.getTaskId());
		issue.setKey(getAttributeValue(taskData, JiraAttribute.TASK_KEY));
		issue.setSummary(getAttributeValue(taskData, JiraAttribute.SUMMARY));
		issue.setDescription(getAttributeValue(taskData, JiraAttribute.DESCRIPTION));

		// TODO sync due date between jira and local planning
		issue.setDue(JiraUtil.stringToDate(getAttributeValue(taskData, JiraAttribute.DUE_DATE)));

		String parentId = getAttributeValue(taskData, JiraAttribute.PARENT_ID);
		if (parentId != null) {
			issue.setParentId(parentId);
		}

		TaskAttribute securityLevelAttribute = taskData.getMappedAttribute(IJiraConstants.ATTRIBUTE_SECURITY_LEVEL);
		if (securityLevelAttribute != null) {
			issue.setSecurityLevel(new SecurityLevel(securityLevelAttribute.getValue()));
		}

		String estimate = getAttributeValue(taskData, JiraAttribute.ESTIMATE);
		if (estimate != null) {
			JiraTimeFormat timeFormat = new JiraTimeFormat();
			issue.setEstimate(timeFormat.parse(estimate));
		}

		estimate = getAttributeValue(taskData, JiraAttribute.INITIAL_ESTIMATE);
		if (estimate != null) {
			JiraTimeFormat timeFormat = new JiraTimeFormat();
			issue.setInitialEstimate(timeFormat.parse(estimate));
		}

		issue.setProject(new Project(getAttributeValue(taskData, JiraAttribute.PROJECT)));

		boolean subTaskType = Boolean.parseBoolean(getAttribute(taskData, JiraAttribute.TYPE).getMetaData(
				IJiraConstants.META_SUB_TASK_TYPE));
		IssueType issueType = new IssueType(getAttributeValue(taskData, JiraAttribute.TYPE), subTaskType);
		issue.setType(issueType);

		issue.setStatus(new JiraStatus(getAttributeValue(taskData, JiraAttribute.STATUS)));

		TaskAttribute componentsAttribute = taskData.getMappedAttribute(IJiraConstants.ATTRIBUTE_COMPONENTS);
		if (componentsAttribute != null) {
			ArrayList<Component> components = new ArrayList<Component>();
			for (String value : componentsAttribute.getValues()) {
				components.add(new Component(value));
			}
			issue.setComponents(components.toArray(new Component[components.size()]));
		}

		TaskAttribute fixVersionAttr = taskData.getMappedAttribute(IJiraConstants.ATTRIBUTE_FIXVERSIONS);
		if (fixVersionAttr != null) {
			ArrayList<Version> fixVersions = new ArrayList<Version>();
			for (String value : fixVersionAttr.getValues()) {
				fixVersions.add(new Version(value));
			}
			issue.setFixVersions(fixVersions.toArray(new Version[fixVersions.size()]));
		}

		TaskAttribute affectsVersionAttr = taskData.getMappedAttribute(IJiraConstants.ATTRIBUTE_AFFECTSVERSIONS);
		if (affectsVersionAttr != null) {
			ArrayList<Version> affectsVersions = new ArrayList<Version>();
			for (String value : affectsVersionAttr.getValues()) {
				affectsVersions.add(new Version(value));
			}
			issue.setReportedVersions(affectsVersions.toArray(new Version[affectsVersions.size()]));
		}

		issue.setReporter(getAttributeValue(taskData, JiraAttribute.USER_REPORTER));

		String assignee = getAttributeValue(taskData, JiraAttribute.USER_ASSIGNED);
		issue.setAssignee(JiraRepositoryConnector.getAssigneeFromAttribute(assignee));

		issue.setEnvironment(getAttributeValue(taskData, JiraAttribute.ENVIRONMENT));
		issue.setPriority(new Priority(getAttributeValue(taskData, JiraAttribute.PRIORITY)));

		ArrayList<CustomField> customFields = new ArrayList<CustomField>();
		for (TaskAttribute attribute : taskData.getRoot().getAttributes().values()) {
			if (attribute.getId().startsWith(IJiraConstants.ATTRIBUTE_CUSTOM_PREFIX)) {
				String id = attribute.getId().substring(IJiraConstants.ATTRIBUTE_CUSTOM_PREFIX.length());
				String type = attribute.getMetaData(IJiraConstants.META_TYPE);
				CustomField field = new CustomField(id, type, "", attribute.getValues());
				customFields.add(field);
			}
		}
		issue.setCustomFields(customFields.toArray(new CustomField[customFields.size()]));

		return issue;
	}

	private TaskAttribute getAttribute(TaskData taskData, JiraAttribute key) {
		return taskData.getRoot().getAttribute(key.getId());
	}

	private String getAttributeValue(TaskData taskData, JiraAttribute key) {
		TaskAttribute attribute = taskData.getRoot().getAttribute(key.getId());
		return (attribute != null) ? attribute.getValue() : null;
	}

	@Override
	public TaskRelation[] getTaskRelations(TaskData taskData) {
		List<TaskRelation> relations = new ArrayList<TaskRelation>();
		TaskAttribute attribute = taskData.getRoot().getAttribute(JiraAttribute.SUBTASK_IDS.getId());
		if (attribute != null) {
			for (String taskId : attribute.getValues()) {
				relations.add(TaskRelation.subtask(taskId));
			}
		}
		attribute = taskData.getRoot().getAttribute(JiraAttribute.LINKED_IDS.getId());
		if (attribute != null) {
			for (String taskId : attribute.getValues()) {
				relations.add(TaskRelation.dependency(taskId, Direction.OUTWARD));
			}
		}
		return relations.toArray(new TaskRelation[0]);
	}

	private static void trace(IStatus status) {
		if (TRACE_ENABLED) {
			JiraCorePlugin.getDefault().getLog().log(status);
		}
	}

	@Override
	public TaskAttributeMapper getAttributeMapper(TaskRepository taskRepository) {
		JiraClient client = clientFactory.getJiraClient(taskRepository);
		return new JiraAttributeMapper(taskRepository, client);
	}

	@Override
	public void migrateTaskData(TaskRepository taskRepository, TaskData taskData) {
		if (TASK_DATA_VERSION_1_0.equals(taskData.getVersion())) {
			for (TaskAttribute attribute : taskData.getRoot().getAttributes().values()) {
				JiraFieldType type = JiraFieldType.fromKey(attribute.getMetaData(IJiraConstants.META_TYPE));
				if ((JiraFieldType.SELECT == type || JiraFieldType.MULTISELECT == type)
						&& !attribute.getOptions().isEmpty()) {
					// convert option values to keys: version 1.0 stored value whereas 2.0 stores keys 
					Set<String> values = new HashSet<String>(attribute.getValues());
					attribute.clearValues();
					Map<String, String> options = attribute.getOptions();
					for (String key : options.keySet()) {
						if (values.contains(options.get(key))) {
							attribute.addValue(key);
						}
					}
				}
			}
			taskData.setVersion(TASK_DATA_VERSION_2_0);
		}
	}

	private String mapCommonAttributeKey(String key) {
		if ("summary".equals(key)) {
			return JiraAttribute.SUMMARY.getId();
		} else if ("description".equals(key)) {
			return JiraAttribute.DESCRIPTION.getId();
		} else if ("priority".equals(key)) {
			return JiraAttribute.PRIORITY.getId();
		} else if ("resolution".equals(key)) {
			return JiraAttribute.RESOLUTION.getId();
		} else if ("assignee".equals(key)) {
			return JiraAttribute.USER_ASSIGNED.getId();
		} else if ("environment".equals(key)) {
			return JiraAttribute.ENVIRONMENT.getId();
		} else if ("issuetype".equals(key)) {
			return JiraAttribute.TYPE.getId();
		} else if ("components".equals(key)) {
			return JiraAttribute.COMPONENTS.getId();
		} else if ("versions".equals(key)) {
			return JiraAttribute.AFFECTSVERSIONS.getId();
		} else if ("fixVersions".equals(key)) {
			return JiraAttribute.FIXVERSIONS.getId();
		} else if ("timetracking".equals(key)) {
			return JiraAttribute.ESTIMATE.getId();
		} else if ("duedate".equals(key)) {
			return JiraAttribute.DUE_DATE.getId();
		}
		if (key.startsWith("issueLink")) {
			return IJiraConstants.ATTRIBUTE_LINK_PREFIX + key;
		}
		if (key.startsWith("customfield")) {
			return IJiraConstants.ATTRIBUTE_CUSTOM_PREFIX + key;
		}
		return key;
	}

}