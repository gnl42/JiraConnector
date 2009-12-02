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

public class LocalStatus {
	private final String scmPath;

	private final String revision;

	private final boolean dirty;

	private final boolean binary;

	private final boolean added;

	public LocalStatus(String scmPath, String revision, boolean added, boolean dirty, boolean binary) {
		this.scmPath = scmPath;
		this.revision = revision;
		this.binary = binary;
		this.dirty = dirty;
		this.added = added;
	}

	public String getScmPath() {
		return scmPath;
	}

	public String getRevision() {
		return revision;
	}

	public boolean isBinary() {
		return binary;
	}

	public boolean isDirty() {
		return dirty;
	}

	public boolean isAdded() {
		return added;
	}

	@Override
	public String toString() {
		return scmPath + "@" + revision;
	}
}
