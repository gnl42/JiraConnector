/*******************************************************************************
 * Copyright (c) 2004, 2008 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core.model;

import java.io.Serializable;

import org.eclipse.core.runtime.Assert;

/**
 * @author Brock Janiczak
 */
public class JiraIssueType implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String BUG_ID = "1"; //$NON-NLS-1$

    public static final String NEW_FEATURE_ID = "2"; //$NON-NLS-1$

    public static final String TASK_ID = "3"; //$NON-NLS-1$

    public static final String IMPROVEMENT_ID = "4"; //$NON-NLS-1$

    private String id;

    private String name;

    private String description;

    private String icon;

    private boolean subTaskType;

    public JiraIssueType(final String id, final String name, final String description, final String icon) {
        this(id, name, false);
        this.name = name;
        this.description = description;
        this.icon = icon;
    }

    public JiraIssueType(final String id, final String name, final boolean subTaskType) {
        Assert.isNotNull(id);
        Assert.isNotNull(name);
        this.id = id;
        this.name = name;
        this.subTaskType = subTaskType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(final String icon) {
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isSubTaskType() {
        return subTaskType;
    }

    public void setSubTaskType(final boolean subTaskType) {
        this.subTaskType = subTaskType;
    }

    @Override
    public boolean equals(final Object obj) {
        if ((obj == null) || !(obj instanceof JiraIssueType)) {
            return false;
        }

        final JiraIssueType that = (JiraIssueType) obj;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
