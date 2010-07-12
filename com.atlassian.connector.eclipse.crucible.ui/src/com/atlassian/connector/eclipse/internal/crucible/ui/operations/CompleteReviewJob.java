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

package com.atlassian.connector.eclipse.internal.crucible.ui.operations;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import com.atlassian.connector.eclipse.internal.core.client.RemoteOperation;
import com.atlassian.connector.eclipse.internal.core.jobs.JobWithStatus;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.theplugin.commons.crucible.api.CrucibleSession;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.mylyn.tasks.core.TaskRepository;

public class CompleteReviewJob extends JobWithStatus {

	private final Review review;

	private final boolean refreshReview;

	private final boolean markAsComplete;

	public CompleteReviewJob(Review review, boolean markAsComplete, boolean refresh) {
		super(markAsComplete ? "Complete Review" : "Uncomplete Review");
		this.review = review;
		this.refreshReview = refresh;
		this.markAsComplete = markAsComplete;
	}

	@Override
	protected void runImpl(IProgressMonitor monitor) {
		TaskRepository taskRepository = getTaskRepository();
		SubMonitor submonitor = SubMonitor.convert(monitor, getName(), 1);
		CrucibleRepositoryConnector connector = CrucibleCorePlugin.getRepositoryConnector();
		CrucibleClient client = connector.getClientManager().getClient(taskRepository);

		try {
			client.execute(new RemoteOperation<Void, CrucibleServerFacade2>(submonitor.newChild(1), taskRepository) {
				@Override
				public Void run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
						throws RemoteApiException, ServerPasswordNotProvidedException {
					CrucibleSession session = server.getSession(serverCfg);

					session.completeReview(review.getPermId(), markAsComplete);
					return null;
				}
			});
		} catch (CoreException e) {
			setStatus(e.getStatus());
		}

		if (refreshReview) {
			// hack to trigger task list synchronization
			try {
				client.getReview(getTaskRepository(), CrucibleUtil.getTaskIdFromPermId(review.getPermId().getId()),
						true, monitor);
			} catch (CoreException e) {
				setStatus(e.getStatus());
				return;
			}
		}

	}

	private TaskRepository getTaskRepository() {
		return CrucibleUiUtil.getCrucibleTaskRepository(review);
	}
}
