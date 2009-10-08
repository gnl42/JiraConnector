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

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewActionListener;
import com.atlassian.theplugin.commons.crucible.ReviewFileContent;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.UrlUtil;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.widgets.Shell;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public abstract class AbstractUploadedVirtualFileAction extends AbstractBackgroundJobReviewAction implements
		IReviewAction {

	private IReviewActionListener actionListener;

	public AbstractUploadedVirtualFileAction(String text, Review review, Comment comment, Shell shell,
			String jobMessage, ImageDescriptor imageDescriptor, RemoteCrucibleOperation remoteOperation,
			boolean reloadReview) {
		super(text, review, comment, shell, jobMessage, imageDescriptor, remoteOperation, reloadReview);
	}

	@Override
	public void run(IAction action) {
		CrucibleUiUtil.checkAndRequestReviewActivation(getReview()); // executed in the UI thread
		super.run(action);
		if (actionListener != null) {
			actionListener.actionRan(this);
		}
	}

	public void setActionListener(IReviewActionListener listener) {
		this.actionListener = listener;
	}

	protected static ReviewFileContent getContent(String contentUrl, CrucibleServerFacade2 crucibleServerFacade,
			ConnectionCfg crucibleServerCfg) throws RemoteApiException, ServerPasswordNotProvidedException {

		try {
			contentUrl = URLDecoder.decode(contentUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			final String message = "Error while decoding remote file url.\n" + e.getMessage();
			final Status status = new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, message, e);
			StatusHandler.log(status);
		}

		if (contentUrl == null) {
			return null;
		}

		contentUrl = UrlUtil.adjustUrlPath(contentUrl, crucibleServerCfg.getUrl());

		byte[] content = crucibleServerFacade.getFileContent(crucibleServerCfg, contentUrl);
		return new ReviewFileContent(content);
	}
}
