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

package com.atlassian.connector.eclipse.internal.perforce.ui;

import com.atlassian.connector.eclipse.internal.perforce.core.FileUtility;
import com.atlassian.connector.eclipse.internal.perforce.core.IStateFilter;
import com.atlassian.connector.eclipse.team.ui.AbstractTeamUiConnector;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.connector.eclipse.team.ui.ICustomChangesetLogEntry;
import com.atlassian.connector.eclipse.team.ui.LocalStatus;
import com.atlassian.connector.eclipse.team.ui.ScmRepository;
import com.atlassian.connector.eclipse.team.ui.TeamConnectorType;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

/**
 * Connector to handle connecting to a CVS repository
 * 
 * @author Pawel Niewiadomski
 * @author Wojciech Seliga
 */
public class PerforceTeamUiResourceConnector extends AbstractTeamUiConnector {

	public boolean isEnabled() {
		return true;
	}

	@NotNull
	public SortedSet<ICustomChangesetLogEntry> getLatestChangesets(@NotNull String repositoryUrl, int limit,
			IProgressMonitor monitor) throws CoreException {

		throw new UnsupportedOperationException();
	}

	public Collection<ScmRepository> getRepositories(IProgressMonitor monitor) {
		throw new UnsupportedOperationException();
	}

	protected ScmRepository getRepository(String url, IProgressMonitor monitor) {
		throw new UnsupportedOperationException();
	}

	public LocalStatus getLocalRevision(IResource resource) throws CoreException {
		throw new UnsupportedOperationException();
	}

	public ScmRepository getApplicableRepository(IResource resource) {
		throw new UnsupportedOperationException();
	}

	public String getName() {
		return "Perforce";
	}

	public boolean haveMatchingResourcesRecursive(IResource[] roots, State filter) {
		return FileUtility.checkForResourcesPresenceRecursive(roots, getStateFilter(filter));
	}

	public Collection<UploadItem> getUploadItemsForResources(IResource[] resources, IProgressMonitor monitor)
			throws CoreException {
		throw new UnsupportedOperationException();
	}

	public List<IResource> getResourcesByFilterRecursive(IResource[] roots, State filter) {
		return FileUtility.getResourcesByFilterRecursive(roots, getStateFilter(filter));
	}

	public boolean isResourceAcceptedByFilter(IResource resource, State filter) {
		IP4Resource svnResource = P4Workspace.getWorkspace().getResource(resource);
		return getStateFilter(filter).accept(svnResource);
	}

	public boolean isResourceManagedBy(IResource resource) {
		if (!isEnabled()) {
			return false;
		}
		return FileUtility.isManagedByPerforce(resource);
	}

	public TeamConnectorType getType() {
		return TeamConnectorType.SVN;
	}

	public boolean canHandleEditorInput(IEditorInput editorInput) {
		throw new UnsupportedOperationException();
	}

	public boolean canHandleFile(IFile file) {
		throw new UnsupportedOperationException();
	}

	public CrucibleFile getCrucibleFileFromReview(Review activeReview, IFile file) {
		throw new UnsupportedOperationException();
	}

	public CrucibleFile getCrucibleFileFromReview(Review activeReview, String fileUrl, String revision) {
		throw new UnsupportedOperationException();
	}

	public CrucibleFile getCrucibleFileFromReview(Review activeReview, IEditorInput editorInput) {
		throw new UnsupportedOperationException();
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
		return FileUtility.getAllMembers(element);
	}

}
