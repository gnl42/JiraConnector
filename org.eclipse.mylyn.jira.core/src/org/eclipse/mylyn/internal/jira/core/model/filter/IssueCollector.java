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

import org.eclipse.mylyn.internal.jira.core.model.Issue;

/**
 * @author Brock Janiczak
 */
public interface IssueCollector {

	public static final int NO_LIMIT = -1;

	/**
	 * Issues will start arriving soon. Do any setup that is required
	 */
	public void start();

	public void collectIssue(Issue issue);

	/**
	 * Determine if the collector doesn't want to receive issue notifications anymore
	 * 
	 * @return <code>true</code> if the collector does not wish to be notified of new issues
	 */
	public boolean isCancelled();

	/**
	 * This method will be called by the issue processor when it has finished processing all of the issues. It is a hint
	 * to the collector that there will be no more data.
	 */
	public void done();

	/**
	 * If the server only supports inefficient mechanisims for getting issues the user can choose to limit the number of
	 * matches. This is only used as a hint.
	 * 
	 * @return Maximum number of matches to return or<code>NO_LIMIT</code> if there is no limit.
	 */
	public int getMaxHits();

}
