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

package com.atlassian.connector.eclipse.ui.team;

import com.atlassian.connector.eclipse.ui.IAnnotationCompareInput;

import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.internal.ui.history.CompareFileRevisionEditorInput;
import org.eclipse.team.internal.ui.history.FileRevisionTypedElement;
import org.eclipse.ui.IWorkbenchPage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Custom Editor Input to circumvent the encapsulation of the CompareFileRevisionEditorInput in the MyDiffNode inner
 * class
 * 
 * @author Thomas Ehrnhoefer
 */
//class uses a whole lot of restricted API without us being able to do something against it
@SuppressWarnings("restriction")
public class TeamCompareFileRevisionEditorInput extends CompareFileRevisionEditorInput implements
		IAnnotationCompareInput {

	public class TeamMyDiffNode extends MyDiffNode {

		public TeamMyDiffNode(ITypedElement left, ITypedElement right) {
			super(left, right);
		}

		public TeamCompareFileRevisionEditorInput getEnclosingInput() {
			return TeamCompareFileRevisionEditorInput.this;
		}

	}

	private final ICompareAnnotationModel annotationModelToAttach;

	public TeamCompareFileRevisionEditorInput(ITypedElement left, ITypedElement right, IWorkbenchPage page,
			ICompareAnnotationModel annotationModelToAttach) {
		super(left, right, page);
		this.annotationModelToAttach = annotationModelToAttach;
	}

	@Override
	public Viewer findContentViewer(Viewer oldViewer, ICompareInput input, Composite parent) {
		Viewer contentViewer = super.findContentViewer(oldViewer, input, parent);
		return TeamUiUtils.findContentViewer(contentViewer, input, parent, annotationModelToAttach);
	}

	public ICompareAnnotationModel getAnnotationModelToAttach() {
		return annotationModelToAttach;
	}

	@Override
	protected ICompareInput prepareCompareInput(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		ICompareInput input = createCompareInput();
		getCompareConfiguration().setLeftEditable(false);
		getCompareConfiguration().setRightEditable(false);
		Class<CompareFileRevisionEditorInput> parentInput = CompareFileRevisionEditorInput.class;
		try {
			Method declaredMethod = parentInput.getDeclaredMethod("ensureContentsCached",
					FileRevisionTypedElement.class, FileRevisionTypedElement.class, IProgressMonitor.class);
			declaredMethod.setAccessible(true);
			declaredMethod.invoke(this, getLeftRevision(), getRightRevision(), monitor);
			declaredMethod = parentInput.getDeclaredMethod("initLabels", ICompareInput.class);
			declaredMethod.setAccessible(true);
			declaredMethod.invoke(this, input);
		} catch (Exception e) {
			return super.prepareCompareInput(monitor);
		}
		return input;
	}

	private ICompareInput createCompareInput() {
		return new TeamMyDiffNode(getLeft(), getRightRevision());
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
		TeamCompareFileRevisionEditorInput other = (TeamCompareFileRevisionEditorInput) obj;
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