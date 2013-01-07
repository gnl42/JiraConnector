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

package com.atlassian.connector.eclipse.internal.jira.core.model;

import java.io.Serializable;

import org.eclipse.core.runtime.Assert;

/**
 * @author Brock Janiczak
 */
public class IssueType implements Serializable {

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

	public IssueType(String id, String name, String description, String icon) {
		this(id, name, false);
		this.name = name;
		this.description = description;
		this.icon = icon;
	}

	public IssueType(String id, String name, boolean subTaskType) {
		Assert.isNotNull(id);
		Assert.isNotNull(name);
		this.id = id;
		this.name = name;
		this.subTaskType = subTaskType;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIcon() {
		return this.icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSubTaskType() {
		return subTaskType;
	}

	public void setSubTaskType(boolean subTaskType) {
		this.subTaskType = subTaskType;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (!(obj instanceof IssueType)) {
			return false;
		}

		IssueType that = (IssueType) obj;

		return this.id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return this.name;
	}
}
