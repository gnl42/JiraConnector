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
import com.atlassian.connector.eclipse.internal.crucible.ui.ActiveReviewManager.IReviewActivationListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.dialogs.CrucibleAddCommentDialog;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.widgets.Display;
import java.util.Collection;

/**
 * Action to add a general file comment to the active review
 * 
 * @author Shawn Minto
 * @author Thomas Ehrnhoefer
 * @author Pawel Niewiadomski
 */
public class AddGeneralCommentToActiveReviewAction extends Action implements IReviewActivationListener {

	private Review review;

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

		commentDialog.open();
	}

	public AddGeneralCommentToActiveReviewAction() {
		super("Add General Comment");
		setEnabled(false);
		setToolTipText("Add General Comment To Active Review");
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return CrucibleImages.ADD_COMMENT;
	}

	public void reviewActivated(final ITask task, final Review aReview) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				AddGeneralCommentToActiveReviewAction.this.review = aReview;
				setEnabled(AddGeneralCommentToActiveReviewAction.this.review != null);
			}
		});
	}

	public void reviewDeactivated(ITask task, Review aReview) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				AddGeneralCommentToActiveReviewAction.this.review = null;
				setEnabled(false);
			}
		});
	}

	public void reviewUpdated(ITask task, Review aReview, Collection<CrucibleNotification> differences) {
		reviewActivated(task, aReview);
	}

}
