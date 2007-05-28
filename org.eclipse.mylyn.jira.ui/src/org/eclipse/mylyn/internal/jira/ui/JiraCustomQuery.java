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

package org.eclipse.mylar.internal.jira.ui;

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

import org.eclipse.mylar.internal.jira.core.model.Component;
import org.eclipse.mylar.internal.jira.core.model.IssueType;
import org.eclipse.mylar.internal.jira.core.model.Priority;
import org.eclipse.mylar.internal.jira.core.model.Project;
import org.eclipse.mylar.internal.jira.core.model.Resolution;
import org.eclipse.mylar.internal.jira.core.model.Status;
import org.eclipse.mylar.internal.jira.core.model.Version;
import org.eclipse.mylar.internal.jira.core.model.filter.ComponentFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.ContentFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.CurrentUserFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.DateFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.DateRangeFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylar.internal.jira.core.model.filter.IssueTypeFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.NobodyFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.PriorityFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.ProjectFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.ResolutionFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.SpecificUserFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.StatusFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.UserFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.UserInGroupFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.VersionFilter;
import org.eclipse.mylar.internal.jira.core.service.JiraClient;
import org.eclipse.mylar.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylar.tasks.core.TaskList;

/**
 * A JiraCustomQuery represents a custom query for issues from a Jira repository.
 *
 * @author Mik Kersten
 * @author Eugene Kuleshov
 */
public class JiraCustomQuery extends AbstractRepositoryQuery {

	private static final String PROJECT_KEY = "pid";
	private static final String COMPONENT_KEY = "component";
	private static final String TYPE_KEY = "type";
	private static final String PRIORITY_KEY = "priority";
	private static final String STATUS_KEY = "status";
	private static final String RESOLUTION_KEY = "resolution";

	private static final String FIXFOR_KEY = "fixfor";
	private static final String VERSION_KEY = "version";

	private static final String QUERY_KEY = "query";
	private static final String ENVIRONMENT_KEY = "environment";
	private static final String BODY_KEY = "body";
	private static final String DESCRIPTION_KEY = "description";
	private static final String SUMMARY_KEY = "summary";

	private static final String ASSIGNEE_KEY = "assignee";
	private static final String REPORTER_KEY = "reporter";

	private static final String CREATED_KEY = "created";
	private static final String UPDATED_KEY = "updated";
	private static final String DUEDATE_KEY = "duedate";

	private static final String ISSUE_SPECIFIC_GROUP = "specificgroup";
	private static final String ISSUE_SPECIFIC_USER = "specificuser";
	private static final String ISSUE_CURRENT_USER = "issue_current_user";
	private static final String ISSUE_NO_REPORTER = "issue_no_reporter";
	
	private static final String VERSION_NONE = "-1";
	private static final String VERSION_RELEASED = "-2";
	private static final String VERSION_UNRELEASED = "-3";
	
	private static final String UNRESOLVED = "-1";
	private static final String COMPONENT_NONE = "-1";

	private String encoding;


	public JiraCustomQuery(String repositoryUrl, FilterDefinition filter, String encoding, TaskList taskList) {
		super(filter.getName(), taskList);
		this.repositoryUrl = repositoryUrl;
		this.encoding = encoding;
		this.url = repositoryUrl + JiraRepositoryConnector.FILTER_URL_PREFIX + "&reset=true" + getQueryParams(filter);
	}

	public JiraCustomQuery(String name, String queryUrl, String repositoryUrl, String encoding, TaskList taskList) {
		super(name, taskList);
		this.repositoryUrl = repositoryUrl;
		this.url = queryUrl;
		this.encoding = encoding;
	}

	public String getRepositoryKind() {
		return JiraUiPlugin.REPOSITORY_KIND;
	}

	public FilterDefinition getFilterDefinition(JiraClient jiraServer, boolean validate) {
		FilterDefinition filter = createFilter(jiraServer, getUrl(), validate);
		filter.setName(getSummary());
		return filter;
	}

	private FilterDefinition createFilter(JiraClient jiraServer, String url, boolean validate) {
		FilterDefinition filter = new FilterDefinition();

		int n = url.indexOf('?');
		if(n==-1) {
			return filter;
		}

		HashMap<String, List<String>> params = new HashMap<String, List<String>>();
		for (String pair : url.substring(n+1).split("&")) {
			String[] tokens = pair.split("=");
			if (tokens.length > 1) {
				try {
					String key = tokens[0];
					String value = tokens.length==1 ? "" : URLDecoder.decode(tokens[1], encoding);
					List<String> values = params.get(key);
					if(values==null) {
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
			Project project = jiraServer.getProjectById(projectId);
			if (project == null) {
				if (validate) {
					// safeguard
					throw new InvalidJiraQueryException("Unknown project " + projectId);
				} else {
					continue;
				}
			}
			
			filter.setProjectFilter(new ProjectFilter(project));

			List<String> componentIds = getIds(params, COMPONENT_KEY);
			List<Component> components = new ArrayList<Component>();
			for (String componentId : componentIds) {
				Component[] projectComponents = project.getComponents();
				for(Component component : projectComponents) {
					if(component.getId().equals(componentId)) {
						components.add(component);
					}
				}
			}
			if(!componentIds.isEmpty()) {
				filter.setComponentFilter(new ComponentFilter(components.toArray(new Component[components.size()])));
			}

			Version[] projectVersions = project.getVersions();

			filter.setFixForVersionFilter(getVersionFilter(filter, getIds(params, FIXFOR_KEY), projectVersions));
			filter.setReportedInVersionFilter(getVersionFilter(filter, getIds(params, VERSION_KEY), projectVersions));
		}

		List<String> typeIds = getIds(params, TYPE_KEY);
		List<IssueType> issueTypes = new ArrayList<IssueType>();
		for (String typeId : typeIds) {
			IssueType issueType = jiraServer.getIssueTypeById(typeId);
			if (issueType != null) {
				issueTypes.add(issueType);
			} else if (validate) {
				throw new InvalidJiraQueryException("Unknown type " + typeId);
			}
		}
		if(!issueTypes.isEmpty()) {
			filter.setIssueTypeFilter(new IssueTypeFilter(issueTypes.toArray(new IssueType[issueTypes.size()])));
		}

		List<String> statusIds = getIds(params, STATUS_KEY);
		List<Status> statuses = new ArrayList<Status>();
		for (String statusId : statusIds) {
			Status status = jiraServer.getStatusById(statusId);
			if (status != null) {
				statuses.add(status);
			} else if (validate) {
				throw new InvalidJiraQueryException("Unknown status " + statusId);
			}
		}
		if(!statuses.isEmpty()) {
			filter.setStatusFilter(new StatusFilter(statuses.toArray(new Status[statuses.size()])));
		}

		List<String> resolutionIds = getIds(params, RESOLUTION_KEY);
		List<Resolution> resolutions = new ArrayList<Resolution>();
		boolean unresolved = false;
		for (String resolutionId : resolutionIds) {
			if (!UNRESOLVED.equals(resolutionId)) {
				Resolution resolution = jiraServer.getResolutionById(resolutionId);
				if (resolution != null) {
					resolutions.add(resolution);
				} else if (validate) {
					throw new InvalidJiraQueryException("Unknown resolution " + resolutionId);
				}
			} else {
				unresolved = true;
			}
		}
		if(!resolutionIds.isEmpty()) {
			filter.setResolutionFilter(new ResolutionFilter(resolutions.toArray(new Resolution[resolutions.size()])));
		} else if (unresolved) {
			filter.setResolutionFilter(new ResolutionFilter(new Resolution[0]));
		}

		List<String> queries = getIds(params, QUERY_KEY);
		for (String query : queries) {
			boolean searchSummary = getIds(params, SUMMARY_KEY).contains("true");
			boolean searchDescription = getIds(params, DESCRIPTION_KEY).contains("true");
			boolean searchEnvironment = getIds(params, ENVIRONMENT_KEY).contains("true");
			boolean searchComments = getIds(params, BODY_KEY).contains("true");
			filter.setContentFilter(new ContentFilter(query, searchSummary, searchDescription, searchEnvironment, searchComments));
		}

		filter.setReportedByFilter(createUserFilter(params, REPORTER_KEY));
		filter.setAssignedToFilter(createUserFilter(params, ASSIGNEE_KEY));

		filter.setCreatedDateFilter(createDateFilter(params, CREATED_KEY));
		filter.setUpdatedDateFilter(createDateFilter(params, UPDATED_KEY));
		filter.setDueDateFilter(createDateFilter(params, DUEDATE_KEY));

		return filter;
	}

	private VersionFilter getVersionFilter(FilterDefinition filter, List<String> fixForIds, Version[] projectVersions) {
		if(fixForIds.isEmpty()) {
			return null;
		}
		
		boolean hasReleasedVersions = false;
		boolean hasUnreleasedVersions = false;
		List<Version> fixForversions = new ArrayList<Version>();
		for (String fixForId : fixForIds) {
			if(fixForId.equals(VERSION_RELEASED)) {
				hasReleasedVersions = true;
			} else if(fixForId.equals(VERSION_UNRELEASED)) {
				hasUnreleasedVersions = true;
			} else {
				for (Version projectVersion : projectVersions) {
					if(projectVersion.getId().equals(fixForId)) {
						fixForversions.add(projectVersion);
					}
				}
			}
		}
		if(!fixForversions.isEmpty()) {
			return new VersionFilter(fixForversions.toArray(new Version[fixForversions.size()]));
		} else if(hasReleasedVersions || hasUnreleasedVersions) {
			return new VersionFilter(hasReleasedVersions, hasUnreleasedVersions);
		}
		return null;
	}

	private DateFilter createDateFilter(Map<String, List<String>> params, String key) {
		String after = getId(params, key + ":after");
		String before = getId(params, key + ":before");

		SimpleDateFormat df = new SimpleDateFormat("d/MMM/yy", Locale.US);
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

		return fromDate==null && toDate==null ? null : new DateRangeFilter(fromDate, toDate);
	}

	private UserFilter createUserFilter(Map<String, List<String>> params, String key) {
		String type = getId(params, key + "Select");
		if(ISSUE_NO_REPORTER.equals(type)) {
			return new NobodyFilter();
		} else if(ISSUE_CURRENT_USER.equals(type)) {
			return new CurrentUserFilter();
		} else {
			String reporter = getId(params, key);
			if(reporter!=null) {
				if(ISSUE_SPECIFIC_USER.equals(type)) {
					return new SpecificUserFilter(reporter);
				} else if(ISSUE_SPECIFIC_GROUP.equals(type)) {
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
		if (ids==null) {
			return Collections.emptyList();
		}
		return ids;
	}


	private String getQueryParams(FilterDefinition filter) {
		StringBuffer sb = new StringBuffer();

		ProjectFilter projectFilter = filter.getProjectFilter();
		if(projectFilter!=null) {
			Project project = projectFilter.getProject();
			// TODO all projects
			addParameter(sb, PROJECT_KEY, project.getId());
		}

		ComponentFilter componentFilter = filter.getComponentFilter();
		// TODO all components
		if(componentFilter!=null) {
			if(componentFilter.hasNoComponent()) {
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
			if(fixForVersionFilter.hasNoVersion()) {
				addParameter(sb, FIXFOR_KEY, VERSION_NONE);
			}
			if(fixForVersionFilter.isReleasedVersions()) {
				addParameter(sb, FIXFOR_KEY, VERSION_RELEASED);
			}
			if(fixForVersionFilter.isUnreleasedVersions()) {
				addParameter(sb, FIXFOR_KEY, VERSION_UNRELEASED);
			}
			if(fixForVersionFilter.getVersions()!=null) {
				for (Version fixVersion : fixForVersionFilter.getVersions()) {
					addParameter(sb, FIXFOR_KEY, fixVersion.getId());
				}
			}
		}

		// TODO
		VersionFilter reportedInVersionFilter = filter.getReportedInVersionFilter();
		if (reportedInVersionFilter != null) {
			if(reportedInVersionFilter.hasNoVersion()) {
				addParameter(sb, VERSION_KEY, VERSION_NONE);
			}
			if(reportedInVersionFilter.isReleasedVersions()) {
				addParameter(sb, VERSION_KEY, VERSION_RELEASED);
			}
			if(reportedInVersionFilter.isUnreleasedVersions()) {
				addParameter(sb, VERSION_KEY, VERSION_UNRELEASED);
			}
			if(reportedInVersionFilter.getVersions()!=null) {
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
		if(statusFilter!=null) {
			for ( Status status : statusFilter.getStatuses()) {
				addParameter(sb, STATUS_KEY, status.getId());
			}
		}

		ResolutionFilter resolutionFilter = filter.getResolutionFilter();
		if(resolutionFilter!=null) {
			Resolution[] resolutions = resolutionFilter.getResolutions();
			if (resolutions.length == 0) {
				addParameter(sb, RESOLUTION_KEY, UNRESOLVED);  // Unresolved
			} else {
				for (Resolution resolution : resolutions) {
					addParameter(sb, RESOLUTION_KEY, resolution.getId());
				}
			}			
		}

		PriorityFilter priorityFilter = filter.getPriorityFilter();
		if(priorityFilter!=null) {
			for ( Priority priority : priorityFilter.getPriorities()) {
				addParameter(sb, PRIORITY_KEY, priority.getId());
			}
		}

		ContentFilter contentFilter = filter.getContentFilter();
		if(contentFilter!=null) {
			String queryString = contentFilter.getQueryString();
			if(queryString!=null) {
				addParameter(sb, QUERY_KEY, queryString);
			}
			if(contentFilter.isSearchingSummary()) {
				addParameter(sb, SUMMARY_KEY, "true");
			}
			if(contentFilter.isSearchingDescription()) {
				addParameter(sb, DESCRIPTION_KEY, "true");
			}
			if(contentFilter.isSearchingComments()) {
				addParameter(sb, BODY_KEY, "true");
			}
			if(contentFilter.isSearchingEnvironment()) {
				addParameter(sb, ENVIRONMENT_KEY, "true");
			}
		}

		addUserFilter(sb, filter.getReportedByFilter(), REPORTER_KEY);
		addUserFilter(sb, filter.getAssignedToFilter(), ASSIGNEE_KEY);

		addDateFilter(sb, filter.getCreatedDateFilter(), CREATED_KEY);
		addDateFilter(sb, filter.getUpdatedDateFilter(), UPDATED_KEY);
		addDateFilter(sb, filter.getDueDateFilter(), DUEDATE_KEY);

		return sb.toString();
	}

	private void addDateFilter(StringBuffer sb, DateFilter filter, String type) {
		if(filter instanceof DateRangeFilter) {
			SimpleDateFormat df = new SimpleDateFormat("d/MMM/yy", Locale.US);
			DateRangeFilter rangeFilter = (DateRangeFilter) filter;
			addParameter(sb, type + ":after", df.format(rangeFilter.getFromDate()));
			addParameter(sb, type + ":before", df.format(rangeFilter.getToDate()));
		}
	}

	private void addUserFilter(StringBuffer sb, UserFilter filter, String type) {
		if(filter instanceof NobodyFilter) {
			addParameter(sb, type + "Select", ISSUE_NO_REPORTER);
		} else if(filter instanceof CurrentUserFilter) {
			addParameter(sb, type + "Select", ISSUE_CURRENT_USER);
		} else if(filter instanceof SpecificUserFilter) {
			addParameter(sb, type + "Select", ISSUE_SPECIFIC_USER);
			addParameter(sb, type, ((SpecificUserFilter) filter).getUser());
		} else if(filter instanceof UserInGroupFilter) {
			addParameter(sb, type + "Select", ISSUE_SPECIFIC_GROUP);
			addParameter(sb, type, ((UserInGroupFilter) filter).getGroup());
		}
	}

	private void addParameter(StringBuffer sb, String name, String value) {
		try {
			sb.append('&').append(name).append('=').append(URLEncoder.encode(value, encoding));
		} catch (UnsupportedEncodingException ex) {
			// ignore
		}
	}

}

//public void refreshHits() {
//isRefreshing = true;
//Job j = new Job(LABEL_REFRESH_JOB) {
//
//	@Override
//	protected IStatus run(final IProgressMonitor monitor) {
//		clearHits();
//		try {
//			TaskRepository repository = MylarTaskListPlugin.getRepositoryManager().getRepository(JiraUiPlugin.JIRA_REPOSITORY_KIND, repositoryUrl);
//			JiraServerFacade.getDefault().getJiraServer(repository).executeNamedFilter(filter, new IssueCollector() {
//
//				public void done() {
//					isRefreshing = false;
//					Display.getDefault().asyncExec(new Runnable() {
//						public void run() {
//							if (TaskListView.getDefault() != null)
//								TaskListView.getDefault().refreshAndFocus();
//						}
//					});
//				}
//
//				public boolean isCancelled() {
//					return monitor.isCanceled();
//				}
//
//				public void collectIssue(Issue issue) {
//					int issueId = new Integer(issue.getId());
//					JiraFilterHit hit = new JiraFilterHit(issue, JiraFilter.this.getRepositoryUrl(), issueId);
//					addHit(hit);
//				}
//
//				public void start() {
//
//				}
//			});
//
//		} catch (Exception e) {
//			isRefreshing = false;
//			JiraServerFacade.handleConnectionException(e);
//			return Status.CANCEL_STATUS;
//		}
//
//		return Status.OK_STATUS;
//	}
//
//};
//
//j.schedule();
//
//}

//public String getQueryUrl() {
//return super.getQueryUrl();
//}

//public String getPriority() {
//return "";
//}

//public String getDescription() {
//if (filter.getName() != null) {
//	return filter.getName();
//} else {
//	return super.getDescription();
//}
//}
//
//public void setDescription(String summary) {
//filter.setDescription(summary);
//}

//public boolean isLocal() {
//return false;
//}

///** True if the filter is currently downloading hits */
//public boolean isRefreshing() {
//return isRefreshing;
//}
//public void setRefreshing(boolean refreshing) {
//this.isRefreshing = refreshing;
//}
