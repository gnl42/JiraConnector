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

package org.eclipse.mylyn.internal.jira.core.service.web.rss;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.Status;
import org.eclipse.mylyn.internal.jira.core.model.filter.DateFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.DateRangeFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.PriorityFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.RelativeDateRangeFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ResolutionFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.StatusFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.RelativeDateRangeFilter.RangeType;

/**
 * @author Brock Janiczak
 */
public class Jira33RssFilterConverter extends RssFilterConverter {
	private final String DATE_FORMAT = "dd-MMM-yyyy"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylyn.internal.jira.core.service.web.rss.RssFilterConverter#convertResolutionFilter(org.eclipse.mylyn.internal.jira.core.model.filter.ResolutionFilter)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylyn.internal.jira.core.service.web.rss.RssFilterConverter#convertPriorityFilter(org.eclipse.mylyn.internal.jira.core.model.filter.PriorityFilter)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylyn.internal.jira.core.service.web.rss.RssFilterConverter#convertStatusFilter(org.eclipse.mylyn.internal.jira.core.model.filter.StatusFilter)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylyn.internal.jira.core.service.web.rss.RssFilterConverter#convertCreatedDateFilter(org.eclipse.mylyn.internal.jira.core.model.filter.DateFilter)
	 */
	protected String convertCreatedDateFilter(DateFilter createdDateFilter) {
		return createDateFilder(createdDateFilter, "created");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylyn.internal.jira.core.service.web.rss.RssFilterConverter#convertUpdatedDateFilter(org.eclipse.mylyn.internal.jira.core.model.filter.DateFilter)
	 */
	protected String convertUpdatedDateFilter(DateFilter updatedDateFilter) {
		return createDateFilder(updatedDateFilter, "updated");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylyn.internal.jira.core.service.web.rss.RssFilterConverter#convertDueDateFilter(org.eclipse.mylyn.internal.jira.core.model.filter.DateFilter)
	 */
	protected String convertDueDateFilter(DateFilter dueDateFilter) {
		return createDateFilder(dueDateFilter, "duedate");
	}

	private String createDateFilder(DateFilter dateFilter, String name) {
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
}
