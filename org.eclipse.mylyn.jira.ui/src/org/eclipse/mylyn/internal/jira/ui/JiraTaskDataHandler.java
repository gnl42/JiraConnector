/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylar.internal.jira.core.model.Attachment;
import org.eclipse.mylar.internal.jira.core.model.Comment;
import org.eclipse.mylar.internal.jira.core.model.Component;
import org.eclipse.mylar.internal.jira.core.model.CustomField;
import org.eclipse.mylar.internal.jira.core.model.Issue;
import org.eclipse.mylar.internal.jira.core.model.IssueType;
import org.eclipse.mylar.internal.jira.core.model.Priority;
import org.eclipse.mylar.internal.jira.core.model.Project;
import org.eclipse.mylar.internal.jira.core.model.Resolution;
import org.eclipse.mylar.internal.jira.core.model.Status;
import org.eclipse.mylar.internal.jira.core.model.Version;
import org.eclipse.mylar.internal.jira.core.service.JiraClient;
import org.eclipse.mylar.internal.jira.core.service.JiraException;
import org.eclipse.mylar.internal.jira.ui.html.HTML2TextReader;
import org.eclipse.mylar.tasks.core.AbstractAttributeFactory;
import org.eclipse.mylar.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.ITaskDataHandler;
import org.eclipse.mylar.tasks.core.RepositoryAttachment;
import org.eclipse.mylar.tasks.core.RepositoryOperation;
import org.eclipse.mylar.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylar.tasks.core.RepositoryTaskData;
import org.eclipse.mylar.tasks.core.Task;
import org.eclipse.mylar.tasks.core.TaskComment;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;

/**
 * @author Mik Kersten
 * @author Rob Elves
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 */
public class JiraTaskDataHandler implements ITaskDataHandler {

	private static final JiraAttributeFactory attributeFactory = new JiraAttributeFactory();

	private static final HashMap<String, String> FIELD_TYPES = new HashMap<String, String>();
	static {
		FIELD_TYPES.put(RepositoryTaskAttribute.RESOLUTION, JiraFieldType.SELECT.getKey());
		FIELD_TYPES.put(RepositoryTaskAttribute.USER_ASSIGNED, JiraFieldType.USERPICKER.getKey());
		FIELD_TYPES.put(RepositoryTaskAttribute.USER_REPORTER, JiraFieldType.USERPICKER.getKey());

		FIELD_TYPES.put(RepositoryTaskAttribute.PRODUCT, JiraFieldType.PROJECT.getKey());
		FIELD_TYPES.put(RepositoryTaskAttribute.PRIORITY, JiraFieldType.SELECT.getKey());

		FIELD_TYPES.put(JiraAttributeFactory.ATTRIBUTE_TYPE, JiraFieldType.SELECT.getKey());

		FIELD_TYPES.put(JiraAttributeFactory.ATTRIBUTE_COMPONENTS, JiraFieldType.MULTISELECT.getKey());
		FIELD_TYPES.put(JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS, JiraFieldType.MULTISELECT.getKey());
		FIELD_TYPES.put(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS, JiraFieldType.MULTISELECT.getKey());

		FIELD_TYPES.put(JiraAttributeFactory.ATTRIBUTE_ESTIMATE, JiraFieldType.TEXTFIELD.getKey());
		FIELD_TYPES.put(JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT, JiraFieldType.TEXTAREA.getKey());
	}
	
	public JiraTaskDataHandler(JiraRepositoryConnector connector) {
	}

	public RepositoryTaskData getTaskData(TaskRepository repository, String taskId) throws CoreException {
		try {
			JiraClient server = JiraClientFacade.getDefault().getJiraClient(repository);
			if (!server.hasDetails()) {
				server.refreshDetails(new NullProgressMonitor());
			}
			Issue jiraIssue = getJiraIssue(server, taskId, repository.getUrl());
			if (jiraIssue == null) {
				throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR, JiraCorePlugin.ID,
						IStatus.OK, "JIRA ticket not found: " + taskId, null));
			}
			RepositoryTaskData data = new RepositoryTaskData(attributeFactory, JiraUiPlugin.REPOSITORY_KIND, repository
					.getUrl(), jiraIssue.getId(), Task.DEFAULT_TASK_KIND);
			initializeTaskData(data, server, jiraIssue.getProject());
			updateTaskData(data, jiraIssue, server);
			addOperations(data, jiraIssue, server);
			return data;
		} catch (JiraException e) {
			throw new CoreException(JiraCorePlugin.toStatus(repository, e));
		}
	}

	private Issue getJiraIssue(JiraClient server, String taskId, String repositoryUrl) throws CoreException,
			JiraException {
		try {
			int id = Integer.parseInt(taskId);
			ITask task = TasksUiPlugin.getTaskListManager().getTaskList().getTask(repositoryUrl, "" + id);
			if (task instanceof JiraTask) {
				JiraTask jiraTask = (JiraTask) task;
				return server.getIssueByKey(jiraTask.getTaskKey());
			} else {
				return server.getIssueById(taskId);
			}
		} catch (NumberFormatException e) {
			return server.getIssueByKey(taskId);
		}
		// throw new CoreException(new
		// org.eclipse.core.runtime.Status(IStatus.ERROR, JiraCorePlugin.ID,
		// IStatus.OK, "JIRA ticket not found: " + taskId, null));
	}

	public void initializeTaskData(RepositoryTaskData data, JiraClient server, Project project) {
		data.removeAllAttributes();

		addAttribute(data, RepositoryTaskAttribute.DATE_CREATION, "Created: ", true);
		addAttribute(data, RepositoryTaskAttribute.SUMMARY, "Summary: ", true);
		addAttribute(data, RepositoryTaskAttribute.DESCRIPTION, "Description: ", true);
		addAttribute(data, RepositoryTaskAttribute.STATUS, "Status: ", true);
		addAttribute(data, JiraAttributeFactory.ATTRIBUTE_ISSUE_KEY, "Issue ID: ", true);
		addAttribute(data, RepositoryTaskAttribute.RESOLUTION, "Resolution: ", true);
		addAttribute(data, RepositoryTaskAttribute.USER_ASSIGNED, "Assigned to: ", true);
		addAttribute(data, RepositoryTaskAttribute.USER_REPORTER, "Reported by: ", true);
		addAttribute(data, RepositoryTaskAttribute.DATE_MODIFIED, "Date modified: ", true);
		
		RepositoryTaskAttribute product = addAttribute(data, RepositoryTaskAttribute.PRODUCT, "Project: ", false);
		product.setReadOnly(true);
		
		RepositoryTaskAttribute priorities = addAttribute(data, RepositoryTaskAttribute.PRIORITY, "Priority: ", false);
		for (Priority priority : server.getPriorities()) {
			priorities.addOption(priority.getName(), priority.getId());
		}
		
		RepositoryTaskAttribute types = addAttribute(data, JiraAttributeFactory.ATTRIBUTE_TYPE, "Type: ", false);
		for (IssueType type : server.getIssueTypes()) {
			types.addOption(type.getName(), type.getId());
		}
		
		addAttribute(data, JiraAttributeFactory.ATTRIBUTE_ESTIMATE, "Estimate: ", false);
		
		RepositoryTaskAttribute components = addAttribute(data, JiraAttributeFactory.ATTRIBUTE_COMPONENTS, "Components: ", false);
		for (Component component : project.getComponents()) {
			components.addOption(component.getName(), component.getId());
		}

		RepositoryTaskAttribute fixVersions = addAttribute(data, JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS, "Fix Versions: ", false);
		for (Version version : project.getVersions()) {
			fixVersions.addOption(version.getName(), version.getId());
		}

		RepositoryTaskAttribute affectsVersions = addAttribute(data, JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS, "Affects Versions: ", false);
		for (Version version : project.getVersions()) {
			affectsVersions.addOption(version.getName(), version.getId());
		}

		addAttribute(data, JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT, "Environment: ", false);
	}

	private RepositoryTaskAttribute addAttribute(RepositoryTaskData data, String key, String name, boolean isHidden) {
		RepositoryTaskAttribute attribute = new RepositoryTaskAttribute(key, name, isHidden);
		String type = FIELD_TYPES.get(key);
		if(type!=null) {
			attribute.putMetaDataValue(JiraAttributeFactory.TYPE_KEY, type);
		}
		data.addAttribute(key, attribute);
		return attribute;
	}

	private void updateTaskData(RepositoryTaskData data, Issue jiraIssue, JiraClient server) {
		updateAttribute(data, RepositoryTaskAttribute.DATE_CREATION, dateToString(jiraIssue.getCreated()));
		updateAttribute(data, RepositoryTaskAttribute.SUMMARY, convertHtml(jiraIssue.getSummary()));
		updateAttribute(data, RepositoryTaskAttribute.DESCRIPTION, convertHtml(jiraIssue.getDescription()));
		updateAttribute(data, RepositoryTaskAttribute.STATUS, convertHtml(jiraIssue.getStatus().getName()));
		updateAttribute(data, JiraAttributeFactory.ATTRIBUTE_ISSUE_KEY, jiraIssue.getKey());
		updateAttribute(data, RepositoryTaskAttribute.RESOLUTION, //
				jiraIssue.getResolution() == null ? "" : jiraIssue.getResolution().getName());
		updateAttribute(data, RepositoryTaskAttribute.DATE_MODIFIED, dateToString(jiraIssue.getUpdated()));

		updateAttribute(data, RepositoryTaskAttribute.USER_ASSIGNED, getAssignee(jiraIssue));
		updateAttribute(data, RepositoryTaskAttribute.USER_REPORTER, jiraIssue.getReporter());

		updateAttribute(data, RepositoryTaskAttribute.PRODUCT, jiraIssue.getProject().getName());
		
		if (jiraIssue.getPriority() != null) {
			updateAttribute(data, RepositoryTaskAttribute.PRIORITY, jiraIssue.getPriority().getName());
		}
		
		updateAttribute(data, JiraAttributeFactory.ATTRIBUTE_TYPE, jiraIssue.getType().getName());
		
		updateAttribute(data, JiraAttributeFactory.ATTRIBUTE_ESTIMATE, Long.toString(jiraIssue.getEstimate()));
		
		if (jiraIssue.getComponents() != null) {
			setAttributeType(data, JiraAttributeFactory.ATTRIBUTE_COMPONENTS);
			for (Component component : jiraIssue.getComponents()) {
				data.addAttributeValue(JiraAttributeFactory.ATTRIBUTE_COMPONENTS, component.getName());
			}
		} else {
			data.removeAttribute(JiraAttributeFactory.ATTRIBUTE_COMPONENTS);
		}

		if (jiraIssue.getReportedVersions() != null) {
			setAttributeType(data, JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS);
			for (Version version : jiraIssue.getReportedVersions()) {
				data.addAttributeValue(JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS, version.getName());
			}
		} else {
			data.removeAttribute(JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS);
		}

		if (jiraIssue.getFixVersions() != null) {
			setAttributeType(data, JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS);
			for (Version version : jiraIssue.getFixVersions()) {
				data.addAttributeValue(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS, version.getName());
			}
		} else {
			data.removeAttribute(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS);
		}
		
		if(jiraIssue.getEnvironment()!=null) {
			updateAttribute(data, JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT, convertHtml(jiraIssue.getEnvironment()));
		}

		int x = 1;
		for (Comment comment : jiraIssue.getComments()) {
			TaskComment taskComment = new TaskComment(attributeFactory, x++);

			RepositoryTaskAttribute attribute = new RepositoryTaskAttribute(RepositoryTaskAttribute.USER_OWNER,
					"Commenter: ", true);
			attribute.setValue(comment.getAuthor());
			taskComment.addAttribute(RepositoryTaskAttribute.USER_OWNER, attribute);

			attribute = new RepositoryTaskAttribute(RepositoryTaskAttribute.COMMENT_TEXT, "Text: ", true);
			attribute.setValue(convertHtml(comment.getComment()));
			attribute.setReadOnly(true);
			taskComment.addAttribute(RepositoryTaskAttribute.COMMENT_TEXT, attribute);

			attribute = new RepositoryTaskAttribute(RepositoryTaskAttribute.COMMENT_DATE, "Text: ", true);
			attribute.setValue(dateToString(comment.getCreated()));
			taskComment.addAttribute(RepositoryTaskAttribute.COMMENT_DATE, attribute);

			data.addComment(taskComment);
		}

		for (Attachment attachment : jiraIssue.getAttachments()) {
			RepositoryAttachment taskAttachment = new RepositoryAttachment(attributeFactory);
			taskAttachment.setCreator(attachment.getAuthor());
			taskAttachment.setRepositoryKind(JiraUiPlugin.REPOSITORY_KIND);
			taskAttachment.setRepositoryUrl(server.getBaseUrl());
			taskAttachment.setTaskId(jiraIssue.getKey());
			taskAttachment.setAttributeValue(RepositoryTaskAttribute.ATTACHMENT_ID, attachment.getId());
			taskAttachment.setAttributeValue(RepositoryTaskAttribute.ATTACHMENT_FILENAME, attachment.getName());
			if (JiraAttachmentHandler.CONTEXT_ATTACHEMNT_FILENAME.equals(attachment.getName())) {
				taskAttachment.setAttributeValue(RepositoryTaskAttribute.DESCRIPTION, AbstractRepositoryConnector.MYLAR_CONTEXT_DESCRIPTION);
			} else {
				taskAttachment.setAttributeValue(RepositoryTaskAttribute.DESCRIPTION, attachment.getName());
			}
			taskAttachment.setAttributeValue(RepositoryTaskAttribute.USER_OWNER, attachment.getAuthor());
			taskAttachment.setAttributeValue(RepositoryTaskAttribute.ATTACHMENT_DATE, dateToString(attachment.getCreated()));
			taskAttachment.setAttributeValue(RepositoryTaskAttribute.ATTACHMENT_URL, server.getBaseUrl()
					+ "/secure/attachment/" + attachment.getId() + "/" + attachment.getName());
			data.addAttachment(taskAttachment);
		}
		
		for (CustomField field : jiraIssue.getCustomFields()) {
			String id = JiraAttributeFactory.ATTRIBUTE_CUSTOM_PREFIX + field.getId();
			// TODO make hidden for now
			RepositoryTaskAttribute attribute = new RepositoryTaskAttribute(id, field.getName() + ":", true);
			String type = field.getKey();
			attribute.putMetaDataValue(JiraAttributeFactory.TYPE_KEY, type);
			for (String value : field.getValues()) {
				if(JiraFieldType.TEXTAREA.getKey().equals(type)) {
					attribute.addValue(convertHtml(value));
				} else {
					attribute.addValue(value);
				}
			}
			data.addAttribute(id, attribute);
		}
	}

	private void updateAttribute(RepositoryTaskData data, String key, String value) {
		// need to differentiate between empty value and absent attribute
		data.setAttributeValue(key, value);
		String type = FIELD_TYPES.get(key);
		if (type != null) {
			data.getAttribute(key).putMetaDataValue(JiraAttributeFactory.TYPE_KEY, type);
		}
	}

	private void setAttributeType(RepositoryTaskData data, String key) {
		String type = FIELD_TYPES.get(key);
		if(type!=null) {
			data.getAttribute(key).putMetaDataValue(JiraAttributeFactory.TYPE_KEY, type);
		}
	}

	private String dateToString(Date date) {
		return new SimpleDateFormat(JiraAttributeFactory.JIRA_DATE_FORMAT, Locale.US).format(date);
	}

	private String getAssignee(Issue jiraIssue) {
		String assignee = jiraIssue.getAssignee();
		return assignee == null || JiraTask.UNASSIGNED_USER.equals(assignee) ? "" : assignee;
	}

	public static String convertHtml(String text) {
		if (text == null || text.length() == 0) {
			return "";
		}
		StringReader stringReader = new StringReader(text);
		HTML2TextReader html2TextReader = new HTML2TextReader(stringReader, null);
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

	private void addOperations(RepositoryTaskData data, Issue issue, JiraClient server) {
		Status status = issue.getStatus();

		RepositoryOperation opLeave = new RepositoryOperation("leave", "Leave as " + issue.getStatus().getName());
		// RepositoryOperation opStart = new
		// RepositoryOperation(Status.STARTED_ID, "Start");
		// RepositoryOperation opStop = new RepositoryOperation(Status.OPEN_ID,
		// "Stop (open)");
		RepositoryOperation opReopen = new RepositoryOperation(Status.REOPENED_ID, "Reopen");

		RepositoryOperation opResolve = new RepositoryOperation(Status.RESOLVED_ID, "Resolve");
		opResolve.setUpOptions("resolution");
		addResolutions(server, opResolve);

		RepositoryOperation opClose = new RepositoryOperation(Status.CLOSED_ID, "Close");
		opClose.setUpOptions("resolution");
		addResolutions(server, opClose);

		RepositoryOperation reassignOperation = new RepositoryOperation("reassign", "Reassign to");
		reassignOperation.setInputName("assignee");
		reassignOperation.setInputValue(server.getUserName());

		opLeave.setChecked(true);
		data.addOperation(opLeave);
		if (status.getId().equals(Status.OPEN_ID) || status.getId().equals(Status.STARTED_ID)) {
			data.addOperation(opResolve);
			data.addOperation(opClose);
			data.addOperation(reassignOperation);
			// //data.addOperation(opStart);
			// } else if (status.getId().equals(Status.STARTED_ID)) {
			// data.addOperation(opResolve);
			// data.addOperation(opStop);
		} else if (status.getId().equals(Status.RESOLVED_ID)) {
			data.addOperation(opReopen);
			data.addOperation(opClose);
		} else if (status.getId().equals(Status.REOPENED_ID)) {
			data.addOperation(opResolve);
			data.addOperation(opClose);
			// data.addOperation(opStart);
			data.addOperation(reassignOperation);
		} else if (status.getId().equals(Status.CLOSED_ID)) {
			data.addOperation(opReopen);
		}
	}

	private void addResolutions(JiraClient server, RepositoryOperation operation) {
		Resolution[] resolutions = server.getResolutions();
		if (resolutions.length > 0) {
			for (Resolution resolution : resolutions) {
				operation.addOption(resolution.getName(), resolution.getId());
				if (Resolution.FIXED_ID.equals(resolution.getId())) {
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

	public String postTaskData(TaskRepository repository, RepositoryTaskData taskData) throws CoreException {
		final JiraClient jiraServer = JiraClientFacade.getDefault().getJiraClient(repository);
		if (jiraServer == null) {
			throw new CoreException(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.Status.ERROR,
					JiraCorePlugin.ID, org.eclipse.core.runtime.Status.ERROR, "Unable to produce Jira Server", null));
		}

		try {
			Issue issue = buildJiraIssue(taskData, jiraServer);
			if (taskData.isNew()) {
				issue = jiraServer.createIssue(issue);
				if (issue == null) {
					throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR, JiraCorePlugin.ID,
							IStatus.OK, "Could not create ticket.", null));
				}
				// this is severly broken: should return id instead
				return issue.getKey();
			} else {
				RepositoryOperation operation = taskData.getSelectedOperation();
				if (operation != null) {
					if ("leave".equals(operation.getKnobName()) || "reassign".equals(operation.getKnobName())) {
						if (!issue.getStatus().isClosed()) {
							jiraServer.updateIssue(issue, taskData.getNewComment());
						} else if (taskData.getNewComment() != null && taskData.getNewComment().length() > 0) {
							jiraServer.addCommentToIssue(issue, taskData.getNewComment());
						}
					} else if (org.eclipse.mylar.internal.jira.core.model.Status.RESOLVED_ID.equals(operation
							.getKnobName())) {
						String value = operation.getOptionValue(operation.getOptionSelection());
						jiraServer.resolveIssue(issue, jiraServer.getResolutionById(value), issue.getFixVersions(),
								taskData.getNewComment(), JiraClient.ASSIGNEE_CURRENT, repository.getUserName());
					} else if (org.eclipse.mylar.internal.jira.core.model.Status.REOPENED_ID.equals(operation
							.getKnobName())) {
						jiraServer.reopenIssue(issue, taskData.getNewComment(), JiraClient.ASSIGNEE_CURRENT, repository
								.getUserName());
					} else if (org.eclipse.mylar.internal.jira.core.model.Status.STARTED_ID.equals(operation
							.getKnobName())) {
						// FIXME update attributes and comment
						jiraServer.startIssue(issue);
					} else if (org.eclipse.mylar.internal.jira.core.model.Status.OPEN_ID
							.equals(operation.getKnobName())) {
						// FIXME update attributes and comment
						jiraServer.startIssue(issue);
					} else if (org.eclipse.mylar.internal.jira.core.model.Status.CLOSED_ID.equals(operation
							.getKnobName())) {
						String value = operation.getOptionValue(operation.getOptionSelection());
						jiraServer.closeIssue(issue, jiraServer.getResolutionById(value), issue.getFixVersions(),
								taskData.getNewComment(), JiraClient.ASSIGNEE_CURRENT, repository.getUserName());
					}
				} else {
					jiraServer.updateIssue(issue, taskData.getNewComment());
				}
				return "";
			}
		} catch (JiraException e) {
			throw new CoreException(JiraCorePlugin.toStatus(repository, e));
		}
	}

	public boolean initializeTaskData(TaskRepository repository, RepositoryTaskData data, IProgressMonitor monitor)
			throws CoreException {
		// JIRA needs a project to create task data
		return false;
	}

	public AbstractAttributeFactory getAttributeFactory(String repositoryUrl, String repositoryKind, String taskKind) {
		// we don't care about the repository information right now
		return attributeFactory;
	}
	
	public AbstractAttributeFactory getAttributeFactory(RepositoryTaskData taskData) {
		return getAttributeFactory(taskData.getRepositoryUrl(), taskData.getRepositoryKind(), taskData.getTaskKind());
	}

	private Issue buildJiraIssue(RepositoryTaskData taskData, JiraClient client) {
		Issue issue = new Issue();
		issue.setSummary(taskData.getAttributeValue(RepositoryTaskAttribute.SUMMARY));
		issue.setDescription(taskData.getAttributeValue(RepositoryTaskAttribute.DESCRIPTION));
		for (org.eclipse.mylar.internal.jira.core.model.Project project : client.getProjects()) {
			if (project.getName().equals(taskData.getAttributeValue(RepositoryTaskAttribute.PRODUCT))) {
				issue.setProject(project);
				break;
			}
		}
		// issue.setEstimate(Long.parseLong(taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_ESTIMATE)));
	
		for (IssueType type : client.getIssueTypes()) {
			if (type.getName().equals(taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_TYPE))) {
				issue.setType(type);
				break;
			}
		}
		for (org.eclipse.mylar.internal.jira.core.model.Status status : client.getStatuses()) {
			if (status.getName().equals(taskData.getAttributeValue(RepositoryTaskAttribute.STATUS))) {
				issue.setStatus(status);
				break;
			}
		}
		RepositoryTaskAttribute componentsAttr = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_COMPONENTS);
		if(componentsAttr!=null) {
			ArrayList<Component> components = new ArrayList<Component>();
			for (String compStr : taskData.getAttributeValues(JiraAttributeFactory.ATTRIBUTE_COMPONENTS)) {
				if (componentsAttr.getOptionParameter(compStr) != null) {
					Component comp = new Component();
					comp.setId(componentsAttr.getOptionParameter(compStr));
					comp.setName(compStr);
					components.add(comp);
				} else {
					MylarStatusHandler.fail(null, "Error setting component for JIRA issue. Component id is null: "
							+ compStr, false);
				}
			}
			issue.setComponents(components.toArray(new Component[components.size()]));
		}
	
		RepositoryTaskAttribute fixVersionAttr = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS);
		if(fixVersionAttr!=null) {
			ArrayList<Version> fixversions = new ArrayList<Version>();
			for (String fixStr : taskData.getAttributeValues(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS)) {
				if (fixVersionAttr.getOptionParameter(fixStr) != null) {
					Version version = new Version();
					version.setId(fixVersionAttr.getOptionParameter(fixStr));
					version.setName(fixStr);
					fixversions.add(version);
				} else {
					MylarStatusHandler.fail(null,
							"Error setting fix version for JIRA issue. Version id is null: " + fixStr, false);
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
					MylarStatusHandler.fail(null, "Error setting affects version for JIRA issue. Version id is null: "
							+ fixStr, false);
				}
			}
			issue.setReportedVersions(affectsversions.toArray(new Version[affectsversions.size()]));
		}
	
		issue.setReporter(taskData.getAttributeValue(RepositoryTaskAttribute.USER_REPORTER));
		
		RepositoryOperation operation = taskData.getSelectedOperation();
		String assignee;
		if (operation != null && "reassign".equals(operation.getKnobName())) {
			assignee = operation.getInputValue();
		} else {
			assignee = taskData.getAttributeValue(RepositoryTaskAttribute.USER_ASSIGNED);
		}
		
		issue.setAssignee(JiraRepositoryConnector.getAssigneeFromAttribute(assignee));
		issue.setEnvironment(taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT));
		issue.setId(taskData.getId());
		issue.setKey(taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_ISSUE_KEY));
		for (Priority priority : client.getPriorities()) {
			if (priority.getName().equals(taskData.getAttributeValue(RepositoryTaskAttribute.PRIORITY))) {
				issue.setPriority(priority);
				break;
			}
		}
		
		ArrayList<CustomField> customFields = new ArrayList<CustomField>();
		for (RepositoryTaskAttribute attr : taskData.getAttributes()) {
			if (attr.getID().startsWith(JiraAttributeFactory.ATTRIBUTE_CUSTOM_PREFIX)) {
				String id = attr.getID().substring(JiraAttributeFactory.ATTRIBUTE_CUSTOM_PREFIX.length());
				customFields.add(new CustomField(id, attr.getMetaDataValue(JiraAttributeFactory.TYPE_KEY), attr.getName(), attr.getValues()));
			}
		}
		issue.setCustomFields(customFields.toArray(new CustomField[customFields.size()]));
		
		return issue;
	}
}