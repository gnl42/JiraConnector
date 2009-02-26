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

package com.atlassian.connector.eclipse.internal.subclipse.ui.compare;

import com.atlassian.connector.eclipse.ui.IAnnotationCompareInput;
import com.atlassian.connector.eclipse.ui.team.ICompareAnnotationModel;

import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.internal.MergeSourceViewer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.tigris.subversion.subclipse.ui.compare.ResourceEditionNode;
import org.tigris.subversion.subclipse.ui.compare.SVNCompareEditorInput;

import java.lang.reflect.Field;

public class CrucibleSubclipseCompareEditorInput extends SVNCompareEditorInput implements IAnnotationCompareInput {

	private final ICompareAnnotationModel annotationModelToAttach;

	public CrucibleSubclipseCompareEditorInput(ResourceEditionNode left, ResourceEditionNode right,
			ICompareAnnotationModel annotationModelToAttach) {
		super(left, right);
		this.annotationModelToAttach = annotationModelToAttach;
	}

	@Override
	public Viewer findContentViewer(Viewer oldViewer, ICompareInput input, Composite parent) {
		Viewer contentViewer = super.findContentViewer(oldViewer, input, parent);
		if (contentViewer instanceof TextMergeViewer) {
			TextMergeViewer textMergeViewer = (TextMergeViewer) contentViewer;
			try {
				Class clazz = TextMergeViewer.class;
				Field declaredField = clazz.getDeclaredField("fLeft");
				declaredField.setAccessible(true);
				final MergeSourceViewer fLeft = (MergeSourceViewer) declaredField.get(textMergeViewer);

				declaredField = clazz.getDeclaredField("fRight");
				declaredField.setAccessible(true);
				final MergeSourceViewer fRight = (MergeSourceViewer) declaredField.get(textMergeViewer);

				annotationModelToAttach.attachToViewer(fLeft, fRight);
			} catch (Throwable t) {
				t.printStackTrace();
			}

		}
		return contentViewer;
	}

	public ICompareAnnotationModel getAnnotationModelToAttach() {
		return annotationModelToAttach;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotationModelToAttach == null) ? 0 : annotationModelToAttach.hashCode());
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
		CrucibleSubclipseCompareEditorInput other = (CrucibleSubclipseCompareEditorInput) obj;
		if (annotationModelToAttach == null) {
			if (other.annotationModelToAttach != null) {
				return false;
			}
		} else if (!annotationModelToAttach.equals(other.annotationModelToAttach)) {
			return false;
		}
		return true;
	}

}
