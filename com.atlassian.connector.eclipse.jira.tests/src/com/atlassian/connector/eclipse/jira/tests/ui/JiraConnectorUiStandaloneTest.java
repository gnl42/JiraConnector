/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.jira.tests.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import junit.framework.TestCase;

import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TaskHyperlink;

import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.ui.JiraConnectorUi;

/**
 * @author Thomas Ehrnhoefer
 */
public class JiraConnectorUiStandaloneTest extends TestCase {

	public void testPatternFindHyperlink() {
		Project[] projects = new Project[2];
		projects[0] = new Project("prone");
		projects[0].setKey("PRONE");
		projects[1] = new Project("some");
		projects[1].setKey("SOME");
		TaskRepository repository = new TaskRepository("connectorKind", "url");

		IHyperlink[] result = findHyperlinks(repository, projects, "foo", -1, 0);
		assertNull(result);

		result = findHyperlinks(repository, projects, "PRONE", -1, 0);
		assertNull(result);

		result = findHyperlinks(repository, projects, "PRONE-1", -1, 0);
		assertNotNull(result);
		assertEquals(1, result.length);
		assertEquals(new Region(0, 7), result[0].getHyperlinkRegion());
		assertEquals("PRONE-1", ((TaskHyperlink) result[0]).getTaskId());

		result = findHyperlinks(repository, projects, " PRONE-1", 2, 3);
		assertNotNull(result);
		assertEquals(1, result.length);
		assertEquals(new Region(4, 7), result[0].getHyperlinkRegion());
		assertEquals("PRONE-1", ((TaskHyperlink) result[0]).getTaskId());

		result = findHyperlinks(repository, projects, " PRONE-1 abc PRONE-23 ABC-123 ", 2, 3);
		assertNotNull(result);
		assertEquals(2, result.length);
		assertEquals(new Region(4, 7), result[0].getHyperlinkRegion());
		assertEquals("PRONE-1", ((TaskHyperlink) result[0]).getTaskId());
		assertEquals(new Region(16, 8), result[1].getHyperlinkRegion());
		assertEquals("PRONE-23", ((TaskHyperlink) result[1]).getTaskId());

		result = findHyperlinks(repository, projects, " PRONE-1 abc PRONE-23 SOME-55 ABC-123 ", 2, 3);
		assertNotNull(result);
		assertEquals(3, result.length);
		assertEquals(new Region(4, 7), result[0].getHyperlinkRegion());
		assertEquals("PRONE-1", ((TaskHyperlink) result[0]).getTaskId());
		assertEquals(new Region(16, 8), result[1].getHyperlinkRegion());
		assertEquals("PRONE-23", ((TaskHyperlink) result[1]).getTaskId());
		assertEquals(new Region(25, 7), result[2].getHyperlinkRegion());
		assertEquals("SOME-55", ((TaskHyperlink) result[2]).getTaskId());

		result = findHyperlinks(repository, projects, " PRONE-1 abc PRONE-2 ABC-123 ", -1, 3);
		assertNotNull(result);
		assertEquals(2, result.length);
		assertEquals(new Region(4, 7), result[0].getHyperlinkRegion());
		assertEquals("PRONE-1", ((TaskHyperlink) result[0]).getTaskId());
		assertEquals(new Region(16, 7), result[1].getHyperlinkRegion());
		assertEquals("PRONE-2", ((TaskHyperlink) result[1]).getTaskId());

		result = findHyperlinks(repository, projects, "PRONE-PRONE-1", -1, 0);
		assertNull(result);

		result = findHyperlinks(repository, projects, "PRONE-1-somethingelse", 2, 3);
		assertNotNull(result);
		assertEquals(1, result.length);
		assertEquals(new Region(3, 7), result[0].getHyperlinkRegion());
		assertEquals("PRONE-1", ((TaskHyperlink) result[0]).getTaskId());

		result = findHyperlinks(repository, projects, "(PRONE-1)", -1, 0);
		assertNotNull(result);
		assertEquals(1, result.length);

		result = findHyperlinks(repository, projects, " PRONE-1", -1, 0);
		assertNotNull(result);
		assertEquals(1, result.length);
	}

	private IHyperlink[] findHyperlinks(TaskRepository repository, Project[] projects, String text, int index,
			int textOffset) {
		List<IHyperlink> links = null;
		Matcher m = JiraConnectorUi.TASK_PATTERN.matcher(text);
		while (m.find()) {
			String projectKey = m.group(3);
			if (containsProjectKey(projects, projectKey)) {
				if (links == null) {
					links = new ArrayList<IHyperlink>();
				}
				Region region = new Region(textOffset + m.start(2), m.end() - m.start(2));
				links.add(new TaskHyperlink(region, repository, m.group(2)));
			}
		}
		return links == null ? null : links.toArray(new IHyperlink[0]);
	}

	private boolean containsProjectKey(Project[] projects, String projectKey) {
		for (Project project : projects) {
			if (project.getKey().equals(projectKey)) {
				return true;
			}
		}
		return false;
	}

}
