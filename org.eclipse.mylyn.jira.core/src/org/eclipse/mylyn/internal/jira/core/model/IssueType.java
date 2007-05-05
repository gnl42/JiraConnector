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

package org.eclipse.mylar.internal.jira.core.model;

import java.io.Serializable;

/**
 * @author	Brock Janiczak
 */
public class IssueType implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String BUG_ID = "1";

	public static final String NEW_FEATURE_ID = "2";

	public static final String TASK_ID = "3";

	public static final String IMPROVEMENT_ID = "4";

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

	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (!(obj instanceof IssueType))
			return false;

		IssueType that = (IssueType) obj;

		return this.id.equals(that.id);
	}

	public int hashCode() {
		return id.hashCode();
	}

	public String toString() {
		return this.name;
	}
}
