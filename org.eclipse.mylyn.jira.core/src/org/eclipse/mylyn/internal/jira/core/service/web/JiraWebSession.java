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
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.mylar.internal.jira.core.DebugManager;
import org.eclipse.mylar.internal.jira.core.service.JiraServer;

// TODO Clean up this implementation. It is really dodgey
/**
 * @author	Brock Janiczak
 */
public class JiraWebSession {

	private final JiraServer server;

	private String baseUrl;

	private HostConfiguration hostConfiguration;

	private String hostname;

	public JiraWebSession(JiraServer server) {
		hostConfiguration = new HostConfiguration();

		// XXX It would be nice if HTTPClient supported a nicer way of doing
		// this
		// Having the url as a URL object would make this whole process
		// easier...
		try {
			hostname = new URL(server.getBaseURL()).getHost();
			if (System.getProperty("http.proxyHost") != null) {
				String nonProxiedHosts = System.getProperty("http.nonProxyHosts");
				if (nonProxiedHosts == null || nonProxiedHosts.indexOf(hostname) == -1) {
					hostConfiguration.setProxy(System.getProperty("http.proxyHost"), Integer.parseInt(System
							.getProperty("http.proxyPort", "80")));
				}

			}
		} catch (MalformedURLException e) {
			DebugManager.log("Unable to set proxy for conection", e);
		}

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
		client.setHostConfiguration(hostConfiguration);
		if (System.getProperty("http.proxyHost") != null) {
			Credentials creds;
			String proxyUser = System.getProperty("http.proxyUser");
			String proxyPass = System.getProperty("http.proxyPassword");
			if (proxyUser != null && proxyPass != null) {
				int domainSplit = proxyUser.indexOf('\\');
				String domain = null;

				if (domainSplit != -1) {
					proxyUser = proxyUser.substring(0, domainSplit);
					domain = proxyUser.substring(domainSplit + 1);
					creds = new NTCredentials(proxyUser, proxyPass, hostname, domain);
				} else {
					creds = new UsernamePasswordCredentials(proxyUser, proxyPass);
				}
				client.getState().setProxyCredentials(AuthScope.ANY, creds);
			}
		}

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
