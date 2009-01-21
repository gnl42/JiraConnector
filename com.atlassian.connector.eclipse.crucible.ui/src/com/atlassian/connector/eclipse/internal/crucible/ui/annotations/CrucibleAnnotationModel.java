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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.connector.eclipse.ui.team.TeamUiUtils;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The model for the annotations
 * 
 * @author Shawn Minto
 */
public class CrucibleAnnotationModel implements IAnnotationModel {

	private final Review activeReview = CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();

	private static final Object CRUCIBLE_ANNOTATION_MODEL_KEY = new Object();

	private final List<CrucibleCommentAnnotation> annotations = new ArrayList<CrucibleCommentAnnotation>(32);

	private final List<IAnnotationModelListener> annotationModelListeners = new ArrayList<IAnnotationModelListener>(2);

	private final ITextEditor textEditor;

	private final IEditorInput editorInput;

	private final IDocument editorDocument;

	private boolean annotated = false;

	private final IDocumentListener documentListener = new IDocumentListener() {
		public void documentChanged(DocumentEvent event) {
			updateAnnotations(false);
		}

		public void documentAboutToBeChanged(DocumentEvent event) {
		}
	};

	public CrucibleAnnotationModel(ITextEditor editor, IEditorInput editorInput, IDocument document) {
		this.textEditor = editor;
		this.editorInput = editorInput;
		this.editorDocument = document;
		updateAnnotations(true);
	}

	protected void updateAnnotations(boolean force) {
		CrucibleFile crucibleFile = null;
		boolean annotate = false;
		if (!textEditor.isDirty() && editorInput != null) {
			crucibleFile = getCrucibleFile(editorInput);
			if (crucibleFile != null) {
				annotate = true;
			} else {
				annotate = false;
			}
		} else {
			annotate = false;
		}

		if (annotate) {
			if (!annotated || force) {
				createAnnotations(crucibleFile);
				annotated = true;
			}
		} else {
			if (annotated) {
				clear();
				annotated = false;
			}
		}
	}

	protected CrucibleFile getCrucibleFile(IEditorInput input) {
		return TeamUiUtils.getCorrespondingCrucibleFileFromEditorInput(input, activeReview);
	}

	protected void clear() {
		AnnotationModelEvent event = new AnnotationModelEvent(this);
		clear(event);
		fireModelChanged(event);
	}

	protected void clear(AnnotationModelEvent event) {
		for (CrucibleCommentAnnotation commentAnnotation : annotations) {
			event.annotationRemoved(commentAnnotation, commentAnnotation.getPosition());
		}
		annotations.clear();
	}

	protected void createAnnotations(CrucibleFile crucibleFile) {
		AnnotationModelEvent event = new AnnotationModelEvent(this);
		clear(event);

		if (activeReview != null && crucibleFile != null) {

			for (VersionedComment comment : crucibleFile.getCrucibleFileInfo().getVersionedComments()) {
				try {
					int startLine = comment.getToStartLine();
					if (crucibleFile.isOldFile()) {
						startLine = comment.getFromStartLine();
					}

					int endLine = comment.getToEndLine();
					if (crucibleFile.isOldFile()) {
						endLine = comment.getFromEndLine();
					}

					if (startLine != 0) {
						int offset = editorDocument.getLineOffset(startLine - 1);
						if (endLine == 0) {
							endLine = startLine + 1;
						}
						int length = editorDocument.getLineOffset(endLine) - offset;
						Position p = new Position(offset, length);

						CrucibleCommentAnnotation ca = new CrucibleCommentAnnotation(offset, length);
						annotations.add(ca);
						event.annotationAdded(ca);
					}
				} catch (BadLocationException e) {
					StatusHandler.log(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID,
							"Unable to add annoation.", e));
				}
			}
		}

		fireModelChanged(event);
	}

	public void addAnnotationModelListener(IAnnotationModelListener listener) {
		if (!annotationModelListeners.contains(listener)) {
			annotationModelListeners.add(listener);
			fireModelChanged(new AnnotationModelEvent(this, true));
		}
	}

	public void removeAnnotationModelListener(IAnnotationModelListener listener) {
		annotationModelListeners.remove(listener);
	}

	protected void fireModelChanged(AnnotationModelEvent event) {
		event.markSealed();
		if (!event.isEmpty()) {
			for (IAnnotationModelListener listener : annotationModelListeners) {
				if (listener instanceof IAnnotationModelListenerExtension) {
					((IAnnotationModelListenerExtension) listener).modelChanged(event);
				} else {
					listener.modelChanged(this);
				}
			}
		}
	}

	public void connect(IDocument document) {

		for (CrucibleCommentAnnotation commentAnnotation : annotations) {
			try {
				document.addPosition(commentAnnotation.getPosition());
			} catch (BadLocationException e) {
				StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
			}
		}
		document.addDocumentListener(documentListener);
	}

	public void disconnect(IDocument document) {

		for (CrucibleCommentAnnotation commentAnnotation : annotations) {
			document.removePosition(commentAnnotation.getPosition());
		}

		document.removeDocumentListener(documentListener);
	}

	public void addAnnotation(Annotation annotation, Position position) {
		// do nothing, we do not support external modification
	}

	public void removeAnnotation(Annotation annotation) {
		// do nothing, we do not support external modification
	}

	public Iterator<CrucibleCommentAnnotation> getAnnotationIterator() {
		return annotations.iterator();
	}

	public Position getPosition(Annotation annotation) {
		if (annotation instanceof CrucibleCommentAnnotation) {
			return ((CrucibleCommentAnnotation) annotation).getPosition();
		} else {
			// we dont understand any other annotations
			return null;
		}
	}

	public static void attach(ITextEditor editor) {
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
			crucibleAnnotationModel = new CrucibleAnnotationModel(editor, editorInput, document);
			annotationModelExtension.addAnnotationModel(CRUCIBLE_ANNOTATION_MODEL_KEY, crucibleAnnotationModel);
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
}
