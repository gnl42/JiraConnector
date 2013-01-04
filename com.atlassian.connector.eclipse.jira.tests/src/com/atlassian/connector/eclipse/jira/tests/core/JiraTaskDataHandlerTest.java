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

package com.atlassian.connector.eclipse.jira.tests.core;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskRelation;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tests.util.TestUtil;
import org.eclipse.mylyn.tests.util.TestUtil.Credentials;
import org.eclipse.mylyn.tests.util.TestUtil.PrivilegeLevel;

import com.atlassian.connector.eclipse.internal.jira.core.IJiraConstants;
import com.atlassian.connector.eclipse.internal.jira.core.JiraAttribute;
import com.atlassian.connector.eclipse.internal.jira.core.JiraClientFactory;
import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;
import com.atlassian.connector.eclipse.internal.jira.core.JiraRepositoryConnector;
import com.atlassian.connector.eclipse.internal.jira.core.JiraTaskDataHandler;
import com.atlassian.connector.eclipse.internal.jira.core.model.Component;
import com.atlassian.connector.eclipse.internal.jira.core.model.CustomField;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus;
import com.atlassian.connector.eclipse.internal.jira.core.model.Priority;
import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.model.Resolution;
import com.atlassian.connector.eclipse.internal.jira.core.model.SecurityLevel;
import com.atlassian.connector.eclipse.internal.jira.core.model.Version;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.connector.eclipse.internal.jira.core.util.JiraUtil;
import com.atlassian.connector.eclipse.jira.tests.util.JiraFixture;
import com.atlassian.connector.eclipse.jira.tests.util.JiraTestUtil;
import com.atlassian.connector.eclipse.jira.tests.util.MockJiraClient;
import com.atlassian.connector.eclipse.jira.tests.util.MockJiraClientFactory;

/**
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 * @author Thomas Ehrnhoefer
 */
public class JiraTaskDataHandlerTest extends TestCase {

	private static final String SECURITY_LEVEL_USERS = "10001";

	private static final String SECURITY_LEVEL_DEVELOPERS = "10000";

	private static final String SECURITY_LEVEL_NONE = "-1";

	private TaskRepository repository;

	private JiraTaskDataHandler dataHandler;

	private JiraClient client;

	private JiraRepositoryConnector connector;

	private String customFieldId;

	private String customFieldName;

	@Override
	protected void setUp() throws Exception {
		JiraTestUtil.setUp();
		connector = (JiraRepositoryConnector) TasksUi.getRepositoryConnector(JiraCorePlugin.CONNECTOR_KIND);
		dataHandler = connector.getTaskDataHandler();
	}

	@Override
	protected void tearDown() throws Exception {
		JiraTestUtil.tearDown();
	}

	protected void init(String url) throws Exception {
		init(url, PrivilegeLevel.USER);
	}

	protected void init(String url, PrivilegeLevel level) throws Exception {
		repository = JiraTestUtil.init(url, level);
		client = JiraClientFactory.getDefault().getJiraClient(repository);

//		customFieldId = JiraTestUtils.getCustomField(server, customFieldName);
//		assertNotNull("Unable to find custom field id", customFieldId);
		customFieldName = "Free Text";
		customFieldId = "customfield_10011";
	}

	public void testGetTaskData() throws Exception {
		init(jiraUrl());

		JiraIssue issue = JiraTestUtil.newIssue(client, "testUpdateTask");
		issue.setEstimate(600);

		// update project cache (otherwise there are no components and we get NPE)
		client.getCache().refreshProjectDetails(issue.getProject().getId(), new NullProgressMonitor());
		assertTrue(client.getCache().getProjectById(issue.getProject().getId()).hasDetails());

		Component component = issue.getProject().getComponents()[0];
		issue.setComponents(new Component[] { component });
		issue = client.createIssue(issue, null);
		assertEquals(600, issue.getInitialEstimate());

		String commentText = "line1\nline2\n\nline4\n\n\n";
		client.addCommentToIssue(issue.getKey(), commentText, null);

		TaskData taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		ITask task = JiraTestUtil.createTask(repository, taskData.getTaskId());
		List<ITaskComment> comments = JiraTestUtil.getTaskComments(task);
		assertEquals(1, comments.size());
		assertEquals(commentText, comments.get(0).getText());
		assertEquals("600", taskData.getRoot().getAttribute(JiraAttribute.ESTIMATE.id()).getValue());
		assertEquals(component.getId(), taskData.getRoot().getAttribute(JiraAttribute.COMPONENTS.id()).getValue());
	}

	public void testGetTaskDataWhenProjectIsMissingDetails() throws Exception {
		init(jiraUrl());

		JiraIssue issue = JiraTestUtil.newIssue(client, "testUpdateTask");
		issue.setEstimate(600);

		// update project cache (otherwise there are no components and we get NPE)
		client.getCache().refreshProjectDetails(issue.getProject().getId(), new NullProgressMonitor());
		assertTrue(client.getCache().getProjectById(issue.getProject().getId()).hasDetails());

		Component component = issue.getProject().getComponents()[0];
		issue.setComponents(new Component[] { component });
		issue = client.createIssue(issue, null);
		assertEquals(600, issue.getInitialEstimate());

		String commentText = "line1\nline2\n\nline4\n\n\n";
		client.addCommentToIssue(issue.getKey(), commentText, null);

		// mark that project requires metadata refresh, getTaskData should update it automatically
		client.getCache().getProjectById(issue.getProject().getId()).setDetails(false);

		TaskData taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		ITask task = JiraTestUtil.createTask(repository, taskData.getTaskId());
		List<ITaskComment> comments = JiraTestUtil.getTaskComments(task);
		assertEquals(1, comments.size());
		assertEquals(commentText, comments.get(0).getText());
		assertEquals("600", taskData.getRoot().getAttribute(JiraAttribute.ESTIMATE.id()).getValue());
		assertEquals(component.getId(), taskData.getRoot().getAttribute(JiraAttribute.COMPONENTS.id()).getValue());
		assertTrue(client.getCache().getProjectById(issue.getProject().getId()).hasDetails());
	}

	public void testCreateTaskData() throws JiraException {
		JiraIssue issue = new JiraIssue();
		final Project project = MockJiraClient.createProject();
		issue.setProject(project);
		issue.setAssignee("eu");
		issue.setId("100");
		issue.setKey("FOO-1");
		issue.setAssignee("boo");
		issue.setSummary("summary");
		issue.setDescription("description");
		JiraStatus status = new JiraStatus("1"); // 1 == open
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
		issue.setUrl("http://mylyn");

		project.setVersions(new Version[] { MockJiraClient.createVersion("3", "3.0"),
				MockJiraClient.createVersion("1", "1.0"), MockJiraClient.createVersion("2", "2.0") });
		project.setComponents(components);
		project.setDetails(true);

		TaskRepository repository = new TaskRepository(JiraCorePlugin.CONNECTOR_KIND, "http://jira.codehaus.org/");
		MockJiraClient client = new MockJiraClient(repository.getRepositoryUrl()) {
			@Override
			public Project[] getProjects(IProgressMonitor monitor) throws JiraException {
				return new Project[] { project };
			};
		};
		JiraTaskDataHandler dataHandler = new JiraTaskDataHandler(new MockJiraClientFactory(client));
		TaskData data = dataHandler.createTaskData(repository, client, issue, null, null);

		assertValues(data, TaskAttribute.TASK_KEY, "FOO-1");
		assertValues(data, TaskAttribute.STATUS, "open");
		assertValues(data, TaskAttribute.PRIORITY, Priority.BLOCKER_ID);
		assertValues(data, TaskAttribute.PRODUCT, "PRONE");
		assertValues(data, IJiraConstants.ATTRIBUTE_TYPE, "3");
		assertValues(data, TaskAttribute.DATE_CREATION, JiraUtil.dateToString(issue.getCreated()));
		assertValues(data, IJiraConstants.ATTRIBUTE_COMPONENTS, "component2", "component3");
		assertValues(data, IJiraConstants.ATTRIBUTE_AFFECTSVERSIONS, "1.0", "2.0");
		assertValues(data, IJiraConstants.ATTRIBUTE_FIXVERSIONS, "3.0");
		assertValues(data, TaskAttribute.SUMMARY, "summary");
		assertValues(data, TaskAttribute.DESCRIPTION, "description");
	}

	private void assertValues(TaskData data, String key, String... values) {
		TaskAttribute attribute = data.getRoot().getAttribute(key);
		int n = 0;
		for (String value : attribute.getValues()) {
			if (!attribute.getOptions().isEmpty()) {
				assertEquals(values[n], attribute.getOption(value));
			} else {
				assertEquals(values[n], attribute.getValue());
			}
			n++;
		}
	}

	public void testUpdateTaskCustomFields() throws Exception {
		init(jiraUrl());

		Date today = new SimpleDateFormat("dd/MMM/yy", Locale.ENGLISH).parse("1/Jun/06");
		String dueDate = JiraUtil.dateToString(today);
		JiraIssue issue = JiraTestUtil.createIssue(client, "testUpdateTask");
		ITask task = JiraTestUtil.createTask(repository, issue.getKey());
		assertEquals("testUpdateTask", task.getSummary());
		assertEquals(false, task.isCompleted());
		assertNull(task.getDueDate());

		String issueKey = issue.getKey();
		TaskData taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		TaskAttribute attribute = dataHandler.createAttribute(taskData, JiraAttribute.DUE_DATE);
		attribute.setValue(dueDate);
		attribute = taskData.getRoot().createAttribute(dataHandler.mapCommonAttributeKey(customFieldId));
		attribute.addValue("foo");
		taskData.getRoot().getAttribute(TaskAttribute.COMMENT_NEW).addValue("add comment");
		dataHandler.postTaskData(repository, taskData, null, new NullProgressMonitor());

		issue = client.getIssueByKey(issueKey, null);
		assertCustomField(issue, customFieldId, customFieldName, "foo");
		assertTrue("Invalid issue due date " + issue.getDue(), today.equals(issue.getDue()));

		{
			String operation = JiraTestUtil.getOperation(client, issue.getKey(), "resolve");
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
		assertTrue("Invalid task due date " + task.getDueDate(), today.equals(task.getDueDate()));

		issue = client.getIssueByKey(issueKey, null);
		assertCustomField(issue, customFieldId, customFieldName, "foo");
		assertTrue("Invalid issue due date " + issue.getDue(), today.equals(issue.getDue()));

		{
			String operation = JiraTestUtil.getOperation(client, issue.getKey(), "close");
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
		assertCustomField(issue, customFieldId, customFieldName, "foo");
		assertTrue("Invalid issue due date " + issue.getDue(), today.equals(issue.getDue()));
	}

	private void setAttributeValue(TaskData taskData, String attributeId, String value) {
		taskData.getRoot().getMappedAttribute(attributeId).setValue(value);
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
		init(jiraUrl());

		JiraIssue parentIssue = JiraTestUtil.createIssue(client, "testSubTask");

		JiraIssue subTaskIssue = JiraTestUtil.newSubTask(client, parentIssue, "testSubTaskChild");
		subTaskIssue = client.createIssue(subTaskIssue, null);

		TaskData taskData = dataHandler.getTaskData(repository, parentIssue.getId(), new NullProgressMonitor());
		TaskAttribute typeAttribute = taskData.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_TYPE);
		assertFalse(typeAttribute.getMetaData().isReadOnly());
		assertTrue(typeAttribute.getOptions().size() > 0);
		Collection<TaskRelation> ids = connector.getTaskRelations(taskData);
		assertEquals(1, ids.size());
		assertEquals(subTaskIssue.getId(), ids.iterator().next().getTaskId());

		taskData = dataHandler.getTaskData(repository, subTaskIssue.getId(), new NullProgressMonitor());
		assertEquals(subTaskIssue.getId(), taskData.getTaskId());
		assertEquals(subTaskIssue.getKey(), taskData.getRoot().getAttribute(JiraAttribute.ISSUE_KEY.id()).getValue());
		typeAttribute = taskData.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_TYPE);
		assertTrue(typeAttribute.getMetaData().isReadOnly());
		assertEquals(1, typeAttribute.getOptions().size());
	}

	public void testPostTaskDataSubTask() throws Exception {
		init(jiraUrl());

		JiraIssue parentIssue = JiraTestUtil.createIssue(client, "testUpdateSubTask");
		JiraIssue subTaskIssue = JiraTestUtil.newSubTask(client, parentIssue, "testUpdateSubTaskChild");
		subTaskIssue = client.createIssue(subTaskIssue, null);
		TaskData taskData = dataHandler.getTaskData(repository, subTaskIssue.getId(), new NullProgressMonitor());
		assertEquals(subTaskIssue.getKey(), taskData.getRoot().getAttribute(JiraAttribute.ISSUE_KEY.id()).getValue());

		taskData.getRoot().getAttribute(JiraAttribute.DESCRIPTION.id()).setValue("new description");
		dataHandler.postTaskData(repository, taskData, null, new NullProgressMonitor());
		JiraIssue updatedSubTaskIssue = client.getIssueByKey(
				taskData.getRoot().getAttribute(JiraAttribute.ISSUE_KEY.id()).getValue(), null);
		assertEquals(subTaskIssue.getId(), updatedSubTaskIssue.getId());
		assertEquals("new description", updatedSubTaskIssue.getDescription());
	}

	public void testInitializeSubTask() throws Exception {
		init(jiraUrl());

		JiraIssue parentIssue = JiraTestUtil.createIssue(client, "testInitializeSubTask");
		TaskData parentTaskData = dataHandler.getTaskData(repository, parentIssue.getId(), new NullProgressMonitor());
		TaskData taskData = new TaskData(dataHandler.getAttributeMapper(repository), JiraCorePlugin.CONNECTOR_KIND,
				repository.getRepositoryUrl(), "");
		dataHandler.initializeSubTaskData(repository, taskData, parentTaskData, new NullProgressMonitor());
		assertEquals(parentIssue.getId(), taskData.getRoot()
				.getAttribute(IJiraConstants.ATTRIBUTE_ISSUE_PARENT_ID)
				.getValue());
		assertEquals(parentIssue.getProject().getId(), taskData.getRoot()
				.getAttribute(TaskAttribute.PRODUCT)
				.getValue());
		assertNotNull(taskData.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_TYPE));
	}

	public void testSecurityLevelNoLevelsDefined() throws Exception {
		init(jiraUrl());

		JiraIssue issue = JiraTestUtil.newIssue(client, "testSecurityLevel");
		issue = JiraTestUtil.createIssue(client, issue);

		TaskData taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertNull(taskData.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_SECURITY_LEVEL));
	}

	public void testUpdateSecurityLevel() throws Exception {
		init(jiraUrl());

		// test security level is set to none for new issues
		JiraIssue issue = JiraTestUtil.newIssue(client, "testSecurityLevel");
		issue.setProject(client.getCache().getProjectByKey("SECURITY"));
		issue = JiraTestUtil.createIssue(client, issue);

		TaskData taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		TaskAttribute attribute = taskData.getRoot().getAttribute(JiraAttribute.SECURITY_LEVEL.id());
		assertNotNull(attribute);
		assertEquals("-1", attribute.getValue());
		assertEquals(4, attribute.getOptions().size());
		assertFalse(attribute.getMetaData().isReadOnly());

		// change security level through JiraClient
		SecurityLevel securityLevel = new SecurityLevel();
		securityLevel.setId(SECURITY_LEVEL_DEVELOPERS);
		issue.setSecurityLevel(securityLevel);
		client.updateIssue(issue, "", null);

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

	public void testSetSecurityLevelToNone() throws Exception {
		init(jiraUrl());

		JiraIssue issue = JiraTestUtil.newIssue(client, "testSecurityLevel");
		issue.setProject(client.getCache().getProjectByKey("SECURITY"));
		issue.setSecurityLevel(new SecurityLevel(SECURITY_LEVEL_DEVELOPERS));
		issue = JiraTestUtil.createIssue(client, issue);

		TaskData taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		TaskAttribute attribute = taskData.getRoot().getAttribute(JiraAttribute.SECURITY_LEVEL.id());
		assertNotNull(attribute);
		assertEquals(SECURITY_LEVEL_DEVELOPERS, attribute.getValue());

		attribute.setValue(SecurityLevel.NONE.getId());
		dataHandler.postTaskData(repository, taskData, null, new NullProgressMonitor());
		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		attribute = taskData.getRoot().getAttribute(JiraAttribute.SECURITY_LEVEL.id());
		assertNotNull(attribute);
		assertEquals("-1", attribute.getValue());
		assertEquals("None", attribute.getOption("-1"));
	}

	public void testPostTaskDataCreateTaskWithSecurityLevel() throws Exception {
		init(jiraUrl());

		// initialize task data
		JiraTestUtil.refreshDetails(client);
		final Project project = client.getCache().getProjectByKey("SECURITY");
		TaskData taskData = new TaskData(dataHandler.getAttributeMapper(repository), repository.getConnectorKind(),
				repository.getRepositoryUrl(), "");
		boolean result = dataHandler.initializeTaskData(repository, taskData, new TaskMapping() {
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
		TaskAttribute attribute = taskData.getRoot().getAttribute(JiraAttribute.SECURITY_LEVEL.id());
		assertNotNull(attribute);
		attribute.setValue(SECURITY_LEVEL_DEVELOPERS);
		RepositoryResponse response = dataHandler.postTaskData(repository, taskData, null, new NullProgressMonitor());

		// verify security level
		taskData = dataHandler.getTaskData(repository, response.getTaskId(), new NullProgressMonitor());
		attribute = taskData.getRoot().getAttribute(JiraAttribute.SECURITY_LEVEL.id());
		assertNotNull(attribute);
		assertEquals(SECURITY_LEVEL_DEVELOPERS, attribute.getValue());
	}

	/**
	 * Verifies that cached operations are refreshed from the repository when the status of an issue changes.
	 */
	public void testCachedOperationsAfterChangingState() throws Exception {
		init(jiraUrl());

		JiraIssue issue = JiraTestUtil.createIssue(client, "testChangeState");

		TaskData taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		List<TaskAttribute> operations = taskData.getAttributeMapper().getAttributesByType(taskData,
				TaskAttribute.TYPE_OPERATION);
		assertNotNull(operations);
		assertEquals(5, operations.size());
		assertEquals("5", operations.get(3).getValue());
		assertEquals("2", operations.get(4).getValue());

		// resolve issue
		issue.setResolution(client.getCache().getResolutionById(Resolution.FIXED_ID));
		client.advanceIssueWorkflow(issue, "5", "", null);
		issue = client.getIssueByKey(issue.getKey(), null);

		TaskData newTaskData = dataHandler.createTaskData(repository, client, issue, taskData, null);
		operations = taskData.getAttributeMapper().getAttributesByType(newTaskData, TaskAttribute.TYPE_OPERATION);
		assertNotNull(operations);
		assertEquals(4, operations.size());
		assertEquals("3", operations.get(3).getValue());

		issue.setSummary("changed");
		client.updateIssue(issue, "", null);
		newTaskData.getRoot().removeAttribute(operations.get(0).getId());

		// make sure cached operations are used
		newTaskData = dataHandler.createTaskData(repository, client, issue, newTaskData, null);
		operations = taskData.getAttributeMapper().getAttributesByType(newTaskData, TaskAttribute.TYPE_OPERATION);
		assertNotNull(operations);
		assertEquals(3, operations.size());
		assertEquals("3", operations.get(2).getValue());
	}

	public void testReadOnly() throws Exception {
		init(jiraUrl(), PrivilegeLevel.GUEST);

		JiraIssue issue = JiraTestUtil.createIssueWithoutAssignee(client, "testReadOnly");

		TaskData taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertNotNull(taskData.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_READ_ONLY));

		taskData.getRoot().getAttribute(JiraAttribute.SUMMARY.id()).setValue("new summary");
		taskData.getRoot().getAttribute(JiraAttribute.COMMENT_NEW.id()).setValue("comment");
		dataHandler.postTaskData(repository, taskData, null, new NullProgressMonitor());

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("testReadOnly", taskData.getRoot().getAttribute(JiraAttribute.SUMMARY.id()).getValue());
		ITask task = JiraTestUtil.createTask(repository, taskData.getTaskId());
		List<ITaskComment> comments = JiraTestUtil.getTaskComments(task);
		assertEquals(1, comments.size());
		assertEquals("comment", comments.get(0).getText());
		assertNotNull(taskData.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_READ_ONLY));

		setUp();
		init(jiraUrl(), PrivilegeLevel.USER);

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertNull(taskData.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_READ_ONLY));

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
		assertNull(taskData.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_READ_ONLY));
	}

	public void testClosedIssueNotEditable() throws Exception {
		init(jiraUrl());

		JiraIssue issue = JiraTestUtil.createIssue(client, "testEditClosed");

		TaskData taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertNull(taskData.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_READ_ONLY));
		assertFalse(taskData.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_FIXVERSIONS).getMetaData().isReadOnly());

		issue.setResolution(new Resolution(Resolution.FIXED_ID));
		// close
		client.advanceIssueWorkflow(issue, "2", "", null);

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertNull(taskData.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_READ_ONLY));
		assertTrue(taskData.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_FIXVERSIONS).getMetaData().isReadOnly());

		taskData.getRoot().getAttribute(JiraAttribute.SUMMARY.id()).setValue("new summary");
		dataHandler.postTaskData(repository, taskData, null, new NullProgressMonitor());
		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("testEditClosed", taskData.getRoot().getAttribute(JiraAttribute.SUMMARY.id()).getValue());
	}

	public void testInitializeTaskDataNoProject() throws Exception {
		init(jiraUrl());
		TaskData data = new TaskData(dataHandler.getAttributeMapper(repository), repository.getConnectorKind(),
				repository.getRepositoryUrl(), "");
		boolean res = dataHandler.initializeTaskData(repository, data, null, null);
		assertFalse("Task data shouldn't be initialized without project", res);
	}

	public void testInitializeTaskDataWithProjectName() throws Exception {
		init(jiraUrl());
		JiraTestUtil.refreshDetails(client);
		final Project project = JiraTestUtil.getProject(client, JiraTestUtil.PROJECT1);
		TaskData data = new TaskData(dataHandler.getAttributeMapper(repository), repository.getConnectorKind(),
				repository.getRepositoryUrl(), "");
		boolean res = dataHandler.initializeTaskData(repository, data, new TaskMapping() {
			@Override
			public String getProduct() {
				return project.getName();
			}
		}, null);
		assertTrue("Task data can't be initialized", res);
		verifyTaskData(data, project);
	}

	public void testInitializeTaskDataWithProjectKey() throws Exception {
		init(jiraUrl());
		JiraTestUtil.refreshDetails(client);
		final Project project = JiraTestUtil.getProject(client, JiraTestUtil.PROJECT1);
		TaskData data = new TaskData(dataHandler.getAttributeMapper(repository), repository.getConnectorKind(),
				repository.getRepositoryUrl(), "");
		boolean res = dataHandler.initializeTaskData(repository, data, new TaskMapping() {
			@Override
			public String getProduct() {
				return project.getKey();
			}
		}, null);
		assertTrue("Task data can't be initialized", res);
		verifyTaskData(data, project);
	}

	private void verifyTaskData(TaskData data, Project project) {
		TaskAttribute projectAttr = data.getRoot().getAttribute(TaskAttribute.PRODUCT);
		assertEquals(project.getId(), projectAttr.getValue());

		TaskAttribute priorityAttr = data.getRoot().getAttribute(TaskAttribute.PRIORITY);
		assertNotNull(priorityAttr);
		assertTrue(!priorityAttr.getOptions().isEmpty());

		TaskAttribute typesAttr = data.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_TYPE);
		assertNotNull(typesAttr);
		assertTrue(!typesAttr.getOptions().isEmpty());

		TaskAttribute componentsAttr = data.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_COMPONENTS);
		assertNotNull(componentsAttr);
		assertTrue(!componentsAttr.getOptions().isEmpty());

		TaskAttribute fixVersionsAttr = data.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_FIXVERSIONS);
		assertNotNull(fixVersionsAttr);
		assertTrue(!fixVersionsAttr.getOptions().isEmpty());

		TaskAttribute affectsVersionsAttr = data.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_AFFECTSVERSIONS);
		assertNotNull(affectsVersionsAttr);
		assertTrue(!affectsVersionsAttr.getOptions().isEmpty());
	}

	public void testPostTaskDataChangeDescription() throws Exception {
		init(jiraUrl());
		JiraIssue issue = JiraTestUtil.createIssue(client, "testDescrPostTaskDataTask");
		TaskData taskDataTestPostTaskData = initTestPostTaskData(issue);

		//change attribute(s), submit and check
		taskDataTestPostTaskData.getRoot().getAttribute(JiraAttribute.DESCRIPTION.id()).setValue("newDescr");
		dataHandler.postTaskData(repository, taskDataTestPostTaskData, null, new NullProgressMonitor());

		taskDataTestPostTaskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("newDescr", taskDataTestPostTaskData.getRoot()
				.getAttribute(JiraAttribute.DESCRIPTION.id())
				.getValue());
	}

	public void testPostTaskDataAddComment() throws Exception {
		init(jiraUrl());
		JiraIssue issue = JiraTestUtil.createIssue(client, "testCommentPostTaskDataTask");
		TaskData taskDataTestPostTaskData = initTestPostTaskData(issue);

		//change attribute(s), submit and check
		TaskAttribute taskAttribute = taskDataTestPostTaskData.getRoot().getMappedAttribute(TaskAttribute.COMMENT_NEW);
		taskAttribute.setValue("comment3");
		dataHandler.postTaskData(repository, taskDataTestPostTaskData, null, new NullProgressMonitor());
		taskDataTestPostTaskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		List<ITaskComment> comments = JiraTestUtil.getTaskComments(taskDataTestPostTaskData);
		assertEquals(3, comments.size());
		assertEquals("comment1", comments.get(0).getText());
		assertEquals("comment2", comments.get(1).getText());
		assertEquals("comment3", comments.get(2).getText());
	}

	public void testPostTaskDataStartProgress() throws Exception {
		init(jiraUrl());
		JiraIssue issue = JiraTestUtil.createIssue(client, "testWorkfPostTaskDataTask");
		TaskData taskDataTestPostTaskData = initTestPostTaskData(issue);

		//change attribute(s), submit and check
		String operation = JiraTestUtil.getOperation(client, issue.getKey(), "start");
		setAttributeValue(taskDataTestPostTaskData, TaskAttribute.OPERATION, operation);
		dataHandler.postTaskData(repository, taskDataTestPostTaskData, null, new NullProgressMonitor());
		taskDataTestPostTaskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("3", taskDataTestPostTaskData.getRoot().getAttribute(TaskAttribute.STATUS).getValue());
	}

	public void testPostTaskDataStartProgressChangeDescription() throws Exception {
		init(jiraUrl());
		JiraIssue issue = JiraTestUtil.createIssue(client, "testDescrWorkfPostTaskDataTask");
		TaskData taskDataTestPostTaskData = initTestPostTaskData(issue);

		//change attribute(s), submit and check
		taskDataTestPostTaskData.getRoot().getAttribute(JiraAttribute.DESCRIPTION.id()).setValue("newerDescr");
		String operation = JiraTestUtil.getOperation(client, issue.getKey(), "start");
		setAttributeValue(taskDataTestPostTaskData, TaskAttribute.OPERATION, operation);
		dataHandler.postTaskData(repository, taskDataTestPostTaskData, null, new NullProgressMonitor());
		taskDataTestPostTaskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("3", taskDataTestPostTaskData.getRoot().getAttribute(TaskAttribute.STATUS).getValue());
		assertEquals("newerDescr", taskDataTestPostTaskData.getRoot()
				.getAttribute(JiraAttribute.DESCRIPTION.id())
				.getValue());
	}

	public void testPostTaskDataStartProgressAddComment() throws Exception {
		init(jiraUrl());
		JiraIssue issue = JiraTestUtil.createIssue(client, "testCommentWorkfPostTaskDataTask");
		TaskData taskDataTestPostTaskData = initTestPostTaskData(issue);

		TaskAttribute taskAttribute = taskDataTestPostTaskData.getRoot().getMappedAttribute(TaskAttribute.COMMENT_NEW);
		taskAttribute.setValue("comment3");
		String operation = JiraTestUtil.getOperation(client, issue.getKey(), "start");
		setAttributeValue(taskDataTestPostTaskData, TaskAttribute.OPERATION, operation);
		dataHandler.postTaskData(repository, taskDataTestPostTaskData, null, new NullProgressMonitor());

		taskDataTestPostTaskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("3", taskDataTestPostTaskData.getRoot().getAttribute(TaskAttribute.STATUS).getValue());
		List<ITaskComment> comments = JiraTestUtil.getTaskComments(taskDataTestPostTaskData);
		assertEquals(3, comments.size());
		assertEquals("comment1", comments.get(0).getText());
		assertEquals("comment2", comments.get(1).getText());
		assertEquals("comment3", comments.get(2).getText());
	}

	public void testPostTaskDataStartProgressAddCommentChangeDescription() throws Exception {
		init(jiraUrl());
		JiraIssue issue = JiraTestUtil.createIssue(client, "testDescrCommentWorkfPostTaskDataTask");
		TaskData taskDataTestPostTaskData = initTestPostTaskData(issue);

		//change attribute(s), submit and check
		taskDataTestPostTaskData.getRoot().getAttribute(JiraAttribute.DESCRIPTION.id()).setValue("newestDescr");
		TaskAttribute taskAttribute = taskDataTestPostTaskData.getRoot().getMappedAttribute(TaskAttribute.COMMENT_NEW);
		taskAttribute.setValue("comment3");
		String operation = JiraTestUtil.getOperation(client, issue.getKey(), "start");
		setAttributeValue(taskDataTestPostTaskData, TaskAttribute.OPERATION, operation);
		dataHandler.postTaskData(repository, taskDataTestPostTaskData, null, new NullProgressMonitor());

		taskDataTestPostTaskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("3", taskDataTestPostTaskData.getRoot().getAttribute(TaskAttribute.STATUS).getValue());
		List<ITaskComment> comments = JiraTestUtil.getTaskComments(taskDataTestPostTaskData);
		assertEquals(3, comments.size());
		assertEquals("comment1", comments.get(0).getText());
		assertEquals("comment2", comments.get(1).getText());
		assertEquals("comment3", comments.get(2).getText());
		assertEquals(taskDataTestPostTaskData.getRoot().getAttribute(JiraAttribute.DESCRIPTION.id()).getValue(),
				"newestDescr");
	}

	public void testPostTaskDataStartProgressChangeAttributes() throws Exception {
		init(jiraUrl());
		JiraIssue issue = JiraTestUtil.createIssue(client, "testWorkfAndOtherAttrPostTaskDataTask");
		TaskData taskDataTestPostTaskData = initTestPostTaskData(issue);

		//change attribute(s), submit and check
		taskDataTestPostTaskData.getRoot().getAttribute(JiraAttribute.PRIORITY.id()).setValue(Priority.BLOCKER_ID);
		taskDataTestPostTaskData.getRoot().getAttribute(JiraAttribute.SUMMARY.id()).setValue("newSummary");
		taskDataTestPostTaskData.getRoot().getAttribute(JiraAttribute.DESCRIPTION.id()).setValue("newestDescr");
		TaskAttribute taskAttribute = taskDataTestPostTaskData.getRoot().getMappedAttribute(TaskAttribute.COMMENT_NEW);
		taskAttribute.setValue("comment3");
		String operation = JiraTestUtil.getOperation(client, issue.getKey(), "start");
		setAttributeValue(taskDataTestPostTaskData, TaskAttribute.OPERATION, operation);
		dataHandler.postTaskData(repository, taskDataTestPostTaskData, null, new NullProgressMonitor());

		taskDataTestPostTaskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("3", taskDataTestPostTaskData.getRoot().getAttribute(TaskAttribute.STATUS).getValue());
		List<ITaskComment> comments = JiraTestUtil.getTaskComments(taskDataTestPostTaskData);
		assertEquals(3, comments.size());
		assertEquals("comment1", comments.get(0).getText());
		assertEquals("comment2", comments.get(1).getText());
		assertEquals("comment3", comments.get(2).getText());
		assertEquals(Priority.BLOCKER_ID, taskDataTestPostTaskData.getRoot()
				.getAttribute(JiraAttribute.PRIORITY.id())
				.getValue());
		assertEquals("newSummary", taskDataTestPostTaskData.getRoot()
				.getAttribute(JiraAttribute.SUMMARY.id())
				.getValue());
		assertEquals("newestDescr", taskDataTestPostTaskData.getRoot()
				.getAttribute(JiraAttribute.DESCRIPTION.id())
				.getValue());
	}

	private TaskData initTestPostTaskData(JiraIssue issue) throws Exception {
		String summary = issue.getSummary();
		issue.setDescription("descr");
		issue.setPriority(new Priority(Priority.MINOR_ID));
		client.updateIssue(issue, "comment1", new NullProgressMonitor());
		// make sure comments are created in the right order
		Thread.sleep(750);
		client.updateIssue(issue, "comment2", new NullProgressMonitor());
		ITask task = JiraTestUtil.createTask(repository, issue.getKey());
		assertEquals(summary, task.getSummary());
		assertEquals(false, task.isCompleted());
		TaskData taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals(summary, taskData.getRoot().getAttribute(JiraAttribute.SUMMARY.id()).getValue());
		assertEquals("descr", taskData.getRoot().getAttribute(JiraAttribute.DESCRIPTION.id()).getValue());
		assertEquals(Priority.MINOR_ID, taskData.getRoot().getAttribute(JiraAttribute.PRIORITY.id()).getValue());
		List<ITaskComment> comments = JiraTestUtil.getTaskComments(taskData);
		assertEquals(2, comments.size());
		assertEquals("comment1", comments.get(0).getText());
		assertEquals("comment2", comments.get(1).getText());
		return taskData;
	}

	/**
	 * Adds a comment to a task which user doesn't have edit permission for.
	 */
	public void testPostTaskDataCommentWithoutEditPermission() throws Exception {
		init(jiraUrl(), PrivilegeLevel.USER);

		JiraIssue issue = JiraTestUtil.createIssue(client, "testWithoutEditPermission");

		TaskData taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		taskData.getRoot().getAttribute(JiraAttribute.SUMMARY.id()).setValue("new summary");
		taskData.getRoot().getAttribute(JiraAttribute.COMMENT_NEW.id()).setValue("comment");
		dataHandler.postTaskData(repository, taskData,
				buildChanged(taskData.getRoot(), JiraAttribute.SUMMARY, JiraAttribute.COMMENT_NEW),
				new NullProgressMonitor());
		assertNull(taskData.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_READ_ONLY));

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals("new summary", taskData.getRoot().getAttribute(JiraAttribute.SUMMARY.id()).getValue());
		ITask task = JiraTestUtil.createTask(repository, taskData.getTaskId());
		List<ITaskComment> comments = JiraTestUtil.getTaskComments(task);
		assertEquals(1, comments.size());
		assertEquals("comment", comments.get(0).getText());
		assertNull(taskData.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_READ_ONLY));

		setUp();
		init(jiraUrl(), PrivilegeLevel.READ_ONLY);

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertNotNull(taskData.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_READ_ONLY));

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
		assertNotNull(taskData.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_READ_ONLY));
	}

	private Set<TaskAttribute> buildChanged(TaskAttribute root, JiraAttribute... attrs) {
		Set<TaskAttribute> changed = new HashSet<TaskAttribute>();
		for (JiraAttribute ja : attrs) {
			changed.add(root.getAttribute(ja.id()));
		}
		return changed;
	}

	/**
	 * Reassigns a task for which the user does not have edit permissions for.
	 */
	public void testPostTaskDataAssignWithoutEditPermission() throws Exception {
		init(jiraUrl(), PrivilegeLevel.USER);

		Credentials userCredentials = TestUtil.readCredentials(PrivilegeLevel.USER);
		JiraIssue issue = JiraTestUtil.createIssue(client, "testWithoutEditPermission");

		TaskData taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		taskData.getRoot().getAttribute(JiraAttribute.USER_ASSIGNED.id()).setValue("-1");

		dataHandler.postTaskData(repository, taskData, buildChanged(taskData.getRoot(), JiraAttribute.USER_ASSIGNED),
				new NullProgressMonitor());
		assertNull(taskData.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_READ_ONLY));

		setUp();
		init(jiraUrl(), PrivilegeLevel.READ_ONLY);

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());

		taskData.getRoot().getAttribute(JiraAttribute.USER_ASSIGNED.id()).setValue(userCredentials.username);

		dataHandler.postTaskData(repository, taskData, buildChanged(taskData.getRoot(), JiraAttribute.USER_ASSIGNED),
				new NullProgressMonitor());

		taskData = dataHandler.getTaskData(repository, issue.getId(), new NullProgressMonitor());
		assertEquals(userCredentials.username, taskData.getRoot()
				.getAttribute(JiraAttribute.USER_ASSIGNED.id())
				.getValue());
	}

	private String jiraUrl() {
		return JiraFixture.current().getRepositoryUrl();
	}

}
