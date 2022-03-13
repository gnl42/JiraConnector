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

package com.atlassian.theplugin.commons.util;

import com.atlassian.theplugin.commons.exception.IncorrectVersionException;

import java.io.Serializable;
import java.util.Scanner;
import java.util.regex.MatchResult;

/**
 * Class encapsulating version descriptor of a form e.g. "0.3.0"
 */
public class ProductVersionUtil implements Serializable {
	public static final String SPECIAL_DEV_VERSION = "${project.version}, SVN:${buildNumber}";

	public static final ProductVersionUtil NULL_VERSION = initNullVersion();

	private static final long serialVersionUID = 1846608052207718100L;

	private static ProductVersionUtil initNullVersion() {
		ProductVersionUtil result = null;
		try {
			result = new ProductVersionUtil("0.0.0.0");
		} catch (IncorrectVersionException e) {
			e.printStackTrace();
		}
		return result;
	}

	private transient VersionNumber versionNumber;

	private final String version;

	public ProductVersionUtil(String version) throws IncorrectVersionException {
		this.version = version;
		parseVersionString(version);
	}

	public ProductVersionUtil() throws IncorrectVersionException {
		this(NULL_VERSION.getVersion());
	}

	public String getVersion() {
		return version;
	}

	private void parseVersionString(String aVersion) throws IncorrectVersionException {
		if (!aVersion.equals(SPECIAL_DEV_VERSION)) {
			tokenize(aVersion.toUpperCase());
		}
	}

	private static final String PATTERN = "^(\\d+)\\.(\\d+)(\\.(\\d+))?(\\.(\\d+))?(.+)?$";

	private static final int MAJOR_TOKEN_GRP = 1;

	private static final int MINOR_TOKEN_GRP = 2;

	private static final int MICRO_TOKEN_GRP = 4;

    private static final int NANO_TOKEN_GRP = 6;

	private void tokenize(final String aVersion) throws IncorrectVersionException {
		Scanner s = new Scanner(aVersion);
		s.findInLine(PATTERN);
		try {
			MatchResult result = s.match();
			versionNumber = new VersionNumber(Integer.valueOf(result.group(MAJOR_TOKEN_GRP)),
					Integer.valueOf(result.group(MINOR_TOKEN_GRP)),
					result.group(MICRO_TOKEN_GRP) != null ? Integer.valueOf(result.group(MICRO_TOKEN_GRP)) : -1,
                    result.group(NANO_TOKEN_GRP) != null ? Integer.valueOf(result.group(NANO_TOKEN_GRP)) : -1);
		} catch (IllegalStateException ex) {
			throw new IncorrectVersionException("Version (" + aVersion + ") does not match pattern (\"" + PATTERN
					+ "\")", ex);
		}
	}

	@Override
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		if (that == null || getClass() != that.getClass()) {
			return false;
		}

		ProductVersionUtil thatVersion = (ProductVersionUtil) that;

		if (version != null ? !version.equals(thatVersion.version) : thatVersion.version != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return (version != null ? version.hashCode() : 0);
	}

	@Override
	public String toString() {
		return version;
	}

	public boolean greater(ProductVersionUtil other) {
		if (other == null) {
			return false;
		}
		if (other.version.equals(SPECIAL_DEV_VERSION)) {
			return false;
		}
		if (version.equals(SPECIAL_DEV_VERSION)) {
			return true;
		}
		return this.getVersionNumber().greater(other.getVersionNumber());
	}

	private VersionNumber getVersionNumber() {
		return versionNumber;
	}

	private static class VersionNumber {

		private final int major;

		private final int minor;

		private final int micro;

        private final int nano;

        private static final int PRIME = 31;

		public VersionNumber(int major, int minor, int micro, int nano) throws IncorrectVersionException {
			this.major = major;
			this.minor = minor;
			this.micro = micro;
            this.nano = nano;
        }

		public boolean greater(VersionNumber other) {
			if (other == null) {
				return false;
			}

			if (major > other.major) {
				return true;
			} else {
				if (major == other.major && minor > other.minor) {
					return true;
				} else {
					if (major == other.major && minor == other.minor && micro > other.micro) {
						return true;
					} else {
                        if (major == other.major && minor == other.minor && micro == other.micro && nano > other.nano) {
                            return true;
                        }
                    }
				}
			}
			return false;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			VersionNumber that = (VersionNumber) o;

			if (major != that.major) {
				return false;
			}
			if (micro != that.micro) {
				return false;
			}
			if (minor != that.minor) {
				return false;
			}
            if (nano != that.nano) {
                return false;
            }

			return true;
		}

		@Override
		public int hashCode() {
			int result;
			result = major;
			result = PRIME * result + minor;
			result = PRIME * result + micro;
            result = PRIME * result + nano;
			return result;
		}

	}

}