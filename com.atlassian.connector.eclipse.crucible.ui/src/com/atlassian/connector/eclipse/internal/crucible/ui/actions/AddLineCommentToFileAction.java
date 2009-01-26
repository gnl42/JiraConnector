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

package com.atlassian.connector.eclipse.internal.crucible.ui.actions;

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.connector.eclipse.ui.team.TeamUiUtils;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

/**
 * Action for adding a comment to a line in the active review
 * 
 * @author Shawn Minto
 */
public class AddLineCommentToFileAction extends BaseSelectionListenerAction implements IWorkbenchWindowActionDelegate {

	private LineRange selectedRange = null;

	private CrucibleFile crucibleFile = null;

	private IWorkbenchWindow workbenchWindow;

	public AddLineCommentToFileAction() {
		super("Create Comment");
	}

	public void dispose() {
		// ignore

	}

	public void init(IWorkbenchWindow window) {
		this.workbenchWindow = window;

	}

	public void run(IAction action) {
		// TODO ask the user for the comment and post it
//		System.out.println(selectedRange.getStartLine() + " " + selectedRange.getNumberOfLines()
//				+ crucibleFile.getCrucibleFileInfo().getFileDescriptor().getAbsoluteUrl());
	}

	public void selectionChanged(IAction action, ISelection selection) {

		IEditorPart editorPart = getActiveEditor();
		IEditorInput editorInput = getEditorInputFromSelection(selection);
		if (editorInput != null && editorPart != null) {
			selectedRange = TeamUiUtils.getSelectedLineNumberRangeFromEditorInput(editorPart, editorInput);

			if (selectedRange != null) {
				crucibleFile = TeamUiUtils.getCorrespondingCrucibleFileFromEditorInput(editorInput,
						CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview());
				if (crucibleFile != null) {
					action.setEnabled(true);
					setEnabled(true);
					return;
				}
			}
		}
		action.setEnabled(false);
		setEnabled(false);
		selectedRange = null;
		crucibleFile = null;

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
