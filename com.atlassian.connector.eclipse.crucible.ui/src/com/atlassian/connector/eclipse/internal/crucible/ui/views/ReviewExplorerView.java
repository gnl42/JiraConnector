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

import com.atlassian.connector.eclipse.internal.crucible.ui.ActiveReviewManager;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.ActiveReviewManager.IReviewActivationListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.AddFileCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.AddGeneralCommentToActiveReviewAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.CommentNavigationAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.CompareVirtualFilesAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.EditActiveTaskAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.EditCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.OpenVirtualFileAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.PostDraftCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.RemoveCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.ReplyToCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.ToggleCommentsLeaveUnreadAction;
import com.atlassian.connector.eclipse.ui.viewers.CollapseAllAction;
import com.atlassian.connector.eclipse.ui.viewers.ExpandAllAction;
import com.atlassian.connector.eclipse.ui.viewers.ExpandCollapseSelectionAction;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Pawel Niewiadomski
 */
public class ReviewExplorerView extends ViewPart implements IReviewActivationListener {

	private OpenVirtualFileAction openOldAction;

	private OpenVirtualFileAction openNewAction;

	private CompareVirtualFilesAction compareAction;

	private TreeViewer viewer;

	private AddFileCommentAction addFileCommentAction;

	private Review initializeWith;

	private Review review;

	private ReplyToCommentAction replyToCommentAction;

	private EditCommentAction editCommentAction;

	private RemoveCommentAction removeCommentAction;

	private PostDraftCommentAction postDraftCommentAction;

	private ExpandAllAction expandAll;

	private CollapseAllAction collapseAll;

	private EditActiveTaskAction openEditorAction;

	private final Collection<IReviewActivationListener> reviewActivationListeners = MiscUtil.buildHashSet();

	private Action showCommentsViewAction;

	private IAction expandSelected;

	private IAction collapseSelected;

	private AddGeneralCommentToActiveReviewAction addGeneralCommentAction;

	private Action showUnreadOnlyAction;

	private static final String[] NO_ACTIVE_REVIEW = new String[] { "There's no active review.\n"
			+ "This view contents are rendered only if there's an active review." };

	protected static final int COMMENT_PREVIEW_LENGTH = 50;

	private static final int MAX_EXPANDED_BY_DEFAULT_ELEMENTS = 100;

	@Override
	public void createPartControl(Composite parent) {

		viewer = new TreeViewer(parent);
		viewer.setUseHashlookup(true);
		viewer.setComparer(new IElementComparer() {
			public int hashCode(Object element) {
				// compare comments only by their perm id
				// this is needed to preserve selection after
				// comment was modified
				if (element instanceof Comment) {
					return ((Comment) element).getPermId().hashCode();
				}
				return element.hashCode();
			}

			public boolean equals(Object a, Object b) {
				if (a instanceof Comment && b instanceof Comment) {
					return ((Comment) a).getPermId().equals(((Comment) b).getPermId());
				}
				if (a instanceof ReviewTreeNode && b instanceof ReviewTreeNode) {
					ReviewTreeNode a1 = (ReviewTreeNode) a;
					ReviewTreeNode b1 = (ReviewTreeNode) b;
					if (a1.getCrucibleFileInfo() != null && b1.getCrucibleFileInfo() != null) {
						return a1.getCrucibleFileInfo().getPermId().equals(b1.getCrucibleFileInfo().getPermId());
					}
				}
				return a.equals(b);
			}
		});
		viewer.addSelectionChangedListener(new MarkCommentsReadSelectionListener());
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (compareAction.isEnabled()) {
					compareAction.run();
				} else if (openNewAction.isEnabled()) {
					openNewAction.run();
				} else if (openOldAction.isEnabled()) {
					openOldAction.run();
				} else {
					if (event.getSelection() instanceof IStructuredSelection) {
						final IStructuredSelection structuredSelection = ((IStructuredSelection) event.getSelection());
						if (structuredSelection.size() != 1) {
							return;
						}
						final Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
						if (viewer.getExpandedState(element)) {
							viewer.collapseToLevel(element, AbstractTreeViewer.ALL_LEVELS);
						} else {
							viewer.expandToLevel(element, AbstractTreeViewer.ALL_LEVELS);

						}
					}
				}
			}
		});

		viewer.setContentProvider(new ReviewContentProvider());
		final DecoratingStyledCellLabelProvider styledLabelProvider = new DecoratingStyledCellLabelProvider(
				new ReviewExplorerLabelProvider(), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator(),
				null);
		viewer.setLabelProvider(styledLabelProvider);
		viewer.setComparator(new ViewerComparator() {

			@Override
			public int compare(Viewer aViewer, Object e1, Object e2) {
				boolean isE1Dir = e1 instanceof ReviewTreeNode;
				boolean isE2Dir = e2 instanceof ReviewTreeNode;
				if (isE1Dir && isE2Dir) {
					return super.compare(aViewer, e1, e2);
				} else if (!isE1Dir && !isE2Dir) {
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

		});

		createActions();
		createToolbar();
		createMenu();
		createContextMenu();

		getSite().setSelectionProvider(viewer);
		setReview(initializeWith);
	}

	private void createMenu() {
		IMenuManager mgr = getViewSite().getActionBars().getMenuManager();
		mgr.add(showUnreadOnlyAction);
	}

	private ReviewTreeNode[] reviewToTreeNodes(final Review newReview) {
		ReviewTreeNode[] nodes = new ReviewTreeNode[] { new ReviewTreeNode(null, "General Comments") {
			@Override
			public List<? extends Object> getChildren() {
				try {
					return newReview.getGeneralComments();
				} catch (ValueNotYetInitialized e) {
					return MiscUtil.buildArrayList();
				}
			}
		}, new ReviewTreeNode(null, "Files") {
			public java.util.List<? extends Object> getChildren() {
				return Arrays.asList(compactTree(newReview));
			};
		} };
		return nodes;
	}

	private void setReview(Review newReview) {
		if (newReview != null) {
			final ReviewTreeNode[] newInput = reviewToTreeNodes(newReview);
			final Object[] previouslyExpandedElements = viewer.getExpandedElements();
			viewer.setInput(newInput);
			if (review == null || !review.equals(newReview)) {
				final ArrayList<Object> expandedElements = MiscUtil.<Object> buildArrayList();
				fillExpandedElements(expandedElements, newInput[0]);
				viewer.setExpandedElements(expandedElements.subList(0,
						Math.min(expandedElements.size(), MAX_EXPANDED_BY_DEFAULT_ELEMENTS)).toArray());
			} else {
				viewer.setExpandedElements(previouslyExpandedElements);
			}
		} else {
			viewer.setInput(NO_ACTIVE_REVIEW);
		}
		review = newReview;
		if (newReview == null) {
			setContentDescription("");
		} else {
			try {
				setContentDescription(NLS.bind("Review files for {0} ({1} files, {2} comments)", new Object[] {
						newReview.getPermId().getId(), newReview.getFiles().size(),
						newReview.getNumberOfVersionedComments() }));
			} catch (ValueNotYetInitialized e) {
				// nothing here
			}

		}

	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);

		final ActiveReviewManager mgr = CrucibleUiPlugin.getDefault().getActiveReviewManager();
		if (mgr.isReviewActive()) {
			reviewActivated(mgr.getActiveTask(), mgr.getActiveReview());
		}
		mgr.addReviewActivationListener(this);
	}

	@Override
	public void dispose() {
		ActiveReviewManager mgr = CrucibleUiPlugin.getDefault().getActiveReviewManager();

		for (IReviewActivationListener listener : reviewActivationListeners) {
			mgr.removeReviewActivationListener(listener);
		}

		mgr.removeReviewActivationListener(this);
		super.dispose();
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void createActions() {
		showUnreadOnlyAction = new Action("Show unread comments only", IAction.AS_CHECK_BOX) {
			private final ViewerFilter filter = new UnreadCommentsViewerFilter();

			{
				setImageDescriptor(CommonImages.FILTER_COMPLETE);
			}

			public void run() {
				if (isChecked()) {
					viewer.addFilter(filter);
					viewer.expandAll();
				} else {
					viewer.removeFilter(filter);
				}
			};
		};

		addFileCommentAction = new AddFileCommentAction("Add File Comment", "Add File Comment");
		viewer.addSelectionChangedListener(addFileCommentAction);

		openOldAction = new OpenVirtualFileAction(true);
		viewer.addSelectionChangedListener(openOldAction);

		openNewAction = new OpenVirtualFileAction(false);
		viewer.addSelectionChangedListener(openNewAction);

		compareAction = new CompareVirtualFilesAction();
		viewer.addSelectionChangedListener(compareAction);

		replyToCommentAction = new ReplyToCommentAction();
		viewer.addSelectionChangedListener(replyToCommentAction);

		editCommentAction = new EditCommentAction();
		viewer.addSelectionChangedListener(editCommentAction);

		removeCommentAction = new RemoveCommentAction();
		viewer.addSelectionChangedListener(removeCommentAction);

		postDraftCommentAction = new PostDraftCommentAction();
		viewer.addSelectionChangedListener(postDraftCommentAction);

		expandAll = new ExpandAllAction(viewer);
		collapseAll = new CollapseAllAction(viewer);

		expandSelected = new ExpandCollapseSelectionAction(viewer, true);
		collapseSelected = new ExpandCollapseSelectionAction(viewer, false);

		showCommentsViewAction = new Action() {
			{
				setText("Show Comment View");
				setToolTipText("Show Comment View");
				setImageDescriptor(CrucibleImages.COMMENT_SMALL);
			}

			public void run() {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
							CrucibleUiPlugin.COMMENT_VIEW_ID);
				} catch (PartInitException e) {
					// don't care
				}
			};
		};

		addGeneralCommentAction = new AddGeneralCommentToActiveReviewAction();
		reviewActivationListeners.add(addGeneralCommentAction);

		openEditorAction = new EditActiveTaskAction();
		reviewActivationListeners.add(openEditorAction);

		// in the end register all aditional activation listeners
		final ActiveReviewManager mgr = CrucibleUiPlugin.getDefault().getActiveReviewManager();
		for (IReviewActivationListener listener : reviewActivationListeners) {
			if (mgr.isReviewActive()) {
				listener.reviewActivated(mgr.getActiveTask(), mgr.getActiveReview());
			}
			mgr.addReviewActivationListener(listener);
		}
	}

	public void createToolbar() {
		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		final CommentNavigationAction prevCommentAction = new CommentNavigationAction(viewer, getViewSite(), false);
		final CommentNavigationAction nextCommentAction = new CommentNavigationAction(viewer, getViewSite(), true);

		mgr.add(expandAll);
		mgr.add(collapseAll);
		mgr.add(showUnreadOnlyAction);
		mgr.add(new Separator());
		mgr.add(prevCommentAction);
		mgr.add(nextCommentAction);
		mgr.add(new Separator());
		mgr.add(openEditorAction);
		mgr.add(showCommentsViewAction);
		mgr.add(addGeneralCommentAction);
		mgr.add(new Separator());
		mgr.add(addFileCommentAction);
		mgr.add(openOldAction);
		mgr.add(openNewAction);
		mgr.add(compareAction);
	}

	private void createContextMenu() {
		final MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(mgr);
			}
		});

		Menu menu = mgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		getSite().registerContextMenu(mgr, viewer);
	}

	private void fillContextMenu(MenuManager mgr) {
		mgr.add(expandSelected);
		mgr.add(collapseSelected);
		mgr.add(new Separator());
		mgr.add(addFileCommentAction);
		mgr.add(replyToCommentAction);
		mgr.add(editCommentAction);
		mgr.add(removeCommentAction);
		mgr.add(postDraftCommentAction);
		ToggleCommentsLeaveUnreadAction action = new ToggleCommentsLeaveUnreadAction();
		action.selectionChanged((IStructuredSelection) viewer.getSelection());
		mgr.add(action);
		mgr.add(new Separator());
		mgr.add(openOldAction);
		mgr.add(openNewAction);
		mgr.add(compareAction);
	}

	private void fillExpandedElements(ArrayList<Object> expandedElements, ReviewTreeNode root) {
		expandedElements.add(root);
		for (Object treeNode : root.getChildren()) {
			if (treeNode instanceof ReviewTreeNode) {
				fillExpandedElements(expandedElements, (ReviewTreeNode) treeNode);
			}
		}
	}

	public void reviewActivated(ITask task, final Review newReview) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (viewer != null) {
					setReview(newReview);
				} else {
					initializeWith = newReview;
				}
			}
		});
	}

	public static ReviewTreeNode[] compactTree(Review review) {
		ReviewTreeNode root = new ReviewTreeNode(null, null);
		try {
			for (CrucibleFileInfo cfi : review.getFiles()) {
				String path = cfi.getFileDescriptor().getUrl();
				final String[] pathTokens = path.split("/|\\\\");
				root.add(pathTokens, cfi);
			}
		} catch (ValueNotYetInitialized e) {
			// this is stupid exception... OMG
		}
		root.compact();
		return new ReviewTreeNode[] { root };
	}

	public void reviewDeactivated(ITask task, Review aReview) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				setReview(null);
			}
		});
	}

	public void reviewUpdated(ITask task, Review aReview) {
		reviewActivated(task, aReview);
	}
}
