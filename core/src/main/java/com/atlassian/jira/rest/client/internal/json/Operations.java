package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.OperationGroup;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

import java.util.Collections;

public class Operations {
	private final Iterable<OperationGroup> linkGroups;

	public Operations(final Iterable<OperationGroup> linkGroups) {
		this.linkGroups = Objects.firstNonNull(linkGroups, Collections.<OperationGroup>emptyList());
	}

	public Iterable<OperationGroup> getLinkGroups() {
		return linkGroups;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(linkGroups);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		final Operations other = (Operations) obj;
		return Iterables.elementsEqual(this.linkGroups, other.linkGroups);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("linkGroups", linkGroups)
				.toString();
	}
}
