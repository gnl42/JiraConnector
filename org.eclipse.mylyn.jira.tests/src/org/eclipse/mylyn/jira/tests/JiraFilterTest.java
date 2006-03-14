/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylar project committers - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.jira.tests;

import junit.framework.TestCase;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylar.internal.jira.JiraRepositoryQuery;
import org.eclipse.mylar.internal.jira.JiraQueryHit;
import org.eclipse.mylar.internal.jira.JiraRepositoryConnector;
import org.eclipse.mylar.internal.jira.JiraServerFacade;
import org.eclipse.mylar.internal.jira.MylarJiraPlugin;
import org.eclipse.mylar.provisional.tasklist.AbstractRepositoryConnector;
import org.eclipse.mylar.provisional.tasklist.MylarTaskListPlugin;
import org.eclipse.mylar.provisional.tasklist.TaskRepository;
import org.tigris.jira.core.model.NamedFilter;

/**
 * @author Wesley Coelho (initial integration patch) *
 */
public class JiraFilterTest extends TestCase {

	private final static String USER = "mylartest";

	private final static String PASSWORD = "mylartest";

	private final static String SERVER_URL = "http://developer.atlassian.com/jira";

	private JiraServerFacade jiraFacade = null;

	private TaskRepository repository = null;

	private JiraRepositoryConnector connector = new JiraRepositoryConnector();

	protected void setUp() throws Exception {
		super.setUp();
		repository = new TaskRepository(MylarJiraPlugin.REPOSITORY_KIND, SERVER_URL);
		repository.setAuthenticationCredentials(USER, PASSWORD);
		MylarTaskListPlugin.getRepositoryManager().addRepository(repository);
		jiraFacade = JiraServerFacade.getDefault();
	}

	protected void tearDown() throws Exception {
		AbstractRepositoryConnector client = MylarTaskListPlugin.getRepositoryManager().getRepositoryConnector(
				MylarJiraPlugin.REPOSITORY_KIND);
		assertNotNull(client);
//		MylarTaskListPlugin.getTaskListManager().getTaskList().clearArchive();
		// client.clearArchive();
//		MylarTaskListPlugin.getTaskListManager().getTaskList().clear();
		MylarTaskListPlugin.getTaskListManager().resetTaskList();
		MylarTaskListPlugin.getRepositoryManager().removeRepository(repository);
		jiraFacade.logOutFromAll();
		super.tearDown();
	}

	public void testJiraFilterRefresh() {
		NamedFilter[] filters = jiraFacade.getJiraServer(repository).getNamedFilters();
		assertTrue(filters.length > 0);
		JiraRepositoryQuery jFilter = new JiraRepositoryQuery(repository.getUrl(), filters[0], MylarTaskListPlugin.getTaskListManager().getTaskList());
		assertTrue(jFilter.getHits().size() == 0);
		// jFilter.refreshHits();
		// boolean done = false;

		Job job = connector.synchronize(jFilter, null);
		while (job.getResult() == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		assertTrue(jFilter.getHits().size() > 0);
		JiraQueryHit jHit = (JiraQueryHit) jFilter.getHits().iterator().next();
		assertTrue(jHit.getDescription().length() > 0);
	}
}
