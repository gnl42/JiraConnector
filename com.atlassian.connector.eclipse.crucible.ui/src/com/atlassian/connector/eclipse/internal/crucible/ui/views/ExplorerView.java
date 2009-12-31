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
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.AddGeneralCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.CompareVirtualFilesAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.EditCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.OpenVirtualFileAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.PostDraftCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.RemoveCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.ReplyToCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.ToggleCommentsLeaveUnreadAction;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CommitType;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.FileType;
import com.atlassian.theplugin.commons.crucible.api.model.RepositoryType;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Comment.ReadState;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonFonts;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import java.text.DateFormat;

/**
 * @author Pawel Niewiadomski
 */
public class ExplorerView extends ViewPart implements IReviewActivationListener {

	private OpenVirtualFileAction openOldAction;

	private OpenVirtualFileAction openNewAction;

	private CompareVirtualFilesAction compareAction;

	private TreeViewer viewer;

	private AddGeneralCommentAction addGeneralCommentAction;

	private Object initilizeWith = NO_ACTIVE_REVIEW;

	private ReplyToCommentAction replyToCommentAction;

	private EditCommentAction editCommentAction;

	private RemoveCommentAction removeCommentAction;

	private PostDraftCommentAction postDraftCommentAction;

	private static final String[] NO_ACTIVE_REVIEW = new String[] { "There's no active review. This view contents are rendered only if there's an active review." };

	protected static final int COMMENT_PREVIEW_LENGTH = 50;

	/**
	 * 
	 */
	public ExplorerView() {
		// ignore
	}

	/**
	 * A simple label provider
	 */
	private static class CrucibleFileInfoLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {

		public String getText(Object element) {
			return getStyledText(element).toString();
		}

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

		public StyledString getStyledText(Object element) {
			if (element instanceof Comment) {
				Comment comment = (Comment) element;
				String headerText = comment.getAuthor().getDisplayName() + "   ";
				headerText += DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(
						comment.getCreateDate());

				String msg = comment.getMessage();
				if (msg.length() > COMMENT_PREVIEW_LENGTH) {
					headerText += " " + msg.substring(0, COMMENT_PREVIEW_LENGTH) + "...";
				} else {
					headerText += " " + msg;
				}
				return new StyledString(headerText);
			}
			if (element instanceof CrucibleFileInfo) {
				CrucibleFileInfo file = (CrucibleFileInfo) element;
				StyledString styledString = new StyledString();
				styledString.append(file.getFileDescriptor().getUrl());
				StringBuilder revisionString = getRevisionInfo(file);

				if (revisionString.length() > 0) {
					styledString.append(" ");
					styledString.append(revisionString.toString(), StyledString.DECORATIONS_STYLER);
				}
				return styledString;
			}

			return element == null ? new StyledString("") : new StyledString(element.toString());//$NON-NLS-1$
		}

		private StringBuilder getRevisionInfo(CrucibleFileInfo file) {

			final VersionedVirtualFile oldFileDescriptor = file.getOldFileDescriptor();
			final VersionedVirtualFile newFileDescriptor = file.getFileDescriptor();

			boolean oldFileHasRevision = oldFileDescriptor != null && oldFileDescriptor.getRevision() != null
					&& oldFileDescriptor.getRevision().length() > 0;
			boolean oldFileHasUrl = oldFileDescriptor != null && oldFileDescriptor.getUrl() != null
					&& oldFileDescriptor.getUrl().length() > 0;

			boolean newFileHasRevision = newFileDescriptor != null && newFileDescriptor.getRevision() != null
					&& newFileDescriptor.getRevision().length() > 0;
			boolean newFileHasUrl = newFileDescriptor != null && newFileDescriptor.getUrl() != null
					&& newFileDescriptor.getUrl().length() > 0;

			FileType filetype = file.getFileType();

			StringBuilder revisionString = new StringBuilder();

			//if repository type is uploaded or patch, display alternative for now since we cannot open the file yet
			if (file.getRepositoryType() == RepositoryType.PATCH) {
				revisionString.append("Part of a Patch");
			} else if (file.getRepositoryType() == RepositoryType.UPLOAD) {
				revisionString.append("Pre-commit");
			} else {
				//if file is deleted or not a file, do not include any revisions 
				//   (we need a local resource to retrieve the old revision from SVN, which we do not have)
				if (file.getCommitType() == CommitType.Deleted || filetype != FileType.File) {
					revisionString.append("N/A ");
				} else {
					if (oldFileHasUrl && oldFileHasRevision) {
						revisionString.append(oldFileDescriptor.getRevision());
					}
					if (oldFileHasRevision) {
						if (newFileHasRevision) {
							revisionString.append("-");
						}
					}

					if (newFileHasUrl && newFileHasRevision && file.getCommitType() != CommitType.Deleted) {
						revisionString.append(newFileDescriptor.getRevision());
					}
				}
			}
			return revisionString;
		}
	}

	@Override
	public void createPartControl(Composite parent) {

		viewer = new TreeViewer(parent);
		viewer.setUseHashlookup(true);
		viewer.addSelectionChangedListener(new MarkCommentsReadSelectionListener());

		viewer.setContentProvider(new ReviewContentProvider());
		final DelegatingStyledCellLabelProvider styledLabelProvider = new DelegatingStyledCellLabelProvider(
				new CrucibleFileInfoLabelProvider());
		viewer.setLabelProvider(styledLabelProvider);

		createActions();
		createToolbar();
		createMenu();
		createContextMenu();

		getSite().setSelectionProvider(viewer);
		viewer.setInput(initilizeWith);
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
		CrucibleUiPlugin.getDefault().getActiveReviewManager().removeReviewActivationListener(this);
		super.dispose();
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void createActions() {
		addGeneralCommentAction = new AddGeneralCommentAction();
		viewer.addSelectionChangedListener(addGeneralCommentAction);

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
	}

	public void createToolbar() {
		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		mgr.add(addGeneralCommentAction);
		mgr.add(openOldAction);
		mgr.add(openNewAction);
		mgr.add(compareAction);
	}

	private void createMenu() {
		//IMenuManager mgr = getViewSite().getActionBars().getMenuManager();
		//mgr.add(selectAllAction);
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
		mgr.add(addGeneralCommentAction);
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

	public void reviewActivated(ITask task, Review review) {
		if (review != null) {
			try {
				setContentDescription(NLS.bind("Review files for {0} ({1} files, {2} comments)", new Object[] {
						review.getPermId().getId(), review.getFiles().size(), review.getNumberOfVersionedComments() }));
			} catch (ValueNotYetInitialized e) {
				// nothing here
			}

			try {
				if (viewer != null) {
					viewer.setInput(review.getFiles());
				} else {
					initilizeWith = review.getFiles();
				}
			} catch (ValueNotYetInitialized e) {
				StatusHandler.log(new Status(IStatus.WARNING, CrucibleUiPlugin.PLUGIN_ID, "Review not initialized", e));
			}
		} else {
			setContentDescription("");
			viewer.setInput(NO_ACTIVE_REVIEW);
		}
	}

	public void reviewDeactivated(ITask task, Review review) {
		setContentDescription("");
		viewer.setInput(NO_ACTIVE_REVIEW);
	}

	public void reviewUpdated(ITask task, Review review) {
		reviewActivated(task, review);
	}
}
