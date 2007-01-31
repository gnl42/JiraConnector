/*******************************************************************************
 * Copyright (c) 2005 Jira Dashboard project.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylar.jira.core.internal.model.filter;

import java.util.Date;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.model.filter.DateFilter#copy(com.gbst.jira.core.model.filter.DateFilter)
	 */
	DateFilter copy() {
		return new DateRangeFilter(fromDate, toDate);
	}
}
