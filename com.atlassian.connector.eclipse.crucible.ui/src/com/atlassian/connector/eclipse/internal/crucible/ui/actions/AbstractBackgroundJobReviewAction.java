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
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractBackgroundJobReviewAction extends AbstractListenableReviewAction {

	protected final Shell shell;

	protected final Comment comment;

	private final String jobMessage;

	private RemoteCrucibleOperation remoteOperation;

	private final boolean reloadReview;

	protected interface RemoteCrucibleOperation {
		void run(CrucibleServerFacade2 crucibleServerFacade, ConnectionCfg crucibleServerCfg)
				throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException;
	}

	public AbstractBackgroundJobReviewAction(String text, Review review, Comment comment, Shell shell,
			String jobMessage, ImageDescriptor imageDescriptor, RemoteCrucibleOperation remoteOperation,
			boolean reloadReview) {
		super(text);
		this.review = review;
		this.comment = comment;
		this.shell = shell;
		this.jobMessage = jobMessage;
		this.remoteOperation = remoteOperation;
		this.reloadReview = reloadReview;
		if (imageDescriptor != null) {
			setImageDescriptor(imageDescriptor);
		}
	}

	@Override
	protected Review getReview() {
		return review;
	}

	protected void updateReview(Review updatedReview) {
		this.review = updatedReview;
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
		RemoteCrucibleOperationJob remoteOperationJob = new RemoteCrucibleOperationJob(review, client, getTaskRepository(), jobMessage,
				remoteOperation, shell, reloadReview);
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

	protected void setRemoteOperation(RemoteCrucibleOperation remoteCrucibleOperation) {
		this.remoteOperation = remoteCrucibleOperation;

	}
}