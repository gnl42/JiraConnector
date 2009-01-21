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

package com.atlassian.connector.eclipse.ui.team;

import com.atlassian.connector.eclipse.ui.AtlassianUiPlugin;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * A default team resource provider that just uses the Eclipse team API
 * 
 * @author Shawn Minto
 */
public class DefaultTeamResourceConnector implements ITeamResourceConnector {

	public boolean canHandleFile(String repoUrl, String filePath, String revisionString, IProgressMonitor monitor) {
		// the default one handles anything
		return true;
	}

	public boolean isEnabled() {
		// the default one is always enabled
		return true;
	}

	public IEditorPart openFile(String repoUrl, String filePath, String revisionString, IProgressMonitor monitor) {
		return openFileWithTeamApi(repoUrl, filePath, revisionString, monitor);
	}

	public boolean canGetCrucibleFileFromEditorInput(IEditorInput editorInput) {
		// we cannot deal with remote files since they are team provider specific
		return editorInput instanceof FileEditorInput;
	}

	public CrucibleFile getCorrespondingCrucibleFileFromEditorInput(IEditorInput editorInput, Review activeReview) {
		if (editorInput instanceof FileEditorInput) {
			// this will only work on local files since they remote files are team provider specific

			IFile file = ((FileEditorInput) editorInput).getFile();

			try {
				IProject project = file.getProject();

				if (project == null) {
					StatusHandler.log(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID,
							"Unable to get project for resource", new Exception()));
					return null;
				}

				RepositoryProvider rp = RepositoryProvider.getProvider(project);
				if (rp != null && rp.getFileHistoryProvider() != null) {

					// this project has a team nature associated with it in the workspace
					final IFileHistoryProvider historyProvider = rp.getFileHistoryProvider();

					IFileRevision localFileRevision = historyProvider.getWorkspaceFileRevision(file);

					boolean inSync = isRemoteFileInSync(file, rp);

					if (inSync && localFileRevision.getContentIdentifier() != null) {

						for (CrucibleFileInfo fileInfo : activeReview.getFiles()) {
							VersionedVirtualFile fileDescriptor = fileInfo.getFileDescriptor();
							VersionedVirtualFile oldFileDescriptor = fileInfo.getOldFileDescriptor();

							IPath newPath = new Path(fileDescriptor.getUrl());
							final IResource newResource = ResourcesPlugin.getWorkspace().getRoot().findMember(newPath);

							IPath oldPath = new Path(fileDescriptor.getUrl());
							final IResource oldResource = ResourcesPlugin.getWorkspace().getRoot().findMember(oldPath);

							if ((newResource != null && newResource.equals(file))
									|| (oldResource != null && oldResource.equals(file))) {

								String revision = localFileRevision.getContentIdentifier();

								if (revision.equals(fileDescriptor.getRevision())) {
									return new CrucibleFile(fileInfo, false);
								}
								if (revision.equals(oldFileDescriptor.getRevision())) {
									return new CrucibleFile(fileInfo, true);
								}
							}
						}

						return null;
					}
				}
			} catch (ValueNotYetInitialized e) {
				StatusHandler.log(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID,
						"Review is not fully initialized.  Unable to get file from review.", e));
			}
		}
		return null;
	}

	private static IEditorPart openFileWithTeamApi(String repoUrl, String filePath, final String revisionString,
			final IProgressMonitor monitor) {
		// this is a good backup (Works for cvs and anyone that uses the history provider

		// TODO add support for finding a project in the path so that we can find the proper resource 
		// (i.e. file path and project name may be different)
		IPath path = new Path(filePath);
		final IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);

		if (resource != null) {
			if (!(resource instanceof IFile)) {
				StatusHandler.log(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID, "Resource is not a file: "
						+ resource.getProjectRelativePath(), new Exception()));
				return null;
			}

			IProject project = resource.getProject();

			if (project == null) {
				StatusHandler.log(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID,
						"Unable to get project for resource", new Exception()));
				return null;
			}

			RepositoryProvider rp = RepositoryProvider.getProvider(project);
			if (rp != null && rp.getFileHistoryProvider() != null) {

				// this project has a team nature associated with it in the workspace
				final IFileHistoryProvider historyProvider = rp.getFileHistoryProvider();

				IFileRevision localFileRevision = historyProvider.getWorkspaceFileRevision(resource);

				boolean inSync = isRemoteFileInSync(resource, rp);

				if (inSync && localFileRevision.getContentIdentifier() != null
						&& localFileRevision.getContentIdentifier().equals(revisionString)) {
					return TeamUiUtils.openLocalResource(resource);
				} else {
					if (Display.getCurrent() != null) {
						return openRemoteResource(revisionString, resource, historyProvider, monitor);
					} else {
						final IEditorPart[] part = new IEditorPart[1];
						PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
							public void run() {
								part[0] = openRemoteResource(revisionString, resource, historyProvider, monitor);
							}
						});
						return part[0];
					}

				}

			} else {
				openNotTeamResourceErrorMessage(repoUrl, filePath, revisionString);
			}
		} else {
			TeamUiUtils.openFileDoesntExistErrorMessage(repoUrl, filePath, revisionString);
		}
		return null;
	}

	private static IEditorPart openRemoteResource(String revisionString, IResource resource,
			IFileHistoryProvider historyProvider, IProgressMonitor monitor) {
		// we need a different revision than the one in the local workspace
		IFileHistory fileHistory = historyProvider.getFileHistoryFor(resource, IFileHistoryProvider.NONE, monitor);

		if (fileHistory != null) {
			IFileRevision remoteFileRevision = fileHistory.getFileRevision(revisionString);
			if (remoteFileRevision != null) {
				try {
					return Utils.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
							remoteFileRevision, monitor);
				} catch (CoreException e) {
					StatusHandler.log(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
		}
		return null;
	}

	private static boolean isRemoteFileInSync(IResource resource, RepositoryProvider rp) {
		boolean inSync = false;
		Subscriber subscriber = rp.getSubscriber();
		if (subscriber != null) {
			try {
				SyncInfo syncInfo = subscriber.getSyncInfo(resource);
				if (syncInfo != null) {
					inSync = SyncInfo.isInSync(syncInfo.getKind());
				} else {
					StatusHandler.log(new Status(IStatus.WARNING, AtlassianUiPlugin.PLUGIN_ID,
							"Unable to determine if file is in sync.  Trying to open remote file.", new Exception()));
				}
			} catch (TeamException e) {
				StatusHandler.log(new Status(IStatus.WARNING, AtlassianUiPlugin.PLUGIN_ID,
						"Unable to determine if file is in sync.  Trying to open remote file.", e));
			}
		} else {
			StatusHandler.log(new Status(IStatus.WARNING, AtlassianUiPlugin.PLUGIN_ID,
					"Unable to determine if file is in sync.  Trying to open remote file.", new Exception()));
		}
		return inSync;
	}

	public static void openNotTeamResourceErrorMessage(final String repoUrl, final String filePath,
			final String revision) {
		if (Display.getCurrent() != null) {
			internalOpenNotTeamResourceErrorMessage(repoUrl, filePath, revision);
		} else {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					internalOpenNotTeamResourceErrorMessage(repoUrl, filePath, revision);
				}
			});
		}
	}

	private static void internalOpenNotTeamResourceErrorMessage(String repoUrl, String filePath, String revision) {
		String fileUrl = repoUrl != null ? repoUrl : "" + filePath;
		String message = "Please checkout the project as the following file is not managed by a team provider:\n\n"
				+ fileUrl;

		MessageDialog.openWarning(null, "Crucible", message);
	}
}
