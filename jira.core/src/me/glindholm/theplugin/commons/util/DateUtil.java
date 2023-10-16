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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author lguminski Taken from the Bamboo project.
 */
public abstract class DateUtil {
    private static final String PRIOR_TEXT = "ago";
    public static final int SECONDS_IN_MINUTE = 60;
    public static final int MILISECONDS_IN_SECOND = 1000;

    private DateUtil() {
    }

    public static String getRelativePastDate(final Instant someDate) {
        return getRelativePastDate(Date.from(someDate));
    }

    public static String getRelativePastDate(final Date someDate) {

        if (someDate != null) {
            return getRelativePastDate(new Date(), someDate);
        }
        return "Unknown";
    }

    public static String getRelativePastDate(final Date comparedTo, final Date someDate) {
        if (someDate != null) {
            final Period period = Period.between(LocalDate.ofInstant(comparedTo.toInstant(), ZoneId.systemDefault()),
                    LocalDate.ofInstant(someDate.toInstant(), ZoneId.systemDefault()));
            final StringBuilder buffer = new StringBuilder();

            final int years = period.getYears();
            if (years > 0) {
                return formatRelativeDateItem(buffer, years, " year");
            }

            final int months = period.getMonths();
            if (months > 0) {
                return formatRelativeDateItem(buffer, months, " month");
            }

            final int weeks = period.getDays() / 7;
            if (weeks > 0) {
                return formatRelativeDateItem(buffer, weeks, " week");
            }

            final int days = period.getDays();
            if (days > 0) {
                return formatRelativeDateItem(buffer, days, " day");
            }

            final Duration duration = Duration.between(LocalDateTime.ofInstant(comparedTo.toInstant(), ZoneId.systemDefault()),
                    LocalDateTime.ofInstant(someDate.toInstant(), ZoneId.systemDefault()));
            final int hours = (int) duration.toHours();
            if (hours > 0) {
                return formatRelativeDateItem(buffer, hours, " hour");
            }
            final int minutes = (int) duration.toMinutes();
            if (minutes > 0) {
                return formatRelativeDateItem(buffer, minutes, " minute");
            }

            final int seconds = (int) duration.getSeconds();
            if (seconds > 0) {
                return formatRelativeDateItem(buffer, seconds, " second");
            }

            // if (someDate.getTime() > comparedTo.getTime()) {
            // return "in the future";
            // }

            return "< 1 second " + PRIOR_TEXT;
        } else {
            // Returning a blank string for relative date
            return "";
        }

    }

    private static String formatRelativeDateItem(final StringBuilder buffer, final int numberOfItems, final String item) {
        buffer.append(numberOfItems).append(item);
        if (numberOfItems > 1) {
            buffer.append("s");
        }
        buffer.append(" " + PRIOR_TEXT);
        return buffer.toString();
    }
}
