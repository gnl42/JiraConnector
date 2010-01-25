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
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.util.MiscUtil;

import java.util.Date;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author Shawn Minto
 */
public class VersionedCommentDateComparatorTest extends TestCase {

	private final Review review = new Review("myurl.com");

	public void testCompare() {

		VersionedCommentDateComparator comparator = new VersionedCommentDateComparator();

		Date d1 = new Date();
		Date d2 = new Date(d1.getTime() + 10);

		// test comment equal
		VersionedComment c1 = new VersionedComment(review);
		c1.setCreateDate(d1);
		c1.setToLineRanges(new IntRanges(new IntRange(10, 11)));

		assertEquals(0, comparator.compare(c1, c1));

		// test comment equal
		VersionedComment c2 = new VersionedComment(review);
		c2.setCreateDate(d1);
		c2.setToLineRanges(new IntRanges(new IntRange(10, 11)));

		assertEquals(0, comparator.compare(c1, c2));

		// test line number smaller
		c2 = new VersionedComment(review);
		c2.setCreateDate(d1);
		c2.setToLineRanges(new IntRanges(new IntRange(9, 11)));

		assertTrue(0 < comparator.compare(c1, c2));

		// test line number larger
		c2 = new VersionedComment(review);
		c2.setCreateDate(d1);
		c2.setToLineRanges(new IntRanges(new IntRange(11)));

		assertTrue(0 > comparator.compare(c1, c2));

		// test date newer
		c2 = new VersionedComment(review);
		c2.setCreateDate(d2);
		c2.setToLineRanges(new IntRanges(new IntRange(10, 11)));

		assertTrue(0 > comparator.compare(c1, c2));
	}

	public void testCompareWithLineRanges() {
		VersionedCommentDateComparator comparator = new VersionedCommentDateComparator();

		Date d1 = new Date();

		// test comment equal
		VersionedComment c1 = new VersionedComment(review);
		c1.setCreateDate(d1);

		final Map<String, IntRanges> lr1 = MiscUtil.buildHashMap();
		lr1.put("10.45.3", new IntRanges(new IntRange(40), new IntRange(100, 120)));
		lr1.put("10.45.4", new IntRanges(new IntRange(2), new IntRange(110, 120)));

		c1.setLineRanges(lr1);

		final Map<String, IntRanges> lr2 = MiscUtil.buildHashMap();
		lr2.put("10.48.3", new IntRanges(new IntRange(6), new IntRange(100, 120)));
		lr2.put("10.48.4", new IntRanges(new IntRange(2), new IntRange(110, 120)));
		lr2.put("10.48.5", new IntRanges(new IntRange(8), new IntRange(10, 120)));

		VersionedComment c2 = new VersionedComment(review);
		c2.setCreateDate(d1);
		c2.setLineRanges(lr2);

		assertEquals(0, comparator.compare(c1, c1));
		assertEquals(0, comparator.compare(c1, c2));

		lr2.put("10.48.4", new IntRanges(new IntRange(1), new IntRange(110, 120)));
		assertTrue(comparator.compare(c1, c2) > 0);

		lr2.put("10.48.4", new IntRanges(new IntRange(3), new IntRange(110, 120)));
		assertTrue(comparator.compare(c1, c2) < 0);

	}

	public void testGetStartLine() {
		final Map<String, IntRanges> ranges = MiscUtil.buildHashMap();
		ranges.put("10.48.3", new IntRanges(new IntRange(6), new IntRange(100, 120)));
		ranges.put("10.48.4", new IntRanges(new IntRange(2), new IntRange(110, 120)));
		ranges.put("10.48.5", new IntRanges(new IntRange(8), new IntRange(10, 120)));
		VersionedCommentDateComparator comparator = new VersionedCommentDateComparator();
		assertEquals(2, comparator.getStartLine(ranges));
		ranges.put("10.48.4", new IntRanges(new IntRange(5, 9), new IntRange(10, 120)));
		assertEquals(5, comparator.getStartLine(ranges));
	}
}
