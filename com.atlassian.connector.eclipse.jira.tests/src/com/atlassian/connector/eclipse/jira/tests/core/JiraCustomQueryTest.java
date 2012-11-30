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

package com.atlassian.connector.eclipse.jira.tests.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import junit.framework.TestCase;

import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import com.atlassian.connector.eclipse.internal.jira.core.InvalidJiraQueryException;
import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;
import com.atlassian.connector.eclipse.internal.jira.core.model.Component;
import com.atlassian.connector.eclipse.internal.jira.core.model.IssueType;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus;
import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.model.Resolution;
import com.atlassian.connector.eclipse.internal.jira.core.model.Version;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ComponentFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ContentFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.DateFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.DateRangeFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.IssueTypeFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ProjectFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ResolutionFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.SpecificUserFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.StatusFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.UserFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.VersionFilter;
import com.atlassian.connector.eclipse.internal.jira.core.service.FilterDefinitionConverter;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClientCache;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraLocalConfiguration;
import com.atlassian.connector.eclipse.internal.jira.core.util.JiraUtil;
import com.atlassian.connector.eclipse.jira.tests.util.JiraFixture;
import com.atlassian.connector.eclipse.jira.tests.util.JiraTestUtil;
import com.atlassian.connector.eclipse.jira.tests.util.MockJiraClient;

/**
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer (multiple projects selection)
 */
public class JiraCustomQueryTest extends TestCase {

	private Project[] projects;

	@Override
	protected void setUp() throws Exception {
		Component[] comps = new Component[3];
		comps[0] = new Component();
		comps[0].setId("comp0");
		comps[1] = new Component();
		comps[1].setId("comp1");
		comps[2] = new Component();
		comps[2].setId("comp2");

		Version[] vers = new Version[3];
		vers[0] = new Version("ver0");
		vers[1] = new Version("ver1");
		vers[2] = new Version("ver2");

		projects = new Project[2];
		projects[0] = new Project();
		projects[0].setId("prj0");
		projects[0].setComponents(comps);
		projects[0].setVersions(vers);
		projects[0].setDetails(true);
		projects[1] = new Project();
		projects[1].setId("prj1");
		projects[1].setComponents(comps);
		projects[1].setVersions(vers);
		projects[1].setDetails(true);
	}

	public void testJiraCustomQuery() {
		String repositoryUrl = "http://host.net/";

		Component[] components = new Component[2];
		components[0] = new Component();
		components[0].setId("comp0");
		components[1] = new Component();
		components[1].setId("comp1");

		Version[] fixVersions = new Version[2];
		fixVersions[0] = new Version("ver0");
		fixVersions[1] = new Version("ver1");

		Version[] repoVersions = new Version[2];
		repoVersions[0] = new Version("ver1");
		repoVersions[1] = new Version("ver2");

		IssueType[] issueTypes = new IssueType[2];
		issueTypes[0] = new IssueType("issue0", false);
		issueTypes[1] = new IssueType("issue1", false);

		JiraStatus[] statuses = new JiraStatus[2];
		statuses[0] = new JiraStatus("status0");
		statuses[1] = new JiraStatus("status1");

		Resolution[] resolutions = new Resolution[2];
		resolutions[0] = new Resolution("resolution0");
		resolutions[1] = new Resolution("resolution1");

		FilterDefinition filter = new FilterDefinition();
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

		filter.setCreatedDateFilter(new DateRangeFilter(new Date(10), new Date(12)));
		filter.setUpdatedDateFilter(new DateRangeFilter(new Date(20), new Date(22)));
		filter.setDueDateFilter(new DateRangeFilter(new Date(30), new Date(32)));

		TaskRepository taskRepository = new TaskRepository(JiraCorePlugin.CONNECTOR_KIND, repositoryUrl);
		taskRepository.setCharacterEncoding("ASCII");
		IRepositoryQuery customQuery = JiraTestUtil.createQuery(taskRepository, filter);
		String queryUrl = customQuery.getUrl();

		MockJiraClient client = new MockJiraClient("");

		JiraClientCache cache = new JiraClientCache(client) {
			@Override
			public Project getProjectById(String id) {
				for (Project prj : projects) {
					if (prj.getId().equals(id)) {
						return prj;
					}
				}
				return null;
			}

			@Override
			public Project[] getProjects() {
				return projects;
			}

			@Override
			public IssueType getIssueTypeById(String id) {
				return new IssueType(id, false);
			};

			@Override
			public JiraStatus getStatusById(String id) {
				return new JiraStatus(id);
			};

			@Override
			public Resolution getResolutionById(String id) {
				return new Resolution(id);
			};
		};
		client.setCache(cache);

		FilterDefinitionConverter converter = new FilterDefinitionConverter(taskRepository.getCharacterEncoding(),
				JiraUtil.getLocalConfiguration(taskRepository).getDateFormat());
		FilterDefinition filter2 = converter.toFilter(client, queryUrl, true);

		ProjectFilter projectFilter2 = filter2.getProjectFilter();
		assertEquals(2, projects.length);
		assertEquals(projects[0].getId(), projectFilter2.getProjects()[0].getId());
		assertEquals(projects[1].getId(), projectFilter2.getProjects()[1].getId());

		Component[] components2 = filter2.getComponentFilter().getComponents();
		assertEquals(2, components2.length);
		assertEquals(components[0].getId(), components2[0].getId());
		assertEquals(components[1].getId(), components2[1].getId());
		assertTrue(filter2.getComponentFilter().hasNoComponent());

		Version[] repoVersions2 = filter2.getReportedInVersionFilter().getVersions();
		assertEquals(2, repoVersions2.length);
		assertEquals(repoVersions[0].getId(), repoVersions2[0].getId());
		assertEquals(repoVersions[1].getId(), repoVersions2[1].getId());
		assertTrue(filter2.getReportedInVersionFilter().hasNoVersion());
		assertTrue(filter2.getReportedInVersionFilter().isReleasedVersions());
		assertTrue(filter2.getReportedInVersionFilter().isUnreleasedVersions());

		Version[] fixVersions2 = filter2.getFixForVersionFilter().getVersions();
		assertEquals(2, fixVersions2.length);
		assertEquals(fixVersions[0].getId(), fixVersions2[0].getId());
		assertEquals(fixVersions[1].getId(), fixVersions2[1].getId());
		assertTrue(filter2.getFixForVersionFilter().hasNoVersion());
		assertTrue(filter2.getFixForVersionFilter().isUnreleasedVersions());
		assertTrue(filter2.getFixForVersionFilter().isReleasedVersions());

		IssueType[] issueTypes2 = filter2.getIssueTypeFilter().getIsueTypes();
		assertEquals(2, issueTypes2.length);
		assertEquals(issueTypes[0].getId(), issueTypes2[0].getId());
		assertEquals(issueTypes[1].getId(), issueTypes2[1].getId());

		JiraStatus[] statuses2 = filter2.getStatusFilter().getStatuses();
		assertEquals(2, statuses2.length);
		assertEquals(statuses[0].getId(), statuses2[0].getId());
		assertEquals(statuses[1].getId(), statuses2[1].getId());

		Resolution[] resolutions2 = filter2.getResolutionFilter().getResolutions();
		assertEquals(2, resolutions2.length);
		assertEquals(resolutions[0].getId(), resolutions2[0].getId());
		assertEquals(resolutions[1].getId(), resolutions2[1].getId());

		ContentFilter contentFilter2 = filter2.getContentFilter();
		assertEquals("query", contentFilter2.getQueryString());
		assertEquals(true, contentFilter2.isSearchingComments());
		assertEquals(true, contentFilter2.isSearchingDescription());
		assertEquals(true, contentFilter2.isSearchingEnvironment());
		assertEquals(true, contentFilter2.isSearchingSummary());

		UserFilter reportedByFilter2 = filter2.getReportedByFilter();
		assertTrue(reportedByFilter2 instanceof SpecificUserFilter);
		assertEquals("reporter", ((SpecificUserFilter) reportedByFilter2).getUser());

		UserFilter assigneeFilter2 = filter2.getAssignedToFilter();
		assertTrue(assigneeFilter2 instanceof SpecificUserFilter);
		assertEquals("assignee", ((SpecificUserFilter) assigneeFilter2).getUser());

		DateFilter createdDateFilter2 = filter.getCreatedDateFilter();
		assertTrue(createdDateFilter2 instanceof DateRangeFilter);
		assertEquals(10, ((DateRangeFilter) createdDateFilter2).getFromDate().getTime());
		assertEquals(12, ((DateRangeFilter) createdDateFilter2).getToDate().getTime());

		DateFilter updatedDateFilter2 = filter.getUpdatedDateFilter();
		assertTrue(updatedDateFilter2 instanceof DateRangeFilter);
		assertEquals(20, ((DateRangeFilter) updatedDateFilter2).getFromDate().getTime());
		assertEquals(22, ((DateRangeFilter) updatedDateFilter2).getToDate().getTime());

		DateFilter dueDateFilter2 = filter.getDueDateFilter();
		assertTrue(dueDateFilter2 instanceof DateRangeFilter);
		assertEquals(30, ((DateRangeFilter) dueDateFilter2).getFromDate().getTime());
		assertEquals(32, ((DateRangeFilter) dueDateFilter2).getToDate().getTime());
	}

	public void testGetFilterDefinitionUnresolvedResolution() {
		String repositoryUrl = JiraFixture.current().getRepositoryUrl();
		MockJiraClient client = new MockJiraClient(repositoryUrl);
		FilterDefinitionConverter converter = new FilterDefinitionConverter(JiraClient.DEFAULT_CHARSET,
				new SimpleDateFormat(JiraLocalConfiguration.DEFAULT_DATE_PATTERN, Locale.US));

		FilterDefinition filter = new FilterDefinition();
		filter.setResolutionFilter(new ResolutionFilter(new Resolution[0]));
		String queryUrl = converter.toUrl(repositoryUrl, filter);
		filter = converter.toFilter(client, queryUrl, true);

		ResolutionFilter resolutionFilter = filter.getResolutionFilter();
		assertNotNull(resolutionFilter);
		assertTrue(resolutionFilter.isUnresolved());

		filter = new FilterDefinition();
		Resolution[] resolutions = new Resolution[1];
		resolutions[0] = new Resolution("123");
		resolutionFilter = new ResolutionFilter(resolutions);
		filter.setResolutionFilter(resolutionFilter);
		queryUrl = converter.toUrl(repositoryUrl, filter);
		try {
			filter = converter.toFilter(client, queryUrl, true);
			fail("Expected InvalidJiraQueryException, got: " + filter);
		} catch (InvalidJiraQueryException e) {
		}
	}

	public void testJiraCustomQueryIncompleteDateFilter() {
		String repositoryUrl = "http://host.net/";

		FilterDefinition filter = new FilterDefinition();
		filter.setProjectFilter(new ProjectFilter(projects));

		filter.setCreatedDateFilter(new DateRangeFilter(new Date(10), null));
		filter.setUpdatedDateFilter(new DateRangeFilter(null, new Date(22)));
		filter.setDueDateFilter(new DateRangeFilter(null, null));

		TaskRepository taskRepository = new TaskRepository(JiraCorePlugin.CONNECTOR_KIND, repositoryUrl);
		taskRepository.setCharacterEncoding("ASCII");
		IRepositoryQuery customQuery = JiraTestUtil.createQuery(taskRepository, filter);
		String queryUrl = customQuery.getUrl();

		MockJiraClient client = new MockJiraClient("");

		JiraClientCache cache = new JiraClientCache(client) {
			@Override
			public Project getProjectById(String id) {
				for (Project prj : projects) {
					if (prj.getId().equals(id)) {
						return prj;
					}
				}
				return null;
			}

			@Override
			public Project[] getProjects() {
				return projects;
			}

			@Override
			public IssueType getIssueTypeById(String id) {
				return new IssueType(id, false);
			};

			@Override
			public JiraStatus getStatusById(String id) {
				return new JiraStatus(id);
			};

			@Override
			public Resolution getResolutionById(String id) {
				Resolution resolution = new Resolution(id);
				return resolution;
			};
		};
		client.setCache(cache);

		FilterDefinitionConverter converter = new FilterDefinitionConverter(taskRepository.getCharacterEncoding(),
				JiraUtil.getLocalConfiguration(taskRepository).getDateFormat());
		FilterDefinition filter2 = converter.toFilter(client, queryUrl, true);

		ProjectFilter projectFilter2 = filter2.getProjectFilter();
		assertEquals(2, projects.length);
		assertEquals(projects[0].getId(), projectFilter2.getProjects()[0].getId());
		assertEquals(projects[1].getId(), projectFilter2.getProjects()[1].getId());

		DateFilter createdDateFilter2 = filter.getCreatedDateFilter();
		assertTrue(createdDateFilter2 instanceof DateRangeFilter);
		assertEquals(10, ((DateRangeFilter) createdDateFilter2).getFromDate().getTime());
		assertNull(((DateRangeFilter) createdDateFilter2).getToDate());

		DateFilter updatedDateFilter2 = filter.getUpdatedDateFilter();
		assertTrue(updatedDateFilter2 instanceof DateRangeFilter);
		assertNull(((DateRangeFilter) updatedDateFilter2).getFromDate());
		assertEquals(22, ((DateRangeFilter) updatedDateFilter2).getToDate().getTime());

		DateFilter dueDateFilter2 = filter.getDueDateFilter();
		assertTrue(dueDateFilter2 instanceof DateRangeFilter);
		assertNull(((DateRangeFilter) dueDateFilter2).getFromDate());
		assertNull(((DateRangeFilter) dueDateFilter2).getToDate());
	}
}