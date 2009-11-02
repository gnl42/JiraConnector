package com.atlassian.connector.eclipse.internal.subclipse.core;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
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
import org.tigris.subversion.subclipse.core.ISVNResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.resources.RemoteFile;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

import com.atlassian.theplugin.commons.util.MiscUtil;

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
		if (projects != null || projects.length == 0) {
			StatusHandler.log(new Status(IStatus.WARNING, AtlassianSubclipseCorePlugin.PLUGIN_ID, "Could not find projects in the workspace."));
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
						if (resource.getType() == IResource.PROJECT && !SVNWorkspaceRoot.isManagedBySubclipse(resource.getProject())) {
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
