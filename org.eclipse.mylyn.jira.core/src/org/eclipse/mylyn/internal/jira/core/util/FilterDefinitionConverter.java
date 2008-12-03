/*******************************************************************************
 * Copyright (c) 2004, 2008 Brock Janiczak and others.
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

package org.eclipse.mylyn.internal.jira.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.mylyn.internal.jira.core.InvalidJiraQueryException;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.model.filter.ComponentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ContentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.CurrentUserFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.DateFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.DateRangeFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueTypeFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.NobodyFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.PriorityFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ProjectFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ResolutionFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.SpecificUserFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.StatusFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.UserFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.UserInGroupFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.VersionFilter;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;

/**
 * A JiraCustomQuery represents a custom query for issues from a Jira repository.
 * 
 * @author Mik Kersten
 * @author Eugene Kuleshov
 * @author Steffen Pingel
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

	private static final String VERSION_NONE = "-1"; //$NON-NLS-1$

	private static final String VERSION_RELEASED = "-2"; //$NON-NLS-1$

	private static final String VERSION_UNRELEASED = "-3"; //$NON-NLS-1$

	private static final String UNRESOLVED = "-1"; //$NON-NLS-1$

	private static final String COMPONENT_NONE = "-1"; //$NON-NLS-1$

	private final String encoding;

	public FilterDefinitionConverter(String encoding) {
		this.encoding = encoding;
	}

	public String toUrl(String repositoryUrl, FilterDefinition filter) {
		return repositoryUrl + JiraRepositoryConnector.FILTER_URL_PREFIX + "&reset=true" + getQueryParams(filter); //$NON-NLS-1$
	}

	public FilterDefinition toFilter(JiraClient client, String url, boolean validate) {
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
		for (String projectId : projectIds) {
			Project project = client.getCache().getProjectById(projectId);
			if (project == null) {
				if (validate) {
					// safeguard
					throw new InvalidJiraQueryException("Unknown project " + projectId); //$NON-NLS-1$
				} else {
					continue;
				}
			}

			filter.setProjectFilter(new ProjectFilter(project));

			List<String> componentIds = getIds(params, COMPONENT_KEY);
			List<Component> components = new ArrayList<Component>();
			for (String componentId : componentIds) {
				Component[] projectComponents = project.getComponents();
				for (Component component : projectComponents) {
					if (component.getId().equals(componentId)) {
						components.add(component);
					}
				}
			}
			if (!componentIds.isEmpty()) {
				filter.setComponentFilter(new ComponentFilter(components.toArray(new Component[components.size()])));
			}

			Version[] projectVersions = project.getVersions();

			filter.setFixForVersionFilter(getVersionFilter(filter, getIds(params, FIXFOR_KEY), projectVersions));
			filter.setReportedInVersionFilter(getVersionFilter(filter, getIds(params, VERSION_KEY), projectVersions));
		}

		List<String> typeIds = getIds(params, TYPE_KEY);
		List<IssueType> issueTypes = new ArrayList<IssueType>();
		for (String typeId : typeIds) {
			IssueType issueType = client.getCache().getIssueTypeById(typeId);
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
			} else if (fixForId.equals(VERSION_RELEASED)) {
				hasReleasedVersions = true;
			} else if (fixForId.equals(VERSION_UNRELEASED)) {
				hasUnreleasedVersions = true;
			} else {
				for (Version projectVersion : projectVersions) {
					if (projectVersion.getId().equals(fixForId)) {
						fixForversions.add(projectVersion);
					}
				}
			}
		}
		if (!fixForversions.isEmpty()) {
			return new VersionFilter(fixForversions.toArray(new Version[fixForversions.size()]));
		} else if (hasNoVersions) {
			return new VersionFilter(new Version[0]);
		} else if (hasReleasedVersions || hasUnreleasedVersions) {
			return new VersionFilter(hasReleasedVersions, hasUnreleasedVersions);
		}
		return null;
	}

	private DateFilter createDateFilter(Map<String, List<String>> params, String key) {
		String after = getId(params, key + ":after"); //$NON-NLS-1$
		String before = getId(params, key + ":before"); //$NON-NLS-1$

		SimpleDateFormat df = new SimpleDateFormat("d/MMM/yy", Locale.US); //$NON-NLS-1$
		Date fromDate;
		try {
			fromDate = df.parse(after);
		} catch (Exception ex) {
			fromDate = null;
		}
		Date toDate;
		try {
			toDate = df.parse(before);
		} catch (Exception ex) {
			toDate = null;
		}

		return fromDate == null && toDate == null ? null : new DateRangeFilter(fromDate, toDate);
	}

	private UserFilter createUserFilter(Map<String, List<String>> params, String key) {
		String type = getId(params, key + "Select"); //$NON-NLS-1$
		if (ISSUE_NO_REPORTER.equals(type)) {
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

	private String getQueryParams(FilterDefinition filter) {
		StringBuilder sb = new StringBuilder();

		ProjectFilter projectFilter = filter.getProjectFilter();
		if (projectFilter != null) {
			Project project = projectFilter.getProject();
			// TODO all projects
			addParameter(sb, PROJECT_KEY, project.getId());
		}

		ComponentFilter componentFilter = filter.getComponentFilter();
		// TODO all components
		if (componentFilter != null) {
			if (componentFilter.hasNoComponent()) {
				addParameter(sb, COMPONENT_KEY, COMPONENT_NONE);
			} else {
				for (Component component : componentFilter.getComponents()) {
					addParameter(sb, COMPONENT_KEY, component.getId());
				}
			}
		}

		// TODO
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

		// TODO
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

		// TODO
		IssueTypeFilter issueTypeFilter = filter.getIssueTypeFilter();
		if (issueTypeFilter != null) {
			for (IssueType issueType : issueTypeFilter.getIsueTypes()) {
				addParameter(sb, TYPE_KEY, issueType.getId());
			}
		}

		// TODO
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

		addUserFilter(sb, filter.getReportedByFilter(), REPORTER_KEY);
		addUserFilter(sb, filter.getAssignedToFilter(), ASSIGNEE_KEY);

		addDateFilter(sb, filter.getCreatedDateFilter(), CREATED_KEY);
		addDateFilter(sb, filter.getUpdatedDateFilter(), UPDATED_KEY);
		addDateFilter(sb, filter.getDueDateFilter(), DUEDATE_KEY);

		return sb.toString();
	}

	private void addDateFilter(StringBuilder sb, DateFilter filter, String type) {
		if (filter instanceof DateRangeFilter) {
			SimpleDateFormat df = new SimpleDateFormat("d/MMM/yy", Locale.US); //$NON-NLS-1$
			DateRangeFilter rangeFilter = (DateRangeFilter) filter;
			addParameter(sb, type + ":after", df.format(rangeFilter.getFromDate())); //$NON-NLS-1$
			addParameter(sb, type + ":before", df.format(rangeFilter.getToDate())); //$NON-NLS-1$
		}
	}

	private void addUserFilter(StringBuilder sb, UserFilter filter, String type) {
		if (filter instanceof NobodyFilter) {
			addParameter(sb, type + "Select", ISSUE_NO_REPORTER); //$NON-NLS-1$
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

}
