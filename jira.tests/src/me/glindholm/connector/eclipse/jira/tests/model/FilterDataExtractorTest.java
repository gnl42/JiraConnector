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

package me.glindholm.connector.eclipse.jira.tests.model;


import java.util.Date;

import org.junit.jupiter.api.BeforeEach;

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraComponent;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueType;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraPriority;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProject;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraResolution;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraStatus;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraVersion;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ComponentFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ContentFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.CurrentUserFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.DateRangeFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.EstimateVsActualFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.FilterDataExtractor;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.IssueTypeFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.NobodyFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.PriorityFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ProjectFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ResolutionFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.SpecificUserFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.StatusFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.VersionFilter;

public abstract class FilterDataExtractorTest  {

	protected FilterDefinition filterDefinition;

	protected FilterDefinition filterDefinition2;

	@BeforeEach
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
		final var filterDefinition = new FilterDefinition();

		final var comps = new JiraComponent[3];
		comps[0] = new JiraComponent("comp0id");
		comps[1] = new JiraComponent("comp1id");
		comps[2] = new JiraComponent("comp2id");

		final var components = new JiraComponent[2];
		components[0] = new JiraComponent();
		components[0].setId("comp0id");
		components[1] = new JiraComponent();
		components[1].setId("comp1id");

		final var issueTypes = new JiraIssueType[2];
		issueTypes[0] = new JiraIssueType("issue0id", "issue0name", "issue0descr", "issue0icon");
		issueTypes[1] = new JiraIssueType("issue1id", "issue1name", "issue1descr", "issue1icon");

		final var statuses = new JiraStatus[2];
		statuses[0] = new JiraStatus("status0id");
		statuses[1] = new JiraStatus("status1id");

		final var vers = new JiraVersion[3];
		vers[0] = new JiraVersion("ver0id", "ver0name");
		vers[1] = new JiraVersion("ver1id", "ver1name");
		vers[2] = new JiraVersion("ver2id", "ver2name");

		final var projects = new JiraProject[2];
		projects[0] = new JiraProject();
		projects[0].setId("prj0");
		projects[0].setComponents(comps);
		projects[0].setVersions(vers);
		projects[0].setDetails(true);
		projects[0].setKey("prj0KEY");
		projects[1] = new JiraProject();
		projects[1].setId("prj1");
		projects[1].setComponents(comps);
		projects[1].setVersions(vers);
		projects[1].setDetails(true);
		projects[1].setKey("prj1KEY");

		final var priorities = new JiraPriority[5];
		priorities[0] = new JiraPriority("1", "Blocker", null, null, null);
		priorities[1] = new JiraPriority("2", "Critical", null, null, null);
		priorities[2] = new JiraPriority("3", "Major", null, null, null);
		priorities[3] = new JiraPriority("4", "Minor", null, null, null);
		priorities[4] = new JiraPriority("5", "Trivial", null, null, null);

		final var resolutions = new JiraResolution[2];
		resolutions[0] = new JiraResolution("res0id", "res0 name", "res0descr", "res0icon");
		resolutions[1] = new JiraResolution("res1id", "res1 name", "res1descr", "res1icon");

		final var fixVersions = new JiraVersion[2];
		fixVersions[0] = new JiraVersion("ver0id", "ver0name");
		fixVersions[1] = new JiraVersion("ver1id", "ver1name");

		final var repoVersions = new JiraVersion[2];
		repoVersions[0] = new JiraVersion("ver1id", "ver1name");
		repoVersions[1] = new JiraVersion("ver2id", "ver2name");

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
		filterDefinition.setCreatedDateFilter(new DateRangeFilter(new Date(2012, 01, 01).toInstant(),
				new Date(2012, 12, 31).toInstant()));
		filterDefinition.setUpdatedDateFilter(new DateRangeFilter(new Date(1998, 02, 01).toInstant(),
				new Date(1998, 02, 28).toInstant()));
		filterDefinition.setDueDateFilter(new DateRangeFilter(new Date(1900, 01, 01).toInstant(),
				new Date(2999, 12, 31, 23, 59, 59).toInstant()));
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
		final var filterDefinition = new FilterDefinition();

		final var projects = new JiraProject[1];
		projects[0] = new JiraProject();
		projects[0].setId("prj0id");
		projects[0].setName("prj0name");

		final var statuses = new JiraStatus[1];
		statuses[0] = new JiraStatus("status0id", "status0name", "status0descr", "status0icon");

		filterDefinition.setProjectFilter(new ProjectFilter(projects));
		filterDefinition.setComponentFilter(new ComponentFilter(new JiraComponent[0], true));
		filterDefinition.setFixForVersionFilter(new VersionFilter(null, true, false, false));
		filterDefinition.setReportedInVersionFilter(new VersionFilter(null, true, false, false));
		filterDefinition.setIssueTypeFilter(new IssueTypeFilter(true, true));
		filterDefinition.setStatusFilter(new StatusFilter(statuses));
		filterDefinition.setResolutionFilter(new ResolutionFilter(new JiraResolution[0]));
		//filterDefinition.setContentFilter(new ContentFilter("search me", true, true, true, true));
		filterDefinition.setReportedByFilter(new NobodyFilter());
		filterDefinition.setAssignedToFilter(new CurrentUserFilter());
		//filterDefinition.setCreatedDateFilter(new DateRangeFilter(new Date(2012, 01, 01), new Date(2012, 12, 31)));
		//filterDefinition.setUpdatedDateFilter(new DateRangeFilter(new Date(1998, 02, 01), new Date(1998, 02, 28)));
		//filterDefinition.setDueDateFilter(new DateRangeFilter(new Date(0), new Date(Long.MAX_VALUE)));

		return filterDefinition;
	}

	/**
	 * @see FilterDataExtractor#extractAssignedTo(me.glindholm.connector.eclipse.internal.jira.core.model.filter.UserFilter)
	 */
	public abstract void testExtractAssignedTo();

	/**
	 * @see FilterDataExtractor#extractJiraComponents(JiraComponentFilter)
	 */
	public abstract void testExtractComponents();

	/**
	 * @see FilterDataExtractor#extractDates(me.glindholm.connector.eclipse.internal.jira.core.model.filter.DateFilter,
	 *      java.text.DateFormat)
	 */
	public abstract void testExtractDates();

	/**
	 * @see FilterDataExtractor#extractIssueTypes(IssueTypeFilter)
	 */
	public abstract void testExtractIssueTypes();

	/**
	 * @see FilterDataExtractor#extractPriorities(me.glindholm.connector.eclipse.internal.jira.core.model.filter.PriorityFilter)
	 */
	public abstract void testExtractPriorities();

	/**
	 * @see FilterDataExtractor#extractProjects(ProjectFilter)
	 */
	public abstract void testExtractProjects();

	/**
	 * @see FilterDataExtractor#extractReportedBy(me.glindholm.connector.eclipse.internal.jira.core.model.filter.UserFilter)
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
