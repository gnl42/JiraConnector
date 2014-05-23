package com.atlassian.jira.rest.client.api.domain;

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
}
