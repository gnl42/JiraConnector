/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.jira;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.internal.text.html.*;
import org.eclipse.mylar.context.core.MylarStatusHandler;
import org.eclipse.mylar.tasks.core.AbstractAttributeFactory;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask;
import org.eclipse.mylar.tasks.core.IOfflineTaskHandler;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.RepositoryOperation;
import org.eclipse.mylar.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylar.tasks.core.RepositoryTaskData;
import org.eclipse.mylar.tasks.core.TaskComment;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.tigris.jira.core.model.Comment;
import org.tigris.jira.core.model.Component;
import org.tigris.jira.core.model.Issue;
import org.tigris.jira.core.model.IssueType;
import org.tigris.jira.core.model.Priority;
import org.tigris.jira.core.model.Resolution;
import org.tigris.jira.core.model.Status;
import org.tigris.jira.core.model.Version;
import org.tigris.jira.core.service.JiraServer;

/**
 * @author Mik Kersten
 * @author Rob Elves
 */
public class JiraOfflineTaskHandler implements IOfflineTaskHandler {

	private AbstractAttributeFactory attributeFactory = new JiraAttributeFactory();

	private static final String DATE_FORMAT_1 = "dd MMM yyyy HH:mm:ss z";

	private static SimpleDateFormat creation_ts_format = new SimpleDateFormat(DATE_FORMAT_1);

	private static SimpleDateFormat modified_ts_format = new SimpleDateFormat(DATE_FORMAT_1);

	private JiraRepositoryConnector connector;

	private static final JiraAttributeFactory attributeFacotry = new JiraAttributeFactory();

	public JiraOfflineTaskHandler(JiraRepositoryConnector connector) {
		this.connector = connector;
	}

	public RepositoryTaskData downloadTaskData(TaskRepository repository, String taskId, Proxy proxySettings)
			throws CoreException {
		JiraServer server = JiraServerFacade.getDefault().getJiraServer(repository);
		String handle = AbstractRepositoryTask.getHandle(repository.getUrl(), taskId);

		ITask task = TasksUiPlugin.getTaskListManager().getTaskList().getTask(handle);
		if (task instanceof JiraTask) {
			JiraTask jiraTask = (JiraTask) task;
			Issue jiraIssue = server.getIssue(jiraTask.getKey());
			RepositoryTaskData data = new RepositoryTaskData(attributeFactory, MylarJiraPlugin.REPOSITORY_KIND,
					repository.getUrl(), taskId);
			connector.updateAttributes(repository, new NullProgressMonitor());
			updateTaskData(data, jiraIssue, server);
			addOperations(jiraIssue, data);
			return data;
		} else {
			return null;
		}
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

		// VISIBLE FIELDS (order added = order in layout)

		attribute = new RepositoryTaskAttribute(RepositoryTaskAttribute.PRODUCT, "Project: ", false);
		attribute.setValue(jiraIssue.getProject().getName());
		attribute.setReadOnly(true);
		data.addAttribute(RepositoryTaskAttribute.PRODUCT, attribute);

		attribute = new RepositoryTaskAttribute(RepositoryTaskAttribute.PRIORITY, "Priority: ", false);
		attribute.setValue(jiraIssue.getPriority().getName());
		for (Priority priority : server.getPriorities()) {
			attribute.addOptionValue(priority.getName(), priority.getId());
		}
		data.addAttribute(RepositoryTaskAttribute.PRIORITY, attribute);

		attribute = new RepositoryTaskAttribute(JiraAttributeFactory.ATTRIBUTE_TYPE, "Type: ", false);
		attribute.setValue(jiraIssue.getType().getName());
		for (IssueType type : server.getIssueTypes()) {
			attribute.addOptionValue(type.getName(), type.getId());
		}
		data.addAttribute(JiraAttributeFactory.ATTRIBUTE_TYPE, attribute);

		attribute = new RepositoryTaskAttribute(JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT, "Environment: ", false);
		attribute.setValue(jiraIssue.getEnvironment());
		data.addAttribute(JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT, attribute);

		attribute = new RepositoryTaskAttribute(JiraAttributeFactory.ATTRIBUTE_COMPONENTS, "Components: ", true);
		for (Component component: jiraIssue.getComponents()) {
			attribute.addValue(component.getName());
		}
		for (Component component : jiraIssue.getProject().getComponents()) {
			attribute.addOptionValue(component.getName(), component.getId());
			
		}
		data.addAttribute(JiraAttributeFactory.ATTRIBUTE_COMPONENTS, attribute);
		
		attribute = new RepositoryTaskAttribute(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS, "Fix Versions: ", true);
		for (Version version: jiraIssue.getFixVersions()) {
			attribute.addValue(version.getName());
		}
		for (Version version: jiraIssue.getProject().getVersions()) {
			attribute.addOptionValue(version.getName(), version.getId());
		}
		data.addAttribute(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS, attribute);
		
		int x = 0;
		for (Comment comment : jiraIssue.getComments()) {
			if (comment != null) {
				TaskComment taskComment = new TaskComment(attributeFacotry, data, x++);

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
			String mappedKey = attributeFactory.mapCommonAttributeKey(attributeKey);
			Date parsedDate = null;
			if (mappedKey.equals(RepositoryTaskAttribute.DATE_MODIFIED)) {
				parsedDate = modified_ts_format.parse(dateString);
			} else if (mappedKey.equals(RepositoryTaskAttribute.DATE_CREATION)) {
				parsedDate = creation_ts_format.parse(dateString);
			}
			return parsedDate;
		} catch (Exception e) {
			MylarStatusHandler.log(e, "Error while parsing date field");
			return null;
		}
	}

	@SuppressWarnings("restriction")
	private String convertHtml(String text) {
		StringReader stringReader = new StringReader(text);
		HTML2TextReader html2TextReader = new HTML2TextReader(stringReader, null);
		try {
			char[] chars = new char[text.length()];
			html2TextReader.read(chars, 0, text.length());
			return new String(chars).trim();
		} catch (IOException e) {
			return text;
		}
		// return text.replace("<br/>", "").replace("&nbsp;",
		// "").replace("\n\n", "\n");
	}

	public AbstractAttributeFactory getAttributeFactory() {
		return attributeFactory;
	}

	public Set<AbstractRepositoryTask> getChangedSinceLastSync(TaskRepository repository,
			Set<AbstractRepositoryTask> tasks, Proxy proxySettings) throws CoreException, UnsupportedEncodingException {
//		JiraServer server = JiraServerFacade.getDefault().getJiraServer(repository);
//		if (server == null) {
//			return Collections.emptySet();
//		} else {
//			List<AbstractRepositoryTask> changedTasks = new ArrayList<AbstractRepositoryTask>();
//			for (AbstractRepositoryTask task : tasks) {
//				if (task instanceof JiraTask) {
//					Date lastCommentDate = null;
//					JiraTask jiraTask = (JiraTask) task;
//					Issue issue = server.getIssue(jiraTask.getKey());
//					if (issue != null) {
//						Comment[] comments = issue.getComments();
//						if (comments != null && comments.length > 0) {
//							lastCommentDate = comments[comments.length - 1].getCreated();
//						}
//					}
//					if (lastCommentDate != null && task.getLastSyncDateStamp()() != null) {
//						if (lastCommentDate.after(task.getLastSynchronized())) {
//							changedTasks.add(task);
//						}
//					}
//				}
//			}
//		}
		return Collections.emptySet();
	}

	private void addOperations(Issue issue, RepositoryTaskData data) {
		Status status = issue.getStatus();
		if (status.isStarted() || status.isReopened()) {
			RepositoryOperation op = new RepositoryOperation("leave", "Leave as " + issue.getStatus().getName());
			op.setChecked(true);
			data.addOperation(op);
			op = new RepositoryOperation(Status.RESOLVED_ID, "Resolve");
			op.setUpOptions("resolution");
			op.addOption("Fixed", Resolution.FIXED_ID);
			op.addOption("Won't Fix", Resolution.WONT_FIX_ID);
			op.addOption("Duplicate", Resolution.DUPLICATE_ID);
			op.addOption("Incomplete", Resolution.INCOMPLETE_ID);
			op.addOption("Cannot Reproduce", Resolution.CANNOT_REPRODUCE_ID);
			data.addOperation(op);
			
			op = new RepositoryOperation(Status.CLOSED_ID, "Close");
			op.setUpOptions("resolution");
			op.addOption("Fixed", Resolution.FIXED_ID);
			op.addOption("Won't Fix", Resolution.WONT_FIX_ID);
			op.addOption("Duplicate", Resolution.DUPLICATE_ID);
			op.addOption("Incomplete", Resolution.INCOMPLETE_ID);
			op.addOption("Cannot Reproduce", Resolution.CANNOT_REPRODUCE_ID);
			data.addOperation(op);

		} else if (status.isClosed() || status.isResolved()) {
			RepositoryOperation op = new RepositoryOperation("leave", "Leave as " + issue.getStatus().getName());
			op.setChecked(true);
			data.addOperation(op);
			data.addOperation(new RepositoryOperation(Status.OPEN_ID, "Open"));
		}
	}
}
