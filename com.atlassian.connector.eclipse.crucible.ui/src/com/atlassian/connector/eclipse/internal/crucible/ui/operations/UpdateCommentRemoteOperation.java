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

import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleRemoteOperation;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.TaskRepository;

public final class UpdateCommentRemoteOperation extends CrucibleRemoteOperation<Comment> {

	private final Review review;

	private final Comment comment;

	public UpdateCommentRemoteOperation(TaskRepository repository, Review review, Comment comment,
			IProgressMonitor monitor) {
		super(monitor, repository);
		this.review = review;
		this.comment = comment;
	}

	@Override
	public Comment run(CrucibleServerFacade server, ServerData serverCfg, IProgressMonitor monitor)
			throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {

		server.updateComment(serverCfg, review.getPermId(), comment);
		return comment;
	}

}
