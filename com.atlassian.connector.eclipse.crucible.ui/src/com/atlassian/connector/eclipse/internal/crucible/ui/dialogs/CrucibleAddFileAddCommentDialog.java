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
import com.atlassian.connector.eclipse.ui.AtlassianImages;
import com.atlassian.connector.eclipse.ui.commons.AtlassianUiUtil;
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
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
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

			decoratedResource = TeamUiUtils.getDecoratedResource(resource, connector);
			if (decoratedResource != null) {

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
				reviewItem = CrucibleUiUtil.getCrucibleFileFromResource(resource, review, monitor);

			} else {
				reportError("Cannot determine SCM details for resource. Your SCM is probably not supported.", null);
			}
		}

		private void reportError(final String message, final Throwable exception) {
			Display d = AtlassianUiUtil.getDisplay(getShell());
			d.asyncExec(new Runnable() {
				public void run() {
					StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, message, exception));
					setErrorMessage(message);
				}
			});
		}
	}

	private Review review;

	private CrucibleFile reviewItem;

	private IResource resource;

	private DecoratedResource decoratedResource;

	public CrucibleAddFileAddCommentDialog(Shell shell, String dialogTitle, Review review, String taskKey,
			String taskId, TaskRepository taskRepository, CrucibleClient client) {
		super(shell, dialogTitle, review, taskKey, taskId, taskRepository, client);
		this.review = review;
	}

	@Override
	public boolean addComment() {

		// add file to review first if needed
		if (resource != null) {
			try {
				run(true, false, new AddFileToReviewRunable());
			} catch (InvocationTargetException e) {
				StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
				setErrorMessage("Unable to add file to the review.");
				return false;
			} catch (InterruptedException e) {
				StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
				setErrorMessage("Unable to add file to the review.");
				return false;
			}

			if (reviewItem == null) {
				StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, "Adding file to review failed."));
				setErrorMessage("Unable to determine CrucibleFile.");
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
		if (ok && resource != null && decoratedResource != null && !decoratedResource.isUpToDate()) {
			MessageDialog.openInformation(getShell(), AtlassianCorePlugin.PRODUCT_NAME,
					"Please reopen the file in Review Explorer in order to see comment annotation.");
		}

		return ok;
	}

	@Override
	protected void createAdditionalControl(Composite composite) {
		if (resource != null) {

			Composite labels = new Composite(composite, SWT.NONE);

			GridLayout layout = new GridLayout(2, false);
			layout.horizontalSpacing = 1;
			layout.marginWidth = 1;
			labels.setLayout(layout);

			Label icon = new Label(labels, SWT.NONE);
			icon.setImage(AtlassianImages.getImage(AtlassianImages.IMG_ECLIPSE_INFO));

			Label explanation = new Label(labels, SWT.NONE);
			String text = "This file is currently not under review. Adding a comment will add this file to review ";
			text += CrucibleUiUtil.getCrucibleTask(review).getTaskKey() + ".";
			explanation.setText(text);
		}
	}

	public void setResource(IResource resource) {
		this.resource = resource;
	}
}
