/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jacek Jaroczynski
 */
public class WdhmUtil {

	private static final String REGEX = "^\\s*-?\\s*(\\d+[wW])?\\s*(\\d+[dD])?\\s*(\\d+[hH])?\\s*(\\d+[mM])?\\s*$"; //$NON-NLS-1$

	private static final Pattern p = Pattern.compile(REGEX);

	public static final int MILLIS_PER_SECOND = 1000;

	public static final int SECONDS_PER_MINUTE = 60;

	public static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * SECONDS_PER_MINUTE;

	public static final int MINUTES_PER_HOUR = 60;

	public static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;

	public static final int HOURS_PER_DAY = 24;

	public static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;

	public static final int DAYS_PER_WEEK = 7;

	public static final int SECONDS_PER_WEEK = SECONDS_PER_DAY * DAYS_PER_WEEK;

	/**
	 * Validates text against w*d*h*m* pattern. It accepts also nulls and empty strings.
	 * 
	 * @param text
	 * @return
	 */
	public static boolean isValid(String text) {
		return text == null || text.length() == 0 || isCorrect(text);
	}

	private static boolean isCorrect(String text) {
		Matcher m = p.matcher(text);
		return m.matches();
	}

	/**
	 * Translate milliseconds to w*d*h*m* format.
	 * 
	 * @param milisSpent
	 * @return
	 */
	public static String generateJiraLogTimeString(final long milisSpent) {

		// TODO jj test it

		final long secondsSpent = milisSpent / MILLIS_PER_SECOND;

		StringBuilder timeLog = new StringBuilder();

		long remainingTime = 0;

		long weeks = secondsSpent / SECONDS_PER_WEEK;
		remainingTime = secondsSpent - weeks * SECONDS_PER_WEEK;

		long days = remainingTime / SECONDS_PER_DAY;
		remainingTime = remainingTime - days * SECONDS_PER_DAY;

		long hours = remainingTime / SECONDS_PER_HOUR;
		remainingTime = remainingTime - hours * SECONDS_PER_HOUR;

		long minutes = remainingTime / SECONDS_PER_MINUTE;

		if (weeks > 0) {
			timeLog.append(weeks).append("w"); //$NON-NLS-1$
		}

		if (days > 0) {
			timeLog.append(days).append("d"); //$NON-NLS-1$
		}

		if (hours > 0) {
			timeLog.append(hours).append("h"); //$NON-NLS-1$
		}

		if (minutes > 0) {
			timeLog.append(minutes).append("m"); //$NON-NLS-1$
		}

		return timeLog.toString();
	}
}
