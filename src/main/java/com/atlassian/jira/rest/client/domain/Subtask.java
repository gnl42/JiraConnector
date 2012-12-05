package com.atlassian.jira.rest.client.domain;

import com.atlassian.jira.rest.client.IdentifiableEntity;
import com.google.common.base.Objects;

import javax.annotation.Nullable;
import java.net.URI;

/**
 *
 */
public class Subtask implements IdentifiableEntity<Long> {

	private final String issueKey;
    @Nullable
    private final Long id;
    private final URI issueUri;
	private final String summary;
	private final IssueType issueType;
	private final Status status;

	public Subtask(String issueKey, @Nullable Long id, URI issueUri, String summary, IssueType issueType, Status status) {
		this.issueKey = issueKey;
        this.id = id;
        this.issueUri = issueUri;
		this.summary = summary;
		this.issueType = issueType;
		this.status = status;
	}

	public String getIssueKey() {
		return issueKey;
	}

	public URI getIssueUri() {
		return issueUri;
	}

	public String getSummary() {
		return summary;
	}

	public IssueType getIssueType() {
		return issueType;
	}

	public Status getStatus() {
		return status;
	}

    @Override
    @Nullable
    public Long getId() {
        return id;
    }

    @Override
	public String toString() {
		return Objects.toStringHelper(this).addValue(super.toString()).
				add("issueKey", issueKey).
                add("id", id).
				add("issueUri", issueUri).
				add("summary", summary).
				add("issueType", issueType).
				add("status", status).
				toString();
	}


	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Subtask) {
			Subtask that = (Subtask) obj;
			return super.equals(obj) && Objects.equal(this.issueKey, that.issueKey)
                    && Objects.equal(this.id, that.id)
					&& Objects.equal(this.issueUri, that.issueUri)
					&& Objects.equal(this.summary, that.summary)
					&& Objects.equal(this.issueType, that.issueType)
					&& Objects.equal(this.status, that.status);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), issueKey, id, issueUri, summary, issueType, status);
	}
}
