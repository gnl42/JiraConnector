package com.atlassian.connector.eclipse.internal.subversive.core;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.svnstorage.SVNRepositoryFile;

import com.atlassian.theplugin.commons.util.MiscUtil;

public final class SubversiveUtil {
	
	private SubversiveUtil() {
		// ignore
	}

	public static IRepositoryLocation getRepositoryLocation(String url) {
		IRepositoryLocation[] repositories = SVNRemoteStorage.instance().getRepositoryLocations();
		if (repositories != null) {
			for (IRepositoryLocation repository : repositories) {
				if (repository.getUrl().equals(url)) {
					return repository;
				}
			}
		}
		return null;
	}
	
	public static IResource getLocalResourceFromFilePath(final String path) {
		if (path == null || path.length() <= 0) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubversiveCorePlugin.PLUGIN_ID, "Requested file path is null or empty."));
			return null;
		}
		
		final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		if (projects == null || projects.length == 0) {
			StatusHandler.log(new Status(IStatus.WARNING, AtlassianSubversiveCorePlugin.PLUGIN_ID, "Could not find projects in the workspace."));
		}
		
		final List<IResource> resources = MiscUtil.buildArrayList();
		final int[] matchLevel = new int[] { 0 };

		try {
			ResourcesPlugin.getWorkspace().getRoot().accept(
				new IResourceVisitor() {
					private int matchingLastSegments(
							IPath firstPath, IPath secondPath) {
						int firstPathLen = firstPath.segmentCount();
						int secondPathLen = secondPath
								.segmentCount();
						int max = Math.min(firstPathLen,
								secondPathLen);
						int count = 0;
						for (int i = 1; i <= max; i++) {
							if (!firstPath
									.segment(firstPathLen - i)
									.equals(
											secondPath
													.segment(secondPathLen
															- i))) {
								return count;
							}
							count++;
						}
						return count;
					}

					public boolean visit(IResource resource)
							throws CoreException {
						if (resource.getType() == IResource.PROJECT 
								&& RepositoryProvider.getProvider(resource.getProject(), SVNTeamPlugin.NATURE_ID) == null) {
							return false;
						}
						
						if (!(resource instanceof IFile)) {
							return true; // skip it if it's not a
											// file, but check its
											// members
						}

						int matchCount = matchingLastSegments(
								new Path(path), resource
										.getLocation());
						if (matchCount > 0) {
							if (matchCount > matchLevel[0]) {
								resources.clear();
								matchLevel[0] = matchCount;
							}
							if (matchCount == matchLevel[0]) {
								resources.add(resource);
							}
						}

						return true; // visit also members of this
										// resource
					}
				});
		} catch(CoreException e) {
			StatusHandler.log(e.getStatus());
			return null;
		}
		
		if (resources.size() > 0) {
			return resources.get(0);
		}
		
		StatusHandler.log(new Status(IStatus.WARNING, AtlassianSubversiveCorePlugin.PLUGIN_ID, NLS.bind("Could not find resource for {0}.", path)));
		return null;
	}

	public static IRepositoryFile getSvnRemoteFile(String repoUrl, String filePath, SVNRevision fileRevision,
			final IProgressMonitor monitor) {
		if (repoUrl == null) {
			return null;
		}

		if (filePath.startsWith("/")) {
			filePath = filePath.substring(1);
		}

		IResource localResource = getLocalResourceFromFilePath(filePath);

		if (localResource != null) {
			final IRepositoryResource repResource = SVNRemoteStorage.instance().asRepositoryResource(localResource);
			final IRepositoryFile remoteFile = new SVNRepositoryFile(repResource.getRepositoryLocation(),
					repResource.getUrl(), fileRevision);
			return remoteFile;
		}
		return null;
	}

}
