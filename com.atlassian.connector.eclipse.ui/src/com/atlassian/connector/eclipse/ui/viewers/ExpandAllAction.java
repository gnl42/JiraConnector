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
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;

public class ExpandAllAction extends Action {

	private final Viewer viewer;

	public ExpandAllAction(Viewer viewer) {
		this.viewer = viewer;
		setImageDescriptor(CommonImages.EXPAND_ALL);
		setText("Expand All");
		setToolTipText("Expand All");
	}

	@Override
	public void run() {
		if (viewer != null && !viewer.getControl().isDisposed() && (viewer instanceof AbstractTreeViewer)) {
			viewer.getControl().setRedraw(false);
			((AbstractTreeViewer) viewer).expandAll();
			viewer.getControl().setRedraw(true);
		}
	}
}
