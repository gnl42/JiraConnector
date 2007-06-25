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

package org.eclipse.mylyn.internal.jira.core.model.filter;

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
	 *            content beindg searched for
	 * @param searchSummary
	 *            Search the issue's summary for matches of <code>queryString</code>
	 * @param searchDescription
	 *            Search the issue's description for matches of <code>queryString</code>
	 * @param searchEnvironment
	 *            Search the issue's environment for matches of </code>queryString</code>
	 * @param searchComments
	 *            Seatch the issue's comments for matches of </code>queryString</code>
	 */
	public ContentFilter(String queryString, boolean searchSummary, boolean searchDescription,
			boolean searchEnvironment, boolean searchComments) {
		assert (queryString != null);
		this.queryString = queryString;
		this.searchingComments = searchComments;
		this.searchingDescription = searchDescription;
		this.searchingSummary = searchSummary;
		this.searchingEnvironment = searchEnvironment;
	}

	public String getQueryString() {
		return this.queryString;
	}

	public boolean isSearchingComments() {
		return this.searchingComments;
	}

	public boolean isSearchingDescription() {
		return this.searchingDescription;
	}

	public boolean isSearchingEnvironment() {
		return this.searchingEnvironment;
	}

	public boolean isSearchingSummary() {
		return this.searchingSummary;
	}

	ContentFilter copy() {
		return new ContentFilter(queryString, searchingSummary, searchingDescription, searchingEnvironment,
				searchingComments);
	}
}
