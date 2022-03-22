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

public final class IntRange {
	private final int min;
	private final int max;
	private static final int MAGIC = 31;

	public IntRange(final int number) {
		this.min = number;
		this.max = number;
	}

	public boolean isSingleNumber() {
		return min == max;
	}

	public IntRange(final int min, final int max) {
		if (min > max) {
			throw new IllegalArgumentException("Lower bound [" + min + "] cannot be greater than upper bound [" + max + "]");
		}
		this.max = max;
		this.min = min;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	@Override
	public String toString() {
		if (isSingleNumber()) {
			return "[" + min + "]";
		} else {
			return "[" + min + "-" + max + "]";
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final IntRange intRange = (IntRange) o;

		if (max != intRange.max) {
			return false;
		}
		//noinspection RedundantIfStatement
		if (min != intRange.min) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = min;
		result = MAGIC * result + max;
		return result;
	}

	public String toNiceString() {
		if (isSingleNumber()) {
			return String.valueOf(min);
		} else {
			return min + " - " + max;
		}
	}
}
