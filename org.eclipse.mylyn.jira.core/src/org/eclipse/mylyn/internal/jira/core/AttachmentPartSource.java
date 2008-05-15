/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.eclipse.mylyn.internal.tasks.core.deprecated.ITaskAttachment;

/**
 * @author Steffen Pingel
 */
@SuppressWarnings( { "restriction", "deprecation" })
@Deprecated
public class AttachmentPartSource implements PartSource {

	private final ITaskAttachment attachment;

	public AttachmentPartSource(ITaskAttachment attachment) {
		this.attachment = attachment;
	}

	public InputStream createInputStream() throws IOException {
		return attachment.createInputStream();
	}

	public String getFileName() {
		return attachment.getFilename();
	}

	public long getLength() {
		return attachment.getLength();
	}

}
