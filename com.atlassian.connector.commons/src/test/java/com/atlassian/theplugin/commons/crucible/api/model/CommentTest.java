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

import com.spartez.util.junit3.TestUtil;
import junit.framework.TestCase;

public class CommentTest extends TestCase {
	private final Review r1 = ReviewTestUtil.createReview("http://localhost");
	private final Review r2 = ReviewTestUtil.createReview("http://localhost");

	public CommentTest() {
		r1.setPermId(new PermId("r1"));
		r2.setPermId(new PermId("r2"));
	}

	public void testEqualsWithReviews() {
		GeneralComment gc1 = new GeneralComment(r1, null);
		GeneralComment gc2 = new GeneralComment(r1, null);
		TestUtil.assertEqualsSymmetrical(gc1, gc2);
		GeneralComment gc3 = new GeneralComment(r2, null);
		TestUtil.assertNotEquals(gc1, gc3);
	}

	public void testEqualsWithParentComments() {
		GeneralComment gc1 = createGeneralComment(r1, null, "gc1");
		GeneralComment gc4 = createGeneralComment(r1, null, "gc4");
		GeneralComment gc2 = createGeneralComment(r1, gc4, "gc2");// new GeneralComment(r1, gc4);
		GeneralComment gc3 = createGeneralComment(r1, gc1, "gc3");
		GeneralComment gc5 = createGeneralComment(r2, gc1, "gc5");
		GeneralComment gc6 = createGeneralComment(r2, gc1, "gc5");
		TestUtil.assertEqualsSymmetrical(gc2, gc2);
		TestUtil.assertNotEquals(gc5, gc3);
		TestUtil.assertNotEquals(gc2, gc3);
		TestUtil.assertEqualsSymmetrical(gc5, gc6);
	}

	private GeneralComment createGeneralComment(Review review, Comment parentComment, String permId) {
		GeneralComment comment = new GeneralComment(review, parentComment);
		if (parentComment != null) {
			parentComment.addReply(comment);
		}
		comment.setPermId(new PermId(permId));
		return comment;
	}

	public void testHasDraftParents() {
		GeneralComment gc1 = createGeneralComment(r1, null, "gc1");
		GeneralComment gc2 = createGeneralComment(r1, gc1, "gc1");
		GeneralComment gc3 = createGeneralComment(r1, gc2, "gc3");
		GeneralComment gc4 = createGeneralComment(r1, gc2, "gc4");
		GeneralComment gc5 = createGeneralComment(r1, gc3, "gc5");
		GeneralComment gc6 = createGeneralComment(r1, gc5, "gc6");
		gc3.setDraft(true);
		assertTrue(gc5.hasDraftParents());
		assertTrue(gc6.hasDraftParents());
		assertFalse(gc4.hasDraftParents());
		assertFalse(gc3.hasDraftParents());
		assertFalse(gc2.hasDraftParents());
		assertFalse(gc1.hasDraftParents());
	}

	public void testRemoveReply() {
		GeneralComment gc1 = createGeneralComment(r1, null, "gc1");
		GeneralComment gc2 = createGeneralComment(r1, gc1, "gc1");
		GeneralComment gc3 = createGeneralComment(r1, gc2, "gc3");

		assertFalse(gc3.removeReply(null));

		assertFalse(gc3.removeReply(gc2));
		assertFalse(gc1.removeReply(gc3));

		assertTrue(gc2.getReplies().contains(gc3));
		assertTrue(gc2.removeReply(gc3));
		assertFalse(gc2.getReplies().contains(gc3));
		assertFalse(gc2.removeReply(gc3));

	}
}
