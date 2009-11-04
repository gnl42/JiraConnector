package com.atlassian.connector.eclipse.internal.subversive.core;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
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
		
		for (IProject project : projects) {
			// skip projects that are closed or not versioned under Subversive 
			if (!project.isOpen() || RepositoryProvider.getProvider(project, SVNTeamPlugin.NATURE_ID) == null) {
				continue; 
			}
			final IRepositoryResource repositoryResource = SVNRemoteStorage.instance().asRepositoryResource(project);
			if (repositoryResource == null) {
				continue;
			}
			final String repositoryUrl = repositoryResource.getUrl();
			if (repositoryUrl == null) {
				continue;
			}
			String repositoryPath;
			try {
				repositoryPath = new URI(repositoryUrl, false).getPath();
			} catch (URIException e) {
				continue;
			} catch (NullPointerException e) {
				continue;
			}
			if (repositoryPath == null) {
				continue;
			}
			final String[] repositorySegments = repositoryPath.split("/");
			if (repositorySegments == null || repositorySegments.length == 0) {
				continue;
			}
			
			IPath resourcePath = new Path(path);

			// Check if both paths have common segments, i.e.:
			// svn PLE trunk com.atlassian
			// trunk com.atlassian META-INF MANIFEST.MF
			for (int i=0, s= Math.min(repositorySegments.length, resourcePath.segmentCount()); i<s; ++i) {
				int offset = repositorySegments.length > resourcePath.segmentCount() ? repositorySegments.length - resourcePath.segmentCount() : 0;
				boolean match = true;
				for (int j = 0; j < s - i; ++j) {
					if (offset + i + j >= repositorySegments.length || !repositorySegments[offset + i + j].equals(resourcePath.segment(j))) {
						match = false;
						break;
					}
				}
				if (match) {
					IPath projectPath = resourcePath.removeFirstSegments(s-i);
					IResource resource = project.findMember(projectPath);
					if (resource != null) {
						return resource;
					}
				}
			}
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
