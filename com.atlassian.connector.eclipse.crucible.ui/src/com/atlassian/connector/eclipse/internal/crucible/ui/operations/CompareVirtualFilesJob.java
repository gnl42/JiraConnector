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
import com.atlassian.connector.eclipse.ui.IAnnotationCompareInput;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

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

				String title = "Compare " + oldFile.getName() + " " + newFile.getRevision() + " and "
						+ oldFile.getRevision();

				CompareConfiguration cc = new CompareConfiguration();
				cc.setLeftLabel(newFile.getName() + " " + newFile.getRevision());
				cc.setRightLabel(oldFile.getName() + " " + oldFile.getRevision());

				CompareEditorInput compareEditorInput = new LocalCompareEditorInput(newContent, newFile.getName(),
						oldContent, oldFile.getName(), annotationModel, title, cc);
				TeamUiUtils.openCompareEditorForInput(compareEditorInput);
				return null;
			}
		});
	}

	private static class LocalCompareEditorInput extends CompareEditorInput implements IAnnotationCompareInput {

		private final byte[] content1;

		private final byte[] content2;

		private final ICompareAnnotationModel annotationModel;

		private final String name1;

		private final String name2;

		public LocalCompareEditorInput(byte[] content1, String name1, byte[] content2, String name2,
				ICompareAnnotationModel annotationModel, String title, CompareConfiguration compareConfiguration) {
			super(compareConfiguration);
			this.content1 = content1;
			this.name1 = name1;
			this.content2 = content2;
			this.name2 = name2;
			this.annotationModel = annotationModel;
			setTitle(title);
		}

		@Override
		protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			Differencer d = new Differencer();
			Object diff = d.findDifferences(false, monitor, null, null, new ByteArrayInput(content1, name1),
					new ByteArrayInput(content2, name2));
			return diff;
		}

		@Override
		public Viewer findContentViewer(Viewer oldViewer, ICompareInput input, Composite parent) {
			Viewer contentViewer = super.findContentViewer(oldViewer, input, parent);
			return TeamUiUtils.findContentViewer(contentViewer, input, parent, annotationModel);
		}

		public ICompareAnnotationModel getAnnotationModelToAttach() {
			return annotationModel;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((annotationModel == null) ? 0 : annotationModel.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			LocalCompareEditorInput other = (LocalCompareEditorInput) obj;
			if (annotationModel == null) {
				if (other.annotationModel != null) {
					return false;
				}
			} else if (!annotationModel.equals(other.annotationModel)) {
				return false;
			}
			return true;
		}
	}

	private TaskRepository getTaskRepository() {
		return CrucibleUiUtil.getCrucibleTaskRepository(review);
	}

	private static class ByteArrayInput implements ITypedElement, IStreamContentAccessor {

		byte[] content;

		private final String name;

		public ByteArrayInput(byte[] content, String name) {
			this.content = content;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public Image getImage() {
			return null;
		}

		public String getType() {
			String extension = FilenameUtils.getExtension(name);
			return extension.length() > 0 ? extension : ITypedElement.TEXT_TYPE;
		}

		public InputStream getContents() throws CoreException {
			return new ByteArrayInputStream(content);
		}

	}
}
