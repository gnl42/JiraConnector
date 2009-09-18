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

package org.eclipse.mylyn.jira.tests.core;

import java.util.Date;

import junit.framework.TestCase;

import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.model.filter.ComponentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ContentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.DateRangeFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueTypeFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ProjectFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ResolutionFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.SpecificUserFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.StatusFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.VersionFilter;
import org.eclipse.mylyn.internal.jira.core.service.FilterDefinitionConverter;
import org.eclipse.mylyn.internal.jira.core.service.JiraClientCache;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.jira.tests.util.MockJiraClient;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * @author Thomas Ehrnhoefer
 */
public class FilterDefinitionConverterTest extends TestCase {

	private static final String CHARACTER_ENC_ASCII = "ASCII";

	private Project[] projects;

	private String filter2Url;

	private FilterDefinition filter;

	private FilterDefinition filter2Url2filter;

	private MockJiraClient client;

	private TaskRepository taskRepository;

	@Override
	protected void setUp() throws Exception {
		String repositoryUrl = "http://host.net/";
		taskRepository = new TaskRepository(JiraCorePlugin.CONNECTOR_KIND, repositoryUrl);
		taskRepository.setCharacterEncoding(CHARACTER_ENC_ASCII);

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

		projects = new Project[2];
		projects[0] = new Project();
		projects[0].setId("prj0");
		projects[0].setComponents(comps);
		projects[0].setVersions(vers);
		projects[1] = new Project();
		projects[1].setId("prj1");
		projects[1].setComponents(comps);
		projects[1].setVersions(vers);

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

		filter = new FilterDefinition();
		filter.setProjectFilter(new ProjectFilter(projects));
		filter.setComponentFilter(new ComponentFilter(components));
		filter.setFixForVersionFilter(new VersionFilter(true, true));
		filter.setReportedInVersionFilter(new VersionFilter(true, true));
		filter.setIssueTypeFilter(new IssueTypeFilter(issueTypes));
		filter.setStatusFilter(new StatusFilter(statuses));
		filter.setResolutionFilter(new ResolutionFilter(resolutions));

		filter.setContentFilter(new ContentFilter("query", true, true, true, true));

		filter.setReportedByFilter(new SpecificUserFilter("reporter"));
		filter.setAssignedToFilter(new SpecificUserFilter("assignee"));

		filter.setCreatedDateFilter(new DateRangeFilter(new Date(10), new Date(12)));
		filter.setUpdatedDateFilter(new DateRangeFilter(new Date(20), new Date(22)));
		filter.setDueDateFilter(new DateRangeFilter(new Date(30), new Date(32)));

		client = new MockJiraClient("");
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
	}

	public void testConversionWithFilterDefinitionConverter() {
		IRepositoryQuery customQuery = new RepositoryQuery(JiraCorePlugin.CONNECTOR_KIND, "1");
		JiraUtil.setQuery(taskRepository, customQuery, filter);
		filter2Url = customQuery.getUrl();

		FilterDefinitionConverter converter = new FilterDefinitionConverter(taskRepository.getCharacterEncoding(),
				JiraUtil.getConfiguration(taskRepository).getDateFormat());
		filter2Url2filter = converter.toFilter(client, filter2Url, true);

		String filter2Url2Filter2Url = new FilterDefinitionConverter(taskRepository.getCharacterEncoding(),
				JiraUtil.getConfiguration(taskRepository).getDateFormat()).toUrl("http://no.url.net/",
				filter2Url2filter);
		compareUrls(filter2Url, filter2Url2Filter2Url);
	}

	private void compareUrls(String urlOne, String urlTwo) {
		//compare the fields that should be in the url - ignore all other parameters / parts of the URL
		urlOne = urlOne.substring(urlOne.indexOf('?') + 1);
		urlTwo = urlTwo.substring(urlTwo.indexOf('?') + 1);
		String[] paramsOne = urlOne.split("&");
		String[] paramsTwo = urlTwo.split("&");

		for (String paramOne : paramsOne) {
			if (checkRelevance(paramOne)) {
				//parameter relevant, check if the whole thing is also contained in the second url
				boolean found = false;
				for (String paramTwo : paramsTwo) {
					if (paramOne.equals(paramTwo)) {
						found = true;
						break;
					}
				}
				assertTrue("URLs unequal - parameter not found in second url: " + paramOne, found);
			}
		}

	}

	private boolean checkRelevance(String paramOne) {
		// check if the parameter is one we care about
		if (paramOne.contains("pid=") || paramOne.contains("component=") || paramOne.contains("fixfor=")
				|| paramOne.contains("version=") || paramOne.contains("type=") || paramOne.contains("status=")
				|| paramOne.contains("resolution=") || paramOne.contains("query=") || paramOne.contains("summary=")
				|| paramOne.contains("description=") || paramOne.contains("body=") || paramOne.contains("environment=")
				|| paramOne.contains("reporterSelect=") || paramOne.contains("reporter=")
				|| paramOne.contains("assigneeSelect=") || paramOne.contains("assignee=")
				|| paramOne.contains("created:after=") || paramOne.contains("created:before=")
				|| paramOne.contains("updated:after=") || paramOne.contains("updated:before=")
				|| paramOne.contains("duedate:after=") || paramOne.contains("duedate:before=")) {
			return true;
		}
		return false;
	}
}
