/*******************************************************************************
 * Copyright (c) 2007 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.model.filter;

import java.io.Serializable;

import org.eclipse.mylyn.internal.jira.core.model.Version;

/**
 * @author	Brock Janiczak
 */
public class VersionFilter implements Filter, Serializable {
	private static final long serialVersionUID = 1L;

	private final Version[] versions;

	private final boolean unreleasedVersions;

	private final boolean releasedVersions;

	public VersionFilter(Version[] versions) {
		if (versions == null) {
			throw new IllegalArgumentException();
		}

		this.versions = versions;
		this.unreleasedVersions = false;
		this.releasedVersions = false;
	}

	public VersionFilter(boolean released, boolean unreleased) {
		versions = null;
		this.releasedVersions = released;
		this.unreleasedVersions = unreleased;
	}

	public boolean isReleasedVersions() {
		return this.releasedVersions;
	}

	public boolean isUnreleasedVersions() {
		return this.unreleasedVersions;
	}

	public boolean hasNoVersion() {
		return versions != null && versions.length == 0;
	}

	public Version[] getVersions() {
		return this.versions;
	}

	VersionFilter copy() {
		if (this.versions != null) {
			return new VersionFilter(this.versions);
		}

		return new VersionFilter(this.releasedVersions, this.unreleasedVersions);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (versions == null) {
			if (isReleasedVersions()) {
				return "<released versions>"; //$NON-NLS-1$
			}

			if (isUnreleasedVersions()) {
				return "<unreleased versions>"; //$NON-NLS-1$
			}
		}

		if (hasNoVersion()) {
			return "<no version>"; //$NON-NLS-1$
		}
		return "<specified versions>"; //$NON-NLS-1$
	}
}
