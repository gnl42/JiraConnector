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

package com.atlassian.connector.eclipse.internal.crucible.core.client;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import com.atlassian.connector.eclipse.internal.core.client.RemoteOperation;
import com.atlassian.theplugin.commons.crucible.api.CrucibleSession;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.TaskRepository;

public class DownloadAvatarsJob extends Job {

	private final Review review;

	private final CrucibleClient client;

	private final TaskRepository taskRepository;

	public DownloadAvatarsJob(CrucibleClient client, TaskRepository taskRepository, Review r) {
		super("Download avatars");
		this.review = r;
		this.client = client;
		this.taskRepository = taskRepository;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			client.execute(new RemoteOperation<Void, CrucibleServerFacade2>(monitor, taskRepository) {
				@Override
				public Void run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
						throws RemoteApiException, ServerPasswordNotProvidedException {
					final CrucibleSession session = server.getSession(serverCfg);

					for (Reviewer user : review.getReviewers()) {
						getUserAvatar(session, serverCfg, user);
					}

					getUserAvatar(session, serverCfg, review.getAuthor());
					if (review.getModerator() != null) {
						getUserAvatar(session, serverCfg, review.getModerator());
					}

					return null;
				}
			});
		} catch (CoreException e1) {
			StatusHandler.log(e1.getStatus());
		}

		return Status.OK_STATUS;
	}

	private void getUserAvatar(CrucibleSession session, ConnectionCfg serverCfg, User user) throws RemoteApiException {
		if (user.getAvatarUrl() != null) {
			if (client.getClientData().getAvatar(user) == null) {
				byte[] avatar = session.getFileContent(user.getAvatarUrl() + "%3Fs%3D16&s=16", true);
				if (avatar != null && avatar.length > 0) {
					client.getClientData().addAvatar(user, avatar);
				}
			}
		}
	}
};
