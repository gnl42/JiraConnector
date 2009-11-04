package com.atlassian.connector.eclipse.internal.subclipse.core;

import java.text.ParseException;
import java.util.Date;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.Team;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRemoteFile;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.resources.RemoteFile;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

public final class SubclipseUtil {

	private SubclipseUtil() {
		// ignore
	}
	
	public static IResource getLocalResourceFromFilePath(final String path) {
		if (path == null || path.length() <= 0) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubclipseCorePlugin.PLUGIN_ID, "Requested file path is null or empty."));
			return null;
		}
		
		final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		
		for (IProject project : projects) {
			// skip projects that are closed or not versioned under Subversive
			if (!project.isOpen() || !SVNWorkspaceRoot.isManagedBySubclipse(project)) {
				continue;
			}
			
			String[] repositorySegments = SVNWorkspaceRoot.getSVNFolderFor(project).getUrl().getPathSegments();
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

		StatusHandler.log(new Status(IStatus.WARNING, AtlassianSubclipseCorePlugin.PLUGIN_ID, NLS.bind("Could not find resource for {0}.", path)));
		return null;
	}
	
	public static ISVNRemoteFile getRemoteFile(IResource localResource, String filePath, SVNRevision svnRevision,
			SVNRevision otherSvnRevision, boolean localFileNotFound) throws ParseException, SVNException {

		ISVNLocalResource local = SVNWorkspaceRoot.getSVNResourceFor(localResource);

		if (local.isManaged()) {
			if (localFileNotFound) {
				//file has been moved, so we have to do some funky file retrieval
				ISVNRepositoryLocation location = local.getRepository();

				SVNUrl svnUrl = local.getUrl();

				if (otherSvnRevision instanceof SVNRevision.Number) {
					return new RemoteFile(null, location, svnUrl, svnRevision, (SVNRevision.Number) svnRevision,
							new Date(), "");
				} else {
					return new RemoteFile(null, location, svnUrl, svnRevision, SVNRevision.INVALID_REVISION,
							new Date(), "");
				}
			} else {
				return (ISVNRemoteFile) local.getRemoteResource(svnRevision);
			}
		}

		return null;
	}


	public static ISVNRemoteFile getSvnRemoteFile(String repoUrl, String filePath, String otherRevisionFilePath,
			String revisionString, String otherRevisionString, final IProgressMonitor monitor) {
		if (repoUrl == null) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubclipseCorePlugin.PLUGIN_ID, "Provided repository url is null"));
			return null;
		}
		try {

			if (filePath.startsWith("/")) {
				filePath = filePath.substring(1);
			}

			IResource localResource = SubclipseUtil.getLocalResourceFromFilePath(filePath);

			boolean localFileNotFound = localResource == null;

			if (localFileNotFound) {
				StatusHandler.log(new Status(IStatus.WARNING, AtlassianSubclipseCorePlugin.PLUGIN_ID, NLS.bind("Could not get local resource from file path {0}", filePath)));
				localResource = SubclipseUtil.getLocalResourceFromFilePath(otherRevisionFilePath);
			}

			if (localResource != null) {
				SVNRevision svnRevision = SVNRevision.getRevision(revisionString);
				SVNRevision otherSvnRevision = SVNRevision.getRevision(otherRevisionString);
				ISVNRemoteFile remoteFile = getRemoteFile(localResource, filePath, svnRevision, otherSvnRevision, localFileNotFound);
				if (remoteFile == null) {
					StatusHandler.log(new Status(IStatus.WARNING, AtlassianSubclipseCorePlugin.PLUGIN_ID, NLS.bind("Could not get remote file for local resource {0}", localResource.getName())));
				}
				return remoteFile;
			} else {
				StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubclipseCorePlugin.PLUGIN_ID, NLS.bind("Could not get local resource from file path {0}", otherRevisionFilePath)));
			}
		} catch (SVNException e) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubclipseCorePlugin.PLUGIN_ID, e.getMessage(), e));
		} catch (ParseException e) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubclipseCorePlugin.PLUGIN_ID, e.getMessage(), e));
		}
		return null;
	}

	public static boolean isIgnored(IResource resource) {
		// Ignore WorkspaceRoot, derived and team-private resources and resources from TeamHints 
        if (resource instanceof IWorkspaceRoot || resource.isDerived() || 
        	FileUtility.isSVNInternals(resource) || Team.isIgnoredHint(resource) || isMergeParts(resource)) {
        	return true;
        }
        return false;
    }

	private static boolean isMergeParts(IResource resource) {
		String ext = resource.getFileExtension();
		return ext != null && ext.matches("r(\\d)+"); //$NON-NLS-1$
	}
	
}
