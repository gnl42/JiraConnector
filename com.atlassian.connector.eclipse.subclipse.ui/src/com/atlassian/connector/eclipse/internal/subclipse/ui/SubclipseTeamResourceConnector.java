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

package com.atlassian.connector.eclipse.internal.subclipse.ui;

import com.atlassian.connector.eclipse.internal.subclipse.core.FileUtility;
import com.atlassian.connector.eclipse.internal.subclipse.core.IStateFilter;
import com.atlassian.connector.eclipse.internal.subclipse.core.SubclipseUtil;
import com.atlassian.connector.eclipse.internal.subclipse.ui.compare.CrucibleSubclipseCompareEditorInput;
import com.atlassian.connector.eclipse.ui.AtlassianUiPlugin;
import com.atlassian.connector.eclipse.ui.team.AbstractTeamConnector;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.connector.eclipse.ui.team.CustomChangeSetLogEntry;
import com.atlassian.connector.eclipse.ui.team.ICompareAnnotationModel;
import com.atlassian.connector.eclipse.ui.team.ICustomChangesetLogEntry;
import com.atlassian.connector.eclipse.ui.team.RepositoryInfo;
import com.atlassian.connector.eclipse.ui.team.RevisionInfo;
import com.atlassian.connector.eclipse.ui.team.TeamUiUtils;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.apache.commons.io.IOUtils;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.jetbrains.annotations.NotNull;
import org.tigris.subversion.subclipse.core.ISVNLocalFile;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRemoteFile;
import org.tigris.subversion.subclipse.core.ISVNRemoteFolder;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.ISVNResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.commands.GetLogsCommand;
import org.tigris.subversion.subclipse.core.history.ILogEntry;
import org.tigris.subversion.subclipse.core.history.LogEntryChangePath;
import org.tigris.subversion.subclipse.core.resources.LocalResourceStatus;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;
import org.tigris.subversion.subclipse.ui.compare.ResourceEditionNode;
import org.tigris.subversion.subclipse.ui.editor.RemoteFileEditorInput;
import org.tigris.subversion.svnclientadapter.ISVNProperty;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Connector to handle connecting to a subclipse repository
 * 
 * @author Shawn Minto
 */
public class SubclipseTeamResourceConnector extends AbstractTeamConnector {

	public boolean isEnabled() {
		return true;
	}

	public boolean canHandleFile(String repoUrl, String filePath, IProgressMonitor monitor) {
		return SubclipseUtil.getLocalResourceFromFilePath(filePath) != null;
	}

	public boolean openCompareEditor(String repoUrl, String newFilePath, String oldFilePath, String oldRevisionString,
			String newRevisionString, ICompareAnnotationModel annotationModel, final IProgressMonitor monitor)
			throws CoreException {
		ISVNRemoteResource oldRemoteFile = SubclipseUtil.getSvnRemoteFile(repoUrl, oldFilePath, newFilePath,
				oldRevisionString, newRevisionString, monitor);
		ISVNRemoteResource newRemoteFile = SubclipseUtil.getSvnRemoteFile(repoUrl, newFilePath, oldFilePath,
				newRevisionString, oldRevisionString, monitor);

		if (oldRemoteFile != null && newRemoteFile != null) {
			ResourceEditionNode left = new ResourceEditionNode(newRemoteFile);
			ResourceEditionNode right = new ResourceEditionNode(oldRemoteFile);
			CompareEditorInput compareEditorInput = new CrucibleSubclipseCompareEditorInput(left, right,
					annotationModel);
			TeamUiUtils.openCompareEditorForInput(compareEditorInput);
			return true;
		}
		throw new CoreException(new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID, NLS.bind(
				"Could not get revisions for {0}.", newFilePath)));
	}

	public SortedSet<Long> getRevisionsForFile(IFile file, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(file);
		ISVNLocalResource local = SVNWorkspaceRoot.getSVNResourceFor(file);
		try {
			monitor.beginTask("Getting Revisions for " + file.getName(), IProgressMonitor.UNKNOWN);
			SVNRevision revision = SVNRevision.HEAD;
			ISVNRemoteResource remoteResource = local.getRemoteResource(revision);
			GetLogsCommand getLogsCommand = new GetLogsCommand(remoteResource, revision, new SVNRevision.Number(0),
					SVNRevision.HEAD, false, 0, null, true);
			getLogsCommand.run(monitor);
			ILogEntry[] logEntries = getLogsCommand.getLogEntries();
			SortedSet<Long> revisions = new TreeSet<Long>();
			for (ILogEntry logEntrie : logEntries) {
				revisions.add(new Long(logEntrie.getRevision().getNumber()));
			}
			return revisions;
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID,
					"Error while retrieving Revisions for file " + file.getName() + ".", e));
		}
	}

	public Collection<RepositoryInfo> getRepositories(IProgressMonitor monitor) {
		ISVNRepositoryLocation[] repos = SVNUIPlugin.getPlugin().getRepositoryManager().getKnownRepositoryLocations(
				monitor);
		List<RepositoryInfo> res = MiscUtil.buildArrayList(repos.length);
		for (ISVNRepositoryLocation repo : repos) {
			res.add(new RepositoryInfo(repo.getUrl().toString(), repo.getLabel(), this));
		}
		return res;
	}

	protected RepositoryInfo getRepository(String url, IProgressMonitor monitor) {
		ISVNRepositoryLocation location = getRepositoryLocation(url, monitor);
		if (location != null) {
			return new RepositoryInfo(location.getUrl().toString(), location.getLabel(), this);
		}
		return null;
	}

	private ISVNRepositoryLocation getRepositoryLocation(@NotNull String url, @NotNull IProgressMonitor monitor) {
		ISVNRepositoryLocation[] repositories = SVNUIPlugin.getPlugin()
				.getRepositoryManager()
				.getKnownRepositoryLocations(monitor);
		if (repositories != null) {
			for (ISVNRepositoryLocation repository : repositories) {
				if (repository.getUrl() != null && repository.getUrl().toString().equals(url)) {
					return repository;
				}
			}
		}
		return null;
	}

	@NotNull
	public SortedSet<ICustomChangesetLogEntry> getLatestChangesets(@NotNull String repositoryUrl, int limit,
			IProgressMonitor monitor) throws CoreException {

		SubMonitor submonitor = SubMonitor.convert(monitor, "Retrieving changesets for " + repositoryUrl, 8);

		ISVNRepositoryLocation location = getRepositoryLocation(repositoryUrl, submonitor.newChild(1));
		if (location == null) {
			throw new CoreException(new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID, NLS.bind(
					"Could not get repository location for {0}", repositoryUrl)));
		}

		SortedSet<ICustomChangesetLogEntry> changesets = new TreeSet<ICustomChangesetLogEntry>();
		ISVNRemoteFolder rootFolder = location.getRootFolder();

		if (limit > 0) { //do not retrieve unlimited revisions
			GetLogsCommand getLogsCommand = new GetLogsCommand(rootFolder, SVNRevision.HEAD, SVNRevision.HEAD,
					new SVNRevision.Number(0), false, limit, null, true);
			try {
				getLogsCommand.run(submonitor.newChild(5));

				ILogEntry[] logEntries = getLogsCommand.getLogEntries();

				submonitor.setWorkRemaining(logEntries.length);
				for (ILogEntry logEntry : logEntries) {
					LogEntryChangePath[] logEntryChangePaths = logEntry.getLogEntryChangePaths();
					String[] changed = new String[logEntryChangePaths.length];
					for (int i = 0; i < logEntryChangePaths.length; i++) {
						changed[i] = logEntryChangePaths[i].getPath();

					}
					ICustomChangesetLogEntry customEntry = new CustomChangeSetLogEntry(logEntry.getComment(),
							logEntry.getAuthor(), logEntry.getRevision().toString(), logEntry.getDate(), changed,
							getRepository(repositoryUrl, submonitor.newChild(1)));
					changesets.add(customEntry);
				}
			} catch (SVNException e) {
				throw new CoreException(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID, NLS.bind(
						"Could not retrieve changesets for {0}", repositoryUrl), e));
			}
		} else {
			throw new CoreException(new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID,
					"Getting all changesets is not supported"));
		}
		return changesets;
	}

	public Map<IFile, SortedSet<Long>> getRevisionsForFiles(Collection<IFile> files, IProgressMonitor monitor)
			throws CoreException {
		Assert.isNotNull(files);

		Map<IFile, SortedSet<Long>> map = new HashMap<IFile, SortedSet<Long>>();

		monitor.beginTask("Getting Revisions", files.size());

		for (IFile file : files) {
			IProgressMonitor subMonitor = Policy.subMonitorFor(monitor, 1);
			try {
				map.put(file, getRevisionsForFile(file, subMonitor));
			} finally {
				subMonitor.done();
			}
		}
		return map;
	}

	public IEditorPart openFile(String repoUrl, String filePath, String otherRevisionFilePath, String revisionString,
			String otherRevisionString, final IProgressMonitor monitor) throws CoreException {
		if (repoUrl == null) {
			throw new CoreException(new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID,
					"No repository URL given.."));
		}
		try {

			IResource localResource = SubclipseUtil.getLocalResourceFromFilePath(filePath);

			boolean localFileNotFound = localResource == null;

			if (localFileNotFound) {
				localResource = SubclipseUtil.getLocalResourceFromFilePath(otherRevisionFilePath);
			}

			if (localResource != null) {
				SVNRevision svnRevision = SVNRevision.getRevision(revisionString);
				SVNRevision otherSvnRevision = SVNRevision.getRevision(otherRevisionString);
				ISVNLocalFile localFile = getLocalFile(localResource);

				if (localFile.getStatus().getLastChangedRevision().equals(svnRevision) && !localFile.isDirty()) {
					// the file is not dirty and we have the right local copy
					IEditorPart editorPart = TeamUiUtils.openLocalResource(localResource);
					if (editorPart == null) {
						throw new CoreException(new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID,
								NLS.bind("Could not open editor for {0}.", localFile.getName())));
					}
					return editorPart;

				} else {
					final ISVNRemoteFile remoteFile = SubclipseUtil.getRemoteFile(localResource, filePath, svnRevision,
							otherSvnRevision, localFileNotFound);
					if (remoteFile != null) {
						// we need to open the remote resource since the file is either dirty or the wrong revision

						IEditorPart editorPart = null;
						if (Display.getCurrent() != null) {
							editorPart = openRemoteSvnFile(remoteFile, monitor);
						} else {
							final IEditorPart[] part = new IEditorPart[1];
							PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
								public void run() {
									part[0] = openRemoteSvnFile(remoteFile, monitor);
								}
							});
							editorPart = part[0];
						}
						if (editorPart == null) {
							throw new CoreException(new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID,
									NLS.bind("Could not open editor for {0}.", remoteFile.getName())));
						}
						return editorPart;
					} else {
						throw new CoreException(new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID,
								NLS.bind("Could not get remote file for {0}.", filePath)));
					}
				}
			} else {
				throw new CoreException(new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID, NLS.bind(
						"Could not find local resource for {0}.", filePath)));
			}
		} catch (SVNException e) {
			Status status = new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID, e.getMessage(), e);
			StatusHandler.log(status);
			throw new CoreException(status);
		} catch (ParseException e) {
			Status status = new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID, e.getMessage(), e);
			StatusHandler.log(status);
			throw new CoreException(status);
		}
	}

	public boolean canHandleEditorInput(IEditorInput editorInput) {
		if (editorInput instanceof FileEditorInput) {
			try {
				IFile file = ((FileEditorInput) editorInput).getFile();
				ISVNLocalFile localFile = getLocalFile(file);
				if (localFile != null && !localFile.isDirty()) {
					return true;
				}
			} catch (SVNException e) {
				StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID,
						"Unable to get svn information for local file.", e));
			}
		} else if (editorInput instanceof RemoteFileEditorInput) {
			return true;
		}
		return false;
	}

	public CrucibleFile getCorrespondingCrucibleFileFromEditorInput(IEditorInput editorInput, Review activeReview) {
		SVNUrl fileUrl = null;
		String revision = null;
		if (editorInput instanceof FileEditorInput) {
			// this is a local file that we know how to deal with
			try {
				IFile file = ((FileEditorInput) editorInput).getFile();
				ISVNLocalFile localFile = getLocalFile(file);
				if (localFile != null && !localFile.isDirty()) {
					fileUrl = localFile.getUrl();
					revision = localFile.getStatus().getLastChangedRevision().toString();
				}
			} catch (SVNException e) {
				StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID,
						"Unable to get svn information for local file.", e));
			}
		} else if (editorInput instanceof RemoteFileEditorInput) {
			// this is a remote file that we know how to deal with
			RemoteFileEditorInput input = (RemoteFileEditorInput) editorInput;
			ISVNRemoteFile remoteFile = input.getSVNRemoteFile();
			fileUrl = remoteFile.getUrl();
			revision = remoteFile.getRevision().toString();
		}

		if (fileUrl != null && revision != null) {
			try {
				for (CrucibleFileInfo file : activeReview.getFiles()) {
					VersionedVirtualFile fileDescriptor = file.getFileDescriptor();
					VersionedVirtualFile oldFileDescriptor = file.getOldFileDescriptor();
					SVNUrl newFileUrl = null;
					String newAbsoluteUrl = getAbsoluteUrl(fileDescriptor);
					if (newAbsoluteUrl != null) {
						newFileUrl = new SVNUrl(newAbsoluteUrl);
					}

					SVNUrl oldFileUrl = null;
					String oldAbsoluteUrl = getAbsoluteUrl(oldFileDescriptor);
					if (oldAbsoluteUrl != null) {
						oldFileUrl = new SVNUrl(oldAbsoluteUrl);
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
			} catch (ValueNotYetInitialized e) {
				StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID,
						"Review is not fully initialized.  Unable to get file from review.", e));
			} catch (MalformedURLException e) {
				// ignore
			}
		}
		return null;
	}

	private String getAbsoluteUrl(VersionedVirtualFile fileDescriptor) {
		//TODO might need some performance tweak, but works for now for M2
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (isResourceManagedBy(project)) {
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

					ISVNResource projectResource = SVNWorkspaceRoot.getSVNResourceFor(resource);

					if (projectResource.getUrl().toString().endsWith(fileDescriptor.getUrl())) {
						return projectResource.getUrl().toString();
					}
				} catch (Exception e) {
					StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
		}
		return null;
	}

	private IEditorPart openRemoteSvnFile(ISVNRemoteFile remoteFile, IProgressMonitor monitor) {
		try {
			IWorkbench workbench = AtlassianSubclipseUiPlugin.getDefault().getWorkbench();
			IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();

			RemoteFileEditorInput editorInput = new CustomRemoteFileEditorInput(remoteFile, monitor);
			String editorId = getEditorId(workbench, remoteFile);
			return page.openEditor(editorInput, editorId);
		} catch (PartInitException e) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}
		return null;
	}

	private ISVNLocalFile getLocalFile(IResource localResource) throws SVNException {
		ISVNLocalResource local = SVNWorkspaceRoot.getSVNResourceFor(localResource);

		if (local.isManaged()) {
			return (ISVNLocalFile) local;
		}
		return null;
	}

	public RevisionInfo getLocalRevision(IResource resource) throws CoreException {
		final IProject project = resource.getProject();
		if (project == null) {
			return null;
		}
		if (isResourceManagedBy(project)) {
			final ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
			final ISVNProperty mimeTypeProp = svnResource.getSvnProperty("svn:mime-type");
			boolean isBinary = (mimeTypeProp != null && !mimeTypeProp.getValue().startsWith("text"));
			try {
				return new RevisionInfo(svnResource.getUrl().toString(), svnResource.getStatus()
						.getLastChangedRevision()
						.toString(), isBinary);
			} catch (SVNException e) {
				throw new CoreException(new Status(IStatus.ERROR, AtlassianSubclipseUiPlugin.PLUGIN_ID,
						"Cannot determine SVN information for resource [" + resource + "]", e));
			}
		}
		return null;

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
			id = "org.eclipse.ui.DefaultTextEditor";
		} else {
			id = descriptor.getId();
		}
		return id;
	}

	public RepositoryInfo getApplicableRepository(IResource resource) {
		final IProject project = resource.getProject();
		if (project == null) {
			return null;
		}
		if (isResourceManagedBy(project)) {
			final ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
			final ISVNRepositoryLocation repository = svnResource.getRepository();
			if (repository != null) {
				return new RepositoryInfo(repository.getUrl().toString(), repository.getLabel(), this);
			}
		}
		// ignore
		return null;
	}

	public String getName() {
		return "Subclipse";
	}

	public boolean haveMatchingResourcesRecursive(IResource[] roots, State filter) {
		return FileUtility.checkForResourcesPresenceRecursive(roots, getStateFilter(filter));
	}

	private IStateFilter getStateFilter(State filter) {
		switch (filter) {
		case SF_ANY_CHANGE:
			return IStateFilter.SF_ANY_CHANGE;
		case SF_UNVERSIONED:
			return IStateFilter.SF_UNVERSIONED;
		case SF_IGNORED:
			return IStateFilter.SF_IGNORED;
		case SF_ALL:
			return IStateFilter.SF_ALL;
		default:
			throw new IllegalStateException("Unhandled IStateFilter");
		}
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

	@NotNull
	public Collection<UploadItem> getUploadItemsForResources(@NotNull IResource[] resources,
			@NotNull IProgressMonitor monitor) throws CoreException {
		List<UploadItem> items = MiscUtil.buildArrayList();
		for (IResource resource : resources) {
			if (resource.getType() != IResource.FILE) {
				// ignore anything but files
				continue;
			}

			final ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
			final LocalResourceStatus status = svnResource.getStatus();

			// for unversioned files SVNRevision.getRevision throws an exception
			final String fileName = getResourcePathWithProjectName(resource);

			// Crucible crashes if newContent is empty so ignore empty files (or mark them)
			if (status.isUnversioned() || status.isAdded()) {
				byte[] newContent = getResourceContent((IFile) resource);
				items.add(new UploadItem(fileName, new byte[0], newContent.length == 0 ? EMPTY_ITEM : newContent));
			} else if (status.isDeleted()) {
				items.add(new UploadItem(fileName,
						getResourceContent(svnResource.getBaseResource().getStorage(monitor)), DELETED_ITEM));
			} else if (status.isDirty()) {
				byte[] newContent = getResourceContent((IFile) resource);
				items.add(new UploadItem(fileName,
						getResourceContent(svnResource.getBaseResource().getStorage(monitor)),
						newContent.length == 0 ? EMPTY_ITEM : newContent));
			}
		}
		return items;
	}

	@NotNull
	public IResource[] getMembersForContainer(@NotNull IContainer element) throws CoreException {
		return FileUtility.getAllMembers(element);
	}

	public List<IResource> getResourcesByFilterRecursive(IResource[] roots, State filter) {
		return FileUtility.getResourcesByFilterRecursive(roots, getStateFilter(filter));
	}

	public boolean isResourceManagedBy(IResource resource) {
		if (!isEnabled()) {
			return false;
		}
		return SVNWorkspaceRoot.isManagedBySubclipse(resource.getProject());
	}
}
