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

package org.eclipse.mylyn.internal.jira.core.service;

import java.net.URL;

/**
 * This interface is used as a pluggable way of prompting a user for a password
 *
 * @author	Brock Janiczak
 */
public interface PasswordPrompter {

	/**
	 * Retrieve the password from the user.
	 * 
	 * @return Password the user entered or was stored in a cache somewhere or
	 *         <code>null</code> if the user declined to enter a password or
	 *         no cached password could be found.
	 * @param baseURL
	 *            Location of the sevured resource being accessed
	 * @param username
	 *            User authentication is being requested for
	 */
	public abstract String getPassword(URL baseURL, String username);
}
