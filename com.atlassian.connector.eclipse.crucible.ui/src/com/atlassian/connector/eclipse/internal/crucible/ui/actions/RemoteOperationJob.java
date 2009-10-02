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

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleRemoteOperation;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.AbstractBackgroundJobReviewAction.RemoteCrucibleOperation;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class RemoteOperationJob extends Job {

	private final CrucibleClient crucibleClient;

	private final TaskRepository taskRepository;

	private final Review review;

	private final String jobMessage;

	private final RemoteCrucibleOperation remoteOperation;

	private final Shell shell;

	private final boolean reloadReview;

	public RemoteOperationJob(Review review, CrucibleClient crucibleClient, TaskRepository taskRepository,
			String jobMessage, RemoteCrucibleOperation remoteOperation, Shell shell, boolean reloadReview) {
		super(jobMessage);
		this.review = review;
		this.jobMessage = jobMessage;
		this.remoteOperation = remoteOperation;
		this.shell = shell;
		this.reloadReview = reloadReview;
		setUser(true);
		this.crucibleClient = crucibleClient;
		this.taskRepository = taskRepository;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			crucibleClient.execute(new CrucibleRemoteOperation<Void>(monitor, taskRepository) {
				@Override
				public Void run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
						throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
					remoteOperation.run(server, serverCfg);
					return null;
				}
			});

			if (reloadReview) {
				crucibleClient.getReview(taskRepository, CrucibleUtil.getTaskIdFromPermId(review.getPermId().getId()),
						true, monitor);
			}

		} catch (final CoreException e) {
			final String message = "Error while executing job: " + jobMessage + "\n" + e.getMessage();
			final Status status = new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, message, e);
			StatusHandler.log(status);
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					ErrorDialog.openError(shell, CrucibleUiPlugin.PRODUCT_NAME, message, status);
				}

			});
		}
		return Status.OK_STATUS;
	}
}
