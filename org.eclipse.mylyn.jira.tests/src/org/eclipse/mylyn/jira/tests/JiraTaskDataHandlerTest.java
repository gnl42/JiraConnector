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
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.context.tests.support.TestUtil;
import org.eclipse.mylyn.context.tests.support.TestUtil.Credentials;
import org.eclipse.mylyn.context.tests.support.TestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.CustomField;
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
import org.eclipse.mylyn.tasks.core.AbstractAttributeFactory;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.RepositoryOperation;
import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylyn.tasks.core.RepositoryTaskData;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;

/**
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 */
public class JiraTaskDataHandlerTest extends TestCase {

	private TaskRepository repository;

	private JiraTaskDataHandler dataHandler;

	private JiraClient client;

	private AbstractRepositoryConnector connector;

	private String customFieldId;

	private String customFieldName;

	private TaskRepositoryManager manager;

	@Override
	protected void setUp() throws Exception {
		manager = TasksUiPlugin.getRepositoryManager();
		manager.clearRepositories(TasksUiPlugin.getDefault().getRepositoriesFilePath());
		
		JiraClientFacade.getDefault().clearClients();
	}
	
	protected void init(String url) throws Exception {
		String kind = JiraUiPlugin.REPOSITORY_KIND;

		Credentials credentials = TestUtil.readCredentials(PrivilegeLevel.USER);

		repository = new TaskRepository(kind, url);
		repository.setAuthenticationCredentials(credentials.username, credentials.password);
		manager.addRepository(repository, TasksUiPlugin.getDefault().getRepositoriesFilePath());
		
		connector = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(kind);
		assertEquals(connector.getConnectorKind(), kind);

		TasksUiPlugin.getSynchronizationManager().setForceSyncExec(true);
		
		dataHandler = (JiraTaskDataHandler) connector.getTaskDataHandler();

		client = JiraClientFacade.getDefault().getJiraClient(repository);
		
//		customFieldId = JiraTestUtils.getCustomField(server, customFieldName);
//		assertNotNull("Unable to find custom field id", customFieldId);
		customFieldName = "Free Text";
		customFieldId = "customfield_10011";
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

	public void testUpdateTaskCustomFields() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		Date today = new SimpleDateFormat("dd/MMM/yy").parse("1/Jun/06");
		SimpleDateFormat df = new SimpleDateFormat(JiraAttributeFactory.JIRA_DATE_FORMAT);
		String dueDate = df.format(today);

		Issue issue = JiraTestUtils.createIssue(client, "testUpdateTask");
		issue = client.createIssue(issue);

		AbstractTask task = connector.createTaskFromExistingId(repository, issue.getKey(), new NullProgressMonitor());
		assertEquals("testUpdateTask", task.getSummary());
		assertEquals(false, task.isCompleted());
		assertNull(task.getDueDate());

		String issueKey = issue.getKey();

		RepositoryTaskData taskData;

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		taskData.addAttributeValue(JiraAttributeFactory.ATTRIBUTE_DUE_DATE, dueDate);
		taskData.addAttributeValue(customFieldId, "foo");
		taskData.addAttributeValue(RepositoryTaskAttribute.COMMENT_NEW, "add comment");
		RepositoryOperation leaveOperation = new RepositoryOperation("leave", "");
		taskData.addOperation(leaveOperation);
		taskData.setSelectedOperation(leaveOperation);

//		issue = server.getIssueByKey(issueKey);
//		issue.setCustomFields(new CustomField[] { new CustomField(fieldId, //
//				"com.atlassian.jira.plugin.system.customfieldtypes:textfield", // 
//				fieldName, Collections.singletonList("foo")) });
//		server.updateIssue(issue, "add comment");

		dataHandler.postTaskData(repository, taskData, new NullProgressMonitor());

		issue = client.getIssueByKey(issueKey);
		assertCustomField(issue, customFieldId, customFieldName, "foo");

		assertTrue("Invalid issue due date " + issue.getDue(), today.equals(issue.getDue()));

		{
			String operation = JiraTestUtils.getOperation(client, issue.getKey(), "resolve");
			assertNotNull("Unable to find id for resolve operation", operation);

//			Map<String, String[]> params = new HashMap<String, String[]>();
//			params.put(JiraAttribute.RESOLUTION.getParamName(), new String[] {JiraTestUtils.getFixedResolution(server).getId()});
//			params.put(JiraAttribute.COMMENT_NEW.getParamName(), new String[] {"comment"});
//			
//			// server.resolveIssue(issue, JiraTestUtils.getFixedResolution(server), null, "comment", JiraClient.ASSIGNEE_DEFAULT, "");
//			server.advanceIssueWorkflow(issue, operation, params);

			taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
			taskData.setAttributeValue(RepositoryTaskAttribute.RESOLUTION, JiraTestUtils.getFixedResolution(client)
					.getId());
			taskData.setAttributeValue(RepositoryTaskAttribute.COMMENT_NEW, "comment");

			RepositoryOperation resolveOperation = new RepositoryOperation(operation, "resolve");
			taskData.addOperation(resolveOperation);
			taskData.setSelectedOperation(resolveOperation);
			dataHandler.postTaskData(repository, taskData, new NullProgressMonitor());
		}
		TasksUiPlugin.getSynchronizationManager().synchronize(connector, task, true, null);
		assertEquals("testUpdateTask", task.getSummary());
		assertEquals(true, task.isCompleted());
		assertNotNull(task.getCompletionDate());

		assertTrue("Invalid task due date " + task.getDueDate(), today.equals(task.getDueDate()));

		issue = client.getIssueByKey(issueKey);
		assertCustomField(issue, customFieldId, customFieldName, "foo");

		assertTrue("Invalid issue due date " + issue.getDue(), today.equals(issue.getDue()));

		{
			String operation = JiraTestUtils.getOperation(client, issue.getKey(), "close");
			assertNotNull("Unable to find id for close operation", operation);

//			Map<String, String[]> params = new HashMap<String, String[]>();
//			params.put(JiraAttribute.RESOLUTION.getParamName(), new String[] {JiraTestUtils.getFixedResolution(server).getId()});
//			params.put(JiraAttribute.COMMENT_NEW.getParamName(), new String[] {"comment"});
//			
//			// server.closeIssue(issue, JiraTestUtils.getFixedResolution(server), null, "comment", JiraClient.ASSIGNEE_DEFAULT, "");
//			server.advanceIssueWorkflow(issue, operation, params);

			taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
			taskData.setAttributeValue(RepositoryTaskAttribute.RESOLUTION, JiraTestUtils.getFixedResolution(client)
					.getId());
			taskData.setAttributeValue(RepositoryTaskAttribute.COMMENT_NEW, "comment");

			RepositoryOperation resolveOperation = new RepositoryOperation(operation, "close");
			taskData.addOperation(resolveOperation);
			taskData.setSelectedOperation(resolveOperation);
			dataHandler.postTaskData(repository, taskData, new NullProgressMonitor());
		}
		TasksUiPlugin.getSynchronizationManager().synchronize(connector, task, true, null);
		assertEquals("testUpdateTask", task.getSummary());
		assertEquals(true, task.isCompleted());
		assertNotNull(task.getCompletionDate());

		issue = client.getIssueByKey(issueKey);
		assertCustomField(issue, customFieldId, customFieldName, "foo");

		assertTrue("Invalid issue due date " + issue.getDue(), today.equals(issue.getDue()));
	}

	private void assertCustomField(Issue issue, String fieldId, String fieldName, String value) {
		CustomField customField;
		customField = issue.getCustomFieldById(fieldId);
		assertNotNull("Expecting to see custom field " + fieldName, customField);
		assertEquals(fieldName, customField.getName());
		assertEquals(1, customField.getValues().size());
		assertEquals(value, customField.getValues().get(0));
	}

	public void testGetTaskDataSubTasks() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		Issue parentIssue = JiraTestUtils.createIssue(client, "testSubTask");
		parentIssue = client.createIssue(parentIssue);
		
		Issue subTaskIssue = JiraTestUtils.createSubTask(client, parentIssue, "testSubTaskChild");
		subTaskIssue = client.createSubTask(subTaskIssue);

		RepositoryTaskData taskData = dataHandler.getTaskData(repository, parentIssue.getId(), new NullProgressMonitor());
		RepositoryTaskAttribute typeAttribute = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_TYPE);
		assertFalse(typeAttribute.isReadOnly());
		assertTrue(typeAttribute.getOptions().size() > 0);		
		Set<String> ids = dataHandler.getSubTaskIds(taskData);
		assertEquals(1, ids.size());
		assertEquals(subTaskIssue.getId(), ids.iterator().next());
		
		taskData = dataHandler.getTaskData(repository, subTaskIssue.getId(), new NullProgressMonitor());
		assertEquals(subTaskIssue.getId(), taskData.getId());
		assertEquals(subTaskIssue.getKey(), taskData.getTaskKey());
		typeAttribute = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_TYPE);
		assertTrue(typeAttribute.isReadOnly());
		assertEquals(0, typeAttribute.getOptions().size());		
	}

	public void testPostTaskDataSubTask() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		Issue parentIssue = JiraTestUtils.createIssue(client, "testUpdateSubTask");
		parentIssue = client.createIssue(parentIssue);
		
		Issue subTaskIssue = JiraTestUtils.createSubTask(client, parentIssue, "testUpdateSubTaskChild");
		subTaskIssue = client.createSubTask(subTaskIssue);

		RepositoryTaskData taskData = dataHandler.getTaskData(repository, subTaskIssue.getId(), new NullProgressMonitor());
		assertEquals(subTaskIssue.getKey(), taskData.getTaskKey());
		
		taskData.setDescription("new description");
		dataHandler.postTaskData(repository, taskData, new NullProgressMonitor());
		
		Issue updatedSubTaskIssue = client.getIssueByKey(taskData.getTaskKey());
		assertEquals(subTaskIssue.getId(), updatedSubTaskIssue.getId());
		assertEquals("new description", updatedSubTaskIssue.getDescription());
	}

	public void testInitializeSubTask() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		Issue parentIssue = JiraTestUtils.createIssue(client, "testInitializeSubTask");
		parentIssue = client.createIssue(parentIssue);

		RepositoryTaskData parentTaskData = dataHandler.getTaskData(repository, parentIssue.getId(), new NullProgressMonitor());
		
		AbstractAttributeFactory attributeFactory = dataHandler.getAttributeFactory(repository.getUrl(),
				repository.getConnectorKind(), AbstractTask.DEFAULT_TASK_KIND);
		RepositoryTaskData taskData = new RepositoryTaskData(attributeFactory, JiraUiPlugin.REPOSITORY_KIND,
				repository.getUrl(), TasksUiPlugin.getDefault().getNextNewRepositoryTaskId());

		dataHandler.initializeSubTaskData(repository, taskData, parentTaskData, new NullProgressMonitor());

		assertEquals(parentIssue.getId(), taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_ISSUE_PARENT_ID));
		assertEquals(parentIssue.getProject().getName(), taskData.getAttributeValue(RepositoryTaskAttribute.PRODUCT));
		assertNotNull(taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_TYPE));
	}
}
