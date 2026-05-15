/*******************************************************************************
 * Copyright (c) 2004, 2009 Eugene Kuleshov and others.
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.glindholm.connector.eclipse.internal.jira.core.InvalidJiraQueryException;
import me.glindholm.connector.eclipse.internal.jira.core.JiraCorePlugin;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraComponent;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueType;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProject;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraResolution;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraStatus;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraVersion;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ComponentFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ContentFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.DateRangeFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.IssueTypeFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ProjectFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ResolutionFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.SpecificUserFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.StatusFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.VersionFilter;
import me.glindholm.connector.eclipse.internal.jira.core.service.FilterDefinitionConverter;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClient;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClientCache;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraLocalConfiguration;
import me.glindholm.connector.eclipse.internal.jira.core.util.JiraUtil;
import me.glindholm.connector.eclipse.jira.tests.util.JiraFixture;
import me.glindholm.connector.eclipse.jira.tests.util.JiraTestUtil;
import me.glindholm.connector.eclipse.jira.tests.util.MockJiraClient;

/**
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer (multiple projects selection)
 */
public class JiraCustomQueryTest  {

	private JiraProject[] projects;

	@BeforeEach
	protected void setUp() throws Exception {
		final var comps = new JiraComponent[3];
		comps[0] = new JiraComponent();
		comps[0].setId("comp0");
		comps[1] = new JiraComponent();
		comps[1].setId("comp1");
		comps[2] = new JiraComponent();
		comps[2].setId("comp2");

		final var vers = new JiraVersion[3];
		vers[0] = new JiraVersion("ver0", "ver0");
		vers[1] = new JiraVersion("ver1", "ver1");
		vers[2] = new JiraVersion("ver2", "ver2");

		projects = new JiraProject[2];
		projects[0] = new JiraProject();
		projects[0].setId("prj0");
		projects[0].setComponents(comps);
		projects[0].setVersions(vers);
		projects[0].setDetails(true);
		projects[1] = new JiraProject();
		projects[1].setId("prj1");
		projects[1].setComponents(comps);
		projects[1].setVersions(vers);
		projects[1].setDetails(true);
	}

	@Test
	public void testJiraCustomQuery() {
		final var repositoryUrl = "http://host.net/";

		final var components = new JiraComponent[2];
		components[0] = new JiraComponent();
		components[0].setId("comp0");
		components[1] = new JiraComponent();
		components[1].setId("comp1");

		final var fixVersions = new JiraVersion[2];
		fixVersions[0] = new JiraVersion("ver0", "ver0");
		fixVersions[1] = new JiraVersion("ver1", "ver1");

		final var repoVersions = new JiraVersion[2];
		repoVersions[0] = new JiraVersion("ver1", "ver1");
		repoVersions[1] = new JiraVersion("ver2", "ver2");

		final var issueTypes = new JiraIssueType[2];
		issueTypes[0] = new JiraIssueType("issue0", "issue0", false);
		issueTypes[1] = new JiraIssueType("issue1", "issue1", false);

		final var statuses = new JiraStatus[2];
		statuses[0] = new JiraStatus("status0");
		statuses[1] = new JiraStatus("status1");

		final var resolutions = new JiraResolution[2];
		resolutions[0] = new JiraResolution("resolution0", "resolution0");
		resolutions[1] = new JiraResolution("resolution1", "resolution0");

		final var filter = new FilterDefinition();
		filter.setProjectFilter(new ProjectFilter(projects));
		filter.setComponentFilter(new ComponentFilter(components, true));
		filter.setFixForVersionFilter(new VersionFilter(fixVersions, true, true, true));
		filter.setReportedInVersionFilter(new VersionFilter(repoVersions, true, true, true));
		filter.setIssueTypeFilter(new IssueTypeFilter(issueTypes));
		filter.setStatusFilter(new StatusFilter(statuses));
		filter.setResolutionFilter(new ResolutionFilter(resolutions));

		filter.setContentFilter(new ContentFilter("query", true, true, true, true));

		filter.setReportedByFilter(new SpecificUserFilter("reporter"));
		filter.setAssignedToFilter(new SpecificUserFilter("assignee"));

		filter.setCreatedDateFilter(new DateRangeFilter(new Date(10).toInstant(), new Date(12).toInstant()));
		filter.setUpdatedDateFilter(new DateRangeFilter(new Date(20).toInstant(), new Date(22).toInstant()));
		filter.setDueDateFilter(new DateRangeFilter(new Date(30).toInstant(), new Date(32).toInstant()));

		final var taskRepository = new TaskRepository(JiraCorePlugin.CONNECTOR_KIND, repositoryUrl);
		taskRepository.setCharacterEncoding("ASCII");
		final var customQuery = JiraTestUtil.createQuery(taskRepository, filter);
		final var queryUrl = customQuery.getAttribute(JiraUtil.KEY_FILTER_CUSTOM_URL);

		final var client = new MockJiraClient("");

		final JiraClientCache cache = new JiraClientCache(client) {
			@Override
			public JiraProject getProjectById(final String id) {
				for (final JiraProject prj : projects) {
					if (prj.getId().equals(id)) {
						return prj;
					}
				}
				return null;
			}

			@Override
			public JiraProject[] getProjects() {
				return projects;
			}

			@Override
			public JiraIssueType getIssueTypeById(final String id) {
				return new JiraIssueType(id, id, false);
			}

			@Override
			public JiraStatus getStatusById(final String id) {
				return new JiraStatus(id);
			}

			@Override
			public JiraResolution getResolutionById(final String id) {
				return new JiraResolution(id, id);
			}
		};
		client.setCache(cache);

		final var converter = new FilterDefinitionConverter(taskRepository.getCharacterEncoding(),
				JiraUtil.getLocalConfiguration(taskRepository).getDateFormat());
		final var filter2 = converter.toFilter(client, queryUrl, true);

		final var projectFilter2 = filter2.getProjectFilter();
		assertEquals(2, projects.length);
		assertEquals(projects[0].getId(), projectFilter2.getProjects()[0].getId());
		assertEquals(projects[1].getId(), projectFilter2.getProjects()[1].getId());

		final var components2 = filter2.getComponentFilter().getComponents();
		assertEquals(2, components2.length);
		assertEquals(components[0].getId(), components2[0].getId());
		assertEquals(components[1].getId(), components2[1].getId());
		assertTrue(filter2.getComponentFilter().hasNoComponent());

		final var repoVersions2 = filter2.getReportedInVersionFilter().getVersions();
		assertEquals(2, repoVersions2.length);
		assertEquals(repoVersions[0].getId(), repoVersions2[0].getId());
		assertEquals(repoVersions[1].getId(), repoVersions2[1].getId());
		assertTrue(filter2.getReportedInVersionFilter().hasNoVersion());
		assertTrue(filter2.getReportedInVersionFilter().isReleasedVersions());
		assertTrue(filter2.getReportedInVersionFilter().isUnreleasedVersions());

		final var fixVersions2 = filter2.getFixForVersionFilter().getVersions();
		assertEquals(2, fixVersions2.length);
		assertEquals(fixVersions[0].getId(), fixVersions2[0].getId());
		assertEquals(fixVersions[1].getId(), fixVersions2[1].getId());
		assertTrue(filter2.getFixForVersionFilter().hasNoVersion());
		assertTrue(filter2.getFixForVersionFilter().isUnreleasedVersions());
		assertTrue(filter2.getFixForVersionFilter().isReleasedVersions());

		final var issueTypes2 = filter2.getIssueTypeFilter().getIsueTypes();
		assertEquals(2, issueTypes2.length);
		assertEquals(issueTypes[0].getId(), issueTypes2[0].getId());
		assertEquals(issueTypes[1].getId(), issueTypes2[1].getId());

		final var statuses2 = filter2.getStatusFilter().getStatuses();
		assertEquals(2, statuses2.length);
		assertEquals(statuses[0].getId(), statuses2[0].getId());
		assertEquals(statuses[1].getId(), statuses2[1].getId());

		final var resolutions2 = filter2.getResolutionFilter().getResolutions();
		assertEquals(2, resolutions2.length);
		assertEquals(resolutions[0].getId(), resolutions2[0].getId());
		assertEquals(resolutions[1].getId(), resolutions2[1].getId());

		final var contentFilter2 = filter2.getContentFilter();
		assertEquals("query", contentFilter2.getQueryString());
		assertEquals(true, contentFilter2.isSearchingComments());
		assertEquals(true, contentFilter2.isSearchingDescription());
		assertEquals(true, contentFilter2.isSearchingEnvironment());
		assertEquals(true, contentFilter2.isSearchingSummary());

		final var reportedByFilter2 = filter2.getReportedByFilter();
		assertTrue(reportedByFilter2 instanceof SpecificUserFilter);
		assertEquals("reporter", ((SpecificUserFilter) reportedByFilter2).getUser());

		final var assigneeFilter2 = filter2.getAssignedToFilter();
		assertTrue(assigneeFilter2 instanceof SpecificUserFilter);
		assertEquals("assignee", ((SpecificUserFilter) assigneeFilter2).getUser());

		final var createdDateFilter2 = filter.getCreatedDateFilter();
		assertTrue(createdDateFilter2 instanceof DateRangeFilter);
		assertEquals(10, ((DateRangeFilter) createdDateFilter2).getFromDate());
		assertEquals(12, ((DateRangeFilter) createdDateFilter2).getToDate());

		final var updatedDateFilter2 = filter.getUpdatedDateFilter();
		assertTrue(updatedDateFilter2 instanceof DateRangeFilter);
		assertEquals(20, ((DateRangeFilter) updatedDateFilter2).getFromDate());
		assertEquals(22, ((DateRangeFilter) updatedDateFilter2).getToDate());

		final var dueDateFilter2 = filter.getDueDateFilter();
		assertTrue(dueDateFilter2 instanceof DateRangeFilter);
		assertEquals(30, ((DateRangeFilter) dueDateFilter2).getFromDate());
		assertEquals(32, ((DateRangeFilter) dueDateFilter2).getToDate());
	}

	@Test
	public void testGetFilterDefinitionUnresolvedResolution() {
		final var repositoryUrl = JiraFixture.current().getRepositoryUrl();
		final var client = new MockJiraClient(repositoryUrl);
		final var converter = new FilterDefinitionConverter(JiraClient.DEFAULT_CHARSET,
				new SimpleDateFormat(JiraLocalConfiguration.DEFAULT_DATE_PATTERN, Locale.US));

		var filter = new FilterDefinition();
		filter.setResolutionFilter(new ResolutionFilter(new JiraResolution[0]));
		var queryUrl = converter.toUrl(repositoryUrl, filter);
		filter = converter.toFilter(client, queryUrl, true);

		var resolutionFilter = filter.getResolutionFilter();
		assertNotNull(resolutionFilter);
		assertTrue(resolutionFilter.isUnresolved());

		filter = new FilterDefinition();
		final var resolutions = new JiraResolution[1];
		resolutions[0] = new JiraResolution("123", "123");
		resolutionFilter = new ResolutionFilter(resolutions);
		filter.setResolutionFilter(resolutionFilter);
		queryUrl = converter.toUrl(repositoryUrl, filter);
		try {
			filter = converter.toFilter(client, queryUrl, true);
			fail("Expected InvalidJiraQueryException, got: " + filter);
		} catch (final InvalidJiraQueryException e) {
		}
	}

	@Test
	public void testJiraCustomQueryIncompleteDateFilter() {
		final var repositoryUrl = "http://host.net/";

		final var filter = new FilterDefinition();
		filter.setProjectFilter(new ProjectFilter(projects));

		filter.setCreatedDateFilter(new DateRangeFilter(new Date(10).toInstant(), null));
		filter.setUpdatedDateFilter(new DateRangeFilter(null, new Date(22).toInstant()));
		filter.setDueDateFilter(new DateRangeFilter(null, null));

		final var taskRepository = new TaskRepository(JiraCorePlugin.CONNECTOR_KIND, repositoryUrl);
		taskRepository.setCharacterEncoding("ASCII");
		final var customQuery = JiraTestUtil.createQuery(taskRepository, filter);
		final var queryUrl = customQuery.getAttribute(JiraUtil.KEY_FILTER_CUSTOM_URL);

		final var client = new MockJiraClient("");

		final JiraClientCache cache = new JiraClientCache(client) {
			@Override
			public JiraProject getProjectById(final String id) {
				for (final JiraProject prj : projects) {
					if (prj.getId().equals(id)) {
						return prj;
					}
				}
				return null;
			}

			@Override
			public JiraProject[] getProjects() {
				return projects;
			}

			@Override
			public JiraIssueType getIssueTypeById(final String id) {
				return new JiraIssueType(id, id, false);
			}

			@Override
			public JiraStatus getStatusById(final String id) {
				return new JiraStatus(id);
			}

			@Override
			public JiraResolution getResolutionById(final String id) {
				return new JiraResolution(id, id);
			}
		};
		client.setCache(cache);

		final var converter = new FilterDefinitionConverter(taskRepository.getCharacterEncoding(),
				JiraUtil.getLocalConfiguration(taskRepository).getDateFormat());
		final var filter2 = converter.toFilter(client, queryUrl, true);

		final var projectFilter2 = filter2.getProjectFilter();
		assertEquals(2, projects.length);
		assertEquals(projects[0].getId(), projectFilter2.getProjects()[0].getId());
		assertEquals(projects[1].getId(), projectFilter2.getProjects()[1].getId());

		final var createdDateFilter2 = filter.getCreatedDateFilter();
		assertTrue(createdDateFilter2 instanceof DateRangeFilter);
		assertEquals(10, ((DateRangeFilter) createdDateFilter2).getFromDate());
		assertNull(((DateRangeFilter) createdDateFilter2).getToDate());

		final var updatedDateFilter2 = filter.getUpdatedDateFilter();
		assertTrue(updatedDateFilter2 instanceof DateRangeFilter);
		assertNull(((DateRangeFilter) updatedDateFilter2).getFromDate());
		assertEquals(22, ((DateRangeFilter) updatedDateFilter2).getToDate());

		final var dueDateFilter2 = filter.getDueDateFilter();
		assertTrue(dueDateFilter2 instanceof DateRangeFilter);
		assertNull(((DateRangeFilter) dueDateFilter2).getFromDate());
		assertNull(((DateRangeFilter) dueDateFilter2).getToDate());
	}
}