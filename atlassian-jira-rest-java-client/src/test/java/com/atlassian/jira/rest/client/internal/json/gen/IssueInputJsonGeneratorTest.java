/*
 * Copyright (C) 2012 Atlassian
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

package com.atlassian.jira.rest.client.internal.json.gen;

import com.atlassian.jira.rest.client.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.atlassian.jira.rest.client.domain.input.IssueInput;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @since v1.0
 */
public class IssueInputJsonGeneratorTest {

	@Test
	public void testGenerate() throws Exception {
		final IssueInputJsonGenerator generator = new IssueInputJsonGenerator();
		final IssueInput issueInput = IssueInput.createWithFields(
				new FieldInput("string", "String value"),
				new FieldInput("integer", 1),
				new FieldInput("long", 1L),
				new FieldInput("complex", new ComplexIssueInputFieldValue(ImmutableMap.<String, Object>of(
						"string", "string",
						"integer", 1,
						"long", 1L,
						"complex", ComplexIssueInputFieldValue.with("test", "id")
				)))
		);
		final String expected = "{\"fields\":{\"complex\":{\"string\":\"string\",\"integer\":\"1\",\"long\":\"1\",\"complex\":{\"test\":\"id\"}},\"integer\":\"1\",\"string\":\"String value\",\"long\":\"1\"}}";
		final String actual = generator.generate(issueInput).toString();
		assertEquals(expected, actual);
	}
}
