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

package com.atlassian.connector.eclipse.internal.crucible.ui.views;

import com.atlassian.connector.eclipse.internal.crucible.core.VersionedCommentDateComparator;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class ReviewTreeComparator extends ViewerComparator {

	private final VersionedCommentDateComparator versionedCommentDateComparator = new VersionedCommentDateComparator();

	@Override
	public int category(Object element) {
		if (element instanceof ReviewTreeNode) {
			return ((ReviewTreeNode) element).getCategory();
		}
		return super.category(element);
	}

	@Override
	public int compare(Viewer aViewer, Object e1, Object e2) {
		boolean isE1Dir = e1 instanceof ReviewTreeNode;
		boolean isE2Dir = e2 instanceof ReviewTreeNode;
		if (isE1Dir && isE2Dir) {
			ReviewTreeNode node1 = (ReviewTreeNode) e1;
			ReviewTreeNode node2 = (ReviewTreeNode) e2;

			if (node1.getCrucibleFileInfo() != null && node2.getCrucibleFileInfo() != null) {
				return node1.getCrucibleFileInfo().getPermId().getId().compareTo(
						node2.getCrucibleFileInfo().getPermId().getId());
			}

			return super.compare(aViewer, e1, e2);
		} else if (!isE1Dir && !isE2Dir) {

			boolean isE1VersionedComment = e1 instanceof VersionedComment;
			boolean isE2VersionedComment = e2 instanceof VersionedComment;
			if (isE1VersionedComment && isE2VersionedComment) {
				return versionedCommentDateComparator.compare((VersionedComment) e1, (VersionedComment) e2);
			}

			boolean isE1Comment = e1 instanceof Comment;
			boolean isE2Comment = e2 instanceof Comment;
			if (isE1Comment && isE2Comment) {
				return ((Comment) e1).getCreateDate().compareTo(((Comment) e2).getCreateDate());
			} else {
				return super.compare(aViewer, e1, e2);
			}
		} else {
			return isE1Dir ? -1 : 1;
		}

	}
}