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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

public abstract class AbstractTeamConnector implements ITeamResourceConnector {
	public static String getResourcePathWithProjectName(IResource resource) {
		final IProject project = resource.getProject();
		return (project != null ? project.getName() : "") + IPath.SEPARATOR
				+ resource.getProjectRelativePath().toString();
	}

	protected static final byte[] DELETED_ITEM = "[--item deleted--]".getBytes();

	protected static final byte[] EMPTY_ITEM = "[--item is empty--]".getBytes();
}
