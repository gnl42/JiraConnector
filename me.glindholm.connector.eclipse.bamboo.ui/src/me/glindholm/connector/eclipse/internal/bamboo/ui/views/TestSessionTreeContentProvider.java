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

package me.glindholm.connector.eclipse.internal.bamboo.ui.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import me.glindholm.connector.eclipse.internal.bamboo.ui.model.TestElement;
import me.glindholm.connector.eclipse.internal.bamboo.ui.model.TestRoot;
import me.glindholm.connector.eclipse.internal.bamboo.ui.model.TestSuiteElement;

public class TestSessionTreeContentProvider implements ITreeContentProvider {

	private final Object[] NO_CHILDREN = new Object[0];

	public void dispose() {
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof TestSuiteElement) {
			return ((TestSuiteElement) parentElement).getChildren();
		} else {
			return NO_CHILDREN;
		}
	}

	public Object[] getElements(Object inputElement) {
		return ((TestRoot) inputElement).getChildren();
	}

	public Object getParent(Object element) {
		return ((TestElement) element).getParent();
	}

	public boolean hasChildren(Object element) {
		if (element instanceof TestSuiteElement) {
			return ((TestSuiteElement) element).getChildren().length != 0;
		} else {
			return false;
		}
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
