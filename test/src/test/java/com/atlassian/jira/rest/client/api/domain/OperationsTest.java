/*
 * Copyright (C) 2014 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.jira.rest.client.api.domain;

import org.junit.Test;

import java.util.Collections;

import static com.atlassian.jira.rest.client.TestUtil.EMPTY_GROUPS;
import static com.atlassian.jira.rest.client.TestUtil.EMPTY_LINKS;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class OperationsTest {

	@Test
	public void testGetLinkById() throws Exception {
		Operations operations = new Operations(Collections.singleton(new OperationGroup(
				null,
				Collections.singleton(new OperationLink("action_id_4", null, "Start", null, "/start", null, null)),
				EMPTY_GROUPS,
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

	@Test
	public void testGetSelfGroupById() throws Exception {
		Operations operations = new Operations(Collections.singleton(new OperationGroup(
				"group_self",
				EMPTY_LINKS,
				EMPTY_GROUPS,
				null,
				null
		)));

		Operation operation = operations.getOperationById("group_self");

		assertThat(operation, allOf(
						instanceOf(OperationGroup.class),
						hasProperty("id", is("group_self"))
				)
		);
	}

	@Test
	public void testGetGroupById() throws Exception {
		Operations operations = new Operations(Collections.singleton(new OperationGroup(
				null,
				EMPTY_LINKS,
				Collections.singleton(new OperationGroup("group_5", EMPTY_LINKS, EMPTY_GROUPS, null, null)),
				null,
				null
		)));

		Operation operation = operations.getOperationById("group_5");

		assertThat(operation, allOf(
						instanceOf(OperationGroup.class),
						hasProperty("id", is("group_5"))
				)
		);
	}

	@Test
	public void testGetHeaderById() throws Exception {
		Operations operations = new Operations(Collections.singleton(new OperationGroup(
				null,
				EMPTY_LINKS,
				EMPTY_GROUPS,
				new OperationHeader("header_6", "header_6", null, null),
				null
		)));

		Operation operation = operations.getOperationById("header_6");

		assertThat(operation, allOf(
						instanceOf(OperationHeader.class),
						hasProperty("id", is("header_6"))
				)
		);
	}
}
