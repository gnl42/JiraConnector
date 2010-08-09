package com.atlassian.jira.restjavaclient.domain;

import com.atlassian.jira.restjavaclient.AddressableEntity;
import com.google.common.base.Objects;

import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class IssueType implements AddressableEntity {
	private final URI self;

	private final String name;

	private final boolean isSubtask;

	public IssueType(URI self, String name, boolean isSubtask) {
		this.self = self;
		this.name = name;
		this.isSubtask = isSubtask;
	}

	public String getName() {
		return name;
	}

	public boolean isSubtask() {
		return isSubtask;
	}

	public URI getSelf() {
		return self;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).
				add("self", self).
				add("name", name).
				add("isSubtask", isSubtask).
				toString();
	}
}
