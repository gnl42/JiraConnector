/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service.web;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.service.JiraAuthenticationException;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.service.JiraServiceUnavailableException;
import org.eclipse.mylyn.web.core.WebClientUtil;

/**
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
public class JiraWebSession {

	private static final int MAX_REDIRECTS = 3;

	private final JiraClient client;

	private String baseUrl;

	private String characterEncoding;

	public JiraWebSession(JiraClient client, String baseUrl) {
		this.client = client;
		this.baseUrl = baseUrl;
	}

	public JiraWebSession(JiraClient client) {
		this(client, client.getBaseUrl());
	}

	public void doInSession(JiraWebSessionCallback callback) throws JiraException {
		HttpClient httpClient = new HttpClient();

		WebClientUtil.setupHttpClient(httpClient, client.getProxy(), baseUrl, client.getHttpUser(),
				client.getHttpPassword());

		login(httpClient);
		try {
			callback.execute(httpClient, client, baseUrl);
		} catch (IOException e) {
			throw new JiraException(e);
		} finally {
			logout(httpClient);
		}
	}

	protected String getCharacterEncoding() {
		return characterEncoding;
	}

	protected String getContentType() throws JiraException {
		return "application/x-www-form-urlencoded; charset=" + client.getCharacterEncoding();
	}
	
	protected String getBaseURL() {
		return baseUrl;
	}

	private void login(HttpClient httpClient) throws JiraException {

		ArrayList<RedirectInfo> redirects = new ArrayList<RedirectInfo>();

		String url = baseUrl + "/login.jsp";
		for (int i = 0; i < MAX_REDIRECTS; i++) {
			PostMethod login = new PostMethod(url);
			login.setFollowRedirects(false);
			login.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
			login.addParameter("os_username", client.getUserName()); //$NON-NLS-1$
			login.addParameter("os_password", client.getPassword()); //$NON-NLS-1$
			login.addParameter("os_destination", "/success"); //$NON-NLS-1$

			try {
				int statusCode = httpClient.executeMethod(login);
				if (statusCode == HttpStatus.SC_OK) {
					throw new JiraAuthenticationException("Login failed.");
				} else if (statusCode != HttpStatus.SC_MOVED_TEMPORARILY
						&& statusCode != HttpStatus.SC_MOVED_PERMANENTLY) {
					throw new JiraServiceUnavailableException("Unexpected status code on login: " + statusCode);
				}

				this.characterEncoding = login.getResponseCharSet();
				
				Header locationHeader = login.getResponseHeader("location");
				redirects.add(new RedirectInfo(url, statusCode, login.getResponseHeaders(),
						login.getResponseBodyAsString()));
				if (locationHeader == null) {
					throw new JiraServiceUnavailableException("Invalid redirect, missing location");
				}
				url = locationHeader.getValue();
				if (url.endsWith("/success")) {
					String newBaseUrl = url.substring(0, url.lastIndexOf("/success"));
					if (baseUrl.equals(newBaseUrl)) {
						return;
					} else {
						// need to login to make sure HttpClient picks up the session cookie
						baseUrl = newBaseUrl;
						url = newBaseUrl + "/login.jsp";
					}
				}
			} catch (IOException e) {
				throw new JiraServiceUnavailableException(e);
			} finally {
				login.releaseConnection();
			}
		}

		StringBuilder sb = new StringBuilder("Login redirects:\n");
		for (RedirectInfo info : redirects) {
			sb.append(info.toString());
		}
		JiraCorePlugin.log(IStatus.INFO, sb.toString(), null);

		throw new JiraServiceUnavailableException("Exceeded maximum number of allowed redirects on login");
	}

	private void logout(HttpClient httpClient) throws JiraException {
		GetMethod logout = new GetMethod(baseUrl + "/logout"); //$NON-NLS-1$
		logout.setFollowRedirects(false);

		try {
			httpClient.executeMethod(logout);
		} catch (IOException e) {
			// It doesn't matter if the logout fails. The server will clean up
			// the session eventually
		} finally {
			logout.releaseConnection();
		}
	}

	private class RedirectInfo {
		
		final int statusCode;

		final String url;

		final Header[] responseHeaders;

		final String responseBody;

		public RedirectInfo(String url, int statusCode, Header[] responseHeaders, String responseBody) {
			this.url = url;
			this.statusCode = statusCode;
			this.responseHeaders = responseHeaders;
			this.responseBody = responseBody;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("Request: ");
			sb.append(statusCode).append(' ').append(url).append('\n');
			for (Header header : responseHeaders) {
				sb.append("  ").append(header.toExternalForm()).append('\n');
			}
			sb.append(responseBody);
			sb.append("-----------\n");
			return sb.toString();
		}
		
	}

}
