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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.part.FileEditorInput;

import java.net.URI;

public class CruciblePostCommitFileInput implements IFileEditorInput, IPathEditorInput, IURIEditorInput,
		IPersistableElement, ICrucibleFileProvider {

	private final CrucibleFile crucibleFile;

	private final FileEditorInput fileEditorInput;

	public CruciblePostCommitFileInput(CrucibleFile crucibleFile, IFile file) {
		this.crucibleFile = crucibleFile;
		this.fileEditorInput = new FileEditorInput(file);
	}

	public IFile getFile() {
		return fileEditorInput.getFile();
	}

	public IPath getPath() {
		return fileEditorInput.getPath();
	}

	public URI getURI() {
		return fileEditorInput.getURI();
	}

	public String getFactoryId() {
		return fileEditorInput.getFactoryId();
	}

	public IStorage getStorage() throws CoreException {
		return fileEditorInput.getStorage();
	}

	public boolean exists() {
		return fileEditorInput.exists();
	}

	public ImageDescriptor getImageDescriptor() {
		return fileEditorInput.getImageDescriptor();
	}

	public String getName() {
		return fileEditorInput.getName();
	}

	public IPersistableElement getPersistable() {
		return fileEditorInput.getPersistable();
	}

	public String getToolTipText() {
		return fileEditorInput.getToolTipText();
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return fileEditorInput.getAdapter(adapter);
	}

	public void saveState(IMemento memento) {
		fileEditorInput.saveState(memento);
	}

	public CrucibleFile getCrucibleFile() {
		return crucibleFile;
	}

	/* (non-Javadoc)
	 * Method declared on Object.
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + crucibleFile.hashCode();
		result = prime * result + fileEditorInput.hashCode();
		return result;
	}

	/* (non-Javadoc)
	 * Method declared on Object.
	 *
	 * The <code>FileEditorInput</code> implementation of this <code>Object</code>
	 * method bases the equality of two <code>FileEditorInput</code> objects on the
	 * equality of their underlying <code>IFile</code> resources.
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CruciblePostCommitFileInput)) {
			return false;
		}
		CruciblePostCommitFileInput other = (CruciblePostCommitFileInput) obj;
		return crucibleFile.equals(other.crucibleFile) && fileEditorInput.equals(other.fileEditorInput);
	}

}
