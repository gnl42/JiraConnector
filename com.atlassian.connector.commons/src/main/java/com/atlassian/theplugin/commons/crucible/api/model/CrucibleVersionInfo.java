/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.commons.crucible.api.model;

import java.io.Serializable;

/**
 * @author Marek Went
 * @author Pawel Niewiadomski
 */
@SuppressWarnings("serial")
public class CrucibleVersionInfo implements Serializable, Comparable<CrucibleVersionInfo> {
	private final String buildDate;

	private final String releaseNumber;

	private Integer major;
	private Integer minor;
	private Integer maintanance;
	private String build;

	public CrucibleVersionInfo(String releaseNumber, String buildDate) {
		this.buildDate = buildDate;
		this.releaseNumber = releaseNumber;
		tokenizeVersionAndSetFields(releaseNumber);
	}

	public String getBuildDate() {
		return buildDate;
	}

	public String getReleaseNumber() {
		return releaseNumber;
	}

	public boolean isVersion2OrGreater() {
		return (major != null && major >= 2);
	}

	public boolean isVersion21OrGreater() {
		if (major == null || minor == null) {
			return false;
		}

		if (major > 2 || (major == 2 && minor >= 1)) {
			return true;
		}

		return false;
	}

	public boolean isVersion24OrGrater() {

		if (major == null || minor == null) {
			return false;
		}

		if (major > 2 || (major == 2 && minor >= 4)) {
			return true;
		}

		return false;
	}
	private void tokenizeVersionAndSetFields(String number) {
		String[] tokens = number.split("[.]");

		major = null;
		minor = null;
		maintanance = null;
		build = null;

		try {
			if (tokens.length > 0) {
				major = Integer.valueOf(tokens[0]);
				if (tokens.length > 1) {
					minor = Integer.valueOf(tokens[1]);
					if (tokens.length > 2) {
						maintanance = Integer.valueOf(tokens[2]);
						if (tokens.length > 3) {
							build = tokens[3];
						}
					}
				}
			}
		} catch (NumberFormatException e) {
			// stop parsing
		}
	}

	public Integer getMajor() {
		return major;
	}

	public Integer getMinor() {
		return minor;
	}

	public Integer getMaintanance() {
		return maintanance;
	}

	public String getBuild() {
		return build;
	}

	private int compareInts(Integer a, Integer b) {
		if (a == null && b != null) {
			return -1;
		} else if (a != null && b == null) {
			return 1;
		} else if (a == null && b == null) {
			return 0;
		} else {
			return a.compareTo(b);
		}
	}

	public int compareTo(CrucibleVersionInfo o) {
		int r = compareInts(major, o.major);
		if (r == 0) {
			r = compareInts(minor, o.minor);
			if (r == 0) {
				return compareInts(maintanance, o.maintanance);
			} else {
				return r;
			}
		} else {
			return r;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (major != null) {
			sb.append(major);
			if (minor != null) {
				sb.append(".");
				sb.append(minor);
				if (maintanance != null) {
					sb.append(".");
					sb.append(maintanance);
					if (build != null) {
						sb.append(".");
						sb.append(build);
					}
				}
			}
		}
		return sb.toString();
	}
}
