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

package com.atlassian.jira.rest.client;

import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.internal.ServerVersionConstants;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClient;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;

public class IntegrationTestUtil {
	public static final BasicUser USER_ADMIN;
	public static final BasicUser USER_ADMIN_LATEST;
    public static final BasicUser USER1;
    public static final BasicUser USER2;
	public static final BasicUser USER_SLASH;
	public static final BasicUser USER_SLASH_LATEST;

	public static final boolean TESTING_JIRA_5_OR_NEWER;
	public static final int START_PROGRESS_TRANSITION_ID = 4;
	public static final int STOP_PROGRESS_TRANSITION_ID = 301;
	public static final String NUMERIC_CUSTOMFIELD_ID = "customfield_10000";
	public static final String NUMERIC_CUSTOMFIELD_TYPE = "com.atlassian.jira.plugin.system.customfieldtypes:float";
	public static final String NUMERIC_CUSTOMFIELD_TYPE_V5 = "number";
	private static final LocalTestEnvironmentData environmentData = new LocalTestEnvironmentData();
	private static final String URI_INTERFIX_FOR_USER;


	static {
        try {
			JerseyJiraRestClient client = new JerseyJiraRestClient(environmentData.getBaseUrl().toURI(), new BasicHttpAuthenticationHandler("admin", "admin"));
			TESTING_JIRA_5_OR_NEWER = client.getMetadataClient().getServerInfo(new NullProgressMonitor()).getBuildNumber() > ServerVersionConstants.BN_JIRA_5;
			// remove it when https://jdog.atlassian.com/browse/JRADEV-7691 is fixed
			URI_INTERFIX_FOR_USER = TESTING_JIRA_5_OR_NEWER ? "2" : "latest";

            USER1 = new BasicUser(getUserUri("wseliga"), "wseliga", "Wojciech Seliga");
            USER2 = new BasicUser(getUserUri("user"), "user", "My Test User");
			USER_SLASH = new BasicUser(getUserUri("a/user/with/slash"), "a/user/with/slash", "A User with / in its username");
			USER_SLASH_LATEST = new BasicUser(getLatestUserUri("a/user/with/slash"), "a/user/with/slash", "A User with / in its username");
            USER_ADMIN = new BasicUser(getUserUri("admin"), "admin", "Administrator");
			USER_ADMIN_LATEST = new BasicUser(getLatestUserUri("admin"), "admin", "Administrator");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

	private static URI getUserUri(String username) throws URISyntaxException {
		return UriBuilder.fromUri(environmentData.getBaseUrl().toURI()).path("/rest/api/" +
				URI_INTERFIX_FOR_USER + "/user").queryParam("username", username).build();
	}

	private static URI getLatestUserUri(String username) throws URISyntaxException {
		return UriBuilder.fromUri(environmentData.getBaseUrl().toURI()).path("/rest/api/latest/user").queryParam("username", username).build();
	}

    public static URI concat(URI uri, String path) {
        return UriBuilder.fromUri(uri).path(path).build();
    }

}
