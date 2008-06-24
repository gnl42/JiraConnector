/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;

/**
 * JIRA time format to convert Long value in seconds to JIRA time format:
 * 
 * <blockquote> The format of this is '*w *d *h *m' (representing weeks, days, hours and minutes - where * can be any
 * number) Examples: 4d, 5h 30m, 60m and 3w. Note: The conversion rates are 1w = 7d and 1d = 24h </blockquote>
 * 
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 */
public class JiraTimeFormat extends Format {

	public static final int DEFAULT_WORK_DAYS_PER_WEEK = 7;

	public static final int DEFAULT_WORK_HOURS_PER_DAY = 24;

	private static final long serialVersionUID = 1L;

	private final int workDaysPerWeek;

	private final int workHoursPerDay;

	public JiraTimeFormat() {
		this(DEFAULT_WORK_DAYS_PER_WEEK, DEFAULT_WORK_HOURS_PER_DAY);
	}

	public JiraTimeFormat(int workDaysPerWeek, int workHoursPerDay) {
		Assert.isTrue(1 <= workDaysPerWeek && workDaysPerWeek <= 7);
		Assert.isTrue(1 <= workHoursPerDay && workHoursPerDay <= 24);
		this.workDaysPerWeek = workDaysPerWeek;
		this.workHoursPerDay = workHoursPerDay;
	}

	/**
	 * A simplified conversion from seconds to '*h *m' format
	 * 
	 * @param a
	 *            Long seconds value to format
	 */
	@Override
	public StringBuffer format(Object obj, StringBuffer sb, FieldPosition pos) {
		if (obj instanceof Long) {
			format(sb, (Long) obj);
		} else if (obj instanceof Integer) {
			format(sb, (Integer) obj);
		}
		return sb;
	}

	private void format(StringBuffer sb, long seconds) {
		long weeks = seconds / (workDaysPerWeek * workHoursPerDay * 60 * 60);
		if (weeks > 0) {
			sb.append(Long.toString(weeks)).append('w');
		}
		long days = (seconds % (workDaysPerWeek * workHoursPerDay * 60 * 60)) / (workHoursPerDay * 60 * 60);
		if (days > 0) {
			if (sb.length() > 0) {
				sb.append(' ');
			}
			sb.append(Long.toString(days)).append('d');
		}
		long hours = (seconds % (workHoursPerDay * 60 * 60)) / (60 * 60);
		if (hours > 0) {
			if (sb.length() > 0) {
				sb.append(' ');
			}
			sb.append(Long.toString(hours)).append('h');
		}
		long minutes = (seconds % (60 * 60)) / 60;
		if (minutes > 0) {
			if (sb.length() > 0) {
				sb.append(' ');
			}
			sb.append(Long.toString(minutes)).append('m');
		} else if (sb.length() == 0) {
			sb.append("0m");
		}
	}

	/**
	 * Returns the time value of <code>source</code> in seconds.
	 * 
	 * @param source
	 *            the time string to parse; must not be <code>null</code>
	 */
	public long parse(String source) {
		Assert.isNotNull(source);
		return (Long) parseObject(source, new ParsePosition(0));
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		Pattern pattern = Pattern.compile("(\\d+w)?\\s?(\\d+d)?\\s?(\\d+h)?\\s?(\\d+m)?");
		Matcher matcher = pattern.matcher(source);
		long value = 0;
		if (matcher.find()) {
			for (int i = 1; i <= matcher.groupCount(); i++) {
				String group = matcher.group(i);
				if (group != null) {
					if (group.endsWith("m")) {
						value += Long.parseLong(group.substring(0, group.length() - 1)) * 60;
					} else if (group.endsWith("h")) {
						value += Long.parseLong(group.substring(0, group.length() - 1)) * 60 * 60;
					} else if (group.endsWith("d")) {
						value += Long.parseLong(group.substring(0, group.length() - 1)) * 60 * 60 * workHoursPerDay;
					} else if (group.endsWith("w")) {
						value += Long.parseLong(group.substring(0, group.length() - 1)) * 60 * 60 * workHoursPerDay
								* workDaysPerWeek;
					}
				}
			}
		}
		pos.setIndex(source.length() + 1);
		return Long.valueOf(value);
	}

}
