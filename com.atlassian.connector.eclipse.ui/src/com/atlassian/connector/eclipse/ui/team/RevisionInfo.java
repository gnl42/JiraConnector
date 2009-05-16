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

public class RevisionInfo {
	private final String scmPath;

	private final String revision;

	private final Boolean isBinary;

	public RevisionInfo(String scmPath, String revision, Boolean isBinary) {
		this.scmPath = scmPath;
		this.revision = revision;
		this.isBinary = isBinary;
	}

	public String getScmPath() {
		return scmPath;
	}

	public String getRevision() {
		return revision;
	}

	public Boolean isBinary() {
		return isBinary;
	}

	@Override
	public String toString() {
		return scmPath + "@" + revision;
	}
}
