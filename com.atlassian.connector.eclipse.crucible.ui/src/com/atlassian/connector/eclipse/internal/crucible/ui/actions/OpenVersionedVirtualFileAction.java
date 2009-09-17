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

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.annotations.CrucibleAnnotationModelManager;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.connector.eclipse.ui.team.TeamUiUtils;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonUiUtil;
import org.eclipse.mylyn.internal.provisional.commons.ui.ICoreRunnable;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Action to open a version file
 * 
 * @author Shawn Minto
 */
public class OpenVersionedVirtualFileAction extends Action {

	private final class OpenVersionedVirtualFileRunnable implements ICoreRunnable {
		public void run(IProgressMonitor monitor) throws CoreException {
			VersionedVirtualFile newFile = crucibleFile.getCrucibleFileInfo().getFileDescriptor();
			VersionedVirtualFile oldFile = crucibleFile.getCrucibleFileInfo().getOldFileDescriptor();
			IEditorPart editor = null;
			if (crucibleFile.isOldFile()) {
				editor = TeamUiUtils.openFile(oldFile.getRepoUrl(), oldFile.getUrl(), newFile.getUrl(),
						oldFile.getRevision(), newFile.getRevision(), monitor);
			} else {
				editor = TeamUiUtils.openFile(newFile.getRepoUrl(), newFile.getUrl(), oldFile.getUrl(),
						newFile.getRevision(), oldFile.getRevision(), monitor);
			}

			if (editor == null) {
				return;
			}

			boolean annotationsAdded = false;
			final ITask activeTask = CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveTask();

			if (editor instanceof ITextEditor) {
				ITextEditor textEditor = ((ITextEditor) editor);
				if (activeTask != null && activeTask.equals(task)) {
					annotationsAdded = CrucibleAnnotationModelManager.attach(textEditor, crucibleFile, review);
				}
				if (versionedComment != null) {
					selectAndRevealComment(textEditor, versionedComment, crucibleFile);
				}
			}

			if (!annotationsAdded) {
				final String msg;
				if (activeTask == null || !activeTask.equals(task)) {
					msg = "To annotate this file, review " + review.getPermId().getId() + " must be active.";
				} else {
					msg = "Editor for selected file doesn't support annotations.";
				}
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						new MessageDialog(WorkbenchUtil.getShell(), "Unable to show annotations", null, msg,
								MessageDialog.INFORMATION, new String[] { IDialogConstants.OK_LABEL }, 0).open();
					}
				});
			}
		}
	}

	private final CrucibleFile crucibleFile;

	private VersionedComment versionedComment;

	private final ITask task;

	private final Review review;

	public OpenVersionedVirtualFileAction(ITask task, CrucibleFile crucibleFile, VersionedComment versionedComment,
			Review review) {
		this(task, crucibleFile, review);
		this.versionedComment = versionedComment;

	}

	public OpenVersionedVirtualFileAction(ITask task, CrucibleFile crucibleFile, Review review) {
		this.crucibleFile = crucibleFile;
		this.task = task;
		this.review = review;
	}

	@Override
	public void run() {
		CrucibleUiUtil.checkAndRequestReviewActivation(review);
		try {
			CommonUiUtil.run(PlatformUI.getWorkbench().getProgressService(), new OpenVersionedVirtualFileRunnable());
		} catch (CoreException e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, NLS.bind(
					"Problems encoutered opening editor: {0}", e.getMessage(), e)));
		} catch (OperationCanceledException e) {
			// ignore since the user requested a cancel
		}
	}

	private void selectAndRevealComment(ITextEditor textEditor, VersionedComment comment, CrucibleFile file) {

		int startLine = comment.getToStartLine();
		if (file.isOldFile()) {
			startLine = comment.getFromStartLine();
		}

		int endLine = comment.getToEndLine();
		if (file.isOldFile()) {
			endLine = comment.getFromEndLine();
		}
		if (endLine == 0) {
			endLine = startLine;
		}
		if (startLine != 0) {
			startLine--;
		}
		TeamUiUtils.selectAndReveal(textEditor, startLine, endLine);

	}
}