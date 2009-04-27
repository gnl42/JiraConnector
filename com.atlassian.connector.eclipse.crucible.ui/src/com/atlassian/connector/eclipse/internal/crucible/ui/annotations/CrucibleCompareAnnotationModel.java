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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.AddGeneralCommentToFileAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.AddLineCommentToFileAction;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.connector.eclipse.ui.team.ICompareAnnotationModel;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.compare.internal.MergeSourceViewer;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.AnnotationBarHoverManager;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
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
@SuppressWarnings("restriction")
public class CrucibleCompareAnnotationModel implements ICompareAnnotationModel {

	private static SourceViewer getSourceViewer(MergeSourceViewer sourceViewer) {
		if (SourceViewer.class.isInstance(sourceViewer)) {
			return SourceViewer.class.cast(sourceViewer);
		} else {
			Object returnValue;
			try {
				Method getSourceViewerRefl = MergeSourceViewer.class.getDeclaredMethod("getSourceViewer");
				getSourceViewerRefl.setAccessible(true);
				returnValue = getSourceViewerRefl.invoke(sourceViewer);
				if (returnValue instanceof SourceViewer) {
					return (SourceViewer) returnValue;
				}
			} catch (Exception e) {
				//ignore
			}
		}
		return null;
	}

	private final class CrucibleViewerTextInputListener implements ITextInputListener, ICrucibleCompareSourceViewer {
		private final SourceViewer sourceViewer;

		private final CrucibleAnnotationModel crucibleAnnotationModel;

		private final boolean oldFile;

		private final MergeSourceViewer mergeSourceViewer;

		private CrucibleViewerTextInputListener(MergeSourceViewer sourceViewer,
				CrucibleAnnotationModel crucibleAnnotationModel, boolean oldFile) {
			this.sourceViewer = getSourceViewer(sourceViewer);
			this.mergeSourceViewer = sourceViewer;
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

						sourceViewer.getTextWidget().addMouseListener(new MouseAdapter() {
							@Override
							public void mouseDown(MouseEvent e) {
								// ignore
								int offset = ((TextSelection) sourceViewer.getSelection()).getOffset();
								CrucibleUiUtil.highlightAnnotationInRichEditor(offset, crucibleAnnotationModel);
							}
						});

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

		public void focusOnLines(int startLine, int endLine) {
			// ignore
			if (sourceViewer != null) {
				IDocument document = sourceViewer.getDocument();
				if (document != null) {
					try {
						int offset = document.getLineOffset(startLine);
						int length = document.getLineOffset(endLine) - offset;
						StyledText widget = sourceViewer.getTextWidget();
						widget.setRedraw(false);
						sourceViewer.revealRange(offset, length);
						widget.setRedraw(true);
					} catch (BadLocationException e) {
						StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
					}
				}
			}
		}

		public void registerContextMenu() {
			if (CrucibleUtil.canAddCommentToReview(review)) {
				AddLineCommentToFileAction addLineCommentAction = new AddLineCommentToFileAction(this,
						crucibleAnnotationModel.getCrucibleFile());
				addLineCommentAction.setImageDescriptor(CrucibleImages.ADD_COMMENT);
				AddGeneralCommentToFileAction addGeneralCommentAction = new AddGeneralCommentToFileAction();
				addGeneralCommentAction.setCrucibleFile(crucibleAnnotationModel.getCrucibleFile());
				addGeneralCommentAction.setReview(review);

				if (sourceViewer != null) {
					sourceViewer.addSelectionChangedListener(addLineCommentAction);
					sourceViewer.addSelectionChangedListener(addGeneralCommentAction);
				}
				mergeSourceViewer.addTextAction(addLineCommentAction);
				mergeSourceViewer.addTextAction(addGeneralCommentAction);
			}
		}

		public LineRange getSelection() {
			if (sourceViewer != null) {
				TextSelection selection = (TextSelection) sourceViewer.getSelection();
				return new LineRange(selection.getStartLine() + 1, selection.getEndLine() - selection.getStartLine());
			}
			return null;
		}
	}

	private final CrucibleAnnotationModel leftAnnotationModel;

	private final CrucibleAnnotationModel rightAnnotationModel;

	private final Review review;

	private CrucibleViewerTextInputListener leftViewerListener;

	private CrucibleViewerTextInputListener rightViewerListener;

	private final VersionedComment commentToFocus;

	public CrucibleCompareAnnotationModel(CrucibleFileInfo crucibleFile, Review review, VersionedComment commentToFocus) {
		super();
		this.review = review;
		this.leftAnnotationModel = new CrucibleAnnotationModel(null, null, null, new CrucibleFile(crucibleFile, false),
				review);
		this.rightAnnotationModel = new CrucibleAnnotationModel(null, null, null, new CrucibleFile(crucibleFile, true),
				review);
		this.commentToFocus = commentToFocus;
	}

	public void attachToViewer(final MergeSourceViewer fLeft, final MergeSourceViewer fRight) {

		leftViewerListener = addTextInputListener(fLeft, leftAnnotationModel, false);
		rightViewerListener = addTextInputListener(fRight, rightAnnotationModel, true);
	}

	private CrucibleViewerTextInputListener addTextInputListener(final MergeSourceViewer sourceViewer,
			final CrucibleAnnotationModel crucibleAnnotationModel, boolean oldFile) {
		CrucibleViewerTextInputListener listener = new CrucibleViewerTextInputListener(sourceViewer,
				crucibleAnnotationModel, oldFile);
		SourceViewer viewer = getSourceViewer(sourceViewer);
		if (viewer != null) {
			viewer.addTextInputListener(listener);
		}
		return listener;
	}

	public void updateCrucibleFile(Review newReview) {
		CrucibleFile leftOldFile = leftAnnotationModel.getCrucibleFile();
		CrucibleFile rightOldFile = rightAnnotationModel.getCrucibleFile();
		try {
			CrucibleFileInfo newLeftFileInfo = newReview.getFileByPermId(leftOldFile.getCrucibleFileInfo().getPermId());
			CrucibleFileInfo newRightFileInfo = newReview.getFileByPermId(rightOldFile.getCrucibleFileInfo()
					.getPermId());
			if (newLeftFileInfo != null && newRightFileInfo != null) {
				leftAnnotationModel.updateCrucibleFile(new CrucibleFile(newLeftFileInfo, leftOldFile.isOldFile()),
						newReview);
				rightAnnotationModel.updateCrucibleFile(new CrucibleFile(newRightFileInfo, rightOldFile.isOldFile()),
						newReview);
			}
		} catch (ValueNotYetInitialized e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}
	}

	public void focusOnComment() {
		if (commentToFocus != null) {
			//ignore general file comment
			if (!commentToFocus.isFromLineInfo() && !commentToFocus.isToLineInfo()) {
				return;
			}
			boolean isOldFile = commentToFocus.isFromLineInfo();

			int startLine = isOldFile ? commentToFocus.getFromStartLine() : commentToFocus.getToStartLine();

			int endLine = isOldFile ? commentToFocus.getFromEndLine() : commentToFocus.getToEndLine();

			if (endLine == 0 || endLine > startLine) {
				endLine = startLine;
			}
			if (startLine != 0) {
				startLine--;
			}
			//get the correct listener (new file is left)
			CrucibleViewerTextInputListener listener = isOldFile ? rightViewerListener : leftViewerListener;
			listener.focusOnLines(startLine, endLine);
		}
	}

	public void registerContextMenu() {
		rightViewerListener.registerContextMenu(); //context menu in old revision disabled - not supported by API
		leftViewerListener.registerContextMenu();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((leftAnnotationModel == null) ? 0 : leftAnnotationModel.hashCode());
		result = prime * result + ((rightAnnotationModel == null) ? 0 : rightAnnotationModel.hashCode());
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
		CrucibleCompareAnnotationModel other = (CrucibleCompareAnnotationModel) obj;
		if (leftAnnotationModel == null) {
			if (other.leftAnnotationModel != null) {
				return false;
			}
		} else if (!leftAnnotationModel.equals(other.leftAnnotationModel)) {
			return false;
		}
		if (rightAnnotationModel == null) {
			if (other.rightAnnotationModel != null) {
				return false;
			}
		} else if (!rightAnnotationModel.equals(other.rightAnnotationModel)) {
			return false;
		}
		return true;
	}

}
