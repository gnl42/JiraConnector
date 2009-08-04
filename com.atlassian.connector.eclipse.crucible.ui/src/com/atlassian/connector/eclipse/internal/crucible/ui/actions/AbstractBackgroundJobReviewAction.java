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
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleRemoteOperation;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractBackgroundJobReviewAction extends AbstractListenableReviewAction {

	protected final Shell shell;

	protected final Comment comment;

	private final String jobMessage;

	private final RemoteOperation remoteOperation;

	protected interface RemoteOperation {
		void run(CrucibleServerFacade crucibleServerFacade, ServerData crucibleServerCfg)
				throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException;
	}

	public AbstractBackgroundJobReviewAction(String text, Review review, Comment comment, Shell shell,
			String jobMessage, ImageDescriptor imageDescriptor, RemoteOperation remoteOperation) {
		super(text);
		this.review = review;
		this.comment = comment;
		this.shell = shell;
		this.jobMessage = jobMessage;
		this.remoteOperation = remoteOperation;
		setImageDescriptor(imageDescriptor);
	}

	@Override
	protected Review getReview() {
		return review;
	}

	public void run(IAction action) {
		CrucibleClient client = CrucibleUiUtil.getClient(review);
		if (client == null) {
			String message = "Unable to get client, please try to refresh";
			Status status = new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, message);
			StatusHandler.log(status);
			ErrorDialog.openError(shell, CrucibleUiPlugin.PRODUCT_NAME, message, status);
			return;
		}
		RemoteOperationJob remoteOperationJob = new RemoteOperationJob(review, client, getTaskRepository());
		remoteOperationJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						setEnabled(true);
					}

				});
			}
		});
		setEnabled(false);
		remoteOperationJob.schedule();
	}

	private class RemoteOperationJob extends Job {

		private final CrucibleClient crucibleClient;

		private final TaskRepository taskRepository;

		public RemoteOperationJob(Review review, CrucibleClient crucibleClient, TaskRepository taskRepository) {
			super(jobMessage);
			setUser(true);
			this.crucibleClient = crucibleClient;
			this.taskRepository = taskRepository;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				crucibleClient.execute(new CrucibleRemoteOperation<Void>(monitor, getTaskRepository()) {
					@Override
					public Void run(CrucibleServerFacade server, ServerData serverCfg, IProgressMonitor monitor)
							throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
						remoteOperation.run(server, serverCfg);
						return null;
					}
				});
				crucibleClient.getReview(taskRepository, CrucibleUtil.getTaskIdFromPermId(review.getPermId().getId()),
						true, monitor);

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

}