/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Eugene Kuleshov - improvements
 *     Pawel Niewiadomski - fix for bug 287736
 *******************************************************************************/

package me.glindholm.connector.eclipse.jira.tests.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.TaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tests.util.TestUtil;
import org.eclipse.mylyn.tests.util.TestUtil.PrivilegeLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.glindholm.connector.eclipse.internal.jira.core.JiraAttribute;
import me.glindholm.connector.eclipse.internal.jira.core.JiraClientFactory;
import me.glindholm.connector.eclipse.internal.jira.core.JiraConstants;
import me.glindholm.connector.eclipse.internal.jira.core.JiraCorePlugin;
import me.glindholm.connector.eclipse.internal.jira.core.JiraRepositoryConnector;
import me.glindholm.connector.eclipse.internal.jira.core.JiraTaskDataHandler;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraComponent;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraCustomField;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssue;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraPriority;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProject;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraResolution;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraSecurityLevel;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraStatus;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraVersion;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClient;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraException;
import me.glindholm.connector.eclipse.internal.jira.core.util.JiraUtil;
import me.glindholm.connector.eclipse.jira.tests.util.JiraFixture;
import me.glindholm.connector.eclipse.jira.tests.util.JiraTestUtil;
import me.glindholm.connector.eclipse.jira.tests.util.MockJiraClient;
import me.glindholm.connector.eclipse.jira.tests.util.MockJiraClientFactory;
import me.glindholm.jira.rest.client.api.domain.BasicUser;

/**
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 * @author Thomas Ehrnhoefer
 * @author Jacek Jaroczynski
 */
public class JiraTaskDataHandlerTest {

	private static final String SECURITY_LEVEL_USERS = "10001";

	private static final String SECURITY_LEVEL_DEVELOPERS = "10000";

	private static final String SECURITY_LEVEL_NONE = "-1";

	private TaskRepository repository;

	private JiraTaskDataHandler dataHandler;

	private JiraClient client;

	private JiraRepositoryConnector connector;

	private String customFieldId;

	private String customFieldName;

	@BeforeEach
	protected void setUp() throws Exception {
		JiraTestUtil.setUp();
		connector = (JiraRepositoryConnector) TasksUi.getRepositoryConnector(JiraCorePlugin.CONNECTOR_KIND);
		dataHandler = connector.getTaskDataHandler();
	}

	@AfterEach
	protected void tearDown() throws Exception {
		JiraTestUtil.tearDown();
	}

	protected void init(final String url) throws Exception {
		init(url, PrivilegeLevel.USER);
	}

	protected void init(final String url, final PrivilegeLevel level) throws Exception {
		repository = JiraTestUtil.init(url, level);
		client = JiraClientFactory.getDefault().getJiraClient(repository);

		//		customFieldId = JiraTestUtils.getCustomField(server, customFieldName);
		//		assertNotNull("Unable to find custom field id", customFieldId);
		customFieldName = "Free Text";
		customFieldId = "customfield_10011";
	}

	@Test
	public void testGetTaskData() throws Exception {
		init(jiraUrl());

		var issue = JiraTestUtil.newIssue(client, "testUpdateTask");
		issue.setEstimate(600);

		// update project cache (otherwise there are no components and we get NPE)
		client.getCache().refreshProjectDetails(issue.getProject().getId(), new NullProgressMonitor());
		assertTrue(client.getCache().getProjectById(issue.getProject().getId()).hasDetails());

		final var component = issue.getProject().getComponents()[0];
		issue.setComponents(new JiraComponent[] { component });
		issue = client.createIssue(issue, null);
		assertTrue(Long.valueOf(600).equals(issue.getInitialEstimate()));

		final var commentText = "line1\nline2\n\nline4\n\n\n";
		client.addCommentToIssue(issue.getKey(), commentText, null);

		final var taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		final var task = JiraTestUtil.createTask(repository, taskData.getTaskId());
		final var comments = JiraTestUtil.getTaskComments(task);
		assertEquals(1, comments.size());
		assertEquals(commentText, comments.get(0).getText().replace("\r\n", "\n"));
		assertEquals("600", taskData.getRoot().getAttribute(JiraAttribute.ESTIMATE.id()).getValue());
		assertEquals(component.getId(), taskData.getRoot().getAttribute(JiraAttribute.COMPONENTS.id()).getValue());
	}

	@Test
	public void testGetTaskDataWhenProjectIsMissingDetails() throws Exception {
		init(jiraUrl());

		var issue = JiraTestUtil.newIssue(client, "testUpdateTask");
		issue.setEstimate(600);

		// update project cache (otherwise there are no components and we get NPE)
		client.getCache().refreshProjectDetails(issue.getProject().getId(), new NullProgressMonitor());
		assertTrue(client.getCache().getProjectById(issue.getProject().getId()).hasDetails());

		final var component = issue.getProject().getComponents()[0];
		issue.setComponents(new JiraComponent[] { component });
		issue = client.createIssue(issue, null);
		assertEquals(600L, issue.getInitialEstimate());

		final var commentText = "line1\nline2\n\nline4\n\n\n";
		client.addCommentToIssue(issue.getKey(), commentText, null);

		// mark that project requires metadata refresh, getTaskData should update it automatically
		client.getCache().getProjectById(issue.getProject().getId()).setDetails(false);

		final var taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		final var task = JiraTestUtil.createTask(repository, taskData.getTaskId());
		final var comments = JiraTestUtil.getTaskComments(task);
		assertEquals(1, comments.size());
		assertEquals(commentText, comments.get(0).getText().replace("\r\n", "\n"));
		assertEquals("600", taskData.getRoot().getAttribute(JiraAttribute.ESTIMATE.id()).getValue());
		assertEquals(component.getId(), taskData.getRoot().getAttribute(JiraAttribute.COMPONENTS.id()).getValue());
		assertTrue(client.getCache().getProjectById(issue.getProject().getId()).hasDetails());
	}

	@Test
	public void testCreateTaskData() throws JiraException {
		final var issue = new JiraIssue();
		final var project = MockJiraClient.createProject();
		issue.setProject(project);
		issue.setAssignee(new BasicUser(null, "eu", "eu"));
		issue.setId("100");
		issue.setKey("FOO-1");
		issue.setAssignee(new BasicUser(null, "boo", "boo"));
		issue.setSummary("summary");
		issue.setDescription("description");
		final var status = new JiraStatus("1"); // 1 == open
		status.setName("open");
		issue.setStatus(status);
		final var created = new Date();
		issue.setCreated(created.toInstant());
		final JiraComponent[] components = { MockJiraClient.createComponent("2", "component2"),
				MockJiraClient.createComponent("3", "component3") };
		issue.setComponents(components);
		final JiraVersion[] fixVersions = { MockJiraClient.createVersion("3", "3.0") };
		issue.setFixVersions(fixVersions);
		final JiraVersion[] reportedVersions = { MockJiraClient.createVersion("1", "1.0"),
				MockJiraClient.createVersion("2", "2.0") };
		issue.setReportedVersions(reportedVersions);
		issue.setType(MockJiraClient.createIssueType("3", "task"));
		issue.setPriority(MockJiraClient.createPriority(JiraPriority.BLOCKER_ID, "blocker"));
		issue.setUrl("http://mylyn");

		project.setVersions(new JiraVersion[] { MockJiraClient.createVersion("3", "3.0"),
				MockJiraClient.createVersion("1", "1.0"), MockJiraClient.createVersion("2", "2.0") });
		project.setComponents(components);
		project.setDetails(true);

		final var repository = new TaskRepository(JiraCorePlugin.CONNECTOR_KIND, "http://jira.codehaus.org/");
		final MockJiraClient client = new MockJiraClient(repository.getRepositoryUrl()) {
			@Override
			public JiraProject[] getProjects(final IProgressMonitor monitor) throws JiraException {
				return new JiraProject[] { project };
			}
		};
		final var dataHandler = new JiraTaskDataHandler(new MockJiraClientFactory(client));
		final var data = dataHandler.createTaskData(repository, client, issue, null, null);

		assertValues(data, TaskAttribute.TASK_KEY, "FOO-1");
		assertValues(data, TaskAttribute.STATUS, "open");
		assertValues(data, TaskAttribute.PRIORITY, JiraPriority.BLOCKER_ID);
		assertValues(data, TaskAttribute.PRODUCT, "PRONE");
		assertValues(data, JiraConstants.ATTRIBUTE_TYPE, "3");
		assertValues(data, TaskAttribute.DATE_CREATION, JiraUtil.dateToString(issue.getCreated()));
		assertValues(data, JiraConstants.ATTRIBUTE_COMPONENTS, "component2", "component3");
		assertValues(data, JiraConstants.ATTRIBUTE_AFFECTSVERSIONS, "1.0", "2.0");
		assertValues(data, JiraConstants.ATTRIBUTE_FIXVERSIONS, "3.0");
		assertValues(data, TaskAttribute.SUMMARY, "summary");
		assertValues(data, TaskAttribute.DESCRIPTION, "description");
	}

	private void assertValues(final TaskData data, final String key, final String... values) {
		final var attribute = data.getRoot().getAttribute(key);
		var n = 0;
		for (final String value : attribute.getValues()) {
			if (!attribute.getOptions().isEmpty()) {
				assertEquals(values[n], attribute.getOption(value));
			} else {
				assertEquals(values[n], attribute.getValue());
			}
			n++;
		}
	}

	@Test
	public void testUpdateTaskCustomFields() throws Exception {
		init(jiraUrl());

		final var today = new SimpleDateFormat("dd/MMM/yy", Locale.ENGLISH).parse("1/Jun/06");
		final var dueDate = JiraUtil.dateToString(today.toInstant());
		var issue = JiraTestUtil.createIssue(client, "testUpdateTask");
		final var task = JiraTestUtil.createTask(repository, issue.getKey());
		assertEquals("testUpdateTask", task.getSummary());
		assertEquals(false, task.isCompleted());
		assertNull(task.getDueDate());

		final var issueKey = issue.getKey();
		var taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		var attribute = dataHandler.createAttribute(taskData, JiraAttribute.DUE_DATE);
		attribute.setValue(dueDate);
		attribute = taskData.getRoot().createAttribute(dataHandler.mapCommonAttributeKey(customFieldId));
		attribute.addValue("foo");
		attribute.getMetaData().setLabel("foo");
		taskData.getRoot().getAttribute(TaskAttribute.COMMENT_NEW).addValue("add comment");
		dataHandler.postTaskData(repository, taskData, null, new NullProgressMonitor());

		issue = client.getIssueByKey(issueKey, null);
		// TODO rest: restore custom filed test when custom field update is possible
		//		assertCustomField(issue, customFieldId, customFieldName, "foo");
		assertTrue(today.equals(issue.getDue()), "Invalid issue due date " + issue.getDue());

		{
			final var operation = JiraTestUtil.getOperation(client, issue.getKey(), "resolve");
			assertNotNull("Unable to find id for resolve operation", operation);
			taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
			setAttributeValue(taskData, TaskAttribute.COMMENT_NEW, "comment");
			setAttributeValue(taskData, TaskAttribute.RESOLUTION, JiraTestUtil.getFixedResolution(client).getId());
			setAttributeValue(taskData, TaskAttribute.OPERATION, operation);
			dataHandler.postTaskData(repository, taskData, null, new NullProgressMonitor());
		}

		TasksUiInternal.synchronizeTask(connector, task, true, null);
		assertEquals("testUpdateTask", task.getSummary());
		assertTrue(task.isCompleted());
		assertNotNull(task.getCompletionDate());
		assertTrue(today.equals(task.getDueDate()), "Invalid task due date " + task.getDueDate());

		issue = client.getIssueByKey(issueKey, null);
		//		assertCustomField(issue, customFieldId, customFieldName, "foo");
		assertTrue(today.equals(issue.getDue()), "Invalid issue due date " + issue.getDue());

		{
			final var operation = JiraTestUtil.getOperation(client, issue.getKey(), "close");
			assertNotNull("Unable to find id for close operation", operation);
			taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
			setAttributeValue(taskData, TaskAttribute.COMMENT_NEW, "comment");
			setAttributeValue(taskData, TaskAttribute.RESOLUTION, JiraTestUtil.getFixedResolution(client).getId());
			setAttributeValue(taskData, TaskAttribute.OPERATION, operation);
			dataHandler.postTaskData(repository, taskData, null, new NullProgressMonitor());
		}

		TasksUiInternal.synchronizeTask(connector, task, true, null);
		assertEquals("testUpdateTask", task.getSummary());
		assertEquals(true, task.isCompleted());
		assertNotNull(task.getCompletionDate());

		issue = client.getIssueByKey(issueKey, null);
		//		assertCustomField(issue, customFieldId, customFieldName, "foo");
		assertTrue(today.equals(issue.getDue()), "Invalid issue due date " + issue.getDue());
	}

	private void setAttributeValue(final TaskData taskData, final String attributeId, final String value) {
		taskData.getRoot().getMappedAttribute(attributeId).setValue(value);
	}

	private void assertCustomField(final JiraIssue issue, final String fieldId, final String fieldName, final String value) {
		JiraCustomField customField;
		customField = issue.getCustomFieldById(fieldId);
		assertNotNull(customField, "Expecting to see custom field " + fieldName);
		assertEquals(fieldName, customField.getName());
		assertEquals(1, customField.getValues().size());
		assertEquals(value, customField.getValues().get(0));
	}

	@Test
	public void testGetTaskDataSubTasks() throws Exception {
		init(jiraUrl());

		final var parentIssue = JiraTestUtil.createIssue(client, "testSubTask");

		var subTaskIssue = JiraTestUtil.newSubTask(client, parentIssue, "testSubTaskChild");
		subTaskIssue = client.createIssue(subTaskIssue, null);

		var taskData = dataHandler.getTaskData(repository, parentIssue.getId(), new NullProgressMonitor());
		var typeAttribute = taskData.getRoot().getAttribute(JiraConstants.ATTRIBUTE_TYPE);
		assertFalse(typeAttribute.getMetaData().isReadOnly());
		assertTrue(typeAttribute.getOptions().size() > 0);
		final var ids = connector.getTaskRelations(taskData);
		assertEquals(1, ids.size());
		assertEquals(subTaskIssue.getId(), ids.iterator().next().getTaskId());

		taskData = dataHandler.getTaskData(repository, subTaskIssue.getId(), new NullProgressMonitor());
		assertEquals(subTaskIssue.getId(), taskData.getTaskId());
		assertEquals(subTaskIssue.getKey(), taskData.getRoot().getAttribute(JiraAttribute.ISSUE_KEY.id()).getValue());
		typeAttribute = taskData.getRoot().getAttribute(JiraConstants.ATTRIBUTE_TYPE);
		assertTrue(typeAttribute.getMetaData().isReadOnly());
		assertEquals(1, typeAttribute.getOptions().size());
	}

	@Test
	public void testPostTaskDataSubTask() throws Exception {
		init(jiraUrl());

		final var parentIssue = JiraTestUtil.createIssue(client, "testUpdateSubTask");
		var subTaskIssue = JiraTestUtil.newSubTask(client, parentIssue, "testUpdateSubTaskChild");
		subTaskIssue = client.createIssue(subTaskIssue, null);
		final var taskData = dataHandler.getTaskData(repository, subTaskIssue.getId(), new NullProgressMonitor());
		assertEquals(subTaskIssue.getKey(), taskData.getRoot().getAttribute(JiraAttribute.ISSUE_KEY.id()).getValue());

		taskData.getRoot().getAttribute(JiraAttribute.DESCRIPTION.id()).setValue("new description");
		dataHandler.postTaskData(repository, taskData, null, new NullProgressMonitor());
		final var updatedSubTaskIssue = client.getIssueByKey(
				taskData.getRoot().getAttribute(JiraAttribute.ISSUE_KEY.id()).getValue(), null);
		assertEquals(subTaskIssue.getId(), updatedSubTaskIssue.getId());
		assertEquals("new description", updatedSubTaskIssue.getDescription());
	}

	@Test
	public void testInitializeSubTask() throws Exception {
		init(jiraUrl());

		final var parentIssue = JiraTestUtil.createIssue(client, "testInitializeSubTask");
		final var parentTaskData = dataHandler.getTaskData(repository, parentIssue.getId(), new NullProgressMonitor());
		final var taskData = new TaskData(dataHandler.getAttributeMapper(repository), JiraCorePlugin.CONNECTOR_KIND,
				repository.getRepositoryUrl(), "");
		dataHandler.initializeSubTaskData(repository, taskData, parentTaskData, new NullProgressMonitor());
		assertEquals(parentIssue.getId(), taskData.getRoot()
				.getAttribute(JiraConstants.ATTRIBUTE_ISSUE_PARENT_ID)
				.getValue());
		assertEquals(parentIssue.getProject().getId(), taskData.getRoot()
				.getAttribute(TaskAttribute.PRODUCT)
				.getValue());
		assertNotNull(taskData.getRoot().getAttribute(JiraConstants.ATTRIBUTE_TYPE));
	}

	@Test
	public void testSecurityLevelNoLevelsDefined() throws Exception {
		init(jiraUrl());

		var issue = JiraTestUtil.newIssue(client, "testSecurityLevel");
		issue = JiraTestUtil.createIssue(client, issue);

		final var taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertNull(taskData.getRoot().getAttribute(JiraConstants.ATTRIBUTE_SECURITY_LEVEL));
	}

	@Test
	public void testUpdateSecurityLevel() throws Exception {
		init(jiraUrl());

		// test security level is set to none for new issues
		var issue = JiraTestUtil.newIssue(client, "testSecurityLevel");
		issue.setProject(client.getCache().getProjectByKey("SECURITY"));
		issue = JiraTestUtil.createIssue(client, issue);

		var taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		var attribute = taskData.getRoot().getAttribute(JiraAttribute.SECURITY_LEVEL.id());
		assertNotNull(attribute);
		assertEquals("-1", attribute.getValue());
		assertEquals(4, attribute.getOptions().size());
		assertFalse(attribute.getMetaData().isReadOnly());

		// change security level through JiraClient
		final var securityLevel = new JiraSecurityLevel(SECURITY_LEVEL_DEVELOPERS);
		issue.setSecurityLevel(securityLevel);
		client.updateIssue(issue, "", null, null);

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		attribute = taskData.getRoot().getAttribute(JiraAttribute.SECURITY_LEVEL.id());
		assertNotNull(attribute);
		assertEquals(SECURITY_LEVEL_DEVELOPERS, attribute.getValue());
		assertEquals("Developers", attribute.getOption(SECURITY_LEVEL_DEVELOPERS));

		// change security level through TaskDataHandler
		attribute.setValue(SECURITY_LEVEL_USERS);
		dataHandler.postTaskData(repository, taskData, null, new NullProgressMonitor());
		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		attribute = taskData.getRoot().getAttribute(JiraAttribute.SECURITY_LEVEL.id());
		assertNotNull(attribute);
		assertEquals(SECURITY_LEVEL_USERS, attribute.getValue());
		assertEquals("Users", attribute.getOption(SECURITY_LEVEL_USERS));

		// remove security level attribute means no change during update
		taskData.getRoot().removeAttribute(JiraAttribute.SECURITY_LEVEL.id());
		dataHandler.postTaskData(repository, taskData, null, new NullProgressMonitor());
		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals(SECURITY_LEVEL_USERS, taskData.getRoot()
				.getAttribute(JiraAttribute.SECURITY_LEVEL.id())
				.getValue());

		// set security level attribute to "-1" means clear security level
		attribute = taskData.getRoot().getAttribute(JiraAttribute.SECURITY_LEVEL.id());
		attribute.setValue(SECURITY_LEVEL_NONE);
		dataHandler.postTaskData(repository, taskData, null, new NullProgressMonitor());
		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("-1", taskData.getRoot().getAttribute(JiraAttribute.SECURITY_LEVEL.id()).getValue());
	}

	@Test
	public void testSetSecurityLevelToNone() throws Exception {
		init(jiraUrl());

		var issue = JiraTestUtil.newIssue(client, "testSecurityLevel");
		issue.setProject(client.getCache().getProjectByKey("SECURITY"));
		issue.setSecurityLevel(new JiraSecurityLevel(SECURITY_LEVEL_DEVELOPERS));
		issue = JiraTestUtil.createIssue(client, issue);

		var taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		var attribute = taskData.getRoot().getAttribute(JiraAttribute.SECURITY_LEVEL.id());
		assertNotNull(attribute);
		assertEquals(SECURITY_LEVEL_DEVELOPERS, attribute.getValue());

		attribute.setValue(JiraSecurityLevel.NONE.getId());
		dataHandler.postTaskData(repository, taskData, null, new NullProgressMonitor());
		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		attribute = taskData.getRoot().getAttribute(JiraAttribute.SECURITY_LEVEL.id());
		assertNotNull(attribute);
		assertEquals("-1", attribute.getValue());
		assertEquals("None", attribute.getOption("-1"));
	}

	@Test
	public void testPostTaskDataCreateTaskWithSecurityLevel() throws Exception {
		init(jiraUrl());

		// initialize task data
		JiraTestUtil.refreshDetails(client);
		final var project = client.getCache().getProjectByKey("SECURITY");
		var taskData = new TaskData(dataHandler.getAttributeMapper(repository), repository.getConnectorKind(),
				repository.getRepositoryUrl(), "");
		final var result = dataHandler.initializeTaskData(repository, taskData, new TaskMapping() {
			@Override
			public String getProduct() {
				return project.getName();
			}
		}, null);
		assertTrue(result);

		// create issue
		taskData.getRoot()
		.getAttribute(JiraAttribute.SUMMARY.id())
		.setValue("testPostTaskDataCreateTaskWithSecurityLevel");
		var attribute = taskData.getRoot().getAttribute(JiraAttribute.SECURITY_LEVEL.id());
		assertNotNull(attribute);
		attribute.setValue(SECURITY_LEVEL_DEVELOPERS);
		final var response = dataHandler.postTaskData(repository, taskData, null, new NullProgressMonitor());

		// verify security level
		taskData = dataHandler.getTaskData(repository, response.getTaskId(), new NullProgressMonitor());
		attribute = taskData.getRoot().getAttribute(JiraAttribute.SECURITY_LEVEL.id());
		assertNotNull(attribute);
		assertEquals(SECURITY_LEVEL_DEVELOPERS, attribute.getValue());
	}

	/**
	 * Verifies that cached operations are refreshed from the repository when the status of an issue changes.
	 */
	@Test
	public void testCachedOperationsAfterChangingState() throws Exception {
		init(jiraUrl());

		var issue = JiraTestUtil.createIssue(client, "testChangeState");

		final var taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		List<TaskAttribute> operations = taskData.getAttributeMapper().getAttributesByType(taskData,
				TaskAttribute.TYPE_OPERATION);
		assertNotNull(operations);
		assertEquals(5, operations.size());
		assertEquals("5", operations.get(3).getValue());
		assertEquals("2", operations.get(4).getValue());

		// resolve issue
		issue.setResolution(client.getCache().getResolutionByName(JiraResolution.FIXED_NAME));
		client.advanceIssueWorkflow(issue, "5", "", null);
		issue = client.getIssueByKey(issue.getKey(), null);

		var newTaskData = dataHandler.createTaskData(repository, client, issue, taskData, null);
		operations = taskData.getAttributeMapper().getAttributesByType(newTaskData, TaskAttribute.TYPE_OPERATION);
		assertNotNull(operations);
		assertEquals(4, operations.size());
		assertEquals("3", operations.get(3).getValue());

		issue.setSummary("changed");
		client.updateIssue(issue, "", null, null);
		newTaskData.getRoot().removeAttribute(operations.get(0).getId());

		// make sure cached operations are used
		newTaskData = dataHandler.createTaskData(repository, client, issue, newTaskData, null);
		operations = taskData.getAttributeMapper().getAttributesByType(newTaskData, TaskAttribute.TYPE_OPERATION);
		assertNotNull(operations);
		assertEquals(3, operations.size());
		assertEquals("3", operations.get(2).getValue());
	}

	@Test
	public void testReadOnly() throws Exception {
		init(jiraUrl(), PrivilegeLevel.GUEST);

		final var issue = JiraTestUtil.createIssueWithoutAssignee(client, "testReadOnly");

		var taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertNotNull(taskData.getRoot().getAttribute(JiraConstants.ATTRIBUTE_READ_ONLY));

		taskData.getRoot().getAttribute(JiraAttribute.SUMMARY.id()).setValue("new summary");
		taskData.getRoot().getAttribute(JiraAttribute.COMMENT_NEW.id()).setValue("comment");
		dataHandler.postTaskData(repository, taskData, null, new NullProgressMonitor());

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("testReadOnly", taskData.getRoot().getAttribute(JiraAttribute.SUMMARY.id()).getValue());
		var task = JiraTestUtil.createTask(repository, taskData.getTaskId());
		var comments = JiraTestUtil.getTaskComments(task);
		assertEquals(1, comments.size());
		assertEquals("comment", comments.get(0).getText());
		assertNotNull(taskData.getRoot().getAttribute(JiraConstants.ATTRIBUTE_READ_ONLY));

		setUp();
		init(jiraUrl(), PrivilegeLevel.USER);

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertNull(taskData.getRoot().getAttribute(JiraConstants.ATTRIBUTE_READ_ONLY));

		taskData.getRoot().getAttribute(JiraAttribute.SUMMARY.id()).setValue("new summary");
		taskData.getRoot().getAttribute(JiraAttribute.COMMENT_NEW.id()).setValue("new comment");
		dataHandler.postTaskData(repository, taskData, null, new NullProgressMonitor());

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("new summary", taskData.getRoot().getAttribute(JiraAttribute.SUMMARY.id()).getValue());
		task = JiraTestUtil.createTask(repository, taskData.getTaskId());
		comments = JiraTestUtil.getTaskComments(task);
		assertEquals(2, comments.size());
		assertEquals("comment", comments.get(0).getText());
		assertEquals("new comment", comments.get(1).getText());
		assertNull(taskData.getRoot().getAttribute(JiraConstants.ATTRIBUTE_READ_ONLY));
	}

	@Test
	public void testClosedIssueNotEditable() throws Exception {
		init(jiraUrl());

		final var issue = JiraTestUtil.createIssue(client, "testEditClosed");

		var taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertNull(taskData.getRoot().getAttribute(JiraConstants.ATTRIBUTE_READ_ONLY));
		assertFalse(taskData.getRoot().getAttribute(JiraConstants.ATTRIBUTE_FIXVERSIONS).getMetaData().isReadOnly());

		issue.setResolution(client.getCache().getResolutionByName(JiraResolution.FIXED_NAME));
		// close
		client.advanceIssueWorkflow(issue, "2", "", null);

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertNull(taskData.getRoot().getAttribute(JiraConstants.ATTRIBUTE_READ_ONLY));
		assertTrue(taskData.getRoot().getAttribute(JiraConstants.ATTRIBUTE_FIXVERSIONS).getMetaData().isReadOnly());

		taskData.getRoot().getAttribute(JiraAttribute.SUMMARY.id()).setValue("new summary");
		dataHandler.postTaskData(repository, taskData, null, new NullProgressMonitor());
		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("testEditClosed", taskData.getRoot().getAttribute(JiraAttribute.SUMMARY.id()).getValue());
	}

	@Test
	public void testInitializeTaskDataNoProject() throws Exception {
		init(jiraUrl());
		final var data = new TaskData(dataHandler.getAttributeMapper(repository), repository.getConnectorKind(),
				repository.getRepositoryUrl(), "");
		final var res = dataHandler.initializeTaskData(repository, data, null, null);
		assertFalse(res, "Task data shouldn't be initialized without project");
	}

	@Test
	public void testInitializeTaskDataWithProjectName() throws Exception {
		init(jiraUrl());
		JiraTestUtil.refreshDetails(client);
		final var project = JiraTestUtil.getProject(client, JiraTestUtil.PROJECT1);
		final var data = new TaskData(dataHandler.getAttributeMapper(repository), repository.getConnectorKind(),
				repository.getRepositoryUrl(), "");
		final var res = dataHandler.initializeTaskData(repository, data, new TaskMapping() {
			@Override
			public String getProduct() {
				return project.getName();
			}
		}, null);
		assertTrue(res,"Task data can't be initialized");
		verifyTaskData(data, project);
	}

	@Test
	public void testInitializeTaskDataWithProjectKey() throws Exception {
		init(jiraUrl());
		JiraTestUtil.refreshDetails(client);
		final var project = JiraTestUtil.getProject(client, JiraTestUtil.PROJECT1);
		final var data = new TaskData(dataHandler.getAttributeMapper(repository), repository.getConnectorKind(),
				repository.getRepositoryUrl(), "");
		final var res = dataHandler.initializeTaskData(repository, data, new TaskMapping() {
			@Override
			public String getProduct() {
				return project.getKey();
			}
		}, null);
		assertTrue(res, "Task data can't be initialized");
		verifyTaskData(data, project);
	}

	private void verifyTaskData(final TaskData data, final JiraProject project) {
		final var projectAttr = data.getRoot().getAttribute(TaskAttribute.PRODUCT);
		assertEquals(project.getId(), projectAttr.getValue());

		final var priorityAttr = data.getRoot().getAttribute(TaskAttribute.PRIORITY);
		assertNotNull(priorityAttr);
		assertTrue(!priorityAttr.getOptions().isEmpty());

		final var typesAttr = data.getRoot().getAttribute(JiraConstants.ATTRIBUTE_TYPE);
		assertNotNull(typesAttr);
		assertTrue(!typesAttr.getOptions().isEmpty());

		final var componentsAttr = data.getRoot().getAttribute(JiraConstants.ATTRIBUTE_COMPONENTS);
		assertNotNull(componentsAttr);
		assertTrue(!componentsAttr.getOptions().isEmpty());

		final var fixVersionsAttr = data.getRoot().getAttribute(JiraConstants.ATTRIBUTE_FIXVERSIONS);
		assertNotNull(fixVersionsAttr);
		assertTrue(!fixVersionsAttr.getOptions().isEmpty());

		final var affectsVersionsAttr = data.getRoot().getAttribute(JiraConstants.ATTRIBUTE_AFFECTSVERSIONS);
		assertNotNull(affectsVersionsAttr);
		assertTrue(!affectsVersionsAttr.getOptions().isEmpty());
	}

	@Test
	public void testPostTaskDataChangeDescription() throws Exception {
		init(jiraUrl());
		final var issue = JiraTestUtil.createIssue(client, "testDescrPostTaskDataTask");
		var taskDataTestPostTaskData = initTestPostTaskData(issue);

		//change attribute(s), submit and check
		taskDataTestPostTaskData.getRoot().getAttribute(JiraAttribute.DESCRIPTION.id()).setValue("newDescr");
		dataHandler.postTaskData(repository, taskDataTestPostTaskData, null, new NullProgressMonitor());

		taskDataTestPostTaskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("newDescr", taskDataTestPostTaskData.getRoot()
				.getAttribute(JiraAttribute.DESCRIPTION.id())
				.getValue());
	}

	@Test
	public void testPostTaskDataAddComment() throws Exception {
		init(jiraUrl());
		final var issue = JiraTestUtil.createIssue(client, "testCommentPostTaskDataTask");
		var taskDataTestPostTaskData = initTestPostTaskData(issue);

		//change attribute(s), submit and check
		final var taskAttribute = taskDataTestPostTaskData.getRoot().getMappedAttribute(TaskAttribute.COMMENT_NEW);
		taskAttribute.setValue("comment3");
		dataHandler.postTaskData(repository, taskDataTestPostTaskData, null, new NullProgressMonitor());
		taskDataTestPostTaskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		final var comments = JiraTestUtil.getTaskComments(taskDataTestPostTaskData);
		assertEquals(3, comments.size());
		assertEquals("comment1", comments.get(0).getText());
		assertEquals("comment2", comments.get(1).getText());
		assertEquals("comment3", comments.get(2).getText());
	}

	@Test
	public void testPostTaskDataStartProgress() throws Exception {
		init(jiraUrl());
		final var issue = JiraTestUtil.createIssue(client, "testWorkfPostTaskDataTask");
		var taskDataTestPostTaskData = initTestPostTaskData(issue);

		//change attribute(s), submit and check
		final var operation = JiraTestUtil.getOperation(client, issue.getKey(), "start");
		setAttributeValue(taskDataTestPostTaskData, TaskAttribute.OPERATION, operation);
		dataHandler.postTaskData(repository, taskDataTestPostTaskData, null, new NullProgressMonitor());
		taskDataTestPostTaskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("3", taskDataTestPostTaskData.getRoot().getAttribute(TaskAttribute.STATUS).getValue());
	}

	@Test
	public void testPostTaskDataStartProgressChangeDescription() throws Exception {
		init(jiraUrl());
		final var issue = JiraTestUtil.createIssue(client, "testDescrWorkfPostTaskDataTask");
		var taskDataTestPostTaskData = initTestPostTaskData(issue);

		//change attribute(s), submit and check
		taskDataTestPostTaskData.getRoot().getAttribute(JiraAttribute.DESCRIPTION.id()).setValue("newerDescr");
		final var operation = JiraTestUtil.getOperation(client, issue.getKey(), "start");
		setAttributeValue(taskDataTestPostTaskData, TaskAttribute.OPERATION, operation);
		dataHandler.postTaskData(repository, taskDataTestPostTaskData, null, new NullProgressMonitor());
		taskDataTestPostTaskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("3", taskDataTestPostTaskData.getRoot().getAttribute(TaskAttribute.STATUS).getValue());
		assertEquals("newerDescr", taskDataTestPostTaskData.getRoot()
				.getAttribute(JiraAttribute.DESCRIPTION.id())
				.getValue());
	}

	@Test
	public void testPostTaskDataStartProgressAddComment() throws Exception {
		init(jiraUrl());
		final var issue = JiraTestUtil.createIssue(client, "testCommentWorkfPostTaskDataTask");
		var taskDataTestPostTaskData = initTestPostTaskData(issue);

		final var taskAttribute = taskDataTestPostTaskData.getRoot().getMappedAttribute(TaskAttribute.COMMENT_NEW);
		taskAttribute.setValue("comment3");
		final var operation = JiraTestUtil.getOperation(client, issue.getKey(), "start");
		setAttributeValue(taskDataTestPostTaskData, TaskAttribute.OPERATION, operation);
		dataHandler.postTaskData(repository, taskDataTestPostTaskData, null, new NullProgressMonitor());

		taskDataTestPostTaskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("3", taskDataTestPostTaskData.getRoot().getAttribute(TaskAttribute.STATUS).getValue());
		final var comments = JiraTestUtil.getTaskComments(taskDataTestPostTaskData);
		assertEquals(3, comments.size());
		assertEquals("comment1", comments.get(0).getText());
		assertEquals("comment2", comments.get(1).getText());
		assertEquals("comment3", comments.get(2).getText());
	}

	@Test
	public void testPostTaskDataStartProgressAddCommentChangeDescription() throws Exception {
		init(jiraUrl());
		final var issue = JiraTestUtil.createIssue(client, "testDescrCommentWorkfPostTaskDataTask");
		var taskDataTestPostTaskData = initTestPostTaskData(issue);

		//change attribute(s), submit and check
		taskDataTestPostTaskData.getRoot().getAttribute(JiraAttribute.DESCRIPTION.id()).setValue("newestDescr");
		final var taskAttribute = taskDataTestPostTaskData.getRoot().getMappedAttribute(TaskAttribute.COMMENT_NEW);
		taskAttribute.setValue("comment3");
		final var operation = JiraTestUtil.getOperation(client, issue.getKey(), "start");
		setAttributeValue(taskDataTestPostTaskData, TaskAttribute.OPERATION, operation);
		dataHandler.postTaskData(repository, taskDataTestPostTaskData, null, new NullProgressMonitor());

		taskDataTestPostTaskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("3", taskDataTestPostTaskData.getRoot().getAttribute(TaskAttribute.STATUS).getValue());
		final var comments = JiraTestUtil.getTaskComments(taskDataTestPostTaskData);
		assertEquals(3, comments.size());
		assertEquals("comment1", comments.get(0).getText());
		assertEquals("comment2", comments.get(1).getText());
		assertEquals("comment3", comments.get(2).getText());
		assertEquals(taskDataTestPostTaskData.getRoot().getAttribute(JiraAttribute.DESCRIPTION.id()).getValue(),
				"newestDescr");
	}

	@Test
	public void testPostTaskDataStartProgressChangeAttributes() throws Exception {
		init(jiraUrl());
		final var issue = JiraTestUtil.createIssue(client, "testWorkfAndOtherAttrPostTaskDataTask");
		var taskDataTestPostTaskData = initTestPostTaskData(issue);

		//change attribute(s), submit and check
		taskDataTestPostTaskData.getRoot().getAttribute(JiraAttribute.PRIORITY.id()).setValue(JiraPriority.BLOCKER_ID);
		taskDataTestPostTaskData.getRoot().getAttribute(JiraAttribute.SUMMARY.id()).setValue("newSummary");
		taskDataTestPostTaskData.getRoot().getAttribute(JiraAttribute.DESCRIPTION.id()).setValue("newestDescr");
		final var taskAttribute = taskDataTestPostTaskData.getRoot().getMappedAttribute(TaskAttribute.COMMENT_NEW);
		taskAttribute.setValue("comment3");
		final var operation = JiraTestUtil.getOperation(client, issue.getKey(), "start");
		setAttributeValue(taskDataTestPostTaskData, TaskAttribute.OPERATION, operation);
		dataHandler.postTaskData(repository, taskDataTestPostTaskData, null, new NullProgressMonitor());

		taskDataTestPostTaskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("3", taskDataTestPostTaskData.getRoot().getAttribute(TaskAttribute.STATUS).getValue());
		final var comments = JiraTestUtil.getTaskComments(taskDataTestPostTaskData);
		assertEquals(3, comments.size());
		assertEquals("comment1", comments.get(0).getText());
		assertEquals("comment2", comments.get(1).getText());
		assertEquals("comment3", comments.get(2).getText());
		assertEquals(JiraPriority.BLOCKER_ID, taskDataTestPostTaskData.getRoot()
				.getAttribute(JiraAttribute.PRIORITY.id())
				.getValue());
		assertEquals("newSummary", taskDataTestPostTaskData.getRoot()
				.getAttribute(JiraAttribute.SUMMARY.id())
				.getValue());
		assertEquals("newestDescr", taskDataTestPostTaskData.getRoot()
				.getAttribute(JiraAttribute.DESCRIPTION.id())
				.getValue());
	}

	private TaskData initTestPostTaskData(final JiraIssue issue) throws Exception {
		final var summary = issue.getSummary();
		issue.setDescription("descr");
		issue.setPriority(new JiraPriority(JiraPriority.MINOR_ID));
		client.updateIssue(issue, "comment1", null, new NullProgressMonitor());
		// make sure comments are created in the right order
		Thread.sleep(750);
		client.updateIssue(issue, "comment2", null, new NullProgressMonitor());
		final var task = JiraTestUtil.createTask(repository, issue.getKey());
		assertEquals(summary, task.getSummary());
		assertEquals(false, task.isCompleted());
		final var taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals(summary, taskData.getRoot().getAttribute(JiraAttribute.SUMMARY.id()).getValue());
		assertEquals("descr", taskData.getRoot().getAttribute(JiraAttribute.DESCRIPTION.id()).getValue());
		assertEquals(JiraPriority.MINOR_ID, taskData.getRoot().getAttribute(JiraAttribute.PRIORITY.id()).getValue());
		final var comments = JiraTestUtil.getTaskComments(taskData);
		assertEquals(2, comments.size());
		assertEquals("comment1", comments.get(0).getText());
		assertEquals("comment2", comments.get(1).getText());
		return taskData;
	}

	/**
	 * Adds a comment to a task which user doesn't have edit permission for.
	 */
	@Test
	public void testPostTaskDataCommentWithoutEditPermission() throws Exception {
		init(jiraUrl(), PrivilegeLevel.USER);

		final var issue = JiraTestUtil.createIssue(client, "testWithoutEditPermission");

		var taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		taskData.getRoot().getAttribute(JiraAttribute.SUMMARY.id()).setValue("new summary");
		taskData.getRoot().getAttribute(JiraAttribute.COMMENT_NEW.id()).setValue("comment");
		dataHandler.postTaskData(repository, taskData,
				buildChanged(taskData.getRoot(), JiraAttribute.SUMMARY, JiraAttribute.COMMENT_NEW),
				new NullProgressMonitor());
		assertNull(taskData.getRoot().getAttribute(JiraConstants.ATTRIBUTE_READ_ONLY));

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("new summary", taskData.getRoot().getAttribute(JiraAttribute.SUMMARY.id()).getValue());
		var task = JiraTestUtil.createTask(repository, taskData.getTaskId());
		var comments = JiraTestUtil.getTaskComments(task);
		assertEquals(1, comments.size());
		assertEquals("comment", comments.get(0).getText());
		assertNull(taskData.getRoot().getAttribute(JiraConstants.ATTRIBUTE_READ_ONLY));

		setUp();
		init(jiraUrl(), PrivilegeLevel.READ_ONLY);

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertNotNull(taskData.getRoot().getAttribute(JiraConstants.ATTRIBUTE_READ_ONLY));

		taskData.getRoot().getAttribute(JiraAttribute.COMMENT_NEW.id()).setValue("new comment");

		dataHandler.postTaskData(repository, taskData, buildChanged(taskData.getRoot(), JiraAttribute.COMMENT_NEW),
				new NullProgressMonitor());

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("new summary", taskData.getRoot().getAttribute(JiraAttribute.SUMMARY.id()).getValue());
		task = JiraTestUtil.createTask(repository, taskData.getTaskId());
		comments = JiraTestUtil.getTaskComments(task);
		assertEquals(2, comments.size());
		assertEquals("comment", comments.get(0).getText());
		assertEquals("new comment", comments.get(1).getText());
		assertNotNull(taskData.getRoot().getAttribute(JiraConstants.ATTRIBUTE_READ_ONLY));
	}

	private Set<TaskAttribute> buildChanged(final TaskAttribute root, final JiraAttribute... attrs) {
		final Set<TaskAttribute> changed = new HashSet<>();
		for (final JiraAttribute ja : attrs) {
			changed.add(root.getAttribute(ja.id()));
		}
		return changed;
	}

	/**
	 * Reassigns a task for which the user does not have edit permissions for.
	 */
	@Test
	public void testPostTaskDataAssignWithoutEditPermission() throws Exception {
		init(jiraUrl(), PrivilegeLevel.USER);

		final var userCredentials = TestUtil.readCredentials(PrivilegeLevel.USER);
		final var issue = JiraTestUtil.createIssue(client, "testWithoutEditPermission");

		var taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		taskData.getRoot().getAttribute(JiraAttribute.USER_ASSIGNED.id()).setValue("");

		// cannot unassing issue
		try {
			dataHandler.postTaskData(repository, taskData,
					buildChanged(taskData.getRoot(), JiraAttribute.USER_ASSIGNED), new NullProgressMonitor());
		} catch (final CoreException e) {
			assertTrue(e.getMessage().contains("Issues must be assigned."));
		}
		//		assertNull(taskData.getRoot().getAttribute(JiraConstants.ATTRIBUTE_READ_ONLY));

		setUp();
		init(jiraUrl(), PrivilegeLevel.READ_ONLY);

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());

		taskData.getRoot().getAttribute(JiraAttribute.USER_ASSIGNED.id()).setValue(userCredentials.username);

		// no permission to set assignee
		try {
			dataHandler.postTaskData(repository, taskData,
					buildChanged(taskData.getRoot(), JiraAttribute.USER_ASSIGNED), new NullProgressMonitor());
		} catch (final CoreException e) {
			assertTrue(e.getMessage().contains(
					"Field 'assignee' cannot be set. It is not on the appropriate screen, or unknown."));
		}

		//		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		//		assertEquals(userCredentials.username, taskData.getRoot()
		//				.getAttribute(JiraAttribute.USER_ASSIGNED.id())
		//				.getValue());
	}

	private String jiraUrl() {
		return JiraFixture.current().getRepositoryUrl();
	}

}
