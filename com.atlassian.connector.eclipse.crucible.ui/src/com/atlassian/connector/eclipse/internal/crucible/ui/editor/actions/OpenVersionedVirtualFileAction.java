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

package com.atlassian.connector.eclipse.internal.crucible.ui.editor.actions;

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.annotations.CrucibleAnnotationModelManager;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.connector.eclipse.ui.team.TeamUiUtils;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import java.lang.reflect.InvocationTargetException;

/**
 * Action to open a version file
 * 
 * @author Shawn Minto
 */
public class OpenVersionedVirtualFileAction extends Action {

	private final CrucibleFile crucibleFile;

	private VersionedComment versionedComment;

	private final ITask task;

	public OpenVersionedVirtualFileAction(ITask task, CrucibleFile crucibleFile, VersionedComment versionedComment) {
		this(task, crucibleFile);
		this.versionedComment = versionedComment;
	}

	public OpenVersionedVirtualFileAction(ITask task, CrucibleFile crucibleFile) {
		this.crucibleFile = crucibleFile;
		this.task = task;
	}

	@Override
	public void run() {
		try {
			PlatformUI.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

					VersionedVirtualFile virtualFile = crucibleFile.getCrucibleFileInfo().getFileDescriptor();
					if (crucibleFile.isOldFile()) {
						virtualFile = crucibleFile.getCrucibleFileInfo().getOldFileDescriptor();
					}

					IEditorPart editor = TeamUiUtils.openFile(virtualFile.getRepoUrl(), virtualFile.getUrl(),
							virtualFile.getRevision(), monitor);
					if (editor instanceof ITextEditor) {
						ITextEditor textEditor = ((ITextEditor) editor);
						if (CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveTask().equals(task)) {
							CrucibleAnnotationModelManager.attach(textEditor, crucibleFile);
						}
						if (versionedComment != null) {
							selectAndRevealComment(textEditor, versionedComment, crucibleFile);
						}
					}
				}

			});
		} catch (InvocationTargetException e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
		} catch (InterruptedException e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
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