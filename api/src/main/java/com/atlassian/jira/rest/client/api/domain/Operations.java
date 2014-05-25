package com.atlassian.jira.rest.client.api.domain;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

import java.util.Collections;

/**
 * Represents operations returned for expand {@link IssueRestClient.Expandos#OPERATIONS}
 *
 * @since 2.0
 */
public class Operations {
	private final Iterable<OperationGroup> linkGroups;

	public Operations(final Iterable<OperationGroup> linkGroups) {
		this.linkGroups = Objects.firstNonNull(linkGroups, Collections.<OperationGroup>emptyList());
	}

	public Iterable<OperationGroup> getLinkGroups() {
		return linkGroups;
	}

	public <T> T accept(OperationVisitor<T> visitor) {
		return OperationGroup.accept(getLinkGroups(), visitor);
	}

	public Operation getOperationById(final String operationId) {
		return accept(new OperationVisitor<Operation>() {
			@Override
			public Operation visit(Operation operation) {
				return operationId.equals(operation.getId()) ? operation : null;
			}
		});
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
