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
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewChangeJob;
import com.atlassian.connector.eclipse.ui.commons.ResourceEditorBean;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.BasicReview;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CreateSnippetReviewJob extends CrucibleReviewChangeJob {

	private final Review review;

	private BasicReview createdReview;

	private final ResourceEditorBean selection;

	public CreateSnippetReviewJob(TaskRepository taskRepository, Review review, ResourceEditorBean selection) {
		super("Create Crucible Snippet Review", taskRepository);
		this.review = review;
		this.selection = selection;
	}

	@Override
	protected IStatus execute(CrucibleClient client, IProgressMonitor monitor) throws CoreException {
		SubMonitor submonitor = SubMonitor.convert(monitor, "Creating new snippet review", 2);

		final String snippet = getSnippet();

		createdReview = client.execute(new CrucibleRemoteOperation<BasicReview>(submonitor.newChild(1),
				getTaskRepository()) {
			@Override
			public BasicReview run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
					throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
				return server.getSession(serverCfg).createSnippetReview(review, snippet,
						selection.getResource().getName());
			}
		});

		if (createdReview == null) {
			return new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, "Server didn't return review");
		}

		return Status.OK_STATUS;
	}

	private String getSnippet() throws CoreException {
		IFile file = (IFile) selection.getResource();
		InputStream stream = file.getContents();
		StringBuffer sb = new StringBuffer();
		try {
			int start = 1, count = -1;
			if (selection.getLineRange() != null) {
				start = selection.getLineRange().getStartLine();
				count = selection.getLineRange().getNumberOfLines();
			}
			List<?> lines = IOUtils.readLines(stream);
			for (int i = 0, s = (count == -1 ? lines.size() : count); i < s; ++i) {
				sb.append(lines.get(start - 1 + i));
				sb.append("\n");
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, "Error reading a file", e));
		} finally {
			IOUtils.closeQuietly(stream);
		}
		return sb.toString();
	}

	public BasicReview getCreatedReview() {
		return createdReview;
	}
}
