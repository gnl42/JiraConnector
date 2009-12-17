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

package com.atlassian.connector.eclipse.crucible.ui.views;

import com.atlassian.connector.eclipse.internal.crucible.ui.ActiveReviewManager;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.ActiveReviewManager.IReviewActivationListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.AddGeneralCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.CompareUploadedVirtualFileAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.CompareVersionedVirtualFileAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.OpenUploadedVirtualFileAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.OpenVersionedVirtualFileAction;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CommitType;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.FileType;
import com.atlassian.theplugin.commons.crucible.api.model.RepositoryType;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import java.text.DateFormat;

/**
 * @author Pawel Niewiadomski
 */
public class ExplorerView extends ViewPart implements IReviewActivationListener {

	private Action openOldAction;

	private Action openNewAction;

	private Action compareAction;

	private TreeViewer viewer;

	private Review review;

	private ITask task;

	private AddGeneralCommentAction addGeneralCommentAction;

	private Object initilizeWith = NO_ACTIVE_REVIEW;

	private static final String[] NO_ACTIVE_REVIEW = new String[] { "There's no active review. This view contents are rendered only if there's an active review." };

	/**
	 * 
	 */
	public ExplorerView() {
		// ignore
	}

	@Override
	public void createPartControl(Composite parent) {

		viewer = new TreeViewer(parent);
		viewer.setContentProvider(new ReviewContentProvider());
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Comment) {
					Comment comment = (Comment) element;
					String headerText = comment.getAuthor().getDisplayName() + "   ";
					headerText += DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(
							comment.getCreateDate());
					return headerText;
				}
				if (element instanceof CrucibleFileInfo) {
					CrucibleFileInfo file = ((CrucibleFileInfo) element);
					String text = file.getFileDescriptor().getUrl();

					text += " [";

					VersionedVirtualFile oldFileDescriptor = file.getOldFileDescriptor();
					VersionedVirtualFile newFileDescriptor = file.getFileDescriptor();

					boolean oldFileHasRevision = oldFileDescriptor != null && oldFileDescriptor.getRevision() != null
							&& oldFileDescriptor.getRevision().length() > 0;
					boolean oldFileHasUrl = oldFileDescriptor != null && oldFileDescriptor.getUrl() != null
							&& oldFileDescriptor.getUrl().length() > 0;

					boolean newFileHasRevision = newFileDescriptor != null && newFileDescriptor.getRevision() != null
							&& newFileDescriptor.getRevision().length() > 0;
					boolean newFileHasUrl = newFileDescriptor != null && newFileDescriptor.getUrl() != null
							&& newFileDescriptor.getUrl().length() > 0;

					FileType filetype = file.getFileType();

					//if repository type is uploaded or patch, display alternative for now since we cannot open the file yet
					if (file.getRepositoryType() == RepositoryType.PATCH) {
						text += "Part of a Patch";
					} else if (file.getRepositoryType() == RepositoryType.UPLOAD) {
						text += "Pre-commit";
					} else {
						//if file is deleted or not a file, do not include any revisions 
						//   (we need a local resource to retrieve the old revision from SVN, which we do not have)
						if (file.getCommitType() == CommitType.Deleted || filetype != FileType.File) {
							text += "Rev: N/A ";
						} else {
							if (oldFileHasUrl && oldFileHasRevision) {
								text += "Rev: " + oldFileDescriptor.getRevision();
							}
							if (oldFileHasRevision) {
								if (newFileHasRevision) {
									text += "-";
								}
							}

							if (newFileHasUrl && newFileHasRevision && file.getCommitType() != CommitType.Deleted) {
								text += newFileDescriptor.getRevision();
							}
						}
					}
					text += "]";
					return text;
				}
				return super.getText(element);
			}
		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				openNewAction.setEnabled(false);
				openOldAction.setEnabled(false);
				compareAction.setEnabled(false);

				if (event.getSelection() instanceof ITreeSelection) {
					TreePath[] paths = ((ITreeSelection) event.getSelection()).getPaths();

					if (paths.length == 1 && paths[0].getFirstSegment() instanceof CrucibleFileInfo) {
						// one item
						CrucibleFileInfo crucibleFileInfo = (CrucibleFileInfo) paths[0].getFirstSegment();

						VersionedVirtualFile oldFileDescriptor = crucibleFileInfo.getOldFileDescriptor();
						VersionedVirtualFile newFileDescriptor = crucibleFileInfo.getFileDescriptor();

						boolean oldFileHasRevision = oldFileDescriptor != null
								&& oldFileDescriptor.getRevision() != null
								&& oldFileDescriptor.getRevision().length() > 0;
						boolean oldFileHasUrl = oldFileDescriptor != null && oldFileDescriptor.getUrl() != null
								&& oldFileDescriptor.getUrl().length() > 0;

						boolean newFileHasRevision = newFileDescriptor != null
								&& newFileDescriptor.getRevision() != null
								&& newFileDescriptor.getRevision().length() > 0;
						boolean newFileHasUrl = newFileDescriptor != null && newFileDescriptor.getUrl() != null
								&& newFileDescriptor.getUrl().length() > 0;

						if (crucibleFileInfo.getRepositoryType() == RepositoryType.UPLOAD
								|| crucibleFileInfo.getRepositoryType() == RepositoryType.SCM) {
							openOldAction.setEnabled(oldFileHasRevision && oldFileHasUrl);
							openNewAction.setEnabled(newFileHasRevision && newFileHasUrl);

							if (crucibleFileInfo.getFileType() == FileType.File) {
								compareAction.setEnabled(oldFileHasRevision && newFileHasRevision);
							}
						}
					}
				}
			}
		});

		getSite().setSelectionProvider(viewer);

		createActions();
		createToolbar();
		createMenu();

		viewer.setInput(initilizeWith);
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);

		ActiveReviewManager mgr = CrucibleUiPlugin.getDefault().getActiveReviewManager();

		mgr.addReviewActivationListener(this);

		if (mgr.isReviewActive()) {
			reviewActivated(mgr.getActiveTask(), mgr.getActiveReview());
		}
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

		openOldAction = new Action() {
			@Override
			public void run() {
				ITreeSelection selection = (ITreeSelection) viewer.getSelection();
				TreePath[] paths = selection.getPaths();
				if (paths == null || paths.length == 0) {
					return;
				}

				CrucibleFileInfo fileInfo = (CrucibleFileInfo) paths[0].getFirstSegment();
				VersionedComment comment = null;
				if (paths[0].getLastSegment() instanceof VersionedComment) {
					comment = (VersionedComment) paths[0].getLastSegment();
				}

				switch (fileInfo.getRepositoryType()) {
				case UPLOAD:
					OpenUploadedVirtualFileAction action = new OpenUploadedVirtualFileAction(task, new CrucibleFile(
							fileInfo, true), fileInfo.getOldFileDescriptor(), review, comment, viewer.getControl()
							.getShell(), getViewSite().getWorkbenchWindow().getActivePage());
					action.run();
					break;
				case SCM:
					OpenVersionedVirtualFileAction action1 = new OpenVersionedVirtualFileAction(task, new CrucibleFile(
							fileInfo, true), comment, review);
					action1.run();
					break;
				default:
					StatusHandler.log(new Status(IStatus.WARNING, CrucibleUiPlugin.PLUGIN_ID, "Invalid Repository Type"));
				}
			}
		};
		openOldAction.setText("old");
		openOldAction.setToolTipText("Open Base Version");
		openOldAction.setEnabled(false);

		openNewAction = new Action() {
			@Override
			public void run() {
				ITreeSelection selection = (ITreeSelection) viewer.getSelection();
				TreePath[] paths = selection.getPaths();
				if (paths == null || paths.length == 0) {
					return;
				}

				CrucibleFileInfo fileInfo = (CrucibleFileInfo) paths[0].getFirstSegment();
				VersionedComment comment = null;
				if (paths[0].getLastSegment() instanceof VersionedComment) {
					comment = (VersionedComment) paths[0].getLastSegment();
				}

				switch (fileInfo.getRepositoryType()) {
				case UPLOAD:
					OpenUploadedVirtualFileAction action = new OpenUploadedVirtualFileAction(task, new CrucibleFile(
							fileInfo, false), fileInfo.getFileDescriptor(), review, comment, viewer.getControl()
							.getShell(), getViewSite().getWorkbenchWindow().getActivePage());
					action.run();
					break;
				case SCM:
					OpenVersionedVirtualFileAction action1 = new OpenVersionedVirtualFileAction(task, new CrucibleFile(
							fileInfo, false), comment, review);
					action1.run();
					break;
				default:
					StatusHandler.log(new Status(IStatus.WARNING, CrucibleUiPlugin.PLUGIN_ID, "Invalid Repository Type"));
				}
			}
		};
		openNewAction.setText("new");
		openNewAction.setToolTipText("Open New Version");
		openNewAction.setEnabled(false);

		compareAction = new Action() {
			@Override
			public void run() {
				ITreeSelection selection = (ITreeSelection) viewer.getSelection();
				TreePath[] paths = selection.getPaths();
				if (paths == null || paths.length == 0) {
					return;
				}

				CrucibleFileInfo fileInfo = (CrucibleFileInfo) paths[0].getFirstSegment();
				VersionedComment comment = null;
				if (paths[0].getLastSegment() instanceof VersionedComment) {
					comment = (VersionedComment) paths[0].getLastSegment();
				}

				IAction action;
				if (fileInfo.getRepositoryType() == RepositoryType.SCM) {
					action = new CompareVersionedVirtualFileAction(fileInfo, comment, review);
				} else {
					action = new CompareUploadedVirtualFileAction(fileInfo, comment, review, viewer.getControl()
							.getShell());
				}
				action.run();
			}
		};
		compareAction.setText("compare");
		compareAction.setToolTipText("Open Compare Editor");
		compareAction.setEnabled(false);
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

	public void reviewActivated(ITask task, Review review) {
		this.review = review;
		this.task = task;
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
			setContentDescription(null);
			viewer.setInput(NO_ACTIVE_REVIEW);
		}
	}

	public void reviewDeactivated(ITask task, Review review) {
		this.review = null;
		this.task = null;
		setContentDescription(null);
		viewer.setInput(NO_ACTIVE_REVIEW);
	}

	public void reviewUpdated(ITask task, Review review) {
		reviewActivated(task, review);
	}
}
