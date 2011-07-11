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
import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.rest.client.domain.VersionRelatedIssuesCount;
import com.atlassian.jira.rest.client.domain.input.VersionInput;
import com.atlassian.jira.rest.client.domain.input.VersionInputBuilder;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import java.net.URI;

import static com.atlassian.jira.rest.client.TestUtil.getLastPathSegment;
import static org.junit.Assert.assertThat;

public class JerseyVersionClientTest extends AbstractRestoringJiraStateJerseyRestClientTest {
	@Test
	public void testCreateAndUpdateVersion() throws Exception {
		if (!isJira4x4OrNewer()) {
			return;
		}

		assertThat(Iterables.transform(client.getProjectClient().getProject("TST", pm).getVersions(),
				new VersionToNameMapper()), IterableMatcher.hasOnlyElements("1.1", "1"));

		final VersionInput versionInput = VersionInput.create("TST", "My newly created version", "A description\nwith\new line", null, false, false);
		final Version version = client.getVersionClient().createVersion(versionInput, pm);
		assertEquals(versionInput, version);
		assertThat(Iterables.transform(client.getProjectClient().getProject("TST", pm).getVersions(), new VersionToNameMapper()),
				IterableMatcher.hasOnlyElements("1.1", "1", versionInput.getName()));

		TestUtil.assertErrorCode(Response.Status.BAD_REQUEST, "A version with this name already exists in this project.", new Runnable() {
			@Override
			public void run() {
				client.getVersionClient().createVersion(versionInput, pm);
			}
		});


		final VersionInput versionInput2 = VersionInput.create("TST", "My newly created version2", "A description\nwith\new line", null, false, false);
		setAnonymousMode();
		TestUtil.assertErrorCode(Response.Status.UNAUTHORIZED, new Runnable() {
			@Override
			public void run() {
				client.getVersionClient().createVersion(versionInput2, pm);
			}
		});

		setUser2();
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, "Project with key 'TST' either does not exist or you do not have permission to create versions in it.", new Runnable() {
			@Override
			public void run() {
				client.getVersionClient().createVersion(versionInput2, pm);
			}
		});

		// now version updates
		setAdmin();
		final VersionInput newVersionInput = new VersionInputBuilder(versionInput.getProjectKey(), version)
				.setDescription("my updated description").setReleased(true).setName("my updated name").build();
		client.getVersionClient().updateVersion(version.getSelf(), newVersionInput, pm);
		final Version modifiedVersion = client.getVersionClient().updateVersion(version.getSelf(), newVersionInput, pm);
		assertEquals(newVersionInput, modifiedVersion);

		final VersionInput duplicateVersionInput = new VersionInputBuilder("TST", modifiedVersion).setName("1.1").build();
		TestUtil.assertErrorCode(Response.Status.BAD_REQUEST, "A version with this name already exists in this project.", new Runnable() {
			@Override
			public void run() {
				client.getVersionClient().updateVersion(modifiedVersion.getSelf(), duplicateVersionInput, pm);
			}
		});

		setAnonymousMode();
		TestUtil.assertErrorCode(Response.Status.UNAUTHORIZED, new Runnable() {
			@Override
			public void run() {
				client.getVersionClient().updateVersion(modifiedVersion.getSelf(), newVersionInput, pm);
			}
		});

		setAdmin();
		final Version restrictedVersion = client.getVersionClient().createVersion(new VersionInputBuilder("RST").setName("My version").build(), pm);
		final VersionInput restrictedVersionInput = new VersionInputBuilder("RST", restrictedVersion).setDescription("another description").build();
		setUser2();
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, "You must have browse project rights in order to view versions.", new Runnable() {
			@Override
			public void run() {
				client.getVersionClient().updateVersion(restrictedVersion.getSelf(), restrictedVersionInput, pm);
			}
		});

	}

	private void assertEquals(VersionInput versionInput, Version version) {
		assertEquals(version.getName(), versionInput.getName());
		assertEquals(version.getDescription(), versionInput.getDescription());
		assertEquals(version.getReleaseDate(), versionInput.getReleaseDate());
		assertEquals(version.isArchived(), versionInput.isArchived());
		assertEquals(version.isReleased(), versionInput.isReleased());
	}


	private static class VersionToNameMapper implements Function<Version, String> {
		@Override
		public String apply(Version from) {
			return from.getName();
		}
	}

	@Test
	public void testGetAndRemoveVersion() {
		if (!isJira4x4OrNewer()) {
			return;
		}
		final Iterable<Version> versionsInTheBeggining = client.getProjectClient().getProject("TST", pm).getVersions();
		final VersionInput versionInput = VersionInput.create("TST", "My newly created version", "A description\nwith\new line", null, false, false);
		final Version version = client.getVersionClient().createVersion(versionInput, pm);
		assertEquals(version, client.getVersionClient().getVersion(version.getSelf(), pm));

		setAnonymousMode();
		// weird - inconsistent with PUT/POST
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, "You must have browse project rights in order to view versions.", new Runnable() {
			@Override
			public void run() {
				client.getVersionClient().getVersion(version.getSelf(), pm);
			}
		});

		setAdmin();
		TestUtil.assertErrorCodeWithRegexp(Response.Status.NOT_FOUND, "Could not find version for id .*", new Runnable() {
			@Override
			public void run() {
				client.getVersionClient().getVersion(TestUtil.toUri(version.getSelf().toString() + "9"), pm);
			}
		});

		setUser1();
		assertEquals(version, client.getVersionClient().getVersion(version.getSelf(), pm));

		TestUtil.assertErrorCode(Response.Status.UNAUTHORIZED, "The user wseliga does not have permission to complete this operation.", new Runnable() {
			@Override
			public void run() {
				client.getVersionClient().removeVersion(version.getSelf(), null, null, pm);
			}
		});

		setAdmin();
		client.getVersionClient().removeVersion(version.getSelf(), null, null, pm);
		TestUtil.assertErrorCodeWithRegexp(Response.Status.NOT_FOUND, "Could not find version for id .*", new Runnable() {
			@Override
			public void run() {
				client.getVersionClient().getVersion(version.getSelf(), pm);
			}
		});

		assertThat(client.getProjectClient().getProject("TST", pm).getVersions(), IterableMatcher.hasOnlyElements(versionsInTheBeggining));
		for (Version ver : versionsInTheBeggining) {
			client.getVersionClient().removeVersion(ver.getSelf(), null, null, pm);
		}
		assertThat(client.getProjectClient().getProject("TST", pm).getVersions(), IterableMatcher.<Version>isEmpty());

	}


	@Test
	public void testDeleteAndMoveVersion() {
		if (!isJira4x4OrNewer()) {
			return;
		}
		final Issue issue = client.getIssueClient().getIssue("TST-2", pm);
		assertThat(Iterables.transform(issue.getFixVersions(), new VersionToNameMapper()), IterableMatcher.hasOnlyElements("1.1"));
		assertThat(Iterables.transform(issue.getAffectedVersions(), new VersionToNameMapper()), IterableMatcher.hasOnlyElements("1", "1.1"));

		final Version version1 = Iterables.find(client.getProjectClient().getProject("TST", pm).getVersions(), new Predicate<Version>() {
			@Override
			public boolean apply(Version input) {
				return "1".equals(input.getName());
			}
		});

		final Version version = Iterables.getOnlyElement(issue.getFixVersions());
		final URI fakeVersionUri = TestUtil.toUri("http://localhost/version/3432");
		final URI fakeVersionUri2 = TestUtil.toUri("http://localhost/version/34323");
		assertInvalidMoveToVersion(version.getSelf(), fakeVersionUri, null, "The fix version with id " +
				getLastPathSegment(fakeVersionUri) + " does not exist.", Response.Status.BAD_REQUEST);
		// @todo fix when bug JRA-25044 is fixed
		assertInvalidMoveToVersion(version.getSelf(), TestUtil.toUri("http://localhost/version/fdsa34323"), null,
				"Could not find version for id '-1'", Response.Status.NOT_FOUND);
		assertInvalidMoveToVersion(version.getSelf(), null, fakeVersionUri2, "The affects version with id " +
				getLastPathSegment(fakeVersionUri2) + " does not exist.", Response.Status.BAD_REQUEST);
		assertInvalidMoveToVersion(version.getSelf(), fakeVersionUri, fakeVersionUri2, "The affects version with id " +
				getLastPathSegment(fakeVersionUri2) + " does not exist.", Response.Status.BAD_REQUEST);

		assertEquals(1, client.getVersionClient().getNumUnresolvedIssues(version.getSelf(), pm));
		assertEquals(new VersionRelatedIssuesCount(version.getSelf(), 1, 1), client.getVersionClient().getVersionRelatedIssuesCount(version.getSelf(), pm));
		assertEquals(new VersionRelatedIssuesCount(version.getSelf(), 1, 1), client.getVersionClient().getVersionRelatedIssuesCount(version.getSelf(), pm));

		// now removing the first version
		client.getVersionClient().removeVersion(version.getSelf(), version1.getSelf(), version1.getSelf(), pm);
		final Issue issueAfterVerRemoval = client.getIssueClient().getIssue("TST-2", pm);
		assertThat(Iterables.transform(issueAfterVerRemoval.getFixVersions(), new VersionToNameMapper()), IterableMatcher.hasOnlyElements("1"));
		assertThat(Iterables.transform(issueAfterVerRemoval.getAffectedVersions(), new VersionToNameMapper()), IterableMatcher.hasOnlyElements("1"));
		assertThat(Iterables.transform(client.getProjectClient().getProject("TST", pm).getVersions(), new VersionToNameMapper()),
				IterableMatcher.hasOnlyElements("1"));

		TestUtil.assertErrorCode(Response.Status.BAD_REQUEST, "You cannot move the issues to the version being deleted.", new Runnable() {
			@Override
			public void run() {
				client.getVersionClient().removeVersion(version1.getSelf(), version1.getSelf(), version1.getSelf(), pm);
			}
		});

		// now removing the other version
		client.getVersionClient().removeVersion(version1.getSelf(), null, null, pm);
		final Issue issueAfter2VerRemoval = client.getIssueClient().getIssue("TST-2", pm);
		assertThat(Iterables.transform(issueAfter2VerRemoval.getFixVersions(), new VersionToNameMapper()), IterableMatcher.<String>isEmpty());
		assertThat(Iterables.transform(issueAfter2VerRemoval.getAffectedVersions(), new VersionToNameMapper()), IterableMatcher.<String>isEmpty());
	}

	private void assertInvalidMoveToVersion(final URI versionUri, @Nullable final URI moveFixIssuesToVersionUri,
			@Nullable final URI moveAffectedIssuesToVersionUri, final String expectedErrorMsg, final Response.Status status) {
		TestUtil.assertErrorCode(status, expectedErrorMsg, new Runnable() {
			@Override
			public void run() {
				client.getVersionClient().removeVersion(versionUri, moveFixIssuesToVersionUri, moveAffectedIssuesToVersionUri, pm);
			}
		});
	}
}
