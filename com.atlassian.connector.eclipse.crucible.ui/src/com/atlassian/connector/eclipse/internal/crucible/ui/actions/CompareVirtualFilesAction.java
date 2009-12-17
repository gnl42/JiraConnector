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
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.FileType;
import com.atlassian.theplugin.commons.crucible.api.model.RepositoryType;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

public class CompareVirtualFilesAction extends BaseSelectionListenerAction {
	private CrucibleFileInfo fileInfo;

	private VersionedComment comment;

	private Review review;

	public CompareVirtualFilesAction() {
		super("Compare Revisions");
		setEnabled(false);
	}

	@Override
	public void run() {
		IAction action;
		if (fileInfo.getRepositoryType() == RepositoryType.SCM) {
			action = new CompareVersionedVirtualFileAction(fileInfo, comment, review);
		} else {
			action = new CompareUploadedVirtualFileAction(fileInfo, comment, review, WorkbenchUtil.getShell());
		}
		action.run();
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		fileInfo = null;
		review = null;

		if (selection instanceof ITreeSelection) {
			TreePath[] paths = ((ITreeSelection) selection).getPaths();
			if (paths == null || paths.length != 1) {
				return false;
			}

			if (paths[0].getFirstSegment() instanceof CrucibleFileInfo) {
				fileInfo = (CrucibleFileInfo) paths[0].getFirstSegment();
				review = getReview(fileInfo);

				comment = null;
				if (paths[0].getLastSegment() instanceof VersionedComment) {
					comment = (VersionedComment) paths[0].getLastSegment();
				}

				VersionedVirtualFile oldFileDescriptor = fileInfo.getOldFileDescriptor();
				VersionedVirtualFile newFileDescriptor = fileInfo.getFileDescriptor();

				boolean oldFileHasRevision = oldFileDescriptor != null && oldFileDescriptor.getRevision() != null
						&& oldFileDescriptor.getRevision().length() > 0;

				boolean newFileHasRevision = newFileDescriptor != null && newFileDescriptor.getRevision() != null
						&& newFileDescriptor.getRevision().length() > 0;

				return (fileInfo.getRepositoryType() == RepositoryType.UPLOAD || fileInfo.getRepositoryType() == RepositoryType.SCM)
						&& fileInfo.getFileType() == FileType.File && oldFileHasRevision && newFileHasRevision;
			}
		}
		return false;
	}

	private Review getReview(CrucibleFileInfo fileInfo) {
		return CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
	}

};
