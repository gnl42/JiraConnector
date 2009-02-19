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

package com.atlassian.connector.eclipse.internal.crucible.ui;

import com.atlassian.connector.eclipse.internal.crucible.ui.annotations.CrucibleCommentPopupDialog;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHoverExtension;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

import java.lang.reflect.Method;

public class FocusTooltipHandler extends AbstractHandler {

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

	public Object execute(ExecutionEvent event) throws ExecutionException {

		//TODO get current annotationHover as well as the sourceViewer of the current annotation hover

		//TODO call makeAnnotationsHoverFocusable

		return null;
	}

	/**
	 * Tries to make an annotation hover focusable (or "sticky").
	 * 
	 * @param sourceViewer
	 *            the source viewer to display the hover over
	 * @param annotationHover
	 *            the hover to make focusable
	 * @return <code>true</code> if successful, <code>false</code> otherwise
	 */
	private boolean makeAnnotationHoverFocusable(ISourceViewer sourceViewer, IAnnotationHover annotationHover) {
		IVerticalRulerInfo info = null;
		try {
			Method declaredMethod2 = SourceViewer.class.getDeclaredMethod("getVerticalRuler");
			declaredMethod2.setAccessible(true);
			info = (CompositeRuler) declaredMethod2.invoke(sourceViewer);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			if (annotationHover instanceof IAnnotationHoverExtension) {
				IAnnotationHoverExtension extension = (IAnnotationHoverExtension) annotationHover;
				ILineRange hoverLineRange = extension.getHoverLineRange(sourceViewer, line);
				if (hoverLineRange == null) {
					return false;
				}
				final int maxVisibleLines = Integer.MAX_VALUE;
				hoverInfo = extension.getHoverInfo(sourceViewer, hoverLineRange, maxVisibleLines);
			} else {
				hoverInfo = annotationHover.getHoverInfo(sourceViewer, line);
			}

			// hover region: the beginning of the concerned line to place the control right over the line
			IDocument document = sourceViewer.getDocument();
			int offset = document.getLineOffset(line);
			String partitioning = new TextSourceViewerConfiguration().getConfiguredDocumentPartitioning(sourceViewer);
			String contentType = TextUtilities.getContentType(document, partitioning, offset, true);

			IInformationControlCreator controlCreator = null;
			if (annotationHover instanceof IInformationProviderExtension2) {
				controlCreator = ((IInformationProviderExtension2) annotationHover).getInformationPresenterControlCreator();
			} else if (annotationHover instanceof IAnnotationHoverExtension) {
				controlCreator = ((IAnnotationHoverExtension) annotationHover).getHoverControlCreator();
			}

			IInformationProvider informationProvider = new InformationProvider(new Region(offset, 0), hoverInfo,
					controlCreator);

			CrucibleCommentPopupDialog dialog = CrucibleCommentPopupDialog.getCurrentPopupDialog();
			if (dialog != null) {

				InformationPresenter fInformationPresenter = dialog.getInformationControl().getInformationPresenter();
				fInformationPresenter.setSizeConstraints(100, 12, true, true);
				fInformationPresenter.install(sourceViewer);
				fInformationPresenter.setDocumentPartitioning(partitioning);
				fInformationPresenter.setOffset(offset);
				fInformationPresenter.setAnchor(AbstractInformationControlManager.ANCHOR_RIGHT);
				fInformationPresenter.setMargins(4, 0); // AnnotationBarHoverManager sets (5,0), minus SourceViewer.GAP_SIZE_1
				fInformationPresenter.setInformationProvider(informationProvider, contentType);
				fInformationPresenter.showInformation();

				return true;
			}

		} catch (BadLocationException e) {
			return false;
		}
		return false;
	}

}
