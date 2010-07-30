package com.atlassian.jira.restjavaclient.domain;

import com.atlassian.jira.restjavaclient.AddressableEntity;
import com.atlassian.jira.restjavaclient.ExpandableResource;

import java.net.URI;
import java.util.Collection;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.2
 */
public class Issue implements AddressableEntity, ExpandableResource {

    public Issue(URI self, String key) {
        this.self = self;
        this.key = key;
    }

    private final URI self;

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
		return null;
	}
}
