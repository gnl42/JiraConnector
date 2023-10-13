/*******************************************************************************
 * Copyright (c) 2004, 2009 Brock Janiczak and others.
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

import org.eclipse.core.runtime.Assert;

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProject;

/**
 * Filter to restrict returned issues to a specific project. If a project filter is used you can add project specific
 * filters such as {@link ComponentFilter}, {@link ReportedInVersionFilter} and {@link FixForVersionFilter}
 *
 * @see com.gbst.jira.core.model.filter.ComponentFilter
 * @see com.gbst.jira.core.model.filter.ReportedInVersionFilter
 * @see com.gbst.jira.core.model.filter.FixForVersionFilter
 *
 * @author Brock Janiczak
 * @author Thomas Ehrnhoefer (multiple projects selection)
 */
public class ProjectFilter implements Filter, Serializable {
    private static final long serialVersionUID = 1L;

    private final JiraProject[] projects;

    public ProjectFilter(final JiraProject[] projects) {
        Assert.isNotNull(projects);
        Assert.isTrue(projects.length > 0);
        for (final JiraProject project : projects) {
            Assert.isNotNull(project);
        }
        this.projects = projects;
    }

    public ProjectFilter(final JiraProject project) {
        this(new JiraProject[] { project });
    }

    public JiraProject[] getProjects() {
        return projects;
    }

    ProjectFilter copy() {
        return new ProjectFilter(projects);
    }
}
