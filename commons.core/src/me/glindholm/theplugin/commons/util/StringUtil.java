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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.jdt.annotation.Nullable;

public final class StringUtil {
    private static final int BUFFER_SIZE = 4096;

    private StringUtil() {
        // this is utility class
    }

    public static String getFirstLine(@Nullable final String s) {
        if (s == null) {
            return null;
        }
        final int index = s.indexOf("\n");
        if (index == -1) {
            return s;
        } else {
            return s.substring(0, index);
        }
    }

    public static synchronized String decode(final String str2decode) {
        // for empty strings we have to handle them separately as empty string and invalid sequence of valid
        // characters
        // have the same effect: Base64.decode returns empty array.
        if (str2decode.length() == 0) {
            return "";
        }
        try {
            final Base64 base64 = new Base64();
            final byte[] passwordBytes = base64.decode(str2decode.getBytes("UTF-8"));
            if (passwordBytes == null || passwordBytes.length == 0) {
                throw new IllegalArgumentException("Cannot decode string due to not supported " + "characters or becuase it is not encoded");
            }

            return new String(passwordBytes, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            /// CLOVER:OFF
            // cannot happen
            throw new RuntimeException("UTF-8 is not supported", e);
            /// CLOVER:ON
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Cannot decode string due to not supported characters " + "or becuase it is not encoded", e);
        }
    }

    public static synchronized String encode(final String str2encode) {
        try {
            final Base64 base64 = new Base64();
            final byte[] bytes = base64.encode(str2encode.getBytes("UTF-8"));
            return new String(bytes);
        } catch (final UnsupportedEncodingException e) {
            /// CLOVER:OFF
            // cannot happen
            throw new RuntimeException("UTF-8 is not supported", e);
            /// CLOVER:ON
        }
    }

    public static String slurp(final InputStream in) throws IOException { // FIXME Replace with IOUtils.toString()
        final StringBuilder out = new StringBuilder();
        final byte[] b = new byte[BUFFER_SIZE];
        for (int n = in.read(b); n != -1; n = in.read(b)) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

    /**
     * Removes slashes from the beginning end the end of the input string
     *
     * @param text string to parse
     * @return string without trailing slashes
     */
    public static String removeLeadingAndTrailingSlashes(String text) {
        if (text == null) {
            return null;
        }

        text = removePrefixSlashes(text);
        text = removeSuffixSlashes(text);

        return text;
    }

    /**
     * Removes slashes from the beginning of the input string
     *
     * @param text string to parse
     * @return string without prefix slashes
     */
    public static String removePrefixSlashes(String text) {
        if (text == null) {
            return null;
        }

        while (text.startsWith("/")) {
            text = text.substring(1);
        }

        return text;
    }

    /**
     * Removes slashes from the end of the input string
     *
     * @param text string to parse
     * @return string without suffix slashes
     */
    public static String removeSuffixSlashes(String text) {
        if (text == null) {
            return null;
        }

        while (text.endsWith("/")) {
            text = text.substring(0, text.length() - 1);
        }

        return text;
    }

    private static final int SECONDS_PER_MINUTE = 60;
    private static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * 60;
    private static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * 24;
    private static final int SECONDS_PER_WEEK = SECONDS_PER_DAY * 7;

    public static String generateJiraLogTimeString(final long secondsSpent) {
        final StringBuilder timeLog = new StringBuilder();
        final long totalSeconds = secondsSpent;
        final long weeks = totalSeconds / SECONDS_PER_WEEK;
        long remainingTime = totalSeconds - weeks * SECONDS_PER_WEEK;
        final long days = remainingTime / SECONDS_PER_DAY;
        remainingTime = remainingTime - days * SECONDS_PER_DAY;
        final long hours = remainingTime / SECONDS_PER_HOUR;
        remainingTime = remainingTime - hours * SECONDS_PER_HOUR;
        final long minutes = remainingTime / SECONDS_PER_MINUTE;

        if (weeks > 0) {
            timeLog.append(" ").append(weeks).append("w");
        }

        if (days > 0) {
            timeLog.append(" ").append(days).append("d");
        }

        if (hours > 0) {
            timeLog.append(" ").append(hours).append("h");
        }

        if (minutes > 0) {
            timeLog.append(" ").append(minutes).append("m");
        }

        return timeLog.toString().trim();
    }
}
