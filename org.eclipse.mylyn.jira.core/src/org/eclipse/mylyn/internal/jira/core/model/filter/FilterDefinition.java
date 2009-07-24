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
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.model.filter;

import java.io.Serializable;

import org.eclipse.core.runtime.Assert;
import org.eclipse.mylyn.internal.jira.core.model.JiraFilter;

/*
 * TODO convert this into a factory. Have methods like addProjectFilter this way
 * we can do checks on what can/can't be added. Also make sure the filter is set
 * up correctly! start with an empty definition then add filters TODO also need
 * to have methods for getting the individual filters. It won't support anything
 * smart. Only the basic filters are supported for now could probabyl even
 * remove the filter super class
 */
/**
 * @author Brock Janiczak
 */
public class FilterDefinition implements JiraFilter, Serializable {
	private static final long serialVersionUID = 1L;

	private ProjectFilter projectFilter;

	private ComponentFilter componentFilter;

	private ContentFilter contentFilter;

	private IssueTypeFilter issueTypeFilter;

	private UserFilter assignedToFilter;

	private UserFilter reportedByFilter;

	private PriorityFilter priorityFilter;

	private StatusFilter statusFilter;

	private ResolutionFilter resolutionFilter;

	private VersionFilter reportedInVersionFilter;

	private VersionFilter fixForVersionFilter;

	private EstimateVsActualFilter estimateVsActualFilter;

	private DateFilter updatedDateFilter;

	private DateFilter createdDateFilter;

	private DateFilter dueDateFilter;

	private Order[] ordering = new Order[0];

	public FilterDefinition() {
	}

	public void copyAttributes(FilterDefinition copy) {
		if (copy.assignedToFilter != null) {
			this.assignedToFilter = copy.assignedToFilter.copy();
		} else {
			this.assignedToFilter = null;
		}

		if (copy.componentFilter != null) {
			this.componentFilter = copy.componentFilter.copy();
		} else {
			this.componentFilter = null;
		}

		if (copy.contentFilter != null) {
			this.contentFilter = copy.contentFilter.copy();
		} else {
			this.contentFilter = null;
		}

		if (copy.createdDateFilter != null) {
			this.createdDateFilter = copy.createdDateFilter.copy();
		} else {
			this.createdDateFilter = null;
		}

		if (copy.dueDateFilter != null) {
			this.dueDateFilter = copy.dueDateFilter.copy();
		} else {
			this.dueDateFilter = null;
		}

		if (copy.estimateVsActualFilter != null) {
			this.estimateVsActualFilter = copy.estimateVsActualFilter.copy();
		} else {
			this.estimateVsActualFilter = null;
		}

		if (copy.fixForVersionFilter != null) {
			this.fixForVersionFilter = copy.fixForVersionFilter.copy();
		} else {
			this.fixForVersionFilter = null;
		}

		if (copy.issueTypeFilter != null) {
			this.issueTypeFilter = copy.issueTypeFilter.copy();
		} else {
			this.issueTypeFilter = null;
		}

		if (copy.priorityFilter != null) {
			this.priorityFilter = copy.priorityFilter.copy();
		} else {
			this.priorityFilter = null;
		}

		if (copy.projectFilter != null) {
			this.projectFilter = copy.projectFilter.copy();
		} else {
			this.projectFilter = null;
		}

		if (copy.reportedByFilter != null) {
			this.reportedByFilter = copy.reportedByFilter.copy();
		} else {
			this.reportedByFilter = null;
		}

		if (copy.reportedInVersionFilter != null) {
			this.reportedInVersionFilter = copy.reportedInVersionFilter.copy();
		} else {
			this.reportedInVersionFilter = null;
		}

		if (copy.resolutionFilter != null) {
			this.resolutionFilter = copy.resolutionFilter.copy();
		} else {
			this.resolutionFilter = null;
		}

		if (copy.statusFilter != null) {
			this.statusFilter = copy.statusFilter.copy();
		} else {
			this.statusFilter = null;
		}

		if (copy.updatedDateFilter != null) {
			this.updatedDateFilter = copy.updatedDateFilter.copy();
		} else {
			this.updatedDateFilter = null;
		}
	}

	/**
	 * Copy constructor for cloning filter definitions
	 * 
	 * @param copy
	 *            Filter definition to copy
	 */
	public FilterDefinition(FilterDefinition copy) {
		copyAttributes(copy);
	}

	public void setProjectFilter(ProjectFilter projectFilter) {
		this.projectFilter = projectFilter;
	}

	public ProjectFilter getProjectFilter() {
		return projectFilter;
	}

	public void setComponentFilter(ComponentFilter componentFilter) {
		this.componentFilter = componentFilter;
	}

	public ComponentFilter getComponentFilter() {
		return componentFilter;
	}

	public void setContentFilter(ContentFilter contentFilter) {
		this.contentFilter = contentFilter;
	}

	public ContentFilter getContentFilter() {
		return contentFilter;
	}

	public void setIssueTypeFilter(IssueTypeFilter issueTypeFilter) {
		this.issueTypeFilter = issueTypeFilter;
	}

	public IssueTypeFilter getIssueTypeFilter() {
		return issueTypeFilter;
	}

	public void setAssignedToFilter(UserFilter assignedToFilter) {
		this.assignedToFilter = assignedToFilter;
	}

	public UserFilter getAssignedToFilter() {
		return assignedToFilter;
	}

	public UserFilter getReportedByFilter() {
		return this.reportedByFilter;
	}

	public void setReportedByFilter(UserFilter reportedByFilter) {
		this.reportedByFilter = reportedByFilter;
	}

	public void setPriorityFilter(PriorityFilter priorityFilter) {
		this.priorityFilter = priorityFilter;
	}

	public PriorityFilter getPriorityFilter() {
		return priorityFilter;
	}

	public void setStatusFilter(StatusFilter statusFilter) {
		this.statusFilter = statusFilter;
	}

	public StatusFilter getStatusFilter() {
		return statusFilter;
	}

	public void setResolutionFilter(ResolutionFilter resolutionFilter) {
		this.resolutionFilter = resolutionFilter;
	}

	public ResolutionFilter getResolutionFilter() {
		return resolutionFilter;
	}

	public void setReportedInVersionFilter(VersionFilter reportedInVersionFilter) {
		this.reportedInVersionFilter = reportedInVersionFilter;
	}

	public VersionFilter getReportedInVersionFilter() {
		return reportedInVersionFilter;
	}

	public void setFixForVersionFilter(VersionFilter fixForVersionFilter) {
		this.fixForVersionFilter = fixForVersionFilter;
	}

	public VersionFilter getFixForVersionFilter() {
		return fixForVersionFilter;
	}

	public EstimateVsActualFilter getEstimateVsActualFilter() {
		return this.estimateVsActualFilter;
	}

	public void setEstimateVsActualFilter(EstimateVsActualFilter estimateVsActualFilter) {
		this.estimateVsActualFilter = estimateVsActualFilter;
	}

	public DateFilter getCreatedDateFilter() {
		return this.createdDateFilter;
	}

	public void setCreatedDateFilter(DateFilter createdDateFilter) {
		this.createdDateFilter = createdDateFilter;
	}

	public DateFilter getDueDateFilter() {
		return this.dueDateFilter;
	}

	public void setDueDateFilter(DateFilter dueDateFilter) {
		this.dueDateFilter = dueDateFilter;
	}

	public DateFilter getUpdatedDateFilter() {
		return this.updatedDateFilter;
	}

	public void setUpdatedDateFilter(DateFilter updatedDateFilter) {
		this.updatedDateFilter = updatedDateFilter;
	}

	public Order[] getOrdering() {
		return this.ordering;
	}

	public void setOrdering(Order[] ordering) {
		Assert.isNotNull(ordering);
		this.ordering = ordering;
	}

}