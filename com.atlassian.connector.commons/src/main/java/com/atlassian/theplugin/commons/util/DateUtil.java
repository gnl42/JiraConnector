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

import org.joda.time.Period;

import java.util.Date;

/**
 * @author lguminski
 *         Taken from the Bamboo project.
 */
public abstract class DateUtil {
	private static final String PRIOR_TEXT = "ago";
	public static final int SECONDS_IN_MINUTE = 60;
	public static final int MILISECONDS_IN_SECOND = 1000;

	private DateUtil() {
	}

	public static String getRelativePastDate(Date someDate) {

		if (someDate != null) {
			return DateUtil.getRelativePastDate(new Date(), someDate);
		}
		return "Unknown";
	}

	public static String getRelativePastDate(Date comparedTo, Date someDate) {
		if (someDate != null) {
			Period period = new Period(someDate.getTime(), comparedTo.getTime());
			StringBuffer buffer = new StringBuffer();

			int years = period.getYears();
			if (years > 0) {
				return formatRelativeDateItem(buffer, years, " year");
			}

			int months = period.getMonths();
			if (months > 0) {
				return formatRelativeDateItem(buffer, months, " month");
			}

			int weeks = period.getWeeks();
			if (weeks > 0) {
				return formatRelativeDateItem(buffer, weeks, " week");
			}

			int days = period.getDays();
			if (days > 0) {
				return formatRelativeDateItem(buffer, days, " day");
			}

			int hours = period.getHours();
			if (hours > 0) {
				return formatRelativeDateItem(buffer, hours, " hour");
			}
			int minutes = period.getMinutes();
			if (minutes > 0) {
				return formatRelativeDateItem(buffer, minutes, " minute");
			}

			int seconds = period.getSeconds();
			if (seconds > 0) {
				return formatRelativeDateItem(buffer, seconds, " second");
			}

//			if (someDate.getTime() > comparedTo.getTime()) {
//				return "in the future";
//			}

			return "< 1 second " + PRIOR_TEXT;
		} else {
			// Returning a blank string for relative date
			return "";
		}

	}

	private static String formatRelativeDateItem(StringBuffer buffer, int numberOfItems, String item) {
		buffer.append(numberOfItems).append(item);
		if (numberOfItems > 1) {
			buffer.append("s");
		}
		buffer.append(" " + PRIOR_TEXT);
		return buffer.toString();
	}
}
