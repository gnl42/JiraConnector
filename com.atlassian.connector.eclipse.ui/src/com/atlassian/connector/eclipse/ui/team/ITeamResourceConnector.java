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

import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * Interface for Team connectors for opening files in the local workspace
 * 
 * @author Shawn Minto
 * @author Wojciech Seliga
 */
public interface ITeamResourceConnector {

	boolean isEnabled();

	boolean canHandleFile(String repoUrl, String filePath, IProgressMonitor monitor);

	IEditorPart openFile(String repoUrl, String filePath, String otherRevisionFilePath, String revisionString,
			String otherRevisionString, IProgressMonitor monitor) throws CoreException;

	boolean canHandleEditorInput(IEditorInput editorInput);

	CrucibleFile getCorrespondingCrucibleFileFromEditorInput(IEditorInput editorInput, Review activeReview);

	boolean openCompareEditor(String repoUrl, String filePath, String otherRevisionFilePath, String oldRevisionString,
			String newRevisionString, ICompareAnnotationModel annotationModel, IProgressMonitor monitor)
			throws CoreException;

	SortedSet<Long> getRevisionsForFile(IFile files, IProgressMonitor monitor) throws CoreException;

	Map<IFile, SortedSet<Long>> getRevisionsForFile(List<IFile> files, IProgressMonitor monitor) throws CoreException;

	/**
	 * @param repositoryUrl
	 *            The repository URL to get changesets from, or NULL to retrieve form all available repositories
	 * @param limit
	 *            The amount of revisions to retrieve
	 */
	Map<CustomRepository, SortedSet<ICustomChangesetLogEntry>> getLatestChangesets(String repositoryUrl, int limit,
			IProgressMonitor monitor, MultiStatus status) throws CoreException;

	RevisionInfo getLocalRevision(IResource resource) throws CoreException;

	/**
	 * @param monitor
	 * @return repositories applicable for the current workspace
	 */
	@NotNull
	Collection<RepositoryInfo> getRepositories(IProgressMonitor monitor);

}
