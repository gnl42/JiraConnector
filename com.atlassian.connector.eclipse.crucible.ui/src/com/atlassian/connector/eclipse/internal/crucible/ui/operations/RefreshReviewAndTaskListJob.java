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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewChangeJob;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

public class RefreshReviewAndTaskListJob extends CrucibleReviewChangeJob {

	private final String reviewId;

	public RefreshReviewAndTaskListJob(Review review) {
		super("Refresh Task List", CrucibleUiUtil.getCrucibleTaskRepository(review), true, false);
		this.reviewId = review.getPermId().getId();
	}

	@Override
	protected IStatus execute(CrucibleClient client, IProgressMonitor monitor) throws CoreException {
		SubMonitor submonitor = SubMonitor.convert(monitor, "Retrieving Crucible Review", 2);

		// check if repositoryData is initialized
		if (client.getClientData() == null || client.getClientData().getCachedUsers().size() == 0
				|| client.getClientData().getCachedProjects().size() == 0) {

			client.updateRepositoryData(submonitor.newChild(1), getTaskRepository());
		}

		// hack to trigger task list synchronization
		client.getReview(getTaskRepository(), CrucibleUtil.getTaskIdFromPermId(reviewId), true, submonitor.newChild(1));
		return Status.OK_STATUS;
	}
}
