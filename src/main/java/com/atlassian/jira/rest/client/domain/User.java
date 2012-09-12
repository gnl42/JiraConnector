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

package com.atlassian.jira.rest.client.domain;

import com.atlassian.jira.rest.client.ExpandableProperty;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Map;

/**
 * Complete information about a single JIRA user
 *
 * @since v0.1
 */
public class User extends BasicUser {

	public static String S16_16 = "16x16";
	public static String S48_48 = "48x48";

	private final String emailAddress;

	private final ExpandableProperty<String> groups;

	private Map<String, URI> avatarUris;

	/**
	 * @since client 0.5, server: 4.4
	 */
	@Nullable
	private String timezone;
	
	public User(URI self, String name, String displayName, String emailAddress, ExpandableProperty<String> groups,
			Map<String, URI> avatarUris, @Nullable String timezone) {
		super(self, name, displayName);
		this.timezone = timezone;
		if (avatarUris.get(S48_48) == null) {
			throw new IllegalArgumentException("At least one avatar URL is expected - for 48x48");
		}
		this.emailAddress = emailAddress;
		this.avatarUris = Maps.newHashMap(avatarUris);
		this.groups = groups;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public URI getAvatarUri() {
		return avatarUris.get(S48_48);
	}

	/**
	 *
	 * @return user avatar image URI for 16x16 pixels
	 * @since 0.5 client, 5.0 server
	 */
	@Nullable
	public URI getSmallAvatarUri() {
		return avatarUris.get(S16_16);
	}

	/**
	 * As of JIRA 5.0 there can be several different user avatar URIs - for different size.
	 *
	 * @param sizeDefinition size like "16x16" or "48x48". URI for 48x48 should be always defined.
	 * @return URI for specified size or <code>null</code> when there is no avatar image with given dimensions specified for this user
	 */
	@Nullable
	public URI getAvatarUri(String sizeDefinition) {
		return avatarUris.get(sizeDefinition);
	}

	/**
	 * @return groups given user belongs to
	 */
	public ExpandableProperty<String> getGroups() {
		return groups;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof User) {
			User that = (User) obj;
			return super.equals(obj) && Objects.equal(this.emailAddress, that.emailAddress)
					&& Objects.equal(this.avatarUris, that.avatarUris);
		}
		return false;
	}


	/**
	 * @since client 0.5, server 4.4
	 * @return user timezone, like "Europe/Berlin" or <code>null</code> if timezone info is not available
	 */
	@Nullable
	public String getTimezone() {
		return timezone;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).addValue(super.toString()).
				add("emailAddress", emailAddress).
				add("avatarUris", avatarUris).
				add("groups", groups).
				toString();
	}

}
