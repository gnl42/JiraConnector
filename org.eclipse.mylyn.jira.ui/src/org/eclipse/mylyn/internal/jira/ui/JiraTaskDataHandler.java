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
import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylar.internal.jira.core.model.Comment;
import org.eclipse.mylar.internal.jira.core.model.Component;
import org.eclipse.mylar.internal.jira.core.model.Issue;
import org.eclipse.mylar.internal.jira.core.model.IssueType;
import org.eclipse.mylar.internal.jira.core.model.Priority;
import org.eclipse.mylar.internal.jira.core.model.Resolution;
import org.eclipse.mylar.internal.jira.core.model.Status;
import org.eclipse.mylar.internal.jira.core.model.Version;
import org.eclipse.mylar.internal.jira.core.service.JiraServer;
import org.eclipse.mylar.internal.jira.ui.html.HTML2TextReader;
import org.eclipse.mylar.tasks.core.AbstractAttributeFactory;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.ITaskDataHandler;
import org.eclipse.mylar.tasks.core.RepositoryOperation;
import org.eclipse.mylar.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylar.tasks.core.RepositoryTaskData;
import org.eclipse.mylar.tasks.core.TaskComment;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;

/**
 * @author Mik Kersten
 * @author Rob Elves
 * @author Eugene Kuleshov
 */
public class JiraTaskDataHandler implements ITaskDataHandler {

	private AbstractAttributeFactory attributeFactory = new JiraAttributeFactory();

	public static final String JIRA_DATE_FORMAT = "dd MMM yyyy HH:mm:ss z";

	private static final JiraAttributeFactory attributeFacotry = new JiraAttributeFactory();

	public JiraTaskDataHandler(JiraRepositoryConnector connector) {
		// this.connector = connector;
	}

	public RepositoryTaskData getTaskData(TaskRepository repository, String taskId) throws CoreException {
		JiraServer server = JiraServerFacade.getDefault().getJiraServer(repository);
		Issue jiraIssue = getJiraIssue(server, taskId, repository.getUrl());
		if (jiraIssue != null) {
			// TODO: remove this call?
			// JiraRepositoryConnector.updateTaskDetails(repository.getUrl(), (JiraTask) task, jiraIssue, false);

			RepositoryTaskData data = new RepositoryTaskData(attributeFactory, JiraUiPlugin.REPOSITORY_KIND,
					repository.getUrl(), taskId);
			// connector.updateAttributes(repository, new
			// NullProgressMonitor());
			updateTaskData(data, jiraIssue, server);
			addOperations(jiraIssue, data);
			return data;
		}
		return null;
	}

	private Issue getJiraIssue(JiraServer server, String taskId, String repositoryUrl) {
		try {
//			int id = Integer.parseInt(taskId);
//			String handle = AbstractRepositoryTask.getHandle(repositoryUrl, id);
			ITask task = TasksUiPlugin.getTaskListManager().getTaskList().getTask(repositoryUrl, taskId);
			if (task instanceof JiraTask) {
				JiraTask jiraTask = (JiraTask) task;
				return server.getIssue(jiraTask.getKey());
			}
		} catch (NumberFormatException e) {
			return server.getIssue(taskId);
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	private void updateTaskData(RepositoryTaskData data, Issue jiraIssue, JiraServer server) {
		data.removeAllAttributes();
		RepositoryTaskAttribute attribute = new RepositoryTaskAttribute(RepositoryTaskAttribute.DATE_CREATION,
				"Created: ", true);
		attribute.setValue(jiraIssue.getCreated().toGMTString());
		data.addAttribute(RepositoryTaskAttribute.DATE_CREATION, attribute);

		attribute = new RepositoryTaskAttribute(RepositoryTaskAttribute.SUMMARY, "Summary: ", true);
		attribute.setValue(convertHtml(jiraIssue.getSummary()));
		data.addAttribute(RepositoryTaskAttribute.SUMMARY, attribute);

		attribute = new RepositoryTaskAttribute(RepositoryTaskAttribute.DESCRIPTION, "Description: ", true);
		attribute.setValue(convertHtml(jiraIssue.getDescription()));
		attribute.setReadOnly(true);
		data.addAttribute(RepositoryTaskAttribute.DESCRIPTION, attribute);

		attribute = new RepositoryTaskAttribute(RepositoryTaskAttribute.STATUS, "Status: ", true);
		attribute.setValue(convertHtml(jiraIssue.getStatus().getName()));
		data.addAttribute(RepositoryTaskAttribute.STATUS, attribute);

		attribute = new RepositoryTaskAttribute(JiraAttributeFactory.ATTRIBUTE_ISSUE_KEY, "Issue ID: ", true);
		attribute.setValue(jiraIssue.getKey());
		data.addAttribute(JiraAttributeFactory.ATTRIBUTE_ISSUE_KEY, attribute);

		attribute = new RepositoryTaskAttribute(RepositoryTaskAttribute.RESOLUTION, "Resolution: ", true);
		attribute.setValue(jiraIssue.getResolution() == null ? "" : jiraIssue.getResolution().getName());
		data.addAttribute(RepositoryTaskAttribute.RESOLUTION, attribute);

		attribute = new RepositoryTaskAttribute(RepositoryTaskAttribute.USER_ASSIGNED, "Assigned to: ", true);
		attribute.setValue(jiraIssue.getAssignee());
		data.addAttribute(RepositoryTaskAttribute.USER_ASSIGNED, attribute);

		attribute = new RepositoryTaskAttribute(RepositoryTaskAttribute.USER_REPORTER, "Reported by: ", true);
		attribute.setValue(jiraIssue.getReporter());
		data.addAttribute(RepositoryTaskAttribute.USER_REPORTER, attribute);

		attribute = new RepositoryTaskAttribute(RepositoryTaskAttribute.DATE_MODIFIED, "Date modified: ", true);
		attribute.setValue(jiraIssue.getUpdated().toGMTString());
		data.addAttribute(RepositoryTaskAttribute.DATE_MODIFIED, attribute);

		attribute = new RepositoryTaskAttribute(JiraAttributeFactory.ATTRIBUTE_COMPONENTS, "Components: ", true);
		for (Component component : jiraIssue.getComponents()) {
			attribute.addValue(component.getName());
		}
		for (Component component : jiraIssue.getProject().getComponents()) {
			attribute.addOption(component.getName(), component.getId());

		}
		data.addAttribute(JiraAttributeFactory.ATTRIBUTE_COMPONENTS, attribute);

		attribute = new RepositoryTaskAttribute(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS, "Fix Versions: ", true);
		for (Version version : jiraIssue.getFixVersions()) {
			attribute.addValue(version.getName());
		}
		for (Version version : jiraIssue.getProject().getVersions()) {
			attribute.addOption(version.getName(), version.getId());
		}
		data.addAttribute(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS, attribute);

		attribute = new RepositoryTaskAttribute(JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS, "Affects Versions: ",
				true);
		for (Version version : jiraIssue.getReportedVersions()) {
			attribute.addValue(version.getName());
		}
		for (Version version : jiraIssue.getProject().getVersions()) {
			attribute.addOption(version.getName(), version.getId());
		}
		data.addAttribute(JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS, attribute);

		attribute = new RepositoryTaskAttribute(JiraAttributeFactory.ATTRIBUTE_ESTIMATE, "Estimate: ", true);
		attribute.setValue(String.valueOf(jiraIssue.getEstimate()));
		data.addAttribute(JiraAttributeFactory.ATTRIBUTE_ESTIMATE, attribute);

		// VISIBLE FIELDS (order added = order in layout)

		attribute = new RepositoryTaskAttribute(RepositoryTaskAttribute.PRODUCT, "Project: ", false);
		attribute.setValue(jiraIssue.getProject().getName());
		attribute.setReadOnly(true);
		data.addAttribute(RepositoryTaskAttribute.PRODUCT, attribute);

		attribute = new RepositoryTaskAttribute(RepositoryTaskAttribute.PRIORITY, "Priority: ", false);
		attribute.setValue(jiraIssue.getPriority().getName());
		for (Priority priority : server.getPriorities()) {
			attribute.addOption(priority.getName(), priority.getId());
		}
		data.addAttribute(RepositoryTaskAttribute.PRIORITY, attribute);

		attribute = new RepositoryTaskAttribute(JiraAttributeFactory.ATTRIBUTE_TYPE, "Type: ", false);
		attribute.setValue(jiraIssue.getType().getName());
		for (IssueType type : server.getIssueTypes()) {
			attribute.addOption(type.getName(), type.getId());
		}
		data.addAttribute(JiraAttributeFactory.ATTRIBUTE_TYPE, attribute);

		attribute = new RepositoryTaskAttribute(JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT, "Environment: ", false);
		attribute.setValue(convertHtml(jiraIssue.getEnvironment()));
		data.addAttribute(JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT, attribute);

		int x = 1;
		for (Comment comment : jiraIssue.getComments()) {
			if (comment != null) {
				TaskComment taskComment = new TaskComment(attributeFacotry, x++);

				attribute = new RepositoryTaskAttribute(RepositoryTaskAttribute.USER_OWNER, "Commenter: ", true);
				attribute.setValue(comment.getAuthor());
				taskComment.addAttribute(RepositoryTaskAttribute.USER_OWNER, attribute);

				attribute = new RepositoryTaskAttribute(RepositoryTaskAttribute.COMMENT_TEXT, "Text: ", true);
				attribute.setValue(convertHtml(comment.getComment()));
				attribute.setReadOnly(true);
				taskComment.addAttribute(RepositoryTaskAttribute.COMMENT_TEXT, attribute);

				attribute = new RepositoryTaskAttribute(RepositoryTaskAttribute.COMMENT_DATE, "Text: ", true);
				attribute.setValue(comment.getCreated().toGMTString());
				taskComment.addAttribute(RepositoryTaskAttribute.COMMENT_DATE, attribute);

				data.addComment(taskComment);

			}
		}

	}

	public Date getDateForAttributeType(String attributeKey, String dateString) {
		if (dateString == null || dateString.equals("")) {
			return null;
		}
		try {
			// String mappedKey =
			// attributeFactory.mapCommonAttributeKey(attributeKey);
			// Date parsedDate = null;
			// if (mappedKey.equals(RepositoryTaskAttribute.DATE_MODIFIED)) {
			// parsedDate = modified_ts_format.parse(dateString);
			// } else if
			// (mappedKey.equals(RepositoryTaskAttribute.DATE_CREATION)) {
			// parsedDate = creation_ts_format.parse(dateString);
			// }
			// return parsedDate;
			return new SimpleDateFormat(JIRA_DATE_FORMAT).parse(dateString);
		} catch (Exception e) {
			MylarStatusHandler.log(e, "Error while parsing date field");
			return null;
		}
	}

	@SuppressWarnings("restriction")
	private String convertHtml(String text) {
		if (text == null || text.length() == 0) {
			return "";
		}
		StringReader stringReader = new StringReader(text);
		HTML2TextReader html2TextReader = new HTML2TextReader(stringReader, null);
		try {
			char[] chars = new char[text.length()];
			int len = html2TextReader.read(chars, 0, text.length());
			return new String(chars, 0, len).trim();
		} catch (IOException e) {
			return text;
		}
	}

	public AbstractAttributeFactory getAttributeFactory() {
		return attributeFactory;
	}


	private void addOperations(Issue issue, RepositoryTaskData data) {
		Status status = issue.getStatus();

		RepositoryOperation opLeave = new RepositoryOperation("leave", "Leave as " + issue.getStatus().getName());
		// RepositoryOperation opStart = new
		// RepositoryOperation(Status.STARTED_ID, "Start");
		// RepositoryOperation opStop = new RepositoryOperation(Status.OPEN_ID,
		// "Stop (open)");
		RepositoryOperation opReopen = new RepositoryOperation(Status.REOPENED_ID, "Reopen");

		RepositoryOperation opResolve = new RepositoryOperation(Status.RESOLVED_ID, "Resolve");
		opResolve.setUpOptions("resolution");
		opResolve.addOption("Fixed", Resolution.FIXED_ID);
		opResolve.addOption("Won't Fix", Resolution.WONT_FIX_ID);
		opResolve.addOption("Duplicate", Resolution.DUPLICATE_ID);
		opResolve.addOption("Incomplete", Resolution.INCOMPLETE_ID);
		opResolve.addOption("Cannot Reproduce", Resolution.CANNOT_REPRODUCE_ID);

		RepositoryOperation opClose = new RepositoryOperation(Status.CLOSED_ID, "Close");
		opClose.setUpOptions("resolution");
		opClose.addOption("Fixed", Resolution.FIXED_ID);
		opClose.addOption("Won't Fix", Resolution.WONT_FIX_ID);
		opClose.addOption("Duplicate", Resolution.DUPLICATE_ID);
		opClose.addOption("Incomplete", Resolution.INCOMPLETE_ID);
		opClose.addOption("Cannot Reproduce", Resolution.CANNOT_REPRODUCE_ID);

		opLeave.setChecked(true);
		data.addOperation(opLeave);
		if (status.getId().equals(Status.OPEN_ID) || status.getId().equals(Status.STARTED_ID)) {
			data.addOperation(opResolve);
			data.addOperation(opClose);
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
		} else if (status.getId().equals(Status.CLOSED_ID)) {
			data.addOperation(opReopen);
		}
	}

	public String postTaskData(TaskRepository repository, RepositoryTaskData taskData) throws CoreException {

		final JiraServer jiraServer = JiraServerFacade.getDefault().getJiraServer(repository);
		if (jiraServer == null) {
			throw new CoreException(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.Status.ERROR,
					JiraCorePlugin.ID, org.eclipse.core.runtime.Status.ERROR, "Unable to produce Jira Server", null));
		}

		Issue issue = JiraRepositoryConnector.buildJiraIssue(taskData, jiraServer);

		RepositoryOperation operation = taskData.getSelectedOperation();
		if (operation != null) {
			if ("leave".equals(operation.getKnobName())) {
				if (!issue.getStatus().isClosed()) {
					jiraServer.updateIssue(issue, taskData.getNewComment());
				} else if (taskData.getNewComment() != null && taskData.getNewComment().length() > 0) {
					jiraServer.addCommentToIssue(issue, taskData.getNewComment());
				}
			} else if (org.eclipse.mylar.internal.jira.core.model.Status.RESOLVED_ID.equals(operation.getKnobName())) {
				String value = operation.getOptionValue(operation.getOptionSelection());
				jiraServer.resolveIssue(issue, jiraServer.getResolutionById(value), issue.getFixVersions(), taskData
						.getNewComment(), JiraServer.ASSIGNEE_CURRENT, repository.getUserName());
			} else if (org.eclipse.mylar.internal.jira.core.model.Status.REOPENED_ID.equals(operation.getKnobName())) {
				jiraServer.reopenIssue(issue, taskData.getNewComment(), JiraServer.ASSIGNEE_CURRENT, repository
						.getUserName());
			} else if (org.eclipse.mylar.internal.jira.core.model.Status.STARTED_ID.equals(operation.getKnobName())) {
				jiraServer.startIssue(issue, taskData.getNewComment(), repository.getUserName());
			} else if (org.eclipse.mylar.internal.jira.core.model.Status.OPEN_ID.equals(operation.getKnobName())) {
				jiraServer.startIssue(issue, taskData.getNewComment(), repository.getUserName());
			} else if (org.eclipse.mylar.internal.jira.core.model.Status.CLOSED_ID.equals(operation.getKnobName())) {
				String value = operation.getOptionValue(operation.getOptionSelection());
				jiraServer.closeIssue(issue, jiraServer.getResolutionById(value), issue.getFixVersions(), taskData
						.getNewComment(), JiraServer.ASSIGNEE_CURRENT, repository.getUserName());
			}
		} else {
			jiraServer.updateIssue(issue, taskData.getNewComment());
		}
		return "";
	}
}