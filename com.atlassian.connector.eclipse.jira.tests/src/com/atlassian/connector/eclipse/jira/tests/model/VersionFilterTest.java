/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.jira.tests.model;

import junit.framework.TestCase;

import com.atlassian.connector.eclipse.internal.jira.core.model.Version;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.VersionFilter;

@SuppressWarnings("restriction")
public class VersionFilterTest extends TestCase {

	public void testCopy() {
		Version[] versions = new Version[] { new Version("v") };
		VersionFilter filter1 = new VersionFilter(versions, true, true, true);

		VersionFilter filter2 = filter1.copy();

		assertEquals(filter1.hasNoVersion(), filter2.hasNoVersion());
		assertEquals(filter1.isReleasedVersions(), filter2.isReleasedVersions());
		assertEquals(filter1.isUnreleasedVersions(), filter2.isUnreleasedVersions());
		assertEquals(filter1.getVersions().length, filter2.getVersions().length);
		assertEquals(filter1.getVersions()[0], filter2.getVersions()[0]);

		versions[0] = new Version("x");
		assertEquals(versions[0], filter1.getVersions()[0]);

		assertNotSame(filter1.getVersions()[0], filter2.getVersions()[0]);
		assertFalse(filter2.getVersions()[0].equals(filter1.getVersions()[0]));
	}
}
