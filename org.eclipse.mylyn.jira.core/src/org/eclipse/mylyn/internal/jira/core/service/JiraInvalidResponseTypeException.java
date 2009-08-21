/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
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
 * Indicates that the repository returned a response of an invalid or unexpected type
 * 
 * @author Thomas Ehrnhoefer
 */
public class JiraInvalidResponseTypeException extends JiraException {

	private static final long serialVersionUID = 1225089680867318262L;

	public JiraInvalidResponseTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public JiraInvalidResponseTypeException(String message) {
		super(message);
	}

	public JiraInvalidResponseTypeException(Throwable cause) {
		super(cause);
	}
}
