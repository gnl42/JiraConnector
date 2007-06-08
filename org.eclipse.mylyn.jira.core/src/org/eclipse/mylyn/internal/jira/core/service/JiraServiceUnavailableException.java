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

/**
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
public class JiraServiceUnavailableException extends JiraException {
	
	private static final long serialVersionUID = -6648244599873827934L;

	public JiraServiceUnavailableException(String message) {
		super(message==null ? "Service unavailable" : message);
	}

	public JiraServiceUnavailableException(Throwable cause) {
		super(cause);
	}

}
