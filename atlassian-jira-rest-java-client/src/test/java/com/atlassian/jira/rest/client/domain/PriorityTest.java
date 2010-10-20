/*
 * Copyright (C) 2010 Atlassian
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

package com.atlassian.jira.rest.client.domain;

import com.atlassian.jira.rest.client.TestUtil;
import org.junit.Test;

import static com.atlassian.jira.rest.client.TestUtil.assertNotEquals;
import static com.atlassian.jira.rest.client.TestUtil.toUri;
import static org.junit.Assert.assertEquals;

public class PriorityTest {
	private static final Priority P1 = new Priority(toUri("http://localhost/1"), "a", "#223344", "a description", toUri("http://localhost/2"));
	private static final Priority P2 = new Priority(toUri("http://localhost/1"), "a", "#223344", "a description", toUri("http://localhost/2"));
	private static final Priority P3 = new Priority(toUri("http://localhost/1"), "b", "#223344", "a description", toUri("http://localhost/2"));
	private static final Priority P4 = new Priority(toUri("http://localhost/2"), "a", "#223344", "a description", toUri("http://localhost/2"));
	private static final Priority P5 = new Priority(toUri("http://localhost/1"), "a", "#123344", "a description", toUri("http://localhost/2"));
	private static final Priority P6 = new Priority(toUri("http://localhost/1"), "a", "#223344", "a description2", toUri("http://localhost/2"));
	private static final Priority P7 = new Priority(toUri("http://localhost/1"), "a", "#223344", "a description", toUri("http://localhost/3"));

	
	@Test
	public void testEquals() throws Exception {
		TestUtil.assertEqualsSymmetrical(P1, P2);
		assertEquals(P1, P1);
		assertNotEquals(P1, null);
		assertNotEquals(P1, P3);
		assertNotEquals(P1, P4);
		assertNotEquals(P1, P5);
		assertNotEquals(P1, P6);
		assertNotEquals(P1, P7);
	}

	@Test
	public void testHashCode() throws Exception {
		assertEquals(P1.hashCode(), P2.hashCode());
	}
}
