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
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.mylyn.tasks.core.ITask;

public final class ReviewTreeUtils {

	private ReviewTreeUtils() {
	}

	public static CrucibleFileInfo getParentCrucibleFileInfo(ISelection selection) {
		if (selection instanceof ITreeSelection) {
			TreePath[] paths = ((ITreeSelection) selection).getPaths();
			if (paths == null || paths.length != 1) {
				return null;
			}

			final TreePath selectedPath = paths[0];

			for (int i = selectedPath.getSegmentCount() - 1; i >= 0; --i) {
				final Object segment = selectedPath.getSegment(i);
				if (segment instanceof CrucibleFileInfo) {
					return (CrucibleFileInfo) segment;
				}
			}
		}
		return null;
	}

	public static VersionedComment getParentVersionedComment(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection strSelection = (IStructuredSelection) selection;
			if (strSelection.size() == 1 && strSelection.getFirstElement() instanceof VersionedComment) {
				return (VersionedComment) strSelection.getFirstElement();
			}
		}
		if (selection instanceof ITreeSelection) {
			TreePath[] paths = ((ITreeSelection) selection).getPaths();
			if (paths == null || paths.length != 1) {
				return null;
			}

			final TreePath selectedPath = paths[0];

			for (int i = selectedPath.getSegmentCount() - 1; i >= 0; --i) {
				final Object segment = selectedPath.getSegment(i);
				if (segment instanceof VersionedComment) {
					return (VersionedComment) segment;
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param selection
	 *            expected {@link ITreeSelection} - otherwise it won't help
	 * @return
	 */
	public static boolean hasDraftParent(ISelection selection) {
		if (selection instanceof ITreeSelection) {
			ITreeSelection treeSelection = (ITreeSelection) selection;
			final Object selectedElement = treeSelection.getFirstElement();
			if (selectedElement == null || treeSelection.size() != 1) {
				return false;
			}

			TreePath[] paths = treeSelection.getPathsFor(selectedElement);
			if (paths == null || paths.length != 1) {
				return false;
			}

			final TreePath selectedPath = paths[0];

			for (int i = selectedPath.getSegmentCount() - 2; i >= 0; --i) {
				final Object segment = selectedPath.getSegment(i);
				if (segment instanceof Comment) {
					if (((Comment) segment).isDraft()) {
						return true;
					}
				} else {
					return false; // stop searching as soon as we navigate up to non-comment node
				}
			}
		}
		return false;
	}

	public static Review getReview(CrucibleFileInfo fileInfo) {
		return CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
	}

	public static ITask getTask(CrucibleFileInfo fileInfo) {
		return CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveTask();
	}

}
