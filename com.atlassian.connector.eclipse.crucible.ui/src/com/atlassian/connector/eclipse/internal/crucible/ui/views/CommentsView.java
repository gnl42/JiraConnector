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

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import java.text.DateFormat;
import java.util.List;

public class CommentsView extends ViewPart implements ISelectionListener, IReviewActivationListener {

	private static final String NO_COMMENT_SELECTED = "No comment was selected in Crucible Review Explorer.";

	private static final String EMPTY = "";

	private EditCommentAction editCommentAction;

	private ReplyToCommentAction replyToCommentAction;

	private RemoveCommentAction removeCommentAction;

	private PostDraftCommentAction postDraftCommentAction;

	private TreePath currentPath;

	private Review activeReview;

	private Text message;

	private Text author;

	private Text date;

	private Text defect;

	private FormToolkit toolkit;

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

		if (toolkit != null) {
			toolkit.dispose();
			toolkit = null;
		}

		super.dispose();
	}

	private Label createLabelControl(FormToolkit toolkit, Composite parent, String labelString) {
		Label labelControl = toolkit.createLabel(parent, labelString);
		labelControl.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		return labelControl;
	}

	@Override
	public void createPartControl(Composite ancestor) {
		if (toolkit == null) {
			toolkit = new FormToolkit(ancestor.getDisplay());
		}

		Composite parent = toolkit.createComposite(ancestor);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(15, 15).applyTo(parent);

		// Author | Date | Defect
		// Comment text here

		Composite header = toolkit.createComposite(parent);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(header);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(header);

		Composite composite = toolkit.createComposite(header);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);

		createLabelControl(toolkit, composite, "Author:");
		author = toolkit.createText(composite, EMPTY, SWT.READ_ONLY | SWT.SINGLE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(author);

		composite = toolkit.createComposite(header);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);

		createLabelControl(toolkit, composite, "Created:");
		date = toolkit.createText(composite, "", SWT.READ_ONLY | SWT.SINGLE);
		GridDataFactory.fillDefaults().hint(150, SWT.DEFAULT).applyTo(date);

		defect = toolkit.createText(header, "", SWT.READ_ONLY | SWT.SINGLE);
		GridDataFactory.fillDefaults().hint(150, SWT.DEFAULT).applyTo(defect);

		message = toolkit.createText(parent, "", SWT.READ_ONLY | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(message);

		createActions();
		createToolbar();
		createMenu();

		message.setText(NO_COMMENT_SELECTED);

		getViewSite().getPage().addSelectionListener(this);

		if (currentPath != null) {
			updateViewer();
		}
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
		action.selectionChanged(currentPath != null ? new StructuredSelection(currentPath.getLastSegment())
				: StructuredSelection.EMPTY);
		manager.add(action);
	}

	private void createActions() {
		replyToCommentAction = new ReplyToCommentAction();
		editCommentAction = new EditCommentAction();
		removeCommentAction = new RemoveCommentAction();
		postDraftCommentAction = new PostDraftCommentAction();

		getViewSite().getPage().addSelectionListener(new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				if (!(part instanceof ExplorerView)) {
					return;
				}

				if (selection instanceof IStructuredSelection) {
					replyToCommentAction.selectionChanged((IStructuredSelection) selection);
					editCommentAction.selectionChanged((IStructuredSelection) selection);
					removeCommentAction.selectionChanged((IStructuredSelection) selection);
					postDraftCommentAction.selectionChanged((IStructuredSelection) selection);
				} else {
					replyToCommentAction.selectionChanged(StructuredSelection.EMPTY);
					editCommentAction.selectionChanged(StructuredSelection.EMPTY);
					removeCommentAction.selectionChanged(StructuredSelection.EMPTY);
					postDraftCommentAction.selectionChanged(StructuredSelection.EMPTY);
				}
			}
		});
	}

	@Override
	public void setFocus() {
		message.setFocus();
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
			}
		}

		updateViewer();
	}

	private void updateViewer() {
		if (currentPath != null) {
			Object lastSegment = currentPath.getLastSegment();

			if (lastSegment instanceof Comment) {
				Comment activeComment = findActiveComment((Comment) lastSegment);

				if (activeComment != null) {
					author.setText(activeComment.getAuthor().getDisplayName());
					author.setToolTipText(activeComment.getAuthor().getUsername());
					date.setText(DateFormat.getDateInstance().format(activeComment.getCreateDate()));
					defect.setText(activeComment.isDefectRaised() ? "Defect"
							: (activeComment.isDefectApproved() ? "Defect Approved" : ""));
					message.setText(activeComment.getMessage());
					return;
				}
			}
		}

		message.setText(NO_COMMENT_SELECTED);
		author.setText(EMPTY);
		author.setToolTipText(EMPTY);
		date.setText(EMPTY);
		defect.setText(EMPTY);
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
		message.setText(NO_COMMENT_SELECTED);
	}

	public void reviewUpdated(ITask task, Review review) {
		activeReview = review;
		if (message != null) {
			updateViewer();
		}
	}
}
