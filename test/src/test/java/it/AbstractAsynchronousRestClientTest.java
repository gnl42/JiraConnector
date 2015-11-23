/*
 * Copyright (C) 2010 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it;

import com.atlassian.jira.nimblefunctests.framework.NimbleFuncTestCase;
import com.atlassian.jira.rest.client.IntegrationTestUtil;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.auth.AnonymousAuthenticationHandler;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.ServerVersionConstants;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.json.TestConstants;

import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.core.UriBuilder;

public abstract class AbstractAsynchronousRestClientTest extends NimbleFuncTestCase {

	protected URI jiraUri;
	protected JiraRestClient client;
	protected URI jiraRestRootUri;
	protected URI jiraAuthRootUri;

	@Override
	public void beforeMethod() {
		super.beforeMethod();

		initUriFields();
		setAdmin();
	}

	private void initUriFields() {
		try {
			jiraUri = UriBuilder.fromUri(environmentData.getBaseUrl().toURI()).build();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		jiraRestRootUri = UriBuilder.fromUri(jiraUri).path(
				IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER ? "/rest/api/2/" : "/rest/api/latest/").build();
		jiraAuthRootUri = UriBuilder.fromUri(jiraUri).path("/rest/auth/latest/").build();
	}

	protected void setAdmin() {
		setClient(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
	}

	protected void setClient(String username, String password) {
		final JiraRestClientFactory clientFactory = new AsynchronousJiraRestClientFactory();
		client = clientFactory.create(jiraUri, new BasicHttpAuthenticationHandler(username, password));
	}

	protected void setAnonymousMode() {
		final JiraRestClientFactory clientFactory = new AsynchronousJiraRestClientFactory();
		client = clientFactory.create(jiraUri, new AnonymousAuthenticationHandler());
	}

	protected void setUser2() {
		setClient(TestConstants.USER2_USERNAME, TestConstants.USER2_PASSWORD);
	}

	protected void setUser1() {
		setClient(TestConstants.USER1_USERNAME, TestConstants.USER1_PASSWORD);
	}

	protected boolean isJira4x4OrNewer() {
		return client.getMetadataClient().getServerInfo().claim().getBuildNumber() >= ServerVersionConstants.BN_JIRA_4_4;
	}

	protected boolean isJira5xOrNewer() {
		return client.getMetadataClient().getServerInfo().claim().getBuildNumber() >= ServerVersionConstants.BN_JIRA_5;
	}

	protected boolean isJira43xOrNewer() {
		return client.getMetadataClient().getServerInfo().claim().getBuildNumber() >= ServerVersionConstants.BN_JIRA_4_3;
	}

	protected boolean isJira61xOrNewer() {
		return client.getMetadataClient().getServerInfo().claim().getBuildNumber() >= ServerVersionConstants.BN_JIRA_6_1;
	}

	protected boolean isJira6_3_7_OrNewer() {
		return client.getMetadataClient().getServerInfo().claim().getBuildNumber() >= ServerVersionConstants.BN_JIRA_6_3_7;
	}

	protected boolean isJira6_4_OrNewer() {
		return client.getMetadataClient().getServerInfo().claim().getBuildNumber() >= ServerVersionConstants.BN_JIRA_6_4;
	}

	protected boolean isJira7_1_OrNewer() {
		return client.getMetadataClient().getServerInfo().claim().getBuildNumber() >= ServerVersionConstants.BN_JIRA_7_1;
	}

	@Override
	public void afterMethod() {
		try {
			super.afterMethod();
			// We may have an empty client when a particular test is disabled because the tests are run
			// on not supported version of JIRA (example: run only on JIRA6 against JIRA5). In this case
			// we don't create a client in beforeMethod.
			if (client != null) {
				client.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
