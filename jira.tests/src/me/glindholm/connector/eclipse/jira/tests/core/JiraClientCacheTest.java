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

package me.glindholm.connector.eclipse.jira.tests.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.jupiter.api.Test;

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueType;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProject;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraVersion;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClient;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraException;
import me.glindholm.connector.eclipse.jira.tests.util.JiraTestUtil;
import me.glindholm.connector.eclipse.jira.tests.util.MockJiraClient;

/**
 * @author Pawel Niewiadomski
 */
public class JiraClientCacheTest {

	@Test
	public void testRefreshProjectDetails() throws JiraException {
		final int whatProject[] = { 1 };
		try (JiraClient mockClient = new MockJiraClient("http://localhost") {

			@Override
			public JiraIssueType[] getIssueTypes(IProgressMonitor monitor) throws JiraException {
				return switch (whatProject[0]) {
				case 2 -> new JiraIssueType[] { MockJiraClient.createIssueType("1", "Issue Type 1") };
				default -> new JiraIssueType[0];
				};
			}

			@Override
			public JiraProject[] getProjects(IProgressMonitor monitor) throws JiraException {
				return switch (whatProject[0]) {
				default -> new JiraProject[] { MockJiraClient.createProject() };
				};
			}

			@Override
			public void getProjectDetails(JiraProject project) throws JiraException {
				project.setVersions(new JiraVersion[] { MockJiraClient.createVersion("1", "Version 1"),
						MockJiraClient.createVersion("2", "Version 2") });
			}
		}) {
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
			var project = mockClient.getCache().getProjectById(JiraTestUtil.PROJECT1);
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
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
