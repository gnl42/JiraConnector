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
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * The model for the annotations
 * 
 * @author Shawn Minto
 */
public class CrucibleAnnotationModel implements IAnnotationModel {

	private final Set<CrucibleCommentAnnotation> annotations = new HashSet<CrucibleCommentAnnotation>(32);

	private final Set<IAnnotationModelListener> annotationModelListeners = new HashSet<IAnnotationModelListener>(2);

	private final ITextEditor textEditor;

	private final IEditorInput editorInput;

	private final IDocument editorDocument;

	private CrucibleFile crucibleFile;

	private boolean annotated = false;

	private final IDocumentListener documentListener = new IDocumentListener() {
		public void documentChanged(DocumentEvent event) {
			updateAnnotations(false);
		}

		public void documentAboutToBeChanged(DocumentEvent event) {
		}
	};

	public CrucibleAnnotationModel(ITextEditor editor, IEditorInput editorInput, IDocument document,
			CrucibleFile crucibleFile) {
		this.textEditor = editor;
		this.editorInput = editorInput;
		this.editorDocument = document;
		this.crucibleFile = crucibleFile;
		updateAnnotations(true);
	}

	protected void updateAnnotations(boolean force) {

		boolean annotate = false;

		// TODO make sure that the local files is in sync otherwise remove the annotations

		if (!textEditor.isDirty() && editorInput != null && crucibleFile != null) {
			annotate = true;
		} else {
			annotate = false;
		}

		if (annotate) {
			if (!annotated || force) {
				createAnnotations();
				annotated = true;
			}
		} else {
			if (annotated) {
				clear();
				annotated = false;
			}
		}
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

	protected void createAnnotations() {
		AnnotationModelEvent event = new AnnotationModelEvent(this);
		clear(event);

		if (crucibleFile != null) {

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
							endLine = startLine;
						}
						int length = editorDocument.getLineOffset(endLine) - offset;

						CrucibleCommentAnnotation ca = new CrucibleCommentAnnotation(offset, length, comment);
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

	public void updateCrucibleFile(CrucibleFile newCrucibleFile) {
		// TODO we could just update the annotations appropriately instaed of remove and re-add

		this.crucibleFile = newCrucibleFile;
		updateAnnotations(true);
	}

	public CrucibleFile getCrucibleFile() {
		return crucibleFile;
	}
}
