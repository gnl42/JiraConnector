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

package com.atlassian.connector.eclipse.internal.crucible.ui.operations;

import com.atlassian.connector.eclipse.team.ui.ICompareAnnotationModel;
import com.atlassian.connector.eclipse.team.ui.TeamUiUtils;
import com.atlassian.connector.eclipse.ui.IAnnotationCompareInput;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

public class CrucibleFileInfoCompareEditorInput extends CompareEditorInput implements IAnnotationCompareInput {

	static class ByteArrayInput implements ITypedElement, IStreamContentAccessor {

		byte[] content;

		private final String name;

		public ByteArrayInput(byte[] content, String name) {
			this.content = content;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public Image getImage() {
			return null;
		}

		public String getType() {
			String extension = FilenameUtils.getExtension(name);
			return extension.length() > 0 ? extension : ITypedElement.TEXT_TYPE;
		}

		public InputStream getContents() throws CoreException {
			return new ByteArrayInputStream(content);
		}

	}

	private final byte[] content1;

	private final byte[] content2;

	private final ICompareAnnotationModel annotationModel;

	private final CrucibleFileInfo fileInfo;

	public CrucibleFileInfoCompareEditorInput(CrucibleFileInfo fileInfo, byte[] content1, byte[] content2,
			ICompareAnnotationModel annotationModel, CompareConfiguration compareConfiguration) {
		super(compareConfiguration);
		this.content1 = content1;
		this.content2 = content2;
		this.annotationModel = annotationModel;
		this.fileInfo = fileInfo;

		VersionedVirtualFile oldFile = fileInfo.getOldFileDescriptor();
		VersionedVirtualFile newFile = fileInfo.getFileDescriptor();

		setTitle("Compare " + oldFile.getName() + " " + newFile.getRevision() + " and " + oldFile.getRevision());
	}

	@Override
	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		Differencer d = new Differencer();
		Object diff = d.findDifferences(false, monitor, null, null, new ByteArrayInput(content1,
				fileInfo.getFileDescriptor().getName()), new ByteArrayInput(content2, fileInfo.getOldFileDescriptor()
				.getName()));
		return diff;
	}

	@Override
	public Viewer findContentViewer(Viewer oldViewer, ICompareInput input, Composite parent) {
		Viewer contentViewer = super.findContentViewer(oldViewer, input, parent);
		return TeamUiUtils.findContentViewer(contentViewer, input, parent, annotationModel);
	}

	public ICompareAnnotationModel getAnnotationModelToAttach() {
		return annotationModel;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotationModel == null) ? 0 : annotationModel.hashCode());
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
		CrucibleFileInfoCompareEditorInput other = (CrucibleFileInfoCompareEditorInput) obj;
		if (annotationModel == null) {
			if (other.annotationModel != null) {
				return false;
			}
		} else if (!annotationModel.equals(other.annotationModel)) {
			return false;
		}
		return true;
	}

	public CrucibleFileInfo getCrucibleFileInfo() {
		return fileInfo;
	}

}