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

package com.atlassian.connector.eclipse.ui.team;

import com.atlassian.connector.eclipse.ui.AtlassianUiPlugin;
import com.atlassian.connector.eclipse.ui.exceptions.UnsupportedTeamProviderException;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.history.FileRevisionEditorInput;
import org.eclipse.team.internal.ui.history.FileRevisionTypedElement;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.osgi.framework.Bundle;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * A default team resource provider that just uses the limited Eclipse team API
 * 
 * @author Shawn Minto
 */
public class DefaultTeamResourceConnector implements ITeamResourceConnector {

	private static final String SUBVERSIVE_MINIMUM_VERSION = "0.7.8";

	public boolean canHandleFile(String repoUrl, String filePath, IProgressMonitor monitor) {
		// the default one handles anything
		return true;
	}

	public boolean isEnabled() {
		// the default one is always enabled
		return true;
	}

	public IEditorPart openFile(String repoUrl, String filePath, String otherRevisionFilePath, String revisionString,
			String otherRevisionString, IProgressMonitor monitor) throws CoreException {
		return openFileWithTeamApi(repoUrl, filePath, otherRevisionFilePath, revisionString, monitor);
	}

	public Map<CustomRepository, SortedSet<ICustomChangesetLogEntry>> getLatestChangesets(String repositoryUrl,
			int limit, IProgressMonitor monitor, MultiStatus status) throws CoreException {
		//TODO
		throw new CoreException(new Status(IStatus.WARNING, AtlassianUiPlugin.PLUGIN_ID, "Not implemented yet."));
	}

	public SortedSet<Long> getRevisionsForFile(IFile file, IProgressMonitor monitor) throws CoreException {
		//TODO
		throw new CoreException(new Status(IStatus.WARNING, AtlassianUiPlugin.PLUGIN_ID, "Not implemented yet."));
	}

	public Map<IFile, SortedSet<Long>> getRevisionsForFiles(Collection<IFile> files, IProgressMonitor monitor)
			throws CoreException {
		//TODO
		throw new CoreException(new Status(IStatus.WARNING, AtlassianUiPlugin.PLUGIN_ID, "Not implemented yet."));
	}

	public boolean openCompareEditor(String repoUrl, String filePath, String otherRevisionFilePath,
			String oldRevisionString, String newRevisionString, final ICompareAnnotationModel annotationModel,
			IProgressMonitor monitor) throws CoreException {
		//TODO support for moved/deleted files
		IResource resource = findResourceForPath(filePath);
		if (resource != null) {
			IFileRevision oldFile = getFileRevision(resource, oldRevisionString, monitor);
			if (oldFile != null) {
				IFileRevision newFile = getFileRevision(resource, newRevisionString, monitor);
				if (newFile != null) {
					@SuppressWarnings("restriction")
					final ITypedElement left = new FileRevisionTypedElement(newFile, getLocalEncoding(resource));
					@SuppressWarnings("restriction")
					final ITypedElement right = new FileRevisionTypedElement(oldFile, getLocalEncoding(resource));

					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							CompareEditorInput input = new TeamCompareFileRevisionEditorInput(left, right,
									AtlassianUiPlugin.getDefault()
											.getWorkbench()
											.getActiveWorkbenchWindow()
											.getActivePage(), annotationModel);
							TeamUiUtils.openCompareEditorForInput(input);
						}
					});
					return true;
				} else {
					throw new CoreException(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID, NLS.bind(
							"Could not get new revision file {0}.", newRevisionString)));
				}
			} else {
				throw new CoreException(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID, NLS.bind(
						"Could not get old revision file {0}.", oldRevisionString)));
			}
		} else {
			throw new CoreException(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID, NLS.bind(
					"Could not locate resource {0}.", filePath)));
		}

	}

	private IFileRevision getFileRevision(IResource resource, String revisionString, IProgressMonitor monitor) {
		IProject project = resource.getProject();

		if (project == null) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID,
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
					return fileHistory.getFileRevision(revisionString);
				}
			}
		}
		return null;

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
		if (rp != null && rp.getID().equals(TeamUiUtils.TEAM_PROV_ID_SVN_SUBVERSIVE)) {
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

	@SuppressWarnings("restriction")
	public boolean canHandleEditorInput(IEditorInput editorInput) {
		// we cannot deal with remote files since they are team provider specific
		return editorInput instanceof FileEditorInput || editorInput instanceof FileRevisionEditorInput;
	}

	@SuppressWarnings("restriction")
	public CrucibleFile getCorrespondingCrucibleFileFromEditorInput(IEditorInput editorInput, Review activeReview) {
		Set<CrucibleFileInfo> activeReviewFiles;
		try {
			activeReviewFiles = activeReview.getFiles();
		} catch (ValueNotYetInitialized e) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID,
					"Review is not fully initialized.  Unable to get file from review.", e));
			return null;
		}

		if (editorInput instanceof FileRevisionEditorInput) {

			IFileRevision fileRevision = ((FileRevisionEditorInput) editorInput).getFileRevision();

			String path = fileRevision.getURI().getPath();

			if (fileRevision.getContentIdentifier() != null) {

				for (CrucibleFileInfo fileInfo : activeReviewFiles) {
					VersionedVirtualFile fileDescriptor = fileInfo.getFileDescriptor();
					VersionedVirtualFile oldFileDescriptor = fileInfo.getOldFileDescriptor();

					String oldUrl = oldFileDescriptor.getUrl();
					String newUrl = fileDescriptor.getUrl();

					if ((newUrl != null && newUrl.length() > 0 && path.endsWith(newUrl))
							|| (oldUrl != null && oldUrl.length() > 0 && path.endsWith(oldUrl))) {

						String revision = fileRevision.getContentIdentifier();

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

		} else if (editorInput instanceof FileEditorInput) {
			// this will only work on local files since they remote files are team provider specific

			IFile file = ((FileEditorInput) editorInput).getFile();

			IProject project = file.getProject();

			if (project == null) {
				StatusHandler.log(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID,
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

					for (CrucibleFileInfo fileInfo : activeReviewFiles) {
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
		}
//		else if (editorInput instanceof FileRevisionEditorInput){
//			FileRevisionEditorInput fileRevisionEditorInput = (FileRevisionEditorInput)editorInput;
//			IFileRevision fileRevision = fileRevisionEditorInput.getFileRevision()
//		}
		return null;
	}

	private static IEditorPart openFileWithTeamApi(String repoUrl, String filePath, String otherRevisionFilePath,
			final String revisionString, final IProgressMonitor monitor) throws CoreException {
		// this is a good backup (Works for cvs and anyone that uses the history provider

		// TODO add support for finding a project in the path so that we can find the proper resource 
		// (i.e. file path and project name may be different)

		//TODO support for moved/deleted files
		IResource resource = findResourceForPath(filePath);

		if (resource == null) {
			resource = findResourceForPath(otherRevisionFilePath);
		}

		if (resource != null) {
			if (!(resource instanceof IFile)) {
				throw new CoreException(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID, NLS.bind(
						"Unable to get resource {0}.", filePath)));
			}

			IProject project = resource.getProject();

			if (project == null) {
				throw new CoreException(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID, NLS.bind(
						"Unable to get project for resource {0}.", filePath)));
			}

			RepositoryProvider rp = RepositoryProvider.getProvider(project);
			checkIfSupportedTeamProvider(rp);
			if (rp != null && rp.getFileHistoryProvider() != null) {

				// this project has a team nature associated with it in the workspace
				final IFileHistoryProvider historyProvider = rp.getFileHistoryProvider();

				IFileRevision localFileRevision = historyProvider.getWorkspaceFileRevision(resource);

				boolean inSync = isRemoteFileInSync(resource, rp);

				IEditorPart editor = null;

				if (inSync && localFileRevision.getContentIdentifier() != null
						&& localFileRevision.getContentIdentifier().equals(revisionString)) {
					editor = TeamUiUtils.openLocalResource(resource);
				} else {
					if (Display.getCurrent() != null) {
						editor = openRemoteResource(revisionString, resource, historyProvider, monitor);
					} else {
						final IEditorPart[] part = new IEditorPart[1];
						final IResource res = resource;
						PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
							public void run() {
								part[0] = openRemoteResource(revisionString, res, historyProvider, monitor);
							}
						});
						editor = part[0];
					}
				}
				if (editor == null) {
					throw new CoreException(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID, NLS.bind(
							"Unable to open editor for resource {0}.", filePath)));
				} else {
					return editor;
				}
			}
			throw new CoreException(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID, NLS.bind(
					"Unable to get repository provider for project {0}.", project.getName())));
		}
		throw new CoreException(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID, NLS.bind(
				"Unable to get resource {0}.", filePath)));
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

	private static IEditorPart openRemoteResource(String revisionString, IResource resource,
			IFileHistoryProvider historyProvider, IProgressMonitor monitor) {
		// we need a different revision than the one in the local workspace
		IFileHistory fileHistory = historyProvider.getFileHistoryFor(resource, IFileHistoryProvider.NONE, monitor);

		if (fileHistory != null) {
			IFileRevision remoteFileRevision = fileHistory.getFileRevision(revisionString);
			if (remoteFileRevision != null) {
				try {
					return Utils.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
							remoteFileRevision, monitor);
				} catch (CoreException e) {
					StatusHandler.log(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
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
					StatusHandler.log(new Status(IStatus.WARNING, AtlassianUiPlugin.PLUGIN_ID,
							"Unable to determine if file is in sync.  Trying to open remote file."));
				}
			} catch (TeamException e) {
				StatusHandler.log(new Status(IStatus.WARNING, AtlassianUiPlugin.PLUGIN_ID,
						"Unable to determine if file is in sync.  Trying to open remote file.", e));
			}
		} else {
			StatusHandler.log(new Status(IStatus.WARNING, AtlassianUiPlugin.PLUGIN_ID,
					"Unable to determine if file is in sync.  Trying to open remote file."));
		}
		return inSync;
	}

	public RevisionInfo getLocalRevision(IResource resource) throws CoreException {
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

				return new RevisionInfo(uriStr, localFileRevision.getContentIdentifier(), null);
			}
			return new RevisionInfo(localFileRevision.getContentIdentifier(), null, null);
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

	public Collection<RepositoryInfo> getRepositories(IProgressMonitor monitor) {
		// @todo wseliga implement it
		return Collections.emptyList();
	}

	public RepositoryInfo getApplicableRepository(IResource resource) {
		// @todo wseliga
		return null;
	}

	public String getName() {
		return "Team API (partial support)";
	}
}
