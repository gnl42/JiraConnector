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
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.jetbrains.annotations.Nullable;

/**
 * @author Wojciech Seliga
 */
public class PublishAllDraftCommentsAction extends BaseSelectionListenerAction {

	private static final String DESCRIPTION = "Publish All Your Draft Comments";
	@Nullable
	private Review review;

	public PublishAllDraftCommentsAction() {
		super(DESCRIPTION);
		setEnabled(false);
	}

	@Override
	public void run() {
		IAction action = new BackgroundJobReviewAction(getText(), review, WorkbenchUtil.getShell(),
				"Publishing all draft comments in review " + review.getPermId().getId(), CrucibleImages.COMMENT_DELETE,
				new BackgroundJobReviewAction.RemoteCrucibleOperation() {
			public void run(CrucibleServerFacade2 server, ConnectionCfg serverCfg)
					throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
				server.publishAllCommentsForReview(serverCfg, review.getPermId());
			}
		}, true);
		action.run();

	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return CrucibleImages.PUBLISH_DRAFT_COMMENTS;
	}

	@Override
	public String getToolTipText() {
		return DESCRIPTION;
	}

	public void reviewUpdated(@Nullable Review freshReview) {
		this.review = freshReview;
		setEnabled(review != null
				&& freshReview.getNumberOfGeneralCommentsDrafts() + freshReview.getNumberOfVersionedCommentsDrafts() > 0);
	}
}
