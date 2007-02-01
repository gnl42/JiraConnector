/*******************************************************************************
 * Copyright (c) 2007 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.core.service.web.rss;

import java.text.SimpleDateFormat;

import org.eclipse.mylar.internal.jira.core.model.Priority;
import org.eclipse.mylar.internal.jira.core.model.Resolution;
import org.eclipse.mylar.internal.jira.core.model.Status;
import org.eclipse.mylar.internal.jira.core.model.filter.DateFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.DateRangeFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.PriorityFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.RelativeDateRangeFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.ResolutionFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.StatusFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.RelativeDateRangeFilter.RangeType;

/**
 * @author Brock Janiczak
 */
public class Jira33RssFilterConverter extends RssFilterConverter {
	private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy"); //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.web.rss.RssFilterConverter#convertResolutionFilter(org.eclipse.mylar.internal.jira.core.model.filter.ResolutionFilter)
	 */
	protected String convertResolutionFilter(ResolutionFilter resolutionFilter) {
		if (resolutionFilter.isUnresolved()) {
			return "resolution=-1"; //$NON-NLS-1$
		}

		StringBuffer buffer = new StringBuffer();
		Resolution[] resolution = resolutionFilter.getResolutions();
		if (resolution.length == 0) {
			return ""; //$NON-NLS-1$
		}

		buffer.append("resolution=").append(resolution[0].getId()); //$NON-NLS-1$

		for (int i = 1; i < resolution.length; i++) {
			buffer.append("&resolution=").append(resolution[i].getId()); //$NON-NLS-1$
		}

		return buffer.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.web.rss.RssFilterConverter#convertPriorityFilter(org.eclipse.mylar.internal.jira.core.model.filter.PriorityFilter)
	 */
	protected String convertPriorityFilter(PriorityFilter priorityFilter) {
		StringBuffer buffer = new StringBuffer();
		Priority[] priorities = priorityFilter.getPriorities();
		if (priorities.length == 0) {
			return ""; //$NON-NLS-1$
		}

		buffer.append("priority=").append(priorities[0].getId()); //$NON-NLS-1$

		for (int i = 1; i < priorities.length; i++) {
			buffer.append("&priority=").append(priorities[i].getId()); //$NON-NLS-1$
		}

		return buffer.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.web.rss.RssFilterConverter#convertStatusFilter(org.eclipse.mylar.internal.jira.core.model.filter.StatusFilter)
	 */
	protected String convertStatusFilter(StatusFilter statusFilter) {
		StringBuffer buffer = new StringBuffer();
		Status[] statuses = statusFilter.getStatuses();
		if (statuses.length == 0) {
			return ""; //$NON-NLS-1$
		}

		buffer.append("status=").append(statuses[0].getId()); //$NON-NLS-1$

		for (int i = 1; i < statuses.length; i++) {
			buffer.append("&status=").append(statuses[i].getId()); //$NON-NLS-1$
		}

		return buffer.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.web.rss.RssFilterConverter#convertCreatedDateFilter(org.eclipse.mylar.internal.jira.core.model.filter.DateFilter)
	 */
	protected String convertCreatedDateFilter(DateFilter createdDateFilter) {
		StringBuffer buffer = new StringBuffer();

		if (createdDateFilter instanceof DateRangeFilter) {
			DateRangeFilter filter = (DateRangeFilter) createdDateFilter;
			if (filter.getFromDate() != null) {
				buffer.append("&created:after=").append(DATE_FORMAT.format(filter.getFromDate())); //$NON-NLS-1$
			}

			if (filter.getToDate() != null) {
				buffer.append("&created:before=").append(DATE_FORMAT.format(filter.getToDate())); //$NON-NLS-1$
			}

		} else if (createdDateFilter instanceof RelativeDateRangeFilter) {
			RelativeDateRangeFilter relativeFilter = ((RelativeDateRangeFilter) createdDateFilter);
			if (relativeFilter.previousMilliseconds() != 0L) {
				buffer
						.append("&created:previous=").append(createRelativeDateString(relativeFilter.getPreviousRangeType(), relativeFilter.getPreviousCount())); //$NON-NLS-1$
			}

			if (relativeFilter.nextMilliseconds() != 0L) {
				buffer
						.append("&created:next=").append(createRelativeDateString(relativeFilter.getNextRangeType(), relativeFilter.getNextCount())); //$NON-NLS-1$
			}
		}

		return buffer.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.web.rss.RssFilterConverter#convertUpdatedDateFilter(org.eclipse.mylar.internal.jira.core.model.filter.DateFilter)
	 */
	protected String convertUpdatedDateFilter(DateFilter updatedDateFilter) {
		StringBuffer buffer = new StringBuffer();

		if (updatedDateFilter instanceof DateRangeFilter) {
			DateRangeFilter filter = (DateRangeFilter) updatedDateFilter;
			if (filter.getFromDate() != null) {
				buffer.append("&updated:after=").append(DATE_FORMAT.format(filter.getFromDate())); //$NON-NLS-1$
			}

			if (filter.getToDate() != null) {
				buffer.append("&updated:before=").append(DATE_FORMAT.format(filter.getToDate())); //$NON-NLS-1$
			}

		} else if (updatedDateFilter instanceof RelativeDateRangeFilter) {
			RelativeDateRangeFilter relativeFilter = ((RelativeDateRangeFilter) updatedDateFilter);
			if (relativeFilter.previousMilliseconds() != 0L) {
				buffer
						.append("&updated:previous=").append(createRelativeDateString(relativeFilter.getPreviousRangeType(), relativeFilter.getPreviousCount())); //$NON-NLS-1$
			}

			if (relativeFilter.nextMilliseconds() != 0L) {
				buffer
						.append("&updated:next=").append(createRelativeDateString(relativeFilter.getNextRangeType(), relativeFilter.getNextCount())); //$NON-NLS-1$
			}
		}

		return buffer.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.web.rss.RssFilterConverter#convertDueDateFilter(org.eclipse.mylar.internal.jira.core.model.filter.DateFilter)
	 */
	protected String convertDueDateFilter(DateFilter dueDateFilter) {
		StringBuffer buffer = new StringBuffer();

		if (dueDateFilter instanceof DateRangeFilter) {
			DateRangeFilter filter = (DateRangeFilter) dueDateFilter;
			if (filter.getFromDate() != null) {
				buffer.append("&duedate:after=").append(DATE_FORMAT.format(filter.getFromDate())); //$NON-NLS-1$
			}

			if (filter.getToDate() != null) {
				buffer.append("&duedate:before=").append(DATE_FORMAT.format(filter.getToDate())); //$NON-NLS-1$
			}

		} else if (dueDateFilter instanceof RelativeDateRangeFilter) {
			RelativeDateRangeFilter relativeFilter = ((RelativeDateRangeFilter) dueDateFilter);
			if (relativeFilter.previousMilliseconds() != 0L) {
				buffer
						.append("&duedate:previous=").append(createRelativeDateString(relativeFilter.getPreviousRangeType(), relativeFilter.getPreviousCount())); //$NON-NLS-1$
			}

			if (relativeFilter.nextMilliseconds() != 0L) {
				buffer
						.append("&duedate:next=").append(createRelativeDateString(relativeFilter.getNextRangeType(), relativeFilter.getNextCount())); //$NON-NLS-1$
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
}
