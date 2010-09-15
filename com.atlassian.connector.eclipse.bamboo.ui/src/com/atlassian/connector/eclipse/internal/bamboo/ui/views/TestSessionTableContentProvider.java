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

package com.atlassian.connector.eclipse.internal.bamboo.ui.views;

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

import com.atlassian.connector.eclipse.internal.bamboo.ui.model.ITestElement;
import com.atlassian.connector.eclipse.internal.bamboo.ui.model.TestCaseElement;
import com.atlassian.connector.eclipse.internal.bamboo.ui.model.TestRoot;
import com.atlassian.connector.eclipse.internal.bamboo.ui.model.TestSuiteElement;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import java.util.ArrayList;

public class TestSessionTableContentProvider implements IStructuredContentProvider {

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public Object[] getElements(Object inputElement) {
		ArrayList all = new ArrayList();
		addAll(all, (TestRoot) inputElement);
		return all.toArray();
	}

	private void addAll(ArrayList all, TestSuiteElement suite) {
		ITestElement[] children = suite.getChildren();
		for (ITestElement element : children) {
			if (element instanceof TestSuiteElement) {
				if (((TestSuiteElement) element).getSuiteStatus().isError()) {
					all.add(element); // add failed suite to flat list too
				}
				addAll(all, (TestSuiteElement) element);
			} else if (element instanceof TestCaseElement) {
				all.add(element);
			}
		}
	}

	public void dispose() {
	}
}
