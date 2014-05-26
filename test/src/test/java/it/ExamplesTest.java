package it;

import com.atlassian.jira.nimblefunctests.annotation.Restore;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.codehaus.jettison.json.JSONException;
import org.junit.Test;
import samples.Example1;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// Ignore "May produce NPE" warnings, as we know what we are doing in tests
@SuppressWarnings("ConstantConditions")
@Restore(TestConstants.DEFAULT_JIRA_DUMP_FILE)
public class ExamplesTest extends AbstractAsynchronousRestClientTest {

	@Test
	public void testExample1() throws URISyntaxException, JSONException, IOException {
		// -- run the example
		Example1.main(new String[]{environmentData.getBaseUrl().toString(), "-q"});

		// -- check state after example
		final Issue issue = client.getIssueClient().getIssue("TST-7", ImmutableList.copyOf(Lists.newArrayList(IssueRestClient
				.Expandos.values()))).claim();

		// votes
		final BasicVotes votes = issue.getVotes();
		assertNotNull(votes);
		assertEquals(1, votes.getVotes());
		assertEquals(true, votes.hasVoted());

		// watchers
		final BasicWatchers watchers = issue.getWatchers();
		assertNotNull(watchers);
		assertEquals(1, watchers.getNumWatchers());

		// resolution
		final Resolution resolution = issue.getResolution();
		assertNotNull(resolution);
		assertEquals("Incomplete", resolution.getName());

		if (isJira5xOrNewer()) {
			// changelog
			final Iterable<ChangelogGroup> changelog = issue.getChangelog();
			assertEquals(2, Iterables.size(changelog));
		}

		// comments
		final Iterable<Comment> comments = issue.getComments();
		assertNotNull(comments);
		assertEquals(1, Iterables.size(comments));
		final Comment comment = comments.iterator().next();
		assertEquals("My comment", comment.getBody());
		assertEquals("Administrator", comment.getAuthor().getDisplayName());
	}

}
