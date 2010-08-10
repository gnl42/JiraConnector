package com.atlassian.jira.restjavaclient.domain;

import com.atlassian.jira.restjavaclient.AddressableEntity;
import com.google.common.base.Objects;

import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class Votes implements AddressableEntity {
	private final URI self;
	private final int votes;
	private final boolean hasVoted;

	public Votes(URI self, int votes, boolean hasVoted) {
		this.self = self;
		this.votes = votes;
		this.hasVoted = hasVoted;
	}

	public URI getSelf() {
		return self;
	}

	public int getVotes() {
		return votes;
	}

	public boolean isHasVoted() {
		return hasVoted;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).
				add("self", self).
				add("votes", votes).
				add("hasVoted", hasVoted).
				toString();
	}
}
