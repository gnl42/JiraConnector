/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.tests.support.TestUtil;
import org.eclipse.mylyn.context.tests.support.TestUtil.Credentials;
import org.eclipse.mylyn.context.tests.support.TestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraCustomQuery;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.JiraTask;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.filter.ContentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.DateFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.DateRangeFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.RelativeDateRangeFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.RelativeDateRangeFilter.RangeType;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.ui.AttachmentUtil;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.wizards.EditRepositoryWizard;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.QueryHitCollector;
import org.eclipse.mylyn.tasks.core.RepositoryAttachment;
import org.eclipse.mylyn.tasks.core.TaskList;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.sync.SynchronizationEvent;
import org.eclipse.mylyn.tasks.ui.TaskFactory;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.web.core.AuthenticationCredentials;
import org.eclipse.mylyn.web.core.AuthenticationType;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 */
public class JiraRepositoryConnectorTest extends TestCase {

	private TaskRepository repository;

	private TaskRepositoryManager manager;

	private JiraRepositoryConnector connector;

	private JiraClient client;

	private TaskList taskList;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		manager = TasksUiPlugin.getRepositoryManager();
		manager.clearRepositories(TasksUiPlugin.getDefault().getRepositoriesFilePath());

		taskList = TasksUiPlugin.getTaskListManager().getTaskList();
		taskList.reset();

		JiraClientFactory.getDefault().clearClients();
	}

	@Override
	protected void tearDown() throws Exception {
		if (client != null) {
			JiraTestUtils.cleanup(client);
		}
	}

	protected void init(String url) throws Exception {
		String kind = JiraCorePlugin.REPOSITORY_KIND;

		Credentials credentials = TestUtil.readCredentials(PrivilegeLevel.USER);
		repository = new TaskRepository(kind, url);
		repository.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials(credentials.username,
				credentials.password), false);

		manager.addRepository(repository, TasksUiPlugin.getDefault().getRepositoriesFilePath());

		connector = (JiraRepositoryConnector) manager.getRepositoryConnector(kind);
		assertEquals(connector.getConnectorKind(), kind);

		client = JiraClientFactory.getDefault().getJiraClient(repository);
	}

	public void testChangeTaskRepositorySettings() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);
		assertEquals(repository.getUserName(), repository.getUserName());

		EditRepositoryWizard wizard = new EditRepositoryWizard(repository);
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.create();

		wizard.getSettingsPage().setUserId("newuser");
		assertTrue(wizard.performFinish());

		client = JiraClientFactory.getDefault().getJiraClient(repository);
		assertEquals("newuser", client.getUserName());
	}

	public void testAttachContext() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		JiraIssue issue = JiraTestUtils.createIssue(client, "testAttachContext");

		AbstractTask task = TasksUiUtil.createTask(repository, issue.getKey(), new NullProgressMonitor());
		assertEquals("testAttachContext", task.getSummary());

		File sourceContextFile = ContextCore.getContextManager().getFileForContext(task.getHandleIdentifier());
		JiraTestUtils.writeFile(sourceContextFile, "Mylyn".getBytes());
		sourceContextFile.deleteOnExit();

		assertTrue(AttachmentUtil.attachContext(connector.getAttachmentHandler(), repository, task, "",
				new NullProgressMonitor()));

		TasksUi.synchronizeTask(connector, task, true, null);

		Set<RepositoryAttachment> contextAttachments = AttachmentUtil.getContextAttachments(repository, task);
		assertEquals(1, contextAttachments.size());

		RepositoryAttachment attachment = contextAttachments.iterator().next();
		assertTrue(AttachmentUtil.retrieveContext(connector.getAttachmentHandler(), repository, task, attachment,
				System.getProperty("java.io.tmpdir"), new NullProgressMonitor()));
	}

	public void testPerformQueryDueDateFilter() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		GregorianCalendar c = new GregorianCalendar();
		c.add(Calendar.MONTH, 1);
		Date fromDate = c.getTime();
		Date toDate = c.getTime();

		DateFilter dueDateFilter = new DateRangeFilter(fromDate, toDate);

		FilterDefinition filter = new FilterDefinition("test query");
		filter.setDueDateFilter(dueDateFilter);

		// AbstractRepositoryQuery query = new JiraCustomQuery("test query", queryUrl, repository.getUrl(), repository.getCharacterEncoding());
		AbstractRepositoryQuery query = new JiraCustomQuery(repository.getRepositoryUrl(), filter,
				repository.getCharacterEncoding());

		TaskFactory taskFactory = new TaskFactory(repository, false, false);

		QueryHitCollector collector1 = new QueryHitCollector(taskFactory);
		connector.performQuery(repository, query, collector1, null, new NullProgressMonitor());

		Set<AbstractTask> tasks1 = collector1.getTasks();
		// assertEquals(-1, tasks.size());

		JiraIssue issue = JiraTestUtils.newIssue(client, "testDueDateFilter");
		issue.setDue(fromDate);
		issue = JiraTestUtils.createIssue(client, issue);
		assertNotNull(issue);

		QueryHitCollector collector2 = new QueryHitCollector(taskFactory);
		connector.performQuery(repository, query, collector2, null, new NullProgressMonitor());
		Set<AbstractTask> tasks2 = collector2.getTasks();

		assertEquals(tasks1.size() + 1, tasks2.size());

		for (AbstractTask task : tasks2) {
			assertNotNull(task.getOwner());
			assertTrue("Expected ", task.getOwner().length() > 0);
		}
	}

	public void testPerformQuerySpaces() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		long currentTimeMillis = System.currentTimeMillis();
		String summary1 = "test search for spaces " + currentTimeMillis;
		JiraTestUtils.createIssue(client, summary1);
		String summary2 = "test search for spaces " + (currentTimeMillis + 1);
		JiraTestUtils.createIssue(client, summary2);

		String queryString = currentTimeMillis + " " + (currentTimeMillis + 1);
		FilterDefinition filter = new FilterDefinition("test query");
		filter.setContentFilter(new ContentFilter(queryString, true, false, false, false));

		AbstractRepositoryQuery query = new JiraCustomQuery(repository.getRepositoryUrl(), filter,
				repository.getCharacterEncoding());

		TaskFactory taskFactory = new TaskFactory(repository, false, false);

		QueryHitCollector collector = new QueryHitCollector(taskFactory);
		connector.performQuery(repository, query, collector, null, new NullProgressMonitor());

		Set<AbstractTask> tasks = collector.getTasks();
		assertEquals(2, tasks.size());
	}

	public void testMarkStaleNoTasks() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		repository.setSynchronizationTimeStamp(null);
		SynchronizationEvent event = new SynchronizationEvent();
		event.tasks = new HashSet<AbstractTask>();
		event.taskRepository = repository;
		event.fullSynchronization = true;
		connector.preSynchronization(event, null);
		assertTrue(event.performQueries);
		assertNotNull(repository.getSynchronizationTimeStamp());
	}

	public void testMarkStaleOneTask() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		JiraIssue issue = JiraTestUtils.createIssue(client, "testMarkStale");

		Date start = new Date();
		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(start));
		AbstractTask task = TasksUiUtil.createTask(repository, issue.getKey(), new NullProgressMonitor());
		taskList.addTask(task);
		Thread.sleep(5); // make sure markStaleTasks() finds a difference 
		assertNull(JiraUtil.getLastUpdate(repository));

		Set<AbstractTask> tasks = new HashSet<AbstractTask>();
		tasks.add(task);
		SynchronizationEvent event = new SynchronizationEvent();
		event.tasks = tasks;
		event.taskRepository = repository;
		event.fullSynchronization = true;

		connector.preSynchronization(event, null);
		assertTrue(event.performQueries);
		assertFalse(task.isStale());
		assertNotNull(repository.getSynchronizationTimeStamp());
		Date timestamp = JiraUtil.stringToDate(repository.getSynchronizationTimeStamp());
		assertTrue(timestamp.after(start));
		assertTrue(timestamp.before(new Date()));
		assertEquals(issue.getUpdated(), JiraUtil.getLastUpdate(repository));

		Thread.sleep(5); // make sure markStaleTasks() finds a difference

		connector.preSynchronization(event, null);
		assertFalse(event.performQueries);
		assertNotNull(repository.getSynchronizationTimeStamp());
		assertFalse(task.isStale());
		assertFalse("Expected updated synchronization timestamp", JiraUtil.dateToString(timestamp).equals(
				repository.getSynchronizationTimeStamp()));
		assertEquals(issue.getUpdated(), JiraUtil.getLastUpdate(repository));
	}

	public void testMarkStaleRepositoryChanged() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		// create two issues, the first one is added to the task list
		Date start = new Date();
		JiraIssue issue = JiraTestUtils.createIssue(client, "testMarkStale");
		AbstractTask task = TasksUiUtil.createTask(repository, issue.getKey(), new NullProgressMonitor());
		taskList.addTask(task);

		// make sure the second issue is created after the first one
		Thread.sleep(1000);

		JiraIssue issue2 = JiraTestUtils.createIssue(client, "testMarkStale2");
		assertTrue(issue2.getUpdated().after(issue.getUpdated()));
		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(start));

		Set<AbstractTask> tasks = new HashSet<AbstractTask>();
		tasks.add(task);

		SynchronizationEvent event = new SynchronizationEvent();
		event.tasks = tasks;
		event.taskRepository = repository;
		event.fullSynchronization = true;
		connector.preSynchronization(event, null);
		assertTrue(event.performQueries);
		assertFalse("Expected updated synchronization timestamp", JiraUtil.dateToString(start).equals(
				repository.getSynchronizationTimeStamp()));
		assertEquals(issue2.getUpdated(), JiraUtil.getLastUpdate(repository));
	}

	public void testMarkStaleClosedTask() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		// create an issue
		JiraIssue issue = JiraTestUtils.createIssue(client, "testMarkStale");
		AbstractTask task = TasksUiUtil.createTask(repository, issue.getKey(), new NullProgressMonitor());
		taskList.addTask(task);
		assertFalse(task.isCompleted());

		// close issue
		String resolveOperation = JiraTestUtils.getOperation(client, issue.getKey(), "resolve");
		issue.setResolution(client.getCache().getResolutionById(Resolution.FIXED_ID));
		client.advanceIssueWorkflow(issue, resolveOperation, "comment", null);

		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(addSecondsToDate(new Date(), -1)));
		Set<AbstractTask> tasks = new HashSet<AbstractTask>();
		tasks.add(task);
		SynchronizationEvent event = new SynchronizationEvent();
		event.tasks = tasks;
		event.taskRepository = repository;
		event.fullSynchronization = true;
		connector.preSynchronization(event, null);
		assertTrue(event.performQueries);
		assertTrue(task.isCompleted());
	}

	public void testGetSynchronizationFilter() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		Set<AbstractTask> tasks = new HashSet<AbstractTask>();
		tasks.add(new JiraTask(JiraTestConstants.JIRA_39_URL, "1", ""));

		Date now = new Date();
		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(now));
		FilterDefinition filter = connector.getSynchronizationFilter(repository, tasks, addSecondsToDate(now, 1));
		assertNotNull(filter);
		assertTrue(filter.getUpdatedDateFilter() instanceof RelativeDateRangeFilter);
		RelativeDateRangeFilter dateFilter = (RelativeDateRangeFilter) filter.getUpdatedDateFilter();
		assertEquals(RangeType.MINUTE, dateFilter.getPreviousRangeType());
		assertEquals(-1, dateFilter.getPreviousCount());
		assertEquals(RangeType.NONE, dateFilter.getNextRangeType());
		assertEquals(0, dateFilter.getNextCount());

		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(addSecondsToDate(now, -121)));
		filter = connector.getSynchronizationFilter(repository, tasks, now);
		assertNotNull(filter);
		assertTrue(filter.getUpdatedDateFilter() instanceof RelativeDateRangeFilter);
		dateFilter = (RelativeDateRangeFilter) filter.getUpdatedDateFilter();
		assertEquals(RangeType.MINUTE, dateFilter.getPreviousRangeType());
		assertEquals(-3, dateFilter.getPreviousCount());
		assertEquals(RangeType.NONE, dateFilter.getNextRangeType());
		assertEquals(0, dateFilter.getNextCount());
	}

	public void testGetSynchronizationFilterTimeStampInTheFuture() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		Set<AbstractTask> tasks = new HashSet<AbstractTask>();
		tasks.add(new JiraTask(JiraTestConstants.JIRA_39_URL, "1", ""));

		Date now = new Date();
		String future = JiraUtil.dateToString(addSecondsToDate(now, 20));
		repository.setSynchronizationTimeStamp(future);
		FilterDefinition filter = connector.getSynchronizationFilter(repository, tasks, now);
		assertNull(filter);
		assertEquals("Expected unchanged timestamp", future, repository.getSynchronizationTimeStamp());

		SynchronizationEvent event = new SynchronizationEvent();
		event.tasks = tasks;
		event.taskRepository = repository;
		event.fullSynchronization = true;
		connector.preSynchronization(event, null);
		assertTrue(event.performQueries);
		assertNotNull(repository.getSynchronizationTimeStamp());
		assertTrue("Expected updated timestamp", !future.equals(repository.getSynchronizationTimeStamp()));
	}

	public void testGetSynchronizationFilterTimeStampInTheFutureWithTask() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		Date now = new Date();
		HashSet<AbstractTask> tasks = new HashSet<AbstractTask>();
		JiraTask task = new JiraTask(repository.getRepositoryUrl(), "1", "");
		task.setLastReadTimeStamp(JiraUtil.dateToString(now));
		tasks.add(task);

		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(addSecondsToDate(new Date(), 121)));
		FilterDefinition filter = connector.getSynchronizationFilter(repository, tasks, now);
		assertNotNull(filter);
		assertTrue(filter.getUpdatedDateFilter() instanceof DateRangeFilter);
		DateRangeFilter dateFilter = (DateRangeFilter) filter.getUpdatedDateFilter();
		assertEquals(JiraUtil.stringToDate(task.getLastReadTimeStamp()), dateFilter.getFromDate());
		assertEquals(null, dateFilter.getToDate());
	}

	public void testCreateTask() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		JiraIssue issue = JiraTestUtils.createIssue(client, "testCreateTask");

		AbstractTask task = TasksUiUtil.createTask(repository, issue.getKey(), new NullProgressMonitor());
		assertEquals("testCreateTask", task.getSummary());
		assertEquals(null, task.getCompletionDate());
		assertFalse(task.isCompleted());
		assertEquals(issue.getCreated(), task.getCreationDate());

		// close issue
		issue.setResolution(client.getCache().getResolutionById(Resolution.FIXED_ID));
		client.advanceIssueWorkflow(issue, "2", "", null);

		issue = client.getIssueByKey(issue.getKey(), null);
		task = TasksUiUtil.createTask(repository, issue.getKey(), new NullProgressMonitor());
		assertTrue(task.isCompleted());
		assertEquals(issue.getUpdated(), task.getCompletionDate());
		assertEquals(issue.getCreated(), task.getCreationDate());
	}

	private Date addSecondsToDate(Date updated, int i) {
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(updated.getTime() + i * 1000);
		return cal.getTime();
	}

//	private void waitForRepositoryTimeSync(Date repositoryTime) throws InterruptedException {
//		Date now = new Date();
//		long diff = now.getTime() - repositoryTime.getTime();
//		if (diff > 10 * 1000) {
//			fail("Local time is too far ahead of repository time: " + now + " > " + repositoryTime);
//		} else if (diff < -10 * 1000) {
//			fail("Repository time is too far ahead of local time: " + repositoryTime + " > " + now);
//		} 
//		
//		if (diff < 0) {
//			// wait a little bit so local time can catch up
//			Thread.sleep(-diff);
//		}
//	}
//	

}
