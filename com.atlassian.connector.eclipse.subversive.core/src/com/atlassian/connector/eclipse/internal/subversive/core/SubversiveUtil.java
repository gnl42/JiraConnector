package com.atlassian.connector.eclipse.internal.subversive.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.svnstorage.SVNRepositoryFile;

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
	
	public static IResource getLocalResourceFromFilePath(String filePath) {
		if (filePath == null || filePath.length() <= 0) {
			return null;
		}
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			// check if project is associated with Subversive Team provider, 
			// if we don't test it asRepositoryResource will throw RuntimeException
			RepositoryProvider provider = RepositoryProvider.getProvider(project, SVNTeamPlugin.NATURE_ID);
			if (provider == null) {
				continue;
			}

			try {
				IPath fileIPath = new Path(filePath);
				IResource resource = project.findMember(fileIPath);
				while (!fileIPath.isEmpty() && resource == null) {
					fileIPath = fileIPath.removeFirstSegments(1);
					resource = project.findMember(fileIPath);
				}
				if (resource == null) {
					continue;
				}

				IRepositoryResource projectResource = SVNRemoteStorage.instance().asRepositoryResource(resource);

				if (projectResource != null && projectResource.getUrl() != null
						&& projectResource.getUrl().endsWith(filePath)) {
					return resource;
				}
			} catch (Exception e) {
				StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubversiveCorePlugin.PLUGIN_ID, e.getMessage(), e));
			}
		}
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
