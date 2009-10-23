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
import com.atlassian.connector.eclipse.internal.subversive.ui.compare.CrucibleSubversiveCompareEditorInput;
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
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.connector.SVNDiffStatus;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNLogPath;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.connector.SVNRevision.Kind;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.GetLocalFileContentOperation;
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.repository.RepositoryFileEditorInput;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Connector to handle connecting to a CVS repository
 * 
 * @author Pawel Niewiadomski
 * @author Wojciech Seliga
 */
public class SubversiveTeamResourceConnector extends AbstractTeamConnector {

	private final class ProgressMonitorWrapper implements ISVNProgressMonitor {
		private final IProgressMonitor subMonitor;

		private ProgressMonitorWrapper(IProgressMonitor subMonitor) {
			this.subMonitor = subMonitor;
		}

		public boolean isActivityCancelled() {
			return false;
		}

		public void progress(int current, int total, ItemState state) {
			ProgressMonitorUtility.progress(subMonitor, current, total);
		}

		public void reportError(String errorMessage) {
		}
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean canHandleFile(String repoUrl, String filePath, IProgressMonitor monitor) {
		return SubversiveUtil.getLocalResourceFromFilePath(filePath) != null;
	}

	public boolean openCompareEditor(String repoUrl, String newFilePath, String oldFilePath, String oldRevisionString,
			String newRevisionString, ICompareAnnotationModel annotationModel, final IProgressMonitor monitor)
			throws CoreException {

		IRepositoryResource oldRemoteFile = SubversiveUtil.getSvnRemoteFile(repoUrl, oldFilePath,
				SVNRevision.fromString(oldRevisionString), monitor);
		IRepositoryResource newRemoteFile = SubversiveUtil.getSvnRemoteFile(repoUrl, newFilePath,
				SVNRevision.fromString(newRevisionString), monitor);

		if (oldRemoteFile != null && newRemoteFile != null) {
			oldRemoteFile.setPegRevision(oldRemoteFile.getSelectedRevision());
			newRemoteFile.setPegRevision(newRemoteFile.getSelectedRevision());

			final IRepositoryLocation location = oldRemoteFile.getRepositoryLocation();
			final ISVNConnector proxy = location.acquireSVNProxy();
			final ArrayList<SVNDiffStatus> statuses = new ArrayList<SVNDiffStatus>();
			final IProgressMonitor subMonitor = org.eclipse.mylyn.commons.net.Policy.subMonitorFor(monitor, 1);

			try {
				subMonitor.beginTask("Retrieving SVN statuses", 101);

				SVNEntryRevisionReference refOldRemoteFile = SVNUtility.getEntryRevisionReference(oldRemoteFile);
				SVNEntryRevisionReference refNewRemoteFile = SVNUtility.getEntryRevisionReference(newRemoteFile);
				if (SVNUtility.useSingleReferenceSignature(refOldRemoteFile, refNewRemoteFile)) {
					SVNUtility.diffStatus(proxy, statuses, refOldRemoteFile, refNewRemoteFile.revision,
							refNewRemoteFile.revision, Depth.INFINITY, ISVNConnector.Options.NONE,
							new ProgressMonitorWrapper(subMonitor));
				} else {
					SVNUtility.diffStatus(proxy, statuses, refOldRemoteFile, refNewRemoteFile, Depth.INFINITY,
							ISVNConnector.Options.NONE, new ProgressMonitorWrapper(subMonitor));
				}
			} catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID, NLS.bind(
						"Could not get revisions for {0}.", newFilePath)));
			} finally {
				location.releaseSVNProxy(proxy);
				subMonitor.done();
			}

			CompareConfiguration cc = new CompareConfiguration();
			CrucibleSubversiveCompareEditorInput input = new CrucibleSubversiveCompareEditorInput(cc, newRemoteFile,
					oldRemoteFile, statuses, annotationModel);
			try {
				input.initialize(monitor);
			} catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID, NLS.bind(
						"Could not get revisions for {0}.", newFilePath)));
			}

			TeamUiUtils.openCompareEditorForInput(input);
			return true;
		}
		throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID, NLS.bind(
				"Could not get revisions for {0}.", newFilePath)));

	}

	public Collection<RepositoryInfo> getRepositories(IProgressMonitor monitor) {
		IRepositoryLocation[] repositories = SVNRemoteStorage.instance().getRepositoryLocations();
		if (repositories == null) {
			return MiscUtil.buildArrayList();
		}

		List<RepositoryInfo> res = MiscUtil.buildArrayList(repositories.length);
		for (IRepositoryLocation repo : repositories) {
			res.add(new RepositoryInfo(repo.getUrl(), repo.getLabel(), this));
		}
		return res;
	}

	protected RepositoryInfo getRepository(String url, IProgressMonitor monitor) {
		IRepositoryLocation location = SubversiveUtil.getRepositoryLocation(url);
		if (location != null) {
			return new RepositoryInfo(location.getUrl(), location.getLabel(), this);
		}
		return null;
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

		if (limit > 0) { //do not retrieve unlimited revisions
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
				throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID, NLS.bind(
						"Could not retrieve changesetes for {0}.", location.getLabel())));
			}
		} else {
			throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID,
					"Getting all changesets is not supported"));
		}

		return changesets;
	}

	public SortedSet<Long> getRevisionsForFile(IFile file, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(file);
		try {
			monitor.beginTask("Getting Revisions for " + file.getName(), IProgressMonitor.UNKNOWN);
			final IRepositoryResource remoteResource = SVNRemoteStorage.instance().asRepositoryResource(file);

			GetLogMessagesOperation getLogMessagesOp = new GetLogMessagesOperation(remoteResource, false);
			getLogMessagesOp.setEndRevision(SVNRevision.fromNumber(0));
			getLogMessagesOp.setStartRevision(SVNRevision.HEAD);
			getLogMessagesOp.setIncludeMerged(true);

			getLogMessagesOp.run(monitor);
			SVNLogEntry[] logEntries = getLogMessagesOp.getMessages();
			SortedSet<Long> revisions = new TreeSet<Long>();
			if (logEntries != null) {
				for (SVNLogEntry logEntry : logEntries) {
					revisions.add(new Long(logEntry.revision));
				}
			}
			return revisions;
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID,
					"Error while retrieving Revisions for file " + file.getName() + ".", e));
		}
	}

	public Map<IFile, SortedSet<Long>> getRevisionsForFiles(Collection<IFile> files, IProgressMonitor monitor)
			throws CoreException {
		Assert.isNotNull(files);

		Map<IFile, SortedSet<Long>> map = new HashMap<IFile, SortedSet<Long>>();

		monitor.beginTask("Getting Revisions", files.size());

		for (IFile file : files) {
			final IProgressMonitor subMonitor = org.eclipse.mylyn.commons.net.Policy.subMonitorFor(monitor, 1);
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
			throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID,
					"No repository URL given."));
		}

		try {
			IEditorPart editor = openFile(repoUrl, filePath, revisionString, monitor);
			if (editor != null) {
				return editor;
			}
		} catch (CoreException e) {
			StatusHandler.log(new Status(IStatus.WARNING, AtlassianSubversiveUiPlugin.PLUGIN_ID, NLS.bind(
					"Failed to open {0} at revision {1}", filePath, revisionString)));
		}

		if (otherRevisionFilePath != null && otherRevisionString != null && !"".equals(otherRevisionString)) {
			IEditorPart editor = openFile(repoUrl, otherRevisionFilePath, otherRevisionString, monitor);
			if (editor != null) {
				return editor;
			}
		}

		throw new CoreException(new Status(IStatus.WARNING, AtlassianSubversiveUiPlugin.PLUGIN_ID, NLS.bind(
				"Failed to open {0} at revision {1}", filePath, revisionString)));
	}

	public IEditorPart openFile(final String repoUrl, final String filePath, final String fileRevision,
			final IProgressMonitor monitor) throws CoreException {

		Assert.isNotNull(repoUrl);

		IResource localResource = SubversiveUtil.getLocalResourceFromFilePath(filePath);
		if (localResource == null) {
			throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID, NLS.bind(
					"Could not find local resource for {0}.", filePath)));
		}

		SVNRevision svnRevision;
		try {
			svnRevision = SVNRevision.fromString(fileRevision);
		} catch (IllegalArgumentException e) {
			throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID, NLS.bind(
					"Invalid revision {0} for {1}", fileRevision, filePath)));
		}

		// check local file first
		final ILocalResource localFile = SVNRemoteStorage.instance().asLocalResource(localResource);
		if (SVNRevision.fromNumber(localFile.getBaseRevision()).equals(svnRevision)
				&& localFile.getChangeMask() == ILocalResource.NO_MODIFICATION) {
			// the file is not dirty and we have the right local copy
			IEditorPart editor = TeamUiUtils.openLocalResource(localResource);
			if (editor != null) {
				return editor;
			}

			throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID, NLS.bind(
					"Could not open editor for {0}.", localFile.getName())));
		}

		// fallback to remote
		final IRepositoryFile remoteFile = SubversiveUtil.getSvnRemoteFile(repoUrl, filePath, svnRevision, monitor);
		if (remoteFile == null) {
			throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID, NLS.bind(
					"Could not get remote file for {0}.", filePath)));
		}

		// we need to open the remote resource since the file is either dirty or the wrong revision
		if (Display.getCurrent() != null) {
			IEditorPart editor = openRemoteSvnFile(remoteFile, monitor);
			if (editor != null) {
				return editor;
			}
		} else {
			final IEditorPart[] part = new IEditorPart[1];
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
					part[0] = openRemoteSvnFile(remoteFile, monitor);
				}
			});
			if (part[0] != null) {
				return part[0];
			}
		}

		throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID, NLS.bind(
				"Could not open editor for {0}.", remoteFile.getName())));
	}

	public boolean canHandleEditorInput(IEditorInput editorInput) {
		if (editorInput instanceof FileEditorInput) {
			final IFile file = ((FileEditorInput) editorInput).getFile();
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
		} else if (editorInput instanceof RepositoryFileEditorInput) {
			return true;
		}
		return false;
	}

	public CrucibleFile getCorrespondingCrucibleFileFromEditorInput(IEditorInput editorInput, Review activeReview) {
		String fileUrl = null;
		String revision = null;
		if (editorInput instanceof FileEditorInput) {
			// this is a local file that we know how to deal with
			IFile file = ((FileEditorInput) editorInput).getFile();
			ILocalResource localFile = SVNRemoteStorage.instance().asLocalResource(file);
			if (localFile != null && localFile.getChangeMask() == ILocalResource.NO_MODIFICATION) {
				fileUrl = SVNRemoteStorage.instance().asRepositoryResource(file).getUrl();
				revision = Long.toString(localFile.getBaseRevision());
			}
		} else if (editorInput instanceof RepositoryFileEditorInput) {
			// this is a remote file that we know how to deal with
			RepositoryFileEditorInput input = (RepositoryFileEditorInput) editorInput;
			IRepositoryResource remoteFile = input.getRepositoryResource();
			fileUrl = remoteFile.getUrl();
			try {
				revision = Long.toString(remoteFile.getRevision());
			} catch (SVNConnectorException e) {
				StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID,
						"Unable to get svn information for local file.", e));
			}
		}

		if (fileUrl != null && revision != null) {
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
		}
		return null;
	}

	public RevisionInfo getLocalRevision(IResource resource) throws CoreException {
		final IProject project = resource.getProject();
		if (project == null) {
			return null;
		}

		// check if project is associated with Subversive Team provider, 
		// if we don't test it asRepositoryResource will throw RuntimeException
		RepositoryProvider provider = RepositoryProvider.getProvider(project, SVNTeamPlugin.NATURE_ID);
		if (provider == null) {
			return null;
		}

		// we use both local and repository resource as I don't know how to fetch SVN URL from local resource
		// with current Subversive implementation svnResource.getUrl() is immediate - i.e. it does not do anything
		// remote
		try {
			final IRepositoryResource svnResource = SVNRemoteStorage.instance().asRepositoryResource(resource);
			final ILocalResource localResource = SVNRemoteStorage.instance().asLocalResource(resource);
			if (svnResource == null || localResource == null) {
				return null;
			}

			final String mimeTypeProp = SVNUtility.getPropertyForNotConnected(resource, SVNProperty.BuiltIn.MIME_TYPE);
			boolean isBinary = (mimeTypeProp != null && !mimeTypeProp.startsWith("text"));
			return new RevisionInfo(svnResource.getUrl(), Long.toString(localResource.getRevision()), isBinary);
		} catch (RuntimeException e) {
			throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID,
					"Cannot determine local revision for [" + resource.getName() + "]", e));
		}
	}

	public RepositoryInfo getApplicableRepository(IResource resource) {
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
		return new RepositoryInfo(rootUrl, label, this);
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

	private IEditorPart openRemoteSvnFile(IRepositoryFile remoteFile, IProgressMonitor monitor) {
		try {
			IWorkbench workbench = AtlassianSubversiveUiPlugin.getDefault().getWorkbench();
			IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();

			RepositoryFileEditorInput editorInput = new RepositoryFileEditorInput(remoteFile);
			String editorId = getEditorId(workbench, remoteFile);
			return page.openEditor(editorInput, editorId);
		} catch (PartInitException e) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}
		return null;
	}

	private String getEditorId(IWorkbench workbench, IRepositoryFile file) {
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

	public String getName() {
		return "Subversive";
	}

	public boolean haveMatchingResourcesRecursive(IResource[] roots, State filter) {
		return FileUtility.checkForResourcesPresenceRecursive(roots, getStateFilter(filter));
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
		default:
			throw new IllegalStateException("Unhandled IStateFilter");
		}
	}

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
}
