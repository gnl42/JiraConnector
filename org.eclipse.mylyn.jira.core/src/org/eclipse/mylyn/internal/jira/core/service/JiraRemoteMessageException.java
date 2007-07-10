/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service;

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
		super(getTitle(htmlMessage));

		this.htmlMessage = htmlMessage;
	}

	private static String getTitle(String text) {
		if (text == null) {
			return null;
		}

		int start = text.indexOf("<strong>");
		if (start != -1) {
			int stop = text.indexOf("</strong>", start + 8);
			if (stop != -1) {
				return text.substring(start + 8, stop);
			}
		}
		return null;
	}

	public String getHtmlMessage() {
		return htmlMessage;
	}

	@Override
	public String toString() {
		String message = getMessage();
		if (message != null) {
			if (htmlMessage != null) {
				return message + " (" + htmlMessage + ")";
			} else {
				return message;
			}
		} else {
			return htmlMessage;
		}
	}

}
