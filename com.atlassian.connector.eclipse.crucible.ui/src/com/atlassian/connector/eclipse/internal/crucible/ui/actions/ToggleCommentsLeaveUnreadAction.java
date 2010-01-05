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

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewActionListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.MarkCommentsLeaveUnreadJob;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.MarkCommentsReadJob;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

import java.util.List;

public class ToggleCommentsLeaveUnreadAction extends BaseSelectionListenerAction implements IReviewAction {

	private static final String LEAVE_UNREAD = "Leave Unread";

	private static final String MARK_READ = "Mark as Read";

	private Review review;

	private IReviewActionListener actionListener;

	private boolean markCommentsAsRead;

	public ToggleCommentsLeaveUnreadAction() {
		super(LEAVE_UNREAD);
		setEnabled(false);
	}

	@SuppressWarnings("unchecked")
	public void run() {
		final List<Comment> comments = getStructuredSelection().toList();
		Job job;
		if (markCommentsAsRead) {
			job = new MarkCommentsReadJob(review, comments, true);
		} else {
			job = new MarkCommentsLeaveUnreadJob(review, comments, true);
		}
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				if (actionListener != null) {
					actionListener.actionRan(ToggleCommentsLeaveUnreadAction.this);
				}
			}
		});
		job.schedule();
	}

	@Override
	public String getText() {
		return markCommentsAsRead ? MARK_READ : LEAVE_UNREAD;
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		this.review = null;
		this.markCommentsAsRead = false;

		final List<?> comments = getStructuredSelection().toList();
		if (comments.size() > 0) {
			this.review = getActiveReview();
			if (this.review != null) {
				for (Object comment : comments) {
					if (!(comment instanceof Comment)) {
						return false;
					}
					switch (((Comment) comment).getReadState()) {
					case LEAVE_UNREAD:
						markCommentsAsRead = true;
						break;
					case UNKNOWN:
						// server doesn't support it
						return false;
					default:
						// nothing here
					}
				}
				return true;
			}
		}
		return false;
	}

	protected Review getActiveReview() {
		return CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
	}

	public void setActionListener(IReviewActionListener listener) {
		this.actionListener = listener;
	}

	@Override
	public String getToolTipText() {
		return markCommentsAsRead ? MARK_READ : LEAVE_UNREAD;
	}
}
