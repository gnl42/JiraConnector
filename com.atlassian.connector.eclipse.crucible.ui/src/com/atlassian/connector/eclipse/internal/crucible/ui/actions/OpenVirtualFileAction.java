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
import com.atlassian.connector.eclipse.internal.fisheye.ui.FishEyeImages;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.RepositoryType;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.team.internal.ui.ITeamUIImages;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

public class OpenVirtualFileAction extends BaseSelectionListenerAction {
	private CrucibleFileInfo fileInfo;

	private VersionedComment comment;

	private Review review;

	private ITask task;

	private final boolean oldFile;

	public OpenVirtualFileAction(boolean oldFile) {
		super(oldFile ? "Open Revision Before Changes" : "Open Revision After Changes");
		this.oldFile = oldFile;
		setEnabled(false);
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		fileInfo = null;
		review = null;
		task = null;
		comment = null;
		if (selection.size() == 1 && selection.getFirstElement() instanceof CrucibleFileInfo) {
			fileInfo = (CrucibleFileInfo) selection.getFirstElement();
			review = getReview(fileInfo);
			task = getTask(fileInfo);
			comment = null;
			// FIXME wseliga support comment
//			if (paths[0].getLastSegment() instanceof VersionedComment) {
//				comment = (VersionedComment) paths[0].getLastSegment();
//			}

			VersionedVirtualFile fileDescriptor;
			if (oldFile) {
				fileDescriptor = fileInfo.getOldFileDescriptor();
			} else {
				fileDescriptor = fileInfo.getFileDescriptor();
			}

			return (fileInfo.getRepositoryType() == RepositoryType.UPLOAD || fileInfo.getRepositoryType() == RepositoryType.SCM)
					&& fileDescriptor != null
					&& fileDescriptor.getUrl() != null
					&& fileDescriptor.getUrl().length() > 0
					&& fileDescriptor.getRevision() != null
					&& fileDescriptor.getRevision().length() > 0;
		}
		return false;
	}

	private Review getReview(CrucibleFileInfo fileInfo) {
		return CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
	}

	private ITask getTask(CrucibleFileInfo fileInfo) {
		return CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveTask();
	}

	@Override
	public void run() {
		switch (fileInfo.getRepositoryType()) {
		case UPLOAD:
			OpenUploadedVirtualFileAction action = new OpenUploadedVirtualFileAction(task, new CrucibleFile(fileInfo,
					oldFile), oldFile ? fileInfo.getOldFileDescriptor() : fileInfo.getFileDescriptor(), review,
					comment, WorkbenchUtil.getShell(), PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow()
							.getActivePage());
			action.run();
			break;
		case SCM:
			OpenVersionedVirtualFileAction action1 = new OpenVersionedVirtualFileAction(task, new CrucibleFile(
					fileInfo, oldFile), comment, review);
			action1.run();
			break;
		default:
			StatusHandler.log(new Status(IStatus.WARNING, CrucibleUiPlugin.PLUGIN_ID, "Invalid Repository Type"));
		}
	}

	@SuppressWarnings("restriction")
	@Override
	public ImageDescriptor getImageDescriptor() {
		if (oldFile) {
			return FishEyeImages.REPOSITORY;
		} else {
			return TeamImages.getImageDescriptor(ITeamUIImages.IMG_SYNC_MODE_CATCHUP);
		}
	}
};
