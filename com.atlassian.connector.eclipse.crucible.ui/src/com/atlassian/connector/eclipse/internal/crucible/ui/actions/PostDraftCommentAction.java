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

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

import org.eclipse.swt.widgets.Shell;

public class PostDraftCommentAction extends AbstractBackgroundJobReviewAction {

	public PostDraftCommentAction(final Review review, final Comment comment, Shell shell) {
		super("Publish Comment", review, comment, shell, "Publishing selected comment for review "
				+ review.getPermId().getId(), CrucibleImages.COMMENT_POST, new RemoteOperation() {

			public void run(CrucibleServerFacade server, ServerData serverCfg) throws CrucibleLoginException,
					RemoteApiException, ServerPasswordNotProvidedException {
				server.publishComment(serverCfg, review.getPermId(), comment.getPermId());
			}
		});
	}

}
