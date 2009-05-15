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

import com.atlassian.connector.eclipse.internal.fisheye.ui.FishEyeUiPlugin;
import com.atlassian.connector.eclipse.ui.team.TeamUiUtils;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class CopyFishEyeLinkToClipboardCommand extends AbstractHandler {

//	public static String buildFishEyeUrl(RevisionInfo revisionInfo, LineRange lineRange) throws IOException {
//		final String scmPath = revisionInfo.getScmPath();
//		final String cfgScmPath = FishEyeUiPlugin.getDefault().getFishEyeUrl();
//		if (scmPath.startsWith(cfgScmPath) == false) {
//			throw new IOException("Cannot find matching FishEye repository for " + revisionInfo);
//		}
//		String path = scmPath.substring(cfgScmPath.length());
//		String repo = FishEyeUiPlugin.getDefault().getFishEyeRepo();
//		String fishEyeUrl = "https://studio.atlassian.com/source";
//		StringBuilder res = new StringBuilder(fishEyeUrl);
//		if (res.length() > 0 && res.charAt(res.length() - 1) != '/') {
//			res.append('/');
//		}
//		res.append("browse/");
//		res.append(repo);
//		res.append(path);
//		if (revisionInfo.getRevision() != null) {
//			res.append("?r=");
//			res.append(revisionInfo.getRevision());
//		}
//		if (lineRange != null) {
//			res.append("#l").append(lineRange.getStartLine());
//		}
//		return res.toString();
//	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IResource resource = null;
		LineRange lineRange = null;

		ISelection selection = HandlerUtil.getCurrentSelection(event);
/*		if (selection instanceof TextSelection) {
			final TextSelection textSelection = (TextSelection) selection;
		} else*/
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			if (structuredSelection.getFirstElement() instanceof IAdaptable) {
				resource = (IResource) ((IAdaptable) structuredSelection.getFirstElement()).getAdapter(IResource.class);
				lineRange = getSelectedLineRange(event);
			}
//			Display.getDefault().asyncExec(new Runnable() {
//				public void run() {
//					MessageDialog.openInformation(null, "mytitle", structuredSelection.toString());
//				}
//
//			});
		} else {
			final IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
			if (activeEditor != null) {
				resource = (IResource) activeEditor.getEditorInput().getAdapter(IResource.class);
				// such call:
				//				lineRange = new LineRange(textSelection.getStartLine(), textSelection.getEndLine()
				//						- textSelection.getStartLine());
				// does not work (i.e. it returns previously selected text region rather than selected now ?!?
				lineRange = TeamUiUtils.getSelectedLineNumberRangeFromEditorInput(activeEditor,
						activeEditor.getEditorInput());
			}
		}
		if (resource != null) {
			processResource(resource, lineRange);
		}

		return null;
	}

	private void processResource(IResource resource, LineRange lineRange) {
		try {
			final String url = FishEyeUiPlugin.getDefault().getFishEyeSettingsManager().buildFishEyeUrl(resource,
					lineRange);
			if (url != null) {
				//RevisionInfo revisionInfo = TeamUiUtils.getLocalRevision(resource);
				TasksUiUtil.openUrl(url);
			}

		} catch (CoreException e) {
			ErrorDialog.openError(null, FishEyeUiPlugin.PLUGIN_ID, "Cannot open " + resource.getName()
					+ " in FishEye web UI", e.getStatus());
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
			return TeamUiUtils.getSelectedLineNumberRangeFromEditorInput(editorPart, editorInput);
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
