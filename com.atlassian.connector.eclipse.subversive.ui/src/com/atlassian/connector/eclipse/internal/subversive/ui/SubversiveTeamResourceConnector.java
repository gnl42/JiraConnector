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

import com.atlassian.connector.eclipse.internal.subversive.ui.compare.CrucibleSubversiveCompareEditorInput;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.connector.eclipse.ui.team.CustomChangeSetLogEntry;
import com.atlassian.connector.eclipse.ui.team.CustomRepository;
import com.atlassian.connector.eclipse.ui.team.ICompareAnnotationModel;
import com.atlassian.connector.eclipse.ui.team.ICustomChangesetLogEntry;
import com.atlassian.connector.eclipse.ui.team.ITeamResourceConnector;
import com.atlassian.connector.eclipse.ui.team.RepositoryInfo;
import com.atlassian.connector.eclipse.ui.team.RevisionInfo;
import com.atlassian.connector.eclipse.ui.team.TeamUiUtils;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.RepositoryProvider;
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
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.svnstorage.SVNRepositoryFile;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
public class SubversiveTeamResourceConnector implements ITeamResourceConnector {

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
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean canHandleFile(String repoUrl, String filePath, IProgressMonitor monitor) {
		return getLocalResourceFromFilePath(filePath) != null;
	}

	public boolean openCompareEditor(String repoUrl, String newFilePath, String oldFilePath, String oldRevisionString,
			String newRevisionString, ICompareAnnotationModel annotationModel, final IProgressMonitor monitor)
			throws CoreException {

		IRepositoryResource oldRemoteFile = getSvnRemoteFile(repoUrl, oldFilePath,
				SVNRevision.fromString(oldRevisionString), newFilePath, SVNRevision.fromString(newRevisionString),
				monitor);
		IRepositoryResource newRemoteFile = getSvnRemoteFile(repoUrl, newFilePath,
				SVNRevision.fromString(newRevisionString), oldFilePath, SVNRevision.fromString(oldRevisionString),
				monitor);

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
			res.add(new RepositoryInfo(repo.getUrl(), repo.getLabel()));
		}
		return res;
	}

	public Map<CustomRepository, SortedSet<ICustomChangesetLogEntry>> getLatestChangesets(String repositoryUrl,
			int limit, IProgressMonitor monitor, MultiStatus status) throws CoreException {

		IRepositoryLocation[] repos = SVNRemoteStorage.instance().getRepositoryLocations();
		if (repos == null) {
			return Collections.emptyMap();
		}

		monitor.beginTask("Retrieving changeset for SVN (Subversive) repositories", repos.length);
		Map<CustomRepository, SortedSet<ICustomChangesetLogEntry>> map = new HashMap<CustomRepository, SortedSet<ICustomChangesetLogEntry>>();
		for (IRepositoryLocation repo : repos) {
			//if a repository is given and the repo does not match the given repository, skip it
			if (repositoryUrl != null && !repositoryUrl.equals(repo.getUrl().toString())) {
				continue;
			}
			IProgressMonitor subMonitor = org.eclipse.mylyn.commons.net.Policy.subMonitorFor(monitor, 1);
			CustomRepository customRepository = new CustomRepository(repo.getUrl().toString());
			SortedSet<ICustomChangesetLogEntry> changesets = new TreeSet<ICustomChangesetLogEntry>();
			IRepositoryRoot rootFolder = repo.getRoot();

			if (limit > 0) { //do not retrieve unlimited revisions
				subMonitor.beginTask("Retrieving changesets for " + repo.getLabel(), 101);
				GetLogMessagesOperation getLogMessagesOp = new GetLogMessagesOperation(rootFolder, false);
				getLogMessagesOp.setLimit(limit);
				getLogMessagesOp.setEndRevision(SVNRevision.fromNumber(0));
				getLogMessagesOp.setStartRevision(SVNRevision.HEAD);
				getLogMessagesOp.setIncludeMerged(SVNTeamPreferences.getMergeBoolean(SVNTeamUIPlugin.instance()
						.getPreferenceStore(), SVNTeamPreferences.MERGE_INCLUDE_MERGED_NAME));
				getLogMessagesOp.run(subMonitor);
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
									logEntry.author, Long.toString(logEntry.revision), new Date(logEntry.date),
									changed, customRepository);
							changesets.add(customEntry);
						}
					}
				} else {
					throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID, NLS.bind(
							"Could not retrieve changesetes for {0}.", repo.getLabel())));
				}
			}
			map.put(customRepository, changesets);
			subMonitor.done();
		}
		return map;
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
					"No repository URL given.."));
		}

		IResource localResource = getLocalResourceFromFilePath(filePath);

		boolean localFileNotFound = localResource == null;

		if (localFileNotFound) {
			localResource = getLocalResourceFromFilePath(otherRevisionFilePath);
		}

		if (localResource != null) {
			final SVNRevision svnRevision = SVNRevision.fromString(revisionString);
			final ILocalResource localFile = SVNRemoteStorage.instance().asLocalResource(localResource);

			if (SVNRevision.fromNumber(localFile.getBaseRevision()).equals(svnRevision)
					&& localFile.getChangeMask() == ILocalResource.NO_MODIFICATION) {
				// the file is not dirty and we have the right local copy
				IEditorPart editorPart = TeamUiUtils.openLocalResource(localResource);
				if (editorPart == null) {
					throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID, NLS.bind(
							"Could not open editor for {0}.", localFile.getName())));
				}
				return editorPart;
			} else {
				final IRepositoryFile remoteFile = getSvnRemoteFile(repoUrl, filePath,
						SVNRevision.fromString(revisionString), otherRevisionFilePath,
						SVNRevision.fromString(otherRevisionString), monitor);

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
						throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID,
								NLS.bind("Could not open editor for {0}.", remoteFile.getName())));
					}
					return editorPart;
				} else {
					throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID, NLS.bind(
							"Could not get remote file for {0}.", filePath)));
				}
			}
		} else {
			throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID, NLS.bind(
					"Could not find local resource for {0}.", filePath)));
		}
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
		return new RepositoryInfo(rootUrl, label);
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

	private IResource getLocalResourceFromFilePath(String filePath) {
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
			id = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
		} else {
			id = descriptor.getId();
		}
		return id;
	}

	private IRepositoryFile getSvnRemoteFile(String repoUrl, String filePath, SVNRevision fileRevision,
			String otherPath, SVNRevision otherRevision, final IProgressMonitor monitor) {
		if (repoUrl == null) {
			return null;
		}

		if (filePath.startsWith("/")) {
			filePath = filePath.substring(1);
		}

		IResource localResource = getLocalResourceFromFilePath(filePath);

		boolean localFileNotFound = localResource == null;

		if (localFileNotFound) {
			localResource = getLocalResourceFromFilePath(otherPath);
			fileRevision = otherRevision;
		}

		if (localResource != null) {
			final IRepositoryResource repResource = SVNRemoteStorage.instance().asRepositoryResource(localResource);
			final IRepositoryFile remoteFile = new SVNRepositoryFile(repResource.getRepositoryLocation(),
					repResource.getUrl(), fileRevision);
			return remoteFile;
		}
		return null;
	}

	public String getName() {
		return "Subversive";
	}
}
