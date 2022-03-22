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

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewTestUtil;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import junit.framework.TestCase;

public class ReviewModelUtilTest extends TestCase {
	public void testGetParentVersionedComment() {
		Review review = ReviewTestUtil.createReview("http://crucible.atlassian.com/cru/");
		GeneralComment gc1 = new GeneralComment(review, null);
		VersionedComment vc1 = new VersionedComment(review, new CrucibleFileInfo(null, null, new PermId("fileid")));
		GeneralComment rc1 = new GeneralComment(review, gc1);
		GeneralComment rc2 = new GeneralComment(review, rc1);

		GeneralComment rc3 = new GeneralComment(review, vc1);
		GeneralComment rc4 = new GeneralComment(review, rc3);

		assertNull(ReviewModelUtil.getParentVersionedComment(null));
		assertNull(ReviewModelUtil.getParentVersionedComment(gc1));
		assertNull(ReviewModelUtil.getParentVersionedComment(rc1));
		assertNull(ReviewModelUtil.getParentVersionedComment(rc2));
		assertEquals(vc1, ReviewModelUtil.getParentVersionedComment(vc1));
		assertEquals(vc1, ReviewModelUtil.getParentVersionedComment(rc3));
		assertEquals(vc1, ReviewModelUtil.getParentVersionedComment(rc4));
	}

}
