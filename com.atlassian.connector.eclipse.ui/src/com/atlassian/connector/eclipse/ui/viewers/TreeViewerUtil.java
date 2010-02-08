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

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;

public final class TreeViewerUtil {
	private TreeViewerUtil() {
	}

	/**
	 * Select any input element (even if it's hidden and not managed by {@link AbstractTreeViewer}
	 * 
	 * @param viewer
	 * @param showElement
	 */
	public static void setSelection(AbstractTreeViewer viewer, Object showElement) {
		Object[] expanded = viewer.getExpandedElements();
		try {
			viewer.getControl().setRedraw(false);
			viewer.expandAll();
			viewer.setSelection(new StructuredSelection(showElement), true);
			ITreeSelection treeSelection = (ITreeSelection) viewer.getSelection();
			if (treeSelection.getPaths() != null && treeSelection.getPaths().length > 0) {
				Object[] toBeExpanded = new Object[expanded.length + treeSelection.getPaths()[0].getSegmentCount()];
				System.arraycopy(expanded, 0, toBeExpanded, 0, expanded.length);
				for (int i = 0, s = treeSelection.getPaths()[0].getSegmentCount(); i < s; ++i) {
					toBeExpanded[expanded.length + i] = treeSelection.getPaths()[0].getSegment(i);
				}
				viewer.setExpandedElements(toBeExpanded);
			}
		} finally {
			viewer.getControl().setRedraw(true);
		}
	}
}
