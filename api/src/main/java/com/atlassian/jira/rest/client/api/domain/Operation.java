package com.atlassian.jira.rest.client.api.domain;

import javax.annotation.Nullable;

public interface Operation {
	@Nullable String getId();

	<T> T accept(OperationVisitor<T> visitor);

}
