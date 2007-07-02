/*******************************************************************************
 * Copyright (c) 2004 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui;

/**
 * JIRA version holder
 * 
 * @author Eugene Kuleshov
 */
class JiraVersion implements Comparable<JiraVersion> {
	private final int major;
	private final int minor;
	private final int micro;
	private final String qualifier;

	public JiraVersion(String version) {
		String[] segments = version==null ? new String[0] : version.split("\\.");
		major = segments.length>0 ? parse(segments[0]) : 0;
		minor = segments.length>1 ? parse(segments[1]) : 0;
		micro = segments.length>2 ? parse(segments[2]) : 0;
		qualifier = segments.length==0 ? "" : getQualifier(segments[segments.length-1]);
	}

	private int parse(String segment) {
		return segment.length()==0 ? 0 : Integer.parseInt(getVersion(segment));
	}

	private String getVersion(String segment) {
		int n = segment.indexOf('-');
		return n==-1 ? segment : segment.substring(0, n);
	}
	private String getQualifier(String segment) {
		int n = segment.indexOf('-');
		return n==-1 ? "" : segment.substring(n);
	}

	/**
	 * 3.6.5-#161 
	 * 3.9-#233 
	 * 3.10-DEV-190607-#251
	 */
	public int compareTo(JiraVersion v) {
		if(major<v.major) {
			return -1;
		} else if(major>v.major) {
			return 1;
		}
		
		if(minor<v.minor) {
			return -1;
		} else if(minor>v.minor) {
			return 1;
		}

		if(micro<v.micro) {
			return -1;
		} else if(micro>v.micro) {
			return 1;
		}

		// qualifier is not needed to compare with min version
		return qualifier.compareTo(v.qualifier);
	}
	
}
