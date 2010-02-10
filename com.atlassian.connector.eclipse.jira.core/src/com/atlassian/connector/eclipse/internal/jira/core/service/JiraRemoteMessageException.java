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

package com.atlassian.connector.eclipse.internal.jira.core.service;

/**
 * Indicates that an error page was displayed by the remote repository.
 * 
 * @author Steffen Pingel
 */
public class JiraRemoteMessageException extends JiraRemoteException {

	private static final long serialVersionUID = 4622602207502097037L;

	private final String htmlMessage;

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

		int start = text.indexOf("<strong>"); //$NON-NLS-1$
		if (start != -1) {
			int stop = text.indexOf("</strong>", start + 8); //$NON-NLS-1$
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
				return message + " (" + htmlMessage + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				return message;
			}
		} else {
			return htmlMessage;
		}
	}

}
