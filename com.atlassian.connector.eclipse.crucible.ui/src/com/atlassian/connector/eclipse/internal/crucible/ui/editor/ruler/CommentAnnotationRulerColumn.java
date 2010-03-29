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

import com.atlassian.connector.eclipse.internal.crucible.ui.ActiveReviewManager.IReviewActivationListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.ICrucibleFileProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.annotations.CrucibleAnnotationModel;
import com.atlassian.connector.eclipse.internal.crucible.ui.annotations.CrucibleCommentAnnotation;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.AbstractRulerColumn;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.texteditor.PropertyEventDispatcher;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.rulers.IContributedRulerColumn;
import org.eclipse.ui.texteditor.rulers.RulerColumnDescriptor;

import java.util.List;

public class CommentAnnotationRulerColumn extends AbstractRulerColumn implements IContributedRulerColumn,
		IReviewActivationListener {

	/** The contribution descriptor. */
	private RulerColumnDescriptor fDescriptor;

	private CrucibleAnnotationModel annotationModel;

	private ITextEditor fEditor;

	private Color colorCommented;

	private ISourceViewer fViewer;

	private PropertyEventDispatcher fDispatcher;

	private IDocumentProvider fDocumentProvider;

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
		fDocumentProvider = fEditor.getDocumentProvider();
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
		try {
			int offset = fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput()).getLineOffset(startLine);
			return annotationModel == null ? null : annotationModel.getAnnotationsForOffset(offset);
		} catch (BadLocationException e) {
		}
		return null;
	}

	private ISharedTextColors getSharedColors() {
		return EditorsUI.getSharedTextColors();
	}

	public static RGB getColorFromAnnotationPreference(IPreferenceStore store, AnnotationPreference pref) {
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
		final IPreferenceStore store = EditorsUI.getPreferenceStore();
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

		fViewer.getAnnotationModel().addAnnotationModelListener(new IAnnotationModelListener() {
			public void modelChanged(IAnnotationModel model) {
				reviewActivated(CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveTask(),
						CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview());
			}
		});

		initialize();

		CrucibleUiPlugin.getDefault().getActiveReviewManager().addReviewActivationListener(this);
		return super.createControl(parentRuler, parentControl);
	}

	public void reviewActivated(ITask task, Review review) {
		annotationModel = null;
		CrucibleFileInfo currentFileInfo = null;
		CrucibleFile file = null;

		if (fEditor.getEditorInput() instanceof ICrucibleFileProvider) {
			ICrucibleFileProvider fileProvider = (ICrucibleFileProvider) fEditor.getEditorInput();
			file = fileProvider.getCrucibleFile();
		}

		if (fEditor.getEditorInput() instanceof IFileEditorInput) {
			IFileEditorInput input = (IFileEditorInput) fEditor.getEditorInput();
			file = CrucibleUiUtil.getCrucibleFileFromResource(input.getFile(), review, new NullProgressMonitor());
		}

		if (file != null) {
			currentFileInfo = review.getFileByPermId(file.getCrucibleFileInfo().getPermId());

			if (currentFileInfo != null) {
				annotationModel = new CrucibleAnnotationModel(fEditor, fEditor.getEditorInput(),
						fDocumentProvider.getDocument(fEditor.getEditorInput()), new CrucibleFile(currentFileInfo,
								file.isOldFile()), review);
			}
		}

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				redraw();
			}
		});
	}

	public void reviewDeactivated(ITask task, Review review) {
		annotationModel = null;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				redraw();
			}
		});
	}

	public void reviewUpdated(ITask task, Review review) {
		reviewActivated(task, review);
	}
}
