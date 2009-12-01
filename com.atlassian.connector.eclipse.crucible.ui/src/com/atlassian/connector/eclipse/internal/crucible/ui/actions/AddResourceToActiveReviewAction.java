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

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleTeamUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.AddResourcesToReviewJob;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 * Action to add a file to the active review
 * 
 * @author Shawn Minto
 * @author Thomas Ehrnhoefer
 * @author Pawel Niewiadomski
 */
public class AddResourceToActiveReviewAction extends AbstractReviewAction {

	private IResource[] resources;

	public AddResourceToActiveReviewAction() {
		super("Add Resource to Active Review...");
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);

		//the following only applies if it is the action from the extension point
		if (action.isEnabled() && isEnabled()) {
			IEditorPart editorPart = getActiveEditor();
			IEditorInput editorInput = getEditorInputFromSelection(selection);
			if (editorInput != null && editorPart != null) {
				CrucibleFile crucibleFile = CrucibleTeamUiUtil.getCorrespondingCrucibleFileFromEditorInput(editorInput,
						CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview());
				if (crucibleFile == null || !CrucibleUiUtil.isFilePartOfActiveReview(crucibleFile)) {
					IResource resource = (IResource) editorInput.getAdapter(IResource.class);
					if (resource != null && resource.getType() == IResource.FILE) {
						action.setEnabled(true);
						setEnabled(true);
						setResources(new IResource[] { resource });
					}
					return;
				}
			}

			// TODO: support for IResource selection 
		}

		action.setEnabled(false);
		setEnabled(false);

	}

	private void setResources(IResource[] iResources) {
		this.resources = iResources;
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (!super.updateSelection(selection)) {
			return false;
		}

		return true;
	}

	@Override
	protected Review getReview() {
		if (review != null) {
			return review;
		} else {
			return CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
		}
	}

	@Override
	public String getToolTipText() {
		return "Add General File Comment...";
	}

	public void run(IAction action) {
		AddResourcesToReviewJob job = new AddResourcesToReviewJob(getReview(), resources);
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}
}
