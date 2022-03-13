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

import static com.spartez.util.junit3.TestUtil.assertNotEquals;

/**
 * BambooServerCfg Tester.
 *
 * @author wseliga
 */
public class BambooServerCfgTest extends TestCase {

	private static final ServerIdImpl SERVER_ID = new ServerIdImpl();
	private BambooServerCfg bamboo1 = createBambooServerCfg(SERVER_ID, "mybamboo1", "myurl", "myusername",
			"mypassword", false, true);
	private BambooServerCfg bamboo2 = new BambooServerCfg("mybamboo2", new ServerIdImpl());
	private BambooServerCfg bamboo1sameId = createBambooServerCfg(SERVER_ID, "mybamboo1", "myurl", "myusername",
			"mypassword", false, true);

	public BambooServerCfgTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	private static BambooServerCfg createBambooServerCfg(ServerIdImpl serverId, String name, String url, String username,
			String password, boolean isUseFavourites, boolean isEnabled) {
		BambooServerCfg res = new BambooServerCfg(name, serverId);
		res.setUrl(url);
		res.setUsername(username);
		res.setPassword(password);
		res.setUseFavourites(isUseFavourites);
		res.setEnabled(isEnabled);
		return res;
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void testEquals() {
		assertEquals(bamboo1, bamboo1);
		assertNotNull(bamboo1);
		assertEquals(bamboo1, bamboo1sameId);
		assertNotEquals(bamboo1, bamboo2);

		bamboo1sameId.setUseFavourites(!bamboo1sameId.isUseFavourites());
		assertNotEquals(bamboo1, bamboo1sameId);
		bamboo1sameId.setUseFavourites(!bamboo1sameId.isUseFavourites());
		assertEquals(bamboo1, bamboo1sameId);

		bamboo1sameId.setName("somenewname");
		assertNotEquals(bamboo1, bamboo1sameId);
		bamboo1sameId.setName(bamboo1.getName());
		assertEquals(bamboo1, bamboo1sameId);

		bamboo1sameId.setUrl("somenewurl");
		assertNotEquals(bamboo1, bamboo1sameId);
		bamboo1sameId.setUrl(bamboo1.getUrl());
		assertEquals(bamboo1, bamboo1sameId);

		bamboo1sameId.setUsername("somenewusername");
		assertNotEquals(bamboo1, bamboo1sameId);
		bamboo1sameId.setUsername(bamboo1.getUsername());
		assertEquals(bamboo1, bamboo1sameId);

		bamboo1sameId.setPassword("somenewpassword");
		assertNotEquals(bamboo1, bamboo1sameId);
		bamboo1sameId.setPassword(bamboo1.getPassword());
		assertEquals(bamboo1, bamboo1sameId);

		bamboo1sameId.setPasswordStored(!bamboo1sameId.isPasswordStored());
		assertNotEquals(bamboo1, bamboo1sameId);
		bamboo1sameId.setPasswordStored(!bamboo1sameId.isPasswordStored());
		assertEquals(bamboo1, bamboo1sameId);

		bamboo1sameId.setEnabled(!bamboo1sameId.isEnabled());
		assertNotEquals(bamboo1, bamboo1sameId);
		bamboo1sameId.setEnabled(!bamboo1sameId.isEnabled());
		assertEquals(bamboo1, bamboo1sameId);

		bamboo1sameId.getSubscribedPlans().add(new SubscribedPlan("myplan", false));
		assertNotEquals(bamboo1, bamboo1sameId);
		bamboo1.getSubscribedPlans().add(new SubscribedPlan("myplan", false));
		assertEquals(bamboo1, bamboo1sameId);
	}

	public void testGetClone() {
		bamboo1.getSubscribedPlans().add(new SubscribedPlan("myplan", false));
		BambooServerCfg clone = bamboo1.getClone();
		assertEquals(bamboo1, clone);
		assertNotSame(bamboo1, clone);
		clone.getSubscribedPlans().add(new SubscribedPlan("myotherplan", false));
		TestUtil.assertNotEquals(bamboo1, clone);

		bamboo1.setPasswordStored(true);
		clone = bamboo1.getClone();
		assertEquals(bamboo1, clone);
		assertNotSame(bamboo1, clone);
	}
}
