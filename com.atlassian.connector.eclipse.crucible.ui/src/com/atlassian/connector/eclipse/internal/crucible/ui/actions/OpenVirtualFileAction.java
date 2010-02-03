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
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.OpenVirtualFileJob;
import com.atlassian.connector.eclipse.internal.crucible.ui.views.CommentView;
import com.atlassian.connector.eclipse.internal.fisheye.ui.FishEyeImages;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.connector.eclipse.ui.commons.AtlassianUiUtil;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.RepositoryType;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.ITeamUIImages;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

@SuppressWarnings("restriction")
public class OpenVirtualFileAction extends BaseSelectionListenerAction {
	private CrucibleFileInfo fileInfo;

	private VersionedComment comment;

	private Review review;

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
		comment = null;
		if (selection.size() != 1) {
			return false;
		}
		if (selection.getFirstElement() instanceof CrucibleFileInfo) {
			fileInfo = (CrucibleFileInfo) selection.getFirstElement();
		} else if (selection.getFirstElement() instanceof Comment) {
			comment = ReviewModelUtil.getParentVersionedComment((Comment) selection.getFirstElement());
			fileInfo = ReviewTreeUtils.getParentCrucibleFileInfo(selection);
		}

		if (fileInfo == null) {
			return false;
		}

		review = ReviewTreeUtils.getReview(fileInfo);

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
				&& fileDescriptor.getRevision() != null && fileDescriptor.getRevision().length() > 0;
	}

	@Override
	public void run() {
		AtlassianUiUtil.ensureViewIsVisible(CommentView.ID);
		OpenVirtualFileJob job = new OpenVirtualFileJob(review, new CrucibleFile(fileInfo, oldFile), comment);
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		if (oldFile) {
			return FishEyeImages.REPOSITORY;
		} else {
			return TeamImages.getImageDescriptor(ITeamUIImages.IMG_SYNC_MODE_CATCHUP);
		}
	}
};
