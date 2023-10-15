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

package me.glindholm.connector.eclipse.internal.jira.core.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

import me.glindholm.connector.eclipse.internal.jira.core.util.JiraUtil;

/**
 * @author Steffen Pingel
 */
public class JiraLocalConfiguration {

    public static final String DEFAULT_DATE_PATTERN = "yyyy/MMM/dd"; //$NON-NLS-1$

    public static final String DEFAULT_DATE_TIME_PATTERN = "yyyy/MMM/dd HH:mm"; //$NON-NLS-1$

    public static final Locale DEFAULT_LOCALE = Locale.getDefault();

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

    private int searchResultsTimeout;

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
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final JiraLocalConfiguration other = (JiraLocalConfiguration) obj;
        if (!Objects.equals(characterEncoding, other.characterEncoding) || (compressionEnabled != other.compressionEnabled) || !Objects.equals(datePattern, other.datePattern) || !Objects.equals(dateTimePattern, other.dateTimePattern)) {
            return false;
        }
        if (followRedirects != other.followRedirects) {
            return false;
        }
        if (!Objects.equals(locale, other.locale)) {
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
        if (maxSearchResults != other.maxSearchResults) {
            return false;
        }

        if (searchResultsTimeout != other.searchResultsTimeout) {
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
        return Objects.hash(characterEncoding, compressionEnabled, datePattern, dateTimePattern, followRedirects, locale, workDaysPerWeek, workHoursPerDay,
                maxSearchResults, searchResultsTimeout);
    }

    public synchronized void setCharacterEncoding(final String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    public synchronized void setCompressionEnabled(final boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }

    public synchronized void setDatePattern(final String dateFormat) {
        datePattern = dateFormat;
    }

    public synchronized void setDateTimePattern(final String dateTimeFormat) {
        dateTimePattern = dateTimeFormat;
    }

    public synchronized void setDefaultCharacterEncoding(final String defaultCharacterEncoding) {
        this.defaultCharacterEncoding = defaultCharacterEncoding;
    }

    public synchronized void setFollowRedirects(final boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    public synchronized void setLocale(final Locale locale) {
        this.locale = locale;
    }

    public synchronized void setWorkDaysPerWeek(final int workDaysPerWeek) {
        this.workDaysPerWeek = workDaysPerWeek;
    }

    public synchronized void setWorkHoursPerDay(final int workHoursPerDay) {
        this.workHoursPerDay = workHoursPerDay;
    }

    public void setUseServerTimeTrackingSettings(final boolean useServerTimeTrackingSettings) {
        this.useServerTimeTrackingSettings = useServerTimeTrackingSettings;
    }

    public synchronized void setMaxSearchResults(final int maxSearchResults) {
        if (maxSearchResults <= 0) {
            this.maxSearchResults = JiraUtil.DEFAULT_MAX_SEARCH_RESULTS;
        } else {
            this.maxSearchResults = maxSearchResults;
        }
    }

    public synchronized int getMaxSearchResults() {
        return maxSearchResults;
    }

    public synchronized void setSearchResultsTimeout(final int searchResultsTimeout) {
        if (searchResultsTimeout < 0) {
            this.searchResultsTimeout = JiraUtil.DEFAULT_SEARCH_RESULT_TIMEOUT;
        } else {
            this.searchResultsTimeout = searchResultsTimeout;
        }
    }

    public synchronized int getSearchResultsTimeout() {
        return searchResultsTimeout;
    }
}
