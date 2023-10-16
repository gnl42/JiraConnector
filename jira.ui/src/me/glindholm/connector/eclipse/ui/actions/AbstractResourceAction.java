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

package me.glindholm.connector.eclipse.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

import me.glindholm.connector.eclipse.ui.commons.IEditorResource;
import me.glindholm.connector.eclipse.ui.commons.JiraConnectorUiUtil;
import me.glindholm.connector.eclipse.ui.commons.ResourceEditorBean;

public abstract class AbstractResourceAction extends BaseSelectionListenerAction implements IActionDelegate {

    private IWorkbenchWindow workbenchWindow;

    private List<ResourceEditorBean> selectionData;

    protected AbstractResourceAction(final String text) {
        super(text);
    }

    protected List<ResourceEditorBean> getSelectionData() {
        return selectionData;
    }

    public void dispose() {
    }

    public void init(final IWorkbenchWindow window) {
        workbenchWindow = window;
    }

    @Override
    public void run(final IAction action) {
        if (selectionData != null && selectionData.get(0) != null) {
            processResources(selectionData, WorkbenchUtil.getShell());
        }
    }

    private List<ResourceEditorBean> getData(final ISelection selection) {
        final List<ResourceEditorBean> ret = new ArrayList<>();
        if (selection instanceof final IStructuredSelection structuredSelection) {
            final Object[] selectedObjects = structuredSelection.toArray();

            for (final Object selectedObject : selectedObjects) {

                if (selectedObject instanceof final IEditorResource a) {
                    ret.add(new ResourceEditorBean(a.getResource(), a.getLineRange()));

                } else if (structuredSelection.getFirstElement() instanceof IAdaptable) {
                    IResource resource = null;
                    resource = ((IAdaptable) structuredSelection.getFirstElement()).getAdapter(IResource.class);
                    LineRange lineRange = getJavaEditorSelection(structuredSelection);
                    ret.add(new ResourceEditorBean(resource, lineRange));
                }
            }
        } else {
            final IEditorPart activeEditor = getActiveEditor();
            if (activeEditor != null) {
                final IEditorInput editorInput = getEditorInputFromSelection(selection);
                if (editorInput != null) {
                    IResource resource = null;
                    resource = editorInput.getAdapter(IResource.class);
                    // such call:
                    // lineRange = new LineRange(textSelection.getStartLine(),
                    // textSelection.getEndLine()
                    // - textSelection.getStartLine());
                    // does not work (i.e. it returns previously selected text region rather than
                    // selected now ?!?
                    LineRange lineRange = JiraConnectorUiUtil.getSelectedLineNumberRangeFromEditorInput(activeEditor, activeEditor.getEditorInput());
                    ret.add(new ResourceEditorBean(resource, lineRange));
                }
            }
        }
        return ret;
    }

    @Override
    protected boolean updateSelection(final IStructuredSelection selection) {
        selectionData = getData(selection);
        return super.updateSelection(selection);
    }

    @Override
    public void selectionChanged(final IAction action, final ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            selectionChanged((IStructuredSelection) selection);
        } else {
            selectionChanged(StructuredSelection.EMPTY);
        }
        action.setEnabled(isEnabled());
    }

    private IEditorPart getActiveEditor() {
        IWorkbenchWindow window = workbenchWindow;
        if (window == null) {
            window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        }
        if (window != null && window.getActivePage() != null) {
            return window.getActivePage().getActiveEditor();
        }
        return null;
    }

    private IEditorInput getEditorInputFromSelection(final ISelection selection) {
        if (selection instanceof final IStructuredSelection structuredSelection) {
            if (structuredSelection.getFirstElement() instanceof IEditorInput) {
                return (IEditorInput) structuredSelection.getFirstElement();
            }
        }
        return null;
    }

    @Nullable
    private LineRange getJavaEditorSelection(final ISelection selection) {
        final IEditorPart editorPart = getActiveEditor();
        final IEditorInput editorInput = getEditorInputFromSelection(selection);
        if (editorInput != null && editorPart != null) {
            return JiraConnectorUiUtil.getSelectedLineNumberRangeFromEditorInput(editorPart, editorInput);
        }
        return null;
    }

    protected abstract void processResources(@NonNull List<ResourceEditorBean> selection, final Shell shell);

}
