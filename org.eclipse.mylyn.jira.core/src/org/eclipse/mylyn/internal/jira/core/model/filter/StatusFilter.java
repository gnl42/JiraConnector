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

import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;

/**
 * @author Brock Janiczak
 */
public class StatusFilter implements Filter, Serializable {
	private static final long serialVersionUID = 1L;

	private final JiraStatus[] statuses;

	public StatusFilter(JiraStatus[] statuses) {
		assert (statuses != null);
		assert (statuses.length > 0);

		this.statuses = statuses;
	}

	public JiraStatus[] getStatuses() {
		return this.statuses;
	}

	StatusFilter copy() {
		return new StatusFilter(this.statuses);
	}

}
