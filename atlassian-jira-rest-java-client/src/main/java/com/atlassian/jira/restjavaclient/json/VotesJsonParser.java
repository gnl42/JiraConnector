package com.atlassian.jira.restjavaclient.json;

import com.atlassian.jira.restjavaclient.domain.Votes;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class VotesJsonParser {
	public Votes parseVotes(JSONObject json) throws JSONException {
		final URI self = JsonParseUtil.getSelfUri(json);
		final int voteCount = json.getInt("votes");
		final boolean hasVoted = json.getBoolean("hasVoted");
		return new Votes(self, voteCount, hasVoted);
	}
}
