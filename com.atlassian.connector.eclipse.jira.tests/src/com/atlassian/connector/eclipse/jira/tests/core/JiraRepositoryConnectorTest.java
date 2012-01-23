/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Eugene Kuleshov - improvements
 *******************************************************************************/

package com.atlassian.connector.eclipse.jira.tests.core;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.lang.time.DateUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.internal.tasks.core.TaskTask;
import org.eclipse.mylyn.internal.tasks.core.sync.SynchronizationSession;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil;
import org.eclipse.mylyn.internal.tasks.ui.wizards.EditRepositoryWizard;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.atlassian.connector.eclipse.internal.jira.core.JiraAttribute;
import com.atlassian.connector.eclipse.internal.jira.core.JiraClientFactory;
import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;
import com.atlassian.connector.eclipse.internal.jira.core.JiraRepositoryConnector;
import com.atlassian.connector.eclipse.internal.jira.core.WorkLogConverter;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import com.atlassian.connector.eclipse.internal.jira.core.model.Resolution;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ContentFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.DateFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.DateRangeFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.RelativeDateRangeFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.RelativeDateRangeFilter.RangeType;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.core.util.JiraUtil;
import com.atlassian.connector.eclipse.jira.tests.util.JiraFixture;
import com.atlassian.connector.eclipse.jira.tests.util.JiraTestResultCollector;
import com.atlassian.connector.eclipse.jira.tests.util.JiraTestUtil;

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
		init(jiraUrl());
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
		init(jiraUrl());

		JiraIssue issue = JiraTestUtil.createIssue(client, "testAttachContext");
		ITask task = JiraTestUtil.createTask(repository, issue.getId());
		assertEquals("testAttachContext", task.getSummary());
		File sourceContextFile = TasksUiPlugin.getContextStore().getFileForContext(task);
		JiraTestUtil.writeFile(sourceContextFile, "Mylyn".getBytes());
		sourceContextFile.deleteOnExit();

		boolean result = AttachmentUtil.postContext(connector, repository, task, "", null, null);
		assertTrue(result);

		task = JiraTestUtil.createTask(repository, issue.getId());
		List<ITaskAttachment> contextAttachments = AttachmentUtil.getContextAttachments(repository, task);
		assertEquals(1, contextAttachments.size());

		ITaskAttachment attachment = contextAttachments.get(0);
		result = AttachmentUtil.downloadContext(task, attachment, PlatformUI.getWorkbench().getProgressService());
		assertTrue(result);
		assertTrue(task.isActive());
	}

	public void testPerformQueryDueDateFilter() throws Exception {
		init(jiraUrl());

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
		init(jiraUrl());

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
		init(jiraUrl());

		long currentTimeMillis = System.currentTimeMillis();
		JiraTestUtil.createIssue(client, "search" + currentTimeMillis);
		JiraTestUtil.createIssue(client, "search" + currentTimeMillis);
		JiraTestUtil.createIssue(client, "search" + currentTimeMillis);
		JiraTestUtil.createIssue(client, "search" + currentTimeMillis);
		FilterDefinition filter = new FilterDefinition();
		filter.setContentFilter(new ContentFilter("search" + currentTimeMillis, true, false, false, false));
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

	/**
	 * Tests that a TaskSearch is not downloading the task details
	 */
	public void testPerformQueryTaskSearch() throws Exception {
		init(jiraUrl());
		String timestamp = Long.toString(System.currentTimeMillis());
		JiraIssue issue = JiraTestUtil.createIssue(client, "testPerformQueryTaskSearch on " + timestamp);
		JiraWorkLog log = new JiraWorkLog();
		log.setComment("a worklog");
		log.setStartDate(new Date(System.currentTimeMillis()));
		log.setTimeSpent(120);
		log.setAuthor(repository.getUserName());
		client.addWorkLog(issue.getKey(), log, null);
		FilterDefinition filter = new FilterDefinition();
		filter.setContentFilter(new ContentFilter(timestamp, true, false, false, false));
		IRepositoryQuery query = JiraTestUtil.createQuery(repository, filter);
		JiraTestResultCollector collector = new JiraTestResultCollector();
		connector.performQuery(repository, query, collector, null, null);
		assertEquals(1, collector.results.size());
		TaskData hit = collector.results.get(0);
		TaskAttribute summary = hit.getRoot().getMappedAttribute(TaskAttribute.SUMMARY);
		assertTrue(summary.getValue().contains(timestamp));
		List<TaskAttribute> worklogs = hit.getAttributeMapper().getAttributesByType(hit, WorkLogConverter.TYPE_WORKLOG);
		assertEquals(0, worklogs.size());
	}

	public void testMarkStaleNoTasks() throws Exception {
		init(jiraUrl());

		repository.setSynchronizationTimeStamp(null);
		SynchronizationSession session = createSession();
		connector.preSynchronization(session, null);
		assertTrue(session.needsPerformQueries());
		assertNotNull(repository.getSynchronizationTimeStamp());
	}

	public void testMarkStaleOneTask() throws Exception {
		init(jiraUrl());

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
		assertEquals(0, session.getStaleTasks().size());
		assertNotNull(repository.getSynchronizationTimeStamp());
		Date timestamp = JiraUtil.stringToDate(repository.getSynchronizationTimeStamp());
		assertTrue(timestamp.after(start));
		assertTrue(timestamp.before(new Date()));
		assertTrue(issue.getUpdated().before(new Date()));

		Thread.sleep(5); // make sure markStaleTasks() finds a difference

		session = createSession(task);
		connector.preSynchronization(session, null);
		assertFalse(session.needsPerformQueries());
		assertNotNull(repository.getSynchronizationTimeStamp());
		assertEquals(0, session.getStaleTasks().size());
		assertFalse("Expected updated synchronization timestamp",
				JiraUtil.dateToString(timestamp).equals(repository.getSynchronizationTimeStamp()));
		assertEquals(issue.getUpdated(), JiraUtil.getLastUpdate(repository));
	}

	public void testMarkStaleRepositoryChanged() throws Exception {
		init(jiraUrl());

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
		assertFalse("Expected updated synchronization timestamp",
				JiraUtil.dateToString(start).equals(repository.getSynchronizationTimeStamp()));
		assertEquals(issue2.getUpdated(), JiraUtil.getLastUpdate(repository));
	}

	public void testMarkStaleClosedTask() throws Exception {
		init(jiraUrl());

		// create an issue
		Date start = new Date();//new Date().getTime() + 1000 * 60 * 3);
		JiraIssue issue = JiraTestUtil.createIssue(client, "testMarkStale");
		ITask task = JiraTestUtil.createTask(repository, issue.getKey());
		assertFalse(task.isCompleted());
		// when tests were against local JIRA (in the same LAN), connector.preSynchronization could skip 
		// updating tasks with data from incoming issue as issues had sometimes "last updated" timestamp 
		// exactly the same (with seconds precision) as the timestamp of originally created created issue
		// so we are manually tweaking the modification date to help JiraRepositoryConnector.hasChanged 
		task.setModificationDate(DateUtils.addMinutes(task.getModificationDate(), -10));

		// close issue
		String resolveOperation = JiraTestUtil.getOperation(client, issue.getKey(), "resolve");
		issue.setResolution(client.getCache().getResolutionById(Resolution.FIXED_ID));
		client.advanceIssueWorkflow(issue, resolveOperation, "comment", null);
		SynchronizationSession session = createSession(task);
		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(start));
		connector.preSynchronization(session, null);
		assertTrue(session.needsPerformQueries());
		assertTrue("Expected preSynchronization() to update task", task.isCompleted());
	}

	public void testGetSynchronizationFilter() throws Exception {
		init(jiraUrl());

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
		init(jiraUrl());

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
		init(jiraUrl());

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
		init(jiraUrl());

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

	public void testGetRepositoryUrlFromTaskUrl() throws Exception {
		init(jiraUrl());
		assertEquals(null, connector.getRepositoryUrlFromTaskUrl("test"));
		assertEquals(null, connector.getRepositoryUrlFromTaskUrl("http://mylyn.eclipse.org"));
		assertEquals("http://mylyn.eclipse.org",
				connector.getRepositoryUrlFromTaskUrl("http://mylyn.eclipse.org/browse/ABC"));
		assertEquals("http://mylyn.eclipse.org/jiratest",
				connector.getRepositoryUrlFromTaskUrl("http://mylyn.eclipse.org/jiratest/browse/ABC"));
		assertEquals("http://mylyn.eclipse.org/jiratest",
				connector.getRepositoryUrlFromTaskUrl("http://mylyn.eclipse.org/jiratest/browse/ABC-123"));
	}

	public void testGetTaskIdFromTaskUrl() throws Exception {
		init(jiraUrl());
		assertEquals(null, connector.getTaskIdFromTaskUrl("test"));
		assertEquals(null, connector.getTaskIdFromTaskUrl("http://mylyn.eclipse.org"));
		assertEquals(null, connector.getTaskIdFromTaskUrl("http://mylyn.eclipse.org/browse/ABC"));
		assertEquals("ABC-123", connector.getTaskIdFromTaskUrl("http://mylyn.eclipse.org/jiratest/browse/ABC-123"));
	}

	private String jiraUrl() {
		return JiraFixture.current().getRepositoryUrl();
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
