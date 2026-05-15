/*******************************************************************************
 * Copyright (c) 2004, 2009 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package me.glindholm.connector.eclipse.jira.tests.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraServerVersion;

/**
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 */
public class JiraVersionTest {

	@Test
	public void testVersion() throws Exception {
		assertVersion(null, null, 0);
		assertVersion(null, "", 0);
		assertVersion(null, "3", -1);

		assertVersion("", null, 0);
		assertVersion("", "", 0);
		assertVersion("", "3", -1);

		assertVersion("3", null, 1);
		assertVersion("3", "", 1);
		assertVersion("3-dev", "", 1);
		assertVersion("2", "3", -1);
		assertVersion("2", "3-dev", -1);
		assertVersion("3", "3", 0);
		assertVersion("3", "3-dev", -3);
		assertVersion("4", "3", 1);
		assertVersion("10", "3", 1);
		assertVersion("10", "100", -1);
		assertVersion("3", "3.1", -1);
		assertVersion("3", "3.1-dev", -1);
		assertVersion("3-dev", "3.1", -1);
		assertVersion("3", "3.3.3", -1);

		assertVersion("3.1", null, 1);
		assertVersion("3.1", "", 1);
		assertVersion("2.1", "3", -1);
		assertVersion("3.1", "3", 1);
		assertVersion("4.1", "3", 1);
		assertVersion("3.1", "3.3", -1);
		assertVersion("3.1", "3.10", -1);
		assertVersion("3.1", "2.10", 1);
		assertVersion("3.1", "3.3.3", -1);

		assertVersion("3.1.1", null, 1);
		assertVersion("3.1.1", "", 1);
		assertVersion("2.1.1", "3", -1);
		assertVersion("3.1.1", "3", 1);
		assertVersion("4.1.1", "3", 1);
		assertVersion("3.1.1", "3.3", -1);
		assertVersion("3.1.1", "3.10", -1);
		assertVersion("3.1.1", "2.10", 1);
		assertVersion("3.1.1", "3.3.3", -1);
		assertVersion("3.3.1", "3.3.3", -1);
		assertVersion("3.3.4", "3.3.3", 1);
		assertVersion("3.3.10", "3.3.3", 1);
	}

	public void testToString() throws Exception {
		assertEquals("3.0", new JiraServerVersion("3.0").toString());
		assertEquals("3.0-dev", new JiraServerVersion("3.0-dev").toString());
		assertEquals("3.6.5-#161", new JiraServerVersion("3.6.5-#161").toString());
		assertEquals("3.9-#233", new JiraServerVersion("3.9-#233").toString());
		assertEquals("3.10-DEV-190607-#251", new JiraServerVersion("3.10-DEV-190607-#251").toString());
	}

	public void testNonNumbericVersion() throws Exception {
		assertEquals("3.6-#161", new JiraServerVersion("3.6.xx-#161").toString());
		assertEquals("0.0", new JiraServerVersion("a.b").toString());
	}

	private void assertVersion(final String s1, final String s2, final int expected) {
		final var v1 = v(s1);
		final var v2 = v(s2);
		assertEquals(expected, v1.compareTo(v2), s1 + " / " + s2);
	}

	private JiraServerVersion v(final String v) {
		return new JiraServerVersion(v);
	}

}
