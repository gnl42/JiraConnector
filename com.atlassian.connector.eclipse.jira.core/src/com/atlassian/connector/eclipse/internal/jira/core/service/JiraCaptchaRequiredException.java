/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.core.service;

@SuppressWarnings("serial")
public class JiraCaptchaRequiredException extends JiraAuthenticationException {

	public JiraCaptchaRequiredException(String message) {
		super(message == null ? "You've been locked out of remote API due to multiple failed login attemps" : message); //$NON-NLS-1$
	}

}
