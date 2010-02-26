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

package com.atlassian.connector.eclipse.internal.crucible.ui.editor.ruler;

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.ICrucibleFileProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.annotations.CrucibleAnnotationModel;
import com.atlassian.connector.eclipse.internal.crucible.ui.annotations.CrucibleCommentAnnotation;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.AbstractRulerColumn;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.texteditor.PropertyEventDispatcher;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.rulers.IContributedRulerColumn;
import org.eclipse.ui.texteditor.rulers.RulerColumnDescriptor;

import java.util.List;

public class CommentAnnotationRulerColumn extends AbstractRulerColumn implements IContributedRulerColumn {

	/** The contribution descriptor. */
	private RulerColumnDescriptor fDescriptor;

	private CrucibleAnnotationModel annotationModel;

	private ITextEditor fEditor;

	private Color colorCommented;

	private ISourceViewer fViewer;

	private PropertyEventDispatcher fDispatcher;

	public CommentAnnotationRulerColumn() {
		setTextInset(10);
		setHover(new CommentAnnotationRulerHover(this));
	}

	@Override
	public void dispose() {
		colorCommented.dispose();

		super.dispose();
	}

	public RulerColumnDescriptor getDescriptor() {
		return fDescriptor;
	}

	public void setDescriptor(RulerColumnDescriptor descriptor) {
		fDescriptor = descriptor;
	}

	public void setEditor(ITextEditor editor) {
		fEditor = editor;
	}

	public ITextEditor getEditor() {
		return fEditor;
	}

	public void columnCreated() {
	}

	public void columnRemoved() {
	}

	protected Color computeLeftBackground(int line) {
		List<CrucibleCommentAnnotation> annotations = getAnnotations(line);
		if (annotations == null || annotations.size() == 0) {
			return super.computeBackground(line);
		} else {
			return colorCommented;
		}
	}

	@Override
	protected Color computeForeground(int line) {
		return Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
	}

	@Override
	protected void paintLine(GC gc, int modelLine, int widgetLine, int linePixel, int lineHeight) {
		gc.setBackground(computeLeftBackground(modelLine));
		gc.fillRectangle(0, linePixel, getWidth(), lineHeight);
	}

	public List<CrucibleCommentAnnotation> getAnnotations(int startLine) {
		if (fEditor.getEditorInput() instanceof ICrucibleFileProvider) {
			annotationModel = new CrucibleAnnotationModel(fEditor, fEditor.getEditorInput(),
					fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput()),
					((ICrucibleFileProvider) fEditor.getEditorInput()).getCrucibleFile(), CrucibleUiPlugin.getDefault()
							.getActiveReviewManager()
							.getActiveReview());
		}

		try {
			int offset = fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput()).getLineOffset(startLine);
			return annotationModel == null ? null : annotationModel.getAnnotationsForOffset(offset);
		} catch (BadLocationException e) {
		}
		return null;
	}

	private IPreferenceStore getPreferenceStore() {
		return EditorsUI.getPreferenceStore();
	}

	private ISharedTextColors getSharedColors() {
		return EditorsUI.getSharedTextColors();
	}

	private static RGB getColorFromAnnotationPreference(IPreferenceStore store, AnnotationPreference pref) {
		String key = pref.getColorPreferenceKey();
		RGB rgb = null;
		if (store.contains(key)) {
			if (store.isDefault(key)) {
				rgb = pref.getColorPreferenceValue();
			} else {
				rgb = PreferenceConverter.getColor(store, key);
			}
		}
		if (rgb == null) {
			rgb = pref.getColorPreferenceValue();
		}
		return rgb;
	}

	private void updateCommentedColor(AnnotationPreference pref, IPreferenceStore store) {
		if (pref != null) {
			RGB rgb = getColorFromAnnotationPreference(store, pref);
			colorCommented = getSharedColors().getColor(rgb);
		}
	}

	/**
	 * Initializes the given line number ruler column from the preference store.
	 */
	private void initialize() {
		final IPreferenceStore store = getPreferenceStore();
		if (store == null) {
			return;
		}

		AnnotationPreferenceLookup lookup = EditorsUI.getAnnotationPreferenceLookup();
		final AnnotationPreference commentedPref = lookup.getAnnotationPreference(CrucibleCommentAnnotation.COMMENT_ANNOTATION_ID);

		updateCommentedColor(commentedPref, store);

		redraw();

		// listen to changes
		fDispatcher = new PropertyEventDispatcher(store);

		if (commentedPref != null) {
			fDispatcher.addPropertyChangeListener(commentedPref.getColorPreferenceKey(), new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					updateCommentedColor(commentedPref, store);
					redraw();
				}
			});
		}
	}

	@Override
	public Control createControl(CompositeRuler parentRuler, Composite parentControl) {
		ITextViewer viewer = parentRuler.getTextViewer();
		Assert.isLegal(viewer instanceof ISourceViewer);
		fViewer = (ISourceViewer) viewer;
		fViewer.showAnnotations(true);
		IAnnotationModel model = fViewer.getAnnotationModel();
		if (model == null) {
			fViewer.setDocument(fViewer.getDocument(), new AnnotationModel());
		}

		initialize();

		/*
		fViewer.getAnnotationModel().addAnnotationModelListener(new IAnnotationModelListener() {
			public void modelChanged(IAnnotationModel model) {
				if (annotationModel == null) {
					if (fEditor.getEditorInput() instanceof ICrucibleFileProvider) {
						annotationModel = new CrucibleAnnotationModel(fEditor, fEditor.getEditorInput(),
								fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput()),
								((ICrucibleFileProvider) fEditor.getEditorInput()).getCrucibleFile(),
								CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview());
					}
				}
			}
		});*/

		return super.createControl(parentRuler, parentControl);
	}
}
