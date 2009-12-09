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

import org.jetbrains.annotations.Nullable;

public class LocalStatus {
	private final String scmPath;

	private final String revision;

	private final boolean dirty;

	private final boolean binary;

	private final boolean added;

	private final boolean versioned;

	public LocalStatus(@Nullable String scmPath, @Nullable String revision, boolean added, boolean dirty,
			boolean binary, boolean versioned) {
		this.scmPath = scmPath;
		this.revision = revision;
		this.binary = binary;
		this.dirty = dirty;
		this.added = added;
		this.versioned = versioned;
	}

	@Nullable
	public String getScmPath() {
		return scmPath;
	}

	@Nullable
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

	public boolean isVersioned() {
		return versioned;
	}

	@Override
	public String toString() {
		return scmPath + "@" + revision;
	}

	public static LocalStatus makeUnversioned() {
		return new LocalStatus(null, null, false, false, false, false);
	}

	public static LocalStatus makeVersioned(@Nullable String scmPath, @Nullable String revision, boolean dirty,
			boolean binary) {
		return new LocalStatus(scmPath, revision, false, dirty, binary, true);
	}

	public static LocalStatus makeVersioned(@Nullable String scmPath, @Nullable String revision) {
		return makeVersioned(scmPath, revision, false, false);
	}

	public static LocalStatus makeAdded(@Nullable String scmPath, boolean binary) {
		return new LocalStatus(scmPath, null, true, true, binary, false);
	}
}
