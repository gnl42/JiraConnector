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

package me.glindholm.connector.eclipse.internal.jira.core.model.filter;

import java.time.Instant;

/**
 * @author Brock Janiczak
 */
public class DateRangeFilter extends DateFilter {

    private static final long serialVersionUID = 1L;

    private final Instant fromDate;

    private final Instant toDate;

    private final String from;

    private final String to;

    public DateRangeFilter(final Instant fromDate, final Instant toDate) {
        this(fromDate, toDate, null, null);
    }

    public DateRangeFilter(final Instant fromDate, final Instant toDate, final String from, final String to) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.from = from;
        this.to = to;
    }

    public Instant getFromDate() {
        return fromDate;
    }

    public Instant getToDate() {
        return toDate;
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
