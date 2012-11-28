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
 * Class which allows to extract certain data from Filters which can be used to build a classic query.
 */
public class ClassicFilterDataExtractor extends FilterDataExtractor {

	@Override
	public Collection<String> extractAssignedTo(final UserFilter userFilter) {
		throw new UnsupportedOperationException("Not implemented"); //$NON-NLS-1$
	}

	/**
	 * Extracts and returns list of component IDs (integers) from given component filter, including special value for
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
				componentIds.add(JiraFieldSpecialValue.COMPONENT_NONE.getClassic());
			}
			if (componentFilter.getComponents() != null) {
				componentIds.addAll(getComponentIds(Arrays.asList(componentFilter.getComponents())));

			}
		}

		return componentIds;
	}

	@Override
	public Collection<String> extractDates(DateFilter dateFilter, DateFormat dateFormat) {
		// ignore
		return null;
	}

	/**
	 * Extracts and returns a list of issue type IDs (integers), including special cases for "all standard issue types"
	 * and "sub-task issue types".
	 * 
	 * @param issueTypeFilter
	 * @return List&lt;String&gt; with issue type IDs
	 * @see FilterDataExtractor#extractIssueTypes(IssueTypeFilter)
	 */
	@Override
	public Collection<String> extractIssueTypes(final IssueTypeFilter issueTypeFilter) {
		final List<String> issueTypeNames = new ArrayList<String>();

		if (issueTypeFilter != null) {
			//TODO not supported in classic query ?
			//if (issueTypeFilter.isStandardTypes()) {
			//	issueTypeNames.add(JiraFieldSpecialValue.ISSUE_TYPE_STANDARD.getClassic());
			//}
			//if (issueTypeFilter.isSubTaskTypes()) {
			//	issueTypeNames.add(JiraFieldSpecialValue.ISSUE_TYPE_SUBTASK.getClassic());
			//}
			if (issueTypeFilter.getIsueTypes() != null) {
				issueTypeNames.addAll(getIssueTypeIds(Arrays.asList(issueTypeFilter.getIsueTypes())));
			}
		}

		return issueTypeNames;
	}

	/**
	 * Extracts list of project IDs (integers) from given project filter.
	 * 
	 * @param projectFilter
	 * @return List&lt;String&gt; list of project IDs (like 123, 777)
	 * @see FilterDataExtractor#extractProjects(ProjectFilter)
	 */
	@Override
	public Collection<String> extractProjects(final ProjectFilter projectFilter) {
		if (projectFilter != null && projectFilter.getProjects() != null) {
			return getProjectIds(Arrays.asList(projectFilter.getProjects()));
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public Collection<String> extractReportedBy(final UserFilter userFilter) {
		throw new UnsupportedOperationException("Not implemented"); //$NON-NLS-1$
	}

	@Override
	public Collection<String> extractResolutions(final ResolutionFilter resolutionFilter) {
		final List<String> resolutionIds = new ArrayList<String>();

		if (resolutionFilter != null) {
			if (resolutionFilter.isUnresolved()) {
				resolutionIds.add(JiraFieldSpecialValue.UNRESOLVED.getClassic());
			}
			if (resolutionFilter.getResolutions() != null) {
				resolutionIds.addAll(getResolutionIds(Arrays.asList(resolutionFilter.getResolutions())));
			}
		}

		return resolutionIds;
	}

	/**
	 * Extracts list of version IDs (integers) from given version filter, including special values like "No version",
	 * "Released versions", "Unreleased versions".
	 * 
	 * @param filter
	 * @return List&lt;String&gt; version IDs (e.g. -1, 33, 88)
	 * @see FilterDataExtractor#extractVersions(VersionFilter)
	 */
	@Override
	public Collection<String> extractVersions(final VersionFilter filter) {
		final List<String> fixVersionIds = new ArrayList<String>();

		if (filter != null) {
			if (filter.hasNoVersion()) {
				fixVersionIds.add(JiraFieldSpecialValue.VERSION_NONE.getClassic());
			}
			if (filter.isReleasedVersions()) {
				fixVersionIds.add(JiraFieldSpecialValue.VERSION_RELEASED.getClassic());
			}
			if (filter.isUnreleasedVersions()) {
				fixVersionIds.add(JiraFieldSpecialValue.VERSION_UNRELEASED.getClassic());
			}
			if (filter.getVersions() != null) {
				fixVersionIds.addAll(getVersionIds(Arrays.asList(filter.getVersions())));
			}
		}

		return fixVersionIds;
	}

}
