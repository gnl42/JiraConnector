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

package com.atlassian.connector.eclipse.ui.viewers;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;

import java.util.Iterator;

public class ExpandCollapseSelectionAction extends Action {

	private final Viewer viewer;

	private final boolean isExpandMode;

	public ExpandCollapseSelectionAction(Viewer viewer, boolean isExpandMode) {
		this.viewer = viewer;
		this.isExpandMode = isExpandMode;
		if (isExpandMode) {
			setImageDescriptor(CommonImages.EXPAND_ALL);
			setText("Expand Selection");
			setToolTipText("Expand Selection");
		} else {
			setImageDescriptor(CommonImages.COLLAPSE_ALL);
			setText("Collapse Selection");
			setToolTipText("Collapse Selection");
		}
	}

	@Override
	public void run() {
		if (viewer != null && !viewer.getControl().isDisposed() && (viewer instanceof AbstractTreeViewer)) {
			viewer.getControl().setRedraw(false);

			if (viewer.getSelection() instanceof ITreeSelection) {
				TreePath[] paths = ((ITreeSelection) viewer.getSelection()).getPaths();
				if (paths != null) {
					for (TreePath path : paths) {
						if (isExpandMode) {
							((AbstractTreeViewer) viewer).expandToLevel(path, AbstractTreeViewer.ALL_LEVELS);
						} else {
							((AbstractTreeViewer) viewer).collapseToLevel(path, AbstractTreeViewer.ALL_LEVELS);
						}
					}
				}
			} else if (viewer.getSelection() instanceof IStructuredSelection) {

				for (Iterator<?> it = ((IStructuredSelection) viewer.getSelection()).iterator(); it.hasNext();) {
					((AbstractTreeViewer) viewer).expandToLevel(it.next(), AbstractTreeViewer.ALL_LEVELS);
				}
			}
			viewer.getControl().setRedraw(true);
		}
	}
}
