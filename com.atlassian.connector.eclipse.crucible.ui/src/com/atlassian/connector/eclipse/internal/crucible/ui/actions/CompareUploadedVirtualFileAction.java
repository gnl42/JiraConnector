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
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewActionListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.annotations.CrucibleCompareAnnotationModel;
import com.atlassian.connector.eclipse.ui.team.ICompareAnnotationModel;
import com.atlassian.connector.eclipse.ui.team.TeamUiUtils;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.ReviewFileContent;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Jacek Jaroczynski
 */
public class CompareUploadedVirtualFileAction extends AbstractUploadedVirtualFileAction implements IReviewAction {

	public CompareUploadedVirtualFileAction(final CrucibleFileInfo crucibleFile, final Review crucibleReview,
			Shell shell) {
		super("", crucibleReview, null, shell, "Fetching Files to Compare", null, new RemoteCrucibleOperation() {

			public void run(CrucibleServerFacade2 crucibleServerFacade, ConnectionCfg crucibleServerCfg)
					throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
				compare(crucibleReview, crucibleFile, crucibleServerFacade, crucibleServerCfg);
			}
		}, false);
	}

	private static void compare(Review crucibleReview, CrucibleFileInfo crucibleFile,
			CrucibleServerFacade2 crucibleServerFacade, ConnectionCfg crucibleServerCfg) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleUiUtil.checkAndRequestReviewActivation(crucibleReview);

		final VersionedVirtualFile oldVirtualFile = crucibleFile.getOldFileDescriptor();

		final VersionedVirtualFile newVirtualFile = crucibleFile.getFileDescriptor();

		final ICompareAnnotationModel annotationModel = new CrucibleCompareAnnotationModel(crucibleFile,
				crucibleReview, null);

		ReviewFileContent oldFile = getContent(oldVirtualFile.getContentUrl(), crucibleServerFacade, crucibleServerCfg);
		ReviewFileContent newFile = getContent(newVirtualFile.getContentUrl(), crucibleServerFacade, crucibleServerCfg);

		CompareEditorInput compareEditorInput = new LocalCompareEditorInput(newFile.getContent(), oldFile.getContent(),
				annotationModel);
		TeamUiUtils.openCompareEditorForInput(compareEditorInput);
	}

	// TODO jj check if we need to implement IReviewAction
	public void setActionListener(IReviewActionListener listner) {
	}

	private static class LocalCompareEditorInput extends CompareEditorInput {

		private final byte[] content1;

		private final byte[] content2;

		public LocalCompareEditorInput(byte[] content1, byte[] content2, ICompareAnnotationModel annotationModel) {
			super(new CompareConfiguration());
			this.content1 = content1;
			this.content2 = content2;
		}

		@Override
		protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			Differencer d = new Differencer();
			Object diff = d.findDifferences(false, monitor, null, null, new Input(content1), new Input(content2));
			return diff;
		}
	}

	private static class Input implements ITypedElement, IStreamContentAccessor {

		byte[] content;

		public Input(byte[] content) {
			this.content = content;
		}

		public String getName() {
			// TODO JJ inspect how to display file name
			return "file";
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
