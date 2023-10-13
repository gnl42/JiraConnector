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

    public Subtask(String issueKey, URI issueUri, String summary, IssueType issueType, Status status) {
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
    public boolean equals(Object obj) {
        if (obj instanceof Subtask) {
            Subtask that = (Subtask) obj;
            return super.equals(obj) && Objects.equals(this.issueKey, that.issueKey)
                    && Objects.equals(this.issueUri, that.issueUri)
                    && Objects.equals(this.summary, that.summary)
                    && Objects.equals(this.issueType, that.issueType)
                    && Objects.equals(this.status, that.status);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), issueKey, issueUri, summary, issueType, status);
    }

}
