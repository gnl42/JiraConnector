package com.atlassian.jira.restjavaclient.domain;

import com.google.common.base.Objects;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class Field {
	private final String name;

	private final String value;

	public Field(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).
				add("name", name).
				add("value", value).
				toString();
	}
}
