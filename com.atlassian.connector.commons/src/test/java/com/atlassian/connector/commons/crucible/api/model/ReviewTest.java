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

package com.atlassian.connector.commons.crucible.api.model;

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewTestUtil;
import java.util.Collections;
import junit.framework.TestCase;

public class ReviewTest extends TestCase {
	public void testGenNumberGeneralCommentDrafts() {
		Review r = ReviewTestUtil.createReview("http://localhost");
		GeneralComment gc1 = new GeneralComment(r, null);
		r.setGeneralComments(Collections.<Comment> singletonList(gc1));
		GeneralComment r1 = new GeneralComment(r, gc1);
		gc1.addReply(r1);
		GeneralComment r2 = new GeneralComment(r, r1);
		r1.addReply(r2);
		GeneralComment r3 = new GeneralComment(r, r1);
		r2.addReply(r3);
		assertEquals(0, r.getNumberOfGeneralCommentsDrafts());
		gc1.setDraft(true);
		assertEquals(1, r.getNumberOfGeneralCommentsDrafts());
		r1.setDraft(true);
		assertEquals(2, r.getNumberOfGeneralCommentsDrafts());
		r2.setDraft(true);
		assertEquals(3, r.getNumberOfGeneralCommentsDrafts());
		r3.setDraft(true);
		assertEquals(4, r.getNumberOfGeneralCommentsDrafts());
	}

}
