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

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
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
			if (connector.isEnabled() && connector.canHandleFile(repoUrl, filePath, monitor)) {
				IEditorPart part = connector.openFile(repoUrl, filePath, revisionString, monitor);
				if (part != null) {
					return getRealEditorPart(part);
				}
			}
		}

		// try a backup solution
		return getRealEditorPart(defaultConnector.openFile(repoUrl, filePath, revisionString, monitor));
	}

	public static void openCompareEditor(String repoUrl, String filePath, String oldRevisionString,
			String newRevisionString, IProgressMonitor monitor) {
		assert (filePath != null);
		assert (oldRevisionString != null);
		assert (newRevisionString != null);

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		TeamResourceManager teamResourceManager = AtlassianUiPlugin.getDefault().getTeamResourceManager();

		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled() && connector.canHandleFile(repoUrl, filePath, monitor)) {
				if (connector.openCompareEditor(repoUrl, filePath, oldRevisionString, newRevisionString, monitor)) {
					return;
				}
			}
		}
		if (!defaultConnector.openCompareEditor(repoUrl, filePath, oldRevisionString, newRevisionString, monitor)) {
			TeamMessageUtils.openUnableToCompareErrorMessage(repoUrl, filePath, oldRevisionString, newRevisionString);
		}
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

	public static void openCompareEditorForInput(final CompareEditorInput compareEditorInput) {
		if (Display.getCurrent() != null) {
			internalOpenCompareEditorForInput(compareEditorInput);
		} else {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					internalOpenCompareEditorForInput(compareEditorInput);
				}
			});
		}
	}

	private static void internalOpenCompareEditorForInput(CompareEditorInput compareEditorInput) {
		IWorkbench workbench = AtlassianUiPlugin.getDefault().getWorkbench();
		IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
		CompareUI.openCompareEditorOnPage(compareEditorInput, page);
	}

	private static void internalSelectAndReveal(ITextEditor textEditor, final int offset, final int length) {
		textEditor.selectAndReveal(offset, length);
	}
}
