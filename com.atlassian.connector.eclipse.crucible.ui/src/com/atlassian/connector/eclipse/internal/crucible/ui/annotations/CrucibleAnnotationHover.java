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
import org.eclipse.core.commands.Command;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHoverExtension;
import org.eclipse.jface.text.source.IAnnotationHoverExtension2;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.projection.AnnotationBag;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class to determine the annotations to show the hover for. This class delegates to a parent hover if it exists.
 * 
 * @author Shawn Minto
 */
public class CrucibleAnnotationHover implements IAnnotationHover, IAnnotationHoverExtension, IAnnotationHoverExtension2 {

	private final IAnnotationHover parentHover;

	private final IInformationControlCreator informationControlCreator;

	private static ISourceViewer currentSourceViewer;

	private static CrucibleAnnotationHover currentAnnotationHover;

	public CrucibleAnnotationHover(IAnnotationHover hover) {
		this.parentHover = hover;
		informationControlCreator = new CrucibleInformationControlCreator();
	}

	public void dispose() {
		// ignore for now
	}

	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		List<CrucibleCommentAnnotation> commentAnnotations = getCrucibleAnnotationsForLine(sourceViewer, lineNumber);
		if (commentAnnotations != null && commentAnnotations.size() > 0) {

			if (commentAnnotations.size() == 1) {
				CrucibleCommentAnnotation annotation = commentAnnotations.get(0);
				String message = annotation.getText();
				if (message != null && message.trim().length() > 0) {
					return formatSingleMessage(message);
				}

			} else {

				List<String> messages = new ArrayList<String>();
				for (CrucibleCommentAnnotation annotation : commentAnnotations) {
					String message = annotation.getText();
					if (message != null && message.trim().length() > 0) {
						messages.add(message.trim());
					}
				}

				if (messages.size() == 1) {
					return formatSingleMessage(messages.get(0));
				}

				if (messages.size() > 1) {
					return formatMultipleMessages(messages);
				}
			}
		} else {
			if (parentHover != null) {
				return parentHover.getHoverInfo(sourceViewer, lineNumber);
			}
		}
		return null;
	}

	public IInformationControlCreator getHoverControlCreator() {
		return informationControlCreator;
	}

	public boolean canHandleMouseCursor() {
		return true;
	}

	public boolean canHandleMouseWheel() {
		return true; // does not work on Ubuntu, but it should be here (maybe works on Windows ;))
	}

	public Object getHoverInfo(ISourceViewer sourceViewer, ILineRange lineRange, int visibleNumberOfLines) {
		List<CrucibleCommentAnnotation> annotationsForLine = getCrucibleAnnotationsForLine(sourceViewer,
				lineRange.getStartLine());
		if (annotationsForLine == null || annotationsForLine.size() == 0) {
			return getHoverInfo(sourceViewer, lineRange.getStartLine());
		} else {
			return new CrucibleAnnotationHoverInput(annotationsForLine);
		}
	}

	public ILineRange getHoverLineRange(ISourceViewer viewer, int lineNumber) {
		currentAnnotationHover = this;
		currentSourceViewer = viewer;
		List<CrucibleCommentAnnotation> commentAnnotations = getCrucibleAnnotationsForLine(viewer, lineNumber);
		if (commentAnnotations != null && commentAnnotations.size() > 0) {
			IDocument document = viewer.getDocument();
			int lowestStart = Integer.MAX_VALUE;
			int highestEnd = 0;
			for (Annotation a : commentAnnotations) {
				if (a instanceof CrucibleCommentAnnotation) {
					Position p = ((CrucibleCommentAnnotation) a).getPosition();
					try {

						int start = document.getLineOfOffset(p.offset);
						int end = document.getLineOfOffset(p.offset + p.length);

						if (start < lowestStart) {
							lowestStart = start;
						}

						if (end > highestEnd) {
							highestEnd = end;
						}
					} catch (BadLocationException e) {
						// ignore
					}
				}
			}
			if (lowestStart != Integer.MAX_VALUE) {
				return new LineRange(lowestStart, highestEnd - lowestStart);
			} else {
				return new LineRange(lineNumber, 1);
			}
		}

		return new LineRange(lineNumber, 1);
	}

	@SuppressWarnings("restriction")
	protected String formatSingleMessage(String message) {
		StringBuffer buffer = new StringBuffer();
		HTMLPrinter.addPageProlog(buffer);
		HTMLPrinter.addParagraph(buffer, HTMLPrinter.convertToHTMLContent(message));
		HTMLPrinter.addPageEpilog(buffer);
		return buffer.toString();
	}

	@SuppressWarnings("restriction")
	protected String formatMultipleMessages(List<String> messages) {
		StringBuffer buffer = new StringBuffer();
		HTMLPrinter.addPageProlog(buffer);
		HTMLPrinter.addParagraph(buffer, HTMLPrinter.convertToHTMLContent("There are multiple comments on this line"));

		HTMLPrinter.startBulletList(buffer);
		for (String message : messages) {
			HTMLPrinter.addBullet(buffer, HTMLPrinter.convertToHTMLContent(message));
		}
		HTMLPrinter.endBulletList(buffer);

		HTMLPrinter.addPageEpilog(buffer);
		return buffer.toString();
	}

	private boolean isRulerLine(Position position, IDocument document, int line) {
		if (position.getOffset() > -1 && position.getLength() > -1) {
			try {
				return line == document.getLineOfOffset(position.getOffset());
			} catch (BadLocationException x) {
				// ignore
			}
		}
		return false;
	}

	private IAnnotationModel getAnnotationModel(ISourceViewer viewer) {
		if (viewer instanceof ISourceViewerExtension2) {
			ISourceViewerExtension2 extension = (ISourceViewerExtension2) viewer;
			return extension.getVisualAnnotationModel();
		}
		return viewer.getAnnotationModel();
	}

	private boolean includeAnnotation(Annotation annotation, Position position,
			List<CrucibleCommentAnnotation> annotations) {
		if (!(annotation instanceof CrucibleCommentAnnotation)) {
			return false;
		}

		return (annotation != null && !annotations.contains(annotation));
	}

	@SuppressWarnings("unchecked")
	private List<CrucibleCommentAnnotation> getCrucibleAnnotationsForLine(ISourceViewer viewer, int line) {
		IAnnotationModel model = getAnnotationModel(viewer);
		if (model == null) {
			return null;
		}

		IDocument document = viewer.getDocument();
		List<CrucibleCommentAnnotation> commentAnnotations = new ArrayList<CrucibleCommentAnnotation>();
		Iterator<Annotation> iterator = model.getAnnotationIterator();

		while (iterator.hasNext()) {
			Annotation annotation = iterator.next();

			Position position = model.getPosition(annotation);
			if (position == null) {
				continue;
			}

			if (!isRulerLine(position, document, line)) {
				continue;
			}

			if (annotation instanceof AnnotationBag) {
				AnnotationBag bag = (AnnotationBag) annotation;
				Iterator<Annotation> e = bag.iterator();
				while (e.hasNext()) {
					annotation = e.next();
					position = model.getPosition(annotation);
					if (position != null && includeAnnotation(annotation, position, commentAnnotations)
							&& annotation instanceof CrucibleCommentAnnotation) {
						commentAnnotations.add((CrucibleCommentAnnotation) annotation);
					}
				}
				continue;
			}

			if (includeAnnotation(annotation, position, commentAnnotations)
					&& annotation instanceof CrucibleCommentAnnotation) {
				commentAnnotations.add((CrucibleCommentAnnotation) annotation);
			}
		}

		return commentAnnotations;
	}

	/**
	 * Tries to make an annotation hover focusable (or "sticky").
	 * 
	 * @return <code>true</code> if successful, <code>false</code> otherwise
	 */
	public static boolean makeAnnotationHoverFocusable() {
		// check sourceviewer and hover
		if (currentSourceViewer == null || currentSourceViewer.getTextWidget().isDisposed()
				|| currentAnnotationHover == null) {
			return false;
		}

		IVerticalRulerInfo info = null;
		try {
			Method declaredMethod2 = SourceViewer.class.getDeclaredMethod("getVerticalRuler");
			declaredMethod2.setAccessible(true);
			info = (CompositeRuler) declaredMethod2.invoke(currentSourceViewer);
		} catch (Exception e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
					"Error getting CompareEditor's vertical ruler. ", e));
		}

		if (info == null) {
			return false;
		}

		int line = info.getLineOfLastMouseButtonActivity();
		if (line == -1) {
			return false;
		}

		try {

			// compute the hover information
			Object hoverInfo;
			if (currentAnnotationHover instanceof IAnnotationHoverExtension) {
				IAnnotationHoverExtension extension = currentAnnotationHover;
				ILineRange hoverLineRange = extension.getHoverLineRange(currentSourceViewer, line);
				if (hoverLineRange == null) {
					return false;
				}
				final int maxVisibleLines = Integer.MAX_VALUE;
				hoverInfo = extension.getHoverInfo(currentSourceViewer, hoverLineRange, maxVisibleLines);
			} else {
				hoverInfo = currentAnnotationHover.getHoverInfo(currentSourceViewer, line);
			}

			// hover region: the beginning of the concerned line to place the control right over the line
			IDocument document = currentSourceViewer.getDocument();
			int offset = document.getLineOffset(line);
			String partitioning = new TextSourceViewerConfiguration().getConfiguredDocumentPartitioning(currentSourceViewer);
			String contentType = TextUtilities.getContentType(document, partitioning, offset, true);

			IInformationControlCreator controlCreator = null;
			if (currentAnnotationHover instanceof IInformationProviderExtension2) {
				IInformationProviderExtension2 provider = (IInformationProviderExtension2) currentAnnotationHover;
				controlCreator = provider.getInformationPresenterControlCreator();
			} else if (currentAnnotationHover instanceof IAnnotationHoverExtension) {
				controlCreator = ((IAnnotationHoverExtension) currentAnnotationHover).getHoverControlCreator();
			}

			IInformationProvider informationProvider = new InformationProvider(new Region(offset, 0), hoverInfo,
					controlCreator);

			CrucibleCommentPopupDialog dialog = CrucibleCommentPopupDialog.getCurrentPopupDialog();
			if (dialog != null) {

				InformationPresenter fInformationPresenter = dialog.getInformationControl().getInformationPresenter();
				fInformationPresenter.setSizeConstraints(100, 12, true, true);
				fInformationPresenter.install(currentSourceViewer);
				fInformationPresenter.setDocumentPartitioning(partitioning);
				fInformationPresenter.setOffset(offset);
				fInformationPresenter.setAnchor(AbstractInformationControlManager.ANCHOR_RIGHT);
				fInformationPresenter.setMargins(4, 0); // AnnotationBarHoverManager sets (5,0), minus SourceViewer.GAP_SIZE_1
				fInformationPresenter.setInformationProvider(informationProvider, contentType);
				fInformationPresenter.showInformation();

				// remove our own handler as F2 focus handler
				ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(
						ICommandService.class);
				Command showInfoCommand = commandService.getCommand(ITextEditorActionDefinitionIds.SHOW_INFORMATION);
				showInfoCommand.setHandler(null);

				return true;
			}

		} catch (BadLocationException e) {
			return false;
		}
		return false;
	}

	/**
	 * Information provider used to present focusable information shells.
	 * 
	 * @since 3.3
	 */
	private static final class InformationProvider implements IInformationProvider, IInformationProviderExtension,
			IInformationProviderExtension2 {

		private final IRegion fHoverRegion;

		private final Object fHoverInfo;

		private final IInformationControlCreator fControlCreator;

		InformationProvider(IRegion hoverRegion, Object hoverInfo, IInformationControlCreator controlCreator) {
			fHoverRegion = hoverRegion;
			fHoverInfo = hoverInfo;
			fControlCreator = controlCreator;
		}

		public IRegion getSubject(ITextViewer textViewer, int invocationOffset) {
			return fHoverRegion;
		}

		@Deprecated
		public String getInformation(ITextViewer textViewer, IRegion subject) {
			return fHoverInfo.toString();
		}

		public Object getInformation2(ITextViewer textViewer, IRegion subject) {
			return fHoverInfo;
		}

		public IInformationControlCreator getInformationPresenterControlCreator() {
			return fControlCreator;
		}
	}

}
