/*******************************************************************************
 * Copyright (c) 2004, 2008 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.model;

/**
 * JIRA version holder
 * 
 * @author Eugene Kuleshov
 */
public class JiraVersion implements Comparable<JiraVersion> {

	public final static JiraVersion MIN_VERSION = new JiraVersion("3.3.3"); //$NON-NLS-1$

	public static final JiraVersion JIRA_3_3 = new JiraVersion("3.3"); //$NON-NLS-1$

	public static final JiraVersion JIRA_3_4 = new JiraVersion("3.4"); //$NON-NLS-1$

	public static final JiraVersion JIRA_3_7 = new JiraVersion("3.7"); //$NON-NLS-1$

	public static final JiraVersion JIRA_3_11 = new JiraVersion("3.11"); //$NON-NLS-1$

	public static final JiraVersion JIRA_3_12 = new JiraVersion("3.12"); //$NON-NLS-1$

	private final int major;

	private final int minor;

	private final int micro;

	private final String qualifier;

	public JiraVersion(String version) {
		String[] segments = version == null ? new String[0] : version.split("\\."); //$NON-NLS-1$
		major = segments.length > 0 ? parse(segments[0]) : 0;
		minor = segments.length > 1 ? parse(segments[1]) : 0;
		micro = segments.length > 2 ? parse(segments[2]) : 0;
		qualifier = segments.length == 0 ? "" : getQualifier(segments[segments.length - 1]); //$NON-NLS-1$
	}

	private int parse(String segment) {
		return segment.length() == 0 ? 0 : Integer.parseInt(getVersion(segment));
	}

	private String getVersion(String segment) {
		int n = segment.indexOf('-');
		return n == -1 ? segment : segment.substring(0, n);
	}

	private String getQualifier(String segment) {
		int n = segment.indexOf('-');
		return n == -1 ? "" : segment.substring(n + 1); //$NON-NLS-1$
	}

	public boolean isSmallerOrEquals(JiraVersion v) {
		return compareTo(v) <= 0;
	}

	/**
	 * 3.6.5-#161 3.9-#233 3.10-DEV-190607-#251
	 */
	public int compareTo(JiraVersion v) {
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
		StringBuilder sb = new StringBuilder();
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
