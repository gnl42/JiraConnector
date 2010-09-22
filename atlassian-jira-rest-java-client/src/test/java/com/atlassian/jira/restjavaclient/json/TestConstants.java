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

package com.atlassian.jira.restjavaclient.json;

import com.atlassian.jira.restjavaclient.TestUtil;
import com.atlassian.jira.restjavaclient.domain.User;

import java.net.URI;
import java.net.URISyntaxException;

import static com.atlassian.jira.restjavaclient.TestUtil.toUri;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class TestConstants {
	public static final String USER1_USERNAME = "wseliga";

	public static final String USER1_PASSWORD = "wseliga";

	public static final User USER1 = new User(toUri("http://localhost:8090/jira/rest/api/latest/user/wseliga"), USER1_USERNAME, "Wojciech Seliga");

	public static final User USER_ADMIN = new User(toUri("http://localhost:8090/jira/rest/api/latest/user/admin"), "admin", "Administrator");

	public static final String USER2_USERNAME = "user";

	public static final String USER2_PASSWORD = "user";

	public static final User USER2 = new User(toUri("http://localhost:8090/jira/rest/api/latest/user/wseliga"), USER2_USERNAME, "My Test User");

}
