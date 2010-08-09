package com.atlassian.jira.restjavaclient.domain;

import com.atlassian.jira.restjavaclient.AddressableEntity;
import com.google.common.base.Objects;

import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class Project implements AddressableEntity {
	private final URI self;
	private final String key;

	public Project(URI self, String key) {
		this.self = self;
		this.key = key;
	}

	public URI getSelf() {
		return self;
	}

	public String getKey() {
		return key;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).
				add("self", self).
				add("key", key).
				toString();
	}
}
