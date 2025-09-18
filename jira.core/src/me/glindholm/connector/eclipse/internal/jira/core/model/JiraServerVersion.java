/*******************************************************************************
 * Copyright (c) 2004, 2009 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core.model;

import java.io.Serializable;

/**
 * JIRA version holder
 *
 * @author Eugene Kuleshov
 * @author Thomas Ehrnhoefer
 */
public class JiraServerVersion implements Comparable<JiraServerVersion>, Serializable {

    private static final long serialVersionUID = -838081214233322531L;

    public static final JiraServerVersion JIRA_3_13 = new JiraServerVersion("3.13"); //$NON-NLS-1$

    public static final JiraServerVersion JIRA_4_1 = new JiraServerVersion("4.1"); //$NON-NLS-1$

    public static final JiraServerVersion JIRA_4_2 = new JiraServerVersion("4.2"); //$NON-NLS-1$

    public static final JiraServerVersion JIRA_5_0 = new JiraServerVersion("5.0"); //$NON-NLS-1$

    public static final JiraServerVersion JIRA_8_0 = new JiraServerVersion("8.0"); //$NON-NLS-1$

    public static final JiraServerVersion JIRA_8_1_14 = new JiraServerVersion("8.1.14"); //$NON-NLS-1$

    public static final JiraServerVersion JIRA_9_0 = new JiraServerVersion("9.0"); //$NON-NLS-1$

    public static final JiraServerVersion JIRA_10_0 = new JiraServerVersion("10.0"); //$NON-NLS-1$

    public final static JiraServerVersion MIN_VERSION = JIRA_8_0;

	public static final JiraServerVersion JIRA_1001_0 = new JiraServerVersion("1001.0"); //$NON-NLS-1$

    private final int major;

    private final int minor;

    private final int micro;

    private final String qualifier;

    public JiraServerVersion(final String version) {
        final String[] segments = version == null ? new String[0] : version.split("\\."); //$NON-NLS-1$
        major = segments.length > 0 ? parse(segments[0]) : 0;
        minor = segments.length > 1 ? parse(segments[1]) : 0;
        micro = segments.length > 2 ? parse(segments[2]) : 0;
        qualifier = segments.length == 0 ? "" : getQualifier(segments[segments.length - 1]); //$NON-NLS-1$
    }

    private int parse(final String segment) {
        try {
            return segment.length() == 0 ? 0 : Integer.parseInt(getVersion(segment));
        } catch (final NumberFormatException e) {
            return 0;
        }
    }

    private String getVersion(final String segment) {
        final int n = segment.indexOf('-');
        return n == -1 ? segment : segment.substring(0, n);
    }

    private String getQualifier(final String segment) {
        final int n = segment.indexOf('-');
        return n == -1 ? "" : segment.substring(n + 1); //$NON-NLS-1$
    }

    public boolean isLessThan(final JiraServerVersion v) {
        return compareTo(v) < 0;
    }

    public boolean isLessThanOrEquals(final JiraServerVersion v) {
        return compareTo(v) <= 0;
    }

    public boolean isGreaterThanOrEquals(final JiraServerVersion v) {
        return compareTo(v) >= 0;
    }

    /**
     * 3.6.5-#161 3.9-#233 3.10-DEV-190607-#251
     */
    @Override
    public int compareTo(final JiraServerVersion v) {
        if (major < v.major) {
            return -1;
        } else if (major > v.major) {
            return 1;
        }

        if (minor < v.minor) {
            return -1;
        } else if (minor > v.minor) {
            return 1;
        }

        if (micro < v.micro) {
            return -1;
        } else if (micro > v.micro) {
            return 1;
        }

        // qualifier is not needed to compare with min version
        return qualifier.compareTo(v.qualifier);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(Integer.toString(major));
        sb.append(".").append(Integer.toString(minor)); //$NON-NLS-1$
        if (micro > 0) {
            sb.append(".").append(Integer.toString(micro)); //$NON-NLS-1$
        }
        if (qualifier.length() > 0) {
            sb.append("-").append(qualifier); //$NON-NLS-1$
        }
        return sb.toString();
    }

}
