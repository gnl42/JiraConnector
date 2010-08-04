package com.atlassian.jira.restjavaclient.domain;

import com.atlassian.jira.restjavaclient.AddressableEntity;
import com.atlassian.jira.restjavaclient.ExpandableProperty;
import com.atlassian.jira.restjavaclient.ExpandableResource;

import java.net.URI;
import java.util.Collection;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class Issue implements AddressableEntity, ExpandableResource {

    public Issue(URI self, String key, Iterable<String> expandos, ExpandableProperty<Comment> comments, ExpandableProperty<Attachment> attachments) {
        this.self = self;
        this.key = key;
        this.expandos = expandos;
        this.comments = comments;
        this.attachments = attachments;
    }

    private final URI self;
    private final Iterable<String> expandos;
	private User reporter;
	private User assignee;
	private String key;
	private Collection<Field> fields;

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
}
