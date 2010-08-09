package com.atlassian.jira.restjavaclient.domain;

import com.atlassian.jira.restjavaclient.AddressableEntity;
import com.atlassian.jira.restjavaclient.ExpandableProperty;
import com.atlassian.jira.restjavaclient.ExpandableResource;
import com.google.common.base.Objects;
import org.joda.time.DateTime;

import java.net.URI;
import java.util.Collection;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class Issue implements AddressableEntity, ExpandableResource {

    public Issue(URI self, String key, IssueType issueType, Iterable<String> expandos, ExpandableProperty<Comment> comments, ExpandableProperty<Attachment> attachments, Collection<Field> fields) {
        this.self = self;
        this.key = key;
        this.expandos = expandos;
        this.comments = comments;
        this.attachments = attachments;
		this.fields = fields;
		this.issueType = issueType;
	}

    private final URI self;
    private final Iterable<String> expandos;
	private User reporter;
	private User assignee;
	private String key;
	private Collection<Field> fields;
	private IssueType issueType;
	private DateTime creationDate;
	private DateTime updateDate;

	public User getReporter() {
		return reporter;
	}

	public User getAssignee() {
		return assignee;
	}

	public String getSummary() {
		return null;
	}


	public Iterable<Field> getFields() {
		return fields;
	}

	public String getKey() {
		return key;
	}

	public URI getSelf() {
		return self;
	}

	public Iterable<String> getExpandos() {
		return expandos;
	}

    private final ExpandableProperty<Attachment> attachments;

    public ExpandableProperty<Attachment> getAttachments() {
        return attachments;
    }

    public ExpandableProperty<Comment> getComments() {
        return comments;
    }

    private final ExpandableProperty<Comment> comments;

	@Override
	public String toString() {
		return Objects.toStringHelper(this).
				add("self", self).
				add("key", key).
				add("expandos", expandos).
				add("reporter", reporter).
				add("assignee", assignee).
				add("fields", fields).
				add("issueType", issueType).
				add("creationDate", creationDate).
				add("updateDate", updateDate).
				add("attachments", attachments).
				add("comments", comments).
				toString();
	}
}
