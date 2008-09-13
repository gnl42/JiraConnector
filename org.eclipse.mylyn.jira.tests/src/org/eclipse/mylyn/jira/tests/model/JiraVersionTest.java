/*******************************************************************************
 * Copyright (c) 2004, 2008 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests.model;

import junit.framework.TestCase;

import org.eclipse.mylyn.internal.jira.core.model.JiraVersion;

/**
 * @author Eugene Kuleshov
 */
public class JiraVersionTest extends TestCase {

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
		assertEquals("3.0", new JiraVersion("3.0").toString());
		assertEquals("3.0-dev", new JiraVersion("3.0-dev").toString());
		assertEquals("3.6.5-#161", new JiraVersion("3.6.5-#161").toString());
		assertEquals("3.9-#233", new JiraVersion("3.9-#233").toString());
		assertEquals("3.10-DEV-190607-#251", new JiraVersion("3.10-DEV-190607-#251").toString());
	}

	private void assertVersion(String s1, String s2, int expected) {
		JiraVersion v1 = v(s1);
		JiraVersion v2 = v(s2);
		assertEquals(s1 + " / " + s2, expected, v1.compareTo(v2));
	}

	private JiraVersion v(String v) {
		return new JiraVersion(v);
	}

}
