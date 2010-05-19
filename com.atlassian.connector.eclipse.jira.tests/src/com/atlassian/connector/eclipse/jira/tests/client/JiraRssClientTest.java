/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.jira.tests.client;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.atlassian.connector.eclipse.internal.jira.core.model.Resolution;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ResolutionFilter;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.connector.eclipse.internal.jira.core.service.web.JiraWebSessionCallback;
import com.atlassian.connector.eclipse.jira.tests.util.JiraFixture;
import com.atlassian.connector.eclipse.jira.tests.util.JiraTestUtil;
import com.atlassian.connector.eclipse.jira.tests.util.MockIssueCollector;

public class JiraRssClientTest extends TestCase {

	private JiraClient client;

	@Override
	protected void setUp() throws Exception {
		client = JiraFixture.current().client();
	}

	@Override
	protected void tearDown() throws Exception {
		JiraTestUtil.tearDown();
	}

	/**
	 * PLE-1081 - check if re-authentication works for all JIRA-s
	 */
	public void testFindIssuesReauthenticate() {
		MockIssueCollector ic = new MockIssueCollector() {
			@Override
			public int getMaxHits() {
				return 1;
			}
		};
		FilterDefinition fd = new FilterDefinition();
		fd.setResolutionFilter(new ResolutionFilter(new Resolution[] { new Resolution("-1") }));
		try {
			client.findIssues(fd, ic, new NullProgressMonitor());
		} catch (JiraException e) {
			fail(e.getMessage());
		}
		try {
			client.getWebSession().doInSession(new JiraWebSessionCallback() {
				@Override
				public void configure(HttpClient httpClient, HostConfiguration hostConfiguration, String baseUrl,
						boolean followRedirects) {
					super.configure(httpClient, hostConfiguration, baseUrl, followRedirects);
					httpClient.getState().clearCookies();
				}

				@Override
				public void run(JiraClient client, String baseUrl, IProgressMonitor monitor) throws JiraException,
						IOException {
					// invalidate cookies
				}
			}, new NullProgressMonitor());
		} catch (JiraException e) {
			fail(e.getMessage());
		}
		try {
			client.findIssues(fd, ic, new NullProgressMonitor());
		} catch (JiraException e) {
			fail(e.getMessage());
		}
	}
}
