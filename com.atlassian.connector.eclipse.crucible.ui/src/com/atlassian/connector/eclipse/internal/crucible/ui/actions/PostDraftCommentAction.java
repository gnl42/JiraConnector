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

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewActionListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.BackgroundJobReviewAction.RemoteCrucibleOperation;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

public class PostDraftCommentAction extends BaseSelectionListenerAction implements IReviewAction {

	private static final String PUBLISH_COMMENT = "Publish Comment";

	private Review review;

	private IReviewActionListener actionListener;

	public PostDraftCommentAction() {
		super(PUBLISH_COMMENT);
		setEnabled(false);
	}

	public void run() {
		final Comment comment = (Comment) getStructuredSelection().getFirstElement();
		IAction action = new BackgroundJobReviewAction("Publish Comment", review,
				WorkbenchUtil.getShell(), "Publishing selected comment for review " + review.getPermId().getId(),
				CrucibleImages.COMMENT_POST, new RemoteCrucibleOperation() {
			public void run(CrucibleServerFacade2 server, ConnectionCfg serverCfg)
					throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
				server.publishComment(serverCfg, review.getPermId(), comment.getPermId());
			}
		}, true);
		action.run();

		if (actionListener != null) {
			actionListener.actionRan(this);
		}
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		review = null;

		Object element = selection.getFirstElement();
		if (element instanceof Comment && selection.size() == 1) {
			review = getActiveReview();
			if (review != null && CrucibleUiUtil.canModifyComment(review, (Comment) element)
					&& CrucibleUtil.canPublishDraft((Comment) element)) {
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
	public ImageDescriptor getImageDescriptor() {
		return CrucibleImages.COMMENT_POST;
	}

	@Override
	public String getToolTipText() {
		return PUBLISH_COMMENT;
	}
}
