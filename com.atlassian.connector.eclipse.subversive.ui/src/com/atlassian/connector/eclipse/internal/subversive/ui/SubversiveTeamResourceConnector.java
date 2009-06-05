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
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

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
		// @todo implement it
		return Collections.emptyMap();
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

		IRepositoryResource svnResource = SVNRemoteStorage.instance().asRepositoryResource(resource);
		try {
			String mimeTypeProp = SVNUtility.getPropertyForNotConnected(resource, SVNProperty.BuiltIn.MIME_TYPE);
			boolean isBinary = (mimeTypeProp != null && !mimeTypeProp.startsWith("text"));

			return new RevisionInfo(svnResource.getUrl(), Long.toString(svnResource.getRevision()), isBinary);
		} catch (SVNConnectorException e) {
			throw new CoreException(new Status(IStatus.ERROR, AtlassianSubversiveUiPlugin.PLUGIN_ID,
					"Cannot determine SVN information for resource [" + resource + "]", e));
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
