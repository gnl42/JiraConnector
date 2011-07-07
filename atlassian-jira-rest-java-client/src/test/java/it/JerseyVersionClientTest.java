/*
 * Copyright (C) 2011 Atlassian
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

package it;

import com.atlassian.jira.rest.client.IterableMatcher;
import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.rest.client.domain.input.VersionInput;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class JerseyVersionClientTest extends AbstractRestoringJiraStateJerseyRestClientTest {
	@Test
	public void testCreateVersion() throws Exception {
		assertThat(Iterables.transform(client.getProjectClient().getProject("TST", pm).getVersions(),
				new VersionToNameMapper()), IterableMatcher.hasOnlyElements("1.1", "1"));

		final VersionInput versionInput = VersionInput.create("TST", "My newly created version", "A description\nwith\new line", null, false, false);
		final Version version = client.getVersionClient().createVersion(versionInput, pm);
		assertEquals(version.getName(), versionInput.getName());
		assertEquals(version.getDescription(), versionInput.getDescription());
		assertEquals(version.getReleaseDate(), versionInput.getReleaseDate());
		assertEquals(version.isArchived(), versionInput.isArchived());
		assertEquals(version.isReleased(), versionInput.isReleased());
		assertThat(Iterables.transform(client.getProjectClient().getProject("TST", pm).getVersions(), new VersionToNameMapper()),
				IterableMatcher.hasOnlyElements("1.1", "1", versionInput.getName()));

	}

	private static class VersionToNameMapper implements Function<Version, String> {
		@Override
		public String apply(Version from) {
			return from.getName();
		}
	}
}
