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

package com.atlassian.connector.eclipse.internal.fisheye.ui.action;

import com.atlassian.connector.eclipse.fisheye.ui.IFishEyeResource;
import com.atlassian.connector.eclipse.team.ui.TeamUiUtils;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("restriction")
public abstract class AbstractResourceAction extends BaseSelectionListenerAction {

	// TODO jj move it up
	public static final class ResourceEditorBean {
		private final IResource resource;

		private final LineRange lineRange;

		private ResourceEditorBean(IResource resource, LineRange lineRange) {
			this.resource = resource;
			this.lineRange = lineRange;
		}

		public IResource getResource() {
			return resource;
		}

		public LineRange getLineRange() {
			return lineRange;
		}
	}

	// TODO jj move to another location the whole action
	private IWorkbenchWindow workbenchWindow;

	private List<ResourceEditorBean> selectionData;

	protected AbstractResourceAction(String text) {
		super(text);
		// ignore
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.workbenchWindow = window;
	}

	public void run(IAction action) {
		if (selectionData != null && selectionData.get(0) != null) {
			processResources(selectionData, WorkbenchUtil.getShell());
		}
	}

	private List<ResourceEditorBean> getData(ISelection selection) {
		List<ResourceEditorBean> ret = new ArrayList<ResourceEditorBean>();
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;

			Object[] selectedObjects = structuredSelection.toArray();

			for (Object selectedObject : selectedObjects) {

				if (selectedObject instanceof IFishEyeResource) {
					IFishEyeResource a = (IFishEyeResource) selectedObject;
					ret.add(new ResourceEditorBean(a.getResource(), a.getLineRange()));

				} else if (structuredSelection.getFirstElement() instanceof IAdaptable) {
					IResource resource = null;
					LineRange lineRange = null;
					resource = (IResource) ((IAdaptable) structuredSelection.getFirstElement()).getAdapter(IResource.class);
					lineRange = getJavaEditorSelection(structuredSelection);
					ret.add(new ResourceEditorBean(resource, lineRange));
				}
			}
		} else {
			IEditorPart activeEditor = getActiveEditor();
			if (activeEditor != null) {
				IEditorInput editorInput = getEditorInputFromSelection(selection);
				if (editorInput != null) {
					IResource resource = null;
					LineRange lineRange = null;
					resource = (IResource) editorInput.getAdapter(IResource.class);
					// such call:
					//				lineRange = new LineRange(textSelection.getStartLine(), textSelection.getEndLine()
					//						- textSelection.getStartLine());
					// does not work (i.e. it returns previously selected text region rather than selected now ?!?
					lineRange = TeamUiUtils.getSelectedLineNumberRangeFromEditorInput(activeEditor,
							activeEditor.getEditorInput());
					ret.add(new ResourceEditorBean(resource, lineRange));
				}
			}
		}
		return ret;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (action.isEnabled()) {
			selectionData = getData(selection);
			selectionChanged(action, selectionData);
		} else {
			selectionData = null;
		}

	}

	protected abstract void selectionChanged(IAction action, List<ResourceEditorBean> selection);

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

	private IEditorInput getEditorInputFromSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = ((IStructuredSelection) selection);
			if (structuredSelection.getFirstElement() instanceof IEditorInput) {
				return (IEditorInput) structuredSelection.getFirstElement();
			}
		}
		return null;
	}

	@Nullable
	private LineRange getJavaEditorSelection(ISelection selection) {
		IEditorPart editorPart = getActiveEditor();
		IEditorInput editorInput = getEditorInputFromSelection(selection);
		if (editorInput != null && editorPart != null) {
			return TeamUiUtils.getSelectedLineNumberRangeFromEditorInput(editorPart, editorInput);
		}
		return null;
	}

	protected abstract void processResources(@NotNull List<ResourceEditorBean> selection, final Shell shell);

}
