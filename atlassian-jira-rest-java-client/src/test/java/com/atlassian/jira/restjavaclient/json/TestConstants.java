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
import com.atlassian.jira.restjavaclient.domain.BasicComponent;
import com.atlassian.jira.restjavaclient.domain.BasicUser;
import com.atlassian.jira.restjavaclient.domain.Version;

import static com.atlassian.jira.restjavaclient.TestUtil.toUri;

/**
 * Constants used in various unit tests.
 * All constants including full URIs are usually useless in integration tests though, as during integration
 * tests we may be testing against a JIRA running on a different port and with a different web context 
 *
 * @since v0.1
 */
public class TestConstants {
	public static final String USER1_USERNAME = "wseliga";

	public static final String USER1_PASSWORD = "wseliga";

	public static final BasicUser USER1 = new BasicUser(toUri("http://localhost:8090/jira/rest/api/latest/user?username=wseliga"), USER1_USERNAME, "Wojciech Seliga");

	public static final BasicUser USER_ADMIN = new BasicUser(toUri("http://localhost:8090/jira/rest/api/latest/user?username=admin"), "admin", "Administrator");

	public static final String USER2_USERNAME = "user";

	public static final String USER2_PASSWORD = "user";

	public static final BasicUser USER2 = new BasicUser(toUri("http://localhost:8090/jira/rest/api/latest/user?username=user"), USER2_USERNAME, "My Test User");

	public static final Version VERSION_1 = new Version(toUri("http://localhost:8090/jira/rest/api/latest/version/10001"),
			"1", "initial version", false, false, null);

	public static final Version VERSION_1_1 = new Version(toUri("http://localhost:8090/jira/rest/api/latest/version/10000"),
			"1.1", "Some version", true, false, TestUtil.toDateTime("2010-08-25T00:00:00.000+0200"));

	public static final BasicComponent BCOMPONENT_A = new BasicComponent(toUri("http://localhost:8090/jira/rest/api/latest/component/10000"),
			"Component A", "this is some description of component A");

	public static final BasicComponent BCOMPONENT_B = new BasicComponent(toUri("http://localhost:8090/jira/rest/api/latest/component/10001"),
			"Component B", "another description");
}
