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
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.context.tests.support.TestUtil;
import org.eclipse.mylyn.context.tests.support.TestUtil.Credentials;
import org.eclipse.mylyn.context.tests.support.TestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.JiraTask;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.filter.ContentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.DateFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.DateRangeFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.core.deprecated.RepositoryAttachment;
import org.eclipse.mylyn.internal.tasks.core.deprecated.RepositoryTaskAttribute;
import org.eclipse.mylyn.internal.tasks.core.deprecated.RepositoryTaskData;
import org.eclipse.mylyn.internal.tasks.core.sync.SynchronizationSession;
import org.eclipse.mylyn.internal.tasks.ui.AttachmentUtil;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.wizards.EditRepositoryWizard;
import org.eclipse.mylyn.jira.tests.util.LegacyResultCollector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 */
@SuppressWarnings("deprecation")
public class JiraLegacyRepositoryConnectorTest extends TestCase {

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

		taskList = TasksUiPlugin.getTaskList();
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
		String kind = JiraCorePlugin.CONNECTOR_KIND;

		Credentials credentials = TestUtil.readCredentials(PrivilegeLevel.USER);
		repository = new TaskRepository(kind, url);
		repository.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials(credentials.username,
				credentials.password), false);

		manager.addRepository(repository);

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

		((AbstractRepositorySettingsPage) wizard.getSettingsPage()).setUserId("newuser");
		assertTrue(wizard.performFinish());

		client = JiraClientFactory.getDefault().getJiraClient(repository);
		assertEquals("newuser", client.getUserName());
	}

	public void testAttachContext() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		JiraIssue issue = JiraTestUtils.createIssue(client, "testAttachContext");

		AbstractTask task = (AbstractTask) TasksUiInternal.createTask(repository, issue.getKey(),
				new NullProgressMonitor());
		assertEquals("testAttachContext", task.getSummary());

		File sourceContextFile = ContextCorePlugin.getContextStore().getFileForContext(task.getHandleIdentifier());
		JiraTestUtils.writeFile(sourceContextFile, "Mylyn".getBytes());
		sourceContextFile.deleteOnExit();

		// FIXME
		fail();
//		assertTrue(AttachmentUtil.attachContext(connector.getAttachmentHandler(), repository, task, "",
//				new NullProgressMonitor()));

		TasksUiInternal.synchronizeTask(connector, task, true, null);

		Set<RepositoryAttachment> contextAttachments = AttachmentUtil.getLegacyContextAttachments(repository, task);
		assertEquals(1, contextAttachments.size());

		RepositoryAttachment attachment = contextAttachments.iterator().next();
		// FIXME
		fail();
//		assertTrue(AttachmentUtil.retrieveContext(connector.getAttachmentHandler(), repository, task, attachment,
//				System.getProperty("java.io.tmpdir"), new NullProgressMonitor()));
	}

	public void testPerformQueryDueDateFilter() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		GregorianCalendar c = new GregorianCalendar();
		c.add(Calendar.MONTH, 1);
		Date fromDate = c.getTime();
		Date toDate = c.getTime();

		DateFilter dueDateFilter = new DateRangeFilter(fromDate, toDate);

		FilterDefinition filter = new FilterDefinition();
		filter.setDueDateFilter(dueDateFilter);

		// AbstractRepositoryQuery query = new JiraCustomQuery("test query", queryUrl, repository.getUrl(), repository.getCharacterEncoding());
		IRepositoryQuery query = JiraTestUtils.createQuery(repository, filter);

		LegacyResultCollector collector1 = new LegacyResultCollector();
		connector.performQuery(repository, query, collector1, null, null);

		JiraIssue issue = JiraTestUtils.newIssue(client, "testDueDateFilter");
		issue.setDue(fromDate);
		issue = JiraTestUtils.createIssue(client, issue);
		assertNotNull(issue);

		LegacyResultCollector collector2 = new LegacyResultCollector();
		connector.performQuery(repository, query, collector2, null, new NullProgressMonitor());
		assertEquals(collector1.results.size() + 1, collector2.results.size());

		for (RepositoryTaskData taskData : collector2.results) {
			String owner = taskData.getAttributeValue(RepositoryTaskAttribute.USER_OWNER);
			assertNotNull(owner);
			assertTrue(owner.length() > 0);
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
		FilterDefinition filter = new FilterDefinition();
		filter.setContentFilter(new ContentFilter(queryString, true, false, false, false));

		IRepositoryQuery query = JiraTestUtils.createQuery(repository, filter);
		LegacyResultCollector collector = new LegacyResultCollector();
		connector.performQuery(repository, query, collector, null, new NullProgressMonitor());
		assertEquals(2, collector.results.size());
	}

	public void testMarkStaleNoTasks() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		repository.setSynchronizationTimeStamp(null);
		SynchronizationSession event = new SynchronizationSession();
		event.setTasks(new HashSet<ITask>());
		event.setTaskRepository(repository);
		event.setFullSynchronization(true);
		connector.preSynchronization(event, null);
		assertTrue(event.needsPerformQueries());
		assertNotNull(repository.getSynchronizationTimeStamp());
	}

	public void testMarkStaleOneTask() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		JiraIssue issue = JiraTestUtils.createIssue(client, "testMarkStale");

		Date start = new Date();
		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(start));
		AbstractTask task = (AbstractTask) TasksUiInternal.createTask(repository, issue.getKey(),
				new NullProgressMonitor());
		taskList.addTask(task);
		Thread.sleep(5); // make sure markStaleTasks() finds a difference 
		assertNull(JiraUtil.getLastUpdate(repository));

		Set<ITask> tasks = new HashSet<ITask>();
		tasks.add(task);
		SynchronizationSession event = new SynchronizationSession();
		event.setTasks(tasks);
		event.setTaskRepository(repository);
		event.setFullSynchronization(true);

		connector.preSynchronization(event, null);
		assertTrue(event.needsPerformQueries());
		assertFalse(task.isStale());
		assertNotNull(repository.getSynchronizationTimeStamp());
		Date timestamp = JiraUtil.stringToDate(repository.getSynchronizationTimeStamp());
		assertTrue(timestamp.after(start));
		assertTrue(timestamp.before(new Date()));
		assertEquals(issue.getUpdated(), JiraUtil.getLastUpdate(repository));

		Thread.sleep(5); // make sure markStaleTasks() finds a difference

		connector.preSynchronization(event, null);
		assertFalse(event.needsPerformQueries());
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
		AbstractTask task = (AbstractTask) TasksUiInternal.createTask(repository, issue.getKey(),
				new NullProgressMonitor());
		taskList.addTask(task);

		// make sure the second issue is created after the first one
		Thread.sleep(1000);

		JiraIssue issue2 = JiraTestUtils.createIssue(client, "testMarkStale2");
		assertTrue(issue2.getUpdated().after(issue.getUpdated()));
		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(start));

		Set<ITask> tasks = new HashSet<ITask>();
		tasks.add(task);

		SynchronizationSession event = new SynchronizationSession();
		event.setTasks(tasks);
		event.setTaskRepository(repository);
		event.setFullSynchronization(true);
		connector.preSynchronization(event, null);
		assertTrue(event.needsPerformQueries());
		assertFalse("Expected updated synchronization timestamp", JiraUtil.dateToString(start).equals(
				repository.getSynchronizationTimeStamp()));
		assertEquals(issue2.getUpdated(), JiraUtil.getLastUpdate(repository));
	}

	public void testMarkStaleClosedTask() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		// create an issue
		JiraIssue issue = JiraTestUtils.createIssue(client, "testMarkStale");
		AbstractTask task = (AbstractTask) TasksUiInternal.createTask(repository, issue.getKey(),
				new NullProgressMonitor());
		taskList.addTask(task);
		assertFalse(task.isCompleted());

		// close issue
		String resolveOperation = JiraTestUtils.getOperation(client, issue.getKey(), "resolve");
		issue.setResolution(client.getCache().getResolutionById(Resolution.FIXED_ID));
		client.advanceIssueWorkflow(issue, resolveOperation, "comment", null);

		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(addSecondsToDate(new Date(), -1)));
		Set<ITask> tasks = new HashSet<ITask>();
		tasks.add(task);
		SynchronizationSession event = new SynchronizationSession();
		event.setTasks(tasks);
		event.setTaskRepository(repository);
		event.setFullSynchronization(true);
		connector.preSynchronization(event, null);
		assertTrue(event.needsPerformQueries());
		assertTrue(task.isCompleted());
	}

	public void testGetSynchronizationFilter() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		Set<ITask> tasks = new HashSet<ITask>();
		tasks.add(new JiraTask(JiraTestConstants.JIRA_39_URL, "1", ""));

		Date now = new Date();
		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(now));
		fail(); // FIXME
//		FilterDefinition filter = connector.getSynchronizationFilter(repository, tasks, addSecondsToDate(now, 1));
//		assertNotNull(filter);
//		assertTrue(filter.getUpdatedDateFilter() instanceof RelativeDateRangeFilter);
//		RelativeDateRangeFilter dateFilter = (RelativeDateRangeFilter) filter.getUpdatedDateFilter();
//		assertEquals(RangeType.MINUTE, dateFilter.getPreviousRangeType());
//		assertEquals(-1, dateFilter.getPreviousCount());
//		assertEquals(RangeType.NONE, dateFilter.getNextRangeType());
//		assertEquals(0, dateFilter.getNextCount());

		fail(); // FIXME
//		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(addSecondsToDate(now, -121)));
//		filter = connector.getSynchronizationFilter(repository, tasks, now);
//		assertNotNull(filter);
//		assertTrue(filter.getUpdatedDateFilter() instanceof RelativeDateRangeFilter);
//		dateFilter = (RelativeDateRangeFilter) filter.getUpdatedDateFilter();
//		assertEquals(RangeType.MINUTE, dateFilter.getPreviousRangeType());
//		assertEquals(-3, dateFilter.getPreviousCount());
//		assertEquals(RangeType.NONE, dateFilter.getNextRangeType());
//		assertEquals(0, dateFilter.getNextCount());
	}

	public void testGetSynchronizationFilterTimeStampInTheFuture() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		Set<ITask> tasks = new HashSet<ITask>();
		tasks.add(new JiraTask(JiraTestConstants.JIRA_39_URL, "1", ""));

		Date now = new Date();
		String future = JiraUtil.dateToString(addSecondsToDate(now, 20));
		repository.setSynchronizationTimeStamp(future);
		fail(); // FIXME
//		FilterDefinition filter = connector.getSynchronizationFilter(repository, tasks, now);
//		assertNull(filter);
//		assertEquals("Expected unchanged timestamp", future, repository.getSynchronizationTimeStamp());
//
//		SynchronizationSession event = new SynchronizationSession();
//		event.setTasks(tasks);
//		event.setTaskRepository(repository);
//		event.setFullSynchronization(true);
//		connector.preSynchronization(event, null);
//		assertTrue(event.needsPerformQueries());
//		assertNotNull(repository.getSynchronizationTimeStamp());
//		assertTrue("Expected updated timestamp", !future.equals(repository.getSynchronizationTimeStamp()));
	}

	public void testGetSynchronizationFilterTimeStampInTheFutureWithTask() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		Date now = new Date();
		HashSet<ITask> tasks = new HashSet<ITask>();
		JiraTask task = new JiraTask(repository.getRepositoryUrl(), "1", "");
		task.setLastReadTimeStamp(JiraUtil.dateToString(now));
		tasks.add(task);

		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(addSecondsToDate(new Date(), 121)));
		fail(); // FIXME
//		FilterDefinition filter = connector.getSynchronizationFilter(repository, tasks, now);
//		assertNotNull(filter);
//		assertTrue(filter.getUpdatedDateFilter() instanceof DateRangeFilter);
//		DateRangeFilter dateFilter = (DateRangeFilter) filter.getUpdatedDateFilter();
//		assertEquals(JiraUtil.stringToDate(task.getLastReadTimeStamp()), dateFilter.getFromDate());
//		assertEquals(null, dateFilter.getToDate());
	}

	public void testCreateTask() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		JiraIssue issue = JiraTestUtils.createIssue(client, "testCreateTask");

		ITask task = TasksUiInternal.createTask(repository, issue.getKey(), new NullProgressMonitor());
		assertEquals("testCreateTask", task.getSummary());
		assertEquals(null, task.getCompletionDate());
		assertFalse(task.isCompleted());
		assertEquals(issue.getCreated(), task.getCreationDate());

		// close issue
		issue.setResolution(client.getCache().getResolutionById(Resolution.FIXED_ID));
		client.advanceIssueWorkflow(issue, "2", "", null);

		issue = client.getIssueByKey(issue.getKey(), null);
		task = TasksUiInternal.createTask(repository, issue.getKey(), new NullProgressMonitor());
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
