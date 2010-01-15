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

import com.atlassian.connector.eclipse.ui.viewers.ArrayTreeContentProvider;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.util.MiscUtil;

import java.util.ArrayList;
import java.util.List;

public final class ReviewContentProvider extends ArrayTreeContentProvider {
	@Override
	public Object[] getChildren(Object inputElement) {
		if (inputElement instanceof ReviewTreeNode) {
			ReviewTreeNode myTreeNode = (ReviewTreeNode) inputElement;
			final ArrayList<Object> children = MiscUtil.buildArrayList();
			for (Object childNode : myTreeNode.getChildren()) {
				if (childNode instanceof ReviewTreeNode && ((ReviewTreeNode) childNode).getCrucibleFileInfo() != null
						&& ((ReviewTreeNode) childNode).getChildren().isEmpty()) {
					children.add(((ReviewTreeNode) childNode).getCrucibleFileInfo());
				} else {
					children.add(childNode);
				}
			}
			return children.toArray();
		}

		if (inputElement instanceof CrucibleFileInfo) {
			return ((CrucibleFileInfo) inputElement).getVersionedComments().toArray();
		}
		if (inputElement instanceof Comment) {
			return ((Comment) inputElement).getReplies().toArray();
		}
		return super.getChildren(inputElement);
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ReviewTreeNode) {
			ReviewTreeNode myTreeNode = (ReviewTreeNode) element;
			if (myTreeNode.getCrucibleFileInfo() != null) {
				List<VersionedComment> comments = myTreeNode.getCrucibleFileInfo().getVersionedComments();
				if (comments != null && comments.size() > 0) {
					return true;
				}
			}
			return !((ReviewTreeNode) element).getChildren().isEmpty();
		}

//		if (element instanceof Review) {
//			try {
//				return !((Review) element).getFiles().isEmpty();
//			} catch (ValueNotYetInitialized e) {
//				return super.hasChildren(element);
//			}
//		}
		if (element instanceof CrucibleFileInfo) {
			List<VersionedComment> comments = ((CrucibleFileInfo) element).getVersionedComments();
			if (comments != null && comments.size() > 0) {
				return true;
			}
		}
		if (element instanceof Comment) {
			List<Comment> replies = ((Comment) element).getReplies();
			if (replies != null && replies.size() > 0) {
				return true;
			}
		}
		return super.hasChildren(element);
	}

}