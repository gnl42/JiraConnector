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

package com.atlassian.connector.eclipse.ui.team;

import com.atlassian.connector.eclipse.ui.AtlassianUiPlugin;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A utility class for doing UI related operations for team items
 * 
 * @author Shawn Minto
 */
public final class TeamUiUtils {

	private static final String MESSAGE_DIALOG_TITLE = "Crucible";

	private static DefaultTeamResourceConnector defaultConnector = new DefaultTeamResourceConnector();

	private static final NotOpenedEditorPart NOT_OPENED_EDITOR = new NotOpenedEditorPart();

	private TeamUiUtils() {
	}

	public static IEditorPart openFile(String repoUrl, String filePath, String revisionString, IProgressMonitor monitor) {
		// TODO if the repo url is null, we should probably use the task repo host and look at all repos

		assert (filePath != null);
		assert (revisionString != null);

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		TeamResourceManager teamResourceManager = AtlassianUiPlugin.getDefault().getTeamResourceManager();

		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled() && connector.canHandleFile(repoUrl, filePath, revisionString, monitor)) {
				IEditorPart part = connector.openFile(repoUrl, filePath, revisionString, monitor);
				if (part != null) {
					return getRealEditorPart(part);
				}
			}
		}

		// try a backup solution
		return getRealEditorPart(defaultConnector.openFile(repoUrl, filePath, revisionString, monitor));
	}

	public static IEditorPart getNotOpenedEditor() {
		return NOT_OPENED_EDITOR;
	}

	private static IEditorPart getRealEditorPart(IEditorPart editorPart) {
		if (editorPart == NOT_OPENED_EDITOR) {
			return null;
		}
		return editorPart;
	}

	public static IEditorPart openLocalResource(final IResource resource) {
		if (Display.getCurrent() != null) {
			return openLocalResourceInternal(resource);
		} else {
			final IEditorPart[] part = new IEditorPart[1];
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
					part[0] = openLocalResourceInternal(resource);
				}
			});
			return part[0];
		}
	}

	private static IEditorPart openLocalResourceInternal(IResource resource) {
		// the local revision matches the revision we care about and the file is in sync
		try {
			return IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
					(IFile) resource, true);
		} catch (PartInitException e) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}
		return null;
	}

	public static void openFileDeletedErrorMessage(final String repoUrl, final String filePath, final String revision) {
		if (Display.getCurrent() != null) {
			internalOpenFileDeletedErrorMessage(repoUrl, filePath, revision);
		} else {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					internalOpenFileDeletedErrorMessage(repoUrl, filePath, revision);
				}
			});
		}

	}

	private static void internalOpenFileDeletedErrorMessage(String repoUrl, String filePath, String revision) {
		String fileUrl = repoUrl != null ? repoUrl : "" + filePath;
		String message = "Please update the project to revision " + revision
				+ "as the following file may have been removed or deleted:\n\n" + fileUrl;

		MessageDialog.openInformation(null, MESSAGE_DIALOG_TITLE, message);
	}

	public static void openFileDoesntExistErrorMessage(final String repoUrl, final String filePath,
			final String revision) {
		if (Display.getCurrent() != null) {
			internalOpenFileDoesntExistErrorMessage(repoUrl, filePath, revision);
		} else {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					internalOpenFileDoesntExistErrorMessage(repoUrl, filePath, revision);
				}
			});
		}
	}

	private static void internalOpenFileDoesntExistErrorMessage(String repoUrl, String filePath, String revision) {

		String fileUrl = repoUrl != null ? repoUrl : "" + filePath;
		String message = "Please update the project to revision " + revision
				+ "as the following file may have been removed or deleted:\n\n" + fileUrl;

		MessageDialog.openInformation(null, MESSAGE_DIALOG_TITLE, message);
	}

	public static CrucibleFile getCorrespondingCrucibleFileFromEditorInput(IEditorInput editorInput, Review activeReview) {
		if (activeReview == null) {
			return null;
		}
		TeamResourceManager teamResourceManager = AtlassianUiPlugin.getDefault().getTeamResourceManager();

		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled() && connector.canGetCrucibleFileFromEditorInput(editorInput)) {
				CrucibleFile fileInfo = connector.getCorrespondingCrucibleFileFromEditorInput(editorInput, activeReview);
				if (fileInfo != null) {
					return fileInfo;
				}
			}
		}

		return defaultConnector.getCorrespondingCrucibleFileFromEditorInput(editorInput, activeReview);
	}

	public static void selectAndReveal(final ITextEditor textEditor, int startLine, int endLine) {
		IDocumentProvider documentProvider = textEditor.getDocumentProvider();
		IEditorInput editorInput = textEditor.getEditorInput();
		if (documentProvider != null) {
			IDocument document = documentProvider.getDocument(editorInput);
			if (document != null) {
				try {
					final int offset = document.getLineOffset(startLine);
					final int length = document.getLineOffset(endLine) - offset;
					if (Display.getCurrent() != null) {
						internalSelectAndReveal(textEditor, offset, length);
					} else {
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							public void run() {
								internalSelectAndReveal(textEditor, offset, length);
							}
						});
					}

				} catch (BadLocationException e) {
					StatusHandler.log(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
		}
	}

	private static void internalSelectAndReveal(ITextEditor textEditor, final int offset, final int length) {
		textEditor.selectAndReveal(offset, length);
	}
}
