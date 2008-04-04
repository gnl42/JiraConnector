/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service.web;

import java.io.IOException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.service.JiraServiceUnavailableException;
import org.eclipse.mylyn.web.core.Policy;
import org.eclipse.mylyn.web.core.WebUtil;

/**
 * @author Steffen Pingel
 */
public abstract class JiraWebSessionCallback {

	private HostConfiguration hostConfiguration;

	private HttpClient httpClient;

	private String baseUrl;

	public void configure(HttpClient httpClient, HostConfiguration hostConfiguration, String baseUrl) {
		this.httpClient = httpClient;
		this.hostConfiguration = hostConfiguration;
		this.baseUrl = baseUrl;
	}

	public int execute(HttpMethod method) throws JiraException {
		return execute(method, Policy.monitorFor(null));
	}

	public int execute(HttpMethod method, IProgressMonitor monitor) throws JiraException {
		try {
			return WebUtil.execute(httpClient, hostConfiguration, method, monitor);
		} catch (IOException e) {
			throw new JiraException(e);
		}
	}

	public abstract void run(JiraClient client, String baseUrl, IProgressMonitor monitor) throws JiraException, IOException;

	protected boolean expectRedirect(HttpMethodBase method, JiraIssue issue) throws JiraException {
		return expectRedirect(method, "/browse/" + issue.getKey());
	}

	protected boolean expectRedirect(HttpMethodBase method, String page) throws JiraException {
		if (method.getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY) {
			return false;
		}

		Header locationHeader = method.getResponseHeader("location");
		if (locationHeader == null) {
			throw new JiraServiceUnavailableException("Invalid server response, missing redirect location");
		}
		String url = locationHeader.getValue();
		if (!url.startsWith(baseUrl + page)) {
			throw new JiraException("Server redirected to unexpected location: " + url);
		}
		return true;
	}

}
