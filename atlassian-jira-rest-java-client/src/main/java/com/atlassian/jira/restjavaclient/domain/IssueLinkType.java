package com.atlassian.jira.restjavaclient.domain;

import com.google.common.base.Objects;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class IssueLinkType {
	public enum Direction { OUTBOUND, INBOUND}
	private final String name;
	private final String description;
	private final Direction direction;

	public IssueLinkType(String name, String description, Direction direction) {
		this.name = name;
		this.description = description;
		this.direction = direction;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Direction getDirection() {
		return direction;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).
				add("name", name).
				add("description", description).
				add("direction", direction).
				toString();
	}
}
