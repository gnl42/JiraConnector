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

import com.atlassian.connector.eclipse.internal.core.jobs.JobWithStatus;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector;
import com.atlassian.connector.eclipse.ui.commons.DecoratedResource;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import java.util.ArrayList;
import java.util.Collection;

public class AddDecoratedResourcesToReviewJob extends JobWithStatus {

	private final ITeamUiResourceConnector connector;

	private final Collection<DecoratedResource> resources;

	private final Review review;

	public AddDecoratedResourcesToReviewJob(Review review, ITeamUiResourceConnector connector,
			Collection<DecoratedResource> resources) {
		super("Add Pre-commit and Post-commit review items");
		this.connector = connector;
		this.resources = resources;
		this.review = review;
	}

	@Override
	protected void runImpl(IProgressMonitor monitor) throws CoreException {
		final SubMonitor submonitor = SubMonitor.convert(monitor, "Preparing data for review", 3);
		final Collection<IResource> postCommitResources = MiscUtil.buildArrayList();
		final Collection<UploadItem> preCommitResources = MiscUtil.buildArrayList();

		Collection<IResource> preCommitTmp = new ArrayList<IResource>();
		for (DecoratedResource resource : resources) {
			if (resource.isUpToDate()) {
				postCommitResources.add(resource.getResource());
			} else {
				preCommitTmp.add(resource.getResource());
			}
		}

		preCommitResources.addAll(connector.getUploadItemsForResources(
				preCommitTmp.toArray(new IResource[preCommitTmp.size()]), monitor));

		// add post-commit resources
		if (postCommitResources.size() > 0) {
			final JobWithStatus job = new AddResourcesToReviewJob(review,
					postCommitResources.toArray(new IResource[postCommitResources.size()]));

			job.run(submonitor.newChild(1, SubMonitor.SUPPRESS_NONE));
			if (!job.getStatus().isOK()) {
				setStatus(job.getStatus());
				return;
			}
		}

		// add pre-commit items
		if (preCommitResources.size() > 0) {
			JobWithStatus job = new AddUploadItemsToReviewJob(review, preCommitResources);

			job.run(submonitor.newChild(1, SubMonitor.SUPPRESS_NONE));
			if (!job.getStatus().isOK()) {
				setStatus(job.getStatus());
				return;
			}
		}
	}

}
