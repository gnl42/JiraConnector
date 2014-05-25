package com.atlassian.jira.rest.client.api.domain;

public interface OperationVisitor<T> {
	T visit(Operation operation);
}
