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

import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.connector.eclipse.ui.team.CustomChangeSetLogEntry;
import com.atlassian.connector.eclipse.ui.team.CustomRepository;
import com.atlassian.connector.eclipse.ui.team.ICompareAnnotationModel;
import com.atlassian.connector.eclipse.ui.team.ICustomChangesetLogEntry;
import com.atlassian.connector.eclipse.ui.team.ITeamResourceConnector;
import com.atlassian.connector.eclipse.ui.team.RepositoryInfo;
import com.atlassian.connector.eclipse.ui.team.RevisionInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNLogPath;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

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

	public boolean isEnabled() {
		return true;
	}

	public boolean canHandleFile(String repoUrl, String filePath, IProgressMonitor monitor) {
		return false;
	}

	public boolean openCompareEditor(String repoUrl, String newFilePath, String oldFilePath, String oldRevisionString,
			String newRevisionString, ICompareAnnotationModel annotationModel, final IProgressMonitor monitor)
			throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID, NLS.bind(
				"Could not get revisions for {0}.", newFilePath)));
	}

	public SortedSet<Long> getRevisionsForFile(IFile file, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(file);
		throw new CoreException(new Status(IStatus.WARNING, AtlassianSubversiveUiPlugin.PLUGIN_ID,
				"Not implemented yet for Subversive."));
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
			int limit, IProgressMonitor monitor, MultiStatus status) {

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
				GetLogMessagesOperation getLogMessagesOp = new GetLogMessagesOperation(rootFolder);
				getLogMessagesOp.setLimit(limit);
				getLogMessagesOp.setEndRevision(SVNRevision.HEAD);
				getLogMessagesOp.setStartRevision(SVNRevision.fromNumber(0));
				getLogMessagesOp.setStopOnCopy(false);
				getLogMessagesOp.setIncludeMerged(true);
				getLogMessagesOp.run(subMonitor);
				SVNLogEntry[] logEntries = getLogMessagesOp.getMessages();
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
							customRepository);
					changesets.add(customEntry);
				}
			}
			map.put(customRepository, changesets);
			subMonitor.done();
		}
		return map;
	}

	public Map<IFile, SortedSet<Long>> getRevisionsForFile(List<IFile> files, IProgressMonitor monitor)
			throws CoreException {
		// @todo
		Assert.isNotNull(files);
		throw new CoreException(new Status(IStatus.WARNING, AtlassianSubversiveUiPlugin.PLUGIN_ID,
				"Not implemented yet for Subversive."));

	}

	public IEditorPart openFile(String repoUrl, String filePath, String otherRevisionFilePath, String revisionString,
			String otherRevisionString, final IProgressMonitor monitor) throws CoreException {
		if (repoUrl == null) {
			throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID,
					"No repository URL given.."));
		}
		// @todo
		return null;
	}

	public boolean canHandleEditorInput(IEditorInput editorInput) {
		// @todo
		return false;
	}

	public CrucibleFile getCorrespondingCrucibleFileFromEditorInput(IEditorInput editorInput, Review activeReview) {
		// @todo
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
}
