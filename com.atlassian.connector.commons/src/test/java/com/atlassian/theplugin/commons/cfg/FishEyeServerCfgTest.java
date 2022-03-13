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

public class FishEyeServerCfgTest extends TestCase {
	private final FishEyeServerCfg fish1 = new FishEyeServerCfg(true, "FServer1", new ServerIdImpl());
	private final FishEyeServerCfg fish2 = new FishEyeServerCfg(true, "FServer2", new ServerIdImpl());

	public void testEquals() {
		//noinspection ObjectEqualsNull
		assertFalse(fish1.equals(null));
		TestUtil.assertNotEquals(fish1, fish2);
		fish2.setName(fish1.getName());
		fish2.setServerId(fish1.getServerId());
		TestUtil.assertEqualsSymmetrical(fish1, fish2);
	}

	public void testClone() {
		final FishEyeServerCfg fishClone = fish1.getClone();
		TestUtil.assertEqualsSymmetrical(fish1, fishClone);
	}
}
