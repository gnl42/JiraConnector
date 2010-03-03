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
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.AddGeneralCommentToFileAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.AddLineCommentToFileAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.ruler.CommentAnnotationRulerColumn;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.compare.internal.MergeSourceViewer;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.AnnotationBarHoverManager;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.internal.texteditor.AnnotationColumn;
import org.eclipse.ui.internal.texteditor.PropertyEventDispatcher;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

/**
 * Model for annotations in the diff view.
 * 
 * @author Thomas Ehrnhoefer
 */
@SuppressWarnings("restriction")
public class CrucibleCompareAnnotationModel {

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
				// ignore
			}
		}
		return null;
	}

	private final class CrucibleViewerTextInputListener implements ITextInputListener, ICrucibleCompareSourceViewer {
		private final class ColoringLineBackgroundListener implements LineBackgroundListener {
			private final StyledText styledText;

			private Color colorCommented;

			private PropertyEventDispatcher fDispatcher;

			private ColoringLineBackgroundListener(StyledText styledText) {
				this.styledText = styledText;
				initialize();
			}

			private void updateCommentedColor(AnnotationPreference pref, IPreferenceStore store) {
				if (pref != null) {
					RGB rgb = CommentAnnotationRulerColumn.getColorFromAnnotationPreference(store, pref);
					colorCommented = getSharedColors().getColor(rgb);
				}
			}

			private void initialize() {
				final IPreferenceStore store = EditorsUI.getPreferenceStore();
				if (store == null) {
					return;
				}

				AnnotationPreferenceLookup lookup = EditorsUI.getAnnotationPreferenceLookup();
				final AnnotationPreference commentedPref = lookup.getAnnotationPreference(CrucibleCommentAnnotation.COMMENT_ANNOTATION_ID);

				updateCommentedColor(commentedPref, store);

				fDispatcher = new PropertyEventDispatcher(store);

				if (commentedPref != null) {
					fDispatcher.addPropertyChangeListener(commentedPref.getColorPreferenceKey(),
							new IPropertyChangeListener() {
								public void propertyChange(PropertyChangeEvent event) {
									updateCommentedColor(commentedPref, store);
								}
							});
				}
			}

			public void lineGetBackground(LineBackgroundEvent event) {
				int documentOffset = 0;
				documentOffset = getDocumentOffset(event);
				int lineNr = styledText.getLineAtOffset(event.lineOffset) + 1 + documentOffset;
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
							event.lineBackground = colorCommented;
						}
					}
				}
			}

			/**
			 * Galileo hack to deal with slaveDocuments (when clicking on java structure elements). The styledText will
			 * not contain the whole text anymore, so our line numbering is off
			 * 
			 * @param event
			 * @return
			 */
			private int getDocumentOffset(LineBackgroundEvent event) {
				/*
				 * there is no access to DefaultDocumentAdapter and thus the (master or slave) document.. so we have to assume
				 * that on first call this event actually has the full text. this text, and the text of the current styled text
				 * will be used to calculate the offset
				 */
				if (event.widget instanceof StyledText) {
					String currentText = ((StyledText) event.widget).getText();
					if (initialText == null) {
						initialText = currentText;
						// since it is initial call, offset should be 0 anyway
						return 0;
					}
					// if text is unchanged, offset it 0
					if (currentText.equals(initialText)) {
						return 0;
					}
					// current text is different, check if it is contained in initialText
					if (initialText.contains(currentText)) {
						// calculate the offset
						int charoffset = initialText.indexOf(currentText);
						int lineOffset = 0;
						String delimiter = ((StyledText) event.widget).getLineDelimiter();
						for (String line : initialText.split(delimiter)) {
							if (charoffset > 0) {
								charoffset -= (line.length() + delimiter.length());
								lineOffset++;
							} else {
								break;
							}
						}
						return lineOffset;
					} else {
						// log error since we assume the initial text contains all slaveTexts.
						StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
								"Could not find text offset for annotation highlighting"
										+ " - current text not contained in initial text."));
					}
				}
				return 0;
			}
		}

		private final SourceViewer sourceViewer;

		private final CrucibleAnnotationModel crucibleAnnotationModel;

		private final boolean oldFile;

		private final MergeSourceViewer mergeSourceViewer;

		private AddLineCommentToFileAction addLineCommentAction;

		private AddGeneralCommentToFileAction addGeneralCommentAction;

		private String initialText;

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
						declaredField2.setAccessible(true);
						Method declaredMethod = sourceViewerClazz.getDeclaredMethod("createVisualAnnotationModel",
								IAnnotationModel.class);
						declaredMethod.setAccessible(true);
						annotationModel = (IAnnotationModel) declaredMethod.invoke(sourceViewer,
								crucibleAnnotationModel);
						declaredField2.set(sourceViewer, annotationModel);
						annotationModel.connect(newInput);
						sourceViewer.showAnnotations(true);

						crucibleAnnotationModel.setEditorDocument(sourceViewer.getDocument());
						createVerticalRuler(newInput, sourceViewerClazz);
						// createOverviewRuler(newInput, sourceViewerClazz);
						createHighlighting(sourceViewerClazz);
					} catch (Throwable t) {
						StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
								"Error attaching Crucible annotation model", t));
					}
				}
			}
		}

		private void createHighlighting(Class<SourceViewer> sourceViewerClazz) throws IllegalArgumentException,
				IllegalAccessException, SecurityException, NoSuchFieldException {
			// TODO this could use some performance tweaks
			final StyledText styledText = sourceViewer.getTextWidget();
			styledText.addLineBackgroundListener(new ColoringLineBackgroundListener(styledText));
		}

		/*
		 * overview ruler problem: displayed in both viewers. the diff editor ruler is actually custom drawn (see
		 * TextMergeViewer.fBirdsEyeCanvas) the ruler that gets created in this method is longer than the editor, meaning its
		 * not an overview (not next to the scrollbar)
		 */
		@SuppressWarnings("unused")
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
			// overviewRuler is null

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

			forceCustomAnnotationHover();

			Method declaredMethod2 = sourceViewerClazz.getDeclaredMethod("getVerticalRuler");
			declaredMethod2.setAccessible(true);
			CompositeRuler ruler = (CompositeRuler) declaredMethod2.invoke(sourceViewer);
			boolean hasDecorator = false;

			Iterator<?> iter = (ruler).getDecoratorIterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof AnnotationColumn) {
					hasDecorator = true;
				}
			}

			if (!hasDecorator) {
				AnnotationColumn annotationColumn = new AnnotationColumn();
				annotationColumn.createControl(ruler, ruler.getControl().getParent());
				ruler.addDecorator(0, annotationColumn);
			}
		}

		public void forceCustomAnnotationHover() throws NoSuchFieldException, IllegalAccessException {
			Class<SourceViewer> sourceViewerClazz = SourceViewer.class;
			sourceViewer.setAnnotationHover(new CrucibleAnnotationHover(null));

			// FIXME: hack for e3.5
			try {
				Field hoverControlCreator = TextViewer.class.getDeclaredField("fHoverControlCreator");
				hoverControlCreator.setAccessible(true);
				hoverControlCreator.set(sourceViewer, new CrucibleInformationControlCreator());
			} catch (Throwable t) {
				// ignore as it may not exist in other versions
			}

			// FIXME: hack for e3.5
			try {
				Method ensureMethod = sourceViewerClazz.getDeclaredMethod("ensureAnnotationHoverManagerInstalled");
				ensureMethod.setAccessible(true);
				ensureMethod.invoke(sourceViewer);
			} catch (Throwable t) {
				// ignore as it may not exist in other versions
			}

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
		}

		public void focusOnLines(IntRanges range) {
			// editors count lines from 0, Crucible counts from 1
			final int startLine = range.getTotalMin() - 1;
			final int endLine = range.getTotalMax() - 1;
			if (sourceViewer != null) {
				IDocument document = sourceViewer.getDocument();
				if (document != null) {
					try {
						int offset = document.getLineOffset(startLine);
						int length = document.getLineOffset(endLine) - offset;
						StyledText widget = sourceViewer.getTextWidget();
						try {
							widget.setRedraw(false);
							sourceViewer.revealRange(offset, length);
							sourceViewer.setSelectedRange(offset, 0);
							// widget.setFocus();
						} finally {
							widget.setRedraw(true);
						}
					} catch (BadLocationException e) {
						StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
					}
				}
			}
		}

		public void registerContextMenu() {
			if (CrucibleUtil.canAddCommentToReview(review) && addLineCommentAction == null
					&& addGeneralCommentAction == null) {
				addLineCommentAction = new AddLineCommentToFileAction(this, crucibleAnnotationModel.getCrucibleFile());
				addLineCommentAction.setImageDescriptor(CrucibleImages.ADD_COMMENT);
				addGeneralCommentAction = new AddGeneralCommentToFileAction();
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

		public boolean isListenerFor(MergeSourceViewer viewer, CrucibleAnnotationModel annotationModel) {
			return this.mergeSourceViewer == viewer && this.crucibleAnnotationModel == annotationModel;
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
		/*
		 * only create listeners if they are not already existing
		 */
		if (!isListenerFor(leftViewerListener, fLeft, leftAnnotationModel)) {
			leftViewerListener = addTextInputListener(fLeft, leftAnnotationModel, false);
		} else {
			/*
			 * Using asyncExec here because if the underlying slaveDocument (part of the file that gets displayed when clicking
			 * on a java structure in the compare editor) is changed, but the master document is not, we do not get any event
			 * afterwards that would give us a place to hook our code to override the annotationHover. Since all is done in the
			 * UI thread, using this asyncExec hack works because the unconfigure and configure of the document is finished and
			 * our hover-hack stays.
			 */
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					try {
						// if listeners exist, just make sure the hover hack is in there
						leftViewerListener.forceCustomAnnotationHover();
					} catch (Exception e) {
						StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
								"Error attaching Crucible annotation hover", e));
					}
				}
			});
		}
		if (!isListenerFor(rightViewerListener, fRight, rightAnnotationModel)) {
			rightViewerListener = addTextInputListener(fRight, rightAnnotationModel, true);
		} else {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					try {
						// if listeners exist, just make sure the hover hack is in there
						rightViewerListener.forceCustomAnnotationHover();
					} catch (Exception e) {
						StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
								"Error attaching Crucible annotation hover", e));
					}
				}
			});
		}
	}

	private boolean isListenerFor(CrucibleViewerTextInputListener listener, MergeSourceViewer viewer,
			CrucibleAnnotationModel annotationModel) {
		if (listener == null) {
			return false;
		}
		return listener.isListenerFor(viewer, annotationModel);
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
		CrucibleFileInfo newLeftFileInfo = newReview.getFileByPermId(leftOldFile.getCrucibleFileInfo().getPermId());
		CrucibleFileInfo newRightFileInfo = newReview.getFileByPermId(rightOldFile.getCrucibleFileInfo().getPermId());
		if (newLeftFileInfo != null && newRightFileInfo != null) {
			leftAnnotationModel.updateCrucibleFile(new CrucibleFile(newLeftFileInfo, leftOldFile.isOldFile()),
					newReview);
			rightAnnotationModel.updateCrucibleFile(new CrucibleFile(newRightFileInfo, rightOldFile.isOldFile()),
					newReview);
		}
	}

	public void focusOnComment() {
		focusOnComment(commentToFocus);
	}

	public void focusOnComment(VersionedComment commentToFocus) {
		if (commentToFocus != null) {
			CrucibleFile leftFile = leftAnnotationModel.getCrucibleFile();
			VersionedVirtualFile virtualLeft = leftFile.getSelectedFile();

			CrucibleFile rightFile = rightAnnotationModel.getCrucibleFile();
			VersionedVirtualFile virtualRight = rightFile.getSelectedFile();

			Map<String, IntRanges> lineRanges = commentToFocus.getLineRanges();
			if (lineRanges != null) {
				IntRanges range;
				if ((range = lineRanges.get(virtualLeft.getRevision())) != null) {
					// get the correct listener (new file is left)
					leftViewerListener.focusOnLines(range);
				} else if ((range = lineRanges.get(virtualRight.getRevision())) != null) {
					rightViewerListener.focusOnLines(range);
				}
			}
		}
	}

	public void registerContextMenu() {
		rightViewerListener.registerContextMenu();
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

	private ISharedTextColors getSharedColors() {
		return EditorsUI.getSharedTextColors();
	}

}
