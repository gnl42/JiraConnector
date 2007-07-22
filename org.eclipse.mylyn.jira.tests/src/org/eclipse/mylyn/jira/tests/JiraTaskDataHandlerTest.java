/*******************************************************************************
 * Copyright (c) 2005, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.mylyn.jira.tests;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.context.tests.support.TestUtil;
import org.eclipse.mylyn.context.tests.support.TestUtil.Credentials;
import org.eclipse.mylyn.context.tests.support.TestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.Issue;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Status;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.ui.JiraAttributeFactory;
import org.eclipse.mylyn.internal.jira.ui.JiraClientFacade;
import org.eclipse.mylyn.internal.jira.ui.JiraTaskDataHandler;
import org.eclipse.mylyn.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylyn.tasks.core.RepositoryTaskData;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;

/**
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 */
public class JiraTaskDataHandlerTest extends TestCase {

	private TaskRepository repository;

	private AbstractTaskDataHandler dataHandler;

	private JiraClient client;

	protected void init(String url) throws Exception {
		String kind = JiraUiPlugin.REPOSITORY_KIND;

		Credentials credentials = TestUtil.readCredentials(PrivilegeLevel.USER);

		repository = new TaskRepository(kind, url);
		repository.setAuthenticationCredentials(credentials.username, credentials.password);

		AbstractRepositoryConnector connector = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(kind);
		assertEquals(connector.getConnectorKind(), kind);

		TasksUiPlugin.getSynchronizationManager().setForceSyncExec(true);

		dataHandler = connector.getTaskDataHandler();

		client = JiraClientFacade.getDefault().getJiraClient(repository);
	}

	public void testGetTaskData() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		String commentText = "line1\nline2\n\nline4\n\n\n";
		Issue issue = JiraTestUtils.createIssue(client, "testUpdateTask");
		issue = client.createIssue(issue);

		client.addCommentToIssue(issue, commentText);

		RepositoryTaskData taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals(1, taskData.getComments().size());
		assertEquals(commentText, taskData.getComments().get(0).getText());
	}

	public void testCreateTaskData() throws JiraException {
		Issue issue = new Issue();
		issue.setProject(MockJiraClient.createProject());
		issue.setAssignee("eu");

		issue.setId("100");
		issue.setKey("FOO-1");
		issue.setAssignee("boo");
		issue.setSummary("summary");
		issue.setDescription("description");

		Status status = new Status();
		status.setId(Status.OPEN_ID);
		status.setName("open");
		issue.setStatus(status);

		Date created = new Date();
		issue.setCreated(created);

		Component[] components = new Component[] { MockJiraClient.createComponent("2", "component2"),
				MockJiraClient.createComponent("3", "component3") };
		issue.setComponents(components);

		Version[] fixVersions = new Version[] { MockJiraClient.createVersion("3", "3.0") };
		issue.setFixVersions(fixVersions);

		Version[] reportedVersions = new Version[] { MockJiraClient.createVersion("1", "1.0"),
				MockJiraClient.createVersion("2", "2.0") };
		issue.setReportedVersions(reportedVersions);

		issue.setType(MockJiraClient.createIssueType("3", "task"));

		issue.setPriority(MockJiraClient.createPriority(Priority.BLOCKER_ID, "blocker"));

		TaskRepository repository = new TaskRepository(JiraUiPlugin.REPOSITORY_KIND, "http://jira.codehaus.org/");
		MockJiraClient client = new MockJiraClient(repository.getUrl());
		JiraTaskDataHandler dataHandler = new JiraTaskDataHandler(new MockJiraClientFactory(client));
		RepositoryTaskData data = dataHandler.createTaskData(repository, client, issue, null);

		assertValues(data, RepositoryTaskAttribute.TASK_KEY, "FOO-1");
		assertValues(data, RepositoryTaskAttribute.STATUS, "open");
		assertValues(data, RepositoryTaskAttribute.PRIORITY, "blocker");
		assertValues(data, RepositoryTaskAttribute.PRODUCT, "Prone");
		assertValues(data, JiraAttributeFactory.ATTRIBUTE_TYPE, "task");

		assertValues(data, RepositoryTaskAttribute.DATE_CREATION, //
				new SimpleDateFormat(JiraAttributeFactory.JIRA_DATE_FORMAT).format(issue.getCreated()));

		assertValues(data, JiraAttributeFactory.ATTRIBUTE_COMPONENTS, "component2", "component3");
		assertValues(data, JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS, "1.0", "2.0");
		assertValues(data, JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS, "3.0");

		assertValues(data, RepositoryTaskAttribute.SUMMARY, "summary");
		assertValues(data, RepositoryTaskAttribute.DESCRIPTION, "description");
	}

	private void assertValues(RepositoryTaskData data, String key, String... values) {
		RepositoryTaskAttribute attribute = data.getAttribute(key);
		int n = 0;
		for (String value : attribute.getValues()) {
			assertEquals(values[n], value);
			n++;
		}
	}

}
