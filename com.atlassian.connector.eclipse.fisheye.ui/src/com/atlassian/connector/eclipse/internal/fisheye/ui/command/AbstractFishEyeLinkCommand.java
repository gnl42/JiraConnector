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

package com.atlassian.connector.eclipse.internal.fisheye.ui.command;

import com.atlassian.connector.eclipse.fisheye.ui.FishEyeUiUtil;
import com.atlassian.connector.eclipse.fisheye.ui.preferences.SourceRepositoryMappingPreferencePage;
import com.atlassian.connector.eclipse.internal.fisheye.ui.FishEyeUiPlugin;
import com.atlassian.connector.eclipse.internal.fisheye.ui.dialogs.ErrorDialogWithHyperlink;
import com.atlassian.connector.eclipse.ui.AtlassianUiPlugin;
import com.atlassian.connector.eclipse.ui.commons.AtlassianUiUtil;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractFishEyeLinkCommand extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IResource resource = null;
		LineRange lineRange = null;

		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			if (structuredSelection.getFirstElement() instanceof IAdaptable) {
				resource = (IResource) ((IAdaptable) structuredSelection.getFirstElement()).getAdapter(IResource.class);
				lineRange = getSelectedLineRange(event);
			}
		} else {
			final IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
			if (activeEditor != null) {
				resource = (IResource) activeEditor.getEditorInput().getAdapter(IResource.class);
				// such call:
				//				lineRange = new LineRange(textSelection.getStartLine(), textSelection.getEndLine()
				//						- textSelection.getStartLine());
				// does not work (i.e. it returns previously selected text region rather than selected now ?!?
				lineRange = AtlassianUiUtil.getSelectedLineNumberRangeFromEditorInput(activeEditor,
						activeEditor.getEditorInput());
				if (lineRange == null) {
					StatusHandler.log(new Status(IStatus.INFO, AtlassianUiPlugin.PLUGIN_ID,
							"Editor is not an ITextEditor or there's no text selection available."));
				}
			}
		}
		if (resource != null) {
			processResource(resource, lineRange, HandlerUtil.getActiveShell(event));
		}

		return null;
	}

	protected abstract void processUrl(@NotNull String url);

	private void processResource(IResource resource, LineRange lineRange, final Shell shell) {
		try {
			final String url = FishEyeUiUtil.buildFishEyeUrl(resource, lineRange);
			if (url != null) {
				processUrl(url);
			}

		} catch (CoreException e) {
			new ErrorDialogWithHyperlink(shell, "Error", "Cannot build FishEye URL for " + resource.getName() + ": "
					+ e.getMessage(), "<a>Configure FishEye Settings</a>", new Runnable() {
				public void run() {
					final PreferenceDialog prefDialog = PreferencesUtil.createPreferenceDialogOn(shell,
							SourceRepositoryMappingPreferencePage.ID, null, null);
					if (prefDialog != null) {
						prefDialog.open();
					}
				}
			}).open();
		}
	}

	private LineRange getSelectedLineRange(ExecutionEvent event) {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (window == null) {
			StatusHandler.log(new Status(IStatus.WARNING, FishEyeUiPlugin.PLUGIN_ID,
					"Cannot find active workbench window"));
			return null;
		}
		IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
		if (editorPart == null) {
			editorPart = getActiveEditor(window);
		}
		if (editorPart == null) {
			//			StatusHandler.log(new Status(IStatus.`, FishEyeUiPlugin.PLUGIN_ID,
			//					"Cannot find active editor"));
			return null;
		}

		IEditorInput editorInput = getEditorInputFromSelection(HandlerUtil.getCurrentSelection(event));
		if (editorInput != null && editorPart != null) {
			return AtlassianUiUtil.getSelectedLineNumberRangeFromEditorInput(editorPart, editorInput);
		}
		return null;
	}

	private IEditorPart getActiveEditor(IWorkbenchWindow window) {
		//		IWorkbenchWindow window = workbenchWindow;
		//		if (window == null) {
		//			window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		//		}
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

}