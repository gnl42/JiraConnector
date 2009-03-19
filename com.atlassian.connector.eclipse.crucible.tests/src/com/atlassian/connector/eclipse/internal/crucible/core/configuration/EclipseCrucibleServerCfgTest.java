/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.core.configuration;

import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;

import junit.framework.TestCase;

public class EclipseCrucibleServerCfgTest extends TestCase {

	public void testHashCode() {
		EclipseCrucibleServerCfg cfg1 = new EclipseCrucibleServerCfg("server", "https://sub.doman.tld/cru", false);
		EclipseCrucibleServerCfg cfg2 = new EclipseCrucibleServerCfg("server", "https://sub.doman.tld/cru", false);
		EclipseCrucibleServerCfg cfg3 = new EclipseCrucibleServerCfg("server", "https://sub.doman.tld/cru", true);
		EclipseCrucibleServerCfg cfg4 = new EclipseCrucibleServerCfg("server", "https://sub.doman.tld/cru", false);
		EclipseCrucibleServerCfg cfg5 = new EclipseCrucibleServerCfg("server", "https://sub.doman.tld/cru", false);
		EclipseCrucibleServerCfg cfg6 = new EclipseCrucibleServerCfg("server", "https://tld.doman.sub/cru", false);

		assertTrue(cfg1.hashCode() == cfg2.hashCode());
		assertFalse(cfg1.hashCode() == cfg3.hashCode());
		assertTrue(cfg1.hashCode() == cfg4.hashCode());
		assertTrue(cfg1.hashCode() == cfg5.hashCode());
		assertFalse(cfg1.hashCode() == cfg6.hashCode());

		cfg1.setUsername("use");
		cfg2.setUsername("use1");
		cfg4.setUsername("use");
		cfg5.setUsername("use1");

		assertFalse(cfg1.hashCode() == cfg2.hashCode());
		assertFalse(cfg1.hashCode() == cfg5.hashCode());
		assertTrue(cfg1.hashCode() == cfg4.hashCode());
		assertTrue(cfg2.hashCode() == cfg5.hashCode());
		assertFalse(cfg4.hashCode() == cfg5.hashCode());

		cfg1.setPassword("pas");
		cfg2.setPassword("pas");
		cfg4.setPassword("pas1");
		cfg5.setPassword("pas1");

		assertFalse(cfg1.hashCode() == cfg2.hashCode());
		assertFalse(cfg1.hashCode() == cfg5.hashCode());
		assertFalse(cfg4.hashCode() == cfg5.hashCode());
		assertTrue(cfg2.hashCode() == cfg5.hashCode()); // different passwords do not mean different servers
		assertTrue(cfg1.hashCode() == cfg4.hashCode()); // different passwords do not mean different servers

		cfg1.setUsername("use");
		cfg2.setUsername("use");
		cfg4.setUsername("use1");
		cfg5.setUsername("use1");

		assertTrue(cfg1.hashCode() == cfg2.hashCode());
		assertFalse(cfg1.hashCode() == cfg5.hashCode());
		assertFalse(cfg1.hashCode() == cfg4.hashCode());
		assertFalse(cfg2.hashCode() == cfg5.hashCode());
		assertTrue(cfg4.hashCode() == cfg5.hashCode());

		cfg1.setEnabled(false);
		cfg4.setEnabled(false);
		cfg5.setEnabled(false);

		assertFalse(cfg1.hashCode() == cfg2.hashCode());
		assertFalse(cfg1.hashCode() == cfg5.hashCode());
		assertFalse(cfg1.hashCode() == cfg4.hashCode());
		assertFalse(cfg2.hashCode() == cfg5.hashCode());
		assertTrue(cfg4.hashCode() == cfg5.hashCode());
	}

	public void testEqualsObject() {
		EclipseCrucibleServerCfg cfg1 = new EclipseCrucibleServerCfg("server", "https://sub.doman.tld/cru", false);
		EclipseCrucibleServerCfg cfg2 = new EclipseCrucibleServerCfg("server", "https://sub.doman.tld/cru", false);
		EclipseCrucibleServerCfg cfg3 = new EclipseCrucibleServerCfg("server", "https://sub.doman.tld/cru", true);
		EclipseCrucibleServerCfg cfg4 = new EclipseCrucibleServerCfg("server", "https://sub.doman.tld/cru", false);
		EclipseCrucibleServerCfg cfg5 = new EclipseCrucibleServerCfg("server", "https://sub.doman.tld/cru", false);
		EclipseCrucibleServerCfg cfg6 = new EclipseCrucibleServerCfg("server", "https://tld.doman.sub/cru", false);

		assertEquals(cfg1, cfg2);
		assertEquals(cfg1, cfg4);
		assertEquals(cfg1, cfg5);
		assertFalse(cfg1.equals(cfg3));
		assertFalse(cfg1.equals(cfg6));
		assertEquals(cfg1, cfg2);
		assertEquals(null, null);
		assertFalse(cfg1.equals(null));
		CrucibleServerCfg servercfg = new CrucibleServerCfg("server", new ServerId());
		servercfg.setUrl("https://tld.doman.sub/cru");
		assertFalse(cfg1.equals(servercfg));

		cfg1.setUsername("use");
		cfg2.setUsername("use1");
		cfg4.setUsername("use");
		cfg5.setUsername("use1");

		assertFalse(cfg1.equals(cfg2));
		assertFalse(cfg1.equals(cfg5));
		assertEquals(cfg1, cfg4);
		assertEquals(cfg2, cfg5);
		assertFalse(cfg4.equals(cfg5));

		cfg1.setPassword("pas");
		cfg2.setPassword("pas");
		cfg4.setPassword("pas1");
		cfg5.setPassword("pas1");

		assertFalse(cfg1.equals(cfg2));
		assertFalse(cfg1.equals(cfg5));
		assertFalse(cfg4.equals(cfg5));
		assertTrue(cfg2.equals(cfg5)); // different passwords do not mean different servers
		assertTrue(cfg1.equals(cfg4)); // different passwords do not mean different servers

		cfg1.setUsername("use");
		cfg2.setUsername("use");
		cfg4.setUsername("use1");
		cfg5.setUsername("use1");

		assertEquals(cfg1, cfg2);
		assertFalse(cfg1.equals(cfg5));
		assertFalse(cfg1.equals(cfg4));
		assertFalse(cfg2.equals(cfg5));
		assertEquals(cfg4, cfg5);

		cfg1.setEnabled(false);
		cfg4.setEnabled(false);
		cfg5.setEnabled(false);

		assertFalse(cfg1.hashCode() == cfg2.hashCode());
		assertFalse(cfg1.hashCode() == cfg5.hashCode());
		assertFalse(cfg1.hashCode() == cfg4.hashCode());
		assertFalse(cfg2.hashCode() == cfg5.hashCode());
		assertTrue(cfg4.hashCode() == cfg5.hashCode());
	}
}
