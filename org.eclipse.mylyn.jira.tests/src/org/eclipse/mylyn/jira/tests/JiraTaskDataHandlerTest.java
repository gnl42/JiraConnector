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
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.context.tests.support.TestUtil;
import org.eclipse.mylyn.context.tests.support.TestUtil.Credentials;
import org.eclipse.mylyn.context.tests.support.TestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.jira.core.JiraAttributeFactory;
import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraTaskDataHandler;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.CustomField;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.SecurityLevel;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.tasks.core.AbstractAttributeFactory;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.RepositoryOperation;
import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylyn.tasks.core.RepositoryTaskData;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.web.core.AuthenticationCredentials;
import org.eclipse.mylyn.web.core.AuthenticationType;

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

		JiraClientFactory.getDefault().clearClients();
	}

	@Override
	protected void tearDown() throws Exception {
		if (client != null) {
			JiraTestUtils.cleanup(client);
		}
	}

	protected void init(String url) throws Exception {
		init(url, PrivilegeLevel.USER);
	}

	protected void init(String url, PrivilegeLevel level) throws Exception {
		String kind = JiraCorePlugin.REPOSITORY_KIND;

		Credentials credentials = TestUtil.readCredentials(level);

		repository = new TaskRepository(kind, url);
		repository.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials(credentials.username,
				credentials.password), false);
		manager.addRepository(repository, TasksUiPlugin.getDefault().getRepositoriesFilePath());

		connector = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(kind);
		assertEquals(connector.getConnectorKind(), kind);

		dataHandler = (JiraTaskDataHandler) connector.getTaskDataHandler();

		client = JiraClientFactory.getDefault().getJiraClient(repository);

//		customFieldId = JiraTestUtils.getCustomField(server, customFieldName);
//		assertNotNull("Unable to find custom field id", customFieldId);
		customFieldName = "Free Text";
		customFieldId = "customfield_10011";
	}

	public void testGetTaskData() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		JiraIssue issue = JiraTestUtils.newIssue(client, "testUpdateTask");
		issue.setEstimate(600);
		Component component = issue.getProject().getComponents()[0];
		issue.setComponents(new Component[] { component });
		issue = client.createIssue(issue, null);
		assertEquals(600, issue.getInitialEstimate());

		String commentText = "line1\nline2\n\nline4\n\n\n";
		client.addCommentToIssue(issue, commentText, null);

		RepositoryTaskData taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals(1, taskData.getComments().size());
		assertEquals(commentText, taskData.getComments().get(0).getText());
		assertEquals("10m", taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_ESTIMATE));
		assertEquals(component.getName(), taskData.getAttributeValue(RepositoryTaskAttribute.COMPONENT));
	}

	public void testCreateTaskData() throws JiraException {
		JiraIssue issue = new JiraIssue();
		issue.setProject(MockJiraClient.createProject());
		issue.setAssignee("eu");

		issue.setId("100");
		issue.setKey("FOO-1");
		issue.setAssignee("boo");
		issue.setSummary("summary");
		issue.setDescription("description");

		JiraStatus status = new JiraStatus();
		status.setId("1"); // open
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

		TaskRepository repository = new TaskRepository(JiraCorePlugin.REPOSITORY_KIND, "http://jira.codehaus.org/");
		MockJiraClient client = new MockJiraClient(repository.getRepositoryUrl());
		JiraTaskDataHandler dataHandler = new JiraTaskDataHandler(new MockJiraClientFactory(client));
		RepositoryTaskData data = dataHandler.createTaskData(repository, client, issue, null, null);

		assertValues(data, RepositoryTaskAttribute.TASK_KEY, "FOO-1");
		assertValues(data, RepositoryTaskAttribute.STATUS, "open");
		assertValues(data, RepositoryTaskAttribute.PRIORITY, "blocker");
		assertValues(data, RepositoryTaskAttribute.PRODUCT, "Prone");
		assertValues(data, JiraAttributeFactory.ATTRIBUTE_TYPE, "task");

		assertValues(data, RepositoryTaskAttribute.DATE_CREATION, JiraUtil.dateToString(issue.getCreated()));

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
		String dueDate = JiraUtil.dateToString(today);

		JiraIssue issue = JiraTestUtils.createIssue(client, "testUpdateTask");

		AbstractTask task = TasksUiUtil.createTask(repository, issue.getKey(), new NullProgressMonitor());
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

		dataHandler.postTaskData(repository, taskData, new NullProgressMonitor());

		issue = client.getIssueByKey(issueKey, null);
		assertCustomField(issue, customFieldId, customFieldName, "foo");

		assertTrue("Invalid issue due date " + issue.getDue(), today.equals(issue.getDue()));

		{
			String operation = JiraTestUtils.getOperation(client, issue.getKey(), "resolve");
			assertNotNull("Unable to find id for resolve operation", operation);

			taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
			taskData.setAttributeValue(RepositoryTaskAttribute.COMMENT_NEW, "comment");

			RepositoryOperation resolveOperation = new RepositoryOperation(operation, "resolve");
			resolveOperation.setInputName("resolution");
			resolveOperation.setInputValue(JiraTestUtils.getFixedResolution(client).getId());
			taskData.addOperation(resolveOperation);
			taskData.setSelectedOperation(resolveOperation);
			dataHandler.postTaskData(repository, taskData, new NullProgressMonitor());
		}

		TasksUi.synchronizeTask(connector, task, true, null);
		assertEquals("testUpdateTask", task.getSummary());
		assertTrue(task.isCompleted());
		assertNotNull(task.getCompletionDate());

		assertTrue("Invalid task due date " + task.getDueDate(), today.equals(task.getDueDate()));

		issue = client.getIssueByKey(issueKey, null);
		assertCustomField(issue, customFieldId, customFieldName, "foo");

		assertTrue("Invalid issue due date " + issue.getDue(), today.equals(issue.getDue()));

		{
			String operation = JiraTestUtils.getOperation(client, issue.getKey(), "close");
			assertNotNull("Unable to find id for close operation", operation);

			taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
			taskData.setAttributeValue(RepositoryTaskAttribute.RESOLUTION, JiraTestUtils.getFixedResolution(client)
					.getId());
			taskData.setAttributeValue(RepositoryTaskAttribute.COMMENT_NEW, "comment");

			RepositoryOperation resolveOperation = new RepositoryOperation(operation, "close");
			taskData.addOperation(resolveOperation);
			taskData.setSelectedOperation(resolveOperation);
			dataHandler.postTaskData(repository, taskData, new NullProgressMonitor());
		}
		TasksUi.synchronizeTask(connector, task, true, null);
		assertEquals("testUpdateTask", task.getSummary());
		assertEquals(true, task.isCompleted());
		assertNotNull(task.getCompletionDate());

		issue = client.getIssueByKey(issueKey, null);
		assertCustomField(issue, customFieldId, customFieldName, "foo");

		assertTrue("Invalid issue due date " + issue.getDue(), today.equals(issue.getDue()));
	}

	private void assertCustomField(JiraIssue issue, String fieldId, String fieldName, String value) {
		CustomField customField;
		customField = issue.getCustomFieldById(fieldId);
		assertNotNull("Expecting to see custom field " + fieldName, customField);
		assertEquals(fieldName, customField.getName());
		assertEquals(1, customField.getValues().size());
		assertEquals(value, customField.getValues().get(0));
	}

	public void testGetTaskDataSubTasks() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		JiraIssue parentIssue = JiraTestUtils.createIssue(client, "testSubTask");

		JiraIssue subTaskIssue = JiraTestUtils.newSubTask(client, parentIssue, "testSubTaskChild");
		subTaskIssue = client.createSubTask(subTaskIssue, null);

		RepositoryTaskData taskData = dataHandler.getTaskData(repository, parentIssue.getId(),
				new NullProgressMonitor());
		RepositoryTaskAttribute typeAttribute = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_TYPE);
		assertFalse(typeAttribute.isReadOnly());
		assertTrue(typeAttribute.getOptions().size() > 0);
		Set<String> ids = dataHandler.getSubTaskIds(taskData);
		assertEquals(1, ids.size());
		assertEquals(subTaskIssue.getId(), ids.iterator().next());

		taskData = dataHandler.getTaskData(repository, subTaskIssue.getId(), new NullProgressMonitor());
		assertEquals(subTaskIssue.getId(), taskData.getTaskId());
		assertEquals(subTaskIssue.getKey(), taskData.getTaskKey());
		typeAttribute = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_TYPE);
		assertTrue(typeAttribute.isReadOnly());
		assertEquals(0, typeAttribute.getOptions().size());
	}

	public void testPostTaskDataSubTask() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		JiraIssue parentIssue = JiraTestUtils.createIssue(client, "testUpdateSubTask");

		JiraIssue subTaskIssue = JiraTestUtils.newSubTask(client, parentIssue, "testUpdateSubTaskChild");
		subTaskIssue = client.createSubTask(subTaskIssue, null);

		RepositoryTaskData taskData = dataHandler.getTaskData(repository, subTaskIssue.getId(),
				new NullProgressMonitor());
		assertEquals(subTaskIssue.getKey(), taskData.getTaskKey());

		taskData.setDescription("new description");
		dataHandler.postTaskData(repository, taskData, new NullProgressMonitor());

		JiraIssue updatedSubTaskIssue = client.getIssueByKey(taskData.getTaskKey(), null);
		assertEquals(subTaskIssue.getId(), updatedSubTaskIssue.getId());
		assertEquals("new description", updatedSubTaskIssue.getDescription());
	}

	public void testInitializeSubTask() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		JiraIssue parentIssue = JiraTestUtils.createIssue(client, "testInitializeSubTask");

		RepositoryTaskData parentTaskData = dataHandler.getTaskData(repository, parentIssue.getId(),
				new NullProgressMonitor());

		AbstractAttributeFactory attributeFactory = dataHandler.getAttributeFactory(repository.getRepositoryUrl(),
				repository.getConnectorKind(), AbstractTask.DEFAULT_TASK_KIND);
		RepositoryTaskData taskData = new RepositoryTaskData(attributeFactory, JiraCorePlugin.REPOSITORY_KIND,
				repository.getRepositoryUrl(), TasksUiPlugin.getDefault().getNextNewRepositoryTaskId());

		dataHandler.initializeSubTaskData(repository, taskData, parentTaskData, new NullProgressMonitor());

		assertEquals(parentIssue.getId(), taskData.getAttributeValue(JiraAttributeFactory.ATTRIBUTE_ISSUE_PARENT_ID));
		assertEquals(parentIssue.getProject().getName(), taskData.getAttributeValue(RepositoryTaskAttribute.PRODUCT));
		assertNotNull(taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_TYPE));
	}

	public void testSecurityLevel() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		JiraIssue issue = JiraTestUtils.newIssue(client, "testSecurityLevel");
		issue.setProject(client.getCache().getProjectByKey("SECURITY"));
		issue = JiraTestUtils.createIssue(client, issue);

		RepositoryTaskData taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertNull(taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_SECURITY_LEVEL));

		SecurityLevel securityLevel = new SecurityLevel();
		securityLevel.setId("10000");
		issue.setSecurityLevel(securityLevel);
		client.updateIssue(issue, "", null);

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		RepositoryTaskAttribute attribute = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_SECURITY_LEVEL);
		assertNotNull(attribute);
		assertEquals("10000", attribute.getOptionParameter("Developers"));
		assertEquals("Developers", attribute.getValue());

		dataHandler.postTaskData(repository, taskData, new NullProgressMonitor());

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		attribute = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_SECURITY_LEVEL);
		assertNotNull(attribute);
		assertEquals("10000", attribute.getOptionParameter("Developers"));
		assertEquals("Developers", attribute.getValue());

		taskData.removeAttribute(JiraAttributeFactory.ATTRIBUTE_SECURITY_LEVEL);
		dataHandler.postTaskData(repository, taskData, new NullProgressMonitor());

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertNull(taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_SECURITY_LEVEL));
	}

	/**
	 * Verifies that cached operations are refreshed from the repository when the status of an issue changes.
	 */
	public void testCachedOperationsAfterChangingState() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		JiraIssue issue = JiraTestUtils.createIssue(client, "testChangeState");

		RepositoryTaskData taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		List<RepositoryOperation> operations = taskData.getOperations();
		assertNotNull(operations);
		assertEquals(5, operations.size());
		assertEquals("5", operations.get(3).getKnobName());
		assertEquals("2", operations.get(4).getKnobName());

		// resolve issue
		issue.setResolution(client.getCache().getResolutionById(Resolution.FIXED_ID));
		client.advanceIssueWorkflow(issue, "5", "", null);
		issue = client.getIssueByKey(issue.getKey(), null);

		RepositoryTaskData newTaskData = dataHandler.createTaskData(repository, client, issue, taskData, null);
		operations = newTaskData.getOperations();
		assertNotNull(operations);
		assertEquals(3, operations.size());
		assertEquals("3", operations.get(2).getKnobName());

		issue.setSummary("changed");
		client.updateIssue(issue, "", null);
		newTaskData.getOperations().remove(0);

		// make sure cached operations are used
		newTaskData = dataHandler.createTaskData(repository, client, issue, newTaskData, null);
		operations = newTaskData.getOperations();
		assertNotNull(operations);
		assertEquals(2, operations.size());
		assertEquals("3", operations.get(1).getKnobName());

	}

	public void testReadOnly() throws Exception {
		init(JiraTestConstants.JIRA_39_URL, PrivilegeLevel.GUEST);

		JiraIssue issue = JiraTestUtils.createIssue(client, "testReadOnly");

		RepositoryTaskData taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertNotNull(taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_READ_ONLY));

		taskData.setSummary("new summary");
		taskData.setNewComment("comment");
		dataHandler.postTaskData(repository, taskData, new NullProgressMonitor());

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("testReadOnly", taskData.getSummary());
		assertEquals(1, taskData.getComments().size());
		assertEquals("comment", taskData.getComments().get(0).getText());
		assertNotNull(taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_READ_ONLY));

		setUp();
		init(JiraTestConstants.JIRA_39_URL, PrivilegeLevel.USER);

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertNull(taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_READ_ONLY));

		taskData.setSummary("new summary");
		taskData.setNewComment("new comment");
		dataHandler.postTaskData(repository, taskData, new NullProgressMonitor());

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("new summary", taskData.getSummary());
		assertEquals(2, taskData.getComments().size());
		assertEquals("comment", taskData.getComments().get(0).getText());
		assertEquals("new comment", taskData.getComments().get(1).getText());
		assertNull(taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_READ_ONLY));
	}

	public void testClosedIssueNotEditable() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		JiraIssue issue = JiraTestUtils.createIssue(client, "testEditClosed");

		RepositoryTaskData taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertNull(taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_READ_ONLY));
		assertFalse(taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS).isReadOnly());

		// close
		client.advanceIssueWorkflow(issue, "2", "", null);

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertNull(taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_READ_ONLY));
		assertTrue(taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS).isReadOnly());

		taskData.setSummary("new summary");
		dataHandler.postTaskData(repository, taskData, new NullProgressMonitor());
		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("testEditClosed", taskData.getSummary());
	}

	public void testInitializeTaskData1() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		RepositoryTaskData data = createTaskData(repository.getConnectorKind(), repository.getRepositoryUrl(), null);

		boolean res = dataHandler.initializeTaskData(repository, data, new NullProgressMonitor());
		assertFalse("Task data shouldn't be initialized without project", res);
	}

	public void testInitializeTaskDataWithProjectName() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);
		JiraTestUtils.refreshDetails(client);
		Project project = JiraTestUtils.getProject(client, JiraTestUtils.PROJECT1);

		RepositoryTaskData data = createTaskData(repository.getConnectorKind(), repository.getRepositoryUrl(),
				project.getName());

		boolean res = dataHandler.initializeTaskData(repository, data, new NullProgressMonitor());
		assertTrue("Task data can't be initialized", res);

		verifyTaskData(data, project);
	}

	public void testInitializeTaskDataWithProjectKey() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);
		JiraTestUtils.refreshDetails(client);
		Project project = JiraTestUtils.getProject(client, JiraTestUtils.PROJECT1);

		RepositoryTaskData data = createTaskData(repository.getConnectorKind(), repository.getRepositoryUrl(),
				JiraTestUtils.PROJECT1);

		boolean res = dataHandler.initializeTaskData(repository, data, new NullProgressMonitor());
		assertTrue("Task data can't be initialized", res);

		verifyTaskData(data, project);
	}

	private RepositoryTaskData createTaskData(String type, String url, String project) {
		AbstractAttributeFactory attributeFactory = dataHandler.getAttributeFactory(url, type,
				AbstractTask.DEFAULT_TASK_KIND);

		RepositoryTaskData data = new RepositoryTaskData(attributeFactory, type, url, "NEW");
		data.setNew(true);

		if (project != null) {
			RepositoryTaskAttribute attribute = new RepositoryTaskAttribute(RepositoryTaskAttribute.PRODUCT,
					"Project:", false);
			attribute.addValue(project);
			data.addAttribute(RepositoryTaskAttribute.PRODUCT, attribute);
		}

		return data;
	}

	private void verifyTaskData(RepositoryTaskData data, Project project) {
		RepositoryTaskAttribute projectAttr = data.getAttribute(RepositoryTaskAttribute.PRODUCT);
		assertEquals(project.getName(), projectAttr.getValue());

		RepositoryTaskAttribute priorityAttr = data.getAttribute(RepositoryTaskAttribute.PRIORITY);
		assertNotNull(priorityAttr);
		assertTrue(!priorityAttr.getOptions().isEmpty());

		RepositoryTaskAttribute typesAttr = data.getAttribute(JiraAttributeFactory.ATTRIBUTE_TYPE);
		assertNotNull(typesAttr);
		assertTrue(!typesAttr.getOptions().isEmpty());

		RepositoryTaskAttribute componentsAttr = data.getAttribute(JiraAttributeFactory.ATTRIBUTE_COMPONENTS);
		assertNotNull(componentsAttr);
		assertTrue(!componentsAttr.getOptions().isEmpty());

		RepositoryTaskAttribute fixVersionsAttr = data.getAttribute(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS);
		assertNotNull(fixVersionsAttr);
		assertTrue(!fixVersionsAttr.getOptions().isEmpty());

		RepositoryTaskAttribute affectsVersionsAttr = data.getAttribute(JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS);
		assertNotNull(affectsVersionsAttr);
		assertTrue(!affectsVersionsAttr.getOptions().isEmpty());
	}

}
