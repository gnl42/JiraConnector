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
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.AbstractTaskDataHandler2;
import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.ITaskDataState;
import org.eclipse.mylyn.tasks.core.data.RepositoryPerson;
import org.eclipse.mylyn.tasks.core.data.TaskAttachment;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeProperties;
import org.eclipse.mylyn.tasks.core.data.TaskComment;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskOperation;
import org.eclipse.mylyn.web.core.Policy;

/**
 * @author Mik Kersten
 * @author Rob Elves
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 * @since 3.0
 */
public class JiraTaskDataHandler2 extends AbstractTaskDataHandler2 {

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

	public JiraTaskDataHandler2(IJiraClientFactory clientFactory) {
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
//			AbstractTask task = TasksUiPlugin.getTaskListManager().getTaskList().getTask(repositoryUrl, "" + id);
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
		createAttribute(data, TaskAttribute.DATE_CREATION);
		createAttribute(data, TaskAttribute.SUMMARY);
		createAttribute(data, TaskAttribute.DESCRIPTION);
		createAttribute(data, TaskAttribute.STATUS);
		createAttribute(data, TaskAttribute.TASK_KEY);
		createAttribute(data, TaskAttribute.USER_ASSIGNED);
		createAttribute(data, TaskAttribute.USER_REPORTER);
		createAttribute(data, TaskAttribute.DATE_MODIFIED);

		createAttribute(data, TaskAttribute.PRODUCT);

		TaskAttribute resolutions = createAttribute(data, TaskAttribute.RESOLUTION);
		Resolution[] jiraResolutions = client.getCache().getResolutions();
		if (jiraResolutions.length > 0) {
			resolutions.setValue(jiraResolutions[0].getId());
			for (Resolution resolution : jiraResolutions) {
				resolutions.putOption(resolution.getId(), resolution.getName());
				if (Resolution.FIXED_ID.equals(resolution.getId())) {
					// set fixed as default
					resolutions.setValue(resolution.getId());
				}
			}
		} else {
			resolutions.putOption(Resolution.FIXED_ID, "Fixed");
			resolutions.putOption(Resolution.WONT_FIX_ID, "Won't Fix");
			resolutions.putOption(Resolution.DUPLICATE_ID, "Duplicate");
			resolutions.putOption(Resolution.INCOMPLETE_ID, "Incomplete");
			resolutions.putOption(Resolution.CANNOT_REPRODUCE_ID, "Cannot Reproduce");
			resolutions.setValue(Resolution.FIXED_ID);
		}

		TaskAttribute priorities = createAttribute(data, TaskAttribute.PRIORITY);
		priorities.putMetaDataValue(JiraAttributeFactory.TYPE_KEY, JiraFieldType.SELECT.getKey());
		Priority[] jiraPriorities = client.getCache().getPriorities();
		for (int i = 0; i < jiraPriorities.length; i++) {
			Priority priority = jiraPriorities[i];
			priorities.putOption(priority.getId(), priority.getName());
			if (i == (jiraPriorities.length / 2)) {
				priorities.setValue(priority.getName());
			}
		}

		TaskAttribute types = createAttribute(data, JiraAttributeFactory.ATTRIBUTE_TYPE);
		types.putMetaDataValue(JiraAttributeFactory.TYPE_KEY, JiraFieldType.SELECT.getKey());
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

		createAttribute(data, JiraAttributeFactory.ATTRIBUTE_ISSUE_PARENT_KEY);
		createAttribute(data, JiraAttributeFactory.ATTRIBUTE_ISSUE_PARENT_ID);

		createAttribute(data, JiraAttributeFactory.ATTRIBUTE_DUE_DATE);
		createAttribute(data, JiraAttributeFactory.ATTRIBUTE_ESTIMATE);
		if (!data.isNew()) {
			createAttribute(data, JiraAttributeFactory.ATTRIBUTE_ACTUAL);
			createAttribute(data, JiraAttributeFactory.ATTRIBUTE_INITIAL_ESTIMATE);
		}

		TaskAttribute affectsVersions = createAttribute(data, JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS);
		affectsVersions.putMetaDataValue(JiraAttributeFactory.TYPE_KEY, JiraFieldType.MULTISELECT.getKey());
		for (Version version : project.getVersions()) {
			affectsVersions.putOption(version.getId(), version.getName());
		}

		TaskAttribute components = createAttribute(data, JiraAttributeFactory.ATTRIBUTE_COMPONENTS);
		components.putMetaDataValue(JiraAttributeFactory.TYPE_KEY, JiraFieldType.SELECT.getKey());
		for (Component component : project.getComponents()) {
			components.putOption(component.getId(), component.getName());
		}

		TaskAttribute fixVersions = createAttribute(data, JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS);
		fixVersions.putMetaDataValue(JiraAttributeFactory.TYPE_KEY, JiraFieldType.MULTISELECT.getKey());
		for (Version version : project.getVersions()) {
			fixVersions.putOption(version.getId(), version.getName());
		}

		TaskAttribute environment = createAttribute(data, JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT);
		environment.putMetaDataValue(JiraAttributeFactory.TYPE_KEY, JiraFieldType.TEXTAREA.getKey());

		TaskAttribute newComment = createAttribute(data, TaskAttribute.COMMENT_NEW);
		newComment.putMetaDataValue(JiraAttributeFactory.TYPE_KEY, JiraFieldType.TEXTAREA.getKey());
	}

	private TaskAttribute createAttribute(TaskData data, String key) {
		return data.getRoot().createAttribute(key);
	}

	private void updateTaskData(TaskData data, JiraIssue jiraIssue, JiraClient client, TaskData oldTaskData,
			IProgressMonitor monitor) throws JiraException {
		String parentKey = jiraIssue.getParentKey();
		if (parentKey != null) {
			setAttributeValue(data, JiraAttributeFactory.ATTRIBUTE_ISSUE_PARENT_KEY, parentKey);
		} else {
			removeAttribute(data, JiraAttributeFactory.ATTRIBUTE_ISSUE_PARENT_KEY);
		}

		String parentId = jiraIssue.getParentId();
		if (parentId != null) {
			setAttributeValue(data, JiraAttributeFactory.ATTRIBUTE_ISSUE_PARENT_ID, parentId);
		} else {
			removeAttribute(data, JiraAttributeFactory.ATTRIBUTE_ISSUE_PARENT_ID);
		}

		removeAttribute(data, JiraAttributeFactory.ATTRIBUTE_SUBTASK_IDS);
		removeAttribute(data, JiraAttributeFactory.ATTRIBUTE_SUBTASK_KEYS);
		Subtask[] subtasks = jiraIssue.getSubtasks();
		if (subtasks != null && subtasks.length > 0) {
			for (Subtask subtask : subtasks) {
				addAttributeValue(data, JiraAttributeFactory.ATTRIBUTE_SUBTASK_IDS, subtask.getIssueId());
				addAttributeValue(data, JiraAttributeFactory.ATTRIBUTE_SUBTASK_KEYS, subtask.getIssueKey());
			}
		}

		IssueLink[] issueLinks = jiraIssue.getIssueLinks();
		removeAttributes(data, JiraAttributeFactory.ATTRIBUTE_LINK_PREFIX);
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
					attribute = createAttribute(data, JiraAttributeFactory.ATTRIBUTE_LINK_PREFIX + key);
					TaskAttributeProperties.defaults()
							.setKind(TaskAttribute.META_KIND_DEFAULT)
							.setLabel(label)
							.applyTo(attribute);
					attribute.putMetaDataValue(JiraAttributeFactory.TYPE_KEY, JiraFieldType.ISSUELINKS.getKey());
					links.put(key, attribute);
				}
				attribute.addValue(link.getIssueKey());

				if (link.getInwardDescription() != null) {
					addAttributeValue(data, JiraAttributeFactory.LINKED_IDS, link.getIssueId());
				}
			}
		}

		setAttributeValue(data, TaskAttribute.DATE_CREATION, JiraUtil.dateToString(jiraIssue.getCreated()));
		setAttributeValue(data, TaskAttribute.SUMMARY, jiraIssue.getSummary());
		setAttributeValue(data, TaskAttribute.DESCRIPTION, jiraIssue.getDescription());
		setAttributeValue(data, TaskAttribute.STATUS, jiraIssue.getStatus().getName());
		setAttributeValue(data, TaskAttribute.TASK_KEY, jiraIssue.getKey());
		setAttributeValue(data, TaskAttribute.RESOLUTION, //
				jiraIssue.getResolution() == null ? "" : jiraIssue.getResolution().getName());
		setAttributeValue(data, TaskAttribute.DATE_MODIFIED, JiraUtil.dateToString(jiraIssue.getUpdated()));

		setAttributeValue(data, TaskAttribute.USER_ASSIGNED, getAssignee(jiraIssue));
		setAttributeValue(data, TaskAttribute.USER_REPORTER, jiraIssue.getReporter());

		setAttributeValue(data, TaskAttribute.PRODUCT, jiraIssue.getProject().getName());

		if (jiraIssue.getPriority() != null) {
			setAttributeValue(data, TaskAttribute.PRIORITY, jiraIssue.getPriority().getName());
		} else {
			removeAttribute(data, TaskAttribute.PRIORITY);
		}

		SecurityLevel securityLevel = jiraIssue.getSecurityLevel();
		if (securityLevel != null) {
			TaskAttribute attribute = createAttribute(data, JiraAttributeFactory.ATTRIBUTE_SECURITY_LEVEL);
			attribute.putOption(securityLevel.getId(), securityLevel.getName());
			attribute.setValue(securityLevel.getName());
		}

		IssueType issueType = jiraIssue.getType();
		if (issueType != null) {
			setAttributeValue(data, JiraAttributeFactory.ATTRIBUTE_TYPE, issueType.getName());
			if (issueType.isSubTaskType()) {
				TaskAttribute attribute = data.getRoot().getAttribute(JiraAttributeFactory.ATTRIBUTE_TYPE);
				setReadOnly(attribute, true);
				attribute.clearOptions();
			}
		} else {
			removeAttribute(data, JiraAttributeFactory.ATTRIBUTE_TYPE);
		}

		JiraTimeFormat timeFormat = new JiraTimeFormat();
		if (jiraIssue.getActual() > 0) {
			setAttributeValue(data, JiraAttributeFactory.ATTRIBUTE_INITIAL_ESTIMATE,
					timeFormat.format(jiraIssue.getInitialEstimate()));
		} else {
			removeAttribute(data, JiraAttributeFactory.ATTRIBUTE_INITIAL_ESTIMATE);
		}
		setAttributeValue(data, JiraAttributeFactory.ATTRIBUTE_ESTIMATE, timeFormat.format(jiraIssue.getEstimate()));
		setAttributeValue(data, JiraAttributeFactory.ATTRIBUTE_ACTUAL, timeFormat.format(jiraIssue.getActual()));

		if (jiraIssue.getDue() != null) {
			setAttributeValue(data, JiraAttributeFactory.ATTRIBUTE_DUE_DATE, JiraUtil.dateToString(jiraIssue.getDue()));
		} else {
			removeAttribute(data, JiraAttributeFactory.ATTRIBUTE_DUE_DATE);
		}

		removeAttributeValues(data, JiraAttributeFactory.ATTRIBUTE_COMPONENTS);
		if (jiraIssue.getComponents() != null) {
			for (Component component : jiraIssue.getComponents()) {
				addAttributeValue(data, JiraAttributeFactory.ATTRIBUTE_COMPONENTS, component.getName());
			}
		}

		removeAttributeValues(data, JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS);
		if (jiraIssue.getReportedVersions() != null) {
			for (Version version : jiraIssue.getReportedVersions()) {
				addAttributeValue(data, JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS, version.getName());
			}
		}

		removeAttributeValues(data, JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS);
		if (jiraIssue.getFixVersions() != null) {
			for (Version version : jiraIssue.getFixVersions()) {
				addAttributeValue(data, JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS, version.getName());
			}
		}

		if (jiraIssue.getEnvironment() != null) {
			setAttributeValue(data, JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT, jiraIssue.getEnvironment());
		} else {
			removeAttribute(data, JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT);
		}

		addComments(data, jiraIssue);
		addAttachments(data, jiraIssue, client);
		addCustomFields(data, jiraIssue);

		updateMarkup(data, jiraIssue, client, oldTaskData, monitor);

		HashSet<String> editableKeys = getEditableKeys(data, jiraIssue, client, oldTaskData, monitor);
		updateProperties(data, editableKeys);
	}

	private void addComments(TaskData data, JiraIssue jiraIssue) {
		TaskAttribute commentContainer = createAttribute(data, TaskAttribute.CONTAINER_COMMENTS);
		int x = 1;
		for (Comment comment : jiraIssue.getComments()) {
			TaskAttribute attribute = commentContainer.createAttribute(x++ + "");
			TaskComment taskComment = TaskComment.createFrom(attribute);
			taskComment.setAuthor(new RepositoryPerson(data.getConnectorKind(), data.getRepositoryUrl(),
					comment.getAuthor()));
			String commentText = comment.getComment();
			if (comment.isMarkupDetected()) {
				commentText = stripTags(commentText);
			}
			taskComment.setText(commentText);
			taskComment.setCreationDate(comment.getCreated());
			// TODO taskComment.setUrl()
			taskComment.applyTo(attribute);
		}
	}

	private void addAttachments(TaskData data, JiraIssue jiraIssue, JiraClient client) {
		TaskAttribute attachmentContainer = createAttribute(data, TaskAttribute.CONTAINER_ATTACHMENTS);
		for (Attachment attachment : jiraIssue.getAttachments()) {
			TaskAttribute attribute = attachmentContainer.createAttribute(attachment.getId());
			TaskAttachment taskAttachment = TaskAttachment.createFrom(attribute);
			taskAttachment.setAuthor(new RepositoryPerson(data.getConnectorKind(), data.getRepositoryUrl(),
					attachment.getAuthor()));
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
		}
	}

	private void addCustomFields(TaskData data, JiraIssue jiraIssue) {
		removeAttributes(data, JiraAttributeFactory.ATTRIBUTE_CUSTOM_PREFIX);
		for (CustomField field : jiraIssue.getCustomFields()) {
			String mappedKey = mapCommonAttributeKey(field.getId());
			String name = field.getName().replace("&", "&&") + ":"; // mask & from SWT
			String kind = (JiraAttribute.valueById(mappedKey).isHidden()) ? null : TaskAttribute.META_KIND_DEFAULT;
			String type = field.getKey();

			TaskAttribute attribute = createAttribute(data, mappedKey);
			TaskAttributeProperties.defaults().setKind(kind).setLabel(name).setReadOnly(field.isReadOnly()).applyTo(
					attribute);
			attribute.putMetaDataValue(JiraAttributeFactory.TYPE_KEY, type);
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
					if (!TaskAttributeProperties.createFrom(attribute).isReadOnly()) {
						editableKeys.add(attribute.getId());
					}
				}

				TaskAttribute attribute = oldTaskData.getRoot().getAttribute(JiraAttributeFactory.ATTRIBUTE_READ_ONLY);
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
					data.getRoot().createAttribute(JiraAttributeFactory.ATTRIBUTE_READ_ONLY);
				}
			}
		}
		return editableKeys;
	}

	private void updateProperties(TaskData data, HashSet<String> editableKeys) {
		for (TaskAttribute attribute : data.getRoot().getAttributes().values()) {
			TaskAttributeProperties properties = TaskAttributeProperties.createFrom(attribute);
			boolean editable = editableKeys.contains(attribute.getId().toLowerCase());
			if (editable && (attribute.getId().startsWith(JiraAttributeFactory.ATTRIBUTE_CUSTOM_PREFIX) //
					|| !JiraAttribute.valueById(attribute.getId()).isHidden())) {
				properties.setKind(TaskAttribute.META_KIND_DEFAULT);
			}

			if (TaskAttribute.COMMENT_NEW.equals(attribute.getId())) {
				properties.setReadOnly(false);
			} else {
				// make attributes read-only if can't find editing options
				String key = attribute.getMetaData(JiraAttributeFactory.TYPE_KEY);
				Map<String, String> options = attribute.getOptions();
				if (JiraFieldType.SELECT.getKey().equals(key) && (options.isEmpty() || properties.isReadOnly())) {
					properties.setReadOnly(true);
				} else if (JiraFieldType.MULTISELECT.getKey().equals(key) && options.isEmpty()) {
					properties.setReadOnly(true);
				} else {
					properties.setReadOnly(false);
				}
			}
			properties.applyTo(attribute);
		}
	}

	private void addAttributeValue(TaskData data, String attributeId, String value) {
		data.getRoot().getAttribute(attributeId).addValue(value);
	}

	private void setAttributeValue(TaskData data, String attributeId, String value) {
		data.getRoot().getAttribute(attributeId).setValue(value);
	}

	private boolean useCachedInformation(JiraIssue issue, TaskData oldTaskData) {
		TaskAttribute attribute = oldTaskData.getMappedAttribute(TaskAttribute.STATUS);
		String status = (attribute != null) ? attribute.getValue() : "";
		return oldTaskData != null && status.equals(issue.getStatus().getName());
	}

	private void removeAttributes(TaskData data, String keyPrefix) {
		List<TaskAttribute> attributes = new ArrayList<TaskAttribute>(data.getRoot().getAttributes().values());
		for (TaskAttribute attribute : attributes) {
			if (attribute.getId().startsWith(keyPrefix)) {
				removeAttribute(data, attribute.getId());
			}
		}
	}

	private void removeAttribute(TaskData data, String attributeId) {
		data.getRoot().removeAttribute(attributeId);
	}

	/**
	 * Removes attribute values without removing attribute to preserve order of attributes
	 */
	private void removeAttributeValues(TaskData data, String attributeId) {
		data.getRoot().getAttribute(attributeId).clearValues();
	}

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
		return assignee == null || JiraTask.UNASSIGNED_USER.equals(assignee) ? "" : assignee;
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
			String value = getAttributeValue(oldTaskData, TaskAttribute.DATE_MODIFIED);
			if (jiraIssue.getUpdated().equals(JiraUtil.stringToDate(value))) {
				// use cached information
				if (data.getRoot().getAttribute(TaskAttribute.DESCRIPTION) != null) {
					setAttributeValue(data, TaskAttribute.DESCRIPTION, getAttributeValue(oldTaskData,
							TaskAttribute.DESCRIPTION));
				}
				if (data.getRoot().getAttribute(JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT) != null) {
					setAttributeValue(data, JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT, getAttributeValue(oldTaskData,
							JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT));
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
				setAttributeValue(data, TaskAttribute.DESCRIPTION, "");
			} else {
				setAttributeValue(data, TaskAttribute.DESCRIPTION, remoteIssue.getDescription());
			}
		}
		if (data.getRoot().getAttribute(JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT) != null) {
			if (remoteIssue.getEnvironment() == null) {
				setAttributeValue(data, JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT, "");
			} else {
				setAttributeValue(data, JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT, remoteIssue.getEnvironment());
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
		TaskAttribute operationContainer = createAttribute(data, TaskAttribute.CONTAINER_OPERATIONS);

		// avoid server round-trips
		if (useCachedInformation(issue, oldTaskData)) {
			TaskAttribute oldOperationContainer = oldTaskData.getMappedAttribute(TaskAttribute.CONTAINER_OPERATIONS);
			if (oldOperationContainer != null) {
				// FIXME associated attributes?
				operationContainer.deepCopyFrom(oldOperationContainer);
			}
			return;
		}

		TaskAttribute attribute = operationContainer.createAttribute(LEAVE_OPERATION);
		TaskOperation operation = TaskOperation.createFrom(attribute);
		operation.setLabel("Leave as " + issue.getStatus().getName());
		operation.applyTo(attribute);

		// set as default
		operationContainer.setValue(LEAVE_OPERATION);

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
				attribute = operationContainer.createAttribute(action.getId());
				operation = TaskOperation.createFrom(attribute);
				operation.setLabel(action.getName());
				operation.applyTo(attribute);

				String[] fields = client.getActionFields(issue.getKey(), action.getName(), monitor);
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
	public String postTaskData(TaskRepository repository, ITaskDataState taskDataState, IProgressMonitor monitor)
			throws CoreException {
		JiraClient client = clientFactory.getJiraClient(repository);
		if (client == null) {
			throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN,
					IStatus.ERROR, "Unable to create Jira client", null));
		}

		try {
			if (!client.getCache().hasDetails()) {
				client.getCache().refreshDetails(new NullProgressMonitor());
			}

			TaskData taskData = taskDataState.getLocalData();
			JiraIssue issue = buildJiraIssue(taskData, client);
			if (taskData.isNew()) {
				if (issue.getType().isSubTaskType() && issue.getParentId() != null) {
					issue = client.createSubTask(issue, monitor);
				} else {
					issue = client.createIssue(issue, monitor);
				}

				if (issue == null) {
					throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR,
							JiraCorePlugin.ID_PLUGIN, IStatus.OK, "Could not create ticket.", null));
				}
				// this is severely broken: should return id instead
				return issue.getKey();
			} else {
				TaskAttribute attribute = taskData.getMappedAttribute(TaskAttribute.CONTAINER_OPERATIONS);
				if (attribute == null) {
					throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR,
							JiraCorePlugin.ID_PLUGIN, IStatus.OK, "No valid action selected", null));
				}

				String operationId = taskData.getAttributeMapper().getValue(attribute);
				if (operationId.length() == 0) {
					operationId = LEAVE_OPERATION;
				}

				String newComment = "";
				attribute = taskData.getMappedAttribute(TaskAttribute.COMMENT_NEW);
				if (attribute != null) {
					newComment = taskData.getAttributeMapper().getValue(attribute);
				}

				if (LEAVE_OPERATION.equals(operationId) || REASSIGN_OPERATION.equals(operationId)) {
					if (!JiraRepositoryConnector.isClosed(issue)
							&& taskData.getMappedAttribute(JiraAttributeFactory.ATTRIBUTE_READ_ONLY) == null) {
						client.updateIssue(issue, newComment, monitor);
					} else if (newComment.length() > 0) {
						client.addCommentToIssue(issue, newComment, monitor);
					}
				} else {
					client.advanceIssueWorkflow(issue, operationId, newComment, monitor);
				}

				return "";
			}
		} catch (JiraException e) {
			IStatus status = JiraCorePlugin.toStatus(repository, e);
			trace(status);
			throw new CoreException(status);
		}
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

		String projectName = projectAttribute.getValue();
		Project project = getProject(client, projectName);
		if (project == null) {
			project = client.getCache().getProjectByKey(projectName);
		}
		if (project == null) {
			return false;
		}

		initializeTaskData(data, client, project);

		setAttributeValue(data, TaskAttribute.PRODUCT, project.getName());

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

	private void setReadOnly(TaskAttribute attribute, boolean readOnly) {
		attribute.putMetaDataValue(TaskAttribute.META_READ_ONLY, Boolean.toString(readOnly));
	}

	@Override
	public boolean canInitializeSubTaskData(AbstractTask task, TaskData parentTaskData) {
		return true;
	}

	private Project getProject(JiraClient client, String projectName) {
		for (org.eclipse.mylyn.internal.jira.core.model.Project project : client.getCache().getProjects()) {
			if (project.getName().equals(projectName)) {
				return project;
			}
		}
		return null;
	}

	private JiraIssue buildJiraIssue(TaskData taskData, JiraClient client) {
		JiraIssue issue = new JiraIssue();
		issue.setId(taskData.getTaskId());
		issue.setKey(getAttributeValue(taskData, TaskAttribute.TASK_KEY));
		issue.setSummary(getAttributeValue(taskData, TaskAttribute.SUMMARY));
		issue.setDescription(getAttributeValue(taskData, TaskAttribute.DESCRIPTION));

		// TODO sync due date between jira and local planning
		issue.setDue(JiraUtil.stringToDate(getAttributeValue(taskData, JiraAttributeFactory.ATTRIBUTE_DUE_DATE)));

		String parentId = getAttributeValue(taskData, JiraAttributeFactory.ATTRIBUTE_ISSUE_PARENT_ID);
		if (parentId != null) {
			issue.setParentId(parentId);
		}

		TaskAttribute attribute = taskData.getMappedAttribute(JiraAttributeFactory.ATTRIBUTE_SECURITY_LEVEL);
		if (attribute != null) {
			SecurityLevel securityLevel = new SecurityLevel();
			securityLevel.setId(attribute.getValue());
			issue.setSecurityLevel(securityLevel);
		}

		String estimate = getAttributeValue(taskData, JiraAttributeFactory.ATTRIBUTE_ESTIMATE);
		if (estimate != null) {
			JiraTimeFormat timeFormat = new JiraTimeFormat();
			issue.setEstimate(timeFormat.parse(estimate));
		}

		estimate = getAttributeValue(taskData, JiraAttributeFactory.ATTRIBUTE_INITIAL_ESTIMATE);
		if (estimate != null) {
			JiraTimeFormat timeFormat = new JiraTimeFormat();
			issue.setInitialEstimate(timeFormat.parse(estimate));
		}

		Project project = getProject(client, getAttributeValue(taskData, TaskAttribute.PRODUCT));
		if (project != null) {
			issue.setProject(project);
		}
		issue.setType(new IssueType());
		for (IssueType type : client.getCache().getIssueTypes()) {
			if (type.getName().equals(getAttributeValue(taskData, JiraAttributeFactory.ATTRIBUTE_TYPE))) {
				issue.setType(type);
				break;
			}
		}
		for (org.eclipse.mylyn.internal.jira.core.model.JiraStatus status : client.getCache().getStatuses()) {
			if (status.getName().equals(getAttributeValue(taskData, TaskAttribute.STATUS))) {
				issue.setStatus(status);
				break;
			}
		}
		TaskAttribute componentsAttr = taskData.getMappedAttribute(JiraAttributeFactory.ATTRIBUTE_COMPONENTS);
		if (componentsAttr != null) {
			ArrayList<Component> components = new ArrayList<Component>();
			for (String value : componentsAttr.getValues()) {
				components.add(new Component(value));
			}
			issue.setComponents(components.toArray(new Component[components.size()]));
		}

		TaskAttribute fixVersionAttr = taskData.getMappedAttribute(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS);
		if (fixVersionAttr != null) {
			ArrayList<Version> fixVersions = new ArrayList<Version>();
			for (String value : fixVersionAttr.getValues()) {
				fixVersions.add(new Version(value));
			}
			issue.setFixVersions(fixVersions.toArray(new Version[fixVersions.size()]));
		}

		TaskAttribute affectsVersionAttr = taskData.getMappedAttribute(JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS);
		if (affectsVersionAttr != null) {
			ArrayList<Version> affectsVersions = new ArrayList<Version>();
			for (String value : affectsVersionAttr.getValues()) {
				affectsVersions.add(new Version(value));
			}
			issue.setReportedVersions(affectsVersions.toArray(new Version[affectsVersions.size()]));
		}

		issue.setReporter(getAttributeValue(taskData, TaskAttribute.USER_REPORTER));

		String assignee = getAttributeValue(taskData, TaskAttribute.USER_ASSIGNED);
		issue.setAssignee(JiraRepositoryConnector.getAssigneeFromAttribute(assignee));

		issue.setEnvironment(getAttributeValue(taskData, JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT));
		for (Priority priority : client.getCache().getPriorities()) {
			if (priority.getName().equals(getAttributeValue(taskData, TaskAttribute.PRIORITY))) {
				issue.setPriority(priority);
				break;
			}
		}

		ArrayList<CustomField> customFields = new ArrayList<CustomField>();
		for (TaskAttribute attr : taskData.getRoot().getAttributes().values()) {
			if (attr.getId().startsWith(JiraAttributeFactory.ATTRIBUTE_CUSTOM_PREFIX)) {
				String id = attr.getId().substring(JiraAttributeFactory.ATTRIBUTE_CUSTOM_PREFIX.length());
				CustomField field = new CustomField(id, attr.getMetaData(JiraAttributeFactory.TYPE_KEY), "",
						attr.getValues());
				customFields.add(field);
			}
		}
		issue.setCustomFields(customFields.toArray(new CustomField[customFields.size()]));

		return issue;
	}

	private String getAttributeValue(TaskData taskData, String attributeId) {
		TaskAttribute attribute = taskData.getRoot().getAttribute(attributeId);
		return (attribute != null) ? attribute.getValue() : null;
	}

	@Override
	public Set<String> getSubTaskIds(TaskData taskData) {
		Set<String> subIds = new HashSet<String>();

		TaskAttribute attribute = taskData.getRoot().getAttribute(JiraAttribute.SUBTASK_IDS.getId());
		if (attribute != null) {
			subIds.addAll(attribute.getValues());
		}

		attribute = taskData.getRoot().getAttribute(JiraAttribute.LINKED_IDS.getId());
		if (attribute != null) {
			subIds.addAll(attribute.getValues());
		}

		return subIds;
	}

	private static void trace(IStatus status) {
		if (TRACE_ENABLED) {
			JiraCorePlugin.getDefault().getLog().log(status);
		}
	}

	@Override
	public AbstractAttributeMapper getAttributeMapper(TaskRepository taskRepository) {
		return new JiraAttributeMapper(taskRepository);
	}

	@Override
	public void migrateTaskData(TaskRepository taskRepository, TaskData taskData) {
		if (TASK_DATA_VERSION_1_0.equals(taskData.getVersion())) {
			for (TaskAttribute attribute : taskData.getRoot().getAttributes().values()) {
				JiraFieldType type = JiraFieldType.valueByKey(attribute.getMetaData(JiraAttributeFactory.TYPE_KEY));
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
			return RepositoryTaskAttribute.SUMMARY;
		} else if ("description".equals(key)) {
			return RepositoryTaskAttribute.DESCRIPTION;
		} else if ("priority".equals(key)) {
			return RepositoryTaskAttribute.PRIORITY;
		} else if ("resolution".equals(key)) {
			return RepositoryTaskAttribute.RESOLUTION;
		} else if ("assignee".equals(key)) {
			return RepositoryTaskAttribute.USER_ASSIGNED;
		} else if ("environment".equals(key)) {
			return JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT;
		} else if ("issuetype".equals(key)) {
			return JiraAttributeFactory.ATTRIBUTE_TYPE;
		} else if ("components".equals(key)) {
			return JiraAttributeFactory.ATTRIBUTE_COMPONENTS;
		} else if ("versions".equals(key)) {
			return JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS;
		} else if ("fixVersions".equals(key)) {
			return JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS;
		} else if ("timetracking".equals(key)) {
			return JiraAttributeFactory.ATTRIBUTE_ESTIMATE;
		} else if ("duedate".equals(key)) {
			return JiraAttributeFactory.ATTRIBUTE_DUE_DATE;
		}

		if (key.startsWith("issueLink")) {
			return JiraAttributeFactory.ATTRIBUTE_LINK_PREFIX + key;
		}
		if (key.startsWith("customfield")) {
			return JiraAttributeFactory.ATTRIBUTE_CUSTOM_PREFIX + key;
		}

		return key;
	}

}