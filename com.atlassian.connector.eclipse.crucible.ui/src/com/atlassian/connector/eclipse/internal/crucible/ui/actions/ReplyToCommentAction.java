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

package com.atlassian.connector.eclipse.internal.crucible.ui.actions;

import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewActionListener;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;

/**
 * Action to reply to a comment
 * 
 * @author Shawn Minto
 * @author Thomas Ehrnhoefer
 */
public class ReplyToCommentAction extends AbstractAddCommentAction implements IReviewAction {
	private final Comment comment;

	private final CrucibleFile crucibleFile;

	private IReviewActionListener actionListener;

	public ReplyToCommentAction(Comment comment, Review review, CrucibleFile crucibleFile) {
		super("Reply to Comment");
		this.comment = comment;
		super.review = review;
		this.crucibleFile = crucibleFile;
	}

	@Override
	protected String getDialogTitle() {
		return getText();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return TasksUiImages.COMMENT_REPLY_SMALL;
	}

	@Override
	public final void run() {
		super.run();

		if (actionListener != null) {
			actionListener.actionRan(this);
		}
	}

	public void setActionListener(IReviewActionListener listener) {
		this.actionListener = listener;
	}

	@Override
	public String getToolTipText() {
		return "Reply";
	}

	@Override
	protected CrucibleFile getCrucibleFile() {
		return crucibleFile;
	}

	@Override
	protected Review getReview() {
		return review;
	}

	@Override
	protected LineRange getSelectedRange() {
		return null;
	}

	@Override
	protected Comment getParentComment() {
		return comment;
	}
}