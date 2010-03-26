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

package com.atlassian.connector.eclipse.team.ui;

import com.atlassian.connector.eclipse.team.ui.exceptions.UnsupportedTeamProviderException;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.framework.Bundle;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

/**
 * A default team resource provider that just uses the limited Eclipse team API
 * 
 * @author Shawn Minto
 */
public class DefaultTeamUiResourceConnector extends AbstractTeamUiConnector {

	private static final String SUBVERSIVE_MINIMUM_VERSION = "0.7.8";

	public static final String TEAM_PROV_ID_SVN_SUBVERSIVE = "org.eclipse.team.svn.core.svnnature";

	public boolean isEnabled() {
		// the default one is always enabled
		return true;
	}

	@NotNull
	public SortedSet<ICustomChangesetLogEntry> getLatestChangesets(@NotNull String repositoryUrl, int limit,
			IProgressMonitor monitor) throws CoreException {
		//TODO
		throw new CoreException(new Status(IStatus.WARNING, AtlassianTeamUiPlugin.PLUGIN_ID, "Not implemented yet."));
	}

	public IFileRevision getFileRevision(IResource resource, String revisionString, IProgressMonitor monitor) {
		IProject project = resource.getProject();

		if (project == null) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianTeamUiPlugin.PLUGIN_ID,
					"Unable to get project for resource"));
			return null;
		}

		RepositoryProvider rp = RepositoryProvider.getProvider(project);
		checkIfSupportedTeamProvider(rp);
		if (rp != null) {
			// this project has a team nature associated with it in the workspace
			IFileHistoryProvider historyProvider = rp.getFileHistoryProvider();
			if (historyProvider != null) {
				IFileHistory fileHistory = historyProvider.getFileHistoryFor(resource, IFileHistoryProvider.NONE,
						monitor);
				if (fileHistory != null) {
					IFileRevision fileRevision = fileHistory.getFileRevision(revisionString);
					if (fileRevision == null) {
						StatusHandler.log(new Status(IStatus.ERROR, AtlassianTeamUiPlugin.PLUGIN_ID, NLS.bind(
								"Could not get revision {0}", revisionString)));
					}
					return fileRevision;
				} else {
					StatusHandler.log(new Status(IStatus.ERROR, AtlassianTeamUiPlugin.PLUGIN_ID, NLS.bind(
							"Could not get file history for {0}", resource.getName())));
				}
			} else {
				StatusHandler.log(new Status(IStatus.ERROR, AtlassianTeamUiPlugin.PLUGIN_ID,
						"Could not get file history provider"));
			}
		} else {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianTeamUiPlugin.PLUGIN_ID, NLS.bind(
					"Could not get repository provider for project {0}", project.getName())));
		}
		return null;

	}

	public LocalStatus getLocalRevision(IResource resource) throws CoreException {
		//resource
		final IProject project = resource.getProject();
		if (project == null) {
			return null;
		}

		RepositoryProvider rp = RepositoryProvider.getProvider(project);
		checkIfSupportedTeamProvider(rp);
		if (rp == null) {
			return null;
		}

		final IFileHistoryProvider historyProvider = rp.getFileHistoryProvider();
		if (historyProvider != null) {
			// this project has a team nature associated with it in the workspace

			IFileRevision localFileRevision = historyProvider.getWorkspaceFileRevision(resource);
			if (localFileRevision == null) {
				return null;
			}
			final URI uri = localFileRevision.getURI();
			if (uri != null) {
				// for CVS URI will include also query with revision number (like ?r=3.23), we want to cut it off
				// without introducing dependency on CVS
				final String query = uri.getQuery();
				String uriStr = uri.toString();
				final int index = uriStr.lastIndexOf("?" + query);
				if (index != -1) {
					uriStr = uriStr.substring(0, index);
				}

				return LocalStatus.makeVersioned(uriStr, localFileRevision.getContentIdentifier());
			}
			return LocalStatus.makeAdded(localFileRevision.getContentIdentifier(), false);
//
//			boolean inSync = isRemoteFileInSync(file, rp);
//
//			if (inSync && localFileRevision.getContentIdentifier() != null) {
//
//				for (CrucibleFileInfo fileInfo : activeReviewFiles) {
//					VersionedVirtualFile fileDescriptor = fileInfo.getFileDescriptor();
//					VersionedVirtualFile oldFileDescriptor = fileInfo.getOldFileDescriptor();
//
//					IPath newPath = new Path(fileDescriptor.getUrl());
//					final IResource newResource = ResourcesPlugin.getWorkspace().getRoot().findMember(newPath);
//
//					IPath oldPath = new Path(fileDescriptor.getUrl());
//					final IResource oldResource = ResourcesPlugin.getWorkspace().getRoot().findMember(oldPath);
//
//					if ((newResource != null && newResource.equals(file))
//							|| (oldResource != null && oldResource.equals(file))) {
//
//						String revision = localFileRevision.getContentIdentifier();
//
//						if (revision.equals(fileDescriptor.getRevision())) {
//							return new CrucibleFile(fileInfo, false);
//						}
//						if (revision.equals(oldFileDescriptor.getRevision())) {
//							return new CrucibleFile(fileInfo, true);
//						}
//					}
//				}
//
//				return null;
//			}
		}
		return null;
	}

	public Collection<ScmRepository> getRepositories(IProgressMonitor monitor) {
		// @todo wseliga implement it
		return Collections.emptyList();
	}

	public ScmRepository getApplicableRepository(IResource resource) {
		// @todo wseliga
		return null;
	}

	public String getName() {
		return "Team API (partial support)";
	}

	public boolean haveMatchingResourcesRecursive(IResource[] roots, State filter) {
		return false;
	}

	@NotNull
	public Collection<UploadItem> getUploadItemsForResources(@NotNull IResource[] resources,
			@NotNull IProgressMonitor monitor) throws CoreException {
		return MiscUtil.buildArrayList();
	}

	@NotNull
	public IResource[] getMembersForContainer(@NotNull IContainer element) throws CoreException {
		return new IResource[0];
	}

	public List<IResource> getResourcesByFilterRecursive(IResource[] roots, State filter) {
		return MiscUtil.buildArrayList();
	}

	public boolean isResourceManagedBy(IResource resource) {
		return false;
	}

	private static void checkIfSupportedTeamProvider(RepositoryProvider rp) {
		//only support subversive > 0.7.8
		Bundle bundle = Platform.getBundle("org.eclipse.team.svn");
		if (bundle != null) {
			Object version = bundle.getHeaders().get("Bundle-Version");
			if (version != null && version instanceof String) {
				if (((String) version).compareTo(SUBVERSIVE_MINIMUM_VERSION) < 0) {
					throw new UnsupportedTeamProviderException("Subversive versions < 0.7.8 are not supported");
				} else {
					return;
				}
			}
		}
		if (rp != null && rp.getID().equals(TEAM_PROV_ID_SVN_SUBVERSIVE)) {
			throw new UnsupportedTeamProviderException("Subversive not supported");
		}
	}

	private String getLocalEncoding(IResource resource) {
		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			try {
				return file.getCharset();
			} catch (CoreException e) {
				StatusHandler.log(e.getStatus());
			}
		}
		return null;
	}

	@Nullable
	public CrucibleFile getCrucibleFileFromReview(@NotNull Review activeReview, @NotNull String fileUrl,
			@NotNull String revision) {

		for (CrucibleFileInfo fileInfo : activeReview.getFiles()) {
			VersionedVirtualFile fileDescriptor = fileInfo.getFileDescriptor();
			VersionedVirtualFile oldFileDescriptor = fileInfo.getOldFileDescriptor();

			String oldUrl = oldFileDescriptor.getUrl();
			String newUrl = fileDescriptor.getUrl();

			if ((newUrl != null && newUrl.length() > 0 && fileUrl.endsWith(newUrl))
					|| (oldUrl != null && oldUrl.length() > 0 && fileUrl.endsWith(oldUrl))) {

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

	public CrucibleFile getCrucibleFileFromReview(Review review, IFile file) {
		IProject project = file.getProject();

		if (project == null) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianTeamUiPlugin.PLUGIN_ID,
					"Unable to get project for resource"));
			return null;
		}

		RepositoryProvider rp = RepositoryProvider.getProvider(project);
		checkIfSupportedTeamProvider(rp);
		if (rp != null && rp.getFileHistoryProvider() != null) {
			// this project has a team nature associated with it in the workspace
			final IFileHistoryProvider historyProvider = rp.getFileHistoryProvider();

			IFileRevision localFileRevision = historyProvider.getWorkspaceFileRevision(file);

			boolean inSync = isRemoteFileInSync(file, rp);

			if (inSync && localFileRevision.getContentIdentifier() != null) {

				for (CrucibleFileInfo fileInfo : review.getFiles()) {
					VersionedVirtualFile fileDescriptor = fileInfo.getFileDescriptor();
					VersionedVirtualFile oldFileDescriptor = fileInfo.getOldFileDescriptor();

					IPath newPath = new Path(fileDescriptor.getUrl());
					final IResource newResource = findResourceForPath(newPath.toPortableString());

					IPath oldPath = new Path(fileDescriptor.getUrl());
					final IResource oldResource = findResourceForPath(oldPath.toPortableString());

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
		return null;
	}

	private static IResource findResourceForPath(String filePath) {
		IPath path = new Path(filePath);
		final IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
		if (resource == null) {
			return findResourceForPath2(filePath);
		}
		return resource;
	}

	private static IResource findResourceForPath2(String filePath) {
		if (filePath == null || filePath.length() <= 0) {
			return null;
		}
		IContainer location = ResourcesPlugin.getWorkspace().getRoot();

		IPath path = new Path(filePath);
		IResource resource = null;
		while (!path.isEmpty() && resource == null) {
			resource = match(location, path);
			path = path.removeFirstSegments(1);
		}
		return resource;
	}

	private static IResource match(IContainer location, IPath path) {
		if (!path.isEmpty()) {
			return location.findMember(path);
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
					StatusHandler.log(new Status(IStatus.WARNING, AtlassianTeamUiPlugin.PLUGIN_ID,
							"Unable to determine if file is in sync.  Trying to open remote file."));
				}
			} catch (TeamException e) {
				StatusHandler.log(new Status(IStatus.WARNING, AtlassianTeamUiPlugin.PLUGIN_ID,
						"Unable to determine if file is in sync.  Trying to open remote file.", e));
			}
		} else {
			StatusHandler.log(new Status(IStatus.WARNING, AtlassianTeamUiPlugin.PLUGIN_ID,
					"Unable to determine if file is in sync.  Trying to open remote file."));
		}
		return inSync;
	}

	public boolean isResourceAcceptedByFilter(IResource resource, State state) {
		// ignore
		return false;
	}

	public boolean canHandleFile(IFile file) {
		return true;
	}

}
