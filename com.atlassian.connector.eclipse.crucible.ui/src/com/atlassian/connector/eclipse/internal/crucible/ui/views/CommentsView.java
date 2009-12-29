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
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.ActiveReviewManager.IReviewActivationListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.EditCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.PostDraftCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.RemoveCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.ReplyToCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.ToggleCommentsLeaveUnreadAction;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Comment.ReadState;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonFonts;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import java.text.DateFormat;
import java.util.List;

public class CommentsView extends ViewPart implements ISelectionListener, IReviewActivationListener {

	private static final String[] NO_COMMENT_SELECTED = new String[] { "No comment was selected in Crucible Review Explorer." };

	private static final String[] REVIEW_ITEM_HAS_NO_COMMENTS = new String[] { "Selected review item has no comments associated with itself." };

	private TreeViewer viewer;

	private EditCommentAction editCommentAction;

	private final List<Comment> comments = MiscUtil.buildArrayList();

	private ReplyToCommentAction replyToCommentAction;

	private RemoveCommentAction removeCommentAction;

	private PostDraftCommentAction postDraftCommentAction;

	private TreePath currentPath;

	private Review activeReview;

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
		CrucibleUiPlugin.getDefault().getActiveReviewManager().removeReviewActivationListener(this);
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent);
		viewer.setUseHashlookup(true);
		viewer.setContentProvider(new ReviewContentProvider());
		viewer.addSelectionChangedListener(new MarkCommentsReadSelectionListener());

		TreeViewerColumn commentColumn = new TreeViewerColumn(viewer, SWT.NONE);
		commentColumn.getColumn().setText("Comment");
		commentColumn.getColumn().setWidth(300);
		commentColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Font getFont(Object element) {
				if (element instanceof Comment) {
					if (((Comment) element).getReadState().equals(ReadState.UNREAD)
							|| ((Comment) element).getReadState().equals(ReadState.LEAVE_UNREAD)) {
						return CommonFonts.BOLD;
					}
				}
				return super.getFont(element);
			}

			@Override
			public String getText(Object element) {
				if (element instanceof Comment) {
					Comment comment = (Comment) element;
					String headerText = comment.getAuthor().getDisplayName() + "   ";
					headerText += DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(
							comment.getCreateDate());

					if (comment.isDefectApproved()) {
						headerText += " Approved Defect";
					} else if (comment.isDefectRaised()) {
						headerText += " Defect";
					}

					if (comment.isDraft()) {
						headerText += " Draft";
					}
					headerText += "\n\n";
					headerText += comment.getMessage();
					return headerText;
				}
				return super.getText(element);
			}
		});

		createActions();
		createToolbar();
		createMenu();
		createContextMenu();

		viewer.setInput(NO_COMMENT_SELECTED);

		getViewSite().getPage().addSelectionListener(this);
	}

	private void createContextMenu() {
		final MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});

		Menu menu = mgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		getSite().registerContextMenu(mgr, viewer);
	}

	private void fillContextMenu(IMenuManager mgr) {
		mgr.add(replyToCommentAction);
		mgr.add(editCommentAction);
		mgr.add(removeCommentAction);
		mgr.add(postDraftCommentAction);
		ToggleCommentsLeaveUnreadAction action = new ToggleCommentsLeaveUnreadAction();
		action.selectionChanged((IStructuredSelection) viewer.getSelection());
		mgr.add(action);
	}

	private void createToolbar() {
		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		mgr.add(replyToCommentAction);
		mgr.add(editCommentAction);
		mgr.add(removeCommentAction);
		mgr.add(postDraftCommentAction);
	}

	private void createMenu() {
		IMenuManager mgr = getViewSite().getActionBars().getMenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillMenu(manager);
			}
		});
		mgr.add(new Separator());
	}

	private void fillMenu(IMenuManager manager) {
		ToggleCommentsLeaveUnreadAction action = new ToggleCommentsLeaveUnreadAction();
		action.selectionChanged((IStructuredSelection) viewer.getSelection());
		manager.add(action);
	}

	private void createActions() {
		replyToCommentAction = new ReplyToCommentAction();
		viewer.addSelectionChangedListener(replyToCommentAction);

		editCommentAction = new EditCommentAction();
		viewer.addSelectionChangedListener(editCommentAction);

		removeCommentAction = new RemoveCommentAction();
		viewer.addSelectionChangedListener(removeCommentAction);

		postDraftCommentAction = new PostDraftCommentAction();
		viewer.addSelectionChangedListener(postDraftCommentAction);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/**
	 * Here we listen to changes in {@link ExplorerView}
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!(part instanceof ExplorerView)) {
			return;
		}

		currentPath = null;
		if (selection instanceof ITreeSelection) {
			TreePath[] paths = ((ITreeSelection) selection).getPaths();

			if (paths != null && paths.length == 1) {
				currentPath = paths[0];
				updateViewer();
				return;
			}
		}

		viewer.setInput(NO_COMMENT_SELECTED);
	}

	private void updateViewer() {
		if (currentPath == null) {
			return;
		}

		Object lastSegment = currentPath.getLastSegment();

		if (lastSegment instanceof Comment) {
			Comment activeComment = findActiveComment((Comment) lastSegment);

			comments.clear();
			if (activeComment != null) {
				comments.add(activeComment);
			}
			viewer.setInput(comments);
			viewer.expandAll();
			return;
		} else if (lastSegment instanceof CrucibleFileInfo) {
			CrucibleFileInfo activeFileInfo;
			try {
				activeFileInfo = activeReview.getFileByPermId(((CrucibleFileInfo) currentPath.getFirstSegment()).getPermId());
			} catch (ValueNotYetInitialized e) {
				activeFileInfo = null;
			}

			comments.clear();
			if (activeFileInfo != null) {
				comments.addAll(activeFileInfo.getVersionedComments());
			}

			if (comments.size() == 0) {
				viewer.setInput(REVIEW_ITEM_HAS_NO_COMMENTS);
			} else {
				viewer.setInput(comments);
				viewer.expandAll();
			}
			return;
		}
		viewer.setInput(NO_COMMENT_SELECTED);
	}

	private Comment findActiveComment(Comment comment) {
		CrucibleFileInfo activeFileInfo;
		try {
			activeFileInfo = activeReview.getFileByPermId(((CrucibleFileInfo) currentPath.getFirstSegment()).getPermId());
		} catch (ValueNotYetInitialized e) {
			return null;
		}

		return findComment(comment.getPermId(), activeFileInfo.getVersionedComments());
	}

	private Comment findComment(PermId commentId, List<? extends Comment> comments) {
		if (comments != null) {
			for (Comment comment : comments) {
				if (comment.getPermId().equals(commentId)) {
					return comment;
				}

				if (comment.getReplies() != null) {
					Comment found = findComment(commentId, comment.getReplies());
					if (found != null) {
						return found;
					}
				}
			}
		}
		return null;
	}

	public void reviewActivated(ITask task, Review review) {
		reviewUpdated(task, review);
	}

	public void reviewDeactivated(ITask task, Review review) {
		viewer.setInput(NO_COMMENT_SELECTED);
	}

	public void reviewUpdated(ITask task, Review review) {
		activeReview = review;
		updateViewer();
	}
}
