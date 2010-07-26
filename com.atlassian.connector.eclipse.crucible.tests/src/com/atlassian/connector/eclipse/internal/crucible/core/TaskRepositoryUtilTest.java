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

package com.atlassian.connector.eclipse.internal.crucible.core;

import com.atlassian.theplugin.commons.util.MiscUtil;

import java.util.Map;

import junit.framework.TestCase;

public class TaskRepositoryUtilTest extends TestCase {

	private Map<String, String> mappings;

	@Override
	public void setUp() {
		mappings = MiscUtil.buildHashMap();
		mappings.put("https://studio.atlassian.com/svn/PLE", "PLE");
		mappings.put(":pserver:11110000b@cvs.dev.java.net:/cvs", "open-ejb");
	}

	public void testSimpleMatching() {
		Map.Entry<String, String> mapping = TaskRepositoryUtil.getMatchingSourceRepository(mappings,
				"https://studio.atlassian.com/svn/PLE");
		assertNotNull(mapping);
		assertEquals("PLE", mapping.getValue());

		mapping = TaskRepositoryUtil.getMatchingSourceRepository(mappings, "https://studio.atlassian.com/svn/PLE/");
		assertNotNull(mapping);
		assertEquals("PLE", mapping.getValue());

		mapping = TaskRepositoryUtil.getMatchingSourceRepository(mappings,
				"https://studio.atlassian.com/svn/PLE//trunk");
		assertNotNull(mapping);
		assertEquals("PLE", mapping.getValue());

		mapping = TaskRepositoryUtil.getMatchingSourceRepository(mappings, ":pserver:11110000b@cvs.dev.java.net:/cvs");
		assertNotNull(mapping);
		assertEquals("open-ejb", mapping.getValue());
	}
}
