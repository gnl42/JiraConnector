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

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.actions.ActionFactory;

import java.util.List;
import java.util.ResourceBundle;

/**
 * This class is modelled after {@link org.eclipse.team.internal.ui.synchronize.actions.NavigateAction}
 * 
 * @author wseliga
 */
public class CommentNavigationAction extends Action {
	private final boolean isNext;

	private final AbstractTreeViewer viewer;

	private static final String ACTION_BUNDLE = "com.atlassian.connector.eclipse.internal.crucible.ui.actions.actions";

	public CommentNavigationAction(AbstractTreeViewer viewer, IViewSite viewSite, boolean next) {
		this.viewer = viewer;
		this.isNext = next;
		IActionBars bars = viewSite.getActionBars();
		if (next) {
			Utils.initAction(this, "action.navigateNext.", ResourceBundle.getBundle(ACTION_BUNDLE)); //$NON-NLS-1$
			// I would like to use ActionFactory.NEXT.getCommandId(), but it's since e3.5...
			// TODO e3.5+ use constants 
			setActionDefinitionId("org.eclipse.ui.navigate.next");
			if (bars != null) {
				bars.setGlobalActionHandler(ActionFactory.NEXT.getId(), this);
			}
		} else {
			Utils.initAction(this, "action.navigatePrevious.", ResourceBundle.getBundle(ACTION_BUNDLE)); //$NON-NLS-1$
			// I would like to use ActionFactory.PREVIOUS.getCommandId(), but it's since e3.5...
			// TODO e3.5+ use constants 
			setActionDefinitionId("org.eclipse.ui.navigate.previous");
			if (bars != null) {
				bars.setGlobalActionHandler(ActionFactory.PREVIOUS.getId(), this);
			}
		}
	}

	public void run() {
		ISelection selection = viewer.getSelection();
		Comment currentComment = null;
		if (selection instanceof IStructuredSelection) {
			Object first = ((IStructuredSelection) selection).getFirstElement();
			if (first instanceof Comment) {
				currentComment = (Comment) first;
			}
		}
		List<Comment> comments = prepareListOfComments();

		if (comments.size() == 0) {
			return;
		}

		ViewerComparator comparator = viewer.getComparator();
		if (currentComment != null) {
			for (int i = 0, s = comments.size(); i < s; ++i) {
				if (comparator.compare(viewer, comments.get(i), currentComment) == 0) {
					if (isNext) {
						currentComment = comments.get(i + 1 >= comments.size() ? 0 : i + 1);
					} else {
						currentComment = comments.get(i - 1 < 0 ? (comments.size() - 1) : i - 1);
					}
					break;
				}
			}
		} else {
			currentComment = comments.get(isNext ? 0 : (comments.size() - 1));
		}

		Object[] expanded = viewer.getExpandedElements();
		try {
			viewer.getControl().setRedraw(false);
			viewer.expandAll();
			viewer.setSelection(new StructuredSelection(currentComment));
			ITreeSelection treeSelection = (ITreeSelection) viewer.getSelection();
			if (treeSelection.getPaths() != null && treeSelection.getPaths().length > 0) {
				Object[] toBeExpanded = new Object[expanded.length + treeSelection.getPaths()[0].getSegmentCount()];
				System.arraycopy(expanded, 0, toBeExpanded, 0, expanded.length);
				for (int i = 0, s = treeSelection.getPaths()[0].getSegmentCount(); i < s; ++i) {
					toBeExpanded[expanded.length + i] = treeSelection.getPaths()[0].getSegment(i);
				}
				viewer.setExpandedElements(toBeExpanded);
				viewer.addSelectionChangedListener(null);
			}
		} finally {
			viewer.getControl().setRedraw(true);
		}
	}

	private List<Comment> prepareListOfComments() {
		ITreeContentProvider provider = (ITreeContentProvider) viewer.getContentProvider();
		List<Comment> comments = MiscUtil.buildArrayList();
		comments.addAll(getComments(viewer, provider, provider.getElements(viewer.getInput())));
		return comments;
	}

	private List<Comment> getComments(AbstractTreeViewer viewer, ITreeContentProvider provider,
			Object[] unsortedElements) {
		Object[] elements = unsortedElements.clone();
		viewer.getComparator().sort(viewer, elements);

		List<Comment> comments = MiscUtil.buildArrayList();
		for (Object element : elements) {
			if (element instanceof Comment) {
				comments.add((Comment) element);
			}
			comments.addAll(getComments(viewer, provider, provider.getChildren(element)));
		}
		return comments;
	}
}