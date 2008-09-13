/*******************************************************************************
 * Copyright (c) 2004, 2008 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service;

/**
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
public class JiraInsufficientPermissionException extends JiraException {

	private static final long serialVersionUID = 8122623908757665154L;

	public JiraInsufficientPermissionException(String message) {
		super(message == null ? "Insufficient permissions" : message);
	}

}
