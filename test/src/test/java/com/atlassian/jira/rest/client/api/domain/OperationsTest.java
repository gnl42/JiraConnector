package com.atlassian.jira.rest.client.api.domain;

import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class OperationsTest {

	@Test
	public void testGetOperationById() throws Exception {
		Operations operations = new Operations(Collections.singleton(new OperationGroup(
				null,
				Collections.singleton(new OperationLink("action_id_4", null, "Start", null, "/start", null, null)),
				Collections.<OperationGroup>emptyList(),
				null,
				null
		)));

		Operation operation = operations.getOperationById("action_id_4");

		assertThat(operation, allOf(
						instanceOf(OperationLink.class),
						hasProperty("id", is("action_id_4"))
				)
		);
	}
}