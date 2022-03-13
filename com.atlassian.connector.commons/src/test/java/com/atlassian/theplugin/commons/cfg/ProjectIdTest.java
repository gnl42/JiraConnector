/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.theplugin.commons.cfg;

import com.spartez.util.junit3.TestUtil;
import junit.framework.TestCase;

/**
 * ProjectId Tester.
 *
 * @author wseliga
 */
public class ProjectIdTest extends TestCase {

	public static final ProjectId PROJECT_ID_1 = new ProjectId();
	public static final ProjectId PROJECT_ID_2 = new ProjectId();
	public static final ProjectId PROJECT_ID_3 = new ProjectId("abc");
	public static final ProjectId PROJECT_ID_4 = new ProjectId("bc");
	public static final ProjectId PROJECT_ID_5 = new ProjectId("abc");

	public ProjectIdTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testEquals() {
		assertEquals(PROJECT_ID_1, PROJECT_ID_1);
		assertEquals(PROJECT_ID_3, PROJECT_ID_3);
		TestUtil.assertNotEquals(PROJECT_ID_1, PROJECT_ID_2);
		TestUtil.assertNotEquals(PROJECT_ID_1, PROJECT_ID_3);
		TestUtil.assertNotEquals(PROJECT_ID_1, PROJECT_ID_4);
		TestUtil.assertEqualsSymmetrical(PROJECT_ID_3, PROJECT_ID_5);
		assertEquals(PROJECT_ID_4, PROJECT_ID_4);
    }

}
