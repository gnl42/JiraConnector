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

import com.atlassian.theplugin.commons.crucible.api.UploadItem;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public abstract class AbstractTeamUiConnector implements ITeamUiResourceConnector {
	public static String getResourcePathWithProjectName(IResource resource) {
		final IProject project = resource.getProject();
		return (project != null ? project.getName() : "") + IPath.SEPARATOR
				+ resource.getProjectRelativePath().toString();
	}

	protected static final byte[] DELETED_ITEM = "[--item deleted--]".getBytes();

	protected static final byte[] EMPTY_ITEM = "[--item is empty--]".getBytes();

	protected byte[] getResourceContent(InputStream is) {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			IOUtils.copy(is, out);
			return out.toByteArray();
		} catch (IOException e) {
			return new byte[0];
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(out);
		}
	}

	protected String getCharset(IFile file) {
		if (file == null) {
			return UploadItem.DEFAULT_CHARSET;
		}
		try {
			return file.getCharset() == null ? UploadItem.DEFAULT_CHARSET : file.getCharset();
		} catch (CoreException e) {
			return UploadItem.DEFAULT_CHARSET;
		}
	}

	protected String getContentType(IFile file) {
		String mimeType = file != null ? URLConnection.getFileNameMap().getContentTypeFor(file.getName()) : null;
		return mimeType == null ? UploadItem.DEFAULT_CONTENT_TYPE : mimeType;
	}
}
