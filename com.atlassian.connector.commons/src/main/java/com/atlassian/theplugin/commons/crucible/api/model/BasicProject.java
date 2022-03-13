/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.commons.crucible.api.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.Serializable;
import java.util.Collection;

@SuppressWarnings("serial")
public class BasicProject implements Serializable {
	private final String id;
	private final String key;
	private final String name;

	private final Collection<String> defaultReviewers;
	private final String defaultRepository;
	private final boolean joiningAllowed;
	private final Integer defaultDuration;
	private final boolean moderatorEnabled;

    private static final int HASH_INT = 31;

	public BasicProject(@NotNull String id, @NotNull String key, @NotNull String name) {
		this(id, key, name, null, null, true, null, true);
	}

	public BasicProject(@NotNull String id, @NotNull String key, @NotNull String name,
			@Nullable Collection<String> defaultReviewers,
			@Nullable String defaultRepository, boolean allowJoin, Integer defaultDuration, boolean moderatorEnabled) {
		this.id = id;
		this.key = key;
		this.name = name;
		this.defaultReviewers = defaultReviewers;
		this.defaultRepository = defaultRepository;
		this.joiningAllowed = allowJoin;
		this.defaultDuration = defaultDuration;
		this.moderatorEnabled = moderatorEnabled;
	}

	@NotNull
    public String getId() {
		return id;
	}

	@NotNull
	public String getKey() {
		return key;
	}

	@NotNull
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		BasicProject that = (BasicProject) o;

		if (id != null ? !id.equals(that.id) : that.id != null) {
			return false;
		}
		if (key != null ? !key.equals(that.key) : that.key != null) {
			return false;
		}
		if (name != null ? !name.equals(that.name) : that.name != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		result = (id != null ? id.hashCode() : 0);
		result = HASH_INT * result + (key != null ? key.hashCode() : 0);
		result = HASH_INT * result + (name != null ? name.hashCode() : 0);
		return result;
	}

	public Collection<String> getDefaultReviewers() {
		return defaultReviewers;
	}

	public String getDefaultRepository() {
		return defaultRepository;
	}

	public boolean isJoiningAllowed() {
		return joiningAllowed;
	}

	public Integer getDefaultDuration() {
		return defaultDuration;
	}

	public boolean isModeratorEnabled() {
		return moderatorEnabled;
	}
}
