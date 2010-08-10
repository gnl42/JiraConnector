package com.atlassian.jira.restjavaclient.domain;

import com.atlassian.jira.restjavaclient.AddressableEntity;
import com.google.common.base.Objects;

import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class BasicStatus implements AddressableEntity {
	private final URI self;
	private final String name;

	public BasicStatus(URI self, String name) {
		this.self = self;
		this.name = name;
	}

	public URI getSelf() {
		return self;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).
				add("self", self).
				add("name", name).
				toString();
	}
}
