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

package me.glindholm.connector.eclipse.jira.tests.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.internal.tasks.core.TaskTask;
import org.eclipse.mylyn.internal.tasks.core.sync.SynchronizationSession;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil;
import org.eclipse.mylyn.internal.tasks.ui.wizards.EditRepositoryWizard;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.ui.PlatformUI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.glindholm.connector.eclipse.internal.jira.core.JiraAttribute;
import me.glindholm.connector.eclipse.internal.jira.core.JiraClientFactory;
import me.glindholm.connector.eclipse.internal.jira.core.JiraCorePlugin;
import me.glindholm.connector.eclipse.internal.jira.core.JiraRepositoryConnector;
import me.glindholm.connector.eclipse.internal.jira.core.WorkLogConverter;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraResolution;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ContentFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.DateFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.DateRangeFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.RelativeDateRangeFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.RelativeDateRangeFilter.RangeType;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClient;
import me.glindholm.connector.eclipse.internal.jira.core.util.JiraUtil;
import me.glindholm.connector.eclipse.jira.tests.util.JiraFixture;
import me.glindholm.connector.eclipse.jira.tests.util.JiraTestResultCollector;
import me.glindholm.connector.eclipse.jira.tests.util.JiraTestUtil;
import me.glindholm.jira.rest.client.api.domain.BasicUser;

/**
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 */
public class JiraRepositoryConnectorTest  {

	private TaskRepository repository;

	private JiraRepositoryConnector connector;

	private JiraClient client;

	@BeforeEach
	protected void setUp() throws Exception {
		JiraTestUtil.setUp();
	}

	@AfterEach
	protected void tearDown() throws Exception {
		JiraTestUtil.tearDown();
	}

	protected void init(final String url) throws Exception {
		repository = JiraTestUtil.init(url);
		connector = (JiraRepositoryConnector) TasksUi.getRepositoryConnector(JiraCorePlugin.CONNECTOR_KIND);
		client = JiraClientFactory.getDefault().getJiraClient(repository);
	}

	private SynchronizationSession createSession(final ITask... tasks) {
		final var session = new SynchronizationSession(TasksUiPlugin.getTaskDataManager());
		session.setNeedsPerformQueries(true);
		session.setTaskRepository(repository);
		session.setFullSynchronization(true);
		session.setTasks(new HashSet<>(Arrays.asList(tasks)));
		return session;
	}

	@Test
	public void testChangeTaskRepositorySettings() throws Exception {
		init(jiraUrl());
		assertEquals(repository.getUserName(), repository.getUserName());

		final var wizard = new EditRepositoryWizard(repository);
		final var shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		final var dialog = new WizardDialog(shell, wizard);
		dialog.create();

		((AbstractRepositorySettingsPage) wizard.getSettingsPage()).setUserId("newuser");
		assertTrue(wizard.performFinish());

		client = JiraClientFactory.getDefault().getJiraClient(repository);
		assertEquals("newuser", client.getUserName());
	}

	@Test
	public void testAttachContext() throws Exception {
		init(jiraUrl());

		final var issue = JiraTestUtil.createIssue(client, "testAttachContext");
		var task = JiraTestUtil.createTask(repository, issue.getId());
		assertEquals("testAttachContext", task.getSummary());
		final var sourceContextFile = TasksUiPlugin.getContextStore().getFileForContext(task);
		JiraTestUtil.writeFile(sourceContextFile, "Mylyn".getBytes());
		sourceContextFile.deleteOnExit();

		var result = AttachmentUtil.postContext(connector, repository, task, "", null, null);
		assertTrue(result);

		task = JiraTestUtil.createTask(repository, issue.getId());
		final var contextAttachments = AttachmentUtil.getContextAttachments(repository, task);
		assertEquals(1, contextAttachments.size());

		final var attachment = contextAttachments.get(0);
		result = AttachmentUtil.downloadContext(task, attachment, PlatformUI.getWorkbench().getProgressService());
		assertTrue(result);
		assertTrue(task.isActive());
	}

	@Test
	public void testPerformQueryDueDateFilter() throws Exception {
		init(jiraUrl());

		final var c = new GregorianCalendar();
		c.add(Calendar.MONTH, 1);
		final var fromDate = c.getTime();
		final var toDate = c.getTime();
		final DateFilter dueDateFilter = new DateRangeFilter(fromDate.toInstant(), toDate.toInstant());
		final var filter = new FilterDefinition();
		filter.setDueDateFilter(dueDateFilter);
		final var query = JiraTestUtil.createQuery(repository, filter);

		final var collector1 = new JiraTestResultCollector();
		connector.performQuery(repository, query, collector1, null, null);

		var issue = JiraTestUtil.newIssue(client, "testDueDateFilter");
		issue.setDue(fromDate.toInstant());
		issue = JiraTestUtil.createIssue(client, issue);
		assertNotNull(issue);

		final var collector2 = new JiraTestResultCollector();
		connector.performQuery(repository, query, collector2, null, new NullProgressMonitor());
		assertEquals(collector1.results.size() + 1, collector2.results.size());
		for (final TaskData taskData : collector2.results) {
			final var owner = taskData.getRoot().getAttribute(JiraAttribute.USER_ASSIGNED.id()).getValue();
			assertNotNull(owner);
			assertTrue(owner.length() > 0);
		}
	}

	@Test
	public void testPerformQuerySpaces() throws Exception {
		init(jiraUrl());

		final var currentTimeMillis = System.currentTimeMillis();
		final var summary1 = "test search for spaces " + currentTimeMillis;
		JiraTestUtil.createIssue(client, summary1);
		final var summary2 = "test search for spaces " + (currentTimeMillis + 1);
		JiraTestUtil.createIssue(client, summary2);

		var queryString = currentTimeMillis + " " + (currentTimeMillis + 1);
		var filter = new FilterDefinition();
		filter.setContentFilter(new ContentFilter(queryString, true, false, false, false));

		var query = JiraTestUtil.createQuery(repository, filter);
		var collector = new JiraTestResultCollector();
		connector.performQuery(repository, query, collector, null, new NullProgressMonitor());

		// "abc def" is translated in JQL to ~"abc def" (not OR as it was for classic search)
		assertEquals(0, collector.results.size());

		queryString = currentTimeMillis + "";
		filter = new FilterDefinition();
		filter.setContentFilter(new ContentFilter(queryString, true, false, false, false));

		query = JiraTestUtil.createQuery(repository, filter);
		collector = new JiraTestResultCollector();
		connector.performQuery(repository, query, collector, null, new NullProgressMonitor());

		assertEquals(1, collector.results.size());
	}

	@Test
	public void testPerformQueryLimitNumberOfResults() throws Exception {
		init(jiraUrl());

		final var currentTimeMillis = System.currentTimeMillis();
		JiraTestUtil.createIssue(client, "search" + currentTimeMillis);
		JiraTestUtil.createIssue(client, "search" + currentTimeMillis);
		JiraTestUtil.createIssue(client, "search" + currentTimeMillis);
		JiraTestUtil.createIssue(client, "search" + currentTimeMillis);
		final var filter = new FilterDefinition();
		filter.setContentFilter(new ContentFilter("search" + currentTimeMillis, true, false, false, false));
		final var query = JiraTestUtil.createQuery(repository, filter);

		try {
			//			JiraUtil.setMaxSearchResults(repository, JiraUtil.DEFAULT_MAX_SEARCH_RESULTS);
			var collector = new JiraTestResultCollector();
			connector.performQuery(repository, query, collector, null, null);
			assertEquals(4, collector.results.size());

			//			JiraUtil.setMaxSearchResults(repository, 2);
			client.getLocalConfiguration().setMaxSearchResults(2);
			collector = new JiraTestResultCollector();
			connector.performQuery(repository, query, collector, null, null);
			assertEquals(2, collector.results.size());

			//			JiraUtil.setMaxSearchResults(repository, -1);
			// <= 0 means default
			client.getLocalConfiguration().setMaxSearchResults(-1);
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
	@Test
	public void testPerformQueryTaskSearch() throws Exception {
		init(jiraUrl());
		final var timestamp = Long.toString(System.currentTimeMillis());
		final var issue = JiraTestUtil.createIssue(client, "testPerformQueryTaskSearch on " + timestamp);
		final var log = new JiraWorkLog();
		log.setComment("a worklog");
		log.setStartDate(new Date().toInstant());
		log.setTimeSpent(120);
		log.setAuthor(new BasicUser(null, repository.getUserName(), repository.getUserName()));
		client.addWorkLog(issue.getKey(), log, null);
		final var filter = new FilterDefinition();
		filter.setContentFilter(new ContentFilter(timestamp, true, false, false, false));
		final var query = JiraTestUtil.createQuery(repository, filter);
		final var collector = new JiraTestResultCollector();
		connector.performQuery(repository, query, collector, null, null);
		assertEquals(1, collector.results.size());
		final var hit = collector.results.get(0);
		final var summary = hit.getRoot().getMappedAttribute(TaskAttribute.SUMMARY);
		assertTrue(summary.getValue().contains(timestamp));
		final List<TaskAttribute> worklogs = hit.getAttributeMapper().getAttributesByType(hit, WorkLogConverter.TYPE_WORKLOG);
		assertEquals(0, worklogs.size());
	}

	//	public void testMarkStaleNoTasks() throws Exception {
	//		init(jiraUrl());
	//
	//		repository.setSynchronizationTimeStamp(null);
	//		SynchronizationSession session = createSession();
	//		connector.preSynchronization(session, null);
	//		assertTrue(session.needsPerformQueries());
	//		assertNotNull(repository.getSynchronizationTimeStamp());
	//	}

	//	public void testMarkStaleOneTask() throws Exception {
	//		init(jiraUrl());
	//
	//		JiraIssue issue = JiraTestUtil.createIssue(client, "testMarkStale");
	//		Date start = new Date();
	//		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(start));
	//		ITask task = JiraTestUtil.createTask(repository, issue.getKey());
	//		task.setModificationDate(null);
	//		Thread.sleep(5); // make sure markStaleTasks() finds a difference
	//		assertNull(JiraUtil.getLastUpdate(repository));
	//
	//		SynchronizationSession session = createSession(task);
	//		connector.preSynchronization(session, null);
	//		assertTrue(session.needsPerformQueries());
	//		assertEquals(0, session.getStaleTasks().size());
	//		assertNotNull(repository.getSynchronizationTimeStamp());
	//		Date timestamp = JiraUtil.stringToDate(repository.getSynchronizationTimeStamp());
	//		assertTrue(timestamp.after(start));
	//		assertTrue(timestamp.before(new Date()));
	//		assertTrue(issue.getUpdated().before(new Date()));
	//
	//		Thread.sleep(5); // make sure markStaleTasks() finds a difference
	//
	//		session = createSession(task);
	//		connector.preSynchronization(session, null);
	//		assertFalse(session.needsPerformQueries());
	//		assertNotNull(repository.getSynchronizationTimeStamp());
	//		assertEquals(0, session.getStaleTasks().size());
	//		assertFalse("Expected updated synchronization timestamp",
	//				JiraUtil.dateToString(timestamp).equals(repository.getSynchronizationTimeStamp()));
	//		assertEquals(issue.getUpdated(), JiraUtil.getLastUpdate(repository));
	//	}

	//	public void testMarkStaleRepositoryChanged() throws Exception {
	//		init(jiraUrl());
	//
	//		// create two issues, the first one is added to the task list
	//		Date start = new Date();
	//		JiraIssue issue = JiraTestUtil.createIssue(client, "testMarkStale");
	//		ITask task = JiraTestUtil.createTask(repository, issue.getKey());
	//		// make sure the second issue is created after the first one
	//		Thread.sleep(1000);
	//		JiraIssue issue2 = JiraTestUtil.createIssue(client, "testMarkStale2");
	//		assertTrue(issue2.getUpdated().after(issue.getUpdated()));
	//		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(start));
	//
	//		SynchronizationSession session = createSession(task);
	//		connector.preSynchronization(session, null);
	//		assertTrue(session.needsPerformQueries());
	//		assertFalse("Expected updated synchronization timestamp",
	//				JiraUtil.dateToString(start).equals(repository.getSynchronizationTimeStamp()));
	//		assertEquals(issue2.getUpdated(), JiraUtil.getLastUpdate(repository));
	//	}

	//	public void testMarkStaleClosedTask() throws Exception {
	//		init(jiraUrl());
	//
	//		// create an issue
	//		Date start = new Date();//new Date().getTime() + 1000 * 60 * 3);
	//		JiraIssue issue = JiraTestUtil.createIssue(client, "testMarkStale");
	//		ITask task = JiraTestUtil.createTask(repository, issue.getKey());
	//		assertFalse(task.isCompleted());
	//		// when tests were against local JIRA (in the same LAN), connector.preSynchronization could skip
	//		// updating tasks with data from incoming issue as issues had sometimes "last updated" timestamp
	//		// exactly the same (with seconds precision) as the timestamp of originally created created issue
	//		// so we are manually tweaking the modification date to help JiraRepositoryConnector.hasChanged
	//		task.setModificationDate(DateUtils.addMinutes(task.getModificationDate(), -10));
	//
	//		// close issue
	//		String resolveOperation = JiraTestUtil.getOperation(client, issue.getKey(), "resolve");
	//		issue.setResolution(client.getCache().getResolutionByName(Resolution.FIXED_NAME));
	//		client.advanceIssueWorkflow(issue, resolveOperation, "comment", null);
	//		SynchronizationSession session = createSession(task);
	//		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(start));
	//		connector.preSynchronization(session, null);
	//		connector.postSynchronization(session, null);
	//		assertTrue(session.needsPerformQueries());
	//		assertTrue("Expected preSynchronization() to update task", task.isCompleted());
	//	}

	@Test
	public void testGetSynchronizationFilter() throws Exception {
		init(jiraUrl());

		final var now = new Date().toInstant();
		final ITask task = new TaskTask(JiraCorePlugin.CONNECTOR_KIND, repository.getRepositoryUrl(), "1");
		final var session = createSession(task);

		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(now));
		var filter = connector.getSynchronizationFilter(session, addSecondsToDate(now, 1));
		assertNotNull(filter);
		assertTrue(filter.getUpdatedDateFilter() instanceof RelativeDateRangeFilter);
		var dateFilter = (RelativeDateRangeFilter) filter.getUpdatedDateFilter();
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

	//	public void testGetSynchronizationFilterTimeStampInTheFuture() throws Exception {
	//		init(jiraUrl());
	//
	//		ITask task = new TaskTask(JiraCorePlugin.CONNECTOR_KIND, repository.getRepositoryUrl(), "1");
	//		Date now = new Date();
	//		String future = JiraUtil.dateToString(addSecondsToDate(now, 20));
	//		SynchronizationSession session = createSession(task);
	//
	//		repository.setSynchronizationTimeStamp(future);
	//		FilterDefinition filter = connector.getSynchronizationFilter(session, now);
	//		assertNull(filter);
	//		assertEquals("Expected unchanged timestamp", future, repository.getSynchronizationTimeStamp());
	//
	//		connector.preSynchronization(session, null);
	//		assertTrue(session.needsPerformQueries());
	//		assertNotNull(repository.getSynchronizationTimeStamp());
	//		assertTrue("Expected updated timestamp", !future.equals(repository.getSynchronizationTimeStamp()));
	//	}

	@Test
	public void testGetSynchronizationFilterTimeStampInTheFutureWithTask() throws Exception {
		init(jiraUrl());

		final var now = new Date();
		final ITask task = new TaskTask(JiraCorePlugin.CONNECTOR_KIND, repository.getRepositoryUrl(), "1");
		task.setModificationDate(now);

		final var session = createSession(task);
		repository.setSynchronizationTimeStamp(JiraUtil.dateToString(addSecondsToDate(new Date().toInstant(), 121)));
		final var filter = connector.getSynchronizationFilter(session, now.toInstant());

		assertNotNull(filter);
		assertTrue(filter.getUpdatedDateFilter() instanceof DateRangeFilter);
		final var dateFilter = (DateRangeFilter) filter.getUpdatedDateFilter();
		assertEquals(task.getModificationDate(), dateFilter.getFromDate());
		assertEquals(null, dateFilter.getToDate());
	}

	@Test
	public void testCreateTask() throws Exception {
		init(jiraUrl());

		var issue = JiraTestUtil.createIssue(client, "testCreateTask");

		var task = JiraTestUtil.createTask(repository, issue.getKey());
		assertEquals("testCreateTask", task.getSummary());
		assertEquals(null, task.getCompletionDate());
		assertFalse(task.isCompleted());
		assertEquals(issue.getCreated(), task.getCreationDate());

		// close issue
		issue.setResolution(client.getCache().getResolutionByName(JiraResolution.FIXED_NAME));
		client.advanceIssueWorkflow(issue, "2", "", null);

		issue = client.getIssueByKey(issue.getKey(), null);
		task = JiraTestUtil.createTask(repository, issue.getKey());
		assertTrue(task.isCompleted());
		assertEquals(issue.getUpdated(), task.getCompletionDate());
		assertEquals(issue.getCreated(), task.getCreationDate());
	}

	private Instant addSecondsToDate(final Instant updated, final int i) {
		return Instant.ofEpochMilli(updated.toEpochMilli() + i * 1000);
	}

	@Test
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

	@Test
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
