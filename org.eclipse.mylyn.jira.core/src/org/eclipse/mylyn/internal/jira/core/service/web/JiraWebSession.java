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

package org.eclipse.mylar.internal.jira.core.service.web;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.mylar.core.net.WebClientUtil;
import org.eclipse.mylar.internal.jira.core.service.JiraServer;

// TODO Clean up this implementation. It is really dodgey
/**
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
public class JiraWebSession {

	private final JiraServer server;

	private String baseUrl;

	public JiraWebSession(JiraServer server) {
		this.server = server;
		// TODO this canonization is duplicated
		StringBuffer urlBuffer = new StringBuffer(server.getBaseURL());
		if (urlBuffer.charAt(urlBuffer.length() - 1) != '/') {
			urlBuffer.append('/');
		}
		baseUrl = urlBuffer.toString();
	}

	public void doInSession(JiraWebSessionCallback callback) {
		HttpClient client = new HttpClient();

		WebClientUtil.setupHttpClient(client, server.getProxy(), baseUrl, server.getHttpUser(), server.getHttpPassword());

		login(client);
		try {
			callback.execute(client, server);
		} finally {
			logout(client);
		}
	}

	private void login(HttpClient client) {
		PostMethod login = new PostMethod(baseUrl + "login.jsp"); //$NON-NLS-1$
		login.addParameter("os_username", server.getCurrentUserName()); //$NON-NLS-1$
		login.addParameter("os_password", server.getCurrentUserPassword()); //$NON-NLS-1$

		try {
			int statusCode = client.executeMethod(login);
			switch (statusCode) {
			case HttpStatus.SC_OK:
				break;
			case HttpStatus.SC_FORBIDDEN:
			case HttpStatus.SC_UNAUTHORIZED:
				// TODO let the user know
				break;
			default:
				// log the exception and tell the user
			}
		} catch (HttpException e) {
		} catch (IOException e) {
		} finally {
			login.releaseConnection();
		}
	}

	private void logout(HttpClient client) {
		GetMethod logout = new GetMethod(baseUrl + "logout"); //$NON-NLS-1$

		try {
			client.executeMethod(logout);
		} catch (HttpException e) {
			// It doesn't matter if the logout fails. The server will clean up
			// the session eventually
		} catch (IOException e) {
		} finally {
			logout.releaseConnection();
		}
	}
}
