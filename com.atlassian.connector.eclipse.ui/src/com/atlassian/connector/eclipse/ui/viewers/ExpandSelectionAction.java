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

public class ExpandSelectionAction extends Action {

	private final Viewer viewer;

	public ExpandSelectionAction(Viewer viewer) {
		this.viewer = viewer;
		setImageDescriptor(CommonImages.EXPAND_ALL);
		setText("Expand Selection");
		setToolTipText("Expand Selection");
	}

	@Override
	public void run() {
		if (viewer != null && !viewer.getControl().isDisposed() && (viewer instanceof AbstractTreeViewer)) {
			viewer.getControl().setRedraw(false);

			if (viewer.getSelection() instanceof ITreeSelection) {
				TreePath[] paths = ((ITreeSelection) viewer.getSelection()).getPaths();
				if (paths != null) {
					for (TreePath path : paths) {
						((AbstractTreeViewer) viewer).expandToLevel(path, AbstractTreeViewer.ALL_LEVELS);
					}
				}
			} else if (viewer.getSelection() instanceof IStructuredSelection) {
				((AbstractTreeViewer) viewer).expandToLevel(
						((IStructuredSelection) viewer.getSelection()).getFirstElement(), AbstractTreeViewer.ALL_LEVELS);
			}
			viewer.getControl().setRedraw(true);
		}
	}
}
