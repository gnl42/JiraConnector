package com.atlassian.jira.rest.client.api.domain;

import javax.annotation.Nullable;
import java.util.Collections;

public class OperationGroup {
	private final String id;
	private final Iterable<OperationHeader> headers;
	private final Iterable<OperationLink> links;
	private final Iterable<OperationGroup> groups;
	@Nullable private final Integer weight;

	public OperationGroup(final String id, @Nullable final Iterable<OperationLink> links,
			@Nullable final Iterable<OperationGroup> groups, @Nullable final Iterable<OperationHeader> headers,
			@Nullable final Integer weight) {
		this.id = id;
		this.headers = headers != null ? headers : Collections.<OperationHeader>emptyList();
		this.links = links != null ? links : Collections.<OperationLink>emptyList();
		this.groups = groups != null ? groups : Collections.<OperationGroup>emptyList();
		this.weight = weight;
	}

	public String getId() {
		return id;
	}

	public Iterable<OperationHeader> getHeaders() {
		return headers;
	}

	public Iterable<OperationLink> getLinks() {
		return links;
	}

	public Iterable<OperationGroup> getGroups() {
		return groups;
	}

	@Nullable
	public Integer getWeight() {
		return weight;
	}
}
