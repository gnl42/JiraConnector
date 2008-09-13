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

package org.eclipse.mylyn.internal.jira.core.service.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.commons.net.UnsupportedRequestException;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.service.JiraAuthenticationException;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.service.JiraServiceUnavailableException;

/**
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
public class JiraWebSession {

	private static final int MAX_REDIRECTS = 3;

	private final JiraClient client;

	private String baseUrl;

	private String characterEncoding;

	private final boolean secure;

	private boolean insecureRedirect;

	private boolean logEnabled;

	private final AbstractWebLocation location;

	protected static final String USER_AGENT = "JiraConnector";

	public JiraWebSession(JiraClient client, String baseUrl) {
		this.client = client;
		this.baseUrl = baseUrl;
		this.secure = baseUrl.startsWith("https");
		this.location = client.getLocation();
	}

	public JiraWebSession(JiraClient client) {
		this(client, client.getBaseUrl());
	}

	public void doInSession(JiraWebSessionCallback callback, IProgressMonitor monitor) throws JiraException {
		monitor = Policy.monitorFor(monitor);
		SimpleHttpConnectionManager connectionManager = new SimpleHttpConnectionManager();
		try {
			HttpClient httpClient = new HttpClient(connectionManager);
			HostConfiguration hostConfiguration = login(httpClient, monitor);
			try {
				callback.configure(httpClient, hostConfiguration, baseUrl, client.getConfiguration()
						.getFollowRedirects());
				callback.run(client, baseUrl, monitor);
			} catch (IOException e) {
				throw new JiraException(e);
			} finally {
				logout(httpClient, hostConfiguration, monitor);
			}
		} finally {
			connectionManager.shutdown();
		}
	}

	protected String getCharacterEncoding() {
		return characterEncoding;
	}

	protected String getBaseURL() {
		return baseUrl;
	}

	protected boolean isInsecureRedirect() {
		return insecureRedirect;
	}

	protected boolean isSecure() {
		return secure;
	}

	protected boolean isLogEnabled() {
		return logEnabled;
	}

	protected void setLogEnabled(boolean logEnabled) {
		this.logEnabled = logEnabled;
	}

	private HostConfiguration login(HttpClient httpClient, IProgressMonitor monitor) throws JiraException {
		RedirectTracker tracker = new RedirectTracker();

		String url = baseUrl + "/login.jsp";
		for (int i = 0; i <= MAX_REDIRECTS; i++) {
			AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
			if (credentials == null) {
				// TODO prompt user?
				credentials = new AuthenticationCredentials("", "");
			}

			PostMethod login = new PostMethod(url);
			login.setFollowRedirects(false);
			login.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
			login.addParameter("os_username", credentials.getUserName()); //$NON-NLS-1$
			login.addParameter("os_password", credentials.getPassword()); //$NON-NLS-1$
			login.addParameter("os_destination", "/success"); //$NON-NLS-1$

			tracker.addUrl(url);

			try {
				HostConfiguration hostConfiguration = WebUtil.createHostConfiguration(httpClient, location, monitor);
				int statusCode = WebUtil.execute(httpClient, hostConfiguration, login, monitor);
				if (needsReauthentication(httpClient, statusCode, monitor)) {
					continue;
				} else if (statusCode != HttpStatus.SC_MOVED_TEMPORARILY
						&& statusCode != HttpStatus.SC_MOVED_PERMANENTLY) {
					throw new JiraServiceUnavailableException("Unexpected status code during login: " + statusCode);
				}

				tracker.addRedirect(url, login, statusCode);

				this.characterEncoding = login.getResponseCharSet();

				Header locationHeader = login.getResponseHeader("location");
				if (locationHeader == null) {
					throw new JiraServiceUnavailableException("Invalid redirect, missing location");
				}
				url = locationHeader.getValue();
				tracker.checkForCircle(url);
				if (!insecureRedirect && isSecure() && url.startsWith("http://")) {
					tracker.log("Redirect to insecure location during login to repository: " + client.getBaseUrl());
					insecureRedirect = true;
				}
				if (url.endsWith("/success")) {
					String newBaseUrl = url.substring(0, url.lastIndexOf("/success"));
					if (baseUrl.equals(newBaseUrl) || !client.getConfiguration().getFollowRedirects()) {
						// success
						return hostConfiguration;
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

		tracker.log("Exceeded maximum number of allowed redirects during login to repository: " + client.getBaseUrl());

		throw new JiraServiceUnavailableException("Exceeded maximum number of allowed redirects during login");
	}

	private boolean needsReauthentication(HttpClient httpClient, int code, IProgressMonitor monitor)
			throws JiraAuthenticationException {
		final AuthenticationType authenticationType;
		if (code == HttpStatus.SC_OK) {
			authenticationType = AuthenticationType.REPOSITORY;
		} else if (code == HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED) {
			authenticationType = AuthenticationType.PROXY;
		} else {
			return false;
		}

		try {
			location.requestCredentials(authenticationType, null, monitor);
		} catch (UnsupportedRequestException ignored) {
			throw new JiraAuthenticationException("Login failed.");
		}

		return true;
	}

	private void logout(HttpClient httpClient, HostConfiguration hostConfiguration, IProgressMonitor monitor)
			throws JiraException {
		GetMethod logout = new GetMethod(baseUrl + "/logout"); //$NON-NLS-1$
		logout.setFollowRedirects(false);
		try {
			WebUtil.execute(httpClient, hostConfiguration, logout, monitor);
		} catch (IOException e) {
			// It doesn't matter if the logout fails. The server will clean up
			// the session eventually
		} finally {
			logout.releaseConnection();
		}
	}

	private class RedirectTracker {

		ArrayList<RedirectInfo> redirects = new ArrayList<RedirectInfo>();

		Set<String> urls = new HashSet<String>();

		private boolean loggedCircle;

		public void addUrl(String url) {
			urls.add(url);
		}

		public void checkForCircle(String url) {
			if (!loggedCircle && urls.contains(url)) {
				log("Circular redirect detected while login in to repository: " + client.getBaseUrl());
				loggedCircle = true;
			}
		}

		public void addRedirect(String url, HttpMethodBase method, int statusCode) {
			redirects.add(new RedirectInfo(url, statusCode, method.getResponseHeaders()));
		}

		public void log(String message) {
			if (!isLogEnabled()) {
				return;
			}

			MultiStatus status = new MultiStatus(JiraCorePlugin.ID_PLUGIN, 0, message, null);
			for (RedirectInfo info : redirects) {
				status.add(new Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN, 0, info.toString(), null));
			}
			JiraCorePlugin.log(status);
		}

	}

	private class RedirectInfo {

		final int statusCode;

		final String url;

		final Header[] responseHeaders;

		public RedirectInfo(String url, int statusCode, Header[] responseHeaders) {
			this.url = url;
			this.statusCode = statusCode;
			this.responseHeaders = responseHeaders;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("Request: ");
			sb.append(url).append('\n');
			sb.append("Status: ").append(statusCode).append('\n');
			// log useful but insensitive headers
			for (Header header : responseHeaders) {
				if (header.getName().equalsIgnoreCase("Server") || header.getName().equalsIgnoreCase("Location")) {
					sb.append(header.toExternalForm());
				}
			}
			return sb.toString();
		}

	}

}
