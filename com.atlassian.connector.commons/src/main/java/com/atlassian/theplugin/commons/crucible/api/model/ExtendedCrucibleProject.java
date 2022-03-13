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
import java.util.Collection;

@SuppressWarnings("serial")
public class ExtendedCrucibleProject extends BasicProject {
    private final Collection<String> allowedReviewers;

	public ExtendedCrucibleProject(@NotNull String id, @NotNull String key, @NotNull String name) {
		this(id, key, name, null);
	}

	public ExtendedCrucibleProject(@NotNull String id, @NotNull String key, @NotNull String name,
			@Nullable Collection<String> allowedReviewers) {
		this(id, key, name, allowedReviewers, null, null, true, null, true);
    }

	public ExtendedCrucibleProject(@NotNull String id, @NotNull String key, @NotNull String name,
			@Nullable Collection<String> allowedReviewers,
			@Nullable Collection<String> defaultReviewers,
			@Nullable String defaultRepository, boolean allowJoin, Integer defaultDuration, boolean moderatorEnabled) {
		super(id, key, name, defaultReviewers, defaultRepository, allowJoin, defaultDuration, moderatorEnabled);
		this.allowedReviewers = allowedReviewers;
	}

	@Nullable
    public Collection<String> getAllowedReviewers() {
        return allowedReviewers;
    }

}
