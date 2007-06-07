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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;

import junit.framework.TestCase;

import org.eclipse.mylar.internal.jira.core.model.Component;
import org.eclipse.mylar.internal.jira.core.model.IssueType;
import org.eclipse.mylar.internal.jira.core.model.Project;
import org.eclipse.mylar.internal.jira.core.model.Resolution;
import org.eclipse.mylar.internal.jira.core.model.Status;
import org.eclipse.mylar.internal.jira.core.model.Version;
import org.eclipse.mylar.internal.jira.core.model.filter.ComponentFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.ContentFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.DateFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.DateRangeFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylar.internal.jira.core.model.filter.IssueTypeFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.ProjectFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.ResolutionFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.SpecificUserFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.StatusFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.UserFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.VersionFilter;
import org.eclipse.mylar.internal.jira.core.service.JiraClient;
import org.eclipse.mylar.internal.jira.ui.InvalidJiraQueryException;
import org.eclipse.mylar.internal.jira.ui.JiraCustomQuery;
import org.eclipse.mylar.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylar.tasks.core.TaskRepository;

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

		Status[] statuses = new Status[2];
		statuses[0] = new Status();
		statuses[0].setId("status0");
		statuses[1] = new Status();
		statuses[1].setId("status1");

		Resolution[] resolutions = new Resolution[2];
		resolutions[0] = new Resolution();
		resolutions[0].setId("resolution0");
		resolutions[1] = new Resolution();
		resolutions[1].setId("resolution1");

		FilterDefinition filter = new FilterDefinition();
		filter.setName("filter");
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

		TaskRepository taskRepository = new TaskRepository(JiraUiPlugin.REPOSITORY_KIND, repositoryUrl);
		taskRepository.setCharacterEncoding("ASCII");

		JiraCustomQuery customQuery = new JiraCustomQuery(repositoryUrl, filter, taskRepository.getCharacterEncoding());

		String queryUrl = customQuery.getUrl();

		JiraClient jiraServer = (JiraClient) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class[] {JiraClient.class},
				new InvocationHandler() {
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						String name = method.getName();
						if("getProjectById".equals(name)) {
							return project;
						} else if("getIssueTypeById".equals(name)) {
							IssueType issueType = new IssueType();
							issueType.setId((String) args[0]);
							return issueType;
						} else if("getStatusById".equals(name)) {
							Status status = new Status();
							status.setId((String) args[0]);
							return status;
						} else if("getResolutionById".equals(name)) {
							Resolution resolution = new Resolution();
							resolution.setId((String) args[0]);
							return resolution;
						}
						return null;
					}
				});

		JiraCustomQuery customQuery2 = new JiraCustomQuery("test", queryUrl, repositoryUrl, taskRepository
				.getCharacterEncoding());

		FilterDefinition filter2 = customQuery2.getFilterDefinition(jiraServer, true);

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

		Status[] statuses2 = filter2.getStatusFilter().getStatuses();
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
		String repositoryUrl = JiraTestConstants.JIRA_39_URL;

		FilterDefinition filter = new FilterDefinition();
		filter.setResolutionFilter(new ResolutionFilter(new Resolution[0]));
		JiraCustomQuery customQuery = new JiraCustomQuery(repositoryUrl, filter, JiraClient.CHARSET);
		MockJiraClient client = new MockJiraClient(repositoryUrl);
		filter = customQuery.getFilterDefinition(client, true);
		ResolutionFilter resolutionFilter = filter.getResolutionFilter();
		assertNotNull(resolutionFilter);
		assertTrue(resolutionFilter.isUnresolved());
		
		filter = new FilterDefinition();
		Resolution[] resolutions = new Resolution[1];
		resolutions[0] = new Resolution();
		resolutions[0].setId("123");
 		resolutionFilter = new ResolutionFilter(resolutions);
		filter.setResolutionFilter(resolutionFilter);
		customQuery = new JiraCustomQuery(repositoryUrl, filter, JiraClient.CHARSET);
		try {
			filter = customQuery.getFilterDefinition(client, true);
			fail("Expected InvalidJiraQueryException, got: " + filter);
		} catch (InvalidJiraQueryException e) {
		}
	}
}