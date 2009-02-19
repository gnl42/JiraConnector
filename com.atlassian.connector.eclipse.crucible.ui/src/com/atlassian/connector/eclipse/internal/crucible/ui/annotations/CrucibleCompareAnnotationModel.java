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

import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.connector.eclipse.ui.team.ICompareAnnotationModel;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.source.AnnotationBarHoverManager;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.internal.texteditor.AnnotationColumn;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * Model for annotations in the diff view.
 * 
 * @author Thomas Ehrnhoefer
 */
public class CrucibleCompareAnnotationModel implements ICompareAnnotationModel {

	private final class CrucibleViewerTextInputListener implements ITextInputListener {
		private final SourceViewer sourceViewer;

		private final CrucibleAnnotationModel crucibleAnnotationModel;

		private final boolean oldFile;

		private CrucibleViewerTextInputListener(SourceViewer sourceViewer,
				CrucibleAnnotationModel crucibleAnnotationModel, boolean oldFile) {
			this.sourceViewer = sourceViewer;
			this.crucibleAnnotationModel = crucibleAnnotationModel;
			this.oldFile = oldFile;
		}

		public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
			// ignore
		}

		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
			if (oldInput != null) {
				crucibleAnnotationModel.disconnect(oldInput);
			}

			if (newInput != null && sourceViewer != null) {
				IAnnotationModel annotationModel = sourceViewer.getAnnotationModel();

				if (annotationModel instanceof IAnnotationModelExtension) {

					IAnnotationModelExtension annotationModelExtension = (IAnnotationModelExtension) annotationModel;

//					IDocument document = sourceViewer.getDocument();

					annotationModelExtension.addAnnotationModel("test", crucibleAnnotationModel);
					crucibleAnnotationModel.setEditorDocument(sourceViewer.getDocument());
				} else {

					try {

						Class<SourceViewer> sourceViewerClazz = SourceViewer.class;
						Field declaredField2 = sourceViewerClazz.getDeclaredField("fVisualAnnotationModel");
						Method declaredMethod = sourceViewerClazz.getDeclaredMethod("createVisualAnnotationModel",
								IAnnotationModel.class);
						declaredMethod.setAccessible(true);
						declaredField2.setAccessible(true);
						annotationModel = (IAnnotationModel) declaredMethod.invoke(sourceViewer,
								crucibleAnnotationModel);
						declaredField2.set(sourceViewer, annotationModel);
						annotationModel.connect(newInput);
						sourceViewer.showAnnotations(true);

						crucibleAnnotationModel.setEditorDocument(sourceViewer.getDocument());
						createVerticalRuler(newInput, sourceViewerClazz);
//						createOverviewRuler(newInput, sourceViewerClazz);
						createHighlighting(sourceViewerClazz);

					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			}

		}

		private void createHighlighting(Class<SourceViewer> sourceViewerClazz) throws IllegalArgumentException,
				IllegalAccessException, SecurityException, NoSuchFieldException {
			//TODO this could use some performance tweaks
			final StyledText styledText = sourceViewer.getTextWidget();
			styledText.addLineBackgroundListener(new LineBackgroundListener() {
				public void lineGetBackground(LineBackgroundEvent event) {
					int lineNr = styledText.getLineAtOffset(event.lineOffset) + 1;
					Iterator<CrucibleCommentAnnotation> it = crucibleAnnotationModel.getAnnotationIterator();
					while (it.hasNext()) {
						CrucibleCommentAnnotation annotation = it.next();
						int startLine;
						int endLine;
						VersionedComment comment = annotation.getVersionedComment();
						if (oldFile) {
							startLine = comment.getFromStartLine();
							endLine = comment.getFromEndLine();
						} else {
							startLine = comment.getToStartLine();
							endLine = comment.getToEndLine();
						}
						if (lineNr >= startLine && lineNr <= endLine) {
							AnnotationPreference pref = new AnnotationPreferenceLookup().getAnnotationPreference(annotation);
							if (pref.getHighlightPreferenceValue()) {
								event.lineBackground = new Color(Display.getDefault(), pref.getColorPreferenceValue());
							}
						}
					}
				}

			});
		}

		/*
		 * overview ruler problem: displayed in both viewers. the diff editor ruler is actually custom drawn
		 * (see TextMergeViewer.fBirdsEyeCanvas)
		 * the ruler that gets created in this method is longer than the editor, meaning its not an overview
		 * (not next to the scrollbar)
		 */
		private void createOverviewRuler(IDocument newInput, Class<SourceViewer> sourceViewerClazz)
				throws SecurityException, NoSuchMethodException, NoSuchFieldException, IllegalArgumentException,
				IllegalAccessException, InvocationTargetException {

			sourceViewer.setOverviewRulerAnnotationHover(new CrucibleAnnotationHover(null));

			OverviewRuler ruler = new OverviewRuler(new DefaultMarkerAnnotationAccess(), 15, EditorsPlugin.getDefault()
					.getSharedTextColors());
			Field compositeField = sourceViewerClazz.getDeclaredField("fComposite");
			compositeField.setAccessible(true);

			ruler.createControl((Composite) compositeField.get(sourceViewer), sourceViewer);
			ruler.setModel(leftAnnotationModel);
			ruler.update();

			Field overViewRulerField = sourceViewerClazz.getDeclaredField("fOverviewRuler");
			overViewRulerField.setAccessible(true);

			if (overViewRulerField.get(sourceViewer) == null) {
				overViewRulerField.set(sourceViewer, ruler);
			}

			Method declareMethod = sourceViewerClazz.getDeclaredMethod("ensureOverviewHoverManagerInstalled");
			declareMethod.setAccessible(true);
			declareMethod.invoke(sourceViewer);
			//overviewRuler is null

			Field hoverManager = sourceViewerClazz.getDeclaredField("fOverviewRulerHoveringController");
			hoverManager.setAccessible(true);
			AnnotationBarHoverManager manager = (AnnotationBarHoverManager) hoverManager.get(sourceViewer);
			if (manager != null) {
				Field annotationHover = AnnotationBarHoverManager.class.getDeclaredField("fAnnotationHover");
				annotationHover.setAccessible(true);
				IAnnotationHover hover = (IAnnotationHover) annotationHover.get(manager);
				annotationHover.set(manager, new CrucibleAnnotationHover(null));
			}
			sourceViewer.showAnnotations(true);
			sourceViewer.showAnnotationsOverview(true);

			declareMethod = sourceViewerClazz.getDeclaredMethod("showAnnotationsOverview", new Class[] { Boolean.TYPE });
			declareMethod.setAccessible(true);
		}

		private void createVerticalRuler(IDocument newInput, Class<SourceViewer> sourceViewerClazz)
				throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
				InvocationTargetException, NoSuchFieldException {
// handled by listener
//									if (fVerticalRuler != null)
//										fVerticalRuler.setModel(fVisualAnnotationModel);
//
//									if (fOverviewRuler != null)
//										fOverviewRuler.setModel(fVisualAnnotationModel);

			Method declaredMethod2 = sourceViewerClazz.getDeclaredMethod("getVerticalRuler");
			declaredMethod2.setAccessible(true);
			CompositeRuler ruler = (CompositeRuler) declaredMethod2.invoke(sourceViewer);

			boolean hasDecorator = false;

			sourceViewer.setAnnotationHover(new CrucibleAnnotationHover(null));

			Field hoverManager = SourceViewer.class.getDeclaredField("fVerticalRulerHoveringController");
			hoverManager.setAccessible(true);
			AnnotationBarHoverManager manager = (AnnotationBarHoverManager) hoverManager.get(sourceViewer);
			if (manager != null) {

				Field annotationHover = AnnotationBarHoverManager.class.getDeclaredField("fAnnotationHover");
				annotationHover.setAccessible(true);
				IAnnotationHover hover = (IAnnotationHover) annotationHover.get(manager);

				annotationHover.set(manager, new CrucibleAnnotationHover(hover));

			}

			sourceViewer.showAnnotations(true);
			sourceViewer.showAnnotationsOverview(true);

			Iterator iter = (ruler).getDecoratorIterator();
			if (iter.hasNext()) {
				for (Object obj = iter.next(); iter.hasNext(); obj = iter.next()) {
					if (obj instanceof AnnotationRulerColumn) {
						hasDecorator = true;
					}
				}
			}

			if (!hasDecorator) {
				AnnotationColumn annotationColumn = new AnnotationColumn();
				annotationColumn.createControl(ruler, ruler.getControl().getParent());

				ruler.addDecorator(0, annotationColumn);
			}
		}
	}

	private final CrucibleAnnotationModel leftAnnotationModel;

	private final CrucibleAnnotationModel rightAnnotationModel;

	private final Review review;

	public CrucibleCompareAnnotationModel(CrucibleFileInfo crucibleFile, Review review) {
		super();
		this.review = review;
		this.leftAnnotationModel = new CrucibleAnnotationModel(null, null, null, new CrucibleFile(crucibleFile, false),
				review);
		this.rightAnnotationModel = new CrucibleAnnotationModel(null, null, null, new CrucibleFile(crucibleFile, true),
				review);
	}

	public void attachToViewer(final SourceViewer fLeft, final SourceViewer fRight) {
		addTextInputListener(fLeft, leftAnnotationModel, false);
		addTextInputListener(fRight, rightAnnotationModel, true);
	}

	private void addTextInputListener(final SourceViewer sourceViewer,
			final CrucibleAnnotationModel crucibleAnnotationModel, boolean oldFile) {
		sourceViewer.addTextInputListener(new CrucibleViewerTextInputListener(sourceViewer, crucibleAnnotationModel,
				oldFile));
	}

	public void detach() {
		//TODO disconnect - do I need to disconnect something, or is the annotationModel.disconnect called anyway?
	}

	public void updateCrucibleFile(CrucibleFile newCrucibleFile, Review newReview) {
		leftAnnotationModel.updateCrucibleFile(new CrucibleFile(newCrucibleFile.getCrucibleFileInfo(), false),
				newReview);
		rightAnnotationModel.updateCrucibleFile(new CrucibleFile(newCrucibleFile.getCrucibleFileInfo(), true),
				newReview);
	}
}
