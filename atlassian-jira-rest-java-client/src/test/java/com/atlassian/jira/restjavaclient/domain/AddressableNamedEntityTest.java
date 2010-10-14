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

package com.atlassian.jira.restjavaclient.domain;

import com.atlassian.jira.restjavaclient.TestUtil;
import org.junit.Test;

import static com.atlassian.jira.restjavaclient.TestUtil.assertNotEquals;
import static com.atlassian.jira.restjavaclient.TestUtil.toUri;
import static org.junit.Assert.assertEquals;

public class AddressableNamedEntityTest {

	private static final AddressableNamedEntity P1 = new AddressableNamedEntity(toUri("http://localhost/1"), "a");
	private static final AddressableNamedEntity P2 = new AddressableNamedEntity(toUri("http://localhost/1"), "a");
	private static final AddressableNamedEntity P3 = new AddressableNamedEntity(toUri("http://localhost/1"), "b");
	private static final AddressableNamedEntity P4 = new AddressableNamedEntity(toUri("http://localhost/2"), "a");


	@Test
	public void testEquals() {
		TestUtil.assertEqualsSymmetrical(P1, P2);
		assertEquals(P1, P1);
		assertNotEquals(P1, null);
		assertNotEquals(P1, P3);
		assertNotEquals(P1, P4);
	}

	@Test
	public void testHashCode() throws Exception {
		assertEquals(P1.hashCode(), P2.hashCode());
	}

}
