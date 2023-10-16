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

package me.glindholm.connector.eclipse.ui.commons;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class EditorResourceAdapterFactory implements IAdapterFactory {

    private final class BasicEditorResource implements IEditorResource {
        private final IResource resource;

        private final LineRange lineRange;

        public BasicEditorResource(final IResource resource, final LineRange lineRange) {
            this.resource = resource;
            this.lineRange = lineRange;
        }

        private BasicEditorResource(final IResource resource) {
            this(resource, null);
        }

        @Override
        public LineRange getLineRange() {
            return lineRange;
        }

        @Override
        public IResource getResource() {
            return resource;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object getAdapter(final Class adapter) {
            if (!IResource.class.equals(adapter)) {
                return null;
            }

            return resource;
        }
    }

    private static final Class[] ADAPTERS = { IEditorResource.class };

    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(final Object adaptableObject, final Class adapterType) {
        if (!IEditorResource.class.equals(adapterType)) {
            return null;
        }

        if (adaptableObject instanceof IResource) {
            return new BasicEditorResource((IResource) adaptableObject);
        }

        if (adaptableObject instanceof final IEditorInput editorInput) {
            final IResource resource = editorInput.getAdapter(IResource.class);
            if (resource == null) {
                return null;
            }
            final IEditorPart editorPart = getActiveEditor();

            // such call:
            // lineRange = new LineRange(textSelection.getStartLine(), textSelection.getEndLine()
            // - textSelection.getStartLine());
            // does not work (i.e. it returns previously selected text region rather than selected now ?!?
            final LineRange lineRange = JiraConnectorUiUtil.getSelectedLineNumberRangeFromEditorInput(editorPart, editorInput);
            return new BasicEditorResource(resource, lineRange);
        }

        if (adaptableObject instanceof final IAdaptable adaptable) {
            final IResource resource = adaptable.getAdapter(IResource.class);
            if (resource != null) {
                return new BasicEditorResource(resource);
            }
        }

        return null;
    }

    private IEditorPart getActiveEditor() {
        IWorkbenchWindow window = null;
        if (window == null) {
            window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        }
        if (window != null && window.getActivePage() != null) {
            return window.getActivePage().getActiveEditor();
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class[] getAdapterList() {
        return ADAPTERS;
    }

}
