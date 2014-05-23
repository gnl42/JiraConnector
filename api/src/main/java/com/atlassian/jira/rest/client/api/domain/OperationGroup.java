package com.atlassian.jira.rest.client.api.domain;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.Collections;

public class OperationGroup {
	@Nullable private final String id;
	@Nullable private final OperationHeader header;
	private final Iterable<OperationLink> links;
	private final Iterable<OperationGroup> groups;
	@Nullable private final Integer weight;

	public OperationGroup(@Nullable final String id, @Nullable final Iterable<OperationLink> links,
			@Nullable final Iterable<OperationGroup> groups, @Nullable final OperationHeader header,
			@Nullable final Integer weight) {
		this.id = id;
		this.header = header;
		this.links = links != null ? links : Collections.<OperationLink>emptyList();
		this.groups = groups != null ? groups : Collections.<OperationGroup>emptyList();
		this.weight = weight;
	}

	@Nullable
	public String getId() {
		return id;
	}

	@Nullable
	public OperationHeader getHeader() {
		return header;
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

	@Override
	public int hashCode() {
		return Objects.hashCode(id, header, links, groups, weight);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		final OperationGroup other = (OperationGroup) obj;
		return Objects.equal(this.id, other.id)
				&& Objects.equal(this.header, other.header)
				&& Iterables.elementsEqual(this.links, other.links)
				&& Iterables.elementsEqual(this.groups, other.groups)
				&& Objects.equal(this.weight, other.weight);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("id", id)
				.add("header", header)
				.add("links", links)
				.add("groups", groups)
				.add("weight", weight)
				.toString();
	}
}
