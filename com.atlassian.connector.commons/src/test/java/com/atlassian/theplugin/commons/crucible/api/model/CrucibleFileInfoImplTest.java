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

import com.atlassian.theplugin.commons.crucible.api.model.Comment.ReadState;
import junit.framework.TestCase;

public class CrucibleFileInfoImplTest extends TestCase {

	private final Review review = ReviewTestUtil.createReview("http://myurl.com");

	private CrucibleFileInfo prepareCrucibleFileInfo() {
		PermId permId1 = new PermId("1");
		CrucibleFileInfo cfi = new CrucibleFileInfo(null, null, permId1);
		VersionedComment vc1 = new VersionedComment(review, cfi);
		VersionedComment rpl1 = new VersionedComment(review, cfi);
		rpl1.setDraft(true);
		VersionedComment rpl2 = new VersionedComment(review, cfi);
		rpl2.setDraft(false);
		VersionedComment rpl3 = new VersionedComment(review, cfi);
		rpl3.setDraft(true);
		VersionedComment rpl4 = new VersionedComment(review, cfi);
		rpl4.setReadState(ReadState.UNREAD);
		rpl4.setDraft(true);
		vc1.addReply(rpl1);
		vc1.addReply(rpl2);
		rpl2.addReply(rpl3);
		rpl2.addReply(rpl4);
		cfi.addComment(vc1);
		VersionedComment vc2 = new VersionedComment(review, cfi);
		VersionedComment rpl5 = new VersionedComment(review, cfi);
		rpl5.setReadState(ReadState.UNREAD);
		vc2.addReply(rpl5);
		cfi.addComment(vc2);
		return cfi;
	}

	public void testGetNumberOfUnreadComments() {
		CrucibleFileInfo cfi = prepareCrucibleFileInfo();
		assertEquals(2, cfi.getNumberOfUnreadComments());
	}

	public void testGetNumberOfComments() {
		CrucibleFileInfo cfi = prepareCrucibleFileInfo();
		assertEquals(7, cfi.getNumberOfComments());
	}

	public void testGetNumberOfDraftComments() {
		CrucibleFileInfo cfi = prepareCrucibleFileInfo();
		assertEquals(3, cfi.getNumberOfCommentsDrafts());
	}

}
