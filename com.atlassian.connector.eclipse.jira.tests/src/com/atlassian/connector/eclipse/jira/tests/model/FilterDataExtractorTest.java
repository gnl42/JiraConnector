/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.jira.tests.model;

import junit.framework.TestCase;

import org.joda.time.DateTime;

import com.atlassian.connector.eclipse.internal.jira.core.model.Component;
import com.atlassian.connector.eclipse.internal.jira.core.model.IssueType;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus;
import com.atlassian.connector.eclipse.internal.jira.core.model.Priority;
import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.model.Resolution;
import com.atlassian.connector.eclipse.internal.jira.core.model.Version;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ComponentFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ContentFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.CurrentUserFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.DateRangeFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.EstimateVsActualFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.FilterDataExtractor;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.IssueTypeFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.NobodyFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.PriorityFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ProjectFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ResolutionFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.SpecificUserFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.StatusFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.VersionFilter;

public abstract class FilterDataExtractorTest extends TestCase {

	protected FilterDefinition filterDefinition;

	protected FilterDefinition filterDefinition2;

	@Override
	public void setUp() {
//		String repositoryUrl = "http://host.net/";
//		taskRepository = new TaskRepository(JiraCorePlugin.CONNECTOR_KIND, repositoryUrl);
//		taskRepository.setCharacterEncoding(CHARACTER_ENC_ASCII);

		filterDefinition = createFilter1();
		filterDefinition2 = createFilter2();
	}

	/**
	 * Filter with some set of values.
	 * 
	 * @return {@link FilterDefinition}
	 */
	private FilterDefinition createFilter1() {
		final FilterDefinition filterDefinition = new FilterDefinition();

		final Component[] comps = new Component[3];
		comps[0] = new Component("comp0id");
		comps[1] = new Component("comp1id");
		comps[2] = new Component("comp2id");

		final Component[] components = new Component[2];
		components[0] = new Component();
		components[0].setId("comp0id");
		components[1] = new Component();
		components[1].setId("comp1id");

		final IssueType[] issueTypes = new IssueType[2];
		issueTypes[0] = new IssueType("issue0id", "issue0name", "issue0descr", "issue0icon");
		issueTypes[1] = new IssueType("issue1id", "issue1name", "issue1descr", "issue1icon");

		final JiraStatus[] statuses = new JiraStatus[2];
		statuses[0] = new JiraStatus("status0id");
		statuses[1] = new JiraStatus("status1id");

		final Version[] vers = new Version[3];
		vers[0] = new Version("ver0id", "ver0name");
		vers[1] = new Version("ver1id", "ver1name");
		vers[2] = new Version("ver2id", "ver2name");

		final Project[] projects = new Project[2];
		projects[0] = new Project();
		projects[0].setId("prj0");
		projects[0].setComponents(comps);
		projects[0].setVersions(vers);
		projects[0].setDetails(true);
		projects[0].setKey("prj0KEY");
		projects[1] = new Project();
		projects[1].setId("prj1");
		projects[1].setComponents(comps);
		projects[1].setVersions(vers);
		projects[1].setDetails(true);
		projects[1].setKey("prj1KEY");

		final Priority[] priorities = new Priority[5];
		priorities[0] = new Priority("1", "Blocker", null, null, null);
		priorities[1] = new Priority("2", "Critical", null, null, null);
		priorities[2] = new Priority("3", "Major", null, null, null);
		priorities[3] = new Priority("4", "Minor", null, null, null);
		priorities[4] = new Priority("5", "Trivial", null, null, null);

		final Resolution[] resolutions = new Resolution[2];
		resolutions[0] = new Resolution("res0id", "res0 name", "res0descr", "res0icon");
		resolutions[1] = new Resolution("res1id", "res1 name", "res1descr", "res1icon");

		final Version[] fixVersions = new Version[2];
		fixVersions[0] = new Version("ver0id", "ver0name");
		fixVersions[1] = new Version("ver1id", "ver1name");

		final Version[] repoVersions = new Version[2];
		repoVersions[0] = new Version("ver1id", "ver1name");
		repoVersions[1] = new Version("ver2id", "ver2name");

		filterDefinition.setProjectFilter(new ProjectFilter(projects));
		filterDefinition.setComponentFilter(new ComponentFilter(components, false));
		filterDefinition.setFixForVersionFilter(new VersionFilter(fixVersions, false, true, false));
		filterDefinition.setReportedInVersionFilter(new VersionFilter(repoVersions, false, false, true));
		filterDefinition.setIssueTypeFilter(new IssueTypeFilter(issueTypes));
		filterDefinition.setStatusFilter(new StatusFilter(statuses));
		filterDefinition.setResolutionFilter(new ResolutionFilter(resolutions));
		filterDefinition.setContentFilter(new ContentFilter("search me text", true, true, true, true));
		filterDefinition.setReportedByFilter(new SpecificUserFilter("reporterlogin"));
		filterDefinition.setAssignedToFilter(new SpecificUserFilter("assigneelogin"));
		filterDefinition.setCreatedDateFilter(new DateRangeFilter(new DateTime(2012, 01, 01, 0, 0, 0, 0).toDate(),
				new DateTime(2012, 12, 31, 0, 0, 0, 0).toDate()));
		filterDefinition.setUpdatedDateFilter(new DateRangeFilter(new DateTime(1998, 02, 01, 0, 0, 0, 0).toDate(),
				new DateTime(1998, 02, 28, 0, 0, 0, 0).toDate()));
		filterDefinition.setDueDateFilter(new DateRangeFilter(new DateTime(1900, 01, 01, 0, 0, 0, 0).toDate(),
				new DateTime(2999, 12, 31, 23, 59, 59, 999).toDate()));
		filterDefinition.setPriorityFilter(new PriorityFilter(priorities));
		filterDefinition.setEstimateVsActualFilter(new EstimateVsActualFilter(10, 90));

		return filterDefinition;
	}

	/**
	 * Filter with most of values set to empty, undefined, unassigned etc.
	 * 
	 * @return {@link FilterDefinition}
	 */
	private FilterDefinition createFilter2() {
		final FilterDefinition filterDefinition = new FilterDefinition();

		final Project[] projects = new Project[1];
		projects[0] = new Project();
		projects[0].setId("prj0id");
		projects[0].setName("prj0name");

		JiraStatus[] statuses = new JiraStatus[1];
		statuses[0] = new JiraStatus("status0id", "status0name", "status0descr", "status0icon");

		filterDefinition.setProjectFilter(new ProjectFilter(projects));
		filterDefinition.setComponentFilter(new ComponentFilter(new Component[0], true));
		filterDefinition.setFixForVersionFilter(new VersionFilter(null, true, false, false));
		filterDefinition.setReportedInVersionFilter(new VersionFilter(null, true, false, false));
		filterDefinition.setIssueTypeFilter(new IssueTypeFilter(true, true));
		filterDefinition.setStatusFilter(new StatusFilter(statuses));
		filterDefinition.setResolutionFilter(new ResolutionFilter(new Resolution[0]));
		//filterDefinition.setContentFilter(new ContentFilter("search me", true, true, true, true));
		filterDefinition.setReportedByFilter(new NobodyFilter());
		filterDefinition.setAssignedToFilter(new CurrentUserFilter());
		//filterDefinition.setCreatedDateFilter(new DateRangeFilter(new Date(2012, 01, 01), new Date(2012, 12, 31)));
		//filterDefinition.setUpdatedDateFilter(new DateRangeFilter(new Date(1998, 02, 01), new Date(1998, 02, 28)));
		//filterDefinition.setDueDateFilter(new DateRangeFilter(new Date(0), new Date(Long.MAX_VALUE)));

		return filterDefinition;
	}

	/**
	 * @see FilterDataExtractor#extractAssignedTo(com.atlassian.connector.eclipse.internal.jira.core.model.filter.UserFilter)
	 */
	public abstract void testExtractAssignedTo();

	/**
	 * @see FilterDataExtractor#extractComponents(ComponentFilter)
	 */
	public abstract void testExtractComponents();

	/**
	 * @see FilterDataExtractor#extractDates(com.atlassian.connector.eclipse.internal.jira.core.model.filter.DateFilter,
	 *      java.text.DateFormat)
	 */
	public abstract void testExtractDates();

	/**
	 * @see FilterDataExtractor#extractIssueTypes(IssueTypeFilter)
	 */
	public abstract void testExtractIssueTypes();

	/**
	 * @see FilterDataExtractor#extractPriorities(com.atlassian.connector.eclipse.internal.jira.core.model.filter.PriorityFilter)
	 */
	public abstract void testExtractPriorities();

	/**
	 * @see FilterDataExtractor#extractProjects(ProjectFilter)
	 */
	public abstract void testExtractProjects();

	/**
	 * @see FilterDataExtractor#extractReportedBy(com.atlassian.connector.eclipse.internal.jira.core.model.filter.UserFilter)
	 */
	public abstract void testExtractReportedBy();

	/**
	 * @see FilterDataExtractor#extractResolutions(ResolutionFilter)
	 */
	public abstract void testExtractResolutions();

	/**
	 * @see FilterDataExtractor#extractStatuses(StatusFilter)
	 */
	public abstract void testExtractStatuses();

	/**
	 * @see FilterDataExtractor#extractVersions(VersionFilter)
	 */
	public abstract void testExtractVersions();

	/**
	 * @see FilterDataExtractor#extractWorkRatios(EstimateVsActualFilter)
	 */
	public abstract void testExtractWorkRatios();
}
