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
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.CompareVirtualFilesJob;
import com.atlassian.connector.eclipse.internal.crucible.ui.views.CommentView;
import com.atlassian.connector.eclipse.ui.commons.AtlassianUiUtil;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.FileType;
import com.atlassian.theplugin.commons.crucible.api.model.RepositoryType;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.ITeamUIImages;
import org.eclipse.team.ui.TeamImages;
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
		AtlassianUiUtil.ensureViewIsVisible(CommentView.ID);
		CompareVirtualFilesJob job = new CompareVirtualFilesJob(review, fileInfo, comment);
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		fileInfo = null;
		review = null;
		comment = null;

		if (selection.size() == 1) {
			if (selection.getFirstElement() instanceof CrucibleFileInfo) {
				fileInfo = (CrucibleFileInfo) selection.getFirstElement();
				comment = null;
			} else if (selection.getFirstElement() instanceof Comment) {
				comment = ReviewModelUtil.getParentVersionedComment((Comment) selection.getFirstElement());
				fileInfo = ReviewTreeUtils.getParentCrucibleFileInfo(selection);
			}

			if (fileInfo == null) {
				return false;
			}
			review = ReviewTreeUtils.getReview(fileInfo);
			// FIXME wseliga restore support for comment
			// if (paths[0].getLastSegment() instanceof VersionedComment) {
			// comment = (VersionedComment) paths[0].getLastSegment();
			// }

			final VersionedVirtualFile oldFileDescriptor = fileInfo.getOldFileDescriptor();
			final VersionedVirtualFile newFileDescriptor = fileInfo.getFileDescriptor();

			boolean oldFileHasRevision = oldFileDescriptor != null && oldFileDescriptor.getRevision() != null
					&& oldFileDescriptor.getRevision().length() > 0;

			boolean newFileHasRevision = newFileDescriptor != null && newFileDescriptor.getRevision() != null
					&& newFileDescriptor.getRevision().length() > 0;

			return (fileInfo.getRepositoryType() == RepositoryType.UPLOAD || fileInfo.getRepositoryType() == RepositoryType.SCM)
					&& fileInfo.getFileType() == FileType.File && oldFileHasRevision && newFileHasRevision;
		}
		return false;
	}

	@SuppressWarnings("restriction")
	@Override
	public ImageDescriptor getImageDescriptor() {
		return TeamImages.getImageDescriptor(ITeamUIImages.IMG_SYNC_VIEW);
	}
};
