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

package org.eclipse.mylar.internal.jira.core.service;

/**
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
public class JiraAuthenticationException extends JiraException {

	private static final long serialVersionUID = 8723151254362915272L;

	public JiraAuthenticationException(String message) {
		super(message==null ? "Invalid user name or password" : message);
	}
	
}
