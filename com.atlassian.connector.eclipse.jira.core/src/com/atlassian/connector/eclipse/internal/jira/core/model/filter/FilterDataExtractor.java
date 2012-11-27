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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.connector.eclipse.internal.jira.core.model.Component;
import com.atlassian.connector.eclipse.internal.jira.core.model.IssueType;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus;
import com.atlassian.connector.eclipse.internal.jira.core.model.Priority;
import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.model.Resolution;
import com.atlassian.connector.eclipse.internal.jira.core.model.Version;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.RelativeDateRangeFilter.RangeType;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * <p>
 * Class which allows to extract certain data from Filters so that the returned data can be used to build a query. See
 * implementations:
 * <ul>
 * <li>{@link ClassicFilterDataExtractor} for classic GET (?key1=value1&key2=value2 ... )</li>
 * <li>{@link JQLFilterDataExtractor} for JQL queries (key1 = value1 AND key2 = value2 ...)</li>
 * </ul>
 * </p>
 * <p>
 * This abstract class provides set of helper getXxxxYyy() methods which extracts certain Yyy attributes from Xxxx
 * fields. The set of extractXxxx() methods shall return a list of values which can be used in a query (usually integer
 * IDs, keys, names, special values), depending on implementation.
 * </p>
 */
public abstract class FilterDataExtractor {

	/**
	 * Extracts list of assignees from UserFilter, including special cases like 'unassigned', 'current user', 'specific
	 * user', 'specific group'.
	 * 
	 * @param userFilter
	 * @return Collection&lt;String&gt; exact content depends on implementation
	 */
	public abstract Collection<String> extractAssignedTo(UserFilter userFilter);

	/**
	 * Extracts and returns list of component identifiers from given component filter, including special value for
	 * "No component".
	 * 
	 * @param componentFilter
	 * @return Collection&lt;String&gt; exact content depends on implementation
	 */
	public abstract Collection<String> extractComponents(ComponentFilter componentFilter);

	public abstract Collection<String> extractDates(DateFilter dateFilter, DateFormat dateFormat);

	/**
	 * Extracts and returns a list of issue types identifiers, including special cases for "all standard issue types"
	 * and "sub-task issue types".
	 * 
	 * @param issueTypeFilter
	 * @return Collection&lt;String&gt; exact content depends on implementation
	 */
	public abstract Collection<String> extractIssueTypes(IssueTypeFilter issueTypeFilter);

	/**
	 * Extracts list of priority IDs (integers) from given project filter.
	 * 
	 * @param priorityFilter
	 * @return Collection&lt;String&gt; list of priority IDs
	 */
	public Collection<String> extractPriorities(final PriorityFilter priorityFilter) {
		if ((priorityFilter != null) && (priorityFilter.getPriorities() != null)) {
			return getPriorityIds(Arrays.asList(priorityFilter.getPriorities()));
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * Extracts list of project identifiers from given project filter.
	 * 
	 * @param projectFilter
	 * @return Collection&lt;String&gt; exact content depends on implementation
	 */
	public abstract Collection<String> extractProjects(ProjectFilter projectFilter);

	/**
	 * Extracts list of assignees from user filter, including special cases like 'no reporter', 'current user',
	 * 'specific user', 'specific group'.
	 * 
	 * @param userFilter
	 * @return Collection&lt;String&gt; exact content depends on implementation
	 */
	public abstract Collection<String> extractReportedBy(UserFilter userFilter);

	/**
	 * Extracts list of resolutions from given resolution filter, including special value for "Unresolved".
	 * 
	 * @param projectFilter
	 * @return Collection&lt;String&gt; exact content depends on implementation
	 */
	public abstract Collection<String> extractResolutions(ResolutionFilter resolutionFilter);

	/**
	 * Extracts list of status IDs (integer) from given status filter
	 * 
	 * @param statusFilter
	 * @return List&lt;String&gt; with status IDs
	 */
	public Collection<String> extractStatuses(final StatusFilter statusFilter) {
		if ((statusFilter != null) && (statusFilter.getStatuses() != null)) {
			return getStatusIds(Arrays.asList(statusFilter.getStatuses()));
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * Extracts list of versions from given version filter, including special values like "No version",
	 * "Released versions", "Unreleased versions".
	 * 
	 * @param filter
	 * @return Collection&lt;String&gt; exact content depends on implementation
	 */
	public abstract Collection<String> extractVersions(VersionFilter filter);

	/**
	 * Converts <code>Collection&lt;Component&gt;</code> into <code>Collection&lt;Component.getId()&gt;</code>
	 * 
	 * @param components
	 * @return
	 */
	protected Collection<String> getComponentIds(Collection<Component> components) {
		return Collections2.transform(components, new Function<Component, String>() {
			public String apply(Component from) {
				return from.getId();
			}
		});
	}

	/**
	 * Converts <code>Collection&lt;IssueType&gt;</code> into <code>Collection&lt;IssueType.getId()&gt;</code>
	 * 
	 * @param issueTypes
	 * @return
	 */
	protected Collection<String> getIssueTypeIds(Collection<IssueType> issueTypes) {
		return Collections2.transform(issueTypes, new Function<IssueType, String>() {
			public String apply(IssueType from) {
				return from.getId();
			}
		});
	}

	/**
	 * Converts <code>Collection&lt;IssueType&gt;</code> into <code>Collection&lt;IssueType.getName()&gt;</code>
	 * 
	 * @param issueTypes
	 * @return
	 */
	protected Collection<String> getIssueTypeNames(Collection<IssueType> issueTypes) {
		return Collections2.transform(issueTypes, new Function<IssueType, String>() {
			public String apply(IssueType from) {
				return putInDoubleQuotes(from.getName());
			}
		});
	}

	/**
	 * Converts <code>Collection&lt;Priority&gt;</code> into <code>Collection&lt;Priority.getId()&gt;</code>
	 * 
	 * @param priorities
	 * @return
	 */
	protected Collection<String> getPriorityIds(Collection<Priority> priorities) {
		return Collections2.transform(priorities, new Function<Priority, String>() {
			public String apply(Priority from) {
				return from.getId();
			}
		});
	}

	/**
	 * Converts <code>Collection&lt;Resolution&gt;</code> into <code>Collection&lt;Resolution.getId()&gt;</code>
	 * 
	 * @param resolutions
	 * @return
	 */
	protected Collection<String> getResolutionIds(Collection<Resolution> resolutions) {
		return Collections2.transform(resolutions, new Function<Resolution, String>() {
			public String apply(Resolution from) {
				return from.getId();
			}
		});
	}

	/**
	 * Converts <code>Collection&lt;Resolution&gt;</code> into <code>Collection&lt;Resolution.getName()&gt;</code>
	 * 
	 * @param resolutions
	 * @return
	 */
	protected Collection<? extends String> getResolutionNames(List<Resolution> resolutions) {
		return Collections2.transform(resolutions, new Function<Resolution, String>() {
			public String apply(Resolution from) {
				return putInDoubleQuotes(from.getName());
			}
		});
	}

	/**
	 * Converts <code>Collection&lt;JiraStatus&gt;</code> into <code>Collection&lt;JiraStatus.getId()&gt;</code>
	 * 
	 * @param jiraStatuses
	 * @return
	 */
	protected Collection<String> getStatusIds(Collection<JiraStatus> jiraStatuses) {
		return Collections2.transform(jiraStatuses, new Function<JiraStatus, String>() {
			public String apply(JiraStatus from) {
				return from.getId();
			}
		});
	}

	/**
	 * Converts <code>Collection&lt;Project&gt;</code> into <code>Collection&lt;Project.getId()&gt;</code>
	 * 
	 * @param projects
	 * @return
	 */
	protected Collection<String> getProjectIds(List<Project> projects) {
		return Collections2.transform(projects, new Function<Project, String>() {
			public String apply(Project from) {
				return from.getId();
			}
		});
	}

	/**
	 * Converts <code>Collection&lt;Project&gt;</code> into <code>Collection&lt;Project.getKey()&gt;</code>
	 * 
	 * @param projects
	 * @return
	 */
	protected Collection<String> getProjectKeys(Collection<Project> projects) {
		return Collections2.transform(projects, new Function<Project, String>() {
			public String apply(Project from) {
				return from.getKey();
			}
		});
	}

	/**
	 * Converts <code>Collection&lt;Version&gt;</code> into <code>Collection&lt;Version.getId()&gt;</code>
	 * 
	 * @param versions
	 * @return list of version integer identifiers
	 */
	protected Collection<String> getVersionIds(Collection<Version> versions) {
		return Collections2.transform(versions, new Function<Version, String>() {
			public String apply(Version from) {
				return from.getId();
			}
		});
	}

	/**
	 * Converts <code>Collection&lt;Version&gt;</code> into <code>Collection&lt;Version.getName()&gt;</code>
	 * 
	 * @param versions
	 * @return list of version names in double quotes
	 */
	protected Collection<String> getVersionNames(Collection<Version> versions) {
		return Collections2.transform(versions, new Function<Version, String>() {
			public String apply(Version from) {
				return putInDoubleQuotes(from.getName());
			}
		});
	}

	/**
	 * Puts input string into double quotes; double quotes present in input string are back-slashed.
	 * 
	 * @param input
	 * @return
	 */
	public static String putInDoubleQuotes(String input) {
		// [abc"def ghi] -> ["abc\"def ghi"]
		return '"' + input.replace("\"", "\\\"") + '"'; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static String createRelativeDateString(RelativeDateRangeFilter.RangeType rangeType, long count) {
		StringBuilder sb = new StringBuilder();
		sb.append(Long.toString(count));
		if (RangeType.MINUTE.equals(rangeType)) {
			sb.append('m');
		} else if (RangeType.HOUR.equals(rangeType)) {
			sb.append('h');
		} else if (RangeType.DAY.equals(rangeType)) {
			sb.append('d');
		} else if (RangeType.WEEK.equals(rangeType)) {
			sb.append('w');
		}
		return sb.toString();
	}
}
