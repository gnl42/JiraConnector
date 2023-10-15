package me.glindholm.jira.rest.client.api.domain;

import java.net.URI;
import java.util.Objects;

/**
 *
 */
public class Subtask {

    private final String issueKey;
    private final URI issueUri;
    private final String summary;
    private final IssueType issueType;
    private final Status status;

    public Subtask(final String issueKey, final URI issueUri, final String summary, final IssueType issueType, final Status status) {
        this.issueKey = issueKey;
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
    public String toString() {
        return "Subtask [issueKey=" + issueKey + ", issueUri=" + issueUri + ", summary=" + summary + ", issueType=" + issueType + ", status=" + status + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final Subtask that) {
            return super.equals(obj) && Objects.equals(issueKey, that.issueKey) && Objects.equals(issueUri, that.issueUri)
                    && Objects.equals(summary, that.summary) && Objects.equals(issueType, that.issueType) && Objects.equals(status, that.status);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), issueKey, issueUri, summary, issueType, status);
    }

}
