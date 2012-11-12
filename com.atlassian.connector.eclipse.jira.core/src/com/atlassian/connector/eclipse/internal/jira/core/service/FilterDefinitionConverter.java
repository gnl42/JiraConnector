/*******************************************************************************
 * Copyright (c) 2004, 2009 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *     Eugene Kuleshov - improvements
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.core.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

import com.atlassian.connector.eclipse.internal.jira.core.InvalidJiraQueryException;
import com.atlassian.connector.eclipse.internal.jira.core.JiraRepositoryConnector;
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
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.DateFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.DateRangeFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.EstimateVsActualFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.IssueTypeFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.NobodyFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.Order;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.PriorityFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ProjectFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.RelativeDateRangeFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.RelativeDateRangeFilter.RangeType;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ResolutionFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.SpecificUserFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.StatusFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.UserFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.UserInGroupFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.VersionFilter;

/**
 * A JiraCustomQuery represents a custom query for issues from a Jira repository.
 * 
 * @author Mik Kersten
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer (multiple projects selection)
 */
public class FilterDefinitionConverter {

	private static final String PROJECT_KEY = "pid"; //$NON-NLS-1$

	private static final String COMPONENT_KEY = "component"; //$NON-NLS-1$

	private static final String TYPE_KEY = "type"; //$NON-NLS-1$

	private static final String PRIORITY_KEY = "priority"; //$NON-NLS-1$

	private static final String STATUS_KEY = "status"; //$NON-NLS-1$

	private static final String RESOLUTION_KEY = "resolution"; //$NON-NLS-1$

	private static final String FIXFOR_KEY = "fixfor"; //$NON-NLS-1$

	private static final String VERSION_KEY = "version"; //$NON-NLS-1$

	private static final String QUERY_KEY = "query"; //$NON-NLS-1$

	private static final String ENVIRONMENT_KEY = "environment"; //$NON-NLS-1$

	private static final String BODY_KEY = "body"; //$NON-NLS-1$

	private static final String DESCRIPTION_KEY = "description"; //$NON-NLS-1$

	private static final String SUMMARY_KEY = "summary"; //$NON-NLS-1$

	private static final String ASSIGNEE_KEY = "assignee"; //$NON-NLS-1$

	private static final String REPORTER_KEY = "reporter"; //$NON-NLS-1$

	private static final String CREATED_KEY = "created"; //$NON-NLS-1$

	private static final String UPDATED_KEY = "updated"; //$NON-NLS-1$

	private static final String DUEDATE_KEY = "duedate"; //$NON-NLS-1$

	private static final String ISSUE_SPECIFIC_GROUP = "specificgroup"; //$NON-NLS-1$

	private static final String ISSUE_SPECIFIC_USER = "specificuser"; //$NON-NLS-1$

	private static final String ISSUE_CURRENT_USER = "issue_current_user"; //$NON-NLS-1$

	private static final String ISSUE_NO_REPORTER = "issue_no_reporter"; //$NON-NLS-1$

	private static final String UNASSIGNED = "unassigned"; //$NON-NLS-1$

	private static final String VERSION_NONE = "-1"; //$NON-NLS-1$

	private static final String VERSION_RELEASED = "-3"; //$NON-NLS-1$

	private static final String VERSION_UNRELEASED = "-2"; //$NON-NLS-1$

	private static final String UNRESOLVED = "-1"; //$NON-NLS-1$

	private static final String COMPONENT_NONE = "-1"; //$NON-NLS-1$

	private final String encoding;

	private final DateFormat dateFormat;

	public FilterDefinitionConverter(String encoding, DateFormat dateFormat) {
		Assert.isNotNull(dateFormat);
		Assert.isNotNull(encoding);
		this.encoding = encoding;
		this.dateFormat = dateFormat;
	}

	public String toUrl(String repositoryUrl, FilterDefinition filter) {
		return repositoryUrl + JiraRepositoryConnector.FILTER_URL_PREFIX + "&reset=true" + getQueryParams(filter); //$NON-NLS-1$
	}

	public FilterDefinition toFilter(JiraClient client, String url, boolean validate) {
		try {
			return toFilter(client, url, validate, false, null);
		} catch (JiraException e) {
			// can never happen since update parameter is false
			throw new RuntimeException(e);
		}
	}

	public FilterDefinition toFilter(JiraClient client, String url, boolean validate, boolean update,
			IProgressMonitor monitor) throws JiraException {
		FilterDefinition filter = new FilterDefinition();

		int n = url.indexOf('?');
		if (n == -1) {
			return filter;
		}

		HashMap<String, List<String>> params = new HashMap<String, List<String>>();
		for (String pair : url.substring(n + 1).split("&")) { //$NON-NLS-1$
			String[] tokens = pair.split("="); //$NON-NLS-1$
			if (tokens.length > 1) {
				try {
					String key = tokens[0];
					String value = tokens.length == 1 ? "" : URLDecoder.decode(tokens[1], encoding); //$NON-NLS-1$
					List<String> values = params.get(key);
					if (values == null) {
						values = new ArrayList<String>();
						params.put(key, values);
					}
					values.add(value);
				} catch (UnsupportedEncodingException ex) {
					// ignore
				}
			}
		}

		List<String> projectIds = getIds(params, PROJECT_KEY);
		List<Project> projects = new ArrayList<Project>();
		for (String projectId : projectIds) {
			Project project = client.getCache().getProjectById(projectId);
			if (update && (project == null || !project.hasDetails())) {
				project = client.getCache().refreshProjectDetails(projectId, monitor);
			}
			if (project == null) {
				if (validate) {
					// safeguard
					throw new InvalidJiraQueryException("Unknown project " + projectId); //$NON-NLS-1$
				} else {
					continue;
				}
			}
			projects.add(project);
		}

		if (projects.size() > 0) {
			filter.setProjectFilter(new ProjectFilter(projects.toArray(new Project[projects.size()])));

			List<String> componentIds = getIds(params, COMPONENT_KEY);
			Set<Component> components = new LinkedHashSet<Component>();
			Set<Version> versions = new LinkedHashSet<Version>();

			boolean hasNoComponent = false;

			for (Project project : projects) {
				if (!project.hasDetails()) {
					continue;
				}
				for (String componentId : componentIds) {
					Component[] projectComponents = project.getComponents();
					for (Component component : projectComponents) {
						if (component.getId().equals(componentId)) {
							components.add(component);
						}
					}

					if (componentId.equals(COMPONENT_NONE)) {
						hasNoComponent = true;
					}
				}
				versions.addAll(Arrays.asList(project.getVersions()));
			}
			if (!componentIds.isEmpty()) {
				filter.setComponentFilter(new ComponentFilter(components.toArray(new Component[components.size()]),
						hasNoComponent));
			}

			Version[] projectVersions = versions.toArray(new Version[versions.size()]);
			filter.setFixForVersionFilter(getVersionFilter(filter, getIds(params, FIXFOR_KEY), projectVersions));
			filter.setReportedInVersionFilter(getVersionFilter(filter, getIds(params, VERSION_KEY), projectVersions));
		}

		List<String> typeIds = getIds(params, TYPE_KEY);
		List<IssueType> issueTypes = new ArrayList<IssueType>();
		for (String typeId : typeIds) {
			IssueType issueType = null;
			if (projects.size() > 0) {
				for (Project project : projects) {
					issueType = project.getIssueTypeById(typeId);
					if (issueType != null) {
						break;
					}
				}
			}

			// fallback - if there are no projects or project issue types are not supported
			if (issueType == null) {
				issueType = client.getCache().getIssueTypeById(typeId);
			}

			if (issueType != null) {
				issueTypes.add(issueType);
			} else if (validate) {
				throw new InvalidJiraQueryException("Unknown type " + typeId); //$NON-NLS-1$
			}
		}
		if (!issueTypes.isEmpty()) {
			filter.setIssueTypeFilter(new IssueTypeFilter(issueTypes.toArray(new IssueType[issueTypes.size()])));
		}

		List<String> statusIds = getIds(params, STATUS_KEY);
		List<JiraStatus> statuses = new ArrayList<JiraStatus>();
		for (String statusId : statusIds) {
			JiraStatus status = client.getCache().getStatusById(statusId);
			if (status != null) {
				statuses.add(status);
			} else if (validate) {
				throw new InvalidJiraQueryException("Unknown status " + statusId); //$NON-NLS-1$
			}
		}
		if (!statuses.isEmpty()) {
			filter.setStatusFilter(new StatusFilter(statuses.toArray(new JiraStatus[statuses.size()])));
		}

		List<String> resolutionIds = getIds(params, RESOLUTION_KEY);
		List<Resolution> resolutions = new ArrayList<Resolution>();
		boolean unresolved = false;
		for (String resolutionId : resolutionIds) {
			if (!UNRESOLVED.equals(resolutionId)) {
				Resolution resolution = client.getCache().getResolutionById(resolutionId);
				if (resolution != null) {
					resolutions.add(resolution);
				} else if (validate) {
					throw new InvalidJiraQueryException("Unknown resolution " + resolutionId); //$NON-NLS-1$
				}
			} else {
				unresolved = true;
			}
		}
		if (!resolutionIds.isEmpty()) {
			filter.setResolutionFilter(new ResolutionFilter(resolutions.toArray(new Resolution[resolutions.size()])));
		} else if (unresolved) {
			filter.setResolutionFilter(new ResolutionFilter(new Resolution[0]));
		}

		List<String> priorityIds = getIds(params, PRIORITY_KEY);
		List<Priority> priorities = new ArrayList<Priority>();
		for (String priorityId : priorityIds) {
			Priority priority = client.getCache().getPriorityById(priorityId);
			if (priority != null) {
				priorities.add(priority);
			} else if (validate) {
				throw new InvalidJiraQueryException("Unknown priority " + priorityId); //$NON-NLS-1$
			}
		}
		if (!priorities.isEmpty()) {
			filter.setPriorityFilter(new PriorityFilter(priorities.toArray(new Priority[priorities.size()])));
		}

		List<String> queries = getIds(params, QUERY_KEY);
		for (String query : queries) {
			boolean searchSummary = getIds(params, SUMMARY_KEY).contains("true"); //$NON-NLS-1$
			boolean searchDescription = getIds(params, DESCRIPTION_KEY).contains("true"); //$NON-NLS-1$
			boolean searchEnvironment = getIds(params, ENVIRONMENT_KEY).contains("true"); //$NON-NLS-1$
			boolean searchComments = getIds(params, BODY_KEY).contains("true"); //$NON-NLS-1$
			filter.setContentFilter(new ContentFilter(query, searchSummary, searchDescription, searchEnvironment,
					searchComments));
		}

		filter.setReportedByFilter(createUserFilter(params, REPORTER_KEY));
		filter.setAssignedToFilter(createUserFilter(params, ASSIGNEE_KEY));

		filter.setCreatedDateFilter(createDateFilter(params, CREATED_KEY));
		filter.setUpdatedDateFilter(createDateFilter(params, UPDATED_KEY));
		filter.setDueDateFilter(createDateFilter(params, DUEDATE_KEY));

		return filter;
	}

	private VersionFilter getVersionFilter(FilterDefinition filter, List<String> fixForIds, Version[] projectVersions) {
		if (fixForIds.isEmpty()) {
			return null;
		}

		boolean hasNoVersions = false;
		boolean hasReleasedVersions = false;
		boolean hasUnreleasedVersions = false;
		List<Version> fixForversions = new ArrayList<Version>();
		for (String fixForId : fixForIds) {
			if (fixForId.equals(VERSION_NONE)) {
				hasNoVersions = true;
			}
			if (fixForId.equals(VERSION_RELEASED)) {
				hasReleasedVersions = true;
			}
			if (fixForId.equals(VERSION_UNRELEASED)) {
				hasUnreleasedVersions = true;
			}

			for (Version projectVersion : projectVersions) {
				if (projectVersion.getId().equals(fixForId)) {
					fixForversions.add(projectVersion);
				}
			}
		}

		return new VersionFilter(fixForversions.toArray(new Version[fixForversions.size()]), hasNoVersions,
				hasReleasedVersions, hasUnreleasedVersions);

	}

	private DateFilter createDateFilter(Map<String, List<String>> params, String key) {
		String after = getId(params, key + ":after"); //$NON-NLS-1$
		String before = getId(params, key + ":before"); //$NON-NLS-1$

		Date afterDate;
		try {
			afterDate = dateFormat.parse(after);
		} catch (Exception ex) {
			afterDate = null;
		}
		Date beforeDate;
		try {
			beforeDate = dateFormat.parse(before);
		} catch (Exception ex) {
			beforeDate = null;
		}

		String previous = getId(params, key + ":previous"); //$NON-NLS-1$
		String next = getId(params, key + ":next"); //$NON-NLS-1$

		return afterDate == null && beforeDate == null && previous == null && next == null ? null
				: new DateRangeFilter(afterDate, beforeDate, previous, next);
	}

	private UserFilter createUserFilter(Map<String, List<String>> params, String key) {
		String type = getId(params, key + "Select"); //$NON-NLS-1$
		if (ISSUE_NO_REPORTER.equals(type) || UNASSIGNED.equals(type)) {
			return new NobodyFilter();
		} else if (ISSUE_CURRENT_USER.equals(type)) {
			return new CurrentUserFilter();
		} else {
			String reporter = getId(params, key);
			if (reporter != null) {
				if (ISSUE_SPECIFIC_USER.equals(type)) {
					return new SpecificUserFilter(reporter);
				} else if (ISSUE_SPECIFIC_GROUP.equals(type)) {
					return new UserInGroupFilter(reporter);
				}
			}
		}
		return null;
	}

	private String getId(Map<String, List<String>> params, String key) {
		List<String> ids = getIds(params, key);
		return ids.isEmpty() ? null : ids.get(0);
	}

	private List<String> getIds(Map<String, List<String>> params, String key) {
		List<String> ids = params.get(key);
		if (ids == null) {
			return Collections.emptyList();
		}
		return ids;
	}

	public String getQueryParams(FilterDefinition filter) {
		StringBuilder sb = new StringBuilder();

		ProjectFilter projectFilter = filter.getProjectFilter();
		if (projectFilter != null) {
			for (Project project : projectFilter.getProjects()) {
				addParameter(sb, PROJECT_KEY, project.getId());
			}
		}

		ComponentFilter componentFilter = filter.getComponentFilter();
		if (componentFilter != null) {
			if (componentFilter.hasNoComponent()) {
				addParameter(sb, COMPONENT_KEY, COMPONENT_NONE);
			}
			if (componentFilter.getComponents() != null) {
				for (Component component : componentFilter.getComponents()) {
					addParameter(sb, COMPONENT_KEY, component.getId());
				}
			}
		}

		VersionFilter fixForVersionFilter = filter.getFixForVersionFilter();
		if (fixForVersionFilter != null) {
			if (fixForVersionFilter.hasNoVersion()) {
				addParameter(sb, FIXFOR_KEY, VERSION_NONE);
			}
			if (fixForVersionFilter.isReleasedVersions()) {
				addParameter(sb, FIXFOR_KEY, VERSION_RELEASED);
			}
			if (fixForVersionFilter.isUnreleasedVersions()) {
				addParameter(sb, FIXFOR_KEY, VERSION_UNRELEASED);
			}
			if (fixForVersionFilter.getVersions() != null) {
				for (Version fixVersion : fixForVersionFilter.getVersions()) {
					addParameter(sb, FIXFOR_KEY, fixVersion.getId());
				}
			}
		}

		VersionFilter reportedInVersionFilter = filter.getReportedInVersionFilter();
		if (reportedInVersionFilter != null) {
			if (reportedInVersionFilter.hasNoVersion()) {
				addParameter(sb, VERSION_KEY, VERSION_NONE);
			}
			if (reportedInVersionFilter.isReleasedVersions()) {
				addParameter(sb, VERSION_KEY, VERSION_RELEASED);
			}
			if (reportedInVersionFilter.isUnreleasedVersions()) {
				addParameter(sb, VERSION_KEY, VERSION_UNRELEASED);
			}
			if (reportedInVersionFilter.getVersions() != null) {
				for (Version reportedVersion : reportedInVersionFilter.getVersions()) {
					addParameter(sb, VERSION_KEY, reportedVersion.getId());
				}
			}
		}

		IssueTypeFilter issueTypeFilter = filter.getIssueTypeFilter();
		if (issueTypeFilter != null) {
			for (IssueType issueType : issueTypeFilter.getIsueTypes()) {
				addParameter(sb, TYPE_KEY, issueType.getId());
			}
		}

		StatusFilter statusFilter = filter.getStatusFilter();
		if (statusFilter != null) {
			for (JiraStatus status : statusFilter.getStatuses()) {
				addParameter(sb, STATUS_KEY, status.getId());
			}
		}

		ResolutionFilter resolutionFilter = filter.getResolutionFilter();
		if (resolutionFilter != null) {
			Resolution[] resolutions = resolutionFilter.getResolutions();
			if (resolutions.length == 0) {
				addParameter(sb, RESOLUTION_KEY, UNRESOLVED); // Unresolved
			} else {
				for (Resolution resolution : resolutions) {
					addParameter(sb, RESOLUTION_KEY, resolution.getId());
				}
			}
		}

		PriorityFilter priorityFilter = filter.getPriorityFilter();
		if (priorityFilter != null) {
			for (Priority priority : priorityFilter.getPriorities()) {
				addParameter(sb, PRIORITY_KEY, priority.getId());
			}
		}

		ContentFilter contentFilter = filter.getContentFilter();
		if (contentFilter != null) {
			String queryString = contentFilter.getQueryString();
			if (queryString != null) {
				addParameter(sb, QUERY_KEY, queryString);
			}
			if (contentFilter.isSearchingSummary()) {
				addParameter(sb, SUMMARY_KEY, "true"); //$NON-NLS-1$
			}
			if (contentFilter.isSearchingDescription()) {
				addParameter(sb, DESCRIPTION_KEY, "true"); //$NON-NLS-1$
			}
			if (contentFilter.isSearchingComments()) {
				addParameter(sb, BODY_KEY, "true"); //$NON-NLS-1$
			}
			if (contentFilter.isSearchingEnvironment()) {
				addParameter(sb, ENVIRONMENT_KEY, "true"); //$NON-NLS-1$
			}
		}

		addUserFilter(sb, filter.getReportedByFilter(), REPORTER_KEY, ISSUE_NO_REPORTER);
		addUserFilter(sb, filter.getAssignedToFilter(), ASSIGNEE_KEY, UNASSIGNED);

		addDateFilter(sb, filter.getCreatedDateFilter(), CREATED_KEY);
		addDateFilter(sb, filter.getUpdatedDateFilter(), UPDATED_KEY);
		addDateFilter(sb, filter.getDueDateFilter(), DUEDATE_KEY);

		addOrdering(sb, filter.getOrdering());

		EstimateVsActualFilter estimateFilter = filter.getEstimateVsActualFilter();
		if (estimateFilter != null) {
			float min = estimateFilter.getMinVariation();
			if (min != 0L) {
				addParameter(sb, "minRatioLimit", Float.toString(min)); //$NON-NLS-1$
			}
			float max = estimateFilter.getMaxVariation();
			if (max != 0L) {
				addParameter(sb, "maxRatioLimit", Float.toString(max)); //$NON-NLS-1$
			}
		}

		return sb.toString();
	}

	public String getJqlString(FilterDefinition filter) {
		List<String> searchParams = new ArrayList<String>();

		ProjectFilter projectFilter = filter.getProjectFilter();
		if (projectFilter != null && projectFilter.getProjects().length > 0) {
			StringBuilder param = new StringBuilder();
			param.append("project in (");
			List<String> projectKeys = new ArrayList<String>();
			for (Project project : projectFilter.getProjects()) {
				projectKeys.add(project.getKey());
//				addParameter(sb, PROJECT_KEY, project.getId());
			}
			param.append(StringUtils.join(projectKeys, ","));
			param.append(")");

			searchParams.add(param.toString());
		}

//		ComponentFilter componentFilter = filter.getComponentFilter();
//		if (componentFilter != null) {
//			if (componentFilter.hasNoComponent()) {
//				addParameter(sb, COMPONENT_KEY, COMPONENT_NONE);
//			}
//			if (componentFilter.getComponents() != null) {
//				for (Component component : componentFilter.getComponents()) {
//					addParameter(sb, COMPONENT_KEY, component.getId());
//				}
//			}
//		}
//
//		VersionFilter fixForVersionFilter = filter.getFixForVersionFilter();
//		if (fixForVersionFilter != null) {
//			if (fixForVersionFilter.hasNoVersion()) {
//				addParameter(sb, FIXFOR_KEY, VERSION_NONE);
//			}
//			if (fixForVersionFilter.isReleasedVersions()) {
//				addParameter(sb, FIXFOR_KEY, VERSION_RELEASED);
//			}
//			if (fixForVersionFilter.isUnreleasedVersions()) {
//				addParameter(sb, FIXFOR_KEY, VERSION_UNRELEASED);
//			}
//			if (fixForVersionFilter.getVersions() != null) {
//				for (Version fixVersion : fixForVersionFilter.getVersions()) {
//					addParameter(sb, FIXFOR_KEY, fixVersion.getId());
//				}
//			}
//		}
//
//		VersionFilter reportedInVersionFilter = filter.getReportedInVersionFilter();
//		if (reportedInVersionFilter != null) {
//			if (reportedInVersionFilter.hasNoVersion()) {
//				addParameter(sb, VERSION_KEY, VERSION_NONE);
//			}
//			if (reportedInVersionFilter.isReleasedVersions()) {
//				addParameter(sb, VERSION_KEY, VERSION_RELEASED);
//			}
//			if (reportedInVersionFilter.isUnreleasedVersions()) {
//				addParameter(sb, VERSION_KEY, VERSION_UNRELEASED);
//			}
//			if (reportedInVersionFilter.getVersions() != null) {
//				for (Version reportedVersion : reportedInVersionFilter.getVersions()) {
//					addParameter(sb, VERSION_KEY, reportedVersion.getId());
//				}
//			}
//		}
//
//		IssueTypeFilter issueTypeFilter = filter.getIssueTypeFilter();
//		if (issueTypeFilter != null) {
//			for (IssueType issueType : issueTypeFilter.getIsueTypes()) {
//				addParameter(sb, TYPE_KEY, issueType.getId());
//			}
//		}
//
//		StatusFilter statusFilter = filter.getStatusFilter();
//		if (statusFilter != null) {
//			for (JiraStatus status : statusFilter.getStatuses()) {
//				addParameter(sb, STATUS_KEY, status.getId());
//			}
//		}
//
//		ResolutionFilter resolutionFilter = filter.getResolutionFilter();
//		if (resolutionFilter != null) {
//			Resolution[] resolutions = resolutionFilter.getResolutions();
//			if (resolutions.length == 0) {
//				addParameter(sb, RESOLUTION_KEY, UNRESOLVED); // Unresolved
//			} else {
//				for (Resolution resolution : resolutions) {
//					addParameter(sb, RESOLUTION_KEY, resolution.getId());
//				}
//			}
//		}
//
//		PriorityFilter priorityFilter = filter.getPriorityFilter();
//		if (priorityFilter != null) {
//			for (Priority priority : priorityFilter.getPriorities()) {
//				addParameter(sb, PRIORITY_KEY, priority.getId());
//			}
//		}
//
//		ContentFilter contentFilter = filter.getContentFilter();
//		if (contentFilter != null) {
//			String queryString = contentFilter.getQueryString();
//			if (queryString != null) {
//				addParameter(sb, QUERY_KEY, queryString);
//			}
//			if (contentFilter.isSearchingSummary()) {
//				addParameter(sb, SUMMARY_KEY, "true"); //$NON-NLS-1$
//			}
//			if (contentFilter.isSearchingDescription()) {
//				addParameter(sb, DESCRIPTION_KEY, "true"); //$NON-NLS-1$
//			}
//			if (contentFilter.isSearchingComments()) {
//				addParameter(sb, BODY_KEY, "true"); //$NON-NLS-1$
//			}
//			if (contentFilter.isSearchingEnvironment()) {
//				addParameter(sb, ENVIRONMENT_KEY, "true"); //$NON-NLS-1$
//			}
//		}
//
//		addUserFilter(sb, filter.getReportedByFilter(), REPORTER_KEY, ISSUE_NO_REPORTER);
//		addUserFilter(sb, filter.getAssignedToFilter(), ASSIGNEE_KEY, UNASSIGNED);
//
//		addDateFilter(sb, filter.getCreatedDateFilter(), CREATED_KEY);
//		addDateFilter(sb, filter.getUpdatedDateFilter(), UPDATED_KEY);
//		addDateFilter(sb, filter.getDueDateFilter(), DUEDATE_KEY);
//
//		addOrdering(sb, filter.getOrdering());
//
//		EstimateVsActualFilter estimateFilter = filter.getEstimateVsActualFilter();
//		if (estimateFilter != null) {
//			float min = estimateFilter.getMinVariation();
//			if (min != 0L) {
//				addParameter(sb, "minRatioLimit", Float.toString(min)); //$NON-NLS-1$
//			}
//			float max = estimateFilter.getMaxVariation();
//			if (max != 0L) {
//				addParameter(sb, "maxRatioLimit", Float.toString(max)); //$NON-NLS-1$
//			}
//		}

		return StringUtils.join(searchParams, " AND ");
	}

	private void addOrdering(StringBuilder sb, Order[] ordering) {
		for (Order order : ordering) {
			String fieldName = getNameFromField(order.getField());
			if (fieldName == null) {
				continue;
			}
			addParameter(sb, "sorter/field", fieldName); //$NON-NLS-1$
			addParameter(sb, "sorter/order", order.isAscending() ? "ASC" : "DESC"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	private void addDateFilter(StringBuilder sb, DateFilter filter, String type) {
		if (filter instanceof DateRangeFilter) {
			DateRangeFilter rangeFilter = (DateRangeFilter) filter;
			if (rangeFilter.getFromDate() != null) {
				addParameter(sb, type + ":after", dateFormat.format(rangeFilter.getFromDate())); //$NON-NLS-1$
			}
			if (rangeFilter.getToDate() != null) {
				addParameter(sb, type + ":before", dateFormat.format(rangeFilter.getToDate())); //$NON-NLS-1$
			}
			if (rangeFilter.getFrom() != null && rangeFilter.getFrom().length() > 0) {
				addParameter(sb, type + ":previous", rangeFilter.getFrom()); //$NON-NLS-1$
			}
			if (rangeFilter.getTo() != null && rangeFilter.getTo().length() > 0) {
				addParameter(sb, type + ":next", rangeFilter.getTo()); //$NON-NLS-1$
			}
		} else if (filter instanceof RelativeDateRangeFilter) {
			RelativeDateRangeFilter rangeFilter = (RelativeDateRangeFilter) filter;
			if (rangeFilter.previousMilliseconds() != 0L) {
				addParameter(sb, type + ":previous", createRelativeDateString(rangeFilter.getPreviousRangeType(), //$NON-NLS-1$
						rangeFilter.getPreviousCount()));
			}
			if (rangeFilter.nextMilliseconds() != 0L) {
				addParameter(sb, type + ":next", createRelativeDateString(rangeFilter.getNextRangeType(), //$NON-NLS-1$
						rangeFilter.getNextCount()));
			}
		}
	}

	private String createRelativeDateString(RelativeDateRangeFilter.RangeType rangeType, long count) {
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

	private void addUserFilter(StringBuilder sb, UserFilter filter, String type, String nobodyText) {
		if (filter instanceof NobodyFilter) {
			addParameter(sb, type + "Select", nobodyText); //$NON-NLS-1$
		} else if (filter instanceof CurrentUserFilter) {
			addParameter(sb, type + "Select", ISSUE_CURRENT_USER); //$NON-NLS-1$
		} else if (filter instanceof SpecificUserFilter) {
			addParameter(sb, type + "Select", ISSUE_SPECIFIC_USER); //$NON-NLS-1$
			addParameter(sb, type, ((SpecificUserFilter) filter).getUser());
		} else if (filter instanceof UserInGroupFilter) {
			addParameter(sb, type + "Select", ISSUE_SPECIFIC_GROUP); //$NON-NLS-1$
			addParameter(sb, type, ((UserInGroupFilter) filter).getGroup());
		}
	}

	private void addParameter(StringBuilder sb, String name, String value) {
		try {
			sb.append('&').append(name).append('=').append(URLEncoder.encode(value, encoding));
		} catch (UnsupportedEncodingException ex) {
			// ignore
		}
	}

	// TODO there should be an easier way of doing this
	// Would it be so bad to have the field name in the field?
	private String getNameFromField(Order.Field field) {
		if (Order.Field.ISSUE_TYPE == field) {
			return "issuetype"; //$NON-NLS-1$
		} else if (Order.Field.ISSUE_KEY == field) {
			return "issuekey"; //$NON-NLS-1$
		} else if (Order.Field.SUMMARY == field) {
			return "summary"; //$NON-NLS-1$
		} else if (Order.Field.ASSIGNEE == field) {
			return "assignee"; //$NON-NLS-1$
		} else if (Order.Field.REPORTER == field) {
			return "reporter"; //$NON-NLS-1$
		} else if (Order.Field.PRIORITY == field) {
			return "priority"; //$NON-NLS-1$
		} else if (Order.Field.STATUS == field) {
			return "status"; //$NON-NLS-1$
		} else if (Order.Field.RESOLUTION == field) {
			return "resolution"; //$NON-NLS-1$
		} else if (Order.Field.CREATED == field) {
			return "created"; //$NON-NLS-1$
		} else if (Order.Field.UPDATED == field) {
			return "updated"; //$NON-NLS-1$
		} else if (Order.Field.DUE_DATE == field) {
			return "duedate"; //$NON-NLS-1$
		}

		return null;
	}

}
