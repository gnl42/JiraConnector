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

@SuppressWarnings("serial")
public class Reviewer extends User {
	private final boolean completed;

	public Reviewer(@NotNull final String userName, final boolean completed) {
		super(userName);
		this.completed = completed;
	}

	public Reviewer(@NotNull final String userName, final String displayName, final boolean completed) {
		this(userName, displayName, completed, null);
	}

	public Reviewer(@NotNull final String userName, final String displayName, final boolean completed, final String avatarUrl) {
		super(userName, displayName, avatarUrl);
		this.completed = completed;
	}

	public Reviewer(@NotNull final String userName, final String displayName) {
		this(userName, displayName, false);
    }

    public boolean isCompleted() {
        return completed;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (completed ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Reviewer other = (Reviewer) obj;
		if (completed != other.completed) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Reviewer [[" + userName + "], [" + displayName + "], " + completed + "]";
	}

}