/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
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
import org.eclipse.ui.PlatformUI;

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

	public boolean openFile(String repoUrl, String filePath, String revisionString, IProgressMonitor monitor) {
		openFileWithTeamApi(repoUrl, filePath, revisionString, monitor);
		return true;
	}

	private static void openFileWithTeamApi(String repoUrl, String filePath, final String revisionString,
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
				return;
			}

			IProject project = resource.getProject();

			if (project == null) {
				StatusHandler.log(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID,
						"Unable to get project for resource", new Exception()));
				return;
			}

			RepositoryProvider rp = RepositoryProvider.getProvider(project);
			if (rp != null && rp.getFileHistoryProvider() != null) {

				// this project has a team nature associated with it in the workspace
				final IFileHistoryProvider historyProvider = rp.getFileHistoryProvider();

				IFileRevision localFileRevision = historyProvider.getWorkspaceFileRevision(resource);

				boolean inSync = isRemoteFileInSync(resource, rp);

				if (inSync && localFileRevision.getContentIdentifier() != null
						&& localFileRevision.getContentIdentifier().equals(revisionString)) {
					TeamUiUtils.openLocalResource(resource);
				} else {
					if (Display.getCurrent() != null) {
						openRemoteResource(revisionString, resource, historyProvider, monitor);
					} else {
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							public void run() {
								openRemoteResource(revisionString, resource, historyProvider, monitor);
							}
						});
					}

				}

			} else {
				openNotTeamResourceErrorMessage(repoUrl, filePath, revisionString);
			}
		} else {
			TeamUiUtils.openFileDoesntExistErrorMessage(repoUrl, filePath, revisionString);
		}
	}

	private static void openRemoteResource(String revisionString, IResource resource,
			IFileHistoryProvider historyProvider, IProgressMonitor monitor) {
		// we need a different revision than the one in the local workspace
		IFileHistory fileHistory = historyProvider.getFileHistoryFor(resource, IFileHistoryProvider.NONE, monitor);

		if (fileHistory != null) {
			IFileRevision remoteFileRevision = fileHistory.getFileRevision(revisionString);
			if (remoteFileRevision != null) {
				try {
					Utils.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
							remoteFileRevision, monitor);
				} catch (CoreException e) {
					StatusHandler.log(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
		}
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
