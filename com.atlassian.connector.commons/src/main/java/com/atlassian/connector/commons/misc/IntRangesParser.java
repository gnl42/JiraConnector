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
package com.atlassian.connector.commons.misc;

import com.atlassian.theplugin.commons.util.MiscUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public final class IntRangesParser {
	private IntRangesParser() {
	}

	@NotNull
	public static IntRanges parse(@NotNull String rangesStr) throws NumberFormatException {
		if (rangesStr == null) {
			throw new IllegalArgumentException("Parameter cannot be null");
		}
		rangesStr = rangesStr.trim();
		if (rangesStr.length() == 0) {
			throw new NumberFormatException("Cannot parse [" + rangesStr + "] into " + IntRanges.class.getName());
		}
		String[] tokens = rangesStr.split(",");
		ArrayList<IntRange> res = MiscUtil.buildArrayList();
		for (final String nontrimmedtoken : tokens) {
			final String token = nontrimmedtoken.trim();
			try {
				int index = token.lastIndexOf('-');
				if (index < 1) {
					res.add(new IntRange(Integer.parseInt(token)));
				} else {
					// now the case for -X- -Y
					int index2 = token.lastIndexOf('-', index - 1);
					if (index2 > 0) {
						index = index2; // there was additional "-" found in the middle of the string 
					}

					final int min = Integer.parseInt(token.substring(0, index).trim());
					final int max = Integer.parseInt(token.substring(index + 1).trim());
					if (min > max) {
						throw new NumberFormatException("The lower bound of the range [" + min + "] cannot be greater than"
								+ "the upper bound of the range [" + max + "]");
					}
					res.add(new IntRange(min, max));
				}
			} catch (NumberFormatException e) {
				final NumberFormatException ex = new NumberFormatException(
						"Cannot parse [" + rangesStr + "] into " + IntRanges.class.getName());
				ex.initCause(e);
				throw ex;
			}
		}
		return new IntRanges(res);
	}
}
