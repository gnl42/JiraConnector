/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service.web.rss;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.Status;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.model.filter.ComponentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ContentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.CurrentUserFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.DateFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.DateRangeFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.EstimateVsActualFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueTypeFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.NobodyFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.Order;
import org.eclipse.mylyn.internal.jira.core.model.filter.PriorityFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ProjectFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.RelativeDateRangeFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ResolutionFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.SpecificUserFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.StatusFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.UserFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.UserInGroupFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.VersionFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.RelativeDateRangeFilter.RangeType;
import org.eclipse.mylyn.internal.jira.core.util.JiraCoreUtil;

/**
 * @author Brock Janiczak
 */
public class JiraRssFilterConverter {

	private final String DATE_FORMAT = "dd/MMM/yy"; //$NON-NLS-1$

	public JiraRssFilterConverter() {
	}

	public String convert(FilterDefinition filterDefinition, String encoding) {
		StringBuffer buffer = new StringBuffer();

		if (filterDefinition.getProjectFilter() != null) {
			buffer.append('&').append(convertProjectFilter(filterDefinition.getProjectFilter()));
			if (filterDefinition.getComponentFilter() != null) {
				buffer.append('&').append(convertComponentFilter(filterDefinition.getComponentFilter()));
			}

			if (filterDefinition.getFixForVersionFilter() != null) {
				String versionFilterString = convertVersionFilter("fixfor", filterDefinition.getFixForVersionFilter()); //$NON-NLS-1$
				if (versionFilterString.length() > 0) {
					buffer.append('&').append(versionFilterString);
				}
			}

			if (filterDefinition.getReportedInVersionFilter() != null) {
				String versionFilterString = convertVersionFilter(
						"version", filterDefinition.getReportedInVersionFilter()); //$NON-NLS-1$
				if (versionFilterString.length() > 0) {
					buffer.append('&').append(versionFilterString);
				}
			}

			// TODO add custom filters here

		}

		if (filterDefinition.getContentFilter() != null) {
			buffer.append('&').append(convertContentFilter(filterDefinition.getContentFilter(), encoding));
		}

		if (filterDefinition.getIssueTypeFilter() != null) {
			buffer.append('&').append(convertIssueTypeFilter(filterDefinition.getIssueTypeFilter()));
		}

		if (filterDefinition.getAssignedToFilter() != null) {
			buffer.append('&').append(convertAssignedToFilter(filterDefinition.getAssignedToFilter()));
		}

		if (filterDefinition.getPriorityFilter() != null) {
			buffer.append('&').append(convertPriorityFilter(filterDefinition.getPriorityFilter()));
		}

		if (filterDefinition.getStatusFilter() != null) {
			buffer.append('&').append(convertStatusFilter(filterDefinition.getStatusFilter()));
		}

		if (filterDefinition.getResolutionFilter() != null) {
			buffer.append('&').append(convertResolutionFilter(filterDefinition.getResolutionFilter()));
		}

		if (filterDefinition.getReportedByFilter() != null) {
			buffer.append('&').append(convertReportedByFilter(filterDefinition.getReportedByFilter()));
		}

		if (filterDefinition.getEstimateVsActualFilter() != null) {
			buffer.append('&').append(convertEstimateVsActualFilter(filterDefinition.getEstimateVsActualFilter()));
		}

		if (filterDefinition.getCreatedDateFilter() != null) {
			buffer.append(convertCreatedDateFilter(filterDefinition.getCreatedDateFilter()));
		}

		if (filterDefinition.getUpdatedDateFilter() != null) {
			buffer.append(convertUpdatedDateFilter(filterDefinition.getUpdatedDateFilter()));
		}

		if (filterDefinition.getDueDateFilter() != null) {
			buffer.append(convertDueDateFilter(filterDefinition.getDueDateFilter()));
		}

		if (filterDefinition.getOrdering() != null) {
			buffer.append(convertOrdering(filterDefinition.getOrdering()));
		}

		if (buffer.length() > 0) {
			// Trim off the leading & if there is one
			return buffer.substring(1);
		}

		return ""; //$NON-NLS-1$
	}

	protected String convertAssignedToFilter(UserFilter assignedToFilter) {
		StringBuffer buffer = new StringBuffer();
		if (assignedToFilter instanceof NobodyFilter) {
			buffer.append("assigneeSelect=unassigned"); //$NON-NLS-1$
		} else if (assignedToFilter instanceof SpecificUserFilter) {
			buffer.append("assigneeSelect=specificuser&assignee=") //$NON-NLS-1$
					.append(((SpecificUserFilter) assignedToFilter).getUser());
		} else if (assignedToFilter instanceof UserInGroupFilter) {
			buffer.append("assigneeSelect=specificgroup&assignee=") //$NON-NLS-1$
					.append(((UserInGroupFilter) assignedToFilter).getGroup());
		} else if (assignedToFilter instanceof CurrentUserFilter) {
			return "assigneeSelect=issue_current_user"; //$NON-NLS-1$
		}

		return buffer.toString();
	}

	protected String convertComponentFilter(ComponentFilter componentFilter) {
		if (componentFilter.hasNoComponent()) {
			return "component=-1"; //$NON-NLS-1$
		}

		StringBuffer buffer = new StringBuffer();
		Component[] components = componentFilter.getComponents();
		if (components.length == 0) {
			return ""; //$NON-NLS-1$
		}

		buffer.append("component=").append(components[0].getId()); //$NON-NLS-1$

		for (int i = 1; i < components.length; i++) {
			buffer.append("&component=").append(components[i].getId()); //$NON-NLS-1$
		}

		return buffer.toString();
	}

	protected String convertContentFilter(ContentFilter contentFilter, String encoding) {
		return new StringBuffer().append("query=").append(JiraCoreUtil.encode(contentFilter.getQueryString(), encoding)) //$NON-NLS-1$
				.append("&summary=").append(contentFilter.isSearchingSummary()) //$NON-NLS-1$
				.append("&description=").append(contentFilter.isSearchingDescription()) //$NON-NLS-1$
				.append("&body=").append(contentFilter.isSearchingComments()) //$NON-NLS-1$
				.append("&environment=").append(contentFilter.isSearchingEnvironment()) //$NON-NLS-1$
				.toString();
	}

	protected String convertCreatedDateFilter(DateFilter createdDateFilter) {
		return createDateFilter(createdDateFilter, "created");
	}

	protected String convertDueDateFilter(DateFilter dueDateFilter) {
		return createDateFilter(dueDateFilter, "duedate");
	}

	protected String convertEstimateVsActualFilter(EstimateVsActualFilter filter) {
		StringBuffer buffer = new StringBuffer();
		float min = filter.getMinVariation();
		float max = filter.getMaxVariation();

		if (min != 0L) {
			buffer.append("minRatioLimit=").append(min); //$NON-NLS-1$
		}

		if (max != 0L) {
			if (buffer.length() > 0) {
				buffer.append('&');
			}
			buffer.append("maxRatioLimit=").append(max); //$NON-NLS-1$
		}
		return buffer.toString();
	}

	protected String convertIssueTypeFilter(IssueTypeFilter issueTypeFilter) {
		StringBuffer buffer = new StringBuffer();

		if (issueTypeFilter.isStandardTypes()) {
			buffer.append("type=").append("-2"); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (issueTypeFilter.isSubTaskTypes()) {
			buffer.append("type=").append("-3"); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (issueTypeFilter.getIsueTypes() != null) {
			IssueType[] issueTypes = issueTypeFilter.getIsueTypes();
			if (issueTypes.length == 0) {
				return ""; //$NON-NLS-1$
			}

			buffer.append("type=").append(issueTypes[0].getId()); //$NON-NLS-1$

			for (int i = 1; i < issueTypes.length; i++) {
				buffer.append('&').append("type=").append(issueTypes[i].getId()); //$NON-NLS-1$
			}
		}
		return buffer.toString();
	}

	protected String convertOrdering(Order[] ordering) {
		StringBuffer buffer = new StringBuffer();

		for (Order order : ordering) {
			String fieldName = getNameFromField(order.getField());
			if (fieldName == null) {
				continue;
			}
			buffer.append("&sorter/field=").append(fieldName) //$NON-NLS-1$
					.append("&sorter/order=").append(order.isAscending() ? "ASC" : "DESC"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return buffer.toString();
	}

	protected String convertPriorityFilter(PriorityFilter priorityFilter) {
		Priority[] priorities = priorityFilter.getPriorities();
		if (priorities.length == 0) {
			return ""; //$NON-NLS-1$
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append("priority=").append(priorities[0].getId()); //$NON-NLS-1$

		for (int i = 1; i < priorities.length; i++) {
			buffer.append("&priority=").append(priorities[i].getId()); //$NON-NLS-1$
		}

		return buffer.toString();
	}

	protected String convertProjectFilter(ProjectFilter projectFilter) {
		return new StringBuffer().append("pid=").append(projectFilter.getProject().getId()) //$NON-NLS-1$
				.toString();
	}

	protected String convertReportedByFilter(UserFilter reportedByFilter) {
		StringBuffer buffer = new StringBuffer();
		if (reportedByFilter instanceof NobodyFilter) {
			return "reporterSelect=unassigned"; //$NON-NLS-1$
		} else if (reportedByFilter instanceof SpecificUserFilter) {
			buffer.append("reporterSelect=specificuser&reporter=") //$NON-NLS-1$
					.append(((SpecificUserFilter) reportedByFilter).getUser());
		} else if (reportedByFilter instanceof UserInGroupFilter) {
			buffer.append("reporterSelect=specificgroup&reporter=") //$NON-NLS-1$
					.append(((UserInGroupFilter) reportedByFilter).getGroup());
		} else if (reportedByFilter instanceof CurrentUserFilter) {
			return "reporterSelect=issue_current_user"; //$NON-NLS-1$
		}

		return buffer.toString();
	}

	protected String convertResolutionFilter(ResolutionFilter resolutionFilter) {
		if (resolutionFilter.isUnresolved()) {
			return "resolution=-1"; //$NON-NLS-1$
		}

		Resolution[] resolution = resolutionFilter.getResolutions();
		if (resolution.length == 0) {
			return ""; //$NON-NLS-1$
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append("resolution=").append(resolution[0].getId()); //$NON-NLS-1$

		for (int i = 1; i < resolution.length; i++) {
			buffer.append("&resolution=").append(resolution[i].getId()); //$NON-NLS-1$
		}

		return buffer.toString();
	}

	protected String convertStatusFilter(StatusFilter statusFilter) {
		Status[] statuses = statusFilter.getStatuses();
		if (statuses.length == 0) {
			return ""; //$NON-NLS-1$
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append("status=").append(statuses[0].getId()); //$NON-NLS-1$

		for (int i = 1; i < statuses.length; i++) {
			buffer.append("&status=").append(statuses[i].getId()); //$NON-NLS-1$
		}

		return buffer.toString();
	}

	protected String convertUpdatedDateFilter(DateFilter updatedDateFilter) {
		return createDateFilter(updatedDateFilter, "updated");
	}

	protected String convertVersionFilter(String param, VersionFilter versionFilter) {
		// Versions can only be either released or unreleased. If both are
		// selected, search in all versions
		if (versionFilter.isReleasedVersions() && versionFilter.isUnreleasedVersions()) {
			return ""; //$NON-NLS-1$
		}

		if (versionFilter.hasNoVersion()) {
			return param + "=-1"; //$NON-NLS-1$
		}
		if (versionFilter.isUnreleasedVersions()) {
			return param + "=-2"; //$NON-NLS-1$
		}
		if (versionFilter.isReleasedVersions()) {
			return param + "=-3"; //$NON-NLS-1$
		}

		StringBuffer buffer = new StringBuffer();
		Version[] versions = versionFilter.getVersions();
		if (versions.length == 0) {
			return ""; //$NON-NLS-1$
		}

		buffer.append(param).append("=") //$NON-NLS-1$
				.append(versions[0].getId());

		for (int i = 1; i < versions.length; i++) {
			buffer.append("&") //$NON-NLS-1$
					.append(param)
					.append("=") //$NON-NLS-1$
					.append(versions[i].getId());
		}

		return buffer.toString();
	}

	private String createDateFilter(DateFilter dateFilter, String name) {
		StringBuffer buffer = new StringBuffer();
		if (dateFilter instanceof DateRangeFilter) {
			SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT, Locale.US);
			DateRangeFilter filter = (DateRangeFilter) dateFilter;
			if (filter.getFromDate() != null) {
				buffer.append("&" + name + ":after=").append(df.format(filter.getFromDate())); //$NON-NLS-1$
			}

			if (filter.getToDate() != null) {
				buffer.append("&" + name + ":before=").append(df.format(filter.getToDate())); //$NON-NLS-1$
			}

		} else if (dateFilter instanceof RelativeDateRangeFilter) {
			RelativeDateRangeFilter filter = ((RelativeDateRangeFilter) dateFilter);
			if (filter.previousMilliseconds() != 0L) {
				buffer.append("&" + name + ":previous=") //$NON-NLS-1$
						.append(createRelativeDateString(filter.getPreviousRangeType(), filter.getPreviousCount()));
			}

			if (filter.nextMilliseconds() != 0L) {
				buffer.append("&" + name + ":next=") //$NON-NLS-1$
						.append(createRelativeDateString(filter.getNextRangeType(), filter.getNextCount()));
			}
		}

		return buffer.toString();
	}

	private String createRelativeDateString(RelativeDateRangeFilter.RangeType rangeType, long count) {
		StringBuffer dateString = new StringBuffer(""); //$NON-NLS-1$
		dateString.append(Long.toString(count));

		if (RangeType.MINUTE.equals(rangeType)) {
			dateString.append('m');
		} else if (RangeType.HOUR.equals(rangeType)) {
			dateString.append('h');
		} else if (RangeType.DAY.equals(rangeType)) {
			dateString.append('d');
		} else if (RangeType.WEEK.equals(rangeType)) {
			dateString.append('w');
		}

		return dateString.toString();
	}

	// TODO there should be an easier way of doing this
	// Would it be so bad to have the field name in the field?
	protected String getNameFromField(Order.Field field) {
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
