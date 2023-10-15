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

package me.glindholm.theplugin.commons.util;

import java.io.Serializable;
import java.util.Scanner;
import java.util.regex.MatchResult;

import me.glindholm.theplugin.commons.exception.IncorrectVersionException;

/**
 * Class encapsulating version descriptor of a form e.g. "0.3.0, SVN:14021"
 */
public class Version implements Serializable {
    public static final String SPECIAL_DEV_VERSION = "${project.version}, SVN:${buildNumber}";
    public static final Version NULL_VERSION = initNullVersion();
    private static final long serialVersionUID = 1846608052207718100L;

    private static Version initNullVersion() {
        Version result = null;
        try {
            result = new Version("0.0.0, SVN:0");
        } catch (final IncorrectVersionException e) {
            e.printStackTrace();
        }
        return result;
    }

    private transient VersionNumber versionNumber;

    private final String version;

    public Version(final String version) throws IncorrectVersionException {
        this.version = version;
        parseVersionString(version);
    }

    public Version() throws IncorrectVersionException {
        this(NULL_VERSION.getVersion());
    }

    public String getVersion() {
        return version;
    }

    private void parseVersionString(final String aVersion) throws IncorrectVersionException {
        if (!aVersion.equals(SPECIAL_DEV_VERSION)) {
            tokenize(aVersion.toUpperCase());
        }
    }

    // private static final String PATTERN = "^(\\d+)\\.(\\d+)\\.(\\d+)((-(SNAPSHOT))?+), SVN:(\\d+)$";
    private static final String PATTERN = "^(\\d+)\\.(\\d+)\\.(\\d+)((-(ALPHA|BETA|SNAPSHOT))?+)((-(\\d+))?+), SVN:(\\d+)$";
    private static final int MAJOR_TOKEN_GRP = 1;
    private static final int MINOR_TOKEN_GRP = 2;
    private static final int MICRO_TOKEN_GRP = 3;
    private static final int ALPHANUM_TOKEN_GRP = 6;
    private static final int ALPHANUM_VERSION_GRP = 9;
    private static final int BUILD_TOKEN_GRP = 10;

    private void tokenize(final String aVersion) throws IncorrectVersionException {
        final Scanner s = new Scanner(aVersion);
        s.findInLine(PATTERN);
        try {
            final MatchResult result = s.match();
            versionNumber = new VersionNumber(Integer.parseInt(result.group(MAJOR_TOKEN_GRP)), Integer.parseInt(result.group(MINOR_TOKEN_GRP)),
                    Integer.parseInt(result.group(MICRO_TOKEN_GRP)), result.group(ALPHANUM_TOKEN_GRP), result.group(ALPHANUM_VERSION_GRP),
                    Integer.parseInt(result.group(BUILD_TOKEN_GRP)));
        } catch (final IllegalStateException ex) {
            throw new IncorrectVersionException("Version (" + aVersion + ") does not match pattern (\"" + PATTERN + "\")", ex);
        } finally {
            s.close();
        }
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) {
            return true;
        }
        if (that == null || getClass() != that.getClass()) {
            return false;
        }

        final Version thatVersion = (Version) that;

        if (version != null ? !version.equals(thatVersion.version) : thatVersion.version != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return version != null ? version.hashCode() : 0;
    }

    @Override
    public String toString() {
        return version;
    }

    public boolean greater(final Version other) {
        if ((other == null) || other.version.equals(SPECIAL_DEV_VERSION)) {
            return false;
        }
        if (version.equals(SPECIAL_DEV_VERSION)) {
            return true;
        }
        return getVersionNumber().greater(other.getVersionNumber());
    }

    private VersionNumber getVersionNumber() {
        return versionNumber;
    }

    private static class VersionNumber {

        private final int major;
        private final int minor;
        private final int micro;
        private final int buildNo;
        private AlphaNum alphaNum;
        private int alphaNumValue = 0;
        private static final int PRIME = 31;

        public enum AlphaNum {
            SNAPSHOT, ALPHA, BETA, NONE
        }

        public VersionNumber(final int major, final int minor, final int micro, final String alphaNum, final String alphaNumValue, final int buildNo)
                throws IncorrectVersionException {
            this.major = major;
            this.minor = minor;
            this.micro = micro;
            this.buildNo = buildNo;

            if (alphaNum == null) {
                this.alphaNum = AlphaNum.NONE;
            } else {
                try {
                    this.alphaNum = AlphaNum.valueOf(alphaNum);

                    if (alphaNumValue != null) {
                        this.alphaNumValue = Integer.parseInt(alphaNumValue);
                    }
                } catch (final IllegalArgumentException ex) {
                    throw new IncorrectVersionException("Unknown version alphanum: " + alphaNum);
                }
            }
        }

        public boolean greater(final VersionNumber other) {
            if (other == null) {
                return false;
            }

            if ((major > other.major) || (major == other.major && minor > other.minor)) {
                return true;
            } else {
                if (major == other.major && minor == other.minor && micro > other.micro) {
                    return true;
                } else {
                    if (major == other.major && minor == other.minor && micro == other.micro) {
                        return buildNo > other.buildNo;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final VersionNumber that = (VersionNumber) o;

            if ((major != that.major) || (micro != that.micro) || (minor != that.minor) || (alphaNum != that.alphaNum)) {
                return false;
            }

            if (alphaNumValue != that.alphaNumValue) {
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
            result = PRIME * result + (alphaNum != null ? alphaNum.hashCode() : 0);
            return result;
        }

    }

}
