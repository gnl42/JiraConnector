/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraAttribute;
import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
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
import org.eclipse.mylyn.internal.tasks.core.TaskTask;
import org.eclipse.mylyn.internal.tasks.core.sync.SynchronizationSession;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil;
import org.eclipse.mylyn.internal.tasks.ui.wizards.EditRepositoryWizard;
import org.eclipse.mylyn.jira.tests.util.JiraTestConstants;
import org.eclipse.mylyn.jira.tests.util.JiraTestResultCollector;
import org.eclipse.mylyn.jira.tests.util.JiraTestUtil;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 */
public class JiraRepositoryConnectorTest extends TestCase {

	private TaskRepository repository;

	private JiraRepositoryConnector connector;

	private JiraClient client;

	@Override
	protected void setUp() throws Exception {
		JiraTestUtil.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		JiraTestUtil.tearDown();
	}

	protected void init(String url) throws Exception {
		repository = JiraTestUtil.init(url);
		connector = (JiraRepositoryConnector) TasksUi.getRepositoryConnector(JiraCorePlugin.CONNECTOR_KIND);
		client = JiraClientFactory.getDefault().getJiraClient(repository);
	}

	private SynchronizationSession createSession(ITask... tasks) {
		SynchronizationSession session = new SynchronizationSession(TasksUiPlugin.getTaskDataManager());
		session.setNeedsPerformQueries(true);
		session.setTaskRepository(repository);
		session.setFullSynchronization(true);
		session.setTasks(new HashSet<ITask>(Arrays.asList(tasks)));
		return session;
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

		JiraIssue issue = JiraTestUtil.createIssue(client, "testAttachContext");
		ITask task = JiraTestUtil.createTask(repository, issue.getId());
		assertEquals("testAttachContext", task.getSummary());
		File sourceContextFile = ContextCorePlugin.getContextStore().getFileForContext(task.getHandleIdentifier());
		JiraTestUtil.writeFile(sourceContextFile, "Mylyn".getBytes());
		sourceContextFile.deleteOnExit();

		boolean result = AttachmentUtil.postContext(connector, repository, task, "", null, null);
		assertTrue(result);

		task = JiraTestUtil.createTask(repository, issue.getId());
		List<ITaskAttachment> contextAttachments = AttachmentUtil.getContextAttachments(repository, task);
		assertEquals(1, contextAttachments.size());

		ITaskAttachment attachment = contextAttachments.get(0);
		result = AttachmentUtil.retrieveContext(connector.getTaskAttachmentHandler(), repository, task, attachment,
				System.getProperty("java.io.tmpdir"), PlatformUI.getWorkbench().getProgressService());
		assertTrue(result);
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
		IRepositoryQuery query = JiraTestUtil.createQuery(repository, filter);

		JiraTestResultCollector collector1 = new JiraTestResultCollector();
		connector.performQuery(repository, query, collector1, null, null);

		JiraIssue issue = JiraTestUtil.newIssue(client, "testDueDateFilter");
		issue.setDue(fromDate);
		issue = JiraTestUtil.createIssue(client, issue);
		assertNotNull(issue);

		JiraTestResultCollector collector2 = new JiraTestResultCollector();
		connector.performQuery(repository, query, collector2, null, new NullProgressMonitor());
		assertEquals(collector1.results.size() + 1, collector2.results.size());
		for (TaskData taskData : collector2.results) {
			String owner = taskData.getRoot().getAttribute(JiraAttribute.USER_ASSIGNED.id()).getValue();
			assertNotNull(owner);
			assertTrue(owner.length() > 0);
		}
	}

	public void testPerformQuerySpaces() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		long currentTimeMillis = System.currentTimeMillis();
		String summary1 = "test search for spaces " + currentTimeMillis;
		JiraTestUtil.createIssue(client, summary1);
		String summary2 = "test search for spaces " + (currentTimeMillis + 1);
		JiraTestUtil.createIssue(client, summary2);

		String queryString = currentTimeMillis + " " + (currentTimeMillis + 1);
		FilterDefinition filter = new FilterDefinition();
		filter.setContentFilter(new ContentFilter(queryString, true, false, false, false));

		IRepositoryQuery query = JiraTestUtil.createQuery(repository, filter);
		JiraTestResultCollector collector = new JiraTestResultCollector();
		connector.performQuery(repository, query, collector, null, new NullProgressMonitor());
		assertEquals(2, collector.results.size());
	}

	public void testPerformQueryLimitNumberOfResults() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		long currentTimeMillis = System.currentTimeMillis();
		JiraTestUtil.createIssue(client, "search " + currentTimeMillis);
		JiraTestUtil.createIssue(client, "search " + currentTimeMillis);
		JiraTestUtil.createIssue(client, "search " + currentTimeMillis);
		JiraTestUtil.createIssue(client, "search " + currentTimeMillis);
		FilterDefinition filter = new FilterDefinition();
		filter.setContentFilter(new ContentFilter("search " + currentTimeMillis, true, false, false, false));
		IRepositoryQuery query = JiraTestUtil.createQuery(repository, filter);

		try {
			JiraUtil.setMaxSearchResults(repository, JiraUtil.DEFAULT_MAX_SEARCH_RESULTS);
			JiraTestResultCollector collector = new JiraTestResultCollector();
			connector.performQuery(repository, query, collector, null, null);
			assertEquals(4, collector.results.size());

			JiraUtil.setMaxSearchResults(repository, 2);
			collector = new JiraTestResultCollector();
			connector.performQuery(repository, query, collector, null, null);
			assertEquals(2, collector.results.size());

			JiraUtil.setMaxSearchResults(repository, -1);
			collector = new JiraTestResultCollector();
			connector.performQuery(repository, query, collector, null, null);
			assertEquals(4, collector.results.size());
		} finally {
			JiraUtil.setMaxSearchResults(repository, JiraUtil.DEFAULT_MAX_SEARCH_RESULTS);
		}
	}

	public void testMarkStaleNoTasks() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		repository.setSynchronizationTimeStamp(null);
		SynchronizationSession session = createSession();
		connector.preSynchronization(session, null);
		assertTrue(session.needsPerformQueries());
		assertNotNull(repository.getSynchronizationTimeStamp());
	}

	public void testMarkStaleOneTask() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		JiraIssue issue = JiraTestUtil.createIssue(client, "testMarkStale");
		Date start = new Date();
		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(start));
		ITask task = JiraTestUtil.createTask(repository, issue.getKey());
		task.setModificationDate(null);
		Thread.sleep(5); // make sure markStaleTasks() finds a difference 
		assertNull(JiraUtil.getLastUpdate(repository));

		SynchronizationSession session = createSession(task);
		connector.preSynchronization(session, null);
		assertTrue(session.needsPerformQueries());
		assertNull(session.getStaleTasks());
		assertNotNull(repository.getSynchronizationTimeStamp());
		Date timestamp = JiraUtil.stringToDate(repository.getSynchronizationTimeStamp());
		assertTrue(timestamp.after(start));
		assertTrue(timestamp.before(new Date()));
		assertEquals(issue.getUpdated(), JiraUtil.getLastUpdate(repository));

		Thread.sleep(5); // make sure markStaleTasks() finds a difference

		session = createSession(task);
		connector.preSynchronization(session, null);
		assertFalse(session.needsPerformQueries());
		assertNotNull(repository.getSynchronizationTimeStamp());
		assertEquals(null, session.getStaleTasks());
		assertFalse("Expected updated synchronization timestamp", JiraUtil.dateToString(timestamp).equals(
				repository.getSynchronizationTimeStamp()));
		assertEquals(issue.getUpdated(), JiraUtil.getLastUpdate(repository));
	}

	public void testMarkStaleRepositoryChanged() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		// create two issues, the first one is added to the task list
		Date start = new Date();
		JiraIssue issue = JiraTestUtil.createIssue(client, "testMarkStale");
		ITask task = JiraTestUtil.createTask(repository, issue.getKey());
		// make sure the second issue is created after the first one
		Thread.sleep(1000);
		JiraIssue issue2 = JiraTestUtil.createIssue(client, "testMarkStale2");
		assertTrue(issue2.getUpdated().after(issue.getUpdated()));
		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(start));

		SynchronizationSession session = createSession(task);
		connector.preSynchronization(session, null);
		assertTrue(session.needsPerformQueries());
		assertFalse("Expected updated synchronization timestamp", JiraUtil.dateToString(start).equals(
				repository.getSynchronizationTimeStamp()));
		assertEquals(issue2.getUpdated(), JiraUtil.getLastUpdate(repository));
	}

	public void testMarkStaleClosedTask() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		// create an issue
		JiraIssue issue = JiraTestUtil.createIssue(client, "testMarkStale");
		ITask task = JiraTestUtil.createTask(repository, issue.getKey());
		assertFalse(task.isCompleted());

		// close issue
		String resolveOperation = JiraTestUtil.getOperation(client, issue.getKey(), "resolve");
		issue.setResolution(client.getCache().getResolutionById(Resolution.FIXED_ID));
		client.advanceIssueWorkflow(issue, resolveOperation, "comment", null);

		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(addSecondsToDate(new Date(), -1)));
		SynchronizationSession session = createSession(task);
		connector.preSynchronization(session, null);
		assertTrue(session.needsPerformQueries());
		assertTrue(task.isCompleted());
	}

	public void testGetSynchronizationFilter() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		Date now = new Date();
		ITask task = new TaskTask(JiraCorePlugin.CONNECTOR_KIND, repository.getRepositoryUrl(), "1");
		SynchronizationSession session = createSession(task);

		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(now));
		FilterDefinition filter = connector.getSynchronizationFilter(session, addSecondsToDate(now, 1));
		assertNotNull(filter);
		assertTrue(filter.getUpdatedDateFilter() instanceof RelativeDateRangeFilter);
		RelativeDateRangeFilter dateFilter = (RelativeDateRangeFilter) filter.getUpdatedDateFilter();
		assertEquals(RangeType.MINUTE, dateFilter.getPreviousRangeType());
		assertEquals(-1, dateFilter.getPreviousCount());
		assertEquals(RangeType.NONE, dateFilter.getNextRangeType());
		assertEquals(0, dateFilter.getNextCount());

		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(addSecondsToDate(now, -121)));
		filter = connector.getSynchronizationFilter(session, now);
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

		ITask task = new TaskTask(JiraCorePlugin.CONNECTOR_KIND, repository.getRepositoryUrl(), "1");
		Date now = new Date();
		String future = JiraUtil.dateToString(addSecondsToDate(now, 20));
		SynchronizationSession session = createSession(task);

		repository.setSynchronizationTimeStamp(future);
		FilterDefinition filter = connector.getSynchronizationFilter(session, now);
		assertNull(filter);
		assertEquals("Expected unchanged timestamp", future, repository.getSynchronizationTimeStamp());

		connector.preSynchronization(session, null);
		assertTrue(session.needsPerformQueries());
		assertNotNull(repository.getSynchronizationTimeStamp());
		assertTrue("Expected updated timestamp", !future.equals(repository.getSynchronizationTimeStamp()));
	}

	public void testGetSynchronizationFilterTimeStampInTheFutureWithTask() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		Date now = new Date();
		ITask task = new TaskTask(JiraCorePlugin.CONNECTOR_KIND, repository.getRepositoryUrl(), "1");
		task.setModificationDate(now);

		SynchronizationSession session = createSession(task);
		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(addSecondsToDate(new Date(), 121)));
		FilterDefinition filter = connector.getSynchronizationFilter(session, now);
		assertNotNull(filter);
		assertTrue(filter.getUpdatedDateFilter() instanceof DateRangeFilter);
		DateRangeFilter dateFilter = (DateRangeFilter) filter.getUpdatedDateFilter();
		assertEquals(task.getModificationDate(), dateFilter.getFromDate());
		assertEquals(null, dateFilter.getToDate());
	}

	public void testCreateTask() throws Exception {
		init(JiraTestConstants.JIRA_39_URL);

		JiraIssue issue = JiraTestUtil.createIssue(client, "testCreateTask");

		ITask task = JiraTestUtil.createTask(repository, issue.getKey());
		assertEquals("testCreateTask", task.getSummary());
		assertEquals(null, task.getCompletionDate());
		assertFalse(task.isCompleted());
		assertEquals(issue.getCreated(), task.getCreationDate());

		// close issue
		issue.setResolution(client.getCache().getResolutionById(Resolution.FIXED_ID));
		client.advanceIssueWorkflow(issue, "2", "", null);

		issue = client.getIssueByKey(issue.getKey(), null);
		task = JiraTestUtil.createTask(repository, issue.getKey());
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
