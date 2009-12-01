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
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleRemoteOperation;
import com.atlassian.connector.eclipse.ui.AtlassianUiPlugin;
import com.atlassian.connector.eclipse.ui.team.ITeamResourceConnector;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.CrucibleSession;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.RevisionData;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Operation for adding files to a review
 * 
 * @author Shawn Minto
 * @author Pawel Niewiadomski
 */
public final class AddFilesToReviewRemoteOperation extends CrucibleRemoteOperation<Review> {

	private final CrucibleClient client;

	private final Review review;

	private final Collection<IFile> files;

	public AddFilesToReviewRemoteOperation(@NotNull TaskRepository repository, @NotNull Review review,
			@NotNull CrucibleClient client, @NotNull Collection<IFile> files, @NotNull IProgressMonitor monitor) {
		super(monitor, repository);
		this.review = review;
		this.client = client;
		this.files = files;
	}

	private String getTaskId() {
		if (review == null) {
			return null;
		}
		return CrucibleUtil.getTaskIdFromPermId(review.getPermId().getId());
	}

	@Override
	public Review run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
			throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
		SubMonitor submonitor = SubMonitor.convert(monitor);
		CrucibleSession session = server.getSession(serverCfg);

		submonitor.setWorkRemaining(files.size());

		final List<IResource> asUploadItems = MiscUtil.buildArrayList();
		final List<UploadItem> uploadItems = MiscUtil.buildArrayList();
		final List<RevisionData> revisions = MiscUtil.buildArrayList();

		for (IResource file : files) {
			ITeamResourceConnector connector = AtlassianUiPlugin.getDefault()
					.getTeamResourceManager()
					.getTeamConnector(file);

			//return server.addItemsToReview(serverCfg, review.getPermId(), uis);
			
		}

		return null;
	}
}