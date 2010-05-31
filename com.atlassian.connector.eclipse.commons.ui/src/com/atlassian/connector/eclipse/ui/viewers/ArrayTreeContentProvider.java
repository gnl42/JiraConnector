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
/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.ui.viewers;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import java.util.Collection;

/**
 * Check ArrayContentProvider
 * 
 * @author Pawel Niewiadomski
 */
public class ArrayTreeContentProvider implements ITreeContentProvider {

	private static ArrayTreeContentProvider instance;

	public static ArrayTreeContentProvider getInstance() {
		synchronized (ArrayTreeContentProvider.class) {
			if (instance == null) {
				instance = new ArrayTreeContentProvider();
			}
			return instance;
		}
	}

	/**
	 * Returns the elements in the input, which must be either an array or a <code>Collection</code>.
	 */
	@SuppressWarnings("unchecked")
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Object[]) {
			return (Object[]) inputElement;
		}
		if (inputElement instanceof Collection) {
			return ((Collection) inputElement).toArray();
		}
		return new Object[0];
	}

	/**
	 * This implementation does nothing.
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// do nothing.
	}

	/**
	 * This implementation does nothing.
	 */
	public void dispose() {
		// do nothing.
	}

	public Object[] getChildren(Object parentElement) {
		return new Object[0];
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return false;
	}
}
