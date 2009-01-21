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

package com.atlassian.connector.eclipse.internal.crucible.ui.annotations;

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.connector.eclipse.ui.team.TeamUiUtils;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Class to manage the annotation model for the open editors
 * 
 * @author Shawn Minto
 */
public final class CrucibleAnnotationModelManager {

	private CrucibleAnnotationModelManager() {
		// ignore
	}

	private static final Object CRUCIBLE_ANNOTATION_MODEL_KEY = new Object();

	public static void attach(ITextEditor editor) {
		IEditorInput editorInput = editor.getEditorInput();
		CrucibleFile crucibleFile = TeamUiUtils.getCorrespondingCrucibleFileFromEditorInput(editorInput,
				CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview());

		attach(editor, crucibleFile);
	}

	public static void attach(ITextEditor editor, CrucibleFile crucibleFile) {
		if (!CrucibleUiPlugin.getDefault().getActiveReviewManager().isReviewActive() || crucibleFile == null) {
			return;
		}
		IDocumentProvider documentProvider = editor.getDocumentProvider();
		IEditorInput editorInput = editor.getEditorInput();
		if (documentProvider == null) {
			return;
		}
		IAnnotationModel annotationModel = documentProvider.getAnnotationModel(editorInput);
		if (!(annotationModel instanceof IAnnotationModelExtension)) {
			// we need to piggyback on another annotation mode
			return;
		}
		IAnnotationModelExtension annotationModelExtension = (IAnnotationModelExtension) annotationModel;

		IDocument document = documentProvider.getDocument(editorInput);

		IAnnotationModel crucibleAnnotationModel = annotationModelExtension.getAnnotationModel(CRUCIBLE_ANNOTATION_MODEL_KEY);
		if (crucibleAnnotationModel == null) {
			crucibleAnnotationModel = new CrucibleAnnotationModel(editor, editorInput, document, crucibleFile);
			annotationModelExtension.addAnnotationModel(CRUCIBLE_ANNOTATION_MODEL_KEY, crucibleAnnotationModel);
		} else if (crucibleAnnotationModel instanceof CrucibleAnnotationModel) {
			((CrucibleAnnotationModel) crucibleAnnotationModel).updateCrucibleFile(crucibleFile);
		}
	}

	public static void attachAllOpenEditors() {
		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (IWorkbenchPage page : window.getPages()) {
				for (IEditorReference editorReference : page.getEditorReferences()) {
					IWorkbenchPart editorPart = editorReference.getPart(false);
					if (editorPart instanceof ITextEditor) {
						attach((ITextEditor) editorPart);
					}
				}
			}
		}
	}

	public static void detach(ITextEditor editor) {
		IDocumentProvider documentProvider = editor.getDocumentProvider();
		IEditorInput editorInput = editor.getEditorInput();
		if (documentProvider == null) {
			return;
		}
		IAnnotationModel annotationModel = documentProvider.getAnnotationModel(editorInput);
		if (!(annotationModel instanceof IAnnotationModelExtension)) {
			// we need to piggyback on another annotation mode
			return;
		}

		IAnnotationModelExtension annotationModelExtension = (IAnnotationModelExtension) annotationModel;
		IAnnotationModel crucibleAnnotationModel = annotationModelExtension.getAnnotationModel(CRUCIBLE_ANNOTATION_MODEL_KEY);
		if (crucibleAnnotationModel instanceof CrucibleAnnotationModel) {
			((CrucibleAnnotationModel) crucibleAnnotationModel).clear();
		}
		annotationModelExtension.removeAnnotationModel(CRUCIBLE_ANNOTATION_MODEL_KEY);
	}

	public static void dettachAllOpenEditors() {
		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (IWorkbenchPage page : window.getPages()) {
				for (IEditorReference editorReference : page.getEditorReferences()) {
					IWorkbenchPart editorPart = editorReference.getPart(false);
					if (editorPart instanceof ITextEditor) {
						detach((ITextEditor) editorPart);
					}
				}
			}
		}
	}

	public static void updateAllOpenEditors(Review activeReview) {
		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (IWorkbenchPage page : window.getPages()) {
				for (IEditorReference editorReference : page.getEditorReferences()) {
					IWorkbenchPart editorPart = editorReference.getPart(false);
					if (editorPart instanceof ITextEditor) {
						update((ITextEditor) editorPart, activeReview);
					}
				}
			}
		}
	}

	private static void update(ITextEditor editor, Review activeReview) {
		if (!CrucibleUiPlugin.getDefault().getActiveReviewManager().isReviewActive() || activeReview == null) {
			return;
		}
		IDocumentProvider documentProvider = editor.getDocumentProvider();
		IEditorInput editorInput = editor.getEditorInput();
		if (documentProvider == null) {
			return;
		}
		IAnnotationModel iAnnotationModel = documentProvider.getAnnotationModel(editorInput);
		if (!(iAnnotationModel instanceof IAnnotationModelExtension)) {
			// we need to piggyback on another annotation mode
			return;
		}
		IAnnotationModelExtension annotationModelExtension = (IAnnotationModelExtension) iAnnotationModel;

		IAnnotationModel annotationModel = annotationModelExtension.getAnnotationModel(CRUCIBLE_ANNOTATION_MODEL_KEY);
		if (annotationModel instanceof CrucibleAnnotationModel) {
			CrucibleAnnotationModel crucibleAnnotationModel = (CrucibleAnnotationModel) annotationModel;

			CrucibleFile crucibleFile = crucibleAnnotationModel.getCrucibleFile();
			if (crucibleFile != null) {

				try {
					CrucibleFileInfo newFileInfo = activeReview.getFileByPermId(crucibleFile.getCrucibleFileInfo()
							.getPermId());
					if (newFileInfo != null) {
						crucibleAnnotationModel.updateCrucibleFile(new CrucibleFile(newFileInfo,
								crucibleFile.isOldFile()));
					}
				} catch (ValueNotYetInitialized e) {
					StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
		} else {
			// backup for if a file has been added and it is already open
			attach(editor);
		}

	}

}
