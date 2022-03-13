/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.theplugin.commons.fisheye.api.model.changeset;

public final class FileRevisionKey {

	private final String rev;
	private final String path;

	public FileRevisionKey(String rev, String path) {
		this.rev = rev;
		this.path = path;
	}

	public String getRev() {
		return rev;
	}
	public String getPath() {
		return path;
	}

}
