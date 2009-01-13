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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * A utility class for doing UI related operations for team items
 * 
 * @author Shawn Minto
 */
public final class TeamUiUtils {

	private TeamUiUtils() {
	}

	public static void openFile(String repoUrl, String filePath, String revisionString, IProgressMonitor monitor) {
		// TODO if the repo url is null, we should probably use the task repo host and look at all repos

		assert (filePath != null);
		assert (revisionString != null);

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		TeamResourceManager teamResourceManager = AtlassianUiPlugin.getDefault().getTeamResourceManager();

		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled() && connector.canHandleFile(repoUrl, filePath, revisionString, monitor)) {
				if (connector.openFile(repoUrl, filePath, revisionString, monitor)) {
					return;
				}
			}
		}

		// try a backup solution
		openFileWithTeamApi(repoUrl, filePath, revisionString, monitor);

	}

	private static void openFileWithTeamApi(String repoUrl, String filePath, String revisionString,
			IProgressMonitor monitor) {
		// this is a good backup (Works for cvs and anyone that uses the history provider

		// TODO add support for finding a project in the path so that we can find the proper resource 
		// (i.e. file path and project name may be different)
		IPath path = new Path(filePath);
		IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);

		if (resource != null) {
			if (!(resource instanceof IFile)) {
				MessageDialog.openWarning(null, "Not a File", "The resource to open is not a File.");
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
				IFileHistoryProvider historyProvider = rp.getFileHistoryProvider();

				IFileRevision localFileRevision = historyProvider.getWorkspaceFileRevision(resource);

				boolean inSync = isRemoteFileInSync(resource, rp);

				if (inSync && localFileRevision.getContentIdentifier() != null
						&& localFileRevision.getContentIdentifier().equals(revisionString)) {
					openLocalResource(resource);
				} else {
					openRemoteResource(revisionString, monitor, resource, historyProvider);
				}

			} else {
				MessageDialog.openWarning(null, "Unable to find file", "The file is not managed by a team provider");
			}
		} else {
			MessageDialog.openWarning(null, "Unable to find file", "The file does not exist in your local workspace");
		}
	}

	private static void openRemoteResource(String revisionString, IProgressMonitor monitor, IResource resource,
			IFileHistoryProvider historyProvider) {
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

	public static void openLocalResource(IResource resource) {
		// the local revision matches the revision we care about and the file is in sync
		try {
			IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), (IFile) resource, true);
		} catch (PartInitException e) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID, e.getMessage(), e));
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
}
