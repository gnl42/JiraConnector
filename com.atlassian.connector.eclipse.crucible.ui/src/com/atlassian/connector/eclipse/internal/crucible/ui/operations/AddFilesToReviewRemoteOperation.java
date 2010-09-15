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
import com.atlassian.theplugin.commons.crucible.api.model.BasicReview;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.RevisionData;
import com.atlassian.theplugin.commons.crucible.api.model.SvnRepository;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.commons.util.StringUtil;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Operation for adding files to a review
 * 
 * @author Shawn Minto
 * @author Pawel Niewiadomski
 */
public final class AddFilesToReviewRemoteOperation extends CrucibleRemoteOperation<Review> {

	private final Review review;

	private final Collection<RevisionData> files;

	private boolean fixPaths;

	public AddFilesToReviewRemoteOperation(@NotNull TaskRepository repository, @NotNull Review review,
			@NotNull Collection<RevisionData> files, @NotNull IProgressMonitor monitor) {
		super(monitor, repository);
		this.review = review;
		this.fixPaths = true;
		this.files = files;
	}

	public void setFixPaths(boolean v) {
		fixPaths = v;
	}

	public boolean getFixPaths() {
		return fixPaths;
	}

	@Override
	public Review run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
			throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
		final CrucibleSession session = server.getSession(serverCfg);

		if (fixPaths) {
			Set<RevisionData> fixedRevision = MiscUtil.buildHashSet();
			Map<String, Repository> repositories = MiscUtil.buildHashMap();

			for (RevisionData revision : files) {
				if (!repositories.containsKey(revision.getSource())) {
					Repository crucibleRepository = server.getRepository(serverCfg, revision.getSource());
					if (crucibleRepository == null) {
						throw new RemoteApiException("Adding file to review failed. Cannot get repository data for "
								+ revision.getSource());
					}
					repositories.put(revision.getSource(), crucibleRepository);
				}

				Repository repository = repositories.get(revision.getSource());
				String filePath = revision.getPath();

				if (repository instanceof SvnRepository) {
					SvnRepository svnRepo = (SvnRepository) repository;
					if (filePath.startsWith(svnRepo.getUrl())) {
						filePath = filePath.substring(svnRepo.getUrl().length());
					}

					filePath = StringUtil.removePrefixSlashes(filePath);
					String cruPath = StringUtil.removePrefixSlashes(svnRepo.getPath());
					if (filePath.startsWith(cruPath)) {
						filePath = filePath.substring(svnRepo.getPath().length());
					}
				}

				filePath = StringUtil.removePrefixSlashes(filePath);
				fixedRevision.add(new RevisionData(revision.getSource(), filePath, revision.getRevisions()));
			}

			return getFullReview(session.addRevisionsToReviewItems(review.getPermId(), fixedRevision), session);
		}

		return getFullReview(session.addRevisionsToReviewItems(review.getPermId(), files), session);
	}

	private Review getFullReview(BasicReview basicReview, CrucibleSession session) throws RemoteApiException {
		if (basicReview == null) {
			return null;
		}
		return session.getReview(basicReview.getPermId());
	}
}