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

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.mylar.core.net.WebClientUtil;
import org.eclipse.mylar.internal.jira.core.service.JiraAuthenticationException;
import org.eclipse.mylar.internal.jira.core.service.JiraException;
import org.eclipse.mylar.internal.jira.core.service.JiraServer;
import org.eclipse.mylar.internal.jira.core.service.JiraServiceUnavailableException;

/**
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
public class JiraWebSession {

	private static final int MAX_REDIRECTS = 3;

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

	public void doInSession(JiraWebSessionCallback callback) throws JiraException {
		HttpClient client = new HttpClient();

		WebClientUtil.setupHttpClient(client, server.getProxy(), baseUrl, server.getHttpUser(), server
				.getHttpPassword());

		login(client);
		try {
			callback.execute(client, server);
		} catch (IOException e) {
			throw new JiraException(e);
		} finally {
			logout(client);
		}
	}

	private void login(HttpClient client) throws JiraException {
		String url = baseUrl + "login.jsp";
		for (int i = 0; i < MAX_REDIRECTS; i++) {
			PostMethod login = new PostMethod(url); //$NON-NLS-1$
			login.setFollowRedirects(false);
			login.addParameter("os_username", server.getUserName()); //$NON-NLS-1$
			login.addParameter("os_password", server.getPassword()); //$NON-NLS-1$
			login.addParameter("os_destination", "/success"); //$NON-NLS-1$

			try {
				int statusCode = client.executeMethod(login);
				if (statusCode == HttpStatus.SC_OK) {
					throw new JiraAuthenticationException("Login failed.");
				} else if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
					Header locationHeader = login.getResponseHeader("location");
					if (locationHeader != null) {
						if (locationHeader.getValue().endsWith("/success")) {
							return;
						}
						else {
							url = locationHeader.getValue();
						}
					} else {
						throw new JiraServiceUnavailableException("Invalid redirect, missing location");
					}
				} else {
					throw new JiraServiceUnavailableException("Unexpected status code on login: " + statusCode);
				}
			} catch (IOException e) {
				throw new JiraServiceUnavailableException(e);
			} finally {
				login.releaseConnection();
			}
		}
		throw new JiraServiceUnavailableException("Exceeded maximum number of allowed redirects on login");
	}
	
	private void logout(HttpClient client) throws JiraException {
		GetMethod logout = new GetMethod(baseUrl + "logout"); //$NON-NLS-1$
		logout.setFollowRedirects(false);

		try {
			client.executeMethod(logout);
		} catch (IOException e) {
			// It doesn't matter if the logout fails. The server will clean up
			// the session eventually
		} finally {
			logout.releaseConnection();
		}
	}
}
