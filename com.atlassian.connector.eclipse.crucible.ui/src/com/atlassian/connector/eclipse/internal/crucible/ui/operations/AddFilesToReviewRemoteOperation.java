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
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleRemoteOperation;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.CrucibleSession;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.RevisionData;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Operation for adding files to a review
 * 
 * @author Shawn Minto
 * @author Pawel Niewiadomski
 */
public final class AddFilesToReviewRemoteOperation extends CrucibleRemoteOperation<Review> {

	private final Review review;

	private final Collection<RevisionData> files;

	public AddFilesToReviewRemoteOperation(@NotNull TaskRepository repository, @NotNull Review review,
			@NotNull Collection<RevisionData> files, @NotNull IProgressMonitor monitor) {
		super(monitor, repository);
		this.review = review;
		this.files = files;
	}

	@Override
	public Review run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
			throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = server.getSession(serverCfg);
		return session.addRevisionsToReviewItems(review.getPermId(), files);
	}
}