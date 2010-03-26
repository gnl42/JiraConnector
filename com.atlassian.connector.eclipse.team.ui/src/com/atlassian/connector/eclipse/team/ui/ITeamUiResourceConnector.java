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

package com.atlassian.connector.eclipse.team.ui;

import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

/**
 * Interface for Team connectors for opening files in the local workspace
 * 
 * @author Shawn Minto
 * @author Wojciech Seliga
 */
public interface ITeamUiResourceConnector {

	enum State {
		// resources modified from SCM base state
		SF_ANY_CHANGE, SF_UNVERSIONED, SF_IGNORED, SF_ALL, SF_VERSIONED
	};

	boolean isEnabled();

	/**
	 * @param repositoryUrl
	 *            The repository URL to get changesets from
	 * @param limit
	 *            The amount of revisions to retrieve (if 0 download all of them, implementations may not support this
	 *            operation)
	 * @return latest changesets
	 * @throws CoreException
	 *             on any error
	 */
	@NotNull
	SortedSet<ICustomChangesetLogEntry> getLatestChangesets(@NotNull String repositoryUrl, int limit,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * Return local revision status. Maybe called from UI thread.
	 * 
	 * @param resource
	 * @return null if operation is not handled/supported, otherwise revision info
	 * @throws CoreException
	 */
	@Nullable
	LocalStatus getLocalRevision(@NotNull IResource resource) throws CoreException;

	/**
	 * @param resource
	 *            a resource which is managed by this team repository connector
	 * @return <code>null</code> if this connector does not support given {@link IResource}
	 * @throws CoreException
	 */
	@Nullable
	ScmRepository getApplicableRepository(@NotNull IResource resource) throws CoreException;

	/**
	 * 
	 * @return human friendly name of this connector (used for instance in error messages)
	 */
	@NotNull
	String getName();

	/**
	 * Returns true if specified roots include recursively resources matching given filter
	 * 
	 * @param roots
	 * @param filter
	 * @return true if given roots (or their children) match given state
	 */
	boolean haveMatchingResourcesRecursive(@NotNull IResource[] roots, State filter);

	/**
	 * Gets all resources matching filter (also their members, and members their members)
	 * 
	 * @param roots
	 * @param filter
	 * @return
	 */
	List<IResource> getResourcesByFilterRecursive(@NotNull IResource[] roots, State filter);

	@NotNull
	Collection<UploadItem> getUploadItemsForResources(@NotNull IResource[] resources, @NotNull IProgressMonitor monitor)
			throws CoreException;

	@NotNull
	IResource[] getMembersForContainer(@NotNull IContainer element) throws CoreException;

	/**
	 * 
	 * @param resource
	 * @return true if specified resource is managed by this {@link ITeamUiResourceConnector}, if
	 *         {@link ITeamUiResourceConnector} is disabled returns false without checking the resource
	 */
	boolean isResourceManagedBy(IResource resource);

	boolean canHandleFile(IFile file);

	/**
	 * 
	 * @param activeReview
	 * @param fileUrl
	 * @param revision
	 * @return
	 */
	@Nullable
	CrucibleFile getCrucibleFileFromReview(@NotNull Review activeReview, @NotNull String fileUrl,
			@NotNull String revision);

	/**
	 */
	@Nullable
	CrucibleFile getCrucibleFileFromReview(@NotNull Review activeReview, @NotNull IFile file);

	/**
	 * @param monitor
	 * @return repositories applicable for the current workspace
	 */
	@NotNull
	Collection<ScmRepository> getRepositories(IProgressMonitor monitor);

	boolean isResourceAcceptedByFilter(IResource resource, State state);

}
