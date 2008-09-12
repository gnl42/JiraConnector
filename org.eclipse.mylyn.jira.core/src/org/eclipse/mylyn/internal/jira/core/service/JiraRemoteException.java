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
 * Indicates that an exception on the repository side has been encountered while processing the request.
 * 
 * @author Steffen Pingel
 */
public class JiraRemoteException extends JiraException {

	private static final long serialVersionUID = -2218183365629101150L;

	public JiraRemoteException() {
	}

	public JiraRemoteException(String message, Throwable cause) {
		super(message, cause);
	}

	public JiraRemoteException(String message) {
		super(message);
	}

	public JiraRemoteException(Throwable cause) {
		super(cause);
	}

}
