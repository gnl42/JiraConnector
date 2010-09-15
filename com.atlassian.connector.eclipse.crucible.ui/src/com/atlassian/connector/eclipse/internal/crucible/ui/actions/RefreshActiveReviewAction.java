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

import com.atlassian.connector.eclipse.internal.crucible.ui.ActiveReviewManager.IReviewActivationListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.RefreshReviewJob;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import java.util.Collection;

public class RefreshActiveReviewAction extends BaseSelectionListenerAction implements IReviewActivationListener {

	private Review review;
	private final JobChangeAdapter jobChangeAdapter;

	public RefreshActiveReviewAction(JobChangeAdapter jobChangeAdapter) {
		super("Refresh");
		this.jobChangeAdapter = jobChangeAdapter;
		setImageDescriptor(CommonImages.REFRESH);
		setToolTipText("Refresh Active Review");
		setEnabled(false);
	}

	@Override
	public void run() {
		final RefreshReviewJob job = RefreshReviewJob.createForReview(review);
		if (job != null) {
			if (jobChangeAdapter != null) {
				job.addJobChangeListener(jobChangeAdapter);
			}
			job.schedule();
		}
	}

	public void reviewActivated(ITask task, Review aReview) {
		this.review = aReview;
		setEnabled(aReview != null);
	}

	public void reviewDeactivated(ITask task, Review aReview) {
		setEnabled(false);
	}

	public void reviewUpdated(ITask task, Review aReview, Collection<CrucibleNotification> differences) {
		reviewActivated(task, aReview);
	}

}
