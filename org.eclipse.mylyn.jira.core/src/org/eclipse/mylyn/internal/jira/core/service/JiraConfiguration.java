/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author Steffen Pingel
 */
public class JiraConfiguration {

	public static final String DEFAULT_DATE_PATTERN = "dd/MMM/yy"; //$NON-NLS-1$

	public static final String DEFAULT_DATE_TIME_PATTERN = "dd/MMM/yy hh:mm a"; //$NON-NLS-1$

	public static final Locale DEFAULT_LOCALE = Locale.US;

	private String datePattern;

	private String dateTimePattern;

	private boolean compressionEnabled;

	private Locale locale;

	private String characterEncoding;

	private boolean followRedirects;

	private int workDaysPerWeek;

	private int workHoursPerDay;

	public static final int DEFAULT_WORK_DAYS_PER_WEEK = 7;

	public static final int DEFAULT_WORK_HOURS_PER_DAY = 24;

	public JiraConfiguration() {
		setDatePattern(DEFAULT_DATE_PATTERN);
		setDateTimePattern(DEFAULT_DATE_TIME_PATTERN);
		setLocale(DEFAULT_LOCALE);
		setCompressionEnabled(false);
	}

	public String getCharacterEncoding() {
		return characterEncoding;
	}

	public String getDatePattern() {
		return datePattern;
	}

	public String getDateTimePattern() {
		return dateTimePattern;
	}

	public Locale getLocale() {
		return locale;
	}

	public boolean isCompressionEnabled() {
		return compressionEnabled;
	}

	public void setCompressionEnabled(boolean compressionEnabled) {
		this.compressionEnabled = compressionEnabled;
	}

	public void setDatePattern(String dateFormat) {
		this.datePattern = dateFormat;
	}

	public void setDateTimePattern(String dateTimeFormat) {
		this.dateTimePattern = dateTimeFormat;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	public DateFormat getDateFormat() {
		return new SimpleDateFormat(getDatePattern(), getLocale());
	}

	public DateFormat getDateTimeFormat() {
		return new SimpleDateFormat(getDateTimePattern(), getLocale());
	}

	public boolean getFollowRedirects() {
		return followRedirects;
	}

	public void setFollowRedirects(boolean followRedirects) {
		this.followRedirects = followRedirects;
	}

	public int getWorkDaysPerWeek() {
		return workDaysPerWeek;
	}

	public void setWorkDaysPerWeek(int workDaysPerWeek) {
		this.workDaysPerWeek = workDaysPerWeek;
	}

	public int getWorkHoursPerDay() {
		return workHoursPerDay;
	}

	public void setWorkHoursPerDay(int workHoursPerDay) {
		this.workHoursPerDay = workHoursPerDay;
	}

}
