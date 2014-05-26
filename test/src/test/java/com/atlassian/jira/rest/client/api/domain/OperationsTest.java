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