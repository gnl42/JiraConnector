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

package com.atlassian.connector.eclipse.internal.crucible.ui.dialogs;

import com.atlassian.connector.eclipse.internal.core.AtlassianCorePlugin;
import com.atlassian.connector.eclipse.internal.core.jobs.JobWithStatus;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.LocalTeamResourceConnector;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.AddDecoratedResourcesToReviewJob;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector;
import com.atlassian.connector.eclipse.team.ui.TeamUiUtils;
import com.atlassian.connector.eclipse.ui.commons.DecoratedResource;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class CrucibleAddFileAddCommentDialog extends CrucibleAddCommentDialog {

	public class AddFileToReviewRunable implements IRunnableWithProgress {

		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

			ITeamUiResourceConnector connector = TeamUiUtils.getTeamConnector(resource);

			if (connector == null) {
				connector = new LocalTeamResourceConnector();
			}

			DecoratedResource decoratedResource = TeamUiUtils.getDecoratedResource(resource, connector);
			if (decoratedResource != null) {

				// TODO jj check if monitor works fine (use submonitor if needed)
				monitor.beginTask("Adding selected file to the review", IProgressMonitor.UNKNOWN);

				JobWithStatus job = new AddDecoratedResourcesToReviewJob(review, connector,
						Arrays.asList(decoratedResource));
				job.run(monitor);

				if (!job.getStatus().isOK()) {
					reportError(job.getStatus().getMessage(), job.getStatus().getException());
					return;
				}

				CrucibleClient client = CrucibleCorePlugin.getRepositoryConnector().getClientManager().getClient(
						getTaskRepository());

				try {
					review = client.getReview(getTaskRepository(), CrucibleUtil.getTaskIdFromReview(review), true,
							monitor);
				} catch (CoreException e) {
					reportError(e.getMessage(), e);
				}
			} else {
				reportError("Cannot determine SCM details for resource. Your SCM is probably not supported.", null);
			}
		}

		private void reportError(final String message, final Throwable exception) {
			Display d = getShell().getDisplay();

			if (d == null) {
				d = Display.getCurrent();
			}

			if (d == null) {
				d = Display.getDefault();
			}

			if (d == null) {
				d = WorkbenchUtil.getShell().getDisplay();
			}
			d.asyncExec(new Runnable() {
				public void run() {
					StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, message, exception));
					setErrorMessage(message);
				}
			});
		}

	}

	private IResource resource;

	private Review review;

	public CrucibleAddFileAddCommentDialog(Shell shell, String dialogTitle, Review review, String taskKey,
			String taskId, TaskRepository taskRepository, CrucibleClient client) {
		super(shell, dialogTitle, review, taskKey, taskId, taskRepository, client);
		this.review = review;
	}

	@Override
	public boolean addComment() {

		// add file to review first
		if (resource != null) {
			try {
				run(true, false, new AddFileToReviewRunable());
			} catch (InvocationTargetException e) {
				StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
				setErrorMessage("Unable to add file to the review");
				return false;
			} catch (InterruptedException e) {
				StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
				setErrorMessage("Unable to add file to the review");
				return false;
			}

			// get CrucibleFile for added file from fresh review
			CrucibleFile reviewItem = CrucibleUiUtil.getCrucibleFileFromResource(resource, review);

			if (reviewItem == null) {
				StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, "Adding file to review failed."));
				setErrorMessage("Unable to add file to the review");
				return false;
			}

			if (review == null) {
				StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
						"Failed to refresh review after file was added."));
				setErrorMessage("Unable to refresh the review");
				return false;
			}

			setReview(review);
			setReviewItem(reviewItem);
		}

		// add comment
		boolean ok = super.addComment();
		if (ok && resource != null) {
			MessageDialog.openInformation(getShell(), AtlassianCorePlugin.PRODUCT_NAME,
					"Please reopen the file in Review Explorer in order to see comment annotation.");
		}

		return ok;
	}

	public void setResource(IResource resource) {
		this.resource = resource;
	}

}
