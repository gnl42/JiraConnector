/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.jira.tests.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.AssertionFailedError;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.tests.support.CommonTestUtil;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tests.util.TestUtil;
import org.eclipse.mylyn.tests.util.TestUtil.Credentials;
import org.eclipse.mylyn.tests.util.TestUtil.PrivilegeLevel;

import com.atlassian.connector.eclipse.internal.jira.core.JiraClientFactory;
import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;
import com.atlassian.connector.eclipse.internal.jira.core.model.CustomField;
import com.atlassian.connector.eclipse.internal.jira.core.model.IssueType;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraAction;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue;
import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.model.Resolution;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClientData;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.connector.eclipse.internal.jira.core.util.JiraUtil;

public class JiraTestUtil {

	// persist caching across test runs
	private static Map<String, JiraClientData> clientDataByUrl = new HashMap<String, JiraClientData>();

	public static String PROJECT1 = "PRONE";

	private static Map<JiraClient, List<JiraIssue>> testIssues = new HashMap<JiraClient, List<JiraIssue>>();

	private static final JiraTestUtil instance = new JiraTestUtil();

	private JiraTestUtil() {
	}

	public static JiraIssue createIssue(JiraClient client, JiraIssue issue) throws JiraException {
		issue = client.createIssue(issue, null);
		List<JiraIssue> list = testIssues.get(client);
		if (list == null) {
			list = new ArrayList<JiraIssue>();
			testIssues.put(client, list);
		}
		list.add(issue);
		return issue;
	}

	public static JiraIssue createIssue(JiraClient client, String summary) throws JiraException {
		JiraIssue issue = newIssue(client, summary);
		return createIssue(client, issue);
	}

	public static IRepositoryQuery createQuery(TaskRepository taskRepository, JiraFilter filter) {
		IRepositoryQuery query = TasksUi.getRepositoryModel().createRepositoryQuery(taskRepository);
		JiraUtil.setQuery(taskRepository, query, filter);
		return query;
	}

	public static ITask createTask(TaskRepository taskRepository, String taskId) throws Exception {
		AbstractRepositoryConnector connector = TasksUi.getRepositoryConnector(JiraCorePlugin.CONNECTOR_KIND);
		TaskData taskData = connector.getTaskData(taskRepository, taskId, null);
		ITask task = TasksUi.getRepositoryModel().createTask(taskRepository, taskData.getTaskId());
		TasksUiPlugin.getTaskDataManager().putUpdatedTaskData(task, taskData, true);
		return task;
	}

	public static String getCustomField(JiraClient server, String name) throws JiraException {
		refreshDetails(server);

		CustomField[] fields = server.getCustomAttributes(null);
		for (CustomField field : fields) {
			if (field.getName().toLowerCase().startsWith(name.toLowerCase())) {
				return field.getId();
			}
		}
		return null;
	}

	public static Resolution getFixedResolution(JiraClient server) throws JiraException {
		refreshDetails(server);

		Resolution[] resolutions = server.getCache().getResolutions();
		for (Resolution resolution : resolutions) {
			if (Resolution.FIXED_ID.equals(resolution.getId())) {
				return resolution;
			}
		}
		return resolutions[0];
	}

	public static String getOperation(JiraClient server, String issueKey, String name) throws JiraException {
		refreshDetails(server);

		ArrayList<String> names = new ArrayList<String>();
		JiraAction[] actions = server.getAvailableActions(issueKey, null);
		for (JiraAction action : actions) {
			names.add(action.getName());
			if (action.getName().toLowerCase().startsWith(name)) {
				return action.getId();
			}
		}

		throw new AssertionFailedError("Unable to find operation " + name + " in " + names);
	}

	public static Project getProject(JiraClient client, String projectKey) {
		Project project = client.getCache().getProjectByKey(projectKey);
		if (project == null) {
			throw new AssertionFailedError("Project '" + projectKey + "' not found");
		}
		return project;
	}

	public static List<ITaskAttachment> getTaskAttachments(ITask task) throws CoreException {
		TaskData taskData = TasksUi.getTaskDataManager().getTaskData(task);
		List<ITaskAttachment> attachments = new ArrayList<ITaskAttachment>();
		List<TaskAttribute> attributes = taskData.getAttributeMapper().getAttributesByType(taskData,
				TaskAttribute.TYPE_ATTACHMENT);
		if (attributes != null) {
			for (TaskAttribute taskAttribute : attributes) {
				ITaskAttachment taskAttachment = TasksUiPlugin.getRepositoryModel().createTaskAttachment(taskAttribute);
				taskData.getAttributeMapper().updateTaskAttachment(taskAttachment, taskAttribute);
				attachments.add(taskAttachment);
			}
		}
		return attachments;
	}

	public static List<ITaskComment> getTaskComments(ITask task) throws CoreException {
		TaskData taskData = TasksUi.getTaskDataManager().getTaskData(task);
		return getTaskComments(taskData);
	}

	public static List<ITaskComment> getTaskComments(TaskData taskData) {
		List<ITaskComment> comments = new ArrayList<ITaskComment>();
		List<TaskAttribute> attributes = taskData.getAttributeMapper().getAttributesByType(taskData,
				TaskAttribute.TYPE_COMMENT);
		if (attributes != null) {
			for (TaskAttribute taskAttribute : attributes) {
				ITaskComment taskComment = TasksUiPlugin.getRepositoryModel().createTaskComment(taskAttribute);
				taskData.getAttributeMapper().updateTaskComment(taskComment, taskAttribute);
				comments.add(taskComment);
			}
		}
		return comments;
	}

	public static TaskRepository init(String url) {
		return init(url, PrivilegeLevel.USER);
	}

	public static TaskRepository init(String url, PrivilegeLevel level) {
		TaskRepositoryManager manager = TasksUiPlugin.getRepositoryManager();
		manager.clearRepositories(TasksUiPlugin.getDefault().getRepositoriesFilePath());

		Credentials credentials = TestUtil.readCredentials(level);
		TaskRepository repository = new TaskRepository(JiraCorePlugin.CONNECTOR_KIND, url);
		repository.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials(credentials.username,
				credentials.password), false);

		manager.addRepository(repository);
		return repository;
	}

	public static JiraIssue newIssue(JiraClient client, String summary) throws JiraException {
		refreshDetails(client);

		JiraIssue issue = new JiraIssue();
		issue.setProject(getProject(client, PROJECT1));
		issue.setType(client.getCache().getIssueTypes()[0]);
		issue.setSummary(summary);
		issue.setAssignee(client.getUserName());
		return issue;
	}

	public static JiraIssue newSubTask(JiraClient client, JiraIssue parent, String summary) throws JiraException {
		refreshDetails(client);

		JiraIssue issue = new JiraIssue();
		Project project = getProject(client, PROJECT1);
		issue.setProject(project);
		issue.setParentId(parent.getId());
		issue.setSummary(summary);
		issue.setAssignee(client.getUserName());
		for (IssueType type : project.getIssueTypes()) {
			if (type.isSubTaskType()) {
				issue.setType(type);
				return issue;
			}
		}
		throw new JiraException("No subtask type found for project '" + project.getKey() + "'");
	}

	public static byte[] readFile(File file) throws IOException {
		if (file.length() > 10000000) {
			throw new IOException("File too big: " + file.getAbsolutePath() + ", size: " + file.length());
		}

		byte[] data = new byte[(int) file.length()];
		InputStream in = new FileInputStream(file);
		try {
			in.read(data);
		} finally {
			in.close();
		}
		return data;
	}

	public static void refreshDetails(JiraClient client) throws JiraException {
		if (!client.getCache().hasDetails()) {
			JiraClientData data = clientDataByUrl.get(client.getBaseUrl());
			if (data != null) {
				client.getCache().setData(data);
			} else {
				client.getCache().refreshDetails(new NullProgressMonitor());
				clientDataByUrl.put(client.getBaseUrl(), client.getCache().getData());
			}
		}
	}

	public static void setUp() {
		JiraClientFactory.getDefault().clearClients();
	}

	public static void tearDown() throws JiraException {
		for (JiraClient client : testIssues.keySet()) {
			for (JiraIssue issue : testIssues.get(client)) {
				client.deleteIssue(issue, null);
			}
		}
		testIssues.clear();
	}

	public static void writeFile(File file, byte[] data) throws IOException {
		OutputStream out = new FileOutputStream(file);
		try {
			out.write(data);
		} finally {
			out.close();
		}
	}

	public static String getMessage(String filename) throws Exception {
		File file = CommonTestUtil.getFile(instance, "testdata/" + filename + ".txt");
		return CommonTestUtil.read(file);
	}

}
