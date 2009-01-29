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
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.viewers.ISelection;
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

	public AddLineCommentToFileAction() {
		super("Create Line Comment");
	}

	@Override
	protected String getDialogTitle() {
		return getText();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		if (action.isEnabled() && isEnabled()) {
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
		} else {
			action.setEnabled(false);
			setEnabled(false);
			selectedRange = null;
			crucibleFile = null;
		}
	}

	@Override
	protected Review getReview() {
		return CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
	}

	@Override
	protected CrucibleFile getCrucibleFile() {
		return crucibleFile;
	}

	@Override
	protected LineRange getSelectedRange() {
		return selectedRange;
	}
}
