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
import java.util.Collection;
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
import org.eclipse.mylyn.internal.jira.core.model.JiraVersion;
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
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.RepositoryResponse.ResponseKind;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskOperation;

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

	private static final JiraVersion TASK_DATA_VERSION_1_0 = new JiraVersion("1.0");

	private static final JiraVersion TASK_DATA_VERSION_2_0 = new JiraVersion("2.0");

	private static final JiraVersion TASK_DATA_VERSION_2_2 = new JiraVersion("2.2");

	private static final JiraVersion TASK_DATA_VERSION_CURRENT = new JiraVersion("3.0");

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
		initializeTaskData(data, client, jiraIssue.getProject());
		updateTaskData(data, jiraIssue, client, oldTaskData, monitor);
		addOperations(data, jiraIssue, client, oldTaskData, monitor);
		return data;
	}

	public void initializeTaskData(TaskData data, JiraClient client, Project project) {
		data.setVersion(TASK_DATA_VERSION_CURRENT.toString());

		createAttribute(data, JiraAttribute.CREATION_DATE);
		TaskAttribute summaryAttribute = createAttribute(data, JiraAttribute.SUMMARY);
		summaryAttribute.getMetaData().setType(TaskAttribute.TYPE_SHORT_RICH_TEXT);
		TaskAttribute descriptionAttribute = createAttribute(data, JiraAttribute.DESCRIPTION);
		descriptionAttribute.getMetaData().setType(TaskAttribute.TYPE_LONG_RICH_TEXT);
		createAttribute(data, JiraAttribute.STATUS);
		createAttribute(data, JiraAttribute.ISSUE_KEY);
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
				priorities.setValue(priority.getId());
			}
		}

		TaskAttribute types = createAttribute(data, JiraAttribute.TYPE);
		IssueType[] jiraIssueTypes = client.getCache().getIssueTypes();
		for (int i = 0; i < jiraIssueTypes.length; i++) {
			IssueType type = jiraIssueTypes[i];
			if (!type.isSubTaskType()) {
				types.putOption(type.getId(), type.getName());
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

		if (!data.isNew()) {
			TaskAttribute commentAttribute = createAttribute(data, JiraAttribute.COMMENT_NEW);
			commentAttribute.getMetaData().setType(TaskAttribute.TYPE_LONG_RICH_TEXT);
		}
	}

	private TaskAttribute createAttribute(TaskData data, JiraAttribute key) {
		TaskAttribute attribute = data.getRoot().createAttribute(key.id());
		attribute.getMetaData().defaults() //
				.setReadOnly(key.isReadOnly())
				.setKind(key.getKind())
				.setLabel(key.getName())
				.setType(key.getType().getTaskType())
				.putValue(IJiraConstants.META_TYPE, key.getType().getKey());
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
					attribute.getMetaData() //
							.setKind(TaskAttribute.KIND_DEFAULT)
							.setLabel(label)
							.setType(JiraFieldType.ISSUELINKS.getTaskType())
							.putValue(IJiraConstants.META_TYPE, JiraFieldType.ISSUELINKS.getKey());
					links.put(key, attribute);
				}
				if (attribute.getValue().length() > 0) {
					attribute.setValue(attribute.getValue() + " " + link.getIssueKey());
				} else {
					attribute.setValue(link.getIssueKey());
				}

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
				jiraIssue.getResolution() == null ? "" : jiraIssue.getResolution().getId());
		setAttributeValue(data, JiraAttribute.MODIFICATION_DATE, JiraUtil.dateToString(jiraIssue.getUpdated()));
		setAttributeValue(data, JiraAttribute.USER_ASSIGNED, getAssignee(jiraIssue));
		setAttributeValue(data, JiraAttribute.USER_REPORTER, getReporter(jiraIssue));
		setAttributeValue(data, JiraAttribute.PROJECT, jiraIssue.getProject().getId());

		if (jiraIssue.getStatus() != null) {
			TaskAttribute attribute = data.getRoot().getAttribute(JiraAttribute.STATUS.id());
			attribute.putOption(jiraIssue.getStatus().getId(), jiraIssue.getStatus().getName());
			attribute.setValue(jiraIssue.getStatus().getId());
		}

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
				attribute.getMetaData() //
						.setReadOnly(true)
						.putValue(IJiraConstants.META_SUB_TASK_TYPE, Boolean.toString(true));
				attribute.clearOptions();
				attribute.putOption(issueType.getId(), issueType.getName());
			}
		} else {
			removeAttribute(data, JiraAttribute.TYPE);
		}

		// if no time was logged initial estimate and estimate are the same value, only include estimate in this case
		if (jiraIssue.getActual() > 0) {
			setAttributeValue(data, JiraAttribute.INITIAL_ESTIMATE, jiraIssue.getInitialEstimate() + "");
		} else {
			removeAttribute(data, JiraAttribute.INITIAL_ESTIMATE);
		}
		setAttributeValue(data, JiraAttribute.ESTIMATE, jiraIssue.getEstimate() + "");
		setAttributeValue(data, JiraAttribute.ACTUAL, jiraIssue.getActual() + "");

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

	private String getReporter(JiraIssue jiraIssue) {
		String reporter = jiraIssue.getReporter();
		return reporter == null ? "" : reporter;
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
			taskAttachment.setAttachmentId(attachment.getId());
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
			attribute.getMetaData().defaults() //
					.setKind(kind)
					.setLabel(name)
					.setReadOnly(field.isReadOnly())
					.setType(taskType)
					.putValue(IJiraConstants.META_TYPE, type);
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
					if (!attribute.getMetaData().isReadOnly()) {
						editableKeys.add(attribute.getId());
					}
				}

				TaskAttribute attribute = oldTaskData.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_READ_ONLY);
				if (attribute != null) {
					data.getRoot().deepAddCopy(attribute);
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
			TaskAttributeMetaData properties = attribute.getMetaData();
			boolean editable = editableKeys.contains(attribute.getId().toLowerCase());
			if (editable && (attribute.getId().startsWith(IJiraConstants.ATTRIBUTE_CUSTOM_PREFIX) //
					|| !JiraAttribute.valueById(attribute.getId()).isHidden())) {
				properties.setKind(TaskAttribute.KIND_DEFAULT);
			}

			if (TaskAttribute.COMMENT_NEW.equals(attribute.getId())
					|| TaskAttribute.RESOLUTION.equals(attribute.getId())
					|| TaskAttribute.USER_ASSIGNED.equals(attribute.getId())) {
				properties.setReadOnly(false);
			} else {
				// make attributes read-only if can't find editing options
				String key = properties.getValue(IJiraConstants.META_TYPE);
				Map<String, String> options = attribute.getOptions();
				if (JiraFieldType.SELECT.getKey().equals(key) && (options.isEmpty() || properties.isReadOnly())) {
					properties.setReadOnly(true);
				} else if (JiraFieldType.MULTISELECT.getKey().equals(key) && options.isEmpty()) {
					properties.setReadOnly(true);
				} else {
					properties.setReadOnly(!editable);
				}
			}
		}
	}

	private void addAttributeValue(TaskData data, JiraAttribute key, String value) {
		data.getRoot().getAttribute(key.id()).addValue(value);
	}

	private TaskAttribute setAttributeValue(TaskData data, JiraAttribute key, String value) {
		TaskAttribute attribute = data.getRoot().getAttribute(key.id());
		attribute.setValue(value);
		return attribute;
	}

	private boolean useCachedInformation(JiraIssue issue, TaskData oldTaskData) {
		if (oldTaskData != null && issue.getStatus() != null) {
			TaskAttribute attribute = oldTaskData.getRoot().getMappedAttribute(TaskAttribute.STATUS);
			if (attribute != null) {
				return attribute.getValue().equals(issue.getStatus().getId());
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
		data.getRoot().removeAttribute(key.id());
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
			List<TaskAttribute> attributes = oldTaskData.getAttributeMapper().getAttributesByType(oldTaskData,
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
						attribute.getMetaData().putValue(TaskAttribute.META_ASSOCIATED_ATTRIBUTE_ID,
								TaskAttribute.RESOLUTION);
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
								&& taskData.getRoot().getMappedAttribute(IJiraConstants.ATTRIBUTE_READ_ONLY) == null) {
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
		TaskAttribute attribute = taskData.getRoot().getMappedAttribute(TaskAttribute.COMMENT_NEW);
		if (attribute != null) {
			newComment = taskData.getAttributeMapper().getValue(attribute);
		}
		return newComment;
	}

	private String getOperationId(TaskData taskData) {
		String operationId = "";
		TaskAttribute attribute = taskData.getRoot().getMappedAttribute(TaskAttribute.OPERATION);
		if (attribute != null) {
			operationId = taskData.getAttributeMapper().getValue(attribute);
		}
		if (operationId.length() == 0) {
			operationId = LEAVE_OPERATION;
		}
		return operationId;
	}

	@Override
	public boolean initializeTaskData(TaskRepository repository, TaskData data, ITaskMapping initializationData,
			IProgressMonitor monitor) throws CoreException {
		if (initializationData == null) {
			return false;
		}
		String product = initializationData.getProduct();
		if (product == null) {
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
		Project project = getProject(client, product);
		if (project == null) {
			return false;
		}
		initializeTaskData(data, client, project);
		return true;
	}

	private Project getProject(JiraClient client, String product) {
		Project[] projects = client.getCache().getProjects();
		for (Project project : projects) {
			if (product.equals(project.getName()) || product.equals(project.getKey())) {
				return project;
			}
		}
		return null;
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

			TaskAttribute projectAttribute = parentTaskData.getRoot().getAttribute(TaskAttribute.PRODUCT);
			if (projectAttribute == null) {
				throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN,
						IStatus.OK, "The parent task does not have a valid project.", null));
			}

			Project project = client.getCache().getProjectById(projectAttribute.getValue());
			initializeTaskData(taskData, client, project);

			new JiraTaskMapper(taskData).merge(new JiraTaskMapper(parentTaskData));
			taskData.getRoot().getAttribute(JiraAttribute.PROJECT.id()).setValue(project.getId());
			taskData.getRoot().getAttribute(JiraAttribute.DESCRIPTION.id()).setValue("");
			taskData.getRoot().getAttribute(JiraAttribute.SUMMARY.id()).setValue("");

			// set subtask type
			TaskAttribute typeAttribute = taskData.getRoot().getAttribute(JiraAttribute.TYPE.id());
			typeAttribute.clearOptions();
			IssueType[] jiraIssueTypes = client.getCache().getIssueTypes();
			for (IssueType type : jiraIssueTypes) {
				if (type.isSubTaskType()) {
					typeAttribute.putOption(type.getId(), type.getName());
				}
			}
			Map<String, String> options = typeAttribute.getOptions();
			if (options.size() == 0) {
				throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN,
						IStatus.OK, "The repository does not support subtasks.", null));
			} else if (options.size() == 1) {
				typeAttribute.getMetaData().setReadOnly(true);
			}
			typeAttribute.setValue(options.keySet().iterator().next());
			typeAttribute.getMetaData().putValue(IJiraConstants.META_SUB_TASK_TYPE, Boolean.TRUE.toString());

			// set parent id
			TaskAttribute attribute = taskData.getRoot().getAttribute(JiraAttribute.PARENT_ID.id());
			attribute.setValue(parentTaskData.getTaskId());
			attribute = taskData.getRoot().getAttribute(JiraAttribute.PARENT_KEY.id());
			attribute.setValue(parentTaskData.getRoot().getAttribute(JiraAttribute.ISSUE_KEY.id()).getValue());

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
	public boolean canInitializeSubTaskData(TaskRepository taskRepository, ITask task) {
		return true;
	}

	private JiraIssue buildJiraIssue(TaskData taskData, JiraClient client) {
		JiraIssue issue = new JiraIssue();
		issue.setId(taskData.getTaskId());
		issue.setKey(getAttributeValue(taskData, JiraAttribute.ISSUE_KEY));
		issue.setSummary(getAttributeValue(taskData, JiraAttribute.SUMMARY));
		issue.setDescription(getAttributeValue(taskData, JiraAttribute.DESCRIPTION));

		// TODO sync due date between jira and local planning
		issue.setDue(JiraUtil.stringToDate(getAttributeValue(taskData, JiraAttribute.DUE_DATE)));

		String parentId = getAttributeValue(taskData, JiraAttribute.PARENT_ID);
		if (parentId != null) {
			issue.setParentId(parentId);
		}

		String securityLevelId = getAttributeValue(taskData, JiraAttribute.SECURITY_LEVEL);
		if (securityLevelId != null) {
			issue.setSecurityLevel(new SecurityLevel(securityLevelId));
		}

		String estimate = getAttributeValue(taskData, JiraAttribute.ESTIMATE);
		if (estimate != null) {
			try {
				issue.setEstimate(Long.parseLong(estimate));
			} catch (NumberFormatException e) {
			}
		}

		estimate = getAttributeValue(taskData, JiraAttribute.INITIAL_ESTIMATE);
		if (estimate != null) {
			try {
				issue.setInitialEstimate(Long.parseLong(estimate));
			} catch (NumberFormatException e) {
			}
		}

		issue.setProject(new Project(getAttributeValue(taskData, JiraAttribute.PROJECT)));

		boolean subTaskType = hasSubTaskType(getAttribute(taskData, JiraAttribute.TYPE));
		IssueType issueType = new IssueType(getAttributeValue(taskData, JiraAttribute.TYPE), subTaskType);
		issue.setType(issueType);

		issue.setStatus(new JiraStatus(getAttributeValue(taskData, JiraAttribute.STATUS)));

		TaskAttribute componentsAttribute = taskData.getRoot().getMappedAttribute(IJiraConstants.ATTRIBUTE_COMPONENTS);
		if (componentsAttribute != null) {
			ArrayList<Component> components = new ArrayList<Component>();
			for (String value : componentsAttribute.getValues()) {
				components.add(new Component(value));
			}
			issue.setComponents(components.toArray(new Component[components.size()]));
		}

		TaskAttribute fixVersionAttr = taskData.getRoot().getMappedAttribute(IJiraConstants.ATTRIBUTE_FIXVERSIONS);
		if (fixVersionAttr != null) {
			ArrayList<Version> fixVersions = new ArrayList<Version>();
			for (String value : fixVersionAttr.getValues()) {
				fixVersions.add(new Version(value));
			}
			issue.setFixVersions(fixVersions.toArray(new Version[fixVersions.size()]));
		}

		TaskAttribute affectsVersionAttr = taskData.getRoot().getMappedAttribute(
				IJiraConstants.ATTRIBUTE_AFFECTSVERSIONS);
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
		String priorityId = getAttributeValue(taskData, JiraAttribute.PRIORITY);
		if (priorityId != null) {
			issue.setPriority(new Priority(priorityId));
		}

		ArrayList<CustomField> customFields = new ArrayList<CustomField>();
		for (TaskAttribute attribute : taskData.getRoot().getAttributes().values()) {
			if (attribute.getId().startsWith(IJiraConstants.ATTRIBUTE_CUSTOM_PREFIX)) {
				String id = attribute.getId().substring(IJiraConstants.ATTRIBUTE_CUSTOM_PREFIX.length());
				String type = attribute.getMetaData().getValue(IJiraConstants.META_TYPE);
				CustomField field = new CustomField(id, type, "", attribute.getValues());
				customFields.add(field);
			}
		}
		issue.setCustomFields(customFields.toArray(new CustomField[customFields.size()]));

		String resolutionId = getAttributeValue(taskData, JiraAttribute.RESOLUTION);
		if (resolutionId != null) {
			issue.setResolution(new Resolution(resolutionId));
		}

		return issue;
	}

	public static boolean hasSubTaskType(TaskAttribute typeAttribute) {
		return Boolean.parseBoolean(typeAttribute.getMetaData().getValue(IJiraConstants.META_SUB_TASK_TYPE));
	}

	private TaskAttribute getAttribute(TaskData taskData, JiraAttribute key) {
		return taskData.getRoot().getAttribute(key.id());
	}

	private String getAttributeValue(TaskData taskData, JiraAttribute key) {
		TaskAttribute attribute = taskData.getRoot().getAttribute(key.id());
		return (attribute != null) ? attribute.getValue() : null;
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
		String taskDataVersion = taskData.getVersion();
		JiraVersion version = new JiraVersion(taskDataVersion != null ? taskDataVersion : "0.0");
		// 1.0: the value was stored in the attribute rather than the key
		if (version.isSmallerOrEquals(TASK_DATA_VERSION_1_0)) {
			for (TaskAttribute attribute : taskData.getRoot().getAttributes().values()) {
				if (TaskAttribute.PRODUCT.equals(attribute.getId())) {
					String projectName = attribute.getValue();
					Map<String, String> options = taskData.getAttributeMapper().getOptions(attribute);
					for (String key : options.keySet()) {
						String value = options.get(key);
						if (projectName.equals(value)) {
							attribute.setValue(key);
						}
						attribute.putOption(key, value);
					}
				} else {
					JiraFieldType type = JiraFieldType.fromKey(attribute.getMetaData().getValue(
							IJiraConstants.META_TYPE));
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
			}
		}
		// 2.0: the type was not always set
		if (version.isSmallerOrEquals(TASK_DATA_VERSION_2_0)) {
			Collection<TaskAttribute> attributes = new ArrayList<TaskAttribute>(taskData.getRoot()
					.getAttributes()
					.values());
			for (TaskAttribute attribute : attributes) {
				if (attribute.getId().startsWith(TaskAttribute.PREFIX_OPERATION)) {
					if (attribute.getValue().equals(REASSIGN_OPERATION)) {
						taskData.getRoot().removeAttribute(attribute.getId());
						continue;
					} else {
						TaskAttribute associatedAttribute = taskData.getAttributeMapper().getAssoctiatedAttribute(
								attribute);
						if (associatedAttribute != null && associatedAttribute.getId().equals("resolution")) {
							TaskAttribute resolutionAttribute = taskData.getRoot().getAttribute(
									JiraAttribute.RESOLUTION.id());
							if (resolutionAttribute != null) {
								Map<String, String> options = associatedAttribute.getOptions();
								for (String key : options.keySet()) {
									resolutionAttribute.putOption(key, options.get(key));
								}
								resolutionAttribute.getMetaData().setType(TaskAttribute.TYPE_SINGLE_SELECT);
								resolutionAttribute.getMetaData().setReadOnly(false);
							}
							attribute.getMetaData().putValue(TaskAttribute.META_ASSOCIATED_ATTRIBUTE_ID,
									JiraAttribute.RESOLUTION.id());
							attribute.removeAttribute(associatedAttribute.getId());
						}
					}
				} else if (attribute.getId().equals(JiraAttribute.TYPE.id())) {
					if (attribute.getOptions().isEmpty()) {
						// sub task
						JiraClient client = clientFactory.getJiraClient(taskRepository);
						IssueType[] jiraIssueTypes = client.getCache().getIssueTypes();
						for (IssueType type : jiraIssueTypes) {
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
		if (version.isSmallerOrEquals(TASK_DATA_VERSION_2_2)) {
			for (TaskAttribute attribute : taskData.getRoot().getAttributes().values()) {
				JiraTimeFormat format = new JiraTimeFormat();
				if (isTimeSpanAttribute(attribute)) {
					String value = attribute.getValue();
					if (value.length() > 0) {
						try {
							Long.parseLong(value);
						} catch (NumberFormatException e) {
							attribute.setValue(String.valueOf(format.parse(value)));
						}
					}
				}
			}
		}
		if (version.isSmallerOrEquals(TASK_DATA_VERSION_CURRENT)) {
			taskData.setVersion(TASK_DATA_VERSION_CURRENT.toString());
		}
	}

	public static boolean isTimeSpanAttribute(TaskAttribute attribute) {
		return JiraAttribute.INITIAL_ESTIMATE.id().equals(attribute.getId())
				|| JiraAttribute.ESTIMATE.id().equals(attribute.getId())
				|| JiraAttribute.ACTUAL.id().equals(attribute.getId());
	}

	private String getType(TaskAttribute taskAttribute) {
		if (JiraAttribute.DESCRIPTION.id().equals(taskAttribute.getId())) {
			return TaskAttribute.TYPE_LONG_RICH_TEXT;
		}
		if (JiraAttribute.COMMENT_NEW.id().equals(taskAttribute.getId())) {
			return TaskAttribute.TYPE_LONG_RICH_TEXT;
		}
		if (JiraAttribute.SUMMARY.id().equals(taskAttribute.getId())) {
			return TaskAttribute.TYPE_SHORT_RICH_TEXT;
		}
		if (TaskAttribute.OPERATION.equals(taskAttribute.getId())
				|| taskAttribute.getId().startsWith(TaskAttribute.PREFIX_OPERATION)) {
			return TaskAttribute.TYPE_OPERATION;
		}
		if (taskAttribute.getId().startsWith(TaskAttribute.PREFIX_COMMENT)) {
			return TaskAttribute.TYPE_COMMENT;
		}
		if (taskAttribute.getId().startsWith(TaskAttribute.PREFIX_ATTACHMENT)) {
			return TaskAttribute.TYPE_ATTACHMENT;
		}
		JiraFieldType fieldType = null;
		if (JiraAttribute.CREATION_DATE.id().equals(taskAttribute.getId())
				|| JiraAttribute.DUE_DATE.id().equals(taskAttribute.getId())
				|| JiraAttribute.MODIFICATION_DATE.id().equals(taskAttribute.getId())) {
			fieldType = JiraFieldType.DATE;
			taskAttribute.getMetaData().putValue(IJiraConstants.META_TYPE, fieldType.getKey());
		}
		if (fieldType == null) {
			fieldType = JiraFieldType.fromKey(taskAttribute.getMetaData().getValue(IJiraConstants.META_TYPE));
		}
		if (fieldType.getTaskType() != null) {
			return fieldType.getTaskType();
		}
		fieldType = JiraAttribute.valueById(taskAttribute.getId()).getType();
		if (fieldType.getTaskType() != null) {
			return fieldType.getTaskType();
		}
		String existingType = taskAttribute.getMetaData().getType();
		if (existingType != null) {
			return existingType;
		}
		return TaskAttribute.TYPE_SHORT_TEXT;
	}

	private String mapCommonAttributeKey(String key) {
		if ("summary".equals(key)) {
			return JiraAttribute.SUMMARY.id();
		} else if ("description".equals(key)) {
			return JiraAttribute.DESCRIPTION.id();
		} else if ("priority".equals(key)) {
			return JiraAttribute.PRIORITY.id();
		} else if ("resolution".equals(key)) {
			return JiraAttribute.RESOLUTION.id();
		} else if ("assignee".equals(key)) {
			return JiraAttribute.USER_ASSIGNED.id();
		} else if ("environment".equals(key)) {
			return JiraAttribute.ENVIRONMENT.id();
		} else if ("issuetype".equals(key)) {
			return JiraAttribute.TYPE.id();
		} else if ("components".equals(key)) {
			return JiraAttribute.COMPONENTS.id();
		} else if ("versions".equals(key)) {
			return JiraAttribute.AFFECTSVERSIONS.id();
		} else if ("fixVersions".equals(key)) {
			return JiraAttribute.FIXVERSIONS.id();
		} else if ("timetracking".equals(key)) {
			return JiraAttribute.ESTIMATE.id();
		} else if ("duedate".equals(key)) {
			return JiraAttribute.DUE_DATE.id();
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