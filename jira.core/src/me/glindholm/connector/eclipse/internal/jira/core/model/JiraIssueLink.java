/*******************************************************************************
 * Copyright (c) 2004, 2008 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Eugene Kuleshov - improvements
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core.model;

import java.io.Serializable;

/**
 * @author Brock Janiczak
 * @author Eugene Kuleshov
 */
public class JiraIssueLink implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String issueId;

    private final String issueKey;

    private final String linkTypeId;

    private final String linkName;

    private final String inwardDescription;

    private final String outwardDescription;

    public JiraIssueLink(final String issueId, final String issueKey, final String linkTypeId, //
            final String linkName, final String inwardDescription, final String outwardDescription) {
        this.issueId = issueId;
        this.issueKey = issueKey;
        this.linkTypeId = linkTypeId;
        this.linkName = linkName;
        this.inwardDescription = inwardDescription;
        this.outwardDescription = outwardDescription;
    }

    public String getIssueId() {
        return issueId;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public String getLinkTypeId() {
        return linkTypeId;
    }

    public String getLinkName() {
        return linkName;
    }

    public String getInwardDescription() {
        return inwardDescription;
    }

    public String getOutwardDescription() {
        return outwardDescription;
    }

}
