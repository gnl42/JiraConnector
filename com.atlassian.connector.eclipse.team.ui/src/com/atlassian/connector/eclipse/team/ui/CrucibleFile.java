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

import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;

/**
 * This is a stucture that represents a crucible file info and whether it is the old or the new file
 * 
 * @author Shawn Minto
 * @author Pawel Niewiadomski
 */
public class CrucibleFile {

	private final CrucibleFileInfo crucibleFileInfo;

	private final VersionedVirtualFile virtualFile;

	public CrucibleFile(CrucibleFileInfo fileInfo, boolean takeOld) {
		this.crucibleFileInfo = fileInfo;
		this.virtualFile = takeOld ? fileInfo.getOldFileDescriptor() : fileInfo.getFileDescriptor();
	}

	public CrucibleFileInfo getCrucibleFileInfo() {
		return crucibleFileInfo;
	}

	public boolean isOldFile() {
		return crucibleFileInfo.getOldFileDescriptor().equals(virtualFile);
	}

	public VersionedVirtualFile getSelectedFile() {
		return virtualFile;
	}

}
