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

import java.io.Serializable;

/**
 * This filter will restrict matches to issues that have either a summary, description, environment or comment matching
 * the specified query string.
 *
 * @author Brock Janiczak
 */
public class ContentFilter implements Filter, Serializable {
    private static final long serialVersionUID = 1L;

    private final String queryString;

    private final boolean searchingSummary;

    private final boolean searchingDescription;

    private final boolean searchingComments;

    private final boolean searchingEnvironment;

    /**
     *
     * @param queryString
     *            content being searched for
     * @param searchSummary
     *            Search the issue's summary for matches of <code>queryString</code>
     * @param searchDescription
     *            Search the issue's description for matches of <code>queryString</code>
     * @param searchEnvironment
     *            Search the issue's environment for matches of </code>queryString</code>
     * @param searchComments
     *            Search the issue's comments for matches of </code>queryString</code>
     */
    public ContentFilter(final String queryString, final boolean searchSummary, final boolean searchDescription,
            final boolean searchEnvironment, final boolean searchComments) {
        assert queryString != null;
        this.queryString = queryString;
        searchingComments = searchComments;
        searchingDescription = searchDescription;
        searchingSummary = searchSummary;
        searchingEnvironment = searchEnvironment;
    }

    public String getQueryString() {
        return queryString;
    }

    public boolean isSearchingComments() {
        return searchingComments;
    }

    public boolean isSearchingDescription() {
        return searchingDescription;
    }

    public boolean isSearchingEnvironment() {
        return searchingEnvironment;
    }

    public boolean isSearchingSummary() {
        return searchingSummary;
    }

    ContentFilter copy() {
        return new ContentFilter(queryString, searchingSummary, searchingDescription, searchingEnvironment,
                searchingComments);
    }
}
