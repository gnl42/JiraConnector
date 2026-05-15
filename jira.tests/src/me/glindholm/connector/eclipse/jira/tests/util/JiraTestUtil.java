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

package me.glindholm.connector.eclipse.jira.tests.util;

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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.sdk.util.CommonTestUtil;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tests.util.TestUtil;
import org.eclipse.mylyn.tests.util.TestUtil.PrivilegeLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import junit.framework.AssertionFailedError;
import me.glindholm.connector.eclipse.internal.jira.core.JiraClientFactory;
import me.glindholm.connector.eclipse.internal.jira.core.JiraCorePlugin;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraAction;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssue;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueType;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProject;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraResolution;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClient;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClientData;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraException;
import me.glindholm.connector.eclipse.internal.jira.core.util.JiraUtil;
import me.glindholm.jira.rest.client.api.domain.BasicUser;

public class JiraTestUtil {

	// persist caching across test runs
	private static Map<String, JiraClientData> clientDataByUrl = new HashMap<>();

	public static String PROJECT1 = "PRONE";

	private static Map<JiraClient, List<JiraIssue>> testIssues = new HashMap<>();

	private static final JiraTestUtil instance = new JiraTestUtil();

	private JiraTestUtil() {
	}

	public static JiraIssue createIssue(final JiraClient client, final JiraIssue issue) throws JiraException {
		final var createdIssue = client.createIssue(issue, null);
		var list = testIssues.get(client);
		if (list == null) {
			list = new ArrayList<>();
			testIssues.put(client, list);
		}
		list.add(createdIssue);
		return createdIssue;
	}

	public static JiraIssue createIssue(final JiraClient client, final String summary) throws JiraException {
		final var issue = newIssue(client, summary);
		return createIssue(client, issue);
	}

	public static JiraIssue createIssueWithoutAssignee(final JiraClient client, final String summary) throws JiraException {
		final var issue = newIssue(client, summary);
		issue.setAssignee(null);
		return createIssue(client, issue);
	}

	public static IRepositoryQuery createQuery(final TaskRepository taskRepository, final JiraFilter filter) {
		final var query = TasksUi.getRepositoryModel().createRepositoryQuery(taskRepository);
		JiraUtil.setQuery(taskRepository, query, filter);
		return query;
	}

	public static ITask createTask(final TaskRepository taskRepository, final String taskId) throws Exception {
		final var connector = TasksUi.getRepositoryConnector(JiraCorePlugin.CONNECTOR_KIND);
		final TaskData taskData = connector.getTaskData(taskRepository, taskId, null);
		final var task = TasksUi.getRepositoryModel().createTask(taskRepository, taskData.getTaskId());
		TasksUiPlugin.getTaskDataManager().putUpdatedTaskData(task, taskData, true);
		return task;
	}

	public static JiraResolution getFixedResolution(final JiraClient server) throws JiraException {
		refreshDetails(server);

		final var resolutions = server.getCache().getResolutions();
		for (final JiraResolution resolution : resolutions) {
			if (JiraResolution.FIXED_ID.equals(resolution.getId())) {
				return resolution;
			}
		}
		return resolutions[0];
	}

	public static String getOperation(final JiraClient server, final String issueKey, final String name) throws JiraException {
		refreshDetails(server);

		final var names = new ArrayList<String>();
		final var actions = server.getAvailableActions(issueKey, null);
		for (final JiraAction action : actions) {
			names.add(action.getName());
			if (action.getName().toLowerCase().startsWith(name)) {
				return action.getId();
			}
		}

		throw new AssertionFailedError("Unable to find operation " + name + " in " + names);
	}

	public static JiraProject getProject(final JiraClient client, final String projectKey) {
		final var project = client.getCache().getProjectByKey(projectKey);
		if (project == null) {
			throw new AssertionFailedError("Project '" + projectKey + "' not found");
		}
		return project;
	}

	public static List<ITaskAttachment> getTaskAttachments(final ITask task) throws CoreException {
		final var taskData = TasksUi.getTaskDataManager().getTaskData(task);
		final List<ITaskAttachment> attachments = new ArrayList<>();
		final List<TaskAttribute> attributes = taskData.getAttributeMapper().getAttributesByType(taskData,
				TaskAttribute.TYPE_ATTACHMENT);
		if (attributes != null) {
			for (final TaskAttribute taskAttribute : attributes) {
				final var taskAttachment = TasksUiPlugin.getRepositoryModel().createTaskAttachment(taskAttribute);
				taskData.getAttributeMapper().updateTaskAttachment(taskAttachment, taskAttribute);
				attachments.add(taskAttachment);
			}
		}
		return attachments;
	}

	public static List<ITaskComment> getTaskComments(final ITask task) throws CoreException {
		final var taskData = TasksUi.getTaskDataManager().getTaskData(task);
		return getTaskComments(taskData);
	}

	public static List<ITaskComment> getTaskComments(final TaskData taskData) {
		final List<ITaskComment> comments = new ArrayList<>();
		final List<TaskAttribute> attributes = taskData.getAttributeMapper().getAttributesByType(taskData,
				TaskAttribute.TYPE_COMMENT);
		if (attributes != null) {
			for (final TaskAttribute taskAttribute : attributes) {
				final var taskComment = TasksUiPlugin.getRepositoryModel().createTaskComment(taskAttribute);
				taskData.getAttributeMapper().updateTaskComment(taskComment, taskAttribute);
				comments.add(taskComment);
			}
		}
		return comments;
	}

	public static TaskRepository init(final String url) {
		return init(url, PrivilegeLevel.USER);
	}

	public static TaskRepository init(final String url, final PrivilegeLevel level) {
		final var manager = TasksUiPlugin.getRepositoryManager();
		manager.clearRepositories(TasksUiPlugin.getDefault().getRepositoriesFilePath());

		final var credentials = TestUtil.readCredentials(level);
		final var repository = new TaskRepository(JiraCorePlugin.CONNECTOR_KIND, url);
		repository.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials(credentials.username,
				credentials.password), true);

		manager.addRepository(repository);
		return repository;
	}

	public static JiraIssue newIssue(final JiraClient client, final String summary) throws JiraException {
		refreshDetails(client);

		final var issue = new JiraIssue();
		issue.setProject(getProject(client, PROJECT1));
		issue.setType(client.getCache().getIssueTypes()[0]);
		issue.setSummary(summary);
		issue.setAssignee(new BasicUser(null, client.getUserName(), client.getUserName()));
		issue.setPriority(client.getCache().getPriorities()[0]);
		return issue;
	}

	public static JiraIssue newSubTask(final JiraClient client, final JiraIssue parent, final String summary) throws JiraException {
		refreshDetails(client);

		final var issue = new JiraIssue();
		final var project = getProject(client, PROJECT1);
		issue.setProject(project);
		issue.setParentId(parent.getId());
		issue.setSummary(summary);
		issue.setAssignee(new BasicUser(null, client.getUserName(), client.getUserName()));
		issue.setPriority(client.getCache().getPriorities()[0]);
		for (final JiraIssueType type : project.getIssueTypes()) {
			if (type.isSubTaskType()) {
				issue.setType(type);
				return issue;
			}
		}
		throw new JiraException("No subtask type found for project '" + project.getKey() + "'");
	}

	public static byte[] readFile(final File file) throws IOException {
		if (file.length() > 10000000) {
			throw new IOException("File too big: " + file.getAbsolutePath() + ", size: " + file.length());
		}

		final var data = new byte[(int) file.length()];
		final InputStream in = new FileInputStream(file);
		try (in) {
			in.read(data);
		}
		return data;
	}

	public static void refreshDetails(final JiraClient client) throws JiraException {
		if (!client.getCache().hasDetails()) {
			final var data = clientDataByUrl.get(client.getBaseUrl());
			if (data != null) {
				client.getCache().setData(data);
			} else {
				client.getCache().refreshDetails(new NullProgressMonitor());
				clientDataByUrl.put(client.getBaseUrl(), client.getCache().getData());
			}
		}
	}

	@BeforeEach
	public static void setUp() {
		JiraClientFactory.getDefault().clearClients();
	}

	@AfterEach
	public static void tearDown() throws JiraException {
		for (final JiraClient client : testIssues.keySet()) {
			for (final JiraIssue issue : testIssues.get(client)) {
				client.deleteIssue(issue, null);
			}
		}
		testIssues.clear();
	}

	public static void writeFile(final File file, final byte[] data) throws IOException {
		final OutputStream out = new FileOutputStream(file);
		try (out) {
			out.write(data);
		}
	}

	public static String getMessage(final String filename) throws Exception {
		final var file = CommonTestUtil.getFile(instance, "testdata/" + filename + ".txt");
		return CommonTestUtil.read(file);
	}

}
