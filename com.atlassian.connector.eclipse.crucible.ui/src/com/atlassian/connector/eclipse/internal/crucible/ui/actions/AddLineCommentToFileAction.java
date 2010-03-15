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
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.ICrucibleFileProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.annotations.ICrucibleCompareSourceViewer;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.connector.eclipse.ui.AtlassianUiPlugin;
import com.atlassian.connector.eclipse.ui.commons.AtlassianUiUtil;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
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

	private IResource file;

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

		selectedRange = null;
		crucibleFile = null;

		if (action.isEnabled() && isEnabled()) {
			IEditorPart editorPart = getActiveEditor();
			IEditorInput editorInput = getEditorInputFromSelection(selection);
			if (editorPart != null && editorInput instanceof ICrucibleFileProvider) {
				crucibleFile = ((ICrucibleFileProvider) editorInput).getCrucibleFile();
				if (crucibleCompareSourceViewer == null) {
					getJavaEditorSelection(selection);
				} else {
					selectedRange = crucibleCompareSourceViewer.getSelection();
				}
				if (selectedRange != null && crucibleFile != null && CrucibleUtil.canAddCommentToReview(getReview())
						&& CrucibleUiUtil.isFilePartOfActiveReview(crucibleFile)) {
					return;
				}
			} else if (getReview() != null && editorInput != null) {
				IResource resource = (IResource) editorInput.getAdapter(IResource.class);

				if (resource instanceof IFile) {

					// try to find file in the review
					// TODO jj add support for pre-commit files (content compare required) 
					CrucibleFile cruFile = CrucibleUiUtil.getCrucibleFileFromResource(resource, getReview());
					if (cruFile != null) {
						crucibleFile = cruFile;
					} else {
						file = resource;
					}

					if (crucibleCompareSourceViewer == null) {
						getJavaEditorSelection(selection);
					} else {
						selectedRange = crucibleCompareSourceViewer.getSelection();
					}
				} else {
					action.setEnabled(false);
					setEnabled(false);
					return;
				}
			}
		}

		if (crucibleFile == null && file == null) {
			action.setEnabled(false);
			setEnabled(false);
		}
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

	private void getJavaEditorSelection(ISelection selection) {
		IEditorPart editorPart = getActiveEditor();
		IEditorInput editorInput = getEditorInputFromSelection(selection);
		if (editorPart != null) {
			selectedRange = AtlassianUiUtil.getSelectedLineNumberRangeFromEditorInput(editorPart, editorInput);
			if (selectedRange == null) {
				StatusHandler.log(new Status(IStatus.INFO, AtlassianUiPlugin.PLUGIN_ID,
						"Editor is not an ITextEditor or there's no text selection available."));
			}
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
	public String getToolTipText() {
		return "Add Line Comment...";
	}

	@Override
	protected LineRange getSelectedRange() {
		//if its the action from the compareeditor, get currently selected lines
		if (crucibleCompareSourceViewer != null) {
			return crucibleCompareSourceViewer.getSelection();
		} else {
			return selectedRange;
		}
	}

	@Override
	protected IResource getResource() {
		return file;
	}
}
