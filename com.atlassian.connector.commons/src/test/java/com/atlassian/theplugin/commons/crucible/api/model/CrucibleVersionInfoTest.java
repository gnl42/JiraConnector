/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.theplugin.commons.crucible.api.model;

import java.util.Date;
import junit.framework.TestCase;

public class CrucibleVersionInfoTest extends TestCase {

	private CrucibleVersionInfo crucibleVersion;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testIsVersionOrGreater() {
		String buildDate = new Date().toString();

		crucibleVersion = new CrucibleVersionInfo("1.6", buildDate);
		assertFalse(crucibleVersion.isVersion2OrGreater());
		assertFalse(crucibleVersion.isVersion21OrGreater());

		crucibleVersion = new CrucibleVersionInfo("1.6.6.1", buildDate);
		assertFalse(crucibleVersion.isVersion2OrGreater());
		assertFalse(crucibleVersion.isVersion21OrGreater());

		crucibleVersion = new CrucibleVersionInfo("2.1", buildDate);
		assertTrue(crucibleVersion.isVersion2OrGreater());
		assertTrue(crucibleVersion.isVersion21OrGreater());

		crucibleVersion = new CrucibleVersionInfo("2.1.0", buildDate);
		assertTrue(crucibleVersion.isVersion2OrGreater());
		assertTrue(crucibleVersion.isVersion21OrGreater());

		crucibleVersion = new CrucibleVersionInfo("2.2", buildDate);
		assertTrue(crucibleVersion.isVersion2OrGreater());
		assertTrue(crucibleVersion.isVersion21OrGreater());

		crucibleVersion = new CrucibleVersionInfo("2.2.0", buildDate);
		assertTrue(crucibleVersion.isVersion2OrGreater());
		assertTrue(crucibleVersion.isVersion21OrGreater());

		crucibleVersion = new CrucibleVersionInfo("2.2.0.M1", buildDate);
		assertTrue(crucibleVersion.isVersion2OrGreater());
		assertTrue(crucibleVersion.isVersion21OrGreater());

		crucibleVersion = new CrucibleVersionInfo("3.0", buildDate);
		assertTrue(crucibleVersion.isVersion2OrGreater());
		assertTrue(crucibleVersion.isVersion21OrGreater());

		crucibleVersion = new CrucibleVersionInfo("3", buildDate);
		assertTrue(crucibleVersion.isVersion2OrGreater());
	}

	public void testCompare() {
		assertTrue(new CrucibleVersionInfo("2", null).compareTo(new CrucibleVersionInfo("2", null)) == 0);
		assertTrue(new CrucibleVersionInfo("2.1.1", null).compareTo(new CrucibleVersionInfo("2.1.1", null)) == 0);

		assertTrue(new CrucibleVersionInfo("2.1", null).compareTo(new CrucibleVersionInfo("2", null)) == 1);
		assertTrue(new CrucibleVersionInfo("2.1.1", null).compareTo(new CrucibleVersionInfo("2", null)) == 1);
		assertTrue(new CrucibleVersionInfo("2.1.1", null).compareTo(new CrucibleVersionInfo("2.1.0.23", null)) == 1);

		assertTrue(new CrucibleVersionInfo("2.1", null).compareTo(new CrucibleVersionInfo("2.2", null)) == -1);
		assertTrue(new CrucibleVersionInfo("2.1.1", null).compareTo(new CrucibleVersionInfo("2.2", null)) == -1);
		assertTrue(new CrucibleVersionInfo("2.1.1", null).compareTo(new CrucibleVersionInfo("2.1.2", null)) == -1);
		assertTrue(new CrucibleVersionInfo("2.1.1.123", null).compareTo(new CrucibleVersionInfo("2.2", null)) == -1);
	}

}
