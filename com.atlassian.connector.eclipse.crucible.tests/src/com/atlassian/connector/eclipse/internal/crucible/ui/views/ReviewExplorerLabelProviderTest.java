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

package com.atlassian.connector.eclipse.internal.crucible.ui.views;

import junit.framework.TestCase;

public class ReviewExplorerLabelProviderTest extends TestCase {

	public void testGetAbbreviatedCommentText() {
		String newLine = System.getProperty("line.separator");

		assertNull(ReviewExplorerLabelProvider.getAbbreviatedCommentText(null));
		assertEquals("", ReviewExplorerLabelProvider.getAbbreviatedCommentText(""));
		assertEquals("abc", ReviewExplorerLabelProvider.getAbbreviatedCommentText("abc"));
		assertEquals("abc", ReviewExplorerLabelProvider.getAbbreviatedCommentText("abc" + newLine));
		assertEquals("abc...", ReviewExplorerLabelProvider.getAbbreviatedCommentText("abc" + newLine + "xyz"));
		assertEquals("abc", ReviewExplorerLabelProvider.getAbbreviatedCommentText("abc" + newLine + " \t " + newLine));
		assertEquals("abc  ", ReviewExplorerLabelProvider.getAbbreviatedCommentText("abc  " + newLine + "      "));
		assertEquals(
				"something very very long 1234567890123456789012345...",
				ReviewExplorerLabelProvider.getAbbreviatedCommentText("something very very long 1234567890123456789012345678901234567890 ..."));
	}

}
