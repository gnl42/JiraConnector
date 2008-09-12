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

import java.io.Serializable;

import org.eclipse.mylyn.internal.jira.core.model.Project;

/**
 * Filter to restrict returned issues to a specific project. If a project filter is used you can add project specific
 * filters such as {@link ComponentFilter}, {@link ReportedInVersionFilter} and {@link FixForVersionFilter}
 * 
 * @see com.gbst.jira.core.model.filter.ComponentFilter
 * @see com.gbst.jira.core.model.filter.ReportedInVersionFilter
 * @see com.gbst.jira.core.model.filter.FixForVersionFilter
 * 
 * @author Brock Janiczak
 */
public class ProjectFilter implements Filter, Serializable {
	private static final long serialVersionUID = 1L;

	private final Project project;

	public ProjectFilter(Project project) {
		assert (project != null);
		this.project = project;
	}

	public Project getProject() {
		return this.project;
	}

	ProjectFilter copy() {
		return new ProjectFilter(this.project);
	}
}
