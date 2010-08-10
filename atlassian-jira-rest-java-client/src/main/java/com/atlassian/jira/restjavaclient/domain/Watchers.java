package com.atlassian.jira.restjavaclient.domain;

import com.atlassian.jira.restjavaclient.AddressableEntity;
import com.atlassian.jira.restjavaclient.ExpandableProperty;
import com.google.common.base.Objects;

import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class Watchers implements AddressableEntity {
	private final URI self;
	private final boolean isWatching;
	private final ExpandableProperty<User> list;

	public Watchers(URI self, boolean watching, ExpandableProperty<User> list) {
		this.self = self;
		isWatching = watching;
		this.list = list;
	}

	public URI getSelf() {
		return self;
	}

	public boolean isWatching() {
		return isWatching;
	}

	public ExpandableProperty<User> getList() {
		return list;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).
				add("self", self).
				add("isWatching", isWatching).
				add("list", list).
				toString();
	}
}
