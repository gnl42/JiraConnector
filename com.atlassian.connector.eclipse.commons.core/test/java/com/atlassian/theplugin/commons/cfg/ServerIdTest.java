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
 * ServerId Tester.
 *
 * @author wseliga
 */
public class ServerIdTest extends TestCase {
	public static final ServerIdImpl SERVER_ID_1 = new ServerIdImpl();
	public static final ServerIdImpl SERVER_ID_2 = new ServerIdImpl();

	public void testEquals() {
		TestUtil.assertNotEquals(SERVER_ID_1, SERVER_ID_2);
		assertEquals(SERVER_ID_1, SERVER_ID_1);
	}

}
