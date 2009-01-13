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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRemoteFile;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.ISVNResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.core.resources.RemoteFile;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.editor.RemoteFileEditorInput;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.utils.SVNUrlUtils;

import java.net.MalformedURLException;
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
		return SVNProviderPlugin.getPlugin().getRepositories().isKnownRepository(repoUrl, false);
	}

	public boolean openFile(String repoUrl, String filePath, String revisionString, IProgressMonitor monitor) {
		IWorkbench workbench = AtlassianSubclipseUiPlugin.getDefault().getWorkbench();
		IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();

		try {

			ISVNRepositoryLocation location = SVNProviderPlugin.getPlugin().getRepositories().getRepository(repoUrl);

			if (filePath.startsWith("/")) {
				filePath = filePath.substring(1);
			}

			IResource localResource = getLocalResource(location, filePath);

			if (localResource != null) {
				ISVNRemoteFile remoteFile = getRemoteFile(localResource, revisionString, location);

				if (remoteFile != null) {
					RemoteFileEditorInput editorInput = new RemoteFileEditorInput(remoteFile, monitor);
					String editorId = getEditorId(workbench, remoteFile);
					page.openEditor(editorInput, editorId);

					return true;
				} else {
					MessageDialog.openInformation(null, "Unable to find file", "May have been deleted");
				}
			} else {
				MessageDialog.openInformation(null, "Unable to find file",
						"This file does not exist in your local workspace");
			}
		} catch (SVNException e) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID, e.getMessage(), e));
		} catch (ParseException e) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID, e.getMessage(), e));
		} catch (PartInitException e) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}
		return false;
	}

	private ISVNRemoteFile getRemoteFile(IResource localResource, String revisionString, ISVNRepositoryLocation location)
			throws ParseException, SVNException {

		SVNRevision svnRevision = SVNRevision.getRevision(revisionString);
		ISVNLocalResource local = SVNWorkspaceRoot.getSVNResourceFor(localResource);

		if (local.isManaged()) {
			return (ISVNRemoteFile) local.getRemoteResource(svnRevision);
		}

		return null;
	}

	private IResource getLocalResource(ISVNRepositoryLocation location, String filePath) {
		SVNUrl fileUrl = location.getUrl().appendPath(filePath);

		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {

			if (SVNWorkspaceRoot.isManagedBySubclipse(project)) {
				try {
					SVNTeamProvider teamProvider = (SVNTeamProvider) RepositoryProvider.getProvider(project,
							SVNProviderPlugin.getTypeId());
					if (location.equals(teamProvider.getSVNWorkspaceRoot().getRepository())) {
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

	private RemoteFile getRemoteFile(String repoUrl, String filePath, String revisionString,
			ISVNRepositoryLocation location) throws MalformedURLException, ParseException, SVNException {
		RemoteFile file;
		SVNUrl svnUrl = new SVNUrl(repoUrl).appendPath(filePath);
		SVNRevision svnRevision = SVNRevision.getRevision(revisionString);

		ISVNClientAdapter svnClient = location.getSVNClient();
		ISVNInfo info = null;
		try {
			if (location.getRepositoryRoot().equals(svnUrl)) {
				file = new RemoteFile(location, svnUrl, svnRevision);
			} else {
				info = svnClient.getInfo(svnUrl, svnRevision, svnRevision);
			}
		} catch (SVNClientException e) {
			throw new SVNException("Can't get latest remote resource for " + svnUrl);
		}

		if (info == null) {
			file = null;//new RemoteFile(location, svnUrl, svnRevision);
		} else {
			file = new RemoteFile(
					null, // we don't know its parent
					location, svnUrl, svnRevision, info.getLastChangedRevision(), info.getLastChangedDate(),
					info.getLastCommitAuthor());
		}
		return file;
	}

	private IResource getLocalResourceForRemove(IResourceVariant resource) throws SVNException {
		return null;
	}

	private IResourceVariant getRemoteResourceForLocal(IResource resource) throws SVNException {
		ISVNLocalResource local = SVNWorkspaceRoot.getSVNResourceFor(resource);
		return local.getRemoteResource(SVNRevision.BASE);
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

}
