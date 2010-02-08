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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.dialogs.AbstractCrucibleReviewActionDialog;
import com.atlassian.connector.eclipse.internal.crucible.ui.dialogs.CrucibleCompleteReviewDialog;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class CompleteReviewAction extends AbstractReviewAction implements IWorkbenchWindowActionDelegate {

	public CompleteReviewAction(Review review, String text) {
		super(text);
		this.review = review;
	}

	public void run(IAction action) {
		if (review == null) {
			return;
		}

		CrucibleRepositoryConnector connector = CrucibleCorePlugin.getRepositoryConnector();
		CrucibleClient client = connector.getClientManager().getClient(getTaskRepository());
		if (client == null) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
					"Unable to get client, please try to refresh"));
			return;
		}

		AbstractCrucibleReviewActionDialog completeReviewDialog = new CrucibleCompleteReviewDialog(WorkbenchUtil.getShell(),
				review, client.getUsername(), getTaskKey(), getTaskId(), getTaskRepository(), client);
		completeReviewDialog.open();

	}

	@Override
	protected Review getReview() {
		return review;
	}
}
