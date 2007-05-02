/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.core.service;

/**
 * Indicates that an error page was displayed by the remote repository.
 * 
 * @author Steffen Pingel
 */
public class JiraRemoteMessageException extends JiraRemoteException {

	private static final long serialVersionUID = 4622602207502097037L;

	private String htmlMessage;

	public JiraRemoteMessageException(String message, String htmlMessage) {
		super(message);

		this.htmlMessage = htmlMessage;
	}

	public JiraRemoteMessageException(String htmlMessage) {
		this.htmlMessage = htmlMessage;
	}

	public String getHtmlMessage() {
		return htmlMessage;
	}

}
