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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jetbrains.annotations.NotNull;

import java.util.SortedSet;

public interface ITeamUiResourceConnector2 extends ITeamUiResourceConnector {

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

}
