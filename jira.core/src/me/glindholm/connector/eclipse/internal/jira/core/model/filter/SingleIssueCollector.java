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
public final class SingleIssueCollector implements IssueCollector {

    private JiraIssue matchingIssue;

    public JiraIssue getIssue() {
        return matchingIssue;
    }

    @Override
    public void done() {
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void collectIssue(final JiraIssue issue) {
        matchingIssue = issue;
    }

    @Override
    public void start() {
    }

    @Override
    public int getMaxHits() {
        return 1;
    }
}