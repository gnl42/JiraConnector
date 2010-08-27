/*******************************************************************************
 * Copyright (c) 2004, 2009 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.core.service.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.commons.net.UnsupportedRequestException;
import org.eclipse.mylyn.commons.net.WebUtil;

import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraAuthenticationException;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraCaptchaRequiredException;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraInvalidResponseTypeException;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraRemoteMessageException;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraServiceUnavailableException;

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

	protected static final String USER_AGENT = "JiraConnector"; //$NON-NLS-1$

	private static final Object SESSION_ID_COOKIE = "JSESSIONID"; //$NON-NLS-1$x

	private HttpClient httpClient;

	private HostConfiguration hostConfiguration;

	private final Lock authenticationLock;

	private volatile boolean reauthenticate;

	public JiraWebSession(JiraClient client, String baseUrl) {
		this.client = client;
		this.baseUrl = baseUrl;
		this.secure = baseUrl.startsWith("https"); //$NON-NLS-1$
		this.location = client.getLocation();
		this.authenticationLock = new ReentrantLock();
	}

	public JiraWebSession(JiraClient client) {
		this(client, client.getBaseUrl());
	}

	public void doInSession(JiraWebSessionCallback callback, IProgressMonitor monitor) throws JiraException {
		monitor = Policy.monitorFor(monitor);
		boolean doLogin = hostConfiguration == null || reauthenticate;
		int MAX_RETRIES = doLogin ? 1 : 2; //if login gets skipped, a second try might be needed
		for (int i = 1; i <= MAX_RETRIES; i++) {
			try {
				lock(monitor);
				if (httpClient == null) {
					httpClient = new HttpClient(WebUtil.getConnectionManager());
					WebUtil.configureHttpClient(httpClient, "JiraConnector"); //$NON-NLS-1$
					httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
				}
				if (doLogin) {
					reauthenticate = false;
					hostConfiguration = login(httpClient, monitor);
				}
			} finally {
				unlock(monitor);
			}
			try {
				// check if session is expired
				if (doLogin || isAuthenticated(httpClient, hostConfiguration, monitor)) {
					callback.configure(httpClient, hostConfiguration, baseUrl, client.getLocalConfiguration()
							.getFollowRedirects());
					callback.run(client, baseUrl, monitor);
					return;
				} else {
					doLogin = true;
				}
			} catch (IOException e) {
				throw new JiraException(e);
			} catch (JiraException e) {
				//if an exception occurs because of a missing login
				if (isAuthenticationFailure(e)) {
					//rinse and repeat with login
					doLogin = true;
				} else {
					throw e;
				}
			}
		}
	}

	private boolean isAuthenticated(HttpClient httpClient, HostConfiguration hostConfiguration, IProgressMonitor monitor)
			throws JiraException {
		String url = baseUrl + "/secure/UpdateUserPreferences!default.jspa"; //$NON-NLS-1$
		GetMethod method = new GetMethod(url);
		method.setFollowRedirects(false);

		try {
			int statusCode = WebUtil.execute(httpClient, hostConfiguration, method, monitor);

			if (statusCode == HttpStatus.SC_OK) {
				return !method.getResponseBodyAsString().contains("/login.jsp?os_destination"); //$NON-NLS-1$
			}
		} catch (IOException e) {
			throw new JiraException(e);
		}
		return false;
	}

	private boolean isAuthenticationFailure(JiraException e) {
		if (e instanceof JiraRemoteMessageException) {
			String message = ((JiraRemoteMessageException) e).getHtmlMessage();
			return message != null && message.contains("login.jsp"); //$NON-NLS-1$
		}
		return (e instanceof JiraInvalidResponseTypeException);
	}

	public void doLogout(IProgressMonitor monitor) throws JiraException {
		monitor = Policy.monitorFor(monitor);
		try {
			lock(monitor);
			if (hostConfiguration == null) {
				// never logged in
				return;
			}
			logout(httpClient, hostConfiguration, monitor);
			hostConfiguration = null;
		} finally {
			unlock(monitor);
		}
	}

	private void lock(IProgressMonitor monitor) {
		while (!monitor.isCanceled()) {
			try {
				if (authenticationLock.tryLock(2000, TimeUnit.MILLISECONDS)) {
					return;
				}
			} catch (InterruptedException e) {
				throw new OperationCanceledException();
			}
		}
		throw new OperationCanceledException();
	}

	private void unlock(IProgressMonitor monitor) {
		authenticationLock.unlock();
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

	private String getContentType() {
		String characterEncoding = getCharacterEncoding();
		if (characterEncoding == null) {
			characterEncoding = client.getLocalConfiguration().getCharacterEncoding();
		}
		if (characterEncoding == null) {
			characterEncoding = client.getLocalConfiguration().getDefaultCharacterEncoding();
		}
		return "application/x-www-form-urlencoded; charset=" + characterEncoding; //$NON-NLS-1$
	}

	private HostConfiguration login(HttpClient httpClient, IProgressMonitor monitor) throws JiraException {
		RedirectTracker tracker = new RedirectTracker();

		//String url = baseUrl + "/login.jsp"; //$NON-NLS-1$
		String url = baseUrl + "/secure/Dashboard.jspa"; //$NON-NLS-1$
		for (int i = 0; i <= MAX_REDIRECTS; i++) {
			AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
			if (credentials == null) {
				// TODO prompt user?
				credentials = new AuthenticationCredentials("", ""); //$NON-NLS-1$ //$NON-NLS-2$
			}

			PostMethod login = new PostMethod(url);
			login.setFollowRedirects(false);
			login.setRequestHeader("Content-Type", getContentType()); //$NON-NLS-1$
			login.addParameter("os_username", credentials.getUserName()); //$NON-NLS-1$
			login.addParameter("os_password", credentials.getPassword()); //$NON-NLS-1$
			login.addParameter("os_destination", "/success"); //$NON-NLS-1$ //$NON-NLS-2$

			tracker.addUrl(url);

			try {
				HostConfiguration hostConfiguration = WebUtil.createHostConfiguration(httpClient, location, monitor);
				int statusCode = WebUtil.execute(httpClient, hostConfiguration, login, monitor);
				if (needsReauthentication(httpClient, login, monitor)) {
					continue;
				} else if (statusCode != HttpStatus.SC_MOVED_TEMPORARILY
						&& statusCode != HttpStatus.SC_MOVED_PERMANENTLY) {
					throw new JiraServiceUnavailableException("Unexpected status code during login: " + statusCode); //$NON-NLS-1$
				}

				tracker.addRedirect(url, login, statusCode);

				this.characterEncoding = login.getResponseCharSet();

				Header locationHeader = login.getResponseHeader("location"); //$NON-NLS-1$
				if (locationHeader == null) {
					throw new JiraServiceUnavailableException("Invalid redirect, missing location"); //$NON-NLS-1$
				}
				url = locationHeader.getValue();
				tracker.checkForCircle(url);
				if (!insecureRedirect && isSecure() && url.startsWith("http://")) { //$NON-NLS-1$
					tracker.log("Redirect to insecure location during login to repository: " + client.getBaseUrl()); //$NON-NLS-1$
					insecureRedirect = true;
				}
				if (url.endsWith("/success")) { //$NON-NLS-1$
					String newBaseUrl = url.substring(0, url.lastIndexOf("/success")); //$NON-NLS-1$
					if (baseUrl.equals(newBaseUrl) || !client.getLocalConfiguration().getFollowRedirects()) {
						// success
						addAuthenticationCookie(httpClient, login);
						return hostConfiguration;
					} else {
						// need to login to make sure HttpClient picks up the session cookie
						baseUrl = newBaseUrl;
						url = newBaseUrl + "/login.jsp"; //$NON-NLS-1$
					}
				}
			} catch (IOException e) {
				throw new JiraServiceUnavailableException(e);
			} finally {
				login.releaseConnection();
			}
		}

		tracker.log("Exceeded maximum number of allowed redirects during login to repository: " + client.getBaseUrl()); //$NON-NLS-1$

		throw new JiraServiceUnavailableException("Exceeded maximum number of allowed redirects during login"); //$NON-NLS-1$
	}

	private void addAuthenticationCookie(HttpClient httpClient, PostMethod method) {
		Cookie[] cookies = httpClient.getState().getCookies();
		for (Cookie cookie : cookies) {
			if (SESSION_ID_COOKIE.equals(cookie.getName())) {
				// already have cookie
				return;
			}
		}

		for (Header header : method.getResponseHeaders()) {
			if (header.getName().equalsIgnoreCase(HTTPConstants.HEADER_SET_COOKIE)) {
				String cookie = header.getValue();
				// chop of path
				int index = cookie.indexOf(';');
				if (index != -1) {
					cookie = cookie.substring(0, index);
				}
				// get session id 
				int i = cookie.indexOf("="); //$NON-NLS-1$
				String key = (i != -1) ? cookie.substring(0, i) : cookie;
				cookie = (i != -1) ? cookie.substring(i + 1) : ""; //$NON-NLS-1$
				httpClient.getState().addCookie(
						new Cookie(WebUtil.getHost(baseUrl), key, cookie, WebUtil.getRequestPath(baseUrl), null,
								isSecure(baseUrl)));
			}
		}
	}

	private static boolean isSecure(String repositoryUrl) {
		return repositoryUrl.matches("https.*"); //$NON-NLS-1$
	}

	private boolean needsReauthentication(HttpClient httpClient, PostMethod method, IProgressMonitor monitor)
			throws JiraAuthenticationException {
		final AuthenticationType authenticationType;
		int code = method.getStatusCode();
		if (code == HttpStatus.SC_OK) {
			authenticationType = AuthenticationType.REPOSITORY;
		} else if (code == HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED) {
			authenticationType = AuthenticationType.PROXY;
		} else {
			return false;
		}

		// the server specified a different character set than what was returned by the server, try to re-authenticate 
		if (!getContentType().endsWith(method.getResponseCharSet())) {
			this.characterEncoding = method.getResponseCharSet();
			return true;
		}

		try {
			client.getSoapClient().login(Policy.backgroundMonitorFor(monitor)); // pass NPM so there will be no request credentials dialog
		} catch (JiraException e) {
			if (e instanceof JiraCaptchaRequiredException) {
				throw (JiraCaptchaRequiredException) e;
			} else {
				// just skip it
			}
		}

		try {
			location.requestCredentials(authenticationType, null, monitor);
		} catch (UnsupportedRequestException ignored) {
			throw new JiraAuthenticationException("Login failed."); //$NON-NLS-1$
		}

		return true;
	}

	private void logout(HttpClient httpClient, HostConfiguration hostConfiguration, IProgressMonitor monitor)
			throws JiraException {
		GetMethod logout = new GetMethod(baseUrl + "/logout"); //$NON-NLS-1$
		logout.setFollowRedirects(false);
		try {
			WebUtil.execute(httpClient, hostConfiguration, logout, monitor);
			httpClient.getState().clear(); //clear cookies and credentials for full logout
		} catch (IOException e) {
			// It doesn't matter if the logout fails. The server will clean up
			// the session eventually
		} finally {
			logout.releaseConnection();
		}
	}

	public void purgeSession() {
		this.reauthenticate = true;
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
				log("Circular redirect detected while login in to repository: " + client.getBaseUrl()); //$NON-NLS-1$
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
			StatusHandler.log(status);
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
			StringBuilder sb = new StringBuilder("Request: "); //$NON-NLS-1$
			sb.append(url).append('\n');
			sb.append("Status: ").append(statusCode).append('\n'); //$NON-NLS-1$
			// log useful but insensitive headers
			for (Header header : responseHeaders) {
				if (header.getName().equalsIgnoreCase("Server") || header.getName().equalsIgnoreCase("Location")) { //$NON-NLS-1$ //$NON-NLS-2$
					sb.append(header.toExternalForm());
				}
			}
			return sb.toString();
		}

	}

}
