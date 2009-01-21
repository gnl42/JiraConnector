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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 * Interface for Team connectors for opening files in the local workspace
 * 
 * @author Shawn Minto
 */
public interface ITeamResourceConnector {

	boolean isEnabled();

	boolean canHandleFile(String repoUrl, String filePath, String revisionString, IProgressMonitor monitor);

	IEditorPart openFile(String repoUrl, String filePath, String revisionString, IProgressMonitor monitor);

	boolean canGetCrucibleFileFromEditorInput(IEditorInput editorInput);

	CrucibleFile getCorrespondingCrucibleFileFromEditorInput(IEditorInput editorInput, Review activeReview);
}
