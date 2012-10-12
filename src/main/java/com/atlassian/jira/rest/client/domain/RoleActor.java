/*
 * Copyright (C) 2012 Atlassian
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

import com.atlassian.jira.rest.client.NamedEntity;
import com.google.common.base.Objects;

import javax.annotation.Nullable;
import java.net.URL;

/**
 * Association between users and project roles.
 * @since 1.0
 */
public class RoleActor implements NamedEntity {

	/**
	 * Default string identifying user with its implementation type.
	 */
	private static final String defaultRoleActorType = "atlassian-user-role-actor";

	private final long id;
	private final String displayName;
	private final String type;
	private final String name;
	private final URL avatarUrl;

	public RoleActor(long id, String displayName, String type, String name, @Nullable URL avatarUrl) {
		this.id = id;
		this.displayName = displayName;
		this.type = type;
		this.name = name;
		this.avatarUrl = avatarUrl;
	}

	/**
	 * Returns the default string identifying user with its implementation type.
	 * @return the default string identifying user with its implementation type.
	 */
	@SuppressWarnings("unused")
	public static String getDefaultRoleActorType() {
		return defaultRoleActorType;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Returns the viewable name of this role actor.
	 * @return the viewable name of this role actor.
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Returns a string that identifies the implementation type. This allows us to group common types.
	 * @return string identifying the implementation type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns an URL of the avatar of this role actor.
	 * @return an URL of the avatar of this role actor.
	 */
	public URL getAvatarUrl() {
		return avatarUrl;
	}

	/**
	 * Returns the unique identifier for this role actor.
	 * @return the unique identifier for this role actor.
	 */
	public long getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof RoleActor) {
			RoleActor that = (RoleActor) o;
			return Objects.equal(this.getName(), that.getName())
					&& Objects.equal(this.getAvatarUrl(), that.getAvatarUrl())
					&& Objects.equal(this.getType(), that.getType())
					&& Objects.equal(this.getDisplayName(), that.getDisplayName());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), name, avatarUrl, type, displayName);
	}

	@Override
	public String toString() {
		return getToStringHelper().toString();
	}

	public Objects.ToStringHelper getToStringHelper() {
		return Objects.toStringHelper(this)
				.add("displayName", displayName)
				.add("type", type)
				.add("name", name)
				.add("avatarUrl", avatarUrl);
	}
}
