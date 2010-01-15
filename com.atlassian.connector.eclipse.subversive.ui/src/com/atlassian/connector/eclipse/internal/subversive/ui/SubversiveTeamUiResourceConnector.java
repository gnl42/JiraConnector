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

package com.atlassian.connector.eclipse.internal.subversive.ui;

import com.atlassian.connector.eclipse.internal.subversive.core.SubversiveUtil;
import com.atlassian.connector.eclipse.team.ui.AbstractTeamUiConnector;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.connector.eclipse.team.ui.CustomChangeSetLogEntry;
import com.atlassian.connector.eclipse.team.ui.ICustomChangesetLogEntry;
import com.atlassian.connector.eclipse.team.ui.LocalStatus;
import com.atlassian.connector.eclipse.team.ui.ScmRepository;
import com.atlassian.connector.eclipse.team.ui.TeamConnectorType;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.SVNChangeStatus;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNLogPath;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevision.Kind;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.GetLocalFileContentOperation;
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.repository.RepositoryFileEditorInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Connector to handle connecting to a CVS repository
 * 
 * @author Pawel Niewiadomski
 * @author Wojciech Seliga
 */
public class SubversiveTeamUiResourceConnector extends AbstractTeamUiConnector {

	public boolean isEnabled() {
		return true;
	}

	@NotNull
	public SortedSet<ICustomChangesetLogEntry> getLatestChangesets(@NotNull String repositoryUrl, int limit,
			IProgressMonitor monitor) throws CoreException {

		IRepositoryLocation location = SubversiveUtil.getRepositoryLocation(repositoryUrl);
		if (location == null) {
			throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID, NLS.bind(
					"Could not get repository location for {0}", repositoryUrl)));
		}

		SortedSet<ICustomChangesetLogEntry> changesets = new TreeSet<ICustomChangesetLogEntry>();
		IRepositoryRoot rootFolder = location.getRoot();

		if (limit > 0) { // do not retrieve unlimited revisions
			monitor.beginTask("Retrieving changesets for " + location.getLabel(), 101);

			GetLogMessagesOperation getLogMessagesOp = new GetLogMessagesOperation(rootFolder, false);
			getLogMessagesOp.setLimit(limit);
			getLogMessagesOp.setEndRevision(SVNRevision.fromNumber(0));
			getLogMessagesOp.setStartRevision(SVNRevision.HEAD);
			getLogMessagesOp.setIncludeMerged(SVNTeamPreferences.getMergeBoolean(SVNTeamUIPlugin.instance()
					.getPreferenceStore(), SVNTeamPreferences.MERGE_INCLUDE_MERGED_NAME));
			getLogMessagesOp.run(monitor);
			if (getLogMessagesOp.getExecutionState() == IActionOperation.OK) {
				SVNLogEntry[] logEntries = getLogMessagesOp.getMessages();
				if (logEntries != null) {
					for (SVNLogEntry logEntry : logEntries) {
						SVNLogPath[] logEntryChangePaths = logEntry.changedPaths;
						if (logEntryChangePaths == null) {
							continue;
						}
						String[] changed = new String[logEntryChangePaths.length];
						for (int i = 0; i < logEntryChangePaths.length; i++) {
							changed[i] = logEntryChangePaths[i].path;
						}
						ICustomChangesetLogEntry customEntry = new CustomChangeSetLogEntry(logEntry.message,
								logEntry.author, Long.toString(logEntry.revision), new Date(logEntry.date), changed,
								getRepository(repositoryUrl, new NullProgressMonitor()));
						changesets.add(customEntry);
					}
				}
			} else {
				if (getLogMessagesOp.getStatus().toString().contains(
						"Selected SVN connector library is not available or cannot be loaded.")) {
					throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID,
							"Subversive doesn't have a default client installed", getLogMessagesOp.getStatus()
									.getException()));
				} else {
					throw new CoreException(getLogMessagesOp.getStatus());
				}
			}
		} else {
			throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID,
					"Getting all changesets is not supported"));
		}

		return changesets;
	}

	public Collection<ScmRepository> getRepositories(IProgressMonitor monitor) {
		IRepositoryLocation[] repositories = SVNRemoteStorage.instance().getRepositoryLocations();
		if (repositories == null) {
			return MiscUtil.buildArrayList();
		}

		List<ScmRepository> res = MiscUtil.buildArrayList(repositories.length);
		for (IRepositoryLocation repo : repositories) {
			res.add(new ScmRepository(repo.getUrl(), repo.getRepositoryRootUrl(), repo.getLabel(), this));
		}
		return res;
	}

	protected ScmRepository getRepository(String url, IProgressMonitor monitor) {
		IRepositoryLocation location = SubversiveUtil.getRepositoryLocation(url);
		if (location != null) {
			return new ScmRepository(location.getUrl(), location.getRepositoryRootUrl(), location.getLabel(), this);
		}
		return null;
	}

	public LocalStatus getLocalRevision(IResource resource) throws CoreException {
		final IProject project = resource.getProject();
		if (project == null) {
			return null;
		}
		if (isResourceManagedBy(resource)) {
			// we use both local and repository resource as I don't know how to fetch SVN URL from local resource
			// with current Subversive implementation svnResource.getUrl() is immediate - i.e. it does not do anything
			// remote
			try {
				final IRepositoryResource svnResource = SVNRemoteStorage.instance().asRepositoryResource(resource);
				final ILocalResource localResource = SVNRemoteStorage.instance().asLocalResource(resource);
				if (svnResource == null || localResource == null) {
					return null;
				}

				if (IStateFilter.SF_UNVERSIONED.accept(localResource)) {
					return LocalStatus.makeUnversioned();
				}

				if (IStateFilter.SF_IGNORED.accept(localResource)) {
					return LocalStatus.makeIngored();
				}

				final String mimeTypeProp = SVNUtility.getPropertyForNotConnected(resource,
						SVNProperty.BuiltIn.MIME_TYPE);
				boolean isBinary = (mimeTypeProp != null && !mimeTypeProp.startsWith("text"));

				if (IStateFilter.SF_ADDED.accept(localResource)) {
					return LocalStatus.makeAdded(svnResource.getUrl(), isBinary);
				}

				final SVNChangeStatus status = SVNUtility.getSVNInfoForNotConnected(resource);

				return LocalStatus.makeVersioned(svnResource.getUrl(), String.valueOf(status.revision),
						String.valueOf(status.lastChangedRevision),
						localResource.getChangeMask() != ILocalResource.NO_MODIFICATION, isBinary);
			} catch (RuntimeException e) {
				throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID,
						"Cannot determine local revision for [" + resource.getName() + "]", e));
			}
		}
		return null;
	}

	public ScmRepository getApplicableRepository(IResource resource) {
		final IProject project = resource.getProject();
		if (project == null) {
			return null;
		}

		RepositoryProvider provider = RepositoryProvider.getProvider(project, SVNTeamPlugin.NATURE_ID);
		if (provider == null) {
			return null;
		}

		final IRepositoryLocation repositoryLocation = SVNRemoteStorage.instance().getRepositoryLocation(resource);
		final String rootUrl = repositoryLocation.getRepositoryRootUrl();
		final String label = repositoryLocation.getLabel();
		return new ScmRepository(rootUrl, rootUrl, label, this);
	}

	public String getName() {
		return "Subversive";
	}

	public boolean haveMatchingResourcesRecursive(IResource[] roots, State filter) {
		return FileUtility.checkForResourcesPresenceRecursive(roots, getStateFilter(filter));
	}

	public Collection<UploadItem> getUploadItemsForResources(IResource[] resources, IProgressMonitor monitor)
			throws CoreException {
		List<UploadItem> items = MiscUtil.buildArrayList();
		for (IResource resource : resources) {
			if (resource.getType() != IResource.FILE) {
				// ignore anything but files
				continue;
			}

			final ILocalResource localResource = SVNRemoteStorage.instance().asLocalResource(resource);
			final String fileName = getResourcePathWithProjectName(resource);

			// Crucible crashes if newContent is empty so ignore empty files (or mark them)
			if (IStateFilter.SF_UNVERSIONED.accept(localResource) || IStateFilter.SF_ADDED.accept(localResource)) {
				byte[] newContent = getResourceContent(((IFile) resource).getContents());
				items.add(new UploadItem(fileName, new byte[0], newContent.length == 0 ? EMPTY_ITEM : newContent));
			} else if (IStateFilter.SF_DELETED.accept(localResource)) {
				GetLocalFileContentOperation getContent = new GetLocalFileContentOperation(resource, Kind.BASE);
				getContent.run(monitor);
				items.add(new UploadItem(fileName, getResourceContent(getContent.getContent()), DELETED_ITEM));
			} else if (IStateFilter.SF_MODIFIED.accept(localResource)) {
				GetLocalFileContentOperation getContent = new GetLocalFileContentOperation(resource, Kind.BASE);
				getContent.run(monitor);
				byte[] newContent = getResourceContent(((IFile) resource).getContents());
				items.add(new UploadItem(fileName, getResourceContent(getContent.getContent()),
						newContent.length == 0 ? EMPTY_ITEM : newContent));
			}
		}
		return items;
	}

	public List<IResource> getResourcesByFilterRecursive(IResource[] roots, State filter) {
		IResource[] result = FileUtility.getResourcesRecursive(roots, getStateFilter(filter));
		return result == null ? new ArrayList<IResource>() : MiscUtil.buildArrayList(result);
	}

	public boolean isResourceAcceptedByFilter(IResource resource, State state) {
		ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
		return getStateFilter(state).accept(local);
	}

	public boolean isResourceManagedBy(IResource resource) {
		if (!isEnabled()) {
			return false;
		}
		// check if project is associated with Subversive Team provider, 
		// if we don't test it asRepositoryResource will throw RuntimeException
		RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject(), SVNTeamPlugin.NATURE_ID);
		if (provider != null) {
			return true;
		}
		return false;
	}

	public TeamConnectorType getType() {
		return TeamConnectorType.SVN;
	}

	public boolean canHandleEditorInput(IEditorInput editorInput) {
		if (editorInput instanceof FileEditorInput) {
			final IFile file = ((FileEditorInput) editorInput).getFile();
			return canHandleFile(file);
		} else if (editorInput instanceof RepositoryFileEditorInput) {
			return true;
		}
		return false;
	}

	public boolean canHandleFile(IFile file) {
		final IProject project = file.getProject();
		if (project == null) {
			return false;
		}

		// check if project is associated with Subversive Team provider, 
		// if we don't test it asRepositoryResource will throw RuntimeException
		RepositoryProvider provider = RepositoryProvider.getProvider(project, SVNTeamPlugin.NATURE_ID);
		if (provider == null) {
			return false;
		}

		ILocalResource localFile = SVNRemoteStorage.instance().asLocalResource(file);
		if (localFile != null && localFile.getChangeMask() == ILocalResource.NO_MODIFICATION) {
			return true;
		}

		return false;
	}

	public CrucibleFile getCrucibleFileFromReview(Review activeReview, IFile file) {
		ILocalResource localFile = SVNRemoteStorage.instance().asLocalResource(file);
		if (localFile != null && localFile.getChangeMask() == ILocalResource.NO_MODIFICATION) {
			String fileUrl = SVNRemoteStorage.instance().asRepositoryResource(file).getUrl();
			String revision = Long.toString(localFile.getRevision());
			if (fileUrl != null && revision != null) {
				return getCrucibleFileFromReview(activeReview, fileUrl, revision);
			}
		}
		return null;
	}

	public CrucibleFile getCrucibleFileFromReview(Review activeReview, String fileUrl, String revision) {
		try {
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
		} catch (ValueNotYetInitialized e) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID,
					"Review is not fully initialized.  Unable to get file from review.", e));
		}
		return null;
	}

	public CrucibleFile getCrucibleFileFromReview(Review activeReview, IEditorInput editorInput) {
		if (editorInput instanceof FileEditorInput) {
			// this is a local file that we know how to deal with
			return getCrucibleFileFromReview(activeReview, ((FileEditorInput) editorInput).getFile());
		} else if (editorInput instanceof RepositoryFileEditorInput) {
			// this is a remote file that we know how to deal with
			RepositoryFileEditorInput input = (RepositoryFileEditorInput) editorInput;
			IRepositoryResource remoteFile = input.getRepositoryResource();
			String revision = null;
			String fileUrl = remoteFile.getUrl();
			try {
				revision = Long.toString(remoteFile.getRevision());
			} catch (SVNConnectorException e) {
				StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID,
						"Unable to get svn information for local file.", e));
			}
			if (revision != null && fileUrl != null) {
				return getCrucibleFileFromReview(activeReview, fileUrl, revision);
			}
		}

		return null;
	}

	private String getAbsoluteUrl(VersionedVirtualFile fileDescriptor) {
		//TODO might need some performance tweak, but works for now for M2
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {

			// check if project is associated with Subversive Team provider, 
			// if we don't test it asRepositoryResource will throw RuntimeException
			RepositoryProvider provider = RepositoryProvider.getProvider(project, SVNTeamPlugin.NATURE_ID);
			if (provider == null) {
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

				IRepositoryResource projectResource = SVNRemoteStorage.instance().asRepositoryResource(resource);
				if (projectResource.getUrl().toString().endsWith(fileDescriptor.getUrl())) {
					return projectResource.getUrl().toString();
				}
			} catch (Exception e) {
				StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID, e.getMessage(), e));
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
		try {
			return SVNRemoteStorage.instance().getRegisteredChildren(element);
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID,
					"Can't get container members", e));
		}
	}

	private byte[] getResourceContent(InputStream is) {
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
