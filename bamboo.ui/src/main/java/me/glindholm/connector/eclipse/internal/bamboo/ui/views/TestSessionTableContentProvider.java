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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import me.glindholm.connector.eclipse.internal.bamboo.ui.model.ITestElement;
import me.glindholm.connector.eclipse.internal.bamboo.ui.model.TestCaseElement;
import me.glindholm.connector.eclipse.internal.bamboo.ui.model.TestRoot;
import me.glindholm.connector.eclipse.internal.bamboo.ui.model.TestSuiteElement;

public class TestSessionTableContentProvider implements IStructuredContentProvider {

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public Object[] getElements(Object inputElement) {
        List<ITestElement> all = new ArrayList<>();
        addAll(all, (TestRoot) inputElement);
        return all.toArray();
    }

    private void addAll(List<ITestElement> all, TestSuiteElement suite) {
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

    @Override
    public void dispose() {
    }
}
