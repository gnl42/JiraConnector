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

package org.eclipse.mylyn.internal.jira.core.model;

import java.io.Serializable;

/**
 * @author	Brock Janiczak
 */
public class Status implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String OPEN_ID = "1"; //$NON-NLS-1$

	public static final String ASSIGNED_ID = "2"; //$NON-NLS-1$

	public static final String STARTED_ID = "3"; //$NON-NLS-1$

	public static final String REOPENED_ID = "4"; //$NON-NLS-1$

	public static final String RESOLVED_ID = "5"; //$NON-NLS-1$

	public static final String CLOSED_ID = "6"; //$NON-NLS-1$

	private String id;

	private String name;

	private String description;

	private String icon;

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

	public boolean isStarted() {
		return STARTED_ID.equals(id);
	}

	public boolean isReopened() {
		return REOPENED_ID.equals(id);
	}

	public boolean isResolved() {
		return RESOLVED_ID.equals(id);
	}

	public boolean isClosed() {
		return CLOSED_ID.equals(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (!(obj instanceof Status))
			return false;

		Status that = (Status) obj;

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
