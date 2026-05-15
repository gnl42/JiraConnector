/*******************************************************************************
 * Copyright (c) 2004, 2008 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package me.glindholm.connector.eclipse.jira.tests.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.tasks.ui.search.RepositorySearchResult;
import org.eclipse.mylyn.internal.tasks.ui.search.SearchHitCollector;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.glindholm.connector.eclipse.internal.jira.core.JiraClientFactory;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssue;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClient;
import me.glindholm.connector.eclipse.internal.jira.ui.JiraSearchHandler;
import me.glindholm.connector.eclipse.jira.tests.util.JiraFixture;
import me.glindholm.connector.eclipse.jira.tests.util.JiraTestUtil;

/**
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 */
public class JiraStackTraceDuplicateDetectorTest  {

	private TaskRepository repository;

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
		client = JiraClientFactory.getDefault().getJiraClient(repository);
	}

	@Test
	public void testStackTraceInDescription() throws Exception {
		init(jiraUrl());

		final var sw = new StringWriter();
		new Exception().printStackTrace(new PrintWriter(sw));
		final var stackTrace = sw.toString();
		var issue1 = JiraTestUtil.newIssue(client, "testStackTraceDetector1");
		issue1.setDescription(stackTrace);
		issue1 = JiraTestUtil.createIssue(client, issue1);

		verifyDuplicate(stackTrace, issue1);
	}

	@Test
	public void testStackTraceInComment() throws Exception {
		init(jiraUrl());

		final var sw = new StringWriter();
		new Exception().printStackTrace(new PrintWriter(sw));
		final var stackTrace = sw.toString();
		final var issue1 = JiraTestUtil.createIssue(client, "testStackTraceDetector2");
		client.updateIssue(issue1, stackTrace, null, null);

		verifyDuplicate(stackTrace, issue1);
	}

	@Test
	public void verifyDuplicate(final String stackTrace, final JiraIssue issue) throws Exception {
		final var task1 = JiraTestUtil.createTask(repository, issue.getKey());
		assertEquals(issue.getSummary(), task1.getSummary());
		assertEquals(false, task1.isCompleted());
		assertNull(task1.getDueDate());

		final var issue2 = JiraTestUtil.newIssue(client, "testStackTraceDetector1");
		issue2.setDescription(stackTrace);

		//		TaskData data = new TaskData(dataHandler.getAttributeMapper(repository), repository.getConnectorKind(),
		//				repository.getRepositoryUrl(), "");
		//		dataHandler.initializeTaskData(repository, data, new TaskMapping() {
		//			@Override
		//			public String getProduct() {
		//				return issue.getProject().getId();
		//			}
		//		}, null);
		//		data.getRoot().getAttribute(JiraAttribute.DESCRIPTION.id()).setValue(stackTrace);

		final var detector = new JiraSearchHandler();
		final var duplicatesQuery = TasksUi.getRepositoryModel().createRepositoryQuery(repository);
		assertTrue(detector.queryForText(repository, duplicatesQuery, null, stackTrace));
		final var collector = new SearchHitCollector(TasksUiInternal.getTaskList(), repository,
				duplicatesQuery);
		collector.run(new NullProgressMonitor());
		final var searchResult = (RepositorySearchResult) collector.getSearchResult();
		assertTrue(searchResult.getMatchCount() > 0, "Expected duplicated task " + issue.getId() + " : " + issue.getKey());
		for (final Object element : searchResult.getElements()) {
			final var task = (ITask) element;
			if (task.getTaskId().equals(issue.getId())) {
				return;
			}
		}
		fail("Duplicated task not found " + issue.getId() + " : " + issue.getKey());
	}

	private String jiraUrl() {
		return JiraFixture.current().getRepositoryUrl();
	}
}
