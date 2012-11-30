/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.core.model.filter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Class which allows to extract certain data from Filters which can be used to build a JQL query.
 */
public class JQLFilterDataExtractor extends FilterDataExtractor {

	/*
	 * (non-Javadoc)
	 * @see FilterDataExtractor#extractStatusIds()
	 */
	@Override
	public Collection<String> extractAssignedTo(final UserFilter userFilter) {
		return extractUsers(userFilter, JiraFieldSpecialValue.UNASSIGNED.getJql());
	}

	/**
	 * Extracts and returns list of component IDs (integers) from given component filter, including special value
	 * "No component".
	 * 
	 * @param componentFilter
	 * @return List&lt;String&gt; with component IDs
	 * @see FilterDataExtractor#extractComponents(ComponentFilter)
	 */
	@Override
	public Collection<String> extractComponents(final ComponentFilter componentFilter) {
		final List<String> componentIds = new ArrayList<String>();

		if (componentFilter != null) {
			if (componentFilter.hasNoComponent()) {
				componentIds.add(JiraFieldSpecialValue.COMPONENT_NONE.getJql());
			}
			if (componentFilter.getComponents() != null) {
				componentIds.addAll(getComponentIds(Arrays.asList(componentFilter.getComponents())));

			}
		}

		return componentIds;
	}

	@Override
	public Collection<String> extractDates(DateFilter dateFilter, DateFormat dateFormat) {
		final List<String> dates = new ArrayList<String>();

		if (dateFilter instanceof DateRangeFilter) {
			DateRangeFilter rangeFilter = (DateRangeFilter) dateFilter;
			if (rangeFilter.getFromDate() != null) {
				dates.add(">= " + putInDoubleQuotes(dateFormat.format(rangeFilter.getFromDate()))); //$NON-NLS-1$
			}
			if (rangeFilter.getToDate() != null) {
				dates.add("<= " + putInDoubleQuotes(dateFormat.format(rangeFilter.getToDate()))); //$NON-NLS-1$
			}
			if (rangeFilter.getFrom() != null && rangeFilter.getFrom().length() > 0) {
				dates.add(">= " + putInDoubleQuotes(rangeFilter.getFrom())); //$NON-NLS-1$
			}
			if (rangeFilter.getTo() != null && rangeFilter.getTo().length() > 0) {
				dates.add("<= " + putInDoubleQuotes(rangeFilter.getTo())); //$NON-NLS-1$
			}
		} else if (dateFilter instanceof RelativeDateRangeFilter) {
			RelativeDateRangeFilter rangeFilter = (RelativeDateRangeFilter) dateFilter;
			if (rangeFilter.previousMilliseconds() != 0L) {
				dates.add(">= " //$NON-NLS-1$
						+ putInDoubleQuotes(createRelativeDateString(rangeFilter.getPreviousRangeType(),
								rangeFilter.getPreviousCount())));
			}
			if (rangeFilter.nextMilliseconds() != 0L) {
				dates.add("<= " + putInDoubleQuotes(createRelativeDateString(rangeFilter.getNextRangeType(), rangeFilter.getNextCount()))); //$NON-NLS-1$
			}
		}

		return dates;
	}

	/**
	 * Extracts and returns a list of issue type names (like Bug, Improvement), including special cases for
	 * "all standard issue types" and "sub-task issue types".
	 * 
	 * @param issueTypeFilter
	 * @return List&lt;String&gt; with issue type names
	 * @see FilterDataExtractor#extractIssueTypes(IssueTypeFilter)
	 */
	@Override
	public Collection<String> extractIssueTypes(final IssueTypeFilter issueTypeFilter) {
		final List<String> issueTypeNames = new ArrayList<String>();

		if (issueTypeFilter != null) {
			if (issueTypeFilter.isStandardTypes()) {
				issueTypeNames.add(JiraFieldSpecialValue.ISSUE_TYPE_STANDARD.getJql());
			}
			if (issueTypeFilter.isSubTaskTypes()) {
				issueTypeNames.add(JiraFieldSpecialValue.ISSUE_TYPE_SUBTASK.getJql());
			}
			if (issueTypeFilter.getIsueTypes() != null) {
				issueTypeNames.addAll(getIssueTypeNames(Arrays.asList(issueTypeFilter.getIsueTypes())));
			}
		}

		return issueTypeNames;
	}

	/**
	 * Extracts list of project keys from given project filter.
	 * 
	 * @param projectFilter
	 * @return List&lt;String&gt; list of project keys (like "CLOV", "JIRA")
	 * @see FilterDataExtractor#extractProjects(ProjectFilter)
	 */
	@Override
	public Collection<String> extractProjects(final ProjectFilter projectFilter) {
		if (projectFilter != null && projectFilter.getProjects() != null) {
			return getProjectKeys(Arrays.asList(projectFilter.getProjects()));
		} else {
			return Collections.emptyList();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see FilterDataExtractor#extractStatusIds()
	 */
	@Override
	public Collection<String> extractReportedBy(final UserFilter userFilter) {
		return extractUsers(userFilter, JiraFieldSpecialValue.ISSUE_NO_REPORTER.getJql());

	}

	/*
	 * (non-Javadoc)
	 * @see FilterDataExtractor#extractResolutionIds()
	 */
	@Override
	public Collection<String> extractResolutions(final ResolutionFilter resolutionFilter) {
		final List<String> resolutionIds = new ArrayList<String>();

		if (resolutionFilter != null) {
			if (resolutionFilter.isUnresolved()) {
				resolutionIds.add(JiraFieldSpecialValue.UNRESOLVED.getJql());
			}
			if (resolutionFilter.getResolutions() != null) {
				resolutionIds.addAll(getResolutionNames(Arrays.asList(resolutionFilter.getResolutions())));
			}
		}

		return resolutionIds;
	}

	/**
	 * Extracts list of version names (like "3.1.7") from given version filter, including special values like
	 * "No version", "Released versions", "Unreleased versions".
	 * 
	 * @param filter
	 * @return List&lt;String&gt; version names
	 * @see FilterDataExtractor#extractVersions(VersionFilter)
	 */
	@Override
	public Collection<String> extractVersions(final VersionFilter filter) {
		final List<String> fixVersionNames = new ArrayList<String>();

		if (filter != null) {
			if (filter.hasNoVersion()) {
				fixVersionNames.add(JiraFieldSpecialValue.VERSION_NONE.getJql());
			}
			if (filter.isReleasedVersions()) {
				fixVersionNames.add(JiraFieldSpecialValue.VERSION_RELEASED.getJql());
			}
			if (filter.isUnreleasedVersions()) {
				fixVersionNames.add(JiraFieldSpecialValue.VERSION_UNRELEASED.getJql());
			}

			if (filter.getVersions() != null) {
				fixVersionNames.addAll(getVersionNames(Arrays.asList(filter.getVersions())));
			}
		}

		return fixVersionNames;
	}

	/**
	 * Helper method for dealing with user filter (Assignee, Reporter fields)
	 * 
	 * @param userFilter
	 * @param emptyUserName
	 * @return
	 */
	private Collection<String> extractUsers(final UserFilter userFilter, final String emptyUserName) {
		final List<String> usersOrGroups = new ArrayList<String>();
		if (userFilter != null) {
			if (userFilter instanceof NobodyFilter) {
				usersOrGroups.add(emptyUserName);
			} else if (userFilter instanceof CurrentUserFilter) {
				usersOrGroups.add(JiraFieldSpecialValue.ISSUE_CURRENT_USER.getJql());
			} else if (userFilter instanceof SpecificUserFilter) {
				usersOrGroups.add(((SpecificUserFilter) userFilter).getUser());
			} else if (userFilter instanceof UserInGroupFilter) {
				usersOrGroups.add(JiraFieldSpecialValue.ISSUE_SPECIFIC_GROUP.getJql() + "(" //$NON-NLS-1$
						+ putInDoubleQuotes(((UserInGroupFilter) userFilter).getGroup()) + ")"); //$NON-NLS-1$
			}
		}
		return usersOrGroups;
	}

}
