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

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.Comment.ReadState;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class UnreadCommentsViewerFilter extends ViewerFilter {
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		ITreeContentProvider provider = (ITreeContentProvider) ((AbstractTreeViewer) viewer).getContentProvider();
		Object[] roots = provider.getElements(viewer.getInput());

		return roots == parentElement || haveUnreadCommentAsLeaf(provider, element);
	}

	private boolean haveUnreadCommentAsLeaf(ITreeContentProvider provider, Object parentElement) {
		if (parentElement instanceof Comment) {
			if (((Comment) parentElement).getReadState().equals(ReadState.LEAVE_UNREAD)
					|| ((Comment) parentElement).getReadState().equals(ReadState.UNREAD)) {
				return true;
			}
		}
		for (Object element : provider.getChildren(parentElement)) {
			if (haveUnreadCommentAsLeaf(provider, element)) {
				return true;
			}
		}
		return false;
	}
};