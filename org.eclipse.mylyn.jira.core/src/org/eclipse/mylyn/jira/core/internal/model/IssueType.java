/*******************************************************************************
 * Copyright (c) 2005 Jira Dashboard project.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylar.jira.core.internal.model;

import java.io.Serializable;

public class IssueType implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final IssueType MISSING_ISSUE_TYPE = createMissingIssueType();

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

	public static IssueType createMissingIssueType() {
		IssueType issueType = new IssueType();
		issueType.setId("-1");
		issueType.setName("Unknown");
		issueType.setDescription("Unknown");
		return issueType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (!(obj instanceof IssueType))
			return false;

		IssueType that = (IssueType) obj;

		return this.id.equals(that.id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return id.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.name;
	}
}
