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

import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.ServerVersionConstants;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class IntegrationTestUtil {
	public static final User USER_ADMIN_FULL;
	public static final BasicUser USER_ADMIN;
	public static final BasicUser USER_ADMIN_LATEST;
	public static final User USER1_FULL;
	public static final BasicUser USER1;
	public static final BasicUser USER2;
	public static final BasicUser USER1_LATEST;
	public static final BasicUser USER2_LATEST;
	public static final BasicUser USER_SLASH;
	public static final BasicUser USER_SLASH_LATEST;
	/**
	 * User representation with /latest/ before 6.0 and /2/ since 6.0
	 */
	public static final BasicUser USER1_60;
	/**
	 * User representation with /latest/ before 6.0 and /2/ since 6.0
	 */
	public static final BasicUser USER2_60;
	/**
	 * User representation with /latest/ before 6.0 and /2/ since 6.0
	 */
	public static final BasicUser USER_ADMIN_60;
	/**
	 * User representation with /latest/ before 6.0 and /2/ since 6.0
	 */
	public static final BasicUser USER_SLASH_60;

	public static final String ROLE_ADMINISTRATORS = "Administrators";

	public static final boolean TESTING_JIRA_5_OR_NEWER;
	public static final boolean TESTING_JIRA_6_OR_NEWER;
	public static final int START_PROGRESS_TRANSITION_ID = 4;
	public static final int STOP_PROGRESS_TRANSITION_ID = 301;
	public static final String NUMERIC_CUSTOMFIELD_ID = "customfield_10000";
	public static final String NUMERIC_CUSTOMFIELD_TYPE = "com.atlassian.jira.plugin.system.customfieldtypes:float";
	public static final String NUMERIC_CUSTOMFIELD_TYPE_V5 = "number";
	public static final String TEXT_CUSTOMFIELD_ID = "customfield_10011";
	private static final LocalTestEnvironmentData environmentData = new LocalTestEnvironmentData();
	private static final String URI_INTERFIX_FOR_USER;

	public static final String GROUP_JIRA_ADMINISTRATORS = "jira-administrators";
	public static final int CURRENT_BUILD_NUMBER;
	public static final ImmutableMap<String, String> AVATAR_SIZE_TO_NAME_MAP;

	static {
		try {
			final com.atlassian.jira.rest.client.api.JiraRestClientFactory clientFactory = new AsynchronousJiraRestClientFactory();
			final com.atlassian.jira.rest.client.api.JiraRestClient client = clientFactory.create(environmentData.getBaseUrl()
					.toURI(), new BasicHttpAuthenticationHandler("admin", "admin"));
			CURRENT_BUILD_NUMBER = client.getMetadataClient().getServerInfo().claim().getBuildNumber();
			TESTING_JIRA_5_OR_NEWER = CURRENT_BUILD_NUMBER > ServerVersionConstants.BN_JIRA_5;
			TESTING_JIRA_6_OR_NEWER = CURRENT_BUILD_NUMBER > ServerVersionConstants.BN_JIRA_6;
			// remove it when https://jdog.atlassian.com/browse/JRADEV-7691 is fixed
			URI_INTERFIX_FOR_USER = TESTING_JIRA_5_OR_NEWER ? "2" : "latest";

			// avatar size names (changed in JIRA 6.0)
			if (CURRENT_BUILD_NUMBER >= 6060) {
				AVATAR_SIZE_TO_NAME_MAP = ImmutableMap.<String, String>builder().put("16x16", "xsmall")
						.put("24x24", "small").put("32x32", "medium").put("48x48", "").build();
			}
			else {
				AVATAR_SIZE_TO_NAME_MAP = ImmutableMap.of("16x16", "small", "48x48", "");
			}

			// users
			USER1_FULL = new User(getUserUri("wseliga"), "wseliga", "Wojciech Seliga", "wojciech.seliga@spartez.com", null, buildUserAvatarUris(null, 10082L), null);
			USER1 = new BasicUser(USER1_FULL.getSelf(), USER1_FULL.getName(), USER1_FULL.getDisplayName());
			USER1_LATEST = new BasicUser(getLatestUserUri("wseliga"), "wseliga", "Wojciech Seliga");
			USER1_60 = TESTING_JIRA_6_OR_NEWER ? USER1 : USER1_LATEST;

			USER2 = new BasicUser(getUserUri("user"), "user", "My Test User");
			USER2_LATEST = new BasicUser(getLatestUserUri("user"), "user", "My Test User");
			USER2_60 = TESTING_JIRA_6_OR_NEWER ? USER2 : USER2_LATEST;

			USER_SLASH = new BasicUser(getUserUri("a/user/with/slash"), "a/user/with/slash", "A User with / in its username");
			USER_SLASH_LATEST = new BasicUser(getLatestUserUri("a/user/with/slash"), "a/user/with/slash", "A User with / in its username");
			USER_SLASH_60 = TESTING_JIRA_6_OR_NEWER ? USER_SLASH : USER_SLASH_LATEST;


			USER_ADMIN_FULL = new User(getUserUri("admin"), "admin", "Administrator", "wojciech.seliga@spartez.com", null, buildUserAvatarUris("admin", 10054L), null);
			USER_ADMIN = new BasicUser(USER_ADMIN_FULL.getSelf(), USER_ADMIN_FULL.getName(), USER_ADMIN_FULL.getDisplayName());
			USER_ADMIN_LATEST = new BasicUser(getLatestUserUri("admin"), "admin", "Administrator");
			USER_ADMIN_60 = TESTING_JIRA_6_OR_NEWER ? USER_ADMIN : USER_ADMIN_LATEST;
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

	}

	public static URI buildUserAvatarUri(@Nullable String userName, Long avatarId, String size) {
		// secure/useravatar?size=small&ownerId=admin&avatarId=10054
		final StringBuilder sb = new StringBuilder("secure/useravatar?");

		// optional size name
		final String sizeName = AVATAR_SIZE_TO_NAME_MAP.get(size);
		if (StringUtils.isNotBlank(sizeName)) {
			sb.append("size=").append(sizeName).append("&");
		}

		// Optional user name
		if (StringUtils.isNotBlank(userName)) {
			sb.append("ownerId=").append(userName).append("&");
		}

		// avatar Id
		sb.append("avatarId=").append(avatarId);
		return resolveURI(sb.toString());
	}

	public static Map<String, URI> buildUserAvatarUris(@Nullable String user, Long avatarId) {
		final ImmutableMap.Builder<String, URI> builder = ImmutableMap.builder();
		for (String size : AVATAR_SIZE_TO_NAME_MAP.keySet()) {
			builder.put(size, buildUserAvatarUri(user, avatarId, size));
		}
		return builder.build();
	}

	public static URI getUserUri(String username) {
		try {
			return UriBuilder.fromUri(environmentData.getBaseUrl().toURI()).path("/rest/api/" +
					URI_INTERFIX_FOR_USER + "/user").queryParam("username", username).build();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private static URI getLatestUserUri(String username) throws URISyntaxException {
		return UriBuilder.fromUri(environmentData.getBaseUrl().toURI()).path("/rest/api/latest/user")
				.queryParam("username", username).build();
	}

	public static URI concat(URI uri, String path) {
		return UriBuilder.fromUri(uri).path(path).build();
	}

	public static URI resolveURI(URI relativeUri) {
		try {
			// resolve would remove "jira" from context path, so we must add / to the end
			return concat(environmentData.getBaseUrl().toURI(), "/").resolve(relativeUri);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public static URI resolveURI(String relativeUri) {
		return resolveURI(TestUtil.toUri(relativeUri));
	}

}
