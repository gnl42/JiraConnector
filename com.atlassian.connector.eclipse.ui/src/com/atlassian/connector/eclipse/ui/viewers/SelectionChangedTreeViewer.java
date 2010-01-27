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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;

public class SelectionChangedTreeViewer extends TreeViewer {

	public SelectionChangedTreeViewer(Composite parent, int style) {
		super(parent, style);
	}

	public SelectionChangedTreeViewer(Composite parent) {
		super(parent);
	}

	@Override
	public void setSelection(ISelection selection) {
		super.setSelection(selection);
		super.fireSelectionChanged(new SelectionChangedEvent(this, selection));
	}

}
