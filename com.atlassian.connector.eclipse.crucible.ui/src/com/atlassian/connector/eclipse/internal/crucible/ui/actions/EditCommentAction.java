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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.dialogs.CrucibleEditCommentDialog;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.widgets.Shell;

public class EditCommentAction extends AbstractListenableReviewAction {

	private final Shell shell;

	private final Comment comment;

	public EditCommentAction(Review review, Comment comment, Shell shell) {
		super("Edit Comment");
		this.review = review;
		this.comment = comment;
		this.shell = shell;
	}

	@Override
	protected Review getReview() {
		// ignore
		return null;
	}

	public void run(IAction action) {
		if (review == null) {
			return;
		}

		CrucibleRepositoryConnector connector = CrucibleCorePlugin.getRepositoryConnector();
		CrucibleClient client = connector.getClientManager().getClient(getTaskRepository());
		if (client == null) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
					"Unable to get client, please try to refresh"));
			return;
		}

		CrucibleEditCommentDialog commentDialog = new CrucibleEditCommentDialog(shell, getDialogTitle(), review,
				comment, getTaskKey(), getTaskId(), getTaskRepository(), client);
		commentDialog.open();
	}

	private String getDialogTitle() {
		return comment.isReply() == false ? "Edit Comment" : "Edit Reply";
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return CrucibleImages.COMMENT_EDIT;
	}

	@Override
	public String getToolTipText() {
		return "Edit";
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled() && CrucibleUiUtil.canModifyComment(review, comment);
	}

}
