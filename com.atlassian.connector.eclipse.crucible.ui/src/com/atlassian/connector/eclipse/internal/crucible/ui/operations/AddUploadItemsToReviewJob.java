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
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleRemoteOperation;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewChangeJob;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import java.util.Collection;

public class AddUploadItemsToReviewJob extends CrucibleReviewChangeJob {
	private final Collection<UploadItem> items;

	private final Review review;

	private Review updatedReview;

	public AddUploadItemsToReviewJob(Review crucibleReview, Collection<UploadItem> items) {
		super("Add pre-commit items to Review", CrucibleUiUtil.getCrucibleTaskRepository(crucibleReview));
		this.items = items;
		this.review = crucibleReview;
	}

	Review getUpdatedReview() {
		return updatedReview;
	}

	@Override
	protected IStatus execute(CrucibleClient client, IProgressMonitor monitor) throws CoreException {
		updatedReview = client.execute(new CrucibleRemoteOperation<Review>(monitor, getTaskRepository()) {
			@Override
			public Review run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
					throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {

				return server.addItemsToReview(serverCfg, review.getPermId(), items);
			}
		});
		return Status.OK_STATUS;
	}

}
