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

package com.atlassian.connector.eclipse.internal.jira.core.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.atlassian.connector.eclipse.internal.jira.core.util.JiraUtil;

/**
 * @author Steffen Pingel
 */
public class JiraLocalConfiguration {

	public static final String DEFAULT_DATE_PATTERN = "dd/MMM/yy"; //$NON-NLS-1$

	public static final String DEFAULT_DATE_TIME_PATTERN = "dd/MMM/yy hh:mm a"; //$NON-NLS-1$

	public static final Locale DEFAULT_LOCALE = Locale.US;

	public static final int DEFAULT_WORK_DAYS_PER_WEEK = 7;

	public static final int DEFAULT_WORK_HOURS_PER_DAY = 24;

	private String characterEncoding;

	private boolean compressionEnabled;

	private String datePattern;

	private String dateTimePattern;

	private boolean followRedirects;

	private Locale locale;

	private int workDaysPerWeek;

	private int workHoursPerDay;

	private String defaultCharacterEncoding;

	private boolean useServerTimeTrackingSettings;

	private int maxSearchResults;

	public JiraLocalConfiguration() {
		setDatePattern(DEFAULT_DATE_PATTERN);
		setDateTimePattern(DEFAULT_DATE_TIME_PATTERN);
		setLocale(DEFAULT_LOCALE);
		setCompressionEnabled(false);
		setWorkDaysPerWeek(DEFAULT_WORK_DAYS_PER_WEEK);
		setWorkHoursPerDay(DEFAULT_WORK_HOURS_PER_DAY);
		setDefaultCharacterEncoding(JiraClient.DEFAULT_CHARSET);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		JiraLocalConfiguration other = (JiraLocalConfiguration) obj;
		if (characterEncoding == null) {
			if (other.characterEncoding != null) {
				return false;
			}
		} else if (!characterEncoding.equals(other.characterEncoding)) {
			return false;
		}
		if (compressionEnabled != other.compressionEnabled) {
			return false;
		}
		if (datePattern == null) {
			if (other.datePattern != null) {
				return false;
			}
		} else if (!datePattern.equals(other.datePattern)) {
			return false;
		}
		if (dateTimePattern == null) {
			if (other.dateTimePattern != null) {
				return false;
			}
		} else if (!dateTimePattern.equals(other.dateTimePattern)) {
			return false;
		}
		if (followRedirects != other.followRedirects) {
			return false;
		}
		if (locale == null) {
			if (other.locale != null) {
				return false;
			}
		} else if (!locale.equals(other.locale)) {
			return false;
		}
		if (workDaysPerWeek != other.workDaysPerWeek) {
			return false;
		}
		if (workHoursPerDay != other.workHoursPerDay) {
			return false;
		}
		if (useServerTimeTrackingSettings != other.useServerTimeTrackingSettings) {
			return false;
		}
		return true;
	}

	public synchronized String getCharacterEncoding() {
		return characterEncoding;
	}

	public synchronized DateFormat getDateFormat() {
		return new SimpleDateFormat(getDatePattern(), getLocale());
	}

	public synchronized String getDatePattern() {
		return datePattern;
	}

	public synchronized DateFormat getDateTimeFormat() {
		return new SimpleDateFormat(getDateTimePattern(), getLocale());
	}

	public synchronized String getDateTimePattern() {
		return dateTimePattern;
	}

	public synchronized String getDefaultCharacterEncoding() {
		return defaultCharacterEncoding;
	}

	public synchronized boolean getFollowRedirects() {
		return followRedirects;
	}

	public synchronized Locale getLocale() {
		return locale;
	}

	public synchronized int getWorkDaysPerWeek() {
		return workDaysPerWeek;
	}

	public synchronized int getWorkHoursPerDay() {
		return workHoursPerDay;
	}

	public synchronized boolean isCompressionEnabled() {
		return compressionEnabled;
	}

	public boolean isUseServerTimeTrackingSettings() {
		return useServerTimeTrackingSettings;
	}

	@Override
	public synchronized int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((characterEncoding == null) ? 0 : characterEncoding.hashCode());
		result = prime * result + (compressionEnabled ? 1231 : 1237);
		result = prime * result + ((datePattern == null) ? 0 : datePattern.hashCode());
		result = prime * result + ((dateTimePattern == null) ? 0 : dateTimePattern.hashCode());
		result = prime * result + (followRedirects ? 1231 : 1237);
		result = prime * result + ((locale == null) ? 0 : locale.hashCode());
		result = prime * result + workDaysPerWeek;
		result = prime * result + workHoursPerDay;
		return result;
	}

	public synchronized void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	public synchronized void setCompressionEnabled(boolean compressionEnabled) {
		this.compressionEnabled = compressionEnabled;
	}

	public synchronized void setDatePattern(String dateFormat) {
		this.datePattern = dateFormat;
	}

	public synchronized void setDateTimePattern(String dateTimeFormat) {
		this.dateTimePattern = dateTimeFormat;
	}

	public synchronized void setDefaultCharacterEncoding(String defaultCharacterEncoding) {
		this.defaultCharacterEncoding = defaultCharacterEncoding;
	}

	public synchronized void setFollowRedirects(boolean followRedirects) {
		this.followRedirects = followRedirects;
	}

	public synchronized void setLocale(Locale locale) {
		this.locale = locale;
	}

	public synchronized void setWorkDaysPerWeek(int workDaysPerWeek) {
		this.workDaysPerWeek = workDaysPerWeek;
	}

	public synchronized void setWorkHoursPerDay(int workHoursPerDay) {
		this.workHoursPerDay = workHoursPerDay;
	}

	public void setUseServerTimeTrackingSettings(boolean useServerTimeTrackingSettings) {
		this.useServerTimeTrackingSettings = useServerTimeTrackingSettings;
	}

	public void setMaxSearchResults(int maxSearchResults) {
		if (maxSearchResults <= 0) {
			this.maxSearchResults = JiraUtil.DEFAULT_MAX_SEARCH_RESULTS;
		} else {
			this.maxSearchResults = maxSearchResults;
		}
	}

	public int getMaxSearchResults() {
		return maxSearchResults;
	}
}
