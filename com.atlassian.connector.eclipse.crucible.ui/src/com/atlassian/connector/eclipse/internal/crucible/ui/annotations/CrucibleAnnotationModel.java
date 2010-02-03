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

import com.atlassian.connector.commons.misc.IntRanges;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
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
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * The model for the annotations
 * 
 * @author Shawn Minto
 */
public class CrucibleAnnotationModel implements IAnnotationModel, ICrucibleAnnotationModel {

	private final Set<CrucibleCommentAnnotation> annotations = new HashSet<CrucibleCommentAnnotation>(32);

	private final Set<IAnnotationModelListener> annotationModelListeners = new HashSet<IAnnotationModelListener>(2);

	private final ITextEditor textEditor;

	private final IEditorInput editorInput;

	private IDocument editorDocument;

	private CrucibleFile crucibleFile;

	private boolean annotated = false;

	private Review review;

	private final IDocumentListener documentListener = new IDocumentListener() {
		public void documentChanged(DocumentEvent event) {
			updateAnnotations(false);
		}

		public void documentAboutToBeChanged(DocumentEvent event) {
		}
	};

	public CrucibleAnnotationModel(ITextEditor editor, IEditorInput editorInput, IDocument document,
			CrucibleFile crucibleFile, Review review) {
		this.textEditor = editor;
		this.editorInput = editorInput;
		this.editorDocument = document;
		this.crucibleFile = crucibleFile;
		this.review = review;
		updateAnnotations(true);
	}

	protected void updateAnnotations(boolean force) {

		boolean annotate = false;

		// TODO make sure that the local files is in sync otherwise remove the annotations

		if (textEditor == null && editorInput == null && editorDocument != null) {
			annotate = true;
		} else {
			if (editorDocument == null) {
				annotate = false;
			} else if (!textEditor.isDirty() && editorInput != null && crucibleFile != null) {
				annotate = true;
			} else {
				annotate = false;
			}
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
				createCommentAnnotation(event, comment);
			}
		}

		fireModelChanged(event);
	}

	private void createCommentAnnotation(AnnotationModelEvent event, VersionedComment comment) {
		try {

			int startLine = 0;
			int endLine = 0;

			final Map<String, IntRanges> lineRanges = comment.getLineRanges();
			if (lineRanges != null && !lineRanges.isEmpty()) {

				final String displayedRev = crucibleFile.getSelectedFile().getRevision();
				final IntRanges intRanges = lineRanges.get(displayedRev);
				if (intRanges != null) {
					startLine = intRanges.getTotalMin();
					endLine = intRanges.getTotalMax();
				} else {
					return;
				}
			} else {

				//if fromLineInfo and not toLineInfo and not oldFile -> old line comment but current model is new file
				if (comment.isFromLineInfo() && !comment.isToLineInfo() && !crucibleFile.isOldFile()) {
					return;
				}

				//if toLineInfo and not fromLineInfo and  and oldfile -> new line comment but current model is old file
				if (comment.isToLineInfo() && !comment.isFromLineInfo() && crucibleFile.isOldFile()) {
					return;
				}
				startLine = comment.getToStartLine();
				if (crucibleFile.isOldFile()) {
					startLine = comment.getFromStartLine();
				}

				endLine = comment.getToEndLine();
				if (crucibleFile.isOldFile()) {
					endLine = comment.getFromEndLine();
				}
			}

			int offset = 0;
			int length = 0;
			if (startLine != 0) {
				offset = editorDocument.getLineOffset(startLine - 1);
				if (endLine == 0) {
					endLine = startLine;
				}
				length = editorDocument.getLineOffset(endLine) - offset;

			}
			CrucibleCommentAnnotation ca = new CrucibleCommentAnnotation(offset, length, comment,
					crucibleFile.getCrucibleFileInfo(), review);
			annotations.add(ca);
			event.annotationAdded(ca);

		} catch (BadLocationException e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID, "Unable to add annoation.", e));
		}
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

	public void updateCrucibleFile(CrucibleFile newCrucibleFile, Review newReview) {
		// TODO we could just update the annotations appropriately instead of remove and re-add
		this.review = newReview;
		this.crucibleFile = newCrucibleFile;
		updateAnnotations(true);
	}

	public CrucibleFile getCrucibleFile() {
		return crucibleFile;
	}

	/**
	 * Returns the first annotation that this knows about for the given offset in the document
	 */
	public CrucibleCommentAnnotation getFirstAnnotationForOffset(int offset) {
		for (CrucibleCommentAnnotation annotation : annotations) {
			if (annotation.getPosition().offset <= offset
					&& (annotation.getPosition().length + annotation.getPosition().offset) >= offset) {
				return annotation;
			}
		}
		return null;
	}

	public void setEditorDocument(IDocument editorDocument) {
		this.editorDocument = editorDocument;
		updateAnnotations(true);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((crucibleFile.getCrucibleFileInfo().getFileDescriptor().getAbsoluteUrl() == null) ? 0
						: crucibleFile.getCrucibleFileInfo().getFileDescriptor().getAbsoluteUrl().hashCode());
		result = prime
				* result
				+ ((crucibleFile.getCrucibleFileInfo().getFileDescriptor().getRevision() == null) ? 0
						: crucibleFile.getCrucibleFileInfo().getFileDescriptor().getRevision().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CrucibleAnnotationModel other = (CrucibleAnnotationModel) obj;
		if (crucibleFile.getCrucibleFileInfo().getFileDescriptor().getAbsoluteUrl() == null) {
			if (other.crucibleFile.getCrucibleFileInfo().getFileDescriptor().getAbsoluteUrl() != null) {
				return false;
			}
		} else if (!crucibleFile.getCrucibleFileInfo().getFileDescriptor().getAbsoluteUrl().equals(
				other.crucibleFile.getCrucibleFileInfo().getFileDescriptor().getAbsoluteUrl())) {
			return false;
		}
		if (crucibleFile.getCrucibleFileInfo().getFileDescriptor().getRevision() == null) {
			if (other.crucibleFile.getCrucibleFileInfo().getFileDescriptor().getRevision() != null) {
				return false;
			}
		} else if (!crucibleFile.getCrucibleFileInfo().getFileDescriptor().getRevision().equals(
				other.crucibleFile.getCrucibleFileInfo().getFileDescriptor().getRevision())) {
			return false;
		}
		return true;
	}

}
