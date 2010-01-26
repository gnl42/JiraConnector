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

package com.atlassian.connector.eclipse.internal.crucible.ui.editor.parts;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

public class CommentPart extends AbstractCommentPart<CommentPart> {

	private Composite composite;

	public CommentPart(Comment comment, Review crucibleReview) {
		super(comment, crucibleReview);
		// ignore
	}

	@Override
	protected boolean represents(Comment comment) {
		return this.comment.getPermId().equals(comment.getPermId());
	}

	@Override
	protected Control update(Composite parentComposite, FormToolkit toolkit, Comment newComment, Review newReview) {
		// TODO update the text 
		if (!CrucibleUtil.areGeneralCommentsDeepEquals(newComment, comment)) {
			this.comment = newComment;
			Control createControl = createOrUpdateControl(parentComposite, toolkit);
			return createControl;
		}
		return getSection();
	}

	@Override
	protected CommentPart createChildPart(Comment comment, Review crucibleReview2) {
		return new CommentPart(comment, crucibleReview2);
	}

	@Override
	protected Composite createSectionContents(Section section, FormToolkit toolkit) {
		composite = super.createSectionContents(section, toolkit);

		updateChildren(composite, toolkit, false, comment.getReplies());
		return composite;
	}

}
