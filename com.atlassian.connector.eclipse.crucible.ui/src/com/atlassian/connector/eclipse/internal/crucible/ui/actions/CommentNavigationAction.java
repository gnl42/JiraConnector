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

import com.atlassian.connector.eclipse.internal.crucible.ui.views.ReviewExplorerView;
import com.atlassian.connector.eclipse.ui.viewers.TreeViewerUtil;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.actions.ActionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * This class is modelled after {@link org.eclipse.team.internal.ui.synchronize.actions.NavigateAction}
 * 
 * @author wseliga
 */
public class CommentNavigationAction extends Action {
	private final boolean isNext;

	private final ReviewExplorerView reviewExplorerView;

	private final TreeViewer viewer;

	private static final String ACTION_BUNDLE = "com.atlassian.connector.eclipse.internal.crucible.ui.actions.actions";

	public CommentNavigationAction(ReviewExplorerView view, IViewSite viewSite, boolean next) {
		this.reviewExplorerView = view;
		this.viewer = reviewExplorerView.getViewer();
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
		if (currentComment == null) {
			currentComment = comments.get(isNext ? comments.size() - 1 : 0);
		}

		int start = 0;
		for (int i = 0, s = comments.size(); i < s; ++i) {
			if (comparator.compare(viewer, comments.get(i), currentComment) == 0) {
				start = i;
				break;
			}
		}

		Comment matching = null;
		if (isNext) {
			for (int i = start + 1, s = comments.size(); i < s; ++i) {
				if (reviewExplorerView.isFocusedOnUnreadComments()) {
					if (comments.get(i).isEffectivelyUnread()) {
						matching = comments.get(i);
					}
				} else {
					matching = comments.get(i);
				}
				if (matching != null) {
					break;
				}
			}

			if (matching == null) {
				for (int i = 0; i < start; ++i) {
					if (reviewExplorerView.isFocusedOnUnreadComments()) {
						if (comments.get(i).isEffectivelyUnread()) {
							matching = comments.get(i);
						}
					} else {
						matching = comments.get(i);
					}
					if (matching != null) {
						break;
					}
				}
			}
		} else {
			for (int i = start - 1; i >= 0; --i) {
				if (reviewExplorerView.isFocusedOnUnreadComments()) {
					if (comments.get(i).isEffectivelyUnread()) {
						matching = comments.get(i);
					}
				} else {
					matching = comments.get(i);
				}
				if (matching != null) {
					break;
				}
			}
			if (matching == null) {
				for (int i = comments.size() - 1; i > start; --i) {
					if (reviewExplorerView.isFocusedOnUnreadComments()) {
						if (comments.get(i).isEffectivelyUnread()) {
							matching = comments.get(i);
						}
					} else {
						matching = comments.get(i);
					}
					if (matching != null) {
						break;
					}
				}
			}

		}

		if (matching != null) {
			TreeViewerUtil.setSelection(viewer, matching);
		}
	}

	private List<Comment> prepareListOfComments() {
		ITreeContentProvider provider = (ITreeContentProvider) viewer.getContentProvider();
		List<Comment> comments = MiscUtil.buildArrayList();
		comments.addAll(getComments(viewer, provider, filter(viewer, viewer.getInput(),
				provider.getElements(viewer.getInput()))));
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
			comments.addAll(getComments(viewer, provider, filter(viewer, element, provider.getChildren(element))));
		}
		return comments;
	}

	/**
	 * Filter the children elements.
	 * 
	 * @param parentElementOrTreePath
	 *            the parent element or path
	 * @param elements
	 *            the child elements
	 * @return the filter list of children
	 */
	private Object[] filter(AbstractTreeViewer viewer, Object parentElementOrTreePath, Object[] elements) {
		List<ViewerFilter> filters;
		if (viewer.getFilters() != null) {
			filters = MiscUtil.buildArrayList(viewer.getFilters());
		} else {
			filters = MiscUtil.buildArrayList();
		}

		if (filters.size() > 0) {
			ArrayList<Object> filtered = new ArrayList<Object>(elements.length);
			for (Object element : elements) {
				boolean add = true;
				for (ViewerFilter filter : filters) {
					add = filter.select(viewer, parentElementOrTreePath, element);
					if (!add) {
						break;
					}
				}
				if (add) {
					filtered.add(element);
				}
			}
			return filtered.toArray();
		}
		return elements;
	}
}