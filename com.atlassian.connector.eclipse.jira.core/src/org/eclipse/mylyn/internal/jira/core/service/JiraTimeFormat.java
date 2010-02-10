/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;

import org.eclipse.core.runtime.Assert;

/**
 * JIRA time format to convert Long value in seconds to JIRA time format: <blockquote> The format of this is '*w *d *h
 * *m' (representing weeks, days, hours and minutes - where * can be any number) Examples: 4d, 5h 30m, 60m and 3w. Note:
 * The conversion rates are 1w = 7d and 1d = 24h </blockquote>
 * 
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
 */
public class JiraTimeFormat extends Format {

	private static final long serialVersionUID = 1L;

	private final int workDaysPerWeek;

	private final int workHoursPerDay;

	public JiraTimeFormat() {
		this(JiraConfiguration.DEFAULT_WORK_DAYS_PER_WEEK, JiraConfiguration.DEFAULT_WORK_HOURS_PER_DAY);
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
			sb.append("0m"); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the time value of <code>source</code> in seconds.
	 * 
	 * @param source
	 *            the time string to parse; must not be <code>null</code>
	 * @throws ParseException
	 *             if the string could not be parsed.
	 */
	public long parse(String source) throws ParseException {
		Assert.isNotNull(source);
		Object parsedObject = parseObject(source, new ParsePosition(0));
		if (parsedObject == null) {
			throw new ParseException("Invalid string", 0); //$NON-NLS-1$
		}
		return (Long) parsedObject;
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		// special case 0 where no letter is needed after a digit
		if (source.trim().equals("0")) { //$NON-NLS-1$
			pos.setIndex(source.length() + 1);
			return new Long(0);
		}

		StringBuffer buffer = new StringBuffer(source.length());
		char[] charArray = source.toCharArray();
		long value = 0;
		boolean processed = false;
		for (int i = 0; i < charArray.length; i++) {
			char c = charArray[i];

			if (Character.isDigit(c)) {
				buffer.append(c);
			} else if (buffer.length() != 0) {
				// if not a digit but digits in buffer, non digit has to be either w,d,h,m
				int count = Integer.parseInt(buffer.toString());
				if (c == 'w') {
					value += count * 60 * 60 * workHoursPerDay * workDaysPerWeek;
				} else if (c == 'd') {
					value += count * 60 * 60 * workHoursPerDay;
				} else if (c == 'h') {
					value += count * 60 * 60;
				} else if (c == 'm') {
					value += count * 60;
				} else {
					// if character after digits it not a valid day identifier, abort
					pos.setErrorIndex(i);
					return null;
				}
				processed = true;
				buffer.setLength(0);
			} else if (!Character.isWhitespace(c)) {
				// if character is no digit, no space and no digits where found so far, abort
				pos.setErrorIndex(i);
				return null;
			}
		}

		// if there are unprocessed digits left, it is an invalid format
		if (!processed || buffer.length() != 0) {
			pos.setErrorIndex(0);
			return null;
		}

		pos.setIndex(source.length() + 1);
		return Long.valueOf(value);
	}
}
