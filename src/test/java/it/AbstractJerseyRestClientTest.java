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
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.auth.AnonymousAuthenticationHandler;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.ServerVersionConstants;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClient;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;

import static com.atlassian.jira.rest.client.internal.json.TestConstants.*;

public abstract class AbstractJerseyRestClientTest extends NimbleFuncTestCase {

	protected URI jiraUri;
	protected JerseyJiraRestClient client;
	protected URI jiraRestRootUri;
	protected URI jiraAuthRootUri;
	protected final NullProgressMonitor pm = new NullProgressMonitor();

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
		setClient(ADMIN_USERNAME, ADMIN_PASSWORD);
	}

	protected void setClient(String username, String password) {
		client = new JerseyJiraRestClient(jiraUri, new BasicHttpAuthenticationHandler(username, password));
	}

	protected void setAnonymousMode() {
		client = new JerseyJiraRestClient(jiraUri, new AnonymousAuthenticationHandler());
	}

	protected void setUser2() {
		setClient(USER2_USERNAME, USER2_PASSWORD);
	}

	protected void setUser1() {
		setClient(USER1_USERNAME, USER1_PASSWORD);
	}

	protected boolean isJira4x4OrNewer() {
		return client.getMetadataClient().getServerInfo(pm).getBuildNumber() >= ServerVersionConstants.BN_JIRA_4_4;
	}

	protected boolean isJira5xOrNewer() {
		return client.getMetadataClient().getServerInfo(pm).getBuildNumber() >= ServerVersionConstants.BN_JIRA_5;
	}

	protected boolean isJira4x3OrNewer() {
		return client.getMetadataClient().getServerInfo(pm).getBuildNumber() >= ServerVersionConstants.BN_JIRA_4_3;
	}

	protected boolean doesJiraSupportRestIssueLinking() {
		return client.getMetadataClient().getServerInfo(pm).getBuildNumber() >= ServerVersionConstants.BN_JIRA_4_3;
	}

}
