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
package me.glindholm.theplugin.commons.cfg;

public class ProjectId {

	private final String internal;

	public ProjectId(final String string) {
		if (string == null) {
			throw new NullPointerException();
		}
		internal = string;
	}

	public ProjectId() {
		internal = null;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ProjectId)) {
			return false;
		}

		if (internal == null) {
			return this == o;
		}

		final ProjectId projectId = (ProjectId) o;

		if (!internal.equals(projectId.internal)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		if (internal == null) {
			return super.hashCode();
		}
		return internal.hashCode();
	}
}
