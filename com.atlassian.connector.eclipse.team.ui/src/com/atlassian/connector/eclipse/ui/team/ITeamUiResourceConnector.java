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

import com.atlassian.connector.eclipse.team.core.ITeamResourceConnector;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for Team connectors for opening files in the local workspace
 * 
 * @author Shawn Minto
 * @author Wojciech Seliga
 */
public interface ITeamUiResourceConnector extends ITeamResourceConnector {

	boolean isEnabled();

	boolean canHandleFile(String repoUrl, String filePath, IProgressMonitor monitor);

	/**
	 * 
	 * @param repoUrl
	 * @param filePath
	 * @param otherRevisionFilePath
	 * @param revisionString
	 * @param otherRevisionString
	 * @param monitor
	 * @return null if operations is not supported/handled, otherwise editor part
	 * @throws CoreException
	 */
	@Nullable
	IEditorPart openFile(String repoUrl, String filePath, String otherRevisionFilePath, String revisionString,
			String otherRevisionString, IProgressMonitor monitor) throws CoreException;

	boolean canHandleEditorInput(IEditorInput editorInput);

	/**
	 * 
	 * @param editorInput
	 * @param activeReview
	 * @return null if operations is not supported/handled, otherwise crucible file
	 */
	@Nullable
	CrucibleFile getCorrespondingCrucibleFileFromEditorInput(IEditorInput editorInput, Review activeReview);

	boolean openCompareEditor(String repoUrl, String filePath, String otherRevisionFilePath, String oldRevisionString,
			String newRevisionString, ICompareAnnotationModel annotationModel, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * 
	 * @return human friendly name of this connector (used for instance in error messages)
	 */
	@NotNull
	String getName();

}
