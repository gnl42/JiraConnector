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

package com.atlassian.connector.eclipse.internal.perforce.ui;

import com.atlassian.connector.eclipse.team.ui.AbstractTeamUiConnector;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.connector.eclipse.team.ui.LocalStatus;
import com.atlassian.connector.eclipse.team.ui.ScmRepository;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.P4ConnectionManager;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

/**
 * Connector to handle connecting to a Perforce repository
 * 
 * @author Pawel Niewiadomski
 * @author Wojciech Seliga
 */
public class PerforceTeamUiResourceConnector extends AbstractTeamUiConnector {

	public boolean isEnabled() {
		return true;
	}

	public Collection<ScmRepository> getRepositories(IProgressMonitor monitor) {
		List<ScmRepository> scms = MiscUtil.buildArrayList();
		P4ConnectionManager mgr = P4ConnectionManager.getManager();
		IP4Connection[] cons = mgr.getConnections();
		if (cons != null) {
			for (IP4Connection con : cons) {
				try {
					scms.add(new ScmRepository(getScmPath(con), con.getClientName(), this));
				} catch (CoreException e) {
					StatusHandler.log(e.getStatus());
				}
			}
		}
		return scms;
	}

	public LocalStatus getLocalRevision(IResource resource) throws CoreException {
		final IProject project = resource.getProject();
		if (project == null) {
			return null;
		}
		if (isResourceManagedBy(resource) && resource.getType() == IResource.FILE) {
			try {
				final IP4Resource scmResource = P4Workspace.getWorkspace().getResource(resource);
				final IP4File scmFile = (IP4File) (scmResource instanceof IP4File ? scmResource : null);
				if (scmResource == null) {
					return null;
				}

				if (IStateFilter.SF_UNVERSIONED.accept(scmResource)) {
					return LocalStatus.makeUnversioned();
				}

				if (IStateFilter.SF_IGNORED.accept(scmResource)) {
					return LocalStatus.makeIngored();
				}

				String mimeTypeProp = null;
				if (scmFile != null) {
					mimeTypeProp = scmFile.getOpenedType() != null ? scmFile.getOpenedType() : scmFile.getHeadType();
				}
				boolean isBinary = (mimeTypeProp != null && !mimeTypeProp.startsWith("text"));

				if (IStateFilter.SF_ADDED.accept(scmResource)) {
					return LocalStatus.makeAdded(getScmPath(scmResource), isBinary);
				}

				return LocalStatus.makeVersioned(getScmPath(scmResource),
						scmFile != null ? String.valueOf(scmFile.getHeadChange()) : null,
						scmFile != null ? String.valueOf(scmFile.getHeadChange()) : null,
						scmFile != null ? scmFile.isOpened() : false, isBinary);
			} catch (RuntimeException e) {
				throw new CoreException(new Status(IStatus.ERROR, AtlassianPerforceUiPlugin.PLUGIN_ID,
						"Cannot determine local revision for [" + resource.getName() + "]", e));
			}
		}

		return null;
	}

	private String getScmPath(IP4Resource scmResource) throws CoreException {
		try {
			return "p4://" + scmResource.getClient().getServer().getServerInfo().getServerAddress()
					+ scmResource.getActionPath();
		} catch (P4JavaException e) {
			throw new CoreException(new Status(IStatus.ERROR, AtlassianPerforceUiPlugin.PLUGIN_ID,
					"Failed to get server address", e));
		}
	}

	public ScmRepository getApplicableRepository(IResource resource) {
		final IProject project = resource.getProject();
		if (project == null) {
			return null;
		}

		if (!isResourceManagedBy(resource)) {
			return null;
		}

		final IP4Resource repositoryLocation = P4Workspace.getWorkspace().getResource(resource);
		try {
			return new ScmRepository(getScmPath(repositoryLocation), getScmPath(repositoryLocation),
					repositoryLocation.getClient().getName(), this);
		} catch (CoreException e) {
			return null;
		}
	}

	public String getName() {
		return "Perforce";
	}

	public boolean haveMatchingResourcesRecursive(IResource[] roots, State filter) {
		return FileUtility.checkForResourcesPresenceRecursive(roots, getStateFilter(filter));
	}

	public Collection<UploadItem> getUploadItemsForResources(IResource[] resources, IProgressMonitor monitor)
			throws CoreException {
		final List<UploadItem> items = MiscUtil.buildArrayList();
		for (IResource resource : resources) {
			if (resource.getType() != IResource.FILE) {
				// ignore anything but files
				continue;
			}

			final IP4File scmResource = (IP4File) P4Workspace.getWorkspace().getResource(resource);
			final String fileName = getResourcePathWithProjectName(resource);

			// Crucible crashes if newContent is empty so ignore empty files (or mark them)
			if (IStateFilter.SF_UNVERSIONED.accept(scmResource) || IStateFilter.SF_ADDED.accept(scmResource)
					|| IStateFilter.SF_IGNORED.accept(scmResource)) {
				byte[] newContent = getResourceContent((IFile) resource);
				items.add(new UploadItem(fileName, new byte[0], newContent.length == 0 ? EMPTY_ITEM : newContent));
			} else if (IStateFilter.SF_DELETED.accept(scmResource)) {
				items.add(new UploadItem(fileName, getResourceContent(scmResource.getHeadContents()), DELETED_ITEM));
			} else if (IStateFilter.SF_ANY_CHANGE.accept(scmResource)) {
				byte[] newContent = getResourceContent((IFile) resource);
				items.add(new UploadItem(fileName, getResourceContent(scmResource.getHeadContents()),
						newContent.length == 0 ? EMPTY_ITEM : newContent));
			}
		}
		return items;
	}

	public List<IResource> getResourcesByFilterRecursive(IResource[] roots, State filter) {
		return FileUtility.getResourcesByFilterRecursive(roots, getStateFilter(filter));
	}

	public boolean isResourceAcceptedByFilter(IResource resource, State filter) {
		IP4Resource svnResource = P4Workspace.getWorkspace().getResource(resource);
		return getStateFilter(filter).accept(svnResource);
	}

	public boolean isResourceManagedBy(IResource resource) {
		if (!isEnabled()) {
			return false;
		}
		return FileUtility.isManagedByPerforce(resource);
	}

	public boolean canHandleFile(IFile file) {
		final IProject project = file.getProject();
		if (project == null) {
			return false;
		}

		if (!isResourceManagedBy(file)) {
			return false;
		}

		IP4Resource localFile = P4Workspace.getWorkspace().getResource(file);
		if (localFile != null && !IStateFilter.SF_ANY_CHANGE.accept(localFile)) {
			return true;
		}

		return false;
	}

	public CrucibleFile getCrucibleFileFromReview(Review activeReview, IFile file) {
		IP4File localFile = (IP4File) P4Workspace.getWorkspace().getResource(file);
		if (localFile != null && !IStateFilter.SF_ANY_CHANGE.accept(localFile)) {
			String fileUrl;
			try {
				fileUrl = getScmPath(localFile);
			} catch (CoreException e) {
				return null;
			}
			String revision = String.valueOf(localFile.getHaveRevision());
			if (fileUrl != null && revision != null) {
				return getCrucibleFileFromReview(activeReview, fileUrl, revision);
			}
		}
		return null;
	}

	public CrucibleFile getCrucibleFileFromReview(Review activeReview, String fileUrl, String revision) {
		for (CrucibleFileInfo file : activeReview.getFiles()) {
			VersionedVirtualFile fileDescriptor = file.getFileDescriptor();
			VersionedVirtualFile oldFileDescriptor = file.getOldFileDescriptor();
			String newFileUrl = null;
			String newAbsoluteUrl = getAbsoluteUrl(fileDescriptor);
			if (newAbsoluteUrl != null) {
				newFileUrl = newAbsoluteUrl;
			}

			String oldFileUrl = null;
			String oldAbsoluteUrl = getAbsoluteUrl(oldFileDescriptor);
			if (oldAbsoluteUrl != null) {
				oldFileUrl = oldAbsoluteUrl;
			}
			if ((newFileUrl != null && newFileUrl.equals(fileUrl))
					|| (oldFileUrl != null && oldFileUrl.equals(fileUrl))) {
				if (revision.equals(fileDescriptor.getRevision())) {
					return new CrucibleFile(file, false);
				}
				if (revision.equals(oldFileDescriptor.getRevision())) {
					return new CrucibleFile(file, true);
				}
				return null;
			}
		}
		return null;
	}

	private String getAbsoluteUrl(VersionedVirtualFile fileDescriptor) {
		//TODO might need some performance tweak, but works for now for M2
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (!isResourceManagedBy(project)) {
				continue;
			}

			try {
				IPath fileIPath = new Path(fileDescriptor.getUrl());
				IResource resource = project.findMember(fileIPath);
				while (!fileIPath.isEmpty() && resource == null) {
					fileIPath = fileIPath.removeFirstSegments(1);
					resource = project.findMember(fileIPath);
				}
				if (resource == null) {
					continue;
				}

				IP4Resource projectResource = P4Workspace.getWorkspace().getResource(resource);
				if (projectResource instanceof IP4File && getScmPath(projectResource).endsWith(fileDescriptor.getUrl())) {
					return getScmPath(projectResource);
				}
			} catch (Exception e) {
				StatusHandler.log(new Status(IStatus.ERROR, AtlassianPerforceUiPlugin.PLUGIN_ID, e.getMessage(), e));
			}
		}
		return null;
	}

	private IStateFilter getStateFilter(State filter) {
		switch (filter) {
		case SF_ANY_CHANGE:
			return IStateFilter.SF_ANY_CHANGE;
		case SF_ALL:
			return IStateFilter.SF_ALL;
		case SF_IGNORED:
			return IStateFilter.SF_IGNORED;
		case SF_UNVERSIONED:
			return IStateFilter.SF_UNVERSIONED;
		case SF_VERSIONED:
			return IStateFilter.SF_VERSIONED;
		default:
			throw new IllegalStateException("Unhandled IStateFilter");
		}
	}

	@NotNull
	public IResource[] getMembersForContainer(IContainer element) throws CoreException {
		return FileUtility.getAllMembers(element);
	}

	private byte[] getResourceContent(IStorage resource) {
		InputStream is;
		try {
			is = resource.getContents();
		} catch (CoreException e) {
			return new byte[0];
		}
		final ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			IOUtils.copy(is, out);
			return out.toByteArray();
		} catch (IOException e) {
			return new byte[0];
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(out);
		}
	}

}
