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

package com.atlassian.connector.eclipse.internal.cvs.ui;

import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector;
import com.atlassian.connector.eclipse.team.ui.LocalStatus;
import com.atlassian.connector.eclipse.team.ui.ScmRepository;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.repo.RepositoryManager;
import org.eclipse.team.internal.ccvs.ui.repo.RepositoryRoot;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Connector to handle connecting to a CVS repository
 * 
 * @author Pawel Niewiadomiski
 * @author Wojciech Seliga
 */
@SuppressWarnings("restriction")
public class CvsTeamResourceConnector implements ITeamUiResourceConnector {

	public boolean isEnabled() {
		return true;
	}

	public Collection<ScmRepository> getRepositories(IProgressMonitor monitor) {
		ICVSRepositoryLocation[] repositories = CVSProviderPlugin.getPlugin().getKnownRepositories();
		if (repositories == null) {
			return MiscUtil.buildArrayList();
		}

		List<ScmRepository> res = MiscUtil.buildArrayList(repositories.length);
		final RepositoryManager repositoryManager = CVSUIPlugin.getPlugin().getRepositoryManager();

		for (ICVSRepositoryLocation repo : repositories) {
			final RepositoryRoot root = repositoryManager.getRepositoryRootFor(repo);
			final String name = (root != null && root.getName() != null) ? root.getName() : null;
			res.add(new ScmRepository(repo.getLocation(true), name, this));
		}
		return res;
	}

	public LocalStatus getLocalRevision(IResource resource) throws CoreException {
		final IProject project = resource.getProject();
		if (project == null) {
			return null;
		}

		if (CVSWorkspaceRoot.isSharedWithCVS(project)) {
			final ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
			final ResourceSyncInfo syncInfo = cvsResource.getSyncInfo();
			// syncInfo is null for projects (directly checked-out)
			final boolean isBinary = syncInfo != null && syncInfo.getKeywordMode().isBinary();

			final ICVSFolder folder = (ICVSFolder) CVSWorkspaceRoot.getCVSResourceFor(project);
			final FolderSyncInfo folderInfo = folder.getFolderSyncInfo();
			final String revision = syncInfo != null ? "".equals(syncInfo.getRevision()) ? null
					: syncInfo.getRevision() : null;

			return new LocalStatus(folderInfo.getRoot() + '/' + cvsResource.getRepositoryRelativePath(), revision,
					syncInfo == null || syncInfo.isAdded(), false, isBinary, true, false);
		}
		return null;
	}

	public ScmRepository getApplicableRepository(IResource resource) throws CoreException {
		final IProject project = resource.getProject();
		if (project == null) {
			return null;
		}
		if (CVSWorkspaceRoot.isSharedWithCVS(project)) {
			final ICVSFolder folder = (ICVSFolder) CVSWorkspaceRoot.getCVSResourceFor(project);
			final FolderSyncInfo folderInfo = folder.getFolderSyncInfo();
			return folderInfo != null ? new ScmRepository(folderInfo.getRoot(), null, this) : null;
		}
		return null;
	}

	public String getName() {
		return "CVS (FishEye only)";
	}

	public boolean haveMatchingResourcesRecursive(IResource[] roots, State filter) {
		return false;
	}

	public Collection<UploadItem> getUploadItemsForResources(IResource[] resources, @NotNull IProgressMonitor monitor)
			throws CoreException {
		return MiscUtil.buildArrayList();
	}

	@NotNull
	public IResource[] getMembersForContainer(@NotNull IContainer element) throws CoreException {
		return new IResource[0];
	}

	public List<IResource> getResourcesByFilterRecursive(IResource[] roots, State filter) {
		return MiscUtil.buildArrayList();
	}

	public boolean isResourceManagedBy(IResource resource) {
		if (!isEnabled()) {
			return false;
		}
		try {
			return CVSWorkspaceRoot.isSharedWithCVS(resource);
		} catch (CVSException e) {
			return false;
		}
	}

	public CrucibleFile getCrucibleFileFromReview(Review activeReview, String fileUrl, String revision) {
		// ignore
		return null;
	}

	public CrucibleFile getCrucibleFileFromReview(Review activeReview, IFile file) {
		// ignore
		return null;
	}

	public boolean isResourceAcceptedByFilter(IResource resource, State state) {
		// ignore
		return false;
	}

	public boolean canHandleFile(IFile file) {
		// ignore
		return false;
	}

}
