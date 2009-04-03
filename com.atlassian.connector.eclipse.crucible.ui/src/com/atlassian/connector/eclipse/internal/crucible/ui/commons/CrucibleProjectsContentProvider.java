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

package com.atlassian.connector.eclipse.internal.crucible.ui.commons;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import java.util.Collection;

/**
 * ContentProvider for Crucible Projects
 * 
 * @author Thomas Ehrnhoefer
 */
public class CrucibleProjectsContentProvider implements ITreeContentProvider {

	public Object[] getChildren(Object parentElement) {
		return new Object[0];
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return false;
	}

	@SuppressWarnings("unchecked")
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Collection) {
			return ((Collection) inputElement).toArray();
		}
		return new Object[0];
	}

	public void dispose() {
		// ignore
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// ignore
	}

}
