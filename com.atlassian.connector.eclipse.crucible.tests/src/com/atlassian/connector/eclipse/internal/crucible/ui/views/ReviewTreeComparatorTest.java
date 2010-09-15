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

import com.atlassian.connector.commons.misc.IntRange;
import com.atlassian.connector.commons.misc.IntRanges;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.apache.commons.lang.time.DateUtils;

import java.util.Date;
import java.util.Map;

import junit.framework.TestCase;

public class ReviewTreeComparatorTest extends TestCase {
	private final ReviewTreeComparator comparator = new ReviewTreeComparator();

	private final Review r1 = new Review("http://localhost", "prkey", new User("myauthor"), new User("mymoderator"));

	public void testCompareViewerObjectObject() {

		final GeneralComment gc1 = new GeneralComment(r1, null);
		final GeneralComment gc2 = new GeneralComment(r1, null);
		assertEquals(0, comparator.compare(null, gc1, gc1));
		assertEquals(0, comparator.compare(null, gc1, gc2));
		Date now = new Date();
		gc1.setCreateDate(now);
		gc2.setCreateDate(DateUtils.addMinutes(now, 1));
		assertTrue(comparator.compare(null, gc1, gc2) < 0);
		assertTrue(comparator.compare(null, gc2, gc1) > 0);

		final CrucibleFileInfo cfi = new CrucibleFileInfo();
		final VersionedComment vc1 = new VersionedComment(r1, cfi);
		final VersionedComment vc2 = new VersionedComment(r1, cfi);
		assertEquals(0, comparator.compare(null, vc1, vc1));
		assertEquals(0, comparator.compare(null, vc1, vc2));
		vc1.setCreateDate(now);
		vc2.setCreateDate(DateUtils.addMinutes(now, 1));
		assertTrue(comparator.compare(null, vc1, vc2) < 0);
		assertTrue(comparator.compare(null, vc2, vc1) > 0);
		// now with lines => should be sorted ascending
		Map<String, IntRanges> lineRanges = MiscUtil.buildHashMap();
		lineRanges.put("10", new IntRanges(new IntRange(4)));
		vc1.setLineRanges(lineRanges);
		assertTrue("file comments should precede line comments", comparator.compare(null, vc1, vc2) > 0);

		ReviewTreeNode dirNode1 = new ReviewTreeNode(null, "abc");
		ReviewTreeNode dirNode2 = new ReviewTreeNode(null, "abd");
		assertTrue("directory nodes should preced comments", comparator.compare(null, dirNode1, vc2) < 0);
		assertTrue("directory nodes should preced comments", comparator.compare(null, vc2, dirNode1) > 0);
		assertTrue("directory should be sorted alphabetically", comparator.compare(null, dirNode1, dirNode2) < 0);
		assertTrue("directory should be sorted alphabetically", comparator.compare(null, dirNode2, dirNode1) > 0);
	}
}
