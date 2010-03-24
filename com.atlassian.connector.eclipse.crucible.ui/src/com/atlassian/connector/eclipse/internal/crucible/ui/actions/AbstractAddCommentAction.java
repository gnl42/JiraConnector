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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.ICrucibleFileProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.dialogs.CrucibleAddFileAddCommentDialog;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.connector.eclipse.ui.commons.AtlassianUiUtil;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.ui.IEditorInput;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract class to deal with adding comments to a review
 * 
 * @author Shawn Minto
 * @author Thomas Ehrnhoefer
 */
public abstract class AbstractAddCommentAction extends AbstractReviewAction {

	private CrucibleAddFileAddCommentDialog commentDialog;

	public class GetCrucibleFileJob extends Job {

		private final IEditorInput editorInput;

		private final Review review;

		private final LineRange commentLines;

		public GetCrucibleFileJob(String name, @NotNull IEditorInput editorInput, LineRange commentLines,
				@NotNull Review review) {
			super(name);
			this.editorInput = editorInput;
			this.commentLines = commentLines;
			this.review = review;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			CrucibleFile crucibleFile = null;

			if (editorInput instanceof ICrucibleFileProvider) {
				crucibleFile = ((ICrucibleFileProvider) editorInput).getCrucibleFile();
			}

			if (!CrucibleUiUtil.isFilePartOfReview(crucibleFile, review)) {
				crucibleFile = null;
			}

			if (crucibleFile == null) {

				IResource resource = (IResource) editorInput.getAdapter(IResource.class);

				if (resource instanceof IFile) {
					crucibleFile = CrucibleUiUtil.getCrucibleFileFromResource(resource, review, monitor);
				}
			}

			if (crucibleFile == null) {
				AtlassianUiUtil.getDisplay().asyncExec(new Runnable() {
					public void run() {
						openDialog((IResource) getEditorInput().getAdapter(IResource.class), commentLines);
					}
				});
			} else {
				final CrucibleFile cf = crucibleFile;

				AtlassianUiUtil.getDisplay().asyncExec(new Runnable() {
					public void run() {
						openDialog(cf, commentLines);
					}
				});
			}
			return Status.OK_STATUS;
		}
	}

	protected AbstractAddCommentAction(String text) {
		super(text);
	}

	public void run(IAction action) {

		if (review == null) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
					"Cannot add comment to file. Review is null."));
			return;
		}

		LineRange commentLines = getSelectedRange();
		CrucibleFile crucibleFile = getCrucibleFile();

		CrucibleRepositoryConnector connector = CrucibleCorePlugin.getRepositoryConnector();
		CrucibleClient client = connector.getClientManager().getClient(getTaskRepository());
		if (client == null) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
					"Unable to get client, please try to refresh"));
			return;
		}

		commentDialog = new CrucibleAddFileAddCommentDialog(WorkbenchUtil.getShell(), getDialogTitle(), review,
				getTaskKey(), getTaskId(), getTaskRepository(), client);

		if (crucibleFile != null) {
			openDialog(crucibleFile, commentLines);
		} else {
			if (getEditorInput() == null) {
				StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
						"Unable to determine crucible file. EditorInput object is null"));
				return;
			}

			GetCrucibleFileJob getCrucibleFileJob = new GetCrucibleFileJob("Getting Crucible file data",
					getEditorInput(), commentLines, review);

			getCrucibleFileJob.setUser(true);
			getCrucibleFileJob.schedule();

		}
	}

	private void openDialog(IResource resource, LineRange commentLines) {
		commentDialog.setResource(resource);
		if (commentLines != null) {
			commentDialog.setCommentLines(commentLines);
		}
		commentDialog.open();
	}

	private void openDialog(CrucibleFile crucibleFile, LineRange commentLines) {
		commentDialog.setReviewItem(crucibleFile);
		if (commentLines != null) {
			commentDialog.setCommentLines(commentLines);
		}
		commentDialog.open();
	}

	protected abstract String getDialogTitle();

	protected abstract IEditorInput getEditorInput();

	protected CrucibleFile getCrucibleFile() {
		return null;
	}

	protected LineRange getSelectedRange() {
		return null;
	}

	@Override
	protected Review getReview() {
		return CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
	}

	@Override
	public boolean isEnabled() {
		Review myReview = getReview();
		return super.isEnabled() && (myReview != null && CrucibleUtil.canAddCommentToReview(getReview()));
	}

}
