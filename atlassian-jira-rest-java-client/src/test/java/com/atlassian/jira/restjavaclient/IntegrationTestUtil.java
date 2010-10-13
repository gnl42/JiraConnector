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

package com.atlassian.jira.restjavaclient;

import com.atlassian.jira.restjavaclient.domain.BasicUser;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class IntegrationTestUtil {
	public static final BasicUser USER_ADMIN;
    public static final BasicUser USER1;
    public static final BasicUser USER2;
	public static final BasicUser USER_SLASH;

	public static final int START_PROGRESS_TRANSITION_ID = 4;
	public static final int STOP_PROGRESS_TRANSITION_ID = 301;
	public static final String NUMERIC_CUSTOMFIELD_ID = "customfield_10000";
	public static final String NUMERIC_CUSTOMFIELD_TYPE = "com.atlassian.jira.plugin.system.customfieldtypes:float";
	private static final LocalTestEnvironmentData environmentData = new LocalTestEnvironmentData();


	static {
        try {
            USER1 = new BasicUser(getUserUri("wseliga"), "wseliga", "Wojciech Seliga");
            USER2 = new BasicUser(getUserUri("user"), "user", "My Test User");
			USER_SLASH = new BasicUser(getUserUri("a/user/with/slash"), "a/user/with/slash", "A User with / in its username");
            USER_ADMIN = new BasicUser(getUserUri("admin"), "admin", "Administrator");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

	private static URI getUserUri(String username) throws URISyntaxException {
		return UriBuilder.fromUri(environmentData.getBaseUrl().toURI()).path("/rest/api/latest/user").queryParam("username", username).build();
	}

    public static URI concat(URI uri, String path) {
        return UriBuilder.fromUri(uri).path(path).build();
    }

}
