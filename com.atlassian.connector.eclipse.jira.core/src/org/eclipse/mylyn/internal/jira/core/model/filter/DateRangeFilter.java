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
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.model.filter;

import java.util.Date;

/**
 * @author Brock Janiczak
 */
public class DateRangeFilter extends DateFilter {

	private static final long serialVersionUID = 1L;

	private final Date fromDate;

	private final Date toDate;

	private final String from;

	private final String to;

	public DateRangeFilter(Date fromDate, Date toDate) {
		this(fromDate, toDate, null, null);
	}

	public DateRangeFilter(Date fromDate, Date toDate, String from, String to) {
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.from = from;
		this.to = to;
	}

	public Date getFromDate() {
		return this.fromDate;
	}

	public Date getToDate() {
		return this.toDate;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	@Override
	DateFilter copy() {
		return new DateRangeFilter(fromDate, toDate, from, to);
	}
}
