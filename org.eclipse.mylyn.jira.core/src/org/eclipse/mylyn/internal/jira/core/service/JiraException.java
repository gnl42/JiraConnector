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

/**
 * Indicates an error during repository access.
 * 
 * @author Steffen Pingel
 */
public class JiraException extends Exception {

	private static final long serialVersionUID = -4354184850277873071L;

	public JiraException() {
	}

	public JiraException(String message, Throwable cause) {
		super(message, cause);
	}

	public JiraException(String message) {
		super(message);
	}

	public JiraException(Throwable cause) {
		super(cause);
	}

}
