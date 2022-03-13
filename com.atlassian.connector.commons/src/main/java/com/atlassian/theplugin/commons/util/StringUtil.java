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

import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTimeConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public final class StringUtil {
	private static final int BUFFER_SIZE = 4096;

	private StringUtil() {
		// this is utility class
	}

	public static String getFirstLine(@Nullable String s) {
		if (s == null) {
			return null;
		}
		int index = s.indexOf("\n");
		if (index == -1) {
			return s;
		} else {
			return s.substring(0, index);
		}
	}

	public static synchronized String decode(String str2decode) {
		// for empty strings we have to handle them separately as empty string and invalid sequence of valid characters
		// have the same effect: Base64.decode returns empty array.
		if (str2decode.length() == 0) {
			return "";
		}
		try {
			Base64 base64 = new Base64();
			byte[] passwordBytes = base64.decode(str2decode.getBytes("UTF-8"));
			if (passwordBytes == null || passwordBytes.length == 0) {
				throw new IllegalArgumentException("Cannot decode string due to not supported "
						+ "characters or becuase it is not encoded");
			}

			return new String(passwordBytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			///CLOVER:OFF
			// cannot happen
			throw new RuntimeException("UTF-8 is not supported", e);
			///CLOVER:ON
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException(
					"Cannot decode string due to not supported characters " + "or becuase it is not encoded", e);
		}
	}

	public static synchronized String encode(String str2encode) {
		try {
			Base64 base64 = new Base64();
			byte[] bytes = base64.encode(str2encode.getBytes("UTF-8"));
			return new String(bytes);
		} catch (UnsupportedEncodingException e) {
			///CLOVER:OFF
			// cannot happen
			throw new RuntimeException("UTF-8 is not supported", e);
			///CLOVER:ON
		}
	}

	public static String slurp(InputStream in) throws IOException {
		StringBuilder out = new StringBuilder();
		byte[] b = new byte[BUFFER_SIZE];
		for (int n = in.read(b); n != -1; n = in.read(b)) {
			out.append(new String(b, 0, n));
		}
		return out.toString();
	}

	/**
	 * Removes slashes from the beginning end the end of the input string
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
	 * @param text string to parse
	 * @return string without prefix slashes
	 */
	public static String removePrefixSlashes(String text) {
		if (text == null) {
			return null;
		}

		while (text.startsWith("/")) {
			text = text.substring(1, text.length());
		}

		return text;
	}

	/**
	 * Removes slashes from the end of the input string
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

	public static String generateJiraLogTimeString(final long secondsSpent) {
		StringBuilder timeLog = new StringBuilder();
		long totalSeconds = secondsSpent;
		long remainingTime = 0;
		long weeks = totalSeconds / DateTimeConstants.SECONDS_PER_WEEK;
		remainingTime = totalSeconds - weeks * DateTimeConstants.SECONDS_PER_WEEK;
		long days = remainingTime / DateTimeConstants.SECONDS_PER_DAY;
		remainingTime = remainingTime - days * DateTimeConstants.SECONDS_PER_DAY;
		long hours = remainingTime / DateTimeConstants.SECONDS_PER_HOUR;
		remainingTime = remainingTime - hours * DateTimeConstants.SECONDS_PER_HOUR;
		long minutes = remainingTime / DateTimeConstants.SECONDS_PER_MINUTE;


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
