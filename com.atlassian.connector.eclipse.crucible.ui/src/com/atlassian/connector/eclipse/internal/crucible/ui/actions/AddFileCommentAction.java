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
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.dialogs.CrucibleAddCommentDialog;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

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

		commentDialog.setReviewItem(new CrucibleFile(fileInfo, true));
		commentDialog.open();
	}

	public AddFileCommentAction(String text, String tooltipText) {
		super(text);
		setEnabled(false);
		setToolTipText(tooltipText);
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return CrucibleImages.ADD_COMMENT;
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		this.review = null;
		this.fileInfo = null;

		Object element = selection.getFirstElement();
		if (element instanceof CrucibleFileInfo && selection.size() == 1) {
			this.review = CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
			if (this.review != null && CrucibleUtil.canAddCommentToReview(review)) {
				this.fileInfo = (CrucibleFileInfo) element;
				return true;
			}
		}

		if (selection instanceof ITreeSelection) {
			this.review = CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
			TreePath[] paths = ((ITreeSelection) selection).getPaths();
			if (paths != null && paths.length == 1) {
				for (TreePath path : paths) {
					for (int i = 0, s = path.getSegmentCount(); i < s; ++i) {
						if (path.getSegment(i) instanceof CrucibleFileInfo) {
							this.fileInfo = (CrucibleFileInfo) path.getSegment(i);
							return true;
						}
					}
				}
			}
		}
		return false;
	}

}
