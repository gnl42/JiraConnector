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

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Abstract Action for opening a Java editor.
 */
public abstract class OpenEditorAction extends Action {
    protected String fClassName;

    protected TestResultsView fTestRunner;

    private final boolean fActivate;

    protected OpenEditorAction(TestResultsView testRunner, String testClassName) {
        this(testRunner, testClassName, true);
    }

    public OpenEditorAction(TestResultsView testRunner, String className, boolean activate) {
        super("&Go to File");
        fClassName = className;
        fTestRunner = testRunner;
        fActivate = activate;
    }

    /*
     * @see IAction#run()
     */
    @Override
    public void run() {
        ITextEditor textEditor = null;
        try {
            IJavaElement element = findElement(fClassName);
            if (element == null) {
                MessageDialog.openError(getShell(), "Cannot Open Editor", "Test class not found in selected project");
                return;
            }
            textEditor = (ITextEditor) JavaUI.openInEditor(element, fActivate, false);
        } catch (CoreException e) {
            ErrorDialog.openError(getShell(), "Error", "Cannot open editor", e.getStatus());
            return;
        }
        if (textEditor == null) {
            fTestRunner.registerInfoMessage("Cannot open editor");
            return;
        }
        reveal(textEditor);
    }

    protected Shell getShell() {
        return fTestRunner.getSite().getShell();
    }

    protected String getClassName() {
        return fClassName;
    }

    protected abstract IJavaElement findElement(String className) throws CoreException;

    protected abstract void reveal(ITextEditor editor);

    protected IType findType(String fullyQualifiedName) throws JavaModelException {
        final IJavaProject[] projects = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProjects();
        for (IJavaProject project : projects) {
            final IType itype = project.findType(fullyQualifiedName);
            if (itype != null && itype.getResource().getProject().equals(project.getProject())) {
                return itype;
            }
        }
        return null;
    }

    private IType internalFindType(IJavaProject project, String className, Set<IJavaProject> visitedProjects,
            IProgressMonitor monitor) throws JavaModelException {
        try {
            if (visitedProjects.contains(project)) {
                return null;
            }
            monitor.beginTask("", 2); //$NON-NLS-1$
            IType type = project.findType(className, SubMonitor.convert(monitor, 1));
            if (type != null) {
                return type;
            }
            //fix for bug 87492: visit required projects explicitly to also find not exported types
            visitedProjects.add(project);
            IJavaModel javaModel = project.getJavaModel();
            String[] requiredProjectNames = project.getRequiredProjectNames();
            IProgressMonitor reqMonitor = SubMonitor.convert(monitor, 1);
            reqMonitor.beginTask("", requiredProjectNames.length); //$NON-NLS-1$
            for (String requiredProjectName : requiredProjectNames) {
                IJavaProject requiredProject = javaModel.getJavaProject(requiredProjectName);
                if (requiredProject.exists()) {
                    type = internalFindType(requiredProject, className, visitedProjects, SubMonitor.convert(
                            reqMonitor, 1));
                    if (type != null) {
                        return type;
                    }
                }
            }
            return null;
        } finally {
            monitor.done();
        }
    }

}
