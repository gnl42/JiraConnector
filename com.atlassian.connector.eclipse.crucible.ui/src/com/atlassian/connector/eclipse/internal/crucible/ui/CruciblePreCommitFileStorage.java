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

package com.atlassian.connector.eclipse.internal.crucible.ui;

import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.theplugin.commons.VersionedVirtualFile;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;

/**
 * 
 * @author Jacek Jaroczynski
 */
public class CruciblePreCommitFileStorage implements IStorage {
	private final byte[] content;

	private final VersionedVirtualFile virtualFile;

	private final CrucibleFile crucibleFile;

	private final File localCopy;

	public CruciblePreCommitFileStorage(CrucibleFile crucibleFile, byte[] content, File localCopy) {
		this.crucibleFile = crucibleFile;
		this.localCopy = localCopy;
		this.virtualFile = crucibleFile.getSelectedFile();
		this.content = content;
	}

	public InputStream getContents() {
		return new ByteArrayInputStream(content);
	}

	public IPath getFullPath() {
		return new Path(virtualFile.getUrl());
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

	public String getName() {
		return virtualFile.getName();
	}

	public boolean isReadOnly() {
		return true;
	}

	public CrucibleFile getCrucibleFile() {
		return crucibleFile;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(content);
		result = prime * result + ((virtualFile == null) ? 0 : virtualFile.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CruciblePreCommitFileStorage other = (CruciblePreCommitFileStorage) obj;
		if (!Arrays.equals(content, other.content)) {
			return false;
		}
		if (virtualFile == null) {
			if (other.virtualFile != null) {
				return false;
			}
		} else if (!virtualFile.equals(other.virtualFile)) {
			return false;
		}
		return true;
	}

	public String getLocalFilePath() {
		if (localCopy != null) {
			return localCopy.getAbsolutePath();
		}
		return null;
	}

}