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

package me.glindholm.connector.eclipse.internal.jira.core.model.filter;

import java.io.Serializable;

import org.eclipse.core.runtime.Assert;

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraFilter;

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

    private Order[] ordering = {};

    public FilterDefinition() {
    }

    public void copyAttributes(final FilterDefinition copy) {
        if (copy.assignedToFilter != null) {
            assignedToFilter = copy.assignedToFilter.copy();
        } else {
            assignedToFilter = null;
        }

        if (copy.componentFilter != null) {
            componentFilter = copy.componentFilter.copy();
        } else {
            componentFilter = null;
        }

        if (copy.contentFilter != null) {
            contentFilter = copy.contentFilter.copy();
        } else {
            contentFilter = null;
        }

        if (copy.createdDateFilter != null) {
            createdDateFilter = copy.createdDateFilter.copy();
        } else {
            createdDateFilter = null;
        }

        if (copy.dueDateFilter != null) {
            dueDateFilter = copy.dueDateFilter.copy();
        } else {
            dueDateFilter = null;
        }

        if (copy.estimateVsActualFilter != null) {
            estimateVsActualFilter = copy.estimateVsActualFilter.copy();
        } else {
            estimateVsActualFilter = null;
        }

        if (copy.fixForVersionFilter != null) {
            fixForVersionFilter = copy.fixForVersionFilter.copy();
        } else {
            fixForVersionFilter = null;
        }

        if (copy.issueTypeFilter != null) {
            issueTypeFilter = copy.issueTypeFilter.copy();
        } else {
            issueTypeFilter = null;
        }

        if (copy.priorityFilter != null) {
            priorityFilter = copy.priorityFilter.copy();
        } else {
            priorityFilter = null;
        }

        if (copy.projectFilter != null) {
            projectFilter = copy.projectFilter.copy();
        } else {
            projectFilter = null;
        }

        if (copy.reportedByFilter != null) {
            reportedByFilter = copy.reportedByFilter.copy();
        } else {
            reportedByFilter = null;
        }

        if (copy.reportedInVersionFilter != null) {
            reportedInVersionFilter = copy.reportedInVersionFilter.copy();
        } else {
            reportedInVersionFilter = null;
        }

        if (copy.resolutionFilter != null) {
            resolutionFilter = copy.resolutionFilter.copy();
        } else {
            resolutionFilter = null;
        }

        if (copy.statusFilter != null) {
            statusFilter = copy.statusFilter.copy();
        } else {
            statusFilter = null;
        }

        if (copy.updatedDateFilter != null) {
            updatedDateFilter = copy.updatedDateFilter.copy();
        } else {
            updatedDateFilter = null;
        }
    }

    /**
     * Copy constructor for cloning filter definitions
     *
     * @param copy Filter definition to copy
     */
    public FilterDefinition(final FilterDefinition copy) {
        copyAttributes(copy);
    }

    public void setProjectFilter(final ProjectFilter projectFilter) {
        this.projectFilter = projectFilter;
    }

    public ProjectFilter getProjectFilter() {
        return projectFilter;
    }

    public void setComponentFilter(final ComponentFilter componentFilter) {
        this.componentFilter = componentFilter;
    }

    public ComponentFilter getComponentFilter() {
        return componentFilter;
    }

    public void setContentFilter(final ContentFilter contentFilter) {
        this.contentFilter = contentFilter;
    }

    public ContentFilter getContentFilter() {
        return contentFilter;
    }

    public void setIssueTypeFilter(final IssueTypeFilter issueTypeFilter) {
        this.issueTypeFilter = issueTypeFilter;
    }

    public IssueTypeFilter getIssueTypeFilter() {
        return issueTypeFilter;
    }

    public void setAssignedToFilter(final UserFilter assignedToFilter) {
        this.assignedToFilter = assignedToFilter;
    }

    public UserFilter getAssignedToFilter() {
        return assignedToFilter;
    }

    public UserFilter getReportedByFilter() {
        return reportedByFilter;
    }

    public void setReportedByFilter(final UserFilter reportedByFilter) {
        this.reportedByFilter = reportedByFilter;
    }

    public void setPriorityFilter(final PriorityFilter priorityFilter) {
        this.priorityFilter = priorityFilter;
    }

    public PriorityFilter getPriorityFilter() {
        return priorityFilter;
    }

    public void setStatusFilter(final StatusFilter statusFilter) {
        this.statusFilter = statusFilter;
    }

    public StatusFilter getStatusFilter() {
        return statusFilter;
    }

    public void setResolutionFilter(final ResolutionFilter resolutionFilter) {
        this.resolutionFilter = resolutionFilter;
    }

    public ResolutionFilter getResolutionFilter() {
        return resolutionFilter;
    }

    public void setReportedInVersionFilter(final VersionFilter reportedInVersionFilter) {
        this.reportedInVersionFilter = reportedInVersionFilter;
    }

    public VersionFilter getReportedInVersionFilter() {
        return reportedInVersionFilter;
    }

    public void setFixForVersionFilter(final VersionFilter fixForVersionFilter) {
        this.fixForVersionFilter = fixForVersionFilter;
    }

    public VersionFilter getFixForVersionFilter() {
        return fixForVersionFilter;
    }

    public EstimateVsActualFilter getEstimateVsActualFilter() {
        return estimateVsActualFilter;
    }

    public void setEstimateVsActualFilter(final EstimateVsActualFilter estimateVsActualFilter) {
        this.estimateVsActualFilter = estimateVsActualFilter;
    }

    public DateFilter getCreatedDateFilter() {
        return createdDateFilter;
    }

    public void setCreatedDateFilter(final DateFilter createdDateFilter) {
        this.createdDateFilter = createdDateFilter;
    }

    public DateFilter getDueDateFilter() {
        return dueDateFilter;
    }

    public void setDueDateFilter(final DateFilter dueDateFilter) {
        this.dueDateFilter = dueDateFilter;
    }

    public DateFilter getUpdatedDateFilter() {
        return updatedDateFilter;
    }

    public void setUpdatedDateFilter(final DateFilter updatedDateFilter) {
        this.updatedDateFilter = updatedDateFilter;
    }

    public Order[] getOrdering() {
        return ordering;
    }

    public void setOrdering(final Order[] ordering) {
        Assert.isNotNull(ordering);
        this.ordering = ordering;
    }

}