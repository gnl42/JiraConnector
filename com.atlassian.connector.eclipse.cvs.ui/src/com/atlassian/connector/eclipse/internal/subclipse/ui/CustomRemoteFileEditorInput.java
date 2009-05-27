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

package com.atlassian.connector.eclipse.internal.subclipse.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.tigris.subversion.subclipse.core.ISVNRemoteFile;
import org.tigris.subversion.subclipse.ui.editor.RemoteFileEditorInput;

/**
 * Custom editor input for remote files. Necessary because the RemoteFileEditorInput.equals does not consider Inputs
 * created from the same ISVNRemoteFile as equal
 * 
 * @author Thomas Ehrnhoefer
 */
public class CustomRemoteFileEditorInput extends RemoteFileEditorInput {

	public CustomRemoteFileEditorInput(ISVNRemoteFile file, IProgressMonitor monitor) {
		super(file, monitor);
	}

	@Override
	public int hashCode() {
		// ignore
		return super.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof CustomRemoteFileEditorInput)) {
			return false;
		}
		ISVNRemoteFile thatFile = ((RemoteFileEditorInput) o).getSVNRemoteFile();
		ISVNRemoteFile thisFile = getSVNRemoteFile();
		if (thatFile == null) {
			if (thisFile == null) {
				return true;
			} else {
				return false;
			}
		} else if (thatFile.getUrl() == null) {
			if (thisFile.getUrl() == null) {
				return true;
			} else {
				return false;
			}
		} else if (thisFile.getRevision() == null) {
			if (thatFile.getRevision() == null) {
				return true;
			}
		} else if (thisFile.getUrl().equals(thatFile.getUrl())) {
			if (thisFile.getRevision().equals(thatFile.getRevision())) {
				return true;
			}
		}
		return false;
	}
}
