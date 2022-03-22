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

import junit.framework.TestCase;

public class PrivateProjectConfigurationTest extends TestCase {
	public void testGetPrivateServerCfgInfo() {
		PrivateProjectConfiguration ppc = new PrivateProjectConfiguration();
		final PrivateServerCfgInfo serverCfgInfo1 =
                new PrivateServerCfgInfo(new ServerIdImpl(), true, false, "abf", "pass1", false, false, "", "", false);
		final PrivateServerCfgInfo serverCfgInfo2 =
                new PrivateServerCfgInfo(new ServerIdImpl(), true, false, "abf2", "pass1", false, false, "", "", false);
		ppc.add(serverCfgInfo1);
		ppc.add(serverCfgInfo2);
		assertEquals(serverCfgInfo1, ppc.getPrivateServerCfgInfo(serverCfgInfo1.getServerId()));
		assertEquals(serverCfgInfo2, ppc.getPrivateServerCfgInfo(serverCfgInfo2.getServerId()));
		assertNull(ppc.getPrivateServerCfgInfo(new ServerIdImpl()));
	}
}
