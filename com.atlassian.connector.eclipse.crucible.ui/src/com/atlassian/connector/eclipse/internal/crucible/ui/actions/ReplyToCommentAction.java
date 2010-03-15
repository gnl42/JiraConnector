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
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewActionListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.dialogs.CrucibleAddCommentDialog;
import com.atlassian.connector.eclipse.internal.crucible.ui.dialogs.ICommentCreatedListener;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.jetbrains.annotations.Nullable;

/**
 * Action to reply to a comment
 * 
 * @author Shawn Minto
 * @author Thomas Ehrnhoefer
 * @author Pawel Niewiadomski
 */
public class ReplyToCommentAction extends BaseSelectionListenerAction implements IReviewAction {

	private static final String REPLY_TO_COMMENT = "Reply to Comment";

	private Review review;

	private IReviewActionListener actionListener;

	private final ICommentCreatedListener commentCreatedListener;

	public ReplyToCommentAction() {
		this(null);
	}

	public ReplyToCommentAction(@Nullable ICommentCreatedListener listener) {
		super(REPLY_TO_COMMENT);
		setEnabled(false);
		this.commentCreatedListener = listener;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return TasksUiImages.COMMENT_REPLY_SMALL;
	}

	@Override
	public void run() {
		TaskRepository taskRepository = CrucibleUiUtil.getCrucibleTaskRepository(review);
		ITask task = CrucibleUiUtil.getCrucibleTask(review);
		CrucibleRepositoryConnector connector = CrucibleCorePlugin.getRepositoryConnector();
		CrucibleClient client = connector.getClientManager().getClient(taskRepository);
		if (client == null) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
					"Unable to get client, please try to refresh"));
			return;
		}

		CrucibleAddCommentDialog commentDialog = new CrucibleAddCommentDialog(WorkbenchUtil.getShell(),
				REPLY_TO_COMMENT, review, task.getTaskKey(), task.getTaskId(), taskRepository, client);

		if (commentCreatedListener != null) {
			commentDialog.addCommentCreatedListener(commentCreatedListener);
		}

		commentDialog.setParentComment((Comment) getStructuredSelection().getFirstElement());
		commentDialog.open();

		if (actionListener != null) {
			actionListener.actionRan(this);
		}
	}

	@Override
	public String getToolTipText() {
		return REPLY_TO_COMMENT;
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		this.review = null;

		Object element = selection.getFirstElement();
		if (element instanceof Comment && selection.size() == 1) {
			this.review = getActiveReview();
			if (this.review != null && CrucibleUtil.canAddCommentToReview(review)) {
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
}