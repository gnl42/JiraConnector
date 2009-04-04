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

import com.atlassian.connector.commons.misc.IntRange;
import com.atlassian.connector.commons.misc.IntRanges;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedCommentBean;

import java.util.Date;

import junit.framework.TestCase;

/**
 * @author Shawn Minto
 */
public class VersionedCommentDateComparatorTest extends TestCase {

	public void testCompare() {

		VersionedCommentDateComparator comparator = new VersionedCommentDateComparator();

		Date d1 = new Date();
		Date d2 = new Date(d1.getTime() + 10);

		// test comment equal
		VersionedCommentBean c1 = new VersionedCommentBean();
		c1.setCreateDate(d1);
		c1.setToLineRanges(new IntRanges(new IntRange(10, 11)));

		assertEquals(0, comparator.compare(c1, c1));

		// test comment equal
		VersionedCommentBean c2 = new VersionedCommentBean();
		c2.setCreateDate(d1);
		c2.setToLineRanges(new IntRanges(new IntRange(10, 11)));

		assertEquals(0, comparator.compare(c1, c2));

		// test line number smaller
		c2 = new VersionedCommentBean();
		c2.setCreateDate(d1);
		c2.setToLineRanges(new IntRanges(new IntRange(9, 11)));

		assertTrue(0 < comparator.compare(c1, c2));

		// test line number larger
		c2 = new VersionedCommentBean();
		c2.setCreateDate(d1);
		c2.setToLineRanges(new IntRanges(new IntRange(11)));

		assertTrue(0 > comparator.compare(c1, c2));

		// test date newer
		c2 = new VersionedCommentBean();
		c2.setCreateDate(d2);
		c2.setToLineRanges(new IntRanges(new IntRange(10, 11)));

		assertTrue(0 > comparator.compare(c1, c2));
	}
}
