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
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.RefreshReviewAndTaskListJob;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

public class RefreshActiveReviewAction extends BaseSelectionListenerAction implements IReviewActivationListener {

	private Review review;

	public RefreshActiveReviewAction() {
		super("Refresh");
		setImageDescriptor(CommonImages.REFRESH);
		setToolTipText("Refresh Active Review");
	}

	@Override
	public void run() {
		RefreshReviewAndTaskListJob job = new RefreshReviewAndTaskListJob(review);
		job.schedule();
	}

	public void reviewActivated(ITask task, Review review) {
		this.review = review;
		setEnabled(review != null);
	}

	public void reviewDeactivated(ITask task, Review review) {
		setEnabled(false);
	}

	public void reviewUpdated(ITask task, Review review) {
		reviewActivated(task, review);
	}
}
