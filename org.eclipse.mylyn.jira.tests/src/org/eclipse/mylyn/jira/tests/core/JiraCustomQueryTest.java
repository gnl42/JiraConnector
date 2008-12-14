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

package org.eclipse.mylyn.jira.tests.core;

import java.util.Date;

import junit.framework.TestCase;

import org.eclipse.mylyn.internal.jira.core.InvalidJiraQueryException;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.model.filter.ComponentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ContentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.DateFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.DateRangeFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueTypeFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ProjectFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ResolutionFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.SpecificUserFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.StatusFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.UserFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.VersionFilter;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraClientCache;
import org.eclipse.mylyn.internal.jira.core.util.FilterDefinitionConverter;
import org.eclipse.mylyn.jira.tests.util.JiraTestConstants;
import org.eclipse.mylyn.jira.tests.util.JiraTestUtil;
import org.eclipse.mylyn.jira.tests.util.MockJiraClient;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 */
public class JiraCustomQueryTest extends TestCase {

	private Project project;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Component[] comps = new Component[3];
		comps[0] = new Component();
		comps[0].setId("comp0");
		comps[1] = new Component();
		comps[1].setId("comp1");
		comps[2] = new Component();
		comps[2].setId("comp2");

		Version[] vers = new Version[3];
		vers[0] = new Version();
		vers[0].setId("ver0");
		vers[1] = new Version();
		vers[1].setId("ver1");
		vers[2] = new Version();
		vers[2].setId("ver2");

		project = new Project();
		project.setId("000");
		project.setComponents(comps);
		project.setVersions(vers);
	}

	public void testJiraCustomQuery() {
		String repositoryUrl = "http://host.net/";

		Component[] components = new Component[2];
		components[0] = new Component();
		components[0].setId("comp0");
		components[1] = new Component();
		components[1].setId("comp1");

		Version[] fixVersions = new Version[2];
		fixVersions[0] = new Version();
		fixVersions[0].setId("ver0");
		fixVersions[1] = new Version();
		fixVersions[1].setId("ver1");

		Version[] repoVersions = new Version[2];
		repoVersions[0] = new Version();
		repoVersions[0].setId("ver1");
		repoVersions[1] = new Version();
		repoVersions[1].setId("ver2");

		IssueType[] issueTypes = new IssueType[2];
		issueTypes[0] = new IssueType();
		issueTypes[0].setId("issue0");
		issueTypes[1] = new IssueType();
		issueTypes[1].setId("issue1");

		JiraStatus[] statuses = new JiraStatus[2];
		statuses[0] = new JiraStatus();
		statuses[0].setId("status0");
		statuses[1] = new JiraStatus();
		statuses[1].setId("status1");

		Resolution[] resolutions = new Resolution[2];
		resolutions[0] = new Resolution();
		resolutions[0].setId("resolution0");
		resolutions[1] = new Resolution();
		resolutions[1].setId("resolution1");

		FilterDefinition filter = new FilterDefinition();
		filter.setProjectFilter(new ProjectFilter(project));
		filter.setComponentFilter(new ComponentFilter(components));
		filter.setFixForVersionFilter(new VersionFilter(fixVersions));
		filter.setReportedInVersionFilter(new VersionFilter(repoVersions));
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
				return project;
			}

			@Override
			public IssueType getIssueTypeById(String id) {
				IssueType issueType = new IssueType();
				issueType.setId(id);
				return issueType;
			};

			@Override
			public JiraStatus getStatusById(String id) {
				JiraStatus status = new JiraStatus();
				status.setId(id);
				return status;
			};

			@Override
			public Resolution getResolutionById(String id) {
				Resolution resolution = new Resolution();
				resolution.setId(id);
				return resolution;
			};
		};
		client.setCache(cache);

		FilterDefinitionConverter converter = new FilterDefinitionConverter(taskRepository.getCharacterEncoding());
		FilterDefinition filter2 = converter.toFilter(client, queryUrl, true);

		ProjectFilter projectFilter2 = filter2.getProjectFilter();
		assertEquals(project.getId(), projectFilter2.getProject().getId());

		Component[] components2 = filter2.getComponentFilter().getComponents();
		assertEquals(2, components2.length);
		assertEquals(components[0].getId(), components2[0].getId());
		assertEquals(components[1].getId(), components2[1].getId());

		Version[] repoVersions2 = filter2.getReportedInVersionFilter().getVersions();
		assertEquals(2, repoVersions2.length);
		assertEquals(repoVersions[0].getId(), repoVersions2[0].getId());
		assertEquals(repoVersions[1].getId(), repoVersions2[1].getId());

		Version[] fixVersions2 = filter2.getFixForVersionFilter().getVersions();
		assertEquals(2, fixVersions2.length);
		assertEquals(fixVersions[0].getId(), fixVersions2[0].getId());
		assertEquals(fixVersions[1].getId(), fixVersions2[1].getId());

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
		String repositoryUrl = JiraTestConstants.JIRA_LATEST_URL;
		MockJiraClient client = new MockJiraClient(repositoryUrl);
		FilterDefinitionConverter converter = new FilterDefinitionConverter(JiraClient.DEFAULT_CHARSET);

		FilterDefinition filter = new FilterDefinition();
		filter.setResolutionFilter(new ResolutionFilter(new Resolution[0]));
		String queryUrl = converter.toUrl(repositoryUrl, filter);
		filter = converter.toFilter(client, queryUrl, true);

		ResolutionFilter resolutionFilter = filter.getResolutionFilter();
		assertNotNull(resolutionFilter);
		assertTrue(resolutionFilter.isUnresolved());

		filter = new FilterDefinition();
		Resolution[] resolutions = new Resolution[1];
		resolutions[0] = new Resolution();
		resolutions[0].setId("123");
		resolutionFilter = new ResolutionFilter(resolutions);
		filter.setResolutionFilter(resolutionFilter);
		queryUrl = converter.toUrl(repositoryUrl, filter);
		try {
			filter = converter.toFilter(client, queryUrl, true);
			fail("Expected InvalidJiraQueryException, got: " + filter);
		} catch (InvalidJiraQueryException e) {
		}
	}
}