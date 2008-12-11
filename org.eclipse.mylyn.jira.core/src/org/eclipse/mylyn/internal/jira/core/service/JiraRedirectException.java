/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service;

import java.text.MessageFormat;

/**
 * Indicates an unexpected redirect during repository access.
 * 
 * @author Steffen Pingel
 */
public class JiraRedirectException extends JiraException {

	private static final long serialVersionUID = 5414408704994061726L;

	private final String location;

	public JiraRedirectException(String message, String location, Throwable cause) {
		super(message, cause);
		this.location = location;
	}

	public JiraRedirectException(String message, String location) {
		super(message);
		this.location = location;
	}

	public JiraRedirectException(String location) {
		super(MessageFormat.format("Server redirected to unexpected location: {0}", location)); //$NON-NLS-1$
		this.location = location;
	}

	public JiraRedirectException() {
		super("Invalid server response, missing redirect location"); //$NON-NLS-1$
		this.location = null;
	}

	public String getLocation() {
		return location;
	}

}
