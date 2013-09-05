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

package com.atlassian.connector.eclipse.jira.tests.core;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import junit.framework.TestCase;

import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;
import com.atlassian.connector.eclipse.internal.jira.core.model.Component;
import com.atlassian.connector.eclipse.internal.jira.core.model.IssueType;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus;
import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.model.Resolution;
import com.atlassian.connector.eclipse.internal.jira.core.model.Version;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ComponentFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ContentFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.DateRangeFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.IssueTypeFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ProjectFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ResolutionFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.SpecificUserFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.StatusFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.VersionFilter;
import com.atlassian.connector.eclipse.internal.jira.core.service.FilterDefinitionConverter;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClientCache;
import com.atlassian.connector.eclipse.internal.jira.core.util.JiraUtil;
import com.atlassian.connector.eclipse.jira.tests.util.MockJiraClient;

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
		vers[0] = new Version("ver0", "ver0");
		vers[1] = new Version("ver1", "ver1");
		vers[2] = new Version("ver2", "ver2");

		projects = new Project[2];
		projects[0] = new Project();
		projects[0].setId("prj0");
		projects[0].setKey("PROJECTZERO");
		projects[0].setComponents(comps);
		projects[0].setVersions(vers);
		projects[0].setDetails(true);
		projects[1] = new Project();
		projects[1].setId("prj1");
		projects[1].setKey("PROJECTONE");
		projects[1].setComponents(comps);
		projects[1].setVersions(vers);
		projects[1].setDetails(true);

		Component[] components = new Component[2];
		components[0] = new Component();
		components[0].setId("comp0");
		components[1] = new Component();
		components[1].setId("comp1");

		Version[] fixVersions = new Version[2];
		fixVersions[0] = new Version("ver0", "ver0 name");
		fixVersions[1] = new Version("ver1", "ver0 name");

		Version[] repoVersions = new Version[2];
		repoVersions[0] = new Version("ver1", "ver1 name");
		repoVersions[1] = new Version("ver2", "ver2 name");

		IssueType[] issueTypes = new IssueType[2];
		issueTypes[0] = new IssueType("issue0", "issue0 name", "issue0 descr", null);
		issueTypes[1] = new IssueType("issue1", "issue1 name", "issue1 descr", null);

		JiraStatus[] statuses = new JiraStatus[2];
		statuses[0] = new JiraStatus("status0");
		statuses[1] = new JiraStatus("status1");

		Resolution[] resolutions = new Resolution[2];
		resolutions[0] = new Resolution("res0id", "res0 name", "res0 descr", "res0icon");
		resolutions[1] = new Resolution("res1id", "res1 name", "res1 descr", "res1icon");

		filter = new FilterDefinition();
		filter.setProjectFilter(new ProjectFilter(projects));
		filter.setComponentFilter(new ComponentFilter(components, false));
		filter.setFixForVersionFilter(new VersionFilter(fixVersions, false, true, false));
		filter.setReportedInVersionFilter(new VersionFilter(repoVersions, false, false, true));
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
				return new IssueType(id, id, false);
			};

			@Override
			public JiraStatus getStatusById(String id) {
				return new JiraStatus(id);
			};

			@Override
			public Resolution getResolutionById(String id) {
				return new Resolution(id, id);
			};
		};
		client.setCache(cache);
	}

	public void testConversionWithFilterDefinitionConverter() {
		IRepositoryQuery customQuery = new RepositoryQuery(JiraCorePlugin.CONNECTOR_KIND, "1");
		JiraUtil.setQuery(taskRepository, customQuery, filter);
		filter2Url = customQuery.getUrl();

		FilterDefinitionConverter converter = new FilterDefinitionConverter(taskRepository.getCharacterEncoding(),
				JiraUtil.getLocalConfiguration(taskRepository).getDateFormat());
		filter2Url2filter = converter.toFilter(client, filter2Url, true);

		String filter2Url2Filter2Url = new FilterDefinitionConverter(taskRepository.getCharacterEncoding(),
				JiraUtil.getLocalConfiguration(taskRepository).getDateFormat()).toUrl("http://no.url.net/",
				filter2Url2filter);
		compareUrls(filter2Url, filter2Url2Filter2Url);
	}

	public void testGetJqlString() {
		// Date 1970-01-01 in a localized date time format and in the local time zone
		final String datetime19700101 = FilterDefinitionConverter.JQL_DATE_TIME_FORMAT.format(new Date(0));
		final String date19700101 = FilterDefinitionConverter.JQL_DATE_FORMAT.format(new Date(0));
		final String jqlExpected = calcExpectedJql(datetime19700101, date19700101);

		final FilterDefinitionConverter converter = new FilterDefinitionConverter(
				taskRepository.getCharacterEncoding(), JiraUtil.getLocalConfiguration(taskRepository).getDateFormat());
		final String jqlResult = converter.getJqlString(filter);

		assertEquals(jqlExpected, jqlResult);
	}

	public void testToJqlUrl() {
		try {
			// Date 1970-01-01 in a localized date time format and in the local time zone
			final String datetime19700101 = FilterDefinitionConverter.JQL_DATE_TIME_FORMAT.format(new Date(0));
			final String date19700101 = FilterDefinitionConverter.JQL_DATE_FORMAT.format(new Date(0));
			final String jqlUrlExpected = "http://host.net//issues/?jql="
					+ URLEncoder.encode(calcExpectedJql(datetime19700101, date19700101),
							taskRepository.getCharacterEncoding());

			final FilterDefinitionConverter converter = new FilterDefinitionConverter(
					taskRepository.getCharacterEncoding(), JiraUtil.getLocalConfiguration(taskRepository)
							.getDateFormat());
			final String jqlUrlResult = converter.toJqlUrl(taskRepository.getUrl(), filter);

			assertEquals(jqlUrlExpected, jqlUrlResult);
		} catch (UnsupportedEncodingException ex) {
			fail(ex.toString());
		}
	}

	private String calcExpectedJql(String datetime, String date) {
		return "project in (\"PROJECTZERO\",\"PROJECTONE\") "
				+ "AND component in (comp0,comp1) "
				+ "AND fixVersion in (releasedVersions(),\"ver0 name\",\"ver0 name\") "
				+ "AND affectedVersion in (unreleasedVersions(),\"ver1 name\",\"ver2 name\") "
				+ "AND issuetype in (\"issue0 name\",\"issue1 name\") "
				+ "AND status in (status0,status1) "
				+ "AND resolution in (\"res0 name\",\"res1 name\") "
				+ "AND (summary ~ \"query\" OR description ~ \"query\" OR comment ~ \"query\" OR environment ~ \"query\") "
				+ "AND reporter in (reporter) AND assignee in (assignee) " + "AND (created >= \"" + datetime
				+ "\" AND created <= \"" + datetime + "\") " + "AND (updated >= \"" + datetime + "\" AND updated <= \""
				+ datetime + "\") " + "AND (duedate >= \"" + date + "\" AND duedate <= \"" + date + "\") ";
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
				if (!found) {
					assertEquals("URLs not equal, parameter not found in second url: " + paramOne, urlOne, urlTwo);
				}
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
