/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.model.filter;

import java.util.Date;

/**
 * @author Brock Janiczak
 */
public class DateRangeFilter extends DateFilter {

	private static final long serialVersionUID = 1L;

	private Date fromDate;

	private Date toDate;

	public DateRangeFilter(Date fromDate, Date toDate) {
		this.fromDate = fromDate;
		this.toDate = toDate;
	}

	public Date getFromDate() {
		return this.fromDate;
	}

	public Date getToDate() {
		return this.toDate;
	}

	@Override
	DateFilter copy() {
		return new DateRangeFilter(fromDate, toDate);
	}
}
