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

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueType;

// TODO consider making this abstract and using subclasses to do the typing
/**
 * @author Brock Janiczak
 */
public class IssueTypeFilter implements Filter, Serializable {
    private static final long serialVersionUID = 1L;

    private final JiraIssueType[] issueTypes;

    private final boolean standardTypes;

    private final boolean subTaskTypes;

    public IssueTypeFilter(final JiraIssueType[] issueTypes) {
        this.issueTypes = issueTypes;
        standardTypes = false;
        subTaskTypes = false;
    }

    public IssueTypeFilter(final boolean standardTypes, final boolean subTaskTypes) {
        assert standardTypes ^ subTaskTypes;

        issueTypes = null;
        this.standardTypes = standardTypes;
        this.subTaskTypes = subTaskTypes;
    }

    public JiraIssueType[] getIsueTypes() {
        return issueTypes;
    }

    public boolean isStandardTypes() {
        return standardTypes;
    }

    public boolean isSubTaskTypes() {
        return subTaskTypes;
    }

    IssueTypeFilter copy() {
        if (issueTypes != null) {
            return new IssueTypeFilter(issueTypes);
        }

        return new IssueTypeFilter(standardTypes, subTaskTypes);
    }

}