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

/**
 * @author Brock Janiczak
 */
public class SpecificUserFilter extends UserFilter {
	private static final long serialVersionUID = 1L;

	private final String user;

	public SpecificUserFilter(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.model.filter.UserFilter#copy()
	 */
	@Override
	UserFilter copy() {
		return new SpecificUserFilter(user);
	}
}
