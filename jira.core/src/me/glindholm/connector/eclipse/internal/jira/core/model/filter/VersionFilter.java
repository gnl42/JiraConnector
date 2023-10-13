/*******************************************************************************
 * Copyright (c) 2004, 2008 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core.model.filter;

import java.io.Serializable;

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraVersion;

/**
 * @author Brock Janiczak
 */
public class VersionFilter implements Filter, Serializable {
    private static final long serialVersionUID = 1L;

    private final JiraVersion[] versions;

    private final boolean unreleasedVersions;

    private final boolean releasedVersions;

    private final boolean hasNoneVersion;

    public VersionFilter(final JiraVersion[] versions, final boolean none, final boolean released, final boolean unreleased) {
        this.versions = versions;
        releasedVersions = released;
        unreleasedVersions = unreleased;
        hasNoneVersion = none;
    }

    public boolean isReleasedVersions() {
        return releasedVersions;
    }

    public boolean isUnreleasedVersions() {
        return unreleasedVersions;
    }

    public boolean hasNoVersion() {
        return hasNoneVersion;
    }

    public JiraVersion[] getVersions() {
        return versions;
    }

    public VersionFilter copy() {
        if (versions != null) {
            final JiraVersion[] copy = new JiraVersion[versions.length];
            System.arraycopy(versions, 0, copy, 0, versions.length);
            return new VersionFilter(copy, hasNoneVersion, releasedVersions, unreleasedVersions);
        }

        return new VersionFilter(versions, hasNoneVersion, releasedVersions, unreleasedVersions);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder();

        if (isReleasedVersions()) {
            sb.append("<released versions>"); //$NON-NLS-1$
        }

        if (isUnreleasedVersions()) {
            sb.append("<unreleased versions>"); //$NON-NLS-1$
        }

        if (hasNoVersion()) {
            sb.append("<no version>"); //$NON-NLS-1$
        }

        if (versions != null && versions.length > 0) {
            sb.append("<specified versions>"); //$NON-NLS-1$
        }

        return sb.toString();
    }
}
