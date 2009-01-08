/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.core.client.model;

import com.atlassian.theplugin.commons.crucible.api.model.User;

import java.io.Serializable;

/**
 * Cached User information
 * 
 * @author Shawn Minto
 */
public class CrucibleCachedUser implements Serializable {

	private static final long serialVersionUID = 2062115610526041666L;

	private final String displayName;

	private final String userName;

	public CrucibleCachedUser(String displayName, String userName) {
		this.displayName = displayName;
		this.userName = userName;
	}

	public CrucibleCachedUser(User user) {
		this(user.getDisplayName(), user.getUserName());
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getUserName() {
		return userName;
	}

}
