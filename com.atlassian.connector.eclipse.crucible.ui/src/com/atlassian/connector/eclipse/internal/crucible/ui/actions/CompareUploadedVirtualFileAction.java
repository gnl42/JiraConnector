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
import com.atlassian.connector.eclipse.internal.crucible.IReviewChangeListenerAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.annotations.CrucibleCompareAnnotationModel;
import com.atlassian.connector.eclipse.ui.IAnnotationCompareInput;
import com.atlassian.connector.eclipse.ui.team.ICompareAnnotationModel;
import com.atlassian.connector.eclipse.ui.team.TeamUiUtils;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.ReviewFileContent;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Jacek Jaroczynski
 */
public class CompareUploadedVirtualFileAction extends AbstractUploadedVirtualFileAction implements
		IReviewChangeListenerAction {

	private CrucibleFileInfo crucibleFile;

	private VersionedComment versionedComment;

	public CompareUploadedVirtualFileAction(final CrucibleFileInfo crucibleFile, VersionedComment versionedComment,
			final Review crucibleReview, Shell shell) {
		super("", crucibleReview, null, shell, "Fetching Files to Compare", null, null, false);

		setRemoteOperation(new LocalRemoteCrucibleOperation());

		this.versionedComment = versionedComment;
		this.crucibleFile = crucibleFile;
	}

	private static void compare(Review crucibleReview, CrucibleFileInfo crucibleFile,
			VersionedComment versionedComment, CrucibleServerFacade2 crucibleServerFacade,
			ConnectionCfg crucibleServerCfg) throws RemoteApiException, ServerPasswordNotProvidedException {

		final VersionedVirtualFile oldVirtualFile = crucibleFile.getOldFileDescriptor();

		final VersionedVirtualFile newVirtualFile = crucibleFile.getFileDescriptor();

		final ICompareAnnotationModel annotationModel = new CrucibleCompareAnnotationModel(crucibleFile,
				crucibleReview, versionedComment);

		ReviewFileContent oldFile = getContent(oldVirtualFile.getContentUrl(), crucibleServerFacade, crucibleServerCfg);
		ReviewFileContent newFile = getContent(newVirtualFile.getContentUrl(), crucibleServerFacade, crucibleServerCfg);

		String title = "Compare " + oldVirtualFile.getName() + " " + newVirtualFile.getRevision() + " and "
				+ oldVirtualFile.getRevision();

		CompareConfiguration cc = new CompareConfiguration();
		cc.setLeftLabel(newVirtualFile.getName() + " " + newVirtualFile.getRevision());
		cc.setRightLabel(oldVirtualFile.getName() + " " + oldVirtualFile.getRevision());

		CompareEditorInput compareEditorInput = new LocalCompareEditorInput(newFile.getContent(), oldFile.getContent(),
				annotationModel, title, cc);
		TeamUiUtils.openCompareEditorForInput(compareEditorInput);
	}

	public void updateReview(Review updatedReview, CrucibleFileInfo updatedCrucibleFile) {
		updateReview(updatedReview);
		this.crucibleFile = updatedCrucibleFile;
	}

	public void updateReview(Review updatedReview, CrucibleFileInfo updatedFile, VersionedComment updatedComment) {
		updateReview(updatedReview);
		this.crucibleFile = updatedFile;
		this.versionedComment = updatedComment;
	}

	final class LocalRemoteCrucibleOperation implements RemoteCrucibleOperation {

		private LocalRemoteCrucibleOperation() {
		}

		public void run(CrucibleServerFacade2 crucibleServerFacade, ConnectionCfg crucibleServerCfg)
				throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
			compare(getReview(), crucibleFile, versionedComment, crucibleServerFacade, crucibleServerCfg);
		}
	}

	private static class LocalCompareEditorInput extends CompareEditorInput implements IAnnotationCompareInput {

		private final byte[] content1;

		private final byte[] content2;

		private final ICompareAnnotationModel annotationModel;

		public LocalCompareEditorInput(byte[] content1, byte[] content2, ICompareAnnotationModel annotationModel,
				String title, CompareConfiguration compareConfiguration) {
			super(compareConfiguration);
			this.content1 = content1;
			this.content2 = content2;
			this.annotationModel = annotationModel;
			setTitle(title);
		}

		@Override
		protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			Differencer d = new Differencer();
			Object diff = d.findDifferences(false, monitor, null, null, new Input(content1), new Input(content2));
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

	private static class Input implements ITypedElement, IStreamContentAccessor {

		byte[] content;

		public Input(byte[] content) {
			this.content = content;
		}

		public String getName() {
			return "";
		}

		public Image getImage() {
			return null;
		}

		public String getType() {
			return "java";
		}

		public InputStream getContents() throws CoreException {
			return new ByteArrayInputStream(content);
		}
	}
}
