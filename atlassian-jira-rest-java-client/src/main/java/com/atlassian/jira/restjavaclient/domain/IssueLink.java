package com.atlassian.jira.restjavaclient.domain;

import com.google.common.base.Objects;

import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class IssueLink {
	private final String targetIssueKey;
	private final URI targetIssueUri;
	private final IssueLinkType issueLinkType;

	public IssueLink(String targetIssueKey, URI targetIssueUri, IssueLinkType issueLinkType) {
		this.targetIssueKey = targetIssueKey;
		this.targetIssueUri = targetIssueUri;
		this.issueLinkType = issueLinkType;
	}

	public String getTargetIssueKey() {
		return targetIssueKey;
	}

	public URI getTargetIssueUri() {
		return targetIssueUri;
	}

	public IssueLinkType getIssueLinkType() {
		return issueLinkType;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).
				add("targetIssueKey", targetIssueKey).
				add("targetIssueUri", targetIssueUri).
				add("issueLinkType", issueLinkType).
				toString();
	}
}
