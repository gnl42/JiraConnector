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

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.EditCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.PostDraftCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.RemoveCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.ReplyToCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.MarkCommentsAsReadJob;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Comment.ReadState;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonFonts;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CommentsView extends ViewPart implements ISelectionListener {

	private static final String[] NO_COMMENT_SELECTED = new String[] { "No comment was selected in Crucible Review Explorer." };

	private static final String[] REVIEW_ITEM_HAS_NO_COMMENTS = new String[] { "Selected review item has no comments associated with itself." };

	private TreeViewer viewer;

	private EditCommentAction editCommentAction;

	private final List<Comment> comments = MiscUtil.buildArrayList();

	private ReplyToCommentAction replyToCommentAction;

	private RemoveCommentAction removeCommentAction;

	private PostDraftCommentAction postDraftCommentAction;

	public CommentsView() {
		// ignore
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent);
		viewer.setUseHashlookup(true);
		viewer.setContentProvider(new ReviewContentProvider());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Iterator<?> it = selection.iterator();
				final Collection<Comment> markAsRead = MiscUtil.buildArrayList();

				while (it.hasNext()) {
					Object element = it.next();
					if (element instanceof Comment) {
						if (((Comment) element).getReadState().equals(ReadState.UNREAD)) {
							markAsRead.add((Comment) element);
						}
					}
				}

				if (markAsRead.size() > 0) {
					Review activeReview = CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
					MarkCommentsAsReadJob job = new MarkCommentsAsReadJob(activeReview, markAsRead);
					job.addJobChangeListener(new JobChangeAdapter() {
						@Override
						public void done(IJobChangeEvent event) {
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									viewer.update(markAsRead.toArray(), null);
								}
							});
						}
					});
					job.schedule();
				}
			}
		});

		TreeViewerColumn commentColumn = new TreeViewerColumn(viewer, SWT.NONE);
		commentColumn.getColumn().setText("Comment");
		commentColumn.getColumn().setWidth(300);
		commentColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Font getFont(Object element) {
				if (element instanceof Comment) {
					if (((Comment) element).getReadState().equals(ReadState.UNREAD)) {
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
		createContextMenu();

		viewer.setInput(NO_COMMENT_SELECTED);

		getViewSite().getPage().addSelectionListener(this);
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
		mgr.add(replyToCommentAction);
		mgr.add(editCommentAction);
		mgr.add(removeCommentAction);
		mgr.add(postDraftCommentAction);
	}

	private void createToolbar() {
		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		mgr.add(replyToCommentAction);
		mgr.add(editCommentAction);
		mgr.add(removeCommentAction);
		mgr.add(postDraftCommentAction);
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

		viewer.setInput(NO_COMMENT_SELECTED);

		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof Comment) {
				comments.clear();
				comments.add((Comment) element);
				viewer.setInput(comments);
			}
			if (element instanceof CrucibleFileInfo) {
				comments.clear();
				comments.addAll(((CrucibleFileInfo) element).getVersionedComments());
				if (comments.size() == 0) {
					viewer.setInput(REVIEW_ITEM_HAS_NO_COMMENTS);
				} else {
					viewer.setInput(comments);
				}
			}
		}
	}
}
