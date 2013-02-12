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

import com.atlassian.jira.nimblefunctests.annotation.JiraBuildNumberDependent;
import com.atlassian.jira.nimblefunctests.annotation.LongCondition;
import com.atlassian.jira.nimblefunctests.annotation.Restore;
import com.atlassian.jira.rest.client.IntegrationTestUtil;
import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.api.domain.EntityHelper;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.VersionRelatedIssuesCount;
import com.atlassian.jira.rest.client.api.domain.input.VersionInput;
import com.atlassian.jira.rest.client.api.domain.input.VersionInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.VersionPosition;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.hamcrest.collection.IsEmptyIterable;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import java.net.URI;

import static com.atlassian.jira.rest.client.TestUtil.getLastPathSegment;
import static com.atlassian.jira.rest.client.internal.ServerVersionConstants.BN_JIRA_6;
import static com.google.common.collect.Iterables.toArray;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.*;

@Restore(TestConstants.DEFAULT_JIRA_DUMP_FILE)
public class AsynchronousVersionRestClientTest extends AbstractAsynchronousRestClientTest {

	@Test
	public void testCreateAndUpdateVersion() throws Exception {
		if (!isJira4x4OrNewer()) {
			return;
		}

		assertThat(Iterables.transform(client.getProjectClient().getProject("TST").claim().getVersions(),
				new VersionToNameMapper()), containsInAnyOrder("1.1", "1"));

		final VersionInput versionInput = VersionInput.create("TST", "My newly created version", "A description\nwith\new line", null, false, false);
		final Version version = client.getVersionRestClient().createVersion(versionInput).claim();
		assertVersionInputAndVersionEquals(versionInput, version);
		assertNotNull(version.getId());
		assertThat(Iterables.transform(client.getProjectClient().getProject("TST").claim()
				.getVersions(), new VersionToNameMapper()),
				containsInAnyOrder("1.1", "1", versionInput.getName()));

		TestUtil.assertErrorCode(Response.Status.BAD_REQUEST, "A version with this name already exists in this project.", new Runnable() {
			@Override
			public void run() {
				client.getVersionRestClient().createVersion(versionInput).claim();
			}
		});


		final VersionInput versionInput2 = VersionInput.create("TST", "My newly created version2", "A description\nwith\new line", null, false, false);
		setAnonymousMode();
		TestUtil.assertErrorCode(IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER ? Response.Status.NOT_FOUND
				: Response.Status.UNAUTHORIZED, new Runnable() {
			@Override
			public void run() {
				client.getVersionRestClient().createVersion(versionInput2).claim();
			}
		});

		setUser2();
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, "Project with key 'TST' either does not exist or you do not have permission to create versions in it.", new Runnable() {
			@Override
			public void run() {
				client.getVersionRestClient().createVersion(versionInput2).claim();
			}
		});

		// now version updates
		setAdmin();
		final VersionInput newVersionInput = new VersionInputBuilder(versionInput.getProjectKey(), version)
				.setDescription("my updated description").setReleased(true).setName("my updated name").build();
		client.getVersionRestClient().updateVersion(version.getSelf(), newVersionInput).claim();
		final Version modifiedVersion = client.getVersionRestClient().updateVersion(version.getSelf(), newVersionInput).claim();
		assertVersionInputAndVersionEquals(newVersionInput, modifiedVersion);

		final VersionInput duplicateVersionInput = new VersionInputBuilder("TST", modifiedVersion).setName("1.1").build();
		TestUtil.assertErrorCode(Response.Status.BAD_REQUEST, "A version with this name already exists in this project.", new Runnable() {
			@Override
			public void run() {
				client.getVersionRestClient().updateVersion(modifiedVersion.getSelf(), duplicateVersionInput).claim();
			}
		});

		setAnonymousMode();
		TestUtil.assertErrorCode(IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER ? Response.Status.NOT_FOUND
				: Response.Status.UNAUTHORIZED, new Runnable() {
			@Override
			public void run() {
				client.getVersionRestClient().updateVersion(modifiedVersion.getSelf(), newVersionInput).claim();
			}
		});

		setAdmin();
		final Version restrictedVersion = client.getVersionRestClient().createVersion(new VersionInputBuilder("RST")
				.setName("My version").build()).claim();
		final VersionInput restrictedVersionInput = new VersionInputBuilder("RST", restrictedVersion)
				.setDescription("another description").build();
		setUser2();
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, "You must have browse project rights in order to view versions.", new Runnable() {
			@Override
			public void run() {
				client.getVersionRestClient().updateVersion(restrictedVersion.getSelf(), restrictedVersionInput).claim();
			}
		});
	}

	private void assertVersionInputAndVersionEquals(VersionInput versionInput, Version version) {
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
		final Iterable<Version> versionsInTheBeggining = client.getProjectClient().getProject("TST").claim().getVersions();
		final VersionInput versionInput = VersionInput
				.create("TST", "My newly created version", "A description\nwith\new line", null, false, false);
		final Version version = client.getVersionRestClient().createVersion(versionInput).claim();
		assertEquals(version, client.getVersionRestClient().getVersion(version.getSelf()).claim());

		setAnonymousMode();
		// weird - inconsistent with PUT/POST
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, "You must have browse project rights in order to view versions.", new Runnable() {
			@Override
			public void run() {
				client.getVersionRestClient().getVersion(version.getSelf()).claim();
			}
		});

		setAdmin();
		TestUtil.assertErrorCodeWithRegexp(Response.Status.NOT_FOUND, "Could not find version for id .*", new Runnable() {
			@Override
			public void run() {
				client.getVersionRestClient().getVersion(TestUtil.toUri(version.getSelf().toString() + "9")).claim();
			}
		});

		setUser1();
		assertEquals(version, client.getVersionRestClient().getVersion(version.getSelf()).claim());

		TestUtil.assertErrorCode(Response.Status.UNAUTHORIZED, "The user wseliga does not have permission to complete this operation.", new Runnable() {
			@Override
			public void run() {
				client.getVersionRestClient().removeVersion(version.getSelf(), null, null).claim();
			}
		});

		setAdmin();
		client.getVersionRestClient().removeVersion(version.getSelf(), null, null).claim();
		TestUtil.assertErrorCodeWithRegexp(Response.Status.NOT_FOUND, "Could not find version for id .*", new Runnable() {
			@Override
			public void run() {
				client.getVersionRestClient().getVersion(version.getSelf()).claim();
			}
		});

		assertThat(client.getProjectClient().getProject("TST").claim()
				.getVersions(), containsInAnyOrder(toArray(versionsInTheBeggining, Version.class)));
		for (Version ver : versionsInTheBeggining) {
			client.getVersionRestClient().removeVersion(ver.getSelf(), null, null).claim();
		}
		assertThat(client.getProjectClient().getProject("TST").claim().getVersions(), IsEmptyIterable.<Version>emptyIterable());
	}

    @JiraBuildNumberDependent(value = BN_JIRA_6, condition = LongCondition.LESS_THAN)
	@Test
	public void testDeleteAndMoveVersionBefore6_0() {
		if (!isJira4x4OrNewer()) {
			return;
		}
		final Issue issue = client.getIssueClient().getIssue("TST-2").claim();
		assertThat(Iterables.transform(issue.getFixVersions(), new VersionToNameMapper()), containsInAnyOrder("1.1"));
		assertThat(Iterables.transform(issue.getAffectedVersions(), new VersionToNameMapper()), containsInAnyOrder("1", "1.1"));

		final Version version1 = EntityHelper.findEntityByName(client.getProjectClient().getProject("TST").claim()
				.getVersions(), "1");

		final Version version = Iterables.getOnlyElement(issue.getFixVersions());
		final URI fakeVersionUri = TestUtil.toUri("http://localhost/version/3432");
		final URI fakeVersionUri2 = TestUtil.toUri("http://localhost/version/34323");
		// @todo expected error code should be rather NOT FOUND in all cases below - see JRA-25045
		assertInvalidMoveToVersion(version.getSelf(), fakeVersionUri, null, "The fix version with id " +
				getLastPathSegment(fakeVersionUri) + " does not exist.", Response.Status.BAD_REQUEST);
		// @todo fix when bug JRA-25044 is fixed
		assertInvalidMoveToVersion(version.getSelf(), TestUtil.toUri("http://localhost/version/fdsa34323"), null,
				"Could not find version for id '-1'", Response.Status.NOT_FOUND);
		assertInvalidMoveToVersion(version.getSelf(), null, fakeVersionUri2, "The affects version with id " +
				getLastPathSegment(fakeVersionUri2) + " does not exist.", Response.Status.BAD_REQUEST);
		assertInvalidMoveToVersion(version.getSelf(), fakeVersionUri, fakeVersionUri2, "The affects version with id " +
				getLastPathSegment(fakeVersionUri2) + " does not exist.", Response.Status.BAD_REQUEST);

		assertEquals(1, client.getVersionRestClient().getNumUnresolvedIssues(version.getSelf()).claim().intValue());
		assertEquals(new VersionRelatedIssuesCount(version.getSelf(), 1, 1), client.getVersionRestClient()
				.getVersionRelatedIssuesCount(version.getSelf()).claim());
		assertEquals(new VersionRelatedIssuesCount(version.getSelf(), 1, 1), client.getVersionRestClient()
				.getVersionRelatedIssuesCount(version.getSelf()).claim());

		// now removing the first version
		client.getVersionRestClient().removeVersion(version.getSelf(), version1.getSelf(), version1.getSelf()).claim();
		final Issue issueAfterVerRemoval = client.getIssueClient().getIssue("TST-2").claim();
		assertThat(Iterables.transform(issueAfterVerRemoval
				.getFixVersions(), new VersionToNameMapper()), containsInAnyOrder("1"));
		assertThat(Iterables.transform(issueAfterVerRemoval
				.getAffectedVersions(), new VersionToNameMapper()), containsInAnyOrder("1"));
		assertThat(Iterables.transform(client.getProjectClient().getProject("TST").claim()
				.getVersions(), new VersionToNameMapper()),
				containsInAnyOrder("1"));

		TestUtil.assertErrorCode(Response.Status.BAD_REQUEST, "You cannot move the issues to the version being deleted.", new Runnable() {
			@Override
			public void run() {
				client.getVersionRestClient().removeVersion(version1.getSelf(), version1.getSelf(), version1.getSelf()).claim();
			}
		});

		// now removing the other version
		client.getVersionRestClient().removeVersion(version1.getSelf(), null, null).claim();
		final Issue issueAfter2VerRemoval = client.getIssueClient().getIssue("TST-2").claim();
		assertThat(Iterables.transform(issueAfter2VerRemoval.getFixVersions(), new VersionToNameMapper()), IsEmptyIterable
				.<String>emptyIterable());
		assertThat(Iterables.transform(issueAfter2VerRemoval.getAffectedVersions(), new VersionToNameMapper()), IsEmptyIterable
				.<String>emptyIterable());
	}

	@JiraBuildNumberDependent(BN_JIRA_6)
	@Test
	public void testDeleteAndMoveVersion() {
		final Issue issue = client.getIssueClient().getIssue("TST-2").claim();
		assertThat(Iterables.transform(issue.getFixVersions(), new VersionToNameMapper()), containsInAnyOrder("1.1"));
		assertThat(Iterables.transform(issue.getAffectedVersions(), new VersionToNameMapper()), containsInAnyOrder("1", "1.1"));

		final Version version1 = EntityHelper.findEntityByName(client.getProjectClient().getProject("TST").claim()
				.getVersions(), "1");

		final Version version = Iterables.getOnlyElement(issue.getFixVersions());
		final URI fakeVersionUri = TestUtil.toUri("http://localhost/version/3432");
		final URI fakeVersionUri2 = TestUtil.toUri("http://localhost/version/34323");
		// @todo expected error code should be rather NOT FOUND in all cases below - see JRA-25045
		assertInvalidMoveToVersion(version.getSelf(), fakeVersionUri, null, "The fix version with id " +
				getLastPathSegment(fakeVersionUri) + " does not exist.", Response.Status.BAD_REQUEST);
		// @todo fix when bug JRA-25044 is fixed
		assertInvalidMoveToVersion(version.getSelf(), TestUtil.toUri("http://localhost/version/fdsa34323"), null,
				"Could not find version for id 'fdsa34323'", Response.Status.NOT_FOUND);
		assertInvalidMoveToVersion(version.getSelf(), null, fakeVersionUri2, "The affects version with id " +
				getLastPathSegment(fakeVersionUri2) + " does not exist.", Response.Status.BAD_REQUEST);
		assertInvalidMoveToVersion(version.getSelf(), fakeVersionUri, fakeVersionUri2, "The affects version with id " +
				getLastPathSegment(fakeVersionUri2) + " does not exist.", Response.Status.BAD_REQUEST);

		assertEquals(1, client.getVersionRestClient().getNumUnresolvedIssues(version.getSelf()).claim().intValue());
		assertEquals(new VersionRelatedIssuesCount(version.getSelf(), 1, 1), client.getVersionRestClient()
				.getVersionRelatedIssuesCount(version.getSelf()).claim());
		assertEquals(new VersionRelatedIssuesCount(version.getSelf(), 1, 1), client.getVersionRestClient()
				.getVersionRelatedIssuesCount(version.getSelf()).claim());

		// now removing the first version
		client.getVersionRestClient().removeVersion(version.getSelf(), version1.getSelf(), version1.getSelf()).claim();
		final Issue issueAfterVerRemoval = client.getIssueClient().getIssue("TST-2").claim();
		assertThat(Iterables.transform(issueAfterVerRemoval
				.getFixVersions(), new VersionToNameMapper()), containsInAnyOrder("1"));
		assertThat(Iterables.transform(issueAfterVerRemoval
				.getAffectedVersions(), new VersionToNameMapper()), containsInAnyOrder("1"));
		assertThat(Iterables.transform(client.getProjectClient().getProject("TST").claim()
				.getVersions(), new VersionToNameMapper()),
				containsInAnyOrder("1"));

		TestUtil.assertErrorCode(Response.Status.BAD_REQUEST, "You cannot move the issues to the version being deleted.", new Runnable() {
			@Override
			public void run() {
				client.getVersionRestClient().removeVersion(version1.getSelf(), version1.getSelf(), version1.getSelf()).claim();
			}
		});

		// now removing the other version
		client.getVersionRestClient().removeVersion(version1.getSelf(), null, null).claim();
		final Issue issueAfter2VerRemoval = client.getIssueClient().getIssue("TST-2").claim();
		assertThat(Iterables.transform(issueAfter2VerRemoval.getFixVersions(), new VersionToNameMapper()), IsEmptyIterable
				.<String>emptyIterable());
		assertThat(Iterables.transform(issueAfter2VerRemoval.getAffectedVersions(), new VersionToNameMapper()), IsEmptyIterable
				.<String>emptyIterable());
	}

	private void assertInvalidMoveToVersion(final URI versionUri, @Nullable final URI moveFixIssuesToVersionUri,
			@Nullable final URI moveAffectedIssuesToVersionUri, final String expectedErrorMsg, final Response.Status status) {
		TestUtil.assertErrorCode(status, expectedErrorMsg, new Runnable() {
			@Override
			public void run() {
				client.getVersionRestClient().removeVersion(versionUri, moveFixIssuesToVersionUri, moveAffectedIssuesToVersionUri)
						.claim();
			}
		});
	}

	@Test
	public void testMoveVersion() {
		if (!isJira4x4OrNewer()) {
			return;
		}
		final Version v3 = client.getVersionRestClient().createVersion(VersionInput
				.create("TST", "my added version", "a description", null, false, false)).claim();
		assertProjectHasOrderedVersions("TST", "1", "1.1", v3.getName());
		client.getVersionRestClient().moveVersion(v3.getSelf(), VersionPosition.FIRST).claim();
		assertProjectHasOrderedVersions("TST", v3.getName(), "1", "1.1");
		client.getVersionRestClient().moveVersion(v3.getSelf(), VersionPosition.LAST).claim();
		assertProjectHasOrderedVersions("TST", "1", "1.1", v3.getName());
		client.getVersionRestClient().moveVersion(v3.getSelf(), VersionPosition.EARLIER).claim();
		assertProjectHasOrderedVersions("TST", "1", v3.getName(), "1.1");
		client.getVersionRestClient().moveVersion(v3.getSelf(), VersionPosition.EARLIER).claim();
		assertProjectHasOrderedVersions("TST", v3.getName(), "1", "1.1");
		client.getVersionRestClient().moveVersion(v3.getSelf(), VersionPosition.LATER).claim();
		assertProjectHasOrderedVersions("TST", "1", v3.getName(), "1.1");
		client.getVersionRestClient().moveVersion(v3.getSelf(), VersionPosition.LATER).claim();
		assertProjectHasOrderedVersions("TST", "1", "1.1", v3.getName());
		// later for the last version means nothing - but also no error
		client.getVersionRestClient().moveVersion(v3.getSelf(), VersionPosition.LATER).claim();
		assertProjectHasOrderedVersions("TST", "1", "1.1", v3.getName());

		setUser1();
		TestUtil.assertErrorCode(Response.Status.UNAUTHORIZED, "You must have global or project administrator rights in order to modify versions.", new Runnable() {
			@Override
			public void run() {
				client.getVersionRestClient().moveVersion(v3.getSelf(), VersionPosition.FIRST).claim();
			}
		});

	}

	@Test
	public void testMoveVersionAfter() {
		if (!isJira4x4OrNewer()) {
			return;
		}
		final Version v3 = client.getVersionRestClient().createVersion(VersionInput
				.create("TST", "my added version", "a description", null, false, false)).claim();
		final Version v4 = client.getVersionRestClient().createVersion(VersionInput
				.create("TST", "my added version2", "a description2", null, true, false)).claim();
		final Version v1 = Iterables.get(client.getProjectClient().getProject("TST").claim().getVersions(), 0);
		final String v1n = v1.getName();
		final String v3n = v3.getName();
		final String v4n = v4.getName();
		assertProjectHasOrderedVersions("TST", v1n, "1.1", v3n, v4n);
		client.getVersionRestClient().moveVersionAfter(v3.getSelf(), v4.getSelf()).claim();
		assertProjectHasOrderedVersions("TST", v1n, "1.1", v4n, v3n);
		client.getVersionRestClient().moveVersionAfter(v3.getSelf(), v1.getSelf()).claim();
		assertProjectHasOrderedVersions("TST", v1n, v3n, "1.1", v4n);
		client.getVersionRestClient().moveVersionAfter(v1.getSelf(), v4.getSelf()).claim();
		assertProjectHasOrderedVersions("TST", v3n, "1.1", v4n, v1n);

		setUser1();
		TestUtil.assertErrorCode(Response.Status.UNAUTHORIZED, "You must have global or project administrator rights in order to modify versions.", new Runnable() {
			@Override
			public void run() {
				client.getVersionRestClient().moveVersionAfter(v3.getSelf(), v4.getSelf()).claim();
			}
		});

		setAnonymousMode();
		TestUtil.assertErrorCode(Response.Status.UNAUTHORIZED, new Runnable() {
			@Override
			public void run() {
				client.getVersionRestClient().moveVersionAfter(v3.getSelf(), v4.getSelf()).claim();
			}
		});
	}


	private void assertProjectHasOrderedVersions(String projectKey, String... expectedVersions) {
		assertEquals(Lists.newArrayList(expectedVersions), Lists.newArrayList(Iterables.transform(client.getProjectClient()
				.getProject(projectKey).claim().getVersions(), new VersionToNameMapper())));
	}
}
