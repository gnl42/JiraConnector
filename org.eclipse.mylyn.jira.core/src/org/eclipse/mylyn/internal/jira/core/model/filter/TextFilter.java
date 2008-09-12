/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
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

import org.eclipse.mylyn.internal.jira.core.model.JiraFilter;

/**
 * Query object that holds the query value that will be passed to the server. TODO Possibly allow the user to construct
 * this object without having to know the syntax
 * 
 * @author Brock Janiczak
 */
public class TextFilter implements JiraFilter {

	private final String keywords;

	public TextFilter(String keywords) {
		this.keywords = keywords;
	}

	/**
	 * @return the keywords
	 */
	public String getKeywords() {
		return this.keywords;
	}
}
