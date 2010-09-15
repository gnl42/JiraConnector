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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.annotations.ICrucibleCompareSourceViewer;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.connector.eclipse.ui.AtlassianUiPlugin;
import com.atlassian.connector.eclipse.ui.commons.AtlassianUiUtil;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 * Action for adding a comment to a line in the active review
 * 
 * @author Shawn Minto
 */
public class AddLineCommentToFileAction extends AbstractAddCommentAction {

	private LineRange selectedRange = null;

	private CrucibleFile crucibleFile = null;

	private ICrucibleCompareSourceViewer crucibleCompareSourceViewer;

	private IEditorInput editorInput;

	public AddLineCommentToFileAction() {
		super("Create Line Comment...");
	}

	public AddLineCommentToFileAction(ICrucibleCompareSourceViewer crucibleCompareSourceViewer,
			CrucibleFile crucibleFile) {
		this();
		this.crucibleCompareSourceViewer = crucibleCompareSourceViewer;
		this.crucibleFile = crucibleFile;
	}

	@Override
	protected String getDialogTitle() {
		return "Create Line Comment";
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);

		editorInput = getEditorInputFromSelection(selection);

	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (crucibleCompareSourceViewer != null) {
			selectedRange = crucibleCompareSourceViewer.getSelection();
			if (selectedRange != null && crucibleFile != null && CrucibleUtil.canAddCommentToReview(getReview())
					&& CrucibleUiUtil.isFilePartOfActiveReview(crucibleFile)) {
				return true;
			}
		}
		return false;
	}

	private LineRange getJavaEditorSelection(IEditorInput editorInput) {
		LineRange lines = null;

		IEditorPart editorPart = getActiveEditor();

		if (editorPart != null && editorInput != null) {
			lines = AtlassianUiUtil.getSelectedLineNumberRangeFromEditorInput(editorPart, editorInput);
			if (lines == null) {
				StatusHandler.log(new Status(IStatus.INFO, AtlassianUiPlugin.PLUGIN_ID,
						"Editor is not an ITextEditor or there's no text selection available."));
			}
		}

		return lines;
	}

	@Override
	protected CrucibleFile getCrucibleFile() {
		return crucibleFile;
	}

	@Override
	public String getToolTipText() {
		return "Add Line Comment...";
	}

	@Override
	protected LineRange getSelectedRange() {
		//if its the action from the compareeditor, get currently selected lines
		if (crucibleCompareSourceViewer != null) {
			return crucibleCompareSourceViewer.getSelection();
		} else {
			return getJavaEditorSelection(getEditorInput());
		}
	}

	@Override
	protected IEditorInput getEditorInput() {
		return editorInput;
	}

}
