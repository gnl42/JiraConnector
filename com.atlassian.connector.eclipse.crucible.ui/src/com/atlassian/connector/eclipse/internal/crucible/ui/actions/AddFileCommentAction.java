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

import com.atlassian.connector.commons.crucible.api.model.ReviewModelUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.dialogs.CrucibleAddCommentDialog;
import com.atlassian.connector.eclipse.internal.crucible.ui.dialogs.ICommentCreatedListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.views.ReviewTreeNode;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.jetbrains.annotations.Nullable;

/**
 * Action to add a general file comment to the active review
 * 
 * @author Shawn Minto
 * @author Thomas Ehrnhoefer
 * @author Pawel Niewiadomski
 */
public class AddFileCommentAction extends BaseSelectionListenerAction {

	private Review review;

	private CrucibleFileInfo fileInfo;

	private final ICommentCreatedListener commentCreatedListener;

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

		CrucibleAddCommentDialog commentDialog = new CrucibleAddCommentDialog(WorkbenchUtil.getShell(), getText(),
				review, task.getTaskKey(), task.getTaskId(), taskRepository, client);

		if (commentCreatedListener != null) {
			commentDialog.addCommentCreatedListener(commentCreatedListener);
		}

		commentDialog.setReviewItem(new CrucibleFile(fileInfo, true));
		commentDialog.open();
	}

	public AddFileCommentAction(@Nullable ICommentCreatedListener listener) {
		super("Add File Comment");
		setEnabled(false);
		setToolTipText("Add File Comment");
		this.commentCreatedListener = listener;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return CrucibleImages.ADD_COMMENT;
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		this.review = null;
		this.fileInfo = null;
		if (selection.size() != 1) {
			return false;
		}

		Object element = selection.getFirstElement();
		if (element instanceof CrucibleFileInfo && selection.size() == 1) {
			this.review = CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
			if (this.review != null && CrucibleUtil.canAddCommentToReview(review)) {
				this.fileInfo = (CrucibleFileInfo) element;
				return true;
			}
		}

		if (element instanceof Comment) {
			this.review = CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
			final VersionedComment parentVersionedComment = ReviewModelUtil.getParentVersionedComment((Comment) element);
			if (parentVersionedComment != null) {
				this.fileInfo = parentVersionedComment.getCrucibleFileInfo();
			}
			return true;
		}

		if (element instanceof ReviewTreeNode) {
			ReviewTreeNode reviewTreeNode = (ReviewTreeNode) element;
			this.review = CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
			fileInfo = reviewTreeNode.getCrucibleFileInfo();
			return fileInfo != null;
		}
		return false;
	}

}
