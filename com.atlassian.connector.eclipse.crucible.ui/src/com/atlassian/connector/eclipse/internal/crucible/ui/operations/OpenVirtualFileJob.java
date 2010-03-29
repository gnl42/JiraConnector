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
import com.atlassian.connector.eclipse.internal.crucible.core.TaskRepositoryUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.ui.CruciblePreCommitFileInput;
import com.atlassian.connector.eclipse.internal.crucible.ui.CruciblePreCommitFileStorage;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.connector.eclipse.team.ui.TeamUiUtils;
import com.atlassian.connector.eclipse.ui.commons.AtlassianUiUtil;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.CrucibleSession;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.RepositoryType;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.UrlUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

public class OpenVirtualFileJob extends JobWithStatus {

	private final Review review;

	private final VersionedComment comment;

	private final CrucibleFile crucibleFile;

	public OpenVirtualFileJob(Review review, CrucibleFile file, VersionedComment comment) {
		super(NLS.bind("Open virtual file from review {0}", review.getPermId().getId()));
		this.review = review;
		this.comment = comment;
		this.crucibleFile = file;
	}

	public static byte[] getContent(String contentUrl, CrucibleSession session, String url) throws RemoteApiException,
			ServerPasswordNotProvidedException {

		try {
			contentUrl = URLDecoder.decode(contentUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
					"Error while decoding remote file URL", e));
		}

		if (contentUrl != null) {
			contentUrl = UrlUtil.adjustUrlPath(contentUrl, url);

			return session.getFileContent(contentUrl);
		}

		return null;
	}

	public static File createTempFile(String fileName, byte[] content) throws IOException {
		String extention = FilenameUtils.getExtension(fileName);
		File retVal = File.createTempFile("openVirtualFileJob", "." + extention, CrucibleUiPlugin.getDefault()
				.getStateLocation()
				.toFile());
		retVal.deleteOnExit();

		FileOutputStream stream = new FileOutputStream(retVal);
		try {
			stream.write(content);
		} catch (IOException e1) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e1.getMessage(), e1));
		} finally {
			IOUtils.closeQuietly(stream);
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

	@Override
	protected void runImpl(IProgressMonitor monitor) throws CoreException {
		final SubMonitor submonitor = SubMonitor.convert(monitor, 2);

		final CrucibleFileInfo fileInfo = crucibleFile.getCrucibleFileInfo();
		final VersionedVirtualFile virtualFile = crucibleFile.getSelectedFile();

		IResource workspaceReviewItem = null;

		// if it's SCM first try to open IResource locally
		if (fileInfo.getRepositoryType().equals(RepositoryType.SCM)) {
			TaskRepository repository = CrucibleUiUtil.getCrucibleTaskRepository(review);
			Map.Entry<String, String> mapping = TaskRepositoryUtil.getNamedSourceRepository(
					TaskRepositoryUtil.getScmRepositoryMappings(repository), fileInfo.getRepositoryName());

			if (mapping != null) {
				workspaceReviewItem = TeamUiUtils.findResourceForPath(mapping.getKey(), virtualFile.getUrl(),
						submonitor.newChild(1));
				if (workspaceReviewItem != null) {
					if (TeamUiUtils.isInSync(workspaceReviewItem, virtualFile.getRevision())
							&& workspaceReviewItem.getType() == IResource.FILE) {
						IEditorPart editor = editLocalFile(crucibleFile, (IFile) workspaceReviewItem);

						if (editor != null) {
							CrucibleUiUtil.focusOnComment(editor, crucibleFile, comment);
							return;
						}
					}
				}
			} else {
				StatusHandler.log(new Status(
						IStatus.WARNING,
						CrucibleUiPlugin.PLUGIN_ID,
						NLS.bind(
								"There's no mapping for Crucible repository {0}. Review item will be downloaded from Crucible.",
								fileInfo.getRepositoryName())));
			}
			submonitor.worked(1);
		}

		// failed to open local file so let's download it from Crucible
		TaskRepository taskRepository = getTaskRepository();
		CrucibleRepositoryConnector connector = CrucibleCorePlugin.getRepositoryConnector();
		CrucibleClient client = connector.getClientManager().getClient(taskRepository);

		client.execute(new FileContentFetchingFromCrucibleOperation(submonitor.newChild(1, SubMonitor.SUPPRESS_NONE),
				taskRepository, workspaceReviewItem));
	}

	private IEditorPart editLocalFile(final CrucibleFile crucibleFile2, final IFile iResource) throws CoreException {
		final IEditorPart[] part = new IEditorPart[1];
		final CoreException[] exception = new CoreException[1];
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				try {
					final String editorId = getEditorId(PlatformUI.getWorkbench(), crucibleFile2.getSelectedFile()
							.getName());
					part[0] = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
							new FileEditorInput(iResource), editorId);
				} catch (CoreException e) {
					exception[0] = e;
				}
			}
		});

		if (exception[0] != null) {
			throw exception[0];
		}
		return part[0];
	}

	private TaskRepository getTaskRepository() {
		return CrucibleUiUtil.getCrucibleTaskRepository(review);
	}

	public static void contentUrlMissingPopup() {
		AtlassianUiUtil.getDisplay().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(
						WorkbenchUtil.getShell(),
						"Unable to open review item",
						"Crucible did not return review item content URL. Probably you're using Crucible 1.6.x which doesn't support direct access to review items. Please upgrade to Crucible 2.x.");
			}
		});
	}

	private final class FileContentFetchingFromCrucibleOperation extends RemoteOperation<Void, CrucibleServerFacade2> {
		private final IResource workspaceReviewItem;

		private FileContentFetchingFromCrucibleOperation(IProgressMonitor monitor, TaskRepository taskRepository,
				IResource workspaceReviewItem) {
			super(monitor, taskRepository);
			this.workspaceReviewItem = workspaceReviewItem;
		}

		@Override
		public Void run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
				throws RemoteApiException, ServerPasswordNotProvidedException {
			final SubMonitor submonitor = SubMonitor.convert(monitor, "Download file from Crucible", 2);
			final VersionedVirtualFile virtualFile = crucibleFile.getSelectedFile();

			if (virtualFile.getContentUrl() == null) {
				contentUrlMissingPopup();
				return null;
			}

			final byte[] file = getContent(virtualFile.getContentUrl(), server.getSession(serverCfg),
					serverCfg.getUrl());
			submonitor.worked(1);

			if (file == null) {
				contentUrlMissingPopup();
				return null;
			}

			final File localCopy;
			try {
				localCopy = createTempFile(virtualFile.getName(), file);
				submonitor.worked(1);
			} catch (IOException e) {
				StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage()));
				return null;
			}

			// we've downloaded review item, but let's check if by any chance it has the same content as local resource
			// and in this case let's open the local one
			if (workspaceReviewItem != null) {
				try {
					if (FileUtils.contentEquals(localCopy, workspaceReviewItem.getRawLocation().toFile())) {
						IEditorPart editor = editLocalFile(crucibleFile, (IFile) workspaceReviewItem);

						if (editor != null) {
							CrucibleUiUtil.focusOnComment(editor, crucibleFile, comment);
							return null;
						}
					}
				} catch (IOException e) {
					StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
							"Failed to compare local resources. Falling back to Crucible remote review item.", e));
				} catch (CoreException e) {
					StatusHandler.log(e.getStatus());
				}
			}

			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					try {
						String editorId = getEditorId(PlatformUI.getWorkbench(), virtualFile.getName());
						IEditorPart editor = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow()
								.getActivePage()
								.openEditor(
										new CruciblePreCommitFileInput(new CruciblePreCommitFileStorage(crucibleFile,
												file, localCopy)), editorId);

						if (editor != null) {
							CrucibleUiUtil.focusOnComment(editor, crucibleFile, comment);
						}
					} catch (PartInitException e) {
						StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
								"Failed to initialize editor", e));
					}
				}
			});
			return null;
		}
	}
}
