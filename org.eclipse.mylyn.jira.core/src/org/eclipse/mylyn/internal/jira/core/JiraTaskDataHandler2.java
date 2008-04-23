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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
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
import org.eclipse.mylyn.monitor.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.AbstractAttributeFactory;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.AbstractTaskDataHandler2;
import org.eclipse.mylyn.tasks.core.RepositoryAttachment;
import org.eclipse.mylyn.tasks.core.RepositoryOperation;
import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylyn.tasks.core.RepositoryTaskData;
import org.eclipse.mylyn.tasks.core.TaskComment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
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

	private static final JiraAttributeFactory attributeFactory = new JiraAttributeFactory();

	private static final String TASK_DATA_VERSION_1_0 = "1.0";

	private static final String TASK_DATA_VERSION_2_0 = "2.0";

	private final IJiraClientFactory clientFactory;

	public JiraTaskDataHandler2(IJiraClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	public RepositoryTaskData getTaskData(TaskRepository repository, String taskId, IProgressMonitor monitor)
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

	public RepositoryTaskData createTaskData(TaskRepository repository, JiraClient client, JiraIssue jiraIssue,
			RepositoryTaskData oldTaskData, IProgressMonitor monitor) throws JiraException {
		RepositoryTaskData data = new RepositoryTaskData(attributeFactory, JiraCorePlugin.CONNECTOR_KIND,
				repository.getRepositoryUrl(), jiraIssue.getId());
		initializeTaskData(data, client, jiraIssue.getProject());
		updateTaskData(data, jiraIssue, client, oldTaskData, monitor);
		addOperations(data, jiraIssue, client, oldTaskData, monitor);
		return data;
	}

	public void initializeTaskData(RepositoryTaskData data, JiraClient client, Project project) {
		data.removeAllAttributes();

		addAttribute(data, RepositoryTaskAttribute.DATE_CREATION);
		addAttribute(data, RepositoryTaskAttribute.SUMMARY);
		addAttribute(data, RepositoryTaskAttribute.DESCRIPTION);
		addAttribute(data, RepositoryTaskAttribute.STATUS);
		addAttribute(data, RepositoryTaskAttribute.TASK_KEY);
		addAttribute(data, RepositoryTaskAttribute.RESOLUTION);
		addAttribute(data, RepositoryTaskAttribute.USER_ASSIGNED);
		addAttribute(data, RepositoryTaskAttribute.USER_REPORTER);
		addAttribute(data, RepositoryTaskAttribute.DATE_MODIFIED);

		addAttribute(data, RepositoryTaskAttribute.PRODUCT);

		RepositoryTaskAttribute priorities = addAttribute(data, RepositoryTaskAttribute.PRIORITY);
		priorities.putMetaDataValue(JiraAttributeFactory.TYPE_KEY, JiraFieldType.SELECT.getKey());
		Priority[] jiraPriorities = client.getCache().getPriorities();
		for (int i = 0; i < jiraPriorities.length; i++) {
			Priority priority = jiraPriorities[i];
			priorities.addOption(priority.getName(), priority.getId());
			if (i == (jiraPriorities.length / 2)) {
				priorities.setValue(priority.getName());
			}
		}

		RepositoryTaskAttribute types = addAttribute(data, JiraAttributeFactory.ATTRIBUTE_TYPE);
		types.putMetaDataValue(JiraAttributeFactory.TYPE_KEY, JiraFieldType.SELECT.getKey());
		IssueType[] jiraIssueTypes = client.getCache().getIssueTypes();
		for (int i = 0; i < jiraIssueTypes.length; i++) {
			IssueType type = jiraIssueTypes[i];
			if (!type.isSubTaskType()) {
				types.addOption(type.getName(), type.getId());
				if (i == 0) {
					types.setValue(type.getName());
				}
			}
		}

		addAttribute(data, JiraAttributeFactory.ATTRIBUTE_ISSUE_PARENT_KEY);
		addAttribute(data, JiraAttributeFactory.ATTRIBUTE_ISSUE_PARENT_ID);

		addAttribute(data, JiraAttributeFactory.ATTRIBUTE_DUE_DATE);
		addAttribute(data, JiraAttributeFactory.ATTRIBUTE_ESTIMATE);
		if (!data.isNew()) {
			addAttribute(data, JiraAttributeFactory.ATTRIBUTE_ACTUAL);
			addAttribute(data, JiraAttributeFactory.ATTRIBUTE_INITIAL_ESTIMATE);
		}

		RepositoryTaskAttribute affectsVersions = addAttribute(data, JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS);
		affectsVersions.putMetaDataValue(JiraAttributeFactory.TYPE_KEY, JiraFieldType.MULTISELECT.getKey());
		for (Version version : project.getVersions()) {
			affectsVersions.addOption(version.getName(), version.getId());
		}

		RepositoryTaskAttribute components = addAttribute(data, JiraAttributeFactory.ATTRIBUTE_COMPONENTS);
		components.putMetaDataValue(JiraAttributeFactory.TYPE_KEY, JiraFieldType.SELECT.getKey());
		for (Component component : project.getComponents()) {
			components.addOption(component.getName(), component.getId());
		}

		RepositoryTaskAttribute fixVersions = addAttribute(data, JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS);
		fixVersions.putMetaDataValue(JiraAttributeFactory.TYPE_KEY, JiraFieldType.MULTISELECT.getKey());
		for (Version version : project.getVersions()) {
			fixVersions.addOption(version.getName(), version.getId());
		}

		RepositoryTaskAttribute environment = addAttribute(data, JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT);
		environment.putMetaDataValue(JiraAttributeFactory.TYPE_KEY, JiraFieldType.TEXTAREA.getKey());

		RepositoryTaskAttribute newComment = addAttribute(data, RepositoryTaskAttribute.COMMENT_NEW);
		newComment.putMetaDataValue(JiraAttributeFactory.TYPE_KEY, JiraFieldType.TEXTAREA.getKey());
	}

	private RepositoryTaskAttribute addAttribute(RepositoryTaskData data, String key) {
		data.addAttribute(key, attributeFactory.createAttribute(key));
		return data.getAttribute(key);
	}

	private void updateTaskData(RepositoryTaskData data, JiraIssue jiraIssue, JiraClient client,
			RepositoryTaskData oldTaskData, IProgressMonitor monitor) throws JiraException {
		String parentKey = jiraIssue.getParentKey();
		if (parentKey != null) {
			data.setAttributeValue(JiraAttributeFactory.ATTRIBUTE_ISSUE_PARENT_KEY, parentKey);
		} else {
			data.removeAttribute(JiraAttributeFactory.ATTRIBUTE_ISSUE_PARENT_KEY);
		}

		String parentId = jiraIssue.getParentId();
		if (parentId != null) {
			data.setAttributeValue(JiraAttributeFactory.ATTRIBUTE_ISSUE_PARENT_ID, parentId);
		} else {
			data.removeAttribute(JiraAttributeFactory.ATTRIBUTE_ISSUE_PARENT_ID);
		}

		data.removeAttribute(JiraAttributeFactory.ATTRIBUTE_SUBTASK_IDS);
		data.removeAttribute(JiraAttributeFactory.ATTRIBUTE_SUBTASK_KEYS);
		Subtask[] subtasks = jiraIssue.getSubtasks();
		if (subtasks != null && subtasks.length > 0) {
			for (Subtask subtask : subtasks) {
				data.addAttributeValue(JiraAttributeFactory.ATTRIBUTE_SUBTASK_IDS, subtask.getIssueId());
				data.addAttributeValue(JiraAttributeFactory.ATTRIBUTE_SUBTASK_KEYS, subtask.getIssueKey());
			}
		}

		IssueLink[] issueLinks = jiraIssue.getIssueLinks();
		removeAttributes(data, JiraAttributeFactory.ATTRIBUTE_LINK_PREFIX);
		if (issueLinks != null && issueLinks.length > 0) {
			HashMap<String, RepositoryTaskAttribute> links = new HashMap<String, RepositoryTaskAttribute>();
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
				RepositoryTaskAttribute attribute = links.get(key);
				if (attribute == null) {
					attribute = new RepositoryTaskAttribute(JiraAttributeFactory.ATTRIBUTE_LINK_PREFIX + key, label,
							false);
					attribute.setReadOnly(true);
					attribute.putMetaDataValue(JiraAttributeFactory.TYPE_KEY, JiraFieldType.ISSUELINKS.getKey());
					links.put(key, attribute);
				}
				attribute.addValue(link.getIssueKey());

				if (link.getInwardDescription() != null) {
					data.addAttributeValue(JiraAttributeFactory.LINKED_IDS, link.getIssueId());
				}
			}

			for (RepositoryTaskAttribute attribute : links.values()) {
				data.addAttribute(attribute.getId(), attribute);
			}
		}

		data.setAttributeValue(RepositoryTaskAttribute.DATE_CREATION, JiraUtil.dateToString(jiraIssue.getCreated()));
		data.setAttributeValue(RepositoryTaskAttribute.SUMMARY, jiraIssue.getSummary());
		data.setAttributeValue(RepositoryTaskAttribute.DESCRIPTION, jiraIssue.getDescription());
		data.setAttributeValue(RepositoryTaskAttribute.STATUS, jiraIssue.getStatus().getName());
		data.setAttributeValue(RepositoryTaskAttribute.TASK_KEY, jiraIssue.getKey());
		data.setAttributeValue(RepositoryTaskAttribute.RESOLUTION, //
				jiraIssue.getResolution() == null ? "" : jiraIssue.getResolution().getName());
		data.setAttributeValue(RepositoryTaskAttribute.DATE_MODIFIED, JiraUtil.dateToString(jiraIssue.getUpdated()));

		data.setAttributeValue(RepositoryTaskAttribute.USER_ASSIGNED, getAssignee(jiraIssue));
		data.setAttributeValue(RepositoryTaskAttribute.USER_REPORTER, jiraIssue.getReporter());

		data.setAttributeValue(RepositoryTaskAttribute.PRODUCT, jiraIssue.getProject().getName());

		if (jiraIssue.getPriority() != null) {
			data.setAttributeValue(RepositoryTaskAttribute.PRIORITY, jiraIssue.getPriority().getName());
		} else {
			data.removeAttribute(RepositoryTaskAttribute.PRIORITY);
		}

		SecurityLevel securityLevel = jiraIssue.getSecurityLevel();
		if (securityLevel != null) {
			RepositoryTaskAttribute attribute = addAttribute(data, JiraAttributeFactory.ATTRIBUTE_SECURITY_LEVEL);
			attribute.addOption(securityLevel.getName(), securityLevel.getId());
			attribute.setValue(securityLevel.getName());
		}

		IssueType issueType = jiraIssue.getType();
		if (issueType != null) {
			data.setAttributeValue(JiraAttributeFactory.ATTRIBUTE_TYPE, issueType.getName());
			if (issueType.isSubTaskType()) {
				RepositoryTaskAttribute attribute = data.getAttribute(JiraAttributeFactory.ATTRIBUTE_TYPE);
				attribute.setReadOnly(true);
				attribute.clearOptions();
			}
		} else {
			data.removeAttribute(JiraAttributeFactory.ATTRIBUTE_TYPE);
		}

		JiraTimeFormat timeFormat = new JiraTimeFormat();
		if (jiraIssue.getActual() > 0) {
			data.setAttributeValue(JiraAttributeFactory.ATTRIBUTE_INITIAL_ESTIMATE,
					timeFormat.format(jiraIssue.getInitialEstimate()));
		} else {
			data.removeAttribute(JiraAttributeFactory.ATTRIBUTE_INITIAL_ESTIMATE);
		}
		data.setAttributeValue(JiraAttributeFactory.ATTRIBUTE_ESTIMATE, timeFormat.format(jiraIssue.getEstimate()));
		data.setAttributeValue(JiraAttributeFactory.ATTRIBUTE_ACTUAL, timeFormat.format(jiraIssue.getActual()));

		if (jiraIssue.getDue() != null) {
			data.setAttributeValue(JiraAttributeFactory.ATTRIBUTE_DUE_DATE, JiraUtil.dateToString(jiraIssue.getDue()));
		} else {
			data.removeAttribute(JiraAttributeFactory.ATTRIBUTE_DUE_DATE);
		}

		removeAttributeValues(data, JiraAttributeFactory.ATTRIBUTE_COMPONENTS);
		if (jiraIssue.getComponents() != null) {
			for (Component component : jiraIssue.getComponents()) {
				data.addAttributeValue(JiraAttributeFactory.ATTRIBUTE_COMPONENTS, component.getName());
			}
		}

		removeAttributeValues(data, JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS);
		if (jiraIssue.getReportedVersions() != null) {
			for (Version version : jiraIssue.getReportedVersions()) {
				data.addAttributeValue(JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS, version.getName());
			}
		}

		removeAttributeValues(data, JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS);
		if (jiraIssue.getFixVersions() != null) {
			for (Version version : jiraIssue.getFixVersions()) {
				data.addAttributeValue(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS, version.getName());
			}
		}

		if (jiraIssue.getEnvironment() != null) {
			data.setAttributeValue(JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT, jiraIssue.getEnvironment());
		} else {
			data.removeAttribute(JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT);
		}

		int x = 1;
		for (Comment comment : jiraIssue.getComments()) {
			TaskComment taskComment = new TaskComment(attributeFactory, x++);

			// XXX ugly because AbstractTaskEditor is using USER_OWNER instead of COMMENT_AUTHOR
			taskComment.addAttribute(RepositoryTaskAttribute.COMMENT_AUTHOR, createAuthorAttribute(comment.getAuthor()));

			String commentText = comment.getComment();
			if (comment.isMarkupDetected()) {
				commentText = stripTags(commentText);
			}
			taskComment.addAttributeValue(RepositoryTaskAttribute.COMMENT_TEXT, commentText);
			taskComment.addAttributeValue(RepositoryTaskAttribute.COMMENT_DATE, formatDate(comment.getCreated()));
			data.addComment(taskComment);
		}

		for (Attachment attachment : jiraIssue.getAttachments()) {
			RepositoryAttachment taskAttachment = new RepositoryAttachment(attributeFactory);
			taskAttachment.setCreator(attachment.getAuthor());
			taskAttachment.setRepositoryKind(JiraCorePlugin.CONNECTOR_KIND);
			taskAttachment.setRepositoryUrl(client.getBaseUrl());
			taskAttachment.setTaskId(jiraIssue.getKey());

			taskAttachment.setAttributeValue(RepositoryTaskAttribute.ATTACHMENT_ID, attachment.getId());
			taskAttachment.setAttributeValue(RepositoryTaskAttribute.ATTACHMENT_FILENAME, attachment.getName());

			if (CONTEXT_ATTACHEMENT_FILENAME.equals(attachment.getName())) {
				taskAttachment.setAttributeValue(RepositoryTaskAttribute.DESCRIPTION, CONTEXT_ATTACHMENT_DESCRIPTION);
			} else if (CONTEXT_ATTACHEMENT_FILENAME_LEGACY.equals(attachment.getName())) {
				taskAttachment.setAttributeValue(RepositoryTaskAttribute.DESCRIPTION,
						CONTEXT_ATTACHMENT_DESCRIPTION_LEGACY);
			} else {
				taskAttachment.setAttributeValue(RepositoryTaskAttribute.DESCRIPTION, attachment.getName());
			}

			taskAttachment.setAttributeValue(RepositoryTaskAttribute.USER_ASSIGNED, attachment.getAuthor());

			taskAttachment.setAttributeValue(RepositoryTaskAttribute.ATTACHMENT_SIZE,
					String.valueOf(attachment.getSize()));
			taskAttachment.setAttributeValue(RepositoryTaskAttribute.ATTACHMENT_DATE,
					formatDate(attachment.getCreated()));
			taskAttachment.setAttributeValue(RepositoryTaskAttribute.ATTACHMENT_URL, //
					client.getBaseUrl() + "/secure/attachment/" + attachment.getId() + "/" + attachment.getName());
			data.addAttachment(taskAttachment);
		}

		removeAttributes(data, JiraAttributeFactory.ATTRIBUTE_CUSTOM_PREFIX);
		for (CustomField field : jiraIssue.getCustomFields()) {
			String mappedKey = attributeFactory.mapCommonAttributeKey(field.getId());
			String name = field.getName().replace("&", "&&") + ":"; // mask & from SWT
			RepositoryTaskAttribute attribute = new RepositoryTaskAttribute(mappedKey, name,
					attributeFactory.isHidden(mappedKey));

			String type = field.getKey();
			attribute.putMetaDataValue(JiraAttributeFactory.TYPE_KEY, type);
			attribute.setReadOnly(field.isReadOnly());

			for (String value : field.getValues()) {
				attribute.addValue(value);
			}
			data.addAttribute(attribute.getId(), attribute);
		}

		updateMarkup(data, jiraIssue, client, oldTaskData, monitor);

		HashSet<String> editableKeys = new HashSet<String>();
		if (!JiraRepositoryConnector.isClosed(jiraIssue)) {
			if (useCachedInformation(jiraIssue, oldTaskData)) {
				// avoid server round-trips
				for (RepositoryTaskAttribute attribute : oldTaskData.getAttributes()) {
					if (!attribute.isReadOnly()) {
						editableKeys.add(attribute.getId());
					}
				}

				RepositoryTaskAttribute attribute = oldTaskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_READ_ONLY);
				if (attribute != null) {
					data.addAttribute(JiraAttributeFactory.ATTRIBUTE_READ_ONLY, attribute);
				}
			} else {
				try {
					RepositoryTaskAttribute[] editableAttributes = client.getEditableAttributes(jiraIssue.getKey(),
							monitor);
					if (editableAttributes != null) {
						for (RepositoryTaskAttribute attribute : editableAttributes) {
							editableKeys.add(attributeFactory.mapCommonAttributeKey(attribute.getId()));
						}
					}
				} catch (JiraInsufficientPermissionException ex) {
					RepositoryTaskAttribute attribute = new RepositoryTaskAttribute(
							JiraAttributeFactory.ATTRIBUTE_READ_ONLY, "Read-only", true);
					data.addAttribute(JiraAttributeFactory.ATTRIBUTE_READ_ONLY, attribute);
				}
			}
		}

		for (RepositoryTaskAttribute attribute : data.getAttributes()) {
			boolean editable = editableKeys.contains(attribute.getId().toLowerCase());
			attribute.setReadOnly(!editable);
			if (editable && (attribute.getId().startsWith(JiraAttributeFactory.ATTRIBUTE_CUSTOM_PREFIX) //
					|| !attributeFactory.isHidden(attribute.getId()))) {
				attribute.setHidden(false);
			}

			if (RepositoryTaskAttribute.COMMENT_NEW.equals(attribute.getId())) {
				attribute.setReadOnly(false);
			} else {
				// make attributes read-only if can't find editing options
				String key = attribute.getMetaDataValue(JiraAttributeFactory.TYPE_KEY);
				Collection<String> options = attribute.getOptions();
				if (JiraFieldType.SELECT.getKey().equals(key)
						&& (options == null || options.isEmpty() || attribute.isReadOnly())) {
					attribute.setReadOnly(true);
				} else if (JiraFieldType.MULTISELECT.getKey().equals(key) && (options == null || options.isEmpty())) {
					attribute.setReadOnly(true);
				}
			}
		}
	}

	private boolean useCachedInformation(JiraIssue issue, RepositoryTaskData oldTaskData) {
		return oldTaskData != null && oldTaskData.getStatus().equals(issue.getStatus().getName());
	}

	private String formatDate(Date date) {
		if (date == null) {
			return "";
		} else {
			return new SimpleDateFormat(JiraAttributeFactory.JIRA_DATE_FORMAT, Locale.US).format(date);
		}
	}

	private void removeAttributes(RepositoryTaskData data, String keyPrefix) {
		for (RepositoryTaskAttribute attribute : data.getAttributes()) {
			if (attribute.getName().startsWith(keyPrefix)) {
				data.removeAttribute(attribute.getId());
			}
		}
	}

	/**
	 * Removes attribute values without removing attribute to preserve order of attributes
	 */
	private void removeAttributeValues(RepositoryTaskData data, String keyPrefix) {
		for (RepositoryTaskAttribute attribute : data.getAttributes()) {
			if (attribute.getId().startsWith(keyPrefix)) {
				ListIterator<String> it = attribute.getValues().listIterator();
				while (it.hasNext()) {
					it.next();
					it.remove();
				}
			}
		}
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

	private RepositoryTaskAttribute createAuthorAttribute(String value) {
		RepositoryTaskAttribute attr = attributeFactory.createAttribute(RepositoryTaskAttribute.COMMENT_AUTHOR);
		attr.setHidden(true);
		attr.setReadOnly(true);
		attr.setValue(value);
		return attr;
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
	private void updateMarkup(RepositoryTaskData data, JiraIssue jiraIssue, JiraClient client,
			RepositoryTaskData oldTaskData, IProgressMonitor monitor) throws JiraException {
		if (!jiraIssue.isMarkupDetected()) {
			return;
		}

		if (jiraIssue.getUpdated() != null && oldTaskData != null) {
			String value = oldTaskData.getAttributeValue(RepositoryTaskAttribute.DATE_MODIFIED);
			if (jiraIssue.getUpdated().equals(JiraUtil.stringToDate(value))) {
				// use cached information
				if (data.getAttribute(RepositoryTaskAttribute.DESCRIPTION) != null) {
					data.setAttributeValue(RepositoryTaskAttribute.DESCRIPTION,
							oldTaskData.getAttributeValue(RepositoryTaskAttribute.DESCRIPTION));
				}
				if (data.getAttribute(JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT) != null) {
					data.setAttributeValue(JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT,
							oldTaskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT));
				}
				for (CustomField field : jiraIssue.getCustomFields()) {
					if (field.isMarkupDetected()) {
						RepositoryTaskAttribute oldAttribute = oldTaskData.getAttribute(field.getId());
						if (oldAttribute != null) {
							RepositoryTaskAttribute attribute = data.getAttribute(field.getId());
							attribute.setValues(new ArrayList<String>(oldAttribute.getValues()));
						}
					}
				}
				return;
			}
		}

		// consider preserving HTML 
		RemoteIssue remoteIssue = client.getSoapClient().getIssueByKey(jiraIssue.getKey(), monitor);
		if (data.getAttribute(RepositoryTaskAttribute.DESCRIPTION) != null) {
			if (remoteIssue.getDescription() == null) {
				data.setAttributeValue(RepositoryTaskAttribute.DESCRIPTION, "");
			} else {
				data.setAttributeValue(RepositoryTaskAttribute.DESCRIPTION, remoteIssue.getDescription());
			}
		}
		if (data.getAttribute(JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT) != null) {
			if (remoteIssue.getEnvironment() == null) {
				data.setAttributeValue(JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT, "");
			} else {
				data.setAttributeValue(JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT, remoteIssue.getEnvironment());
			}
		}
		RemoteCustomFieldValue[] fields = remoteIssue.getCustomFieldValues();
		for (CustomField field : jiraIssue.getCustomFields()) {
			if (field.isMarkupDetected()) {
				innerLoop: for (RemoteCustomFieldValue remoteField : fields) {
					if (field.getId().equals(remoteField.getCustomfieldId())) {
						RepositoryTaskAttribute attribute = data.getAttribute(field.getId());
						if (attribute != null) {
							attribute.setValues(Arrays.asList(remoteField.getValues()));
						}
						break innerLoop;
					}
				}
			}
		}

	}

	public void addOperations(RepositoryTaskData data, JiraIssue issue, JiraClient client,
			RepositoryTaskData oldTaskData, IProgressMonitor monitor) throws JiraException {
		// avoid server round-trips
		if (useCachedInformation(issue, oldTaskData)) {
			for (RepositoryOperation operation : oldTaskData.getOperations()) {
				data.addOperation(operation);
			}
			return;
		}

		RepositoryOperation leaveOperation = new RepositoryOperation(LEAVE_OPERATION, "Leave as "
				+ issue.getStatus().getName());
		leaveOperation.setChecked(true);
		data.addOperation(leaveOperation);

		// TODO need more accurate status matching
		if (!JiraRepositoryConnector.isCompleted(data)) {
			RepositoryOperation reassignOperation = new RepositoryOperation(REASSIGN_OPERATION, "Reassign to");
			reassignOperation.setInputName(JiraAttribute.USER_ASSIGNED.getParamName());
			reassignOperation.setInputValue(client.getUserName());
			data.addOperation(reassignOperation);
		}

		RepositoryOperation[] availableOperations = client.getAvailableOperations(issue.getKey(), monitor);
		if (availableOperations != null) {
			for (RepositoryOperation operation : availableOperations) {
				String[] fields = client.getActionFields(issue.getKey(), operation.getKnobName(), monitor);
				for (String field : fields) {
					if (RepositoryTaskAttribute.RESOLUTION.equals(attributeFactory.mapCommonAttributeKey(field))) {
						operation.setInputName(field);
						operation.setUpOptions(field);
						addResolutions(client, operation);
					}
					// TODO handle other action fields
				}
				data.addOperation(operation);
			}
		}
	}

	private void addResolutions(JiraClient client, RepositoryOperation operation) {
		Resolution[] resolutions = client.getCache().getResolutions();
		if (resolutions.length > 0) {
			for (Resolution resolution : resolutions) {
				operation.addOption(resolution.getName(), resolution.getId());
				if (Resolution.FIXED_ID.equals(resolution.getId())) {
					// set fixed as default
					operation.setOptionSelection(resolution.getName());
				}
			}
		} else {
			operation.addOption("Fixed", Resolution.FIXED_ID);
			operation.addOption("Won't Fix", Resolution.WONT_FIX_ID);
			operation.addOption("Duplicate", Resolution.DUPLICATE_ID);
			operation.addOption("Incomplete", Resolution.INCOMPLETE_ID);
			operation.addOption("Cannot Reproduce", Resolution.CANNOT_REPRODUCE_ID);
			operation.setOptionSelection("Fixed");
		}
	}

	public String postTaskData(TaskRepository repository, RepositoryTaskData taskData, IProgressMonitor monitor)
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
				RepositoryOperation operation = taskData.getSelectedOperation();
				if (operation == null) {
					operation = new RepositoryOperation(LEAVE_OPERATION, "");
				}

				String inputName = operation.getInputName();
				if (inputName != null) {
					String value;
					if (operation.hasOptions()) {
						value = operation.getOptionValue(operation.getOptionSelection());
					} else {
						value = operation.getInputValue();
					}
					if (value != null) {
						issue.setValue(inputName, value);
					}
				}

				if (LEAVE_OPERATION.equals(operation.getKnobName())
						|| REASSIGN_OPERATION.equals(operation.getKnobName())) {
					if (!JiraRepositoryConnector.isClosed(issue)
							&& taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_READ_ONLY) == null) {
						client.updateIssue(issue, taskData.getNewComment(), monitor);
					} else if (taskData.getNewComment() != null && taskData.getNewComment().length() > 0) {
						client.addCommentToIssue(issue, taskData.getNewComment(), monitor);
					}
				} else {
					client.advanceIssueWorkflow(issue, operation.getKnobName(), taskData.getNewComment(), monitor);
				}

				return "";
			}
		} catch (JiraException e) {
			IStatus status = JiraCorePlugin.toStatus(repository, e);
			trace(status);
			throw new CoreException(status);
		}
	}

	public boolean initializeTaskData(TaskRepository repository, RepositoryTaskData data, IProgressMonitor monitor)
			throws CoreException {
		String projectName = data.getAttributeValue(RepositoryTaskAttribute.PRODUCT);
		if (projectName == null) {
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

		Project project = getProject(client, projectName);
		if (project == null) {
			project = client.getCache().getProjectByKey(projectName);
		}
		if (project == null) {
			return false;
		}

		initializeTaskData(data, client, project);

		data.setAttributeValue(RepositoryTaskAttribute.PRODUCT, project.getName());

		return true;
	}

	public boolean initializeSubTaskData(TaskRepository repository, RepositoryTaskData taskData,
			RepositoryTaskData parentTaskData, IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask("Creating subtask", IProgressMonitor.UNKNOWN);

			JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
			if (!client.getCache().hasDetails()) {
				client.getCache().refreshDetails(new SubProgressMonitor(monitor, 1));
			}

			Project project = getProject(client, parentTaskData.getProduct());
			if (project == null) {
				throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN,
						IStatus.OK, "The parent task does not have a valid project.", null));
			}

			initializeTaskData(taskData, client, project);
			//cloneTaskData(parentTaskData, taskData);
			taskData.setDescription("");
			taskData.setSummary("");
			taskData.setAttributeValue(RepositoryTaskAttribute.USER_ASSIGNED, parentTaskData.getAssignedTo());
			taskData.setAttributeValue(RepositoryTaskAttribute.PRODUCT, project.getName());

			// set subtask type
			RepositoryTaskAttribute typeAttribute = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_TYPE);
			typeAttribute.clearOptions();

			IssueType[] jiraIssueTypes = client.getCache().getIssueTypes();
			for (IssueType type : jiraIssueTypes) {
				if (type.isSubTaskType()) {
					typeAttribute.addOption(type.getName(), type.getId());
				}
			}

			List<String> options = typeAttribute.getOptions();
			if (options.size() == 0) {
				throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN,
						IStatus.OK, "The repository does not support subtasks.", null));
			} else if (options.size() == 1) {
				typeAttribute.setReadOnly(true);
			}
			typeAttribute.setValue(options.get(0));

			// set parent id
			RepositoryTaskAttribute attribute = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_ISSUE_PARENT_ID);
			attribute.setValue(parentTaskData.getTaskId());

			attribute = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_ISSUE_PARENT_KEY);
			attribute.setValue(parentTaskData.getTaskKey());

			return true;
		} catch (JiraException e) {
			IStatus status = JiraCorePlugin.toStatus(repository, e);
			trace(status);
			throw new CoreException(status);
		} finally {
			monitor.done();
		}
	}

	public boolean canInitializeSubTaskData(AbstractTask task, RepositoryTaskData parentTaskData) {
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

	@Override
	public AbstractAttributeFactory getAttributeFactory(String repositoryUrl, String repositoryKind, String taskKind) {
		// we don't care about the repository information right now
		return attributeFactory;
	}

	public AbstractAttributeFactory getAttributeFactory(RepositoryTaskData taskData) {
		return getAttributeFactory(taskData.getRepositoryUrl(), taskData.getConnectorKind(), taskData.getTaskKind());
	}

	private JiraIssue buildJiraIssue(RepositoryTaskData taskData, JiraClient client) {
		JiraIssue issue = new JiraIssue();
		issue.setId(taskData.getTaskId());
		issue.setKey(taskData.getTaskKey());
		issue.setSummary(taskData.getAttributeValue(RepositoryTaskAttribute.SUMMARY));
		issue.setDescription(taskData.getAttributeValue(RepositoryTaskAttribute.DESCRIPTION));

		// TODO sync due date between jira and local planning
		issue.setDue(JiraUtil.stringToDate(taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_DUE_DATE)));

		String parentId = taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_ISSUE_PARENT_ID);
		if (parentId != null) {
			issue.setParentId(parentId);
		}

		RepositoryTaskAttribute attribute = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_SECURITY_LEVEL);
		if (attribute != null) {
			SecurityLevel securityLevel = new SecurityLevel();
			securityLevel.setId(attribute.getOptionParameter(attribute.getValue()));
			issue.setSecurityLevel(securityLevel);
		}

		String estimate = taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_ESTIMATE);
		if (estimate != null) {
			JiraTimeFormat timeFormat = new JiraTimeFormat();
			issue.setEstimate(timeFormat.parse(estimate));
		}

		estimate = taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_INITIAL_ESTIMATE);
		if (estimate != null) {
			JiraTimeFormat timeFormat = new JiraTimeFormat();
			issue.setInitialEstimate(timeFormat.parse(estimate));
		}

		Project project = getProject(client, taskData.getAttributeValue(RepositoryTaskAttribute.PRODUCT));
		if (project != null) {
			issue.setProject(project);
		}

		for (IssueType type : client.getCache().getIssueTypes()) {
			if (type.getName().equals(taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_TYPE))) {
				issue.setType(type);
				break;
			}
		}
		for (org.eclipse.mylyn.internal.jira.core.model.JiraStatus status : client.getCache().getStatuses()) {
			if (status.getName().equals(taskData.getAttributeValue(RepositoryTaskAttribute.STATUS))) {
				issue.setStatus(status);
				break;
			}
		}
		RepositoryTaskAttribute componentsAttr = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_COMPONENTS);
		if (componentsAttr != null) {
			ArrayList<Component> components = new ArrayList<Component>();
			for (String compStr : taskData.getAttributeValues(JiraAttributeFactory.ATTRIBUTE_COMPONENTS)) {
				if (componentsAttr.getOptionParameter(compStr) != null) {
					Component comp = new Component();
					comp.setId(componentsAttr.getOptionParameter(compStr));
					comp.setName(compStr);
					components.add(comp);
				} else {
					StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN,
							"Error setting component for JIRA issue. Component id is null: " + compStr));
				}
			}
			issue.setComponents(components.toArray(new Component[components.size()]));
		}

		RepositoryTaskAttribute fixVersionAttr = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS);
		if (fixVersionAttr != null) {
			ArrayList<Version> fixversions = new ArrayList<Version>();
			for (String fixStr : taskData.getAttributeValues(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS)) {
				if (fixVersionAttr.getOptionParameter(fixStr) != null) {
					Version version = new Version();
					version.setId(fixVersionAttr.getOptionParameter(fixStr));
					version.setName(fixStr);
					fixversions.add(version);
				} else {
					StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN,
							"Error setting fix version for JIRA issue. Version id is null: " + fixStr));
				}
			}
			issue.setFixVersions(fixversions.toArray(new Version[fixversions.size()]));
		}

		RepositoryTaskAttribute affectsVersionAttr = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS);
		if (affectsVersionAttr != null) {
			ArrayList<Version> affectsversions = new ArrayList<Version>();
			for (String fixStr : taskData.getAttributeValues(JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS)) {
				if (affectsVersionAttr.getOptionParameter(fixStr) != null) {
					Version version = new Version();
					version.setId(affectsVersionAttr.getOptionParameter(fixStr));
					version.setName(fixStr);
					affectsversions.add(version);
				} else {
					StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN,
							"Error setting affects version for JIRA issue. Version id is null: " + fixStr));
				}
			}
			issue.setReportedVersions(affectsversions.toArray(new Version[affectsversions.size()]));
		}
		issue.setReporter(taskData.getAttributeValue(RepositoryTaskAttribute.USER_REPORTER));

		RepositoryOperation operation = taskData.getSelectedOperation();
		String assignee;
		if (operation != null && REASSIGN_OPERATION.equals(operation.getKnobName())) {
			assignee = operation.getInputValue();
		} else {
			assignee = taskData.getAttributeValue(RepositoryTaskAttribute.USER_ASSIGNED);
		}
		issue.setAssignee(JiraRepositoryConnector.getAssigneeFromAttribute(assignee));

		issue.setEnvironment(taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT));
		for (Priority priority : client.getCache().getPriorities()) {
			if (priority.getName().equals(taskData.getAttributeValue(RepositoryTaskAttribute.PRIORITY))) {
				issue.setPriority(priority);
				break;
			}
		}

		ArrayList<CustomField> customFields = new ArrayList<CustomField>();
		for (RepositoryTaskAttribute attr : taskData.getAttributes()) {
			if (attr.getId().startsWith(JiraAttributeFactory.ATTRIBUTE_CUSTOM_PREFIX)) {
				String id = attr.getId().substring(JiraAttributeFactory.ATTRIBUTE_CUSTOM_PREFIX.length());
				CustomField field = new CustomField(id, attr.getMetaDataValue(JiraAttributeFactory.TYPE_KEY),
						attr.getName(), attr.getValues());
				field.setReadOnly(attr.isReadOnly());
				customFields.add(field);
			}
		}
		issue.setCustomFields(customFields.toArray(new CustomField[customFields.size()]));

		return issue;
	}

	public Set<String> getSubTaskIds(RepositoryTaskData taskData) {
		Set<String> subIds = new HashSet<String>();

		RepositoryTaskAttribute attribute = taskData.getAttribute(JiraAttribute.SUBTASK_IDS.getId());
		if (attribute != null) {
			subIds.addAll(attribute.getValues());
		}

		attribute = taskData.getAttribute(JiraAttribute.LINKED_IDS.getId());
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
	public boolean initializeTaskData(TaskRepository repository, TaskData data, IProgressMonitor monitor)
			throws CoreException {
		// ignore
		return false;
	}

	@Override
	public String postTaskData(TaskRepository repository, TaskData taskData, IProgressMonitor monitor)
			throws CoreException {
		// ignore
		return null;
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

}