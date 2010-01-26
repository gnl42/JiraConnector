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
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.jface.viewers.ISelection;
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

	public static Review getReview(CrucibleFileInfo fileInfo) {
		return CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
	}

	public static ITask getTask(CrucibleFileInfo fileInfo) {
		return CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveTask();
	}

}
