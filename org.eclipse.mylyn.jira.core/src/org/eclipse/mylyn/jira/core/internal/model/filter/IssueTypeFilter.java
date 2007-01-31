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
package org.eclipse.mylar.jira.core.internal.model.filter;

import java.io.Serializable;

import org.eclipse.mylar.jira.core.internal.model.IssueType;

// TODO consider making this abstract and using subclasses to do the typing
public class IssueTypeFilter implements Filter, Serializable {
	private static final long serialVersionUID = 1L;

	private final IssueType[] issueTypes;

	private final boolean standardTypes;

	private final boolean subTaskTypes;

	public IssueTypeFilter(IssueType[] issueTypes) {
		this.issueTypes = issueTypes;
		standardTypes = false;
		subTaskTypes = false;
	}

	public IssueTypeFilter(boolean standardTypes, boolean subTaskTypes) {
		assert (standardTypes ^ subTaskTypes);

		this.issueTypes = null;
		this.standardTypes = standardTypes;
		this.subTaskTypes = subTaskTypes;
	}

	public IssueType[] getIsueTypes() {
		return this.issueTypes;
	}

	public boolean isStandardTypes() {
		return this.standardTypes;
	}

	public boolean isSubTaskTypes() {
		return this.subTaskTypes;
	}

	IssueTypeFilter copy() {
		if (issueTypes != null) {
			return new IssueTypeFilter(this.issueTypes);
		}

		return new IssueTypeFilter(standardTypes, subTaskTypes);
	}

}