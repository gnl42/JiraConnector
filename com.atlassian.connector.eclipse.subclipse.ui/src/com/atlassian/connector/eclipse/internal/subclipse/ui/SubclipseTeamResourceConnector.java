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

package com.atlassian.connector.eclipse.internal.subclipse.ui;

import com.atlassian.connector.eclipse.ui.team.ITeamResourceConnector;
import com.atlassian.connector.eclipse.ui.team.TeamUiUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.tigris.subversion.subclipse.core.ISVNLocalFile;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRemoteFile;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.ISVNResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.editor.RemoteFileEditorInput;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.utils.SVNUrlUtils;

import java.text.ParseException;

/**
 * Connector to handle connecting to a subclipse repository
 * 
 * @author Shawn Minto
 */
public class SubclipseTeamResourceConnector implements ITeamResourceConnector {

	public boolean isEnabled() {
		return true;
	}

	public boolean canHandleFile(String repoUrl, String filePath, String revisionString, IProgressMonitor monitor) {
		if (repoUrl == null) {
			return false;
		}
		return SVNProviderPlugin.getPlugin().getRepositories().isKnownRepository(repoUrl, false);
	}

	public boolean openFile(String repoUrl, String filePath, String revisionString, final IProgressMonitor monitor) {
		if (repoUrl == null) {
			return false;
		}
		try {

			ISVNRepositoryLocation location = SVNProviderPlugin.getPlugin().getRepositories().getRepository(repoUrl);

			if (filePath.startsWith("/")) {
				filePath = filePath.substring(1);
			}

			IResource localResource = getLocalResourceFromFilePath(location, filePath);

			if (localResource != null) {
				SVNRevision svnRevision = SVNRevision.getRevision(revisionString);
				ISVNLocalFile localFile = getLocalFile(localResource, location);

				if (localFile.getRevision().equals(svnRevision) && !localFile.isDirty()) {
					// the file is not dirty and we have the right local copy
					TeamUiUtils.openLocalResource(localResource);
					return true;
				} else {
					final ISVNRemoteFile remoteFile = getRemoteFile(localResource, svnRevision, location);
					if (remoteFile != null) {
						// we need to open the remote resource since the file is either dirty or the wrong revision

						if (Display.getCurrent() != null) {
							openRemoteSvnFile(remoteFile, monitor);
						} else {
							PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
								public void run() {
									openRemoteSvnFile(remoteFile, monitor);
								}
							});
						}
						return true;
					} else {
						TeamUiUtils.openFileDeletedErrorMessage(repoUrl, filePath, revisionString);

						return true;
					}
				}
			} else {
				TeamUiUtils.openFileDoesntExistErrorMessage(repoUrl, filePath, revisionString);
				return true;
			}
			// TODO display an error message for these errors?
		} catch (SVNException e) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID, e.getMessage(), e));
		} catch (ParseException e) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}
		return false;
	}

	private void openRemoteSvnFile(ISVNRemoteFile remoteFile, IProgressMonitor monitor) {
		try {
			IWorkbench workbench = AtlassianSubclipseUiPlugin.getDefault().getWorkbench();
			IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();

			RemoteFileEditorInput editorInput = new RemoteFileEditorInput(remoteFile, monitor);
			String editorId = getEditorId(workbench, remoteFile);
			page.openEditor(editorInput, editorId);
		} catch (PartInitException e) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}
	}

	private ISVNLocalFile getLocalFile(IResource localResource, ISVNRepositoryLocation location) throws SVNException {
		ISVNLocalResource local = SVNWorkspaceRoot.getSVNResourceFor(localResource);

		if (local.isManaged()) {
			return (ISVNLocalFile) local;
		}
		return null;
	}

	private ISVNRemoteFile getRemoteFile(IResource localResource, SVNRevision svnRevision,
			ISVNRepositoryLocation location) throws ParseException, SVNException {

		ISVNLocalResource local = SVNWorkspaceRoot.getSVNResourceFor(localResource);

		if (local.isManaged()) {
			return (ISVNRemoteFile) local.getRemoteResource(svnRevision);
		}

		return null;
	}

	private IResource getLocalResourceFromFilePath(ISVNRepositoryLocation location, String filePath) {
		SVNUrl fileUrl = location.getUrl().appendPath(filePath);

		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {

			if (SVNWorkspaceRoot.isManagedBySubclipse(project)) {
				try {
					SVNTeamProvider teamProvider = (SVNTeamProvider) RepositoryProvider.getProvider(project,
							SVNProviderPlugin.getTypeId());
					if (teamProvider != null && location.equals(teamProvider.getSVNWorkspaceRoot().getRepository())) {
						ISVNResource projectResource = SVNWorkspaceRoot.getSVNResourceFor(project);

						if (SVNUrlUtils.getCommonRootUrl(fileUrl, projectResource.getUrl()).equals(
								projectResource.getUrl())) {
							String path = SVNUrlUtils.getRelativePath(projectResource.getUrl(), fileUrl);
							IFile file = project.getFile(new Path(path));
							if (file.exists()) {
								return file;
							}
						}
					}
				} catch (SVNException e) {
					StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
		}
		return null;
	}

	private String getEditorId(IWorkbench workbench, ISVNRemoteFile file) {
		IEditorRegistry registry = workbench.getEditorRegistry();
		String filename = file.getName();
		IEditorDescriptor descriptor = registry.getDefaultEditor(filename);
		String id;
		if (descriptor == null) {
			descriptor = registry.findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
		}
		if (descriptor == null) {
			id = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
		} else {
			id = descriptor.getId();
		}
		return id;
	}

// Code that can work if there is no file in the local workspace 
//	private RemoteFile getRemoteFile(String repoUrl, String filePath, String revisionString,
//	ISVNRepositoryLocation location) throws MalformedURLException, ParseException, SVNException {
//RemoteFile file;
//SVNUrl svnUrl = new SVNUrl(repoUrl).appendPath(filePath);
//SVNRevision svnRevision = SVNRevision.getRevision(revisionString);
//
//ISVNClientAdapter svnClient = location.getSVNClient();
//ISVNInfo info = null;
//try {
//	if (location.getRepositoryRoot().equals(svnUrl)) {
//		file = new RemoteFile(location, svnUrl, svnRevision);
//	} else {
//		info = svnClient.getInfo(svnUrl, svnRevision, svnRevision);
//	}
//} catch (SVNClientException e) {
//	throw new SVNException("Can't get latest remote resource for " + svnUrl);
//}
//
//if (info == null) {
//	file = null;//new RemoteFile(location, svnUrl, svnRevision);
//} else {
//	file = new RemoteFile(
//			null, // we don't know its parent
//			location, svnUrl, svnRevision, info.getLastChangedRevision(), info.getLastChangedDate(),
//			info.getLastCommitAuthor());
//}
//return file;
//}

}
