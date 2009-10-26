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
import com.atlassian.connector.eclipse.internal.crucible.ui.CruciblePreCommitFileInput;
import com.atlassian.connector.eclipse.internal.crucible.ui.CruciblePreCommitFileStorage;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.ReviewFileContent;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 
 * @author Jacek Jaroczynski
 */
public class OpenUploadedVirtualFileAction extends AbstractUploadedVirtualFileAction implements
		IReviewChangeListenerAction {

	private final ITask task;

	private CrucibleFile crucibleFile;

	private final VersionedVirtualFile virtualFile;

	private VersionedComment versionedComment;

	private final IWorkbenchPage iWorkbenchPage;

	public OpenUploadedVirtualFileAction(final ITask task, final CrucibleFile crucibleFile,
			final VersionedVirtualFile virtualFile, final Review crucibleReview,
			final VersionedComment versionedComment, final Shell shell, final IWorkbenchPage iWorkbenchPage) {
		super("", crucibleReview, null, shell, "Fetching File", null, null, false);

		this.task = task;
		this.crucibleFile = crucibleFile;
		this.virtualFile = virtualFile;
		this.versionedComment = versionedComment;
		this.iWorkbenchPage = iWorkbenchPage;

		setRemoteOperation(new LocalRemoteCrucibleOperation());

	}

	private static void open(final ITask task, final Review crucibleReview, final CrucibleFile crucibleFile,
			final VersionedVirtualFile virtualFile, final VersionedComment versionedComment,
			CrucibleServerFacade2 crucibleServerFacade, final ConnectionCfg crucibleServerCfg,
			final IWorkbenchPage iWorkbenchPage, Shell shell) throws RemoteApiException,
			ServerPasswordNotProvidedException {

		final ReviewFileContent file = getContent(virtualFile.getContentUrl(), crucibleServerFacade, crucibleServerCfg);
		final File[] localCopy = new File[1];
		try {
			localCopy[0] = createTempFile(virtualFile.getName(), file.getContent());
		} catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage()));
		}

		shell.getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					String editorId = getEditorId(iWorkbenchPage.getWorkbenchWindow().getWorkbench(),
							virtualFile.getName());
					IEditorPart editor = iWorkbenchPage.openEditor(new CruciblePreCommitFileInput(
							new CruciblePreCommitFileStorage(crucibleFile, file.getContent(), localCopy[0])), editorId);

					if (editor != null) {
						CrucibleUiUtil.attachCrucibleAnnotation(editor, task, crucibleReview, crucibleFile,
								versionedComment);
					}
				} catch (PartInitException e) {
					StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
		});
	}

	private static File createTempFile(String fileName, byte[] content) throws IOException {
		String baseName = FilenameUtils.getBaseName(fileName);
		String extention = FilenameUtils.getExtension(fileName);
		File retVal = File.createTempFile(baseName, "." + extention, CrucibleUiPlugin.getDefault()
				.getStateLocation()
				.toFile());
		retVal.deleteOnExit();

		FileOutputStream stream = new FileOutputStream(retVal);
		try {
			stream.write(content);
		} catch (IOException e1) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e1.getMessage()));
		} finally {
			try {
				stream.close();
			} catch (Exception e2) {
				StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e2.getMessage()));
			}
		}

		return retVal;
	}

	private static String getEditorId(IWorkbench workbench, String fileName) {
		IEditorRegistry registry = workbench.getEditorRegistry();
		IEditorDescriptor descriptor = registry.getDefaultEditor(fileName);

		String id;
		if (descriptor == null) {
			descriptor = registry.findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
		}

		if (descriptor == null) {
			id = "org.eclipse.ui.DefaultTextEditor";
		} else {
			id = descriptor.getId();
		}
		return id;

	}

	public void updateReview(Review updatedReview, CrucibleFileInfo updatedFile) {
		this.review = updatedReview;
		this.crucibleFile = new CrucibleFile(updatedFile, crucibleFile.isOldFile());
	}

	public void updateReview(Review updatedReview, CrucibleFileInfo updatedFile, VersionedComment updatedComment) {
		versionedComment = updatedComment;
		updateReview(updatedReview, updatedFile);
	}

	private final class LocalRemoteCrucibleOperation implements RemoteCrucibleOperation {

		public void run(CrucibleServerFacade2 crucibleServerFacade, ConnectionCfg crucibleServerCfg)
				throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
			open(task, getReview(), crucibleFile, virtualFile, versionedComment, crucibleServerFacade,
					crucibleServerCfg, iWorkbenchPage, shell);
		}

	}

}
