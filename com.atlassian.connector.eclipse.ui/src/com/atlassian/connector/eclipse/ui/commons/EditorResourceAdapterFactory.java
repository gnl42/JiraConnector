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

package com.atlassian.connector.eclipse.ui.commons;


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

		public BasicEditorResource(IResource resource, LineRange lineRange) {
			this.resource = resource;
			this.lineRange = lineRange;
		}

		private BasicEditorResource(IResource resource) {
			this(resource, null);
		}

		public LineRange getLineRange() {
			return lineRange;
		}

		public IResource getResource() {
			return resource;
		}

		@SuppressWarnings("unchecked")
		public Object getAdapter(Class adapter) {
			if (!IResource.class.equals(adapter)) {
				return null;
			}

			return resource;
		}
	}

	@SuppressWarnings("unchecked")
	private static final Class[] ADAPTERS = { IEditorResource.class };

	@SuppressWarnings("unchecked")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (!IEditorResource.class.equals(adapterType)) {
			return null;
		}

		if (adaptableObject instanceof IResource) {
			return new BasicEditorResource((IResource) adaptableObject);
		}

		if (adaptableObject instanceof IEditorInput) {
			final IEditorInput editorInput = (IEditorInput) adaptableObject;
			final IResource resource = (IResource) editorInput.getAdapter(IResource.class);
			if (resource == null) {
				return null;
			}
			IEditorPart editorPart = getActiveEditor();

			// such call:
			//				lineRange = new LineRange(textSelection.getStartLine(), textSelection.getEndLine()
			//						- textSelection.getStartLine());
			// does not work (i.e. it returns previously selected text region rather than selected now ?!?
			final LineRange lineRange = AtlassianUiUtil.getSelectedLineNumberRangeFromEditorInput(editorPart,
					editorInput);
			return new BasicEditorResource(resource, lineRange);
		}

		if (adaptableObject instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) adaptableObject;
			final IResource resource = (IResource) adaptable.getAdapter(IResource.class);
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

	@SuppressWarnings("unchecked")
	public Class[] getAdapterList() {
		return ADAPTERS;
	}

}
