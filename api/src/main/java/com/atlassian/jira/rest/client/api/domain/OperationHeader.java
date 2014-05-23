package com.atlassian.jira.rest.client.api.domain;

import com.google.common.base.Objects;

public class OperationHeader {
	private final String id;
	private final String label;

	public OperationHeader(final String id, final String label) {
		this.id = id;
		this.label = label;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("id", id)
				.add("label", label)
				.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof OperationHeader) {
			OperationHeader that = (OperationHeader) o;
			return Objects.equal(id, that.id)
					&& Objects.equal(label, that.label);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id, label);
	}
}
