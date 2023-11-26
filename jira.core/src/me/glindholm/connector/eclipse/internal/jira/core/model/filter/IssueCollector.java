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

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssue;

/**
 * @author Brock Janiczak
 */
public interface IssueCollector {

    int NO_LIMIT = -1;

    /**
     * Issues will start arriving soon. Do any setup that is required
     */
    void start();

    void collectIssue(JiraIssue issue);

    /**
     * Determine if the collector doesn't want to receive issue notifications anymore
     *
     * @return <code>true</code> if the collector does not wish to be notified of new issues
     */
    boolean isCancelled();

    /**
     * This method will be called by the issue processor when it has finished processing all of the
     * issues. It is a hint to the collector that there will be no more data.
     */
    void done();

    /**
     * If the server only supports inefficient mechanisims for getting issues the user can choose to
     * limit the number of matches. This is only used as a hint.
     *
     * @return Maximum number of matches to return or<code>NO_LIMIT</code> if there is no limit.
     */
    int getMaxHits();

}
