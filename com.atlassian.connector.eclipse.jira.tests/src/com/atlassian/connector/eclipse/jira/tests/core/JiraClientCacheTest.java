/*******************************************************************************
 * Copyright (c) 2009 Pawel Niewiadomski and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pawel Niewiadomski - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.jira.tests.core;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.atlassian.connector.eclipse.internal.jira.core.model.IssueType;
import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.model.Version;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.connector.eclipse.jira.tests.util.JiraTestUtil;
import com.atlassian.connector.eclipse.jira.tests.util.MockJiraClient;

/**
 * @author Pawel Niewiadomski
 */
public class JiraClientCacheTest extends TestCase {

	public void testRefreshProjectDetails() throws JiraException {
		final int whatProject[] = { 1 };
		JiraClient mockClient = new MockJiraClient("http://localhost") {

			@Override
			public IssueType[] getIssueTypes(IProgressMonitor monitor) throws JiraException {
				switch (whatProject[0]) {
				case 2:
					return new IssueType[] { MockJiraClient.createIssueType("1", "Issue Type 1") };
				default:
					return new IssueType[0];
				}
			}

			@Override
			public Project[] getProjects(IProgressMonitor monitor) throws JiraException {
				switch (whatProject[0]) {
				default:
					return new Project[] { MockJiraClient.createProject() };
				}
			};

			@Override
			public Version[] getVersions(String key, IProgressMonitor monitor) throws JiraException {
				switch (whatProject[0]) {
				case 2:
					return new Version[] { MockJiraClient.createVersion("1", "Version 1"),
							MockJiraClient.createVersion("2", "Version 2") };
				default:
					return null;
				}
			}
		};

		// not initialized yet
		assertFalse(mockClient.getCache().hasDetails());
		assertNotNull(mockClient.getCache().getPriorities());
		assertEquals(0, mockClient.getCache().getPriorities().length);
		assertNotNull(mockClient.getCache().getProjects());
		assertEquals(0, mockClient.getCache().getProjects().length);

		mockClient.getCache().refreshDetails(new NullProgressMonitor());

		assertTrue(mockClient.getCache().hasDetails());
		assertNotNull(mockClient.getCache().getPriorities());
		assertEquals(0, mockClient.getCache().getPriorities().length);
		assertNotNull(mockClient.getCache().getProjects());
		assertEquals(1, mockClient.getCache().getProjects().length);

		// check project
		Project project = mockClient.getCache().getProjectById(JiraTestUtil.PROJECT1);
		assertNotNull(project);

		whatProject[0] = 2;

		mockClient.getCache().refreshProjectDetails(JiraTestUtil.PROJECT1, new NullProgressMonitor());
		project = mockClient.getCache().getProjectById(JiraTestUtil.PROJECT1);
		assertNotNull(project);

		// were versions updated?
		assertNotNull(project.getVersions());
		assertEquals(2, project.getVersions().length);

		// validate that refreshProjectDetails didn't refresh anything else than the selected project
		assertNotNull(mockClient.getCache().getIssueTypes());
		assertEquals(0, mockClient.getCache().getIssueTypes().length);
	}
}
