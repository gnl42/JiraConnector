package com.atlassian.connector.eclipse.internal.perforce.core;

public final class PerforceUtil {
	
	private PerforceUtil() {
		// ignore
	}

	/*
	public static IRepositoryLocation getRepositoryLocation(String url) {
		IRepositoryLocation[] repositories = SVNRemoteStorage.instance().getRepositoryLocations();
		IRepositoryLocation bestMatch = null;
		
		if (repositories != null) {
			for (IRepositoryLocation repository : repositories) {
				if (url.startsWith(repository.getUrl())) {
					if (bestMatch == null || bestMatch.getUrl().length() < repository.getUrl().length()) {
						bestMatch = repository;
					}
				}
			}
		}
		return bestMatch;
	}*/
	
	/**
	 * 
	 * @param repoUrl We need it here because we want to check if file described by path that we found has exactly the same SVN address
	 * @param path
	 * @param monitor
	 * @return
	 */
	/*
	public static IResource getLocalResourceFromFilePath(final String repoUrl, final String path, IProgressMonitor monitor) {
		SubMonitor submonitor = SubMonitor.convert(monitor);
		
		if (path == null || path.length() <= 0) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianPerforceCorePlugin.PLUGIN_ID, "Requested file path is null or empty."));
			return null;
		}
		
		final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		
		for (IProject project : projects) {
			// skip projects that are closed or not versioned under Subversive 
			if (!project.isOpen() || RepositoryProvider.getProvider(project, SVNTeamPlugin.NATURE_ID) == null) {
				continue; 
			}
			final IP4Resource repositoryResource = P4Workspace.getWorkspace().getResource(project);
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

		StatusHandler.log(new Status(IStatus.WARNING, AtlassianPerforceCorePlugin.PLUGIN_ID, NLS.bind("Could not find resource for {0}.", path)));
		return null;
	}*/

	/*
	@Nullable
	public static IP4File getSvnRemoteFile(@NotNull String repoUrl, @NotNull String filePath, @NotNull SVNRevision fileRevision,
			@Nullable final IProgressMonitor monitor) {
		
		IRepositoryLocation repository = getRepositoryLocation(repoUrl);
		if (repository != null) {
			return new SVNRepositoryFile(repository, repoUrl + '/' + filePath, fileRevision);
		}
		return null;
	}*/

}
