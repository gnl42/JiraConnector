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
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewActionListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.dialogs.CrucibleEditCommentDialog;
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
import org.eclipse.ui.actions.BaseSelectionListenerAction;

/**
 * 
 * @author Pawel Niewiadomski
 */
public class EditCommentAction extends BaseSelectionListenerAction implements IReviewAction {

	private Review review;

	private IReviewActionListener actionListener;

	public EditCommentAction() {
		super("Edit Comment");
		setEnabled(false);
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

		CrucibleEditCommentDialog commentDialog = new CrucibleEditCommentDialog(WorkbenchUtil.getShell(),
				"Edit Comment", review, (Comment) getStructuredSelection().getFirstElement(), task.getTaskKey(),
				task.getTaskId(), taskRepository, client);
		commentDialog.open();

		if (this.actionListener != null) {
			this.actionListener.actionRan(this);
		}
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return CrucibleImages.COMMENT_EDIT;
	}

	@Override
	public String getToolTipText() {
		return "Edit Comment";
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		this.review = null;

		Object element = selection.getFirstElement();
		if (element instanceof Comment && selection.size() == 1) {
			this.review = getActiveReview();
			if (this.review != null && CrucibleUiUtil.canModifyComment(review, (Comment) element)) {
				return true;
			}
		}
		return false;
	}

	public void setActionListener(IReviewActionListener listener) {
		this.actionListener = listener;
	}

	/**
	 * Return active review this action should be run against to. Review is associated during
	 * {@link #updateSelection(IStructuredSelection)}
	 * 
	 * @return
	 */
	protected Review getActiveReview() {
		return CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
	}
}
