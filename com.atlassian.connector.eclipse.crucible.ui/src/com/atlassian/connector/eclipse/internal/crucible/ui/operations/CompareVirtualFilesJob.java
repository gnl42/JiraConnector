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
import com.atlassian.connector.eclipse.internal.core.client.RemoteOperation;
import com.atlassian.connector.eclipse.internal.core.jobs.JobWithStatus;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.annotations.CrucibleCompareAnnotationModel;
import com.atlassian.connector.eclipse.team.ui.ICompareAnnotationModel;
import com.atlassian.connector.eclipse.team.ui.TeamUiUtils;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.osgi.util.NLS;

public class CompareVirtualFilesJob extends JobWithStatus {

	private final Review review;

	private final CrucibleFileInfo fileInfo;

	private final VersionedComment comment;

	public CompareVirtualFilesJob(Review review, CrucibleFileInfo fileInfo, VersionedComment comment) {
		super(NLS.bind("Compare virtual files for review {0}", review.getPermId().getId()));
		this.review = review;
		this.fileInfo = fileInfo;
		this.comment = comment;
	}

	@Override
	protected void runImpl(IProgressMonitor monitor) throws CoreException {
		CrucibleUiUtil.checkAndRequestReviewActivation(review);

		TaskRepository taskRepository = getTaskRepository();
		CrucibleRepositoryConnector connector = CrucibleCorePlugin.getRepositoryConnector();
		CrucibleClient client = connector.getClientManager().getClient(taskRepository);

		client.execute(new RemoteOperation<Void, CrucibleServerFacade2>(monitor, taskRepository) {
			@Override
			public Void run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
					throws RemoteApiException, ServerPasswordNotProvidedException {
				final VersionedVirtualFile oldFile = fileInfo.getOldFileDescriptor();
				final VersionedVirtualFile newFile = fileInfo.getFileDescriptor();

				if (oldFile.getContentUrl() == null || newFile.getContentUrl() == null) {
					OpenVirtualFileJob.contentUrlMissingPopup();
					return null;
				}

				final ICompareAnnotationModel annotationModel = new CrucibleCompareAnnotationModel(fileInfo, review,
						comment);

				byte[] oldContent = OpenVirtualFileJob.getContent(oldFile.getContentUrl(),
						server.getSession(serverCfg), serverCfg.getUrl());
				byte[] newContent = OpenVirtualFileJob.getContent(newFile.getContentUrl(),
						server.getSession(serverCfg), serverCfg.getUrl());

				CompareConfiguration cc = new CompareConfiguration();
				cc.setLeftLabel(newFile.getName() + " " + newFile.getRevision());
				cc.setRightLabel(oldFile.getName() + " " + oldFile.getRevision());

				CrucibleFileInfoCompareEditorInput compareEditorInput = new CrucibleFileInfoCompareEditorInput(
						fileInfo, newContent, oldContent, annotationModel, cc);
				TeamUiUtils.openCompareEditorForInput(compareEditorInput);
				return null;
			}
		});
	}

	private TaskRepository getTaskRepository() {
		return CrucibleUiUtil.getCrucibleTaskRepository(review);
	}

}
