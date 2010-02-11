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

package com.atlassian.connector.eclipse.internal.crucible.ui.util;

import com.atlassian.connector.commons.crucible.api.model.ReviewModelUtil;
import com.atlassian.connector.commons.misc.IntRanges;
import com.atlassian.connector.eclipse.internal.crucible.ui.ICrucibleFileProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.CrucibleFileInfoCompareEditorInput;
import com.atlassian.connector.eclipse.team.ui.AtlassianTeamUiPlugin;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import java.lang.reflect.Method;
import java.util.Map;

public final class EditorUtil {
	private EditorUtil() {
		// do not instantiate
	}

	/**
	 * Tests if a CU is currently shown in an editor
	 * 
	 * @param inputElement
	 *            the input element
	 * @return the IEditorPart if shown, null if element is not open in an editor
	 */
	public static IEditorPart isOpenInEditor(Object inputElement) {
		IWorkbenchPage page = getActivePage();

		CrucibleFileInfo fileInfo = null;
		VersionedComment parentComment = null;
		if (inputElement instanceof CrucibleFileInfo) {
			fileInfo = (CrucibleFileInfo) inputElement;
		} else if (inputElement instanceof Comment) {
			parentComment = ReviewModelUtil.getParentVersionedComment((Comment) inputElement);
			if (parentComment != null) {
				fileInfo = parentComment.getCrucibleFileInfo();
			}
		}

		if (fileInfo == null) {
			return null;
		}

		if (page != null) {
			// check current first because we can match multiple editors and would be bad if we switched
			// user to another editor if current matches too
			IEditorPart editor = page.getActiveEditor();
			if (editor != null) {
				if (isOpenInEditor(fileInfo, parentComment, editor.getEditorInput())) {
					return editor;
				}
			}

			IEditorReference[] editors = page.getEditorReferences();
			if (editors != null) {
				for (IEditorReference ref : editors) {
					try {
						if (isOpenInEditor(fileInfo, parentComment, ref.getEditorInput())) {
							return ref.getEditor(true);
						}
					} catch (PartInitException e) {
						// ignore
					}
				}
			}
		}

		return null;
	}

	private static boolean isOpenInEditor(CrucibleFileInfo fileInfo, VersionedComment parentComment, IEditorInput input) {
		if (input instanceof ICrucibleFileProvider) {
			CrucibleFile crucibleFile = ((ICrucibleFileProvider) input).getCrucibleFile();
			if (fileInfo.equals(crucibleFile.getCrucibleFileInfo())) {
				if (parentComment != null) {
					Map<String, IntRanges> commentRanges = parentComment.getLineRanges();
					if (commentRanges != null
							&& commentRanges.containsKey(crucibleFile.getSelectedFile().getRevision())) {
						return true;
					}
				} else {
					return true;
				}
			}
		}
		if (input instanceof CrucibleFileInfoCompareEditorInput) {
			if (fileInfo.equals(((CrucibleFileInfoCompareEditorInput) input).getCrucibleFileInfo())) {
				return true;
			}
		}
		return false;
	}

	private static void internalSelectAndReveal(ITextEditor textEditor, final int offset, final int length) {
		textEditor.selectAndReveal(offset, length);
	}

	public static void selectAndReveal(final ITextEditor textEditor, int startLine) {
		IDocumentProvider documentProvider = textEditor.getDocumentProvider();
		IEditorInput editorInput = textEditor.getEditorInput();
		if (documentProvider != null) {
			IDocument document = documentProvider.getDocument(editorInput);
			if (document != null) {
				try {
					final int offset = document.getLineOffset(startLine);
					if (Display.getCurrent() != null) {
						internalSelectAndReveal(textEditor, offset, 0);
					} else {
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							public void run() {
								internalSelectAndReveal(textEditor, offset, 0);
							}
						});
					}

				} catch (BadLocationException e) {
					StatusHandler.log(new Status(IStatus.ERROR, AtlassianTeamUiPlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
		}
	}

	public static void selectAndReveal(ITextEditor textEditor, VersionedComment comment, VersionedVirtualFile file) {
		Map<String, IntRanges> lineRanges = comment.getLineRanges();
		if (lineRanges == null) {
			return;
		}

		IntRanges lineRange = lineRanges.get(file.getRevision());
		if (lineRange != null) {
			EditorUtil.selectAndReveal(textEditor, lineRange.getTotalMin() - 1);
		}
	}

	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}
		return window.getActivePage();
	}

	public static SourceViewer getSourceViewer(ITextEditor editor) {
		if (editor instanceof AbstractTextEditor) {
			Method getSourceViewer;
			try {
				getSourceViewer = AbstractTextEditor.class.getDeclaredMethod("getSourceViewer");
				getSourceViewer.setAccessible(true);
				return (SourceViewer) getSourceViewer.invoke(editor);
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

}