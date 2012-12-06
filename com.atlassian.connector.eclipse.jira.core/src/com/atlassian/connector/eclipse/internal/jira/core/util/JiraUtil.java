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

package com.atlassian.connector.eclipse.internal.jira.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

import com.atlassian.connector.eclipse.internal.jira.core.IJiraConstants;
import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;
import com.atlassian.connector.eclipse.internal.jira.core.JiraFieldType;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.NamedFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import com.atlassian.connector.eclipse.internal.jira.core.service.FilterDefinitionConverter;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraLocalConfiguration;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraTimeFormat;

/**
 * @author Steffen Pingel
 * @author Jacek Jaroczynski
 */
public class JiraUtil {

	private static final String CHARACTER_ENCODING_VALIDATED = "jira.characterEncodingValidated"; //$NON-NLS-1$

	private static final String COMPRESSION_KEY = "compression"; //$NON-NLS-1$

	private static final String DATE_PATTERN_KEY = "jira.datePattern"; //$NON-NLS-1$

	private static final String DATE_TIME_PATTERN_KEY = "jira.dateTimePattern"; //$NON-NLS-1$

	private static final String FOLLOW_REDIRECTS_KEY = "jira.followRedirects"; //$NON-NLS-1$

	private static final String KEY_FILTER_CUSTOM_URL = "FilterCustomUrl"; //$NON-NLS-1$

	private static final String KEY_FILTER_ID = "FilterID"; //$NON-NLS-1$

	private static final String KEY_FILTER_NAME = "FilterName"; //$NON-NLS-1$

	private static final String KEY_FILTER_JQL = "FilterJql"; //$NON-NLS-1$

	private static final String KEY_FILTER_URL = "FilterUrl"; //$NON-NLS-1$

	private static final String REFRESH_CONFIGURATION_KEY = "refreshConfiguration"; //$NON-NLS-1$

	private static final String REPOSITORY_UPDATE_TIME_STAMP = "jira.lastIssueUpdate"; //$NON-NLS-1$

	private static final String WORK_HOURS_PER_DAY = "jira.workHoursPerDay"; //$NON-NLS-1$

	private static final String WORK_DAYS_PER_WEEK = "jira.workDaysPerWeek"; //$NON-NLS-1$

	private static final String TIME_TRACKING_SERVER_SETTINGS = "jira.timeTrackingServerSettings"; //$NON-NLS-1$

	private static final String MAX_SEARCH_RESULTS = "jira.maxSearchResults"; //$NON-NLS-1$

//	public static final int DEFAULT_MAX_SEARCH_RESULTS = TaskDataCollector.MAX_HITS;
	public static final int DEFAULT_MAX_SEARCH_RESULTS = 1000;

	private static final boolean TRACE_ENABLED = Boolean.valueOf(Platform.getDebugOption("com.atlassian.connector.eclipse.jira.core/debug/repository")); //$NON-NLS-1$

	private static final String LINKED_TASKS_AS_SUBTASKS = "jira.linkedTasksAsSubtasks"; //$NON-NLS-1$

	private static final String LOCALE_KEY = "jira.locale"; //$NON-NLS-1$

	public static String dateToString(Date date) {
		if (date == null) {
			return ""; //$NON-NLS-1$
		} else {
			return date.getTime() + ""; //$NON-NLS-1$
		}
	}

	public static String encode(String text, String encoding) {
		try {
			return URLEncoder.encode(text, encoding);
		} catch (UnsupportedEncodingException e) {
			try {
				return URLEncoder.encode(text, JiraClient.DEFAULT_CHARSET);
			} catch (UnsupportedEncodingException e1) {
				// should never happen
				StatusHandler.log(new Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN, 0, "Could not encode text \"" //$NON-NLS-1$
						+ text + "\"", e)); //$NON-NLS-1$
				return text;
			}
		}
	}

	public static boolean getAutoRefreshConfiguration(TaskRepository repository) {
		return Boolean.parseBoolean(repository.getProperty(REFRESH_CONFIGURATION_KEY));
	}

	public static boolean getCharacterEncodingValidated(TaskRepository taskRepository) {
		return Boolean.parseBoolean(taskRepository.getProperty(CHARACTER_ENCODING_VALIDATED));
	}

	public static boolean getCompression(TaskRepository taskRepository) {
		return Boolean.parseBoolean(taskRepository.getProperty(COMPRESSION_KEY));
	}

	public static FilterDefinition getFilterDefinition(TaskRepository taskRepository, JiraClient client,
			IRepositoryQuery query, boolean validate) {
		String customUrl = query.getAttribute(KEY_FILTER_CUSTOM_URL);
		if (customUrl != null && customUrl.length() > 0) {
			FilterDefinitionConverter converter = new FilterDefinitionConverter(taskRepository.getCharacterEncoding(),
					JiraUtil.getLocalConfiguration(taskRepository).getDateFormat());
			return converter.toFilter(client, customUrl, validate);
		}
		return null;
	}

	public static FilterDefinition getFilterDefinition(TaskRepository taskRepository, JiraClient client,
			IRepositoryQuery query, boolean validate, IProgressMonitor monitor) throws JiraException {
		String customUrl = query.getAttribute(KEY_FILTER_CUSTOM_URL);
		if (customUrl != null && customUrl.length() > 0) {
			FilterDefinitionConverter converter = new FilterDefinitionConverter(taskRepository.getCharacterEncoding(),
					JiraUtil.getLocalConfiguration(taskRepository).getDateFormat());
			return converter.toFilter(client, customUrl, validate, true, monitor);
		}
		return null;
	}

	private static int getInteger(TaskRepository repository, String key, int defaultValue) {
		String value = repository.getProperty(key);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				// ignore
			}
		}
		return defaultValue;
	}

	public static Date getLastUpdate(TaskRepository repository) {
		return JiraUtil.stringToDate(repository.getProperty(REPOSITORY_UPDATE_TIME_STAMP));
	}

	public static boolean getLinkedTasksAsSubtasks(TaskRepository taskRepository) {
		return Boolean.parseBoolean(taskRepository.getProperty(LINKED_TASKS_AS_SUBTASKS));
	}

	public static int getMaxSearchResults(TaskRepository repository) {
		int value = getInteger(repository, MAX_SEARCH_RESULTS, DEFAULT_MAX_SEARCH_RESULTS);
		if (value < -1) {
			return -1;
		} else if (value == 0) {
			// see bug 243849
			return DEFAULT_MAX_SEARCH_RESULTS;
		}
		return value;
	}

	public static NamedFilter getNamedFilter(IRepositoryQuery query) {
		String id = query.getAttribute(KEY_FILTER_ID);
		if (id != null) {
			NamedFilter namedFilter = new NamedFilter();
			namedFilter.setId(id);
			namedFilter.setName(query.getAttribute(KEY_FILTER_NAME));
			namedFilter.setJql(query.getAttribute(KEY_FILTER_JQL));
			namedFilter.setViewUrl(query.getAttribute(KEY_FILTER_URL));
			return namedFilter;
		}
		return null;
	}

	public static JiraFilter getQuery(TaskRepository taskRepository, JiraClient client, IRepositoryQuery query,
			boolean validate) {
		JiraFilter filter = getFilterDefinition(taskRepository, client, query, validate);
		if (filter != null) {
			return filter;
		} else {
			return getNamedFilter(query);
		}
	}

	public static JiraFilter getQuery(TaskRepository taskRepository, JiraClient client, IRepositoryQuery query,
			boolean validate, IProgressMonitor monitor) throws JiraException {
		JiraFilter filter = getFilterDefinition(taskRepository, client, query, validate, monitor);
		if (filter != null) {
			return filter;
		} else {
			return getNamedFilter(query);
		}
	}

	public static JiraTimeFormat getTimeFormat(TaskRepository taskRepository) {
		return new JiraTimeFormat(JiraUtil.getWorkDaysPerWeek(taskRepository),
				JiraUtil.getWorkHoursPerDay(taskRepository));
	}

	public static int getWorkDaysPerWeek(TaskRepository repository) {
		JiraClient client = JiraCorePlugin.getClientManager().getClient(repository.getUrl());
		return getWorkDaysPerWeek(client);
	}

	public static int getWorkDaysPerWeek(JiraClient jiraClient) {
//		if (isUseServerTimeTrackingSettings(jiraClient.getLocalConfiguration())) {
//			JiraConfiguration conf = jiraClient.getCache().getConfiguration();
//			return conf != null ? conf.getTimeTrackingDaysPerWeek() : JiraLocalConfiguration.DEFAULT_WORK_DAYS_PER_WEEK;
//		} else {
		return getWorkDaysPerWeekLocal(jiraClient.getLocalConfiguration());
//		}
	}

	public static int getWorkHoursPerDay(TaskRepository repository) {
		JiraClient client = JiraCorePlugin.getClientManager().getClient(repository.getUrl());
		return getWorkHoursPerDay(client);
	}

	public static int getWorkHoursPerDay(JiraClient jiraClient) {
//		if (isUseServerTimeTrackingSettings(jiraClient.getLocalConfiguration())) {
//			JiraConfiguration conf = jiraClient.getCache().getConfiguration();
//			return conf != null ? conf.getTimeTrackingHoursPerDay() : JiraLocalConfiguration.DEFAULT_WORK_HOURS_PER_DAY;
//		} else {
		return getWorkHoursPerDayLocal(jiraClient.getLocalConfiguration());
//		}
	}

	public static int getWorkDaysPerWeekLocal(TaskRepository repository) {
		int value = getInteger(repository, WORK_DAYS_PER_WEEK, JiraLocalConfiguration.DEFAULT_WORK_DAYS_PER_WEEK);
		return workDaysPerWeekNormalize(value);
	}

	public static int getWorkDaysPerWeekLocal(JiraLocalConfiguration conf) {
		int value = conf.getWorkDaysPerWeek();
		return workDaysPerWeekNormalize(value);
	}

	private static int workDaysPerWeekNormalize(int value) {
		if (value < 1) {
			return 1;
		}
		if (value > 7) {
			return 7;
		}

		return value;
	}

	public static int getWorkHoursPerDayLocal(TaskRepository repository) {
		int value = getInteger(repository, WORK_HOURS_PER_DAY, JiraLocalConfiguration.DEFAULT_WORK_HOURS_PER_DAY);
		return workHoursPerDayNormalize(value);
	}

	public static int getWorkHoursPerDayLocal(JiraLocalConfiguration conf) {
		int value = conf.getWorkHoursPerDay();
		return workHoursPerDayNormalize(value);
	}

	private static int workHoursPerDayNormalize(int value) {
		if (value < 1) {
			return 1;
		}
		if (value > 24) {
			return 24;
		}
		return value;
	}

	public static boolean isUseServerTimeTrackingSettings(TaskRepository repository) {
		return Boolean.parseBoolean(repository.getProperty(TIME_TRACKING_SERVER_SETTINGS));
	}

	public static boolean isUseServerTimeTrackingSettings(JiraLocalConfiguration conf) {
		return conf.isUseServerTimeTrackingSettings();
	}

	public static boolean isFilterDefinition(IRepositoryQuery query) {
		String customUrl = query.getAttribute(KEY_FILTER_CUSTOM_URL);
		return customUrl != null && customUrl.length() > 0;
	}

	public static void setAutoRefreshConfiguration(TaskRepository repository, boolean autoRefreshConfiguration) {
		repository.setProperty(REFRESH_CONFIGURATION_KEY, String.valueOf(autoRefreshConfiguration));
	}

	public static void setCharacterEncodingValidated(TaskRepository taskRepository, boolean validated) {
		taskRepository.setProperty(CHARACTER_ENCODING_VALIDATED, String.valueOf(validated));
	}

	public static void setCompression(TaskRepository taskRepository, boolean compression) {
		taskRepository.setProperty(COMPRESSION_KEY, String.valueOf(compression));
	}

	public static void setLastUpdate(TaskRepository repository, Date date) {
		repository.setProperty(REPOSITORY_UPDATE_TIME_STAMP, JiraUtil.dateToString(date));
	}

	public static void setLinkedTasksAsSubtasks(TaskRepository taskRepository, boolean linkedTasksAsSubtasks) {
		taskRepository.setProperty(LINKED_TASKS_AS_SUBTASKS, String.valueOf(linkedTasksAsSubtasks));
	}

	public static void setMaxSearchResults(TaskRepository repository, int maxSearchResults) {
		repository.setProperty(MAX_SEARCH_RESULTS, String.valueOf(maxSearchResults));
	}

	public static void setQuery(TaskRepository taskRepository, IRepositoryQuery query, JiraFilter filter) {
		if (filter instanceof NamedFilter) {
			NamedFilter namedFilter = (NamedFilter) filter;
			query.setAttribute(KEY_FILTER_ID, namedFilter.getId());
			query.setAttribute(KEY_FILTER_NAME, namedFilter.getName());
			query.setAttribute(KEY_FILTER_JQL, namedFilter.getJql());
			query.setAttribute(KEY_FILTER_URL, namedFilter.getViewUrl());
			query.setUrl(namedFilter.getViewUrl());
		} else if (filter instanceof FilterDefinition) {
			FilterDefinitionConverter converter = new FilterDefinitionConverter(taskRepository.getCharacterEncoding(),
					JiraUtil.getLocalConfiguration(taskRepository).getDateFormat());
			String url = converter.toUrl(taskRepository.getRepositoryUrl(), (FilterDefinition) filter);
			query.setAttribute(KEY_FILTER_CUSTOM_URL, url);
			query.setUrl(converter.toJqlUrl(taskRepository.getRepositoryUrl(), (FilterDefinition) filter));
		}
	}

	public static void setWorkDaysPerWeekLocal(TaskRepository repository, int workDaysPerWeek) {
		repository.setProperty(WORK_DAYS_PER_WEEK, String.valueOf(workDaysPerWeek));
	}

	public static void setWorkHoursPerDayLocal(TaskRepository repository, int workHoursPerDay) {
		repository.setProperty(WORK_HOURS_PER_DAY, String.valueOf(workHoursPerDay));
	}

	public static void setUseServerTimeTrackingSettings(TaskRepository repository, boolean selection) {
		repository.setProperty(TIME_TRACKING_SERVER_SETTINGS, String.valueOf(selection));

	}

	public static Date stringToDate(String dateString) {
		if (dateString == null || dateString.length() == 0) {
			return null;
		}
		try {
			return new Date(Long.parseLong(dateString));
		} catch (NumberFormatException nfe) {
			try {
				return new SimpleDateFormat(IJiraConstants.JIRA_DATE_FORMAT, Locale.US).parse(dateString);
			} catch (ParseException e) {
				trace(new Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN, 0, "Error while parsing date string " //$NON-NLS-1$
						+ dateString, e));
				return null;
			}
		}
	}

	public static void trace(IStatus status) {
		if (TRACE_ENABLED) {
			StatusHandler.log(status);
		}
	}

	public static JiraLocalConfiguration getLocalConfiguration(TaskRepository repository) {
		JiraLocalConfiguration configuration = new JiraLocalConfiguration();
		if (JiraUtil.getCharacterEncodingValidated(repository)) {
			configuration.setCharacterEncoding(repository.getCharacterEncoding());
		}
		configuration.setCompressionEnabled(JiraUtil.getCompression(repository));
		if (repository.getProperty(DATE_PATTERN_KEY) != null) {
			configuration.setDatePattern(repository.getProperty(DATE_PATTERN_KEY));
		}
		if (repository.getProperty(DATE_TIME_PATTERN_KEY) != null) {
			configuration.setDateTimePattern(repository.getProperty(DATE_TIME_PATTERN_KEY));
		}
		String localeString = repository.getProperty(LOCALE_KEY);
		if (localeString != null) {
			for (Locale locale : Locale.getAvailableLocales()) {
				if (locale.toString().equals(localeString)) {
					configuration.setLocale(locale);
					break;
				}
			}
		}
		if (repository.getProperty(FOLLOW_REDIRECTS_KEY) == null) {
			configuration.setFollowRedirects(true);
		} else {
			configuration.setFollowRedirects(Boolean.parseBoolean(repository.getProperty(FOLLOW_REDIRECTS_KEY)));
		}
		configuration.setWorkHoursPerDay(getWorkHoursPerDayLocal(repository));
		configuration.setWorkDaysPerWeek(getWorkDaysPerWeekLocal(repository));
		configuration.setDefaultCharacterEncoding(repository.getCharacterEncoding());
		configuration.setUseServerTimeTrackingSettings(isUseServerTimeTrackingSettings(repository));
		return configuration;
	}

	public static void setConfiguration(TaskRepository repository, JiraLocalConfiguration configuration) {
		if (JiraLocalConfiguration.DEFAULT_DATE_PATTERN.equals(configuration.getDatePattern())) {
			repository.removeProperty(DATE_PATTERN_KEY);
		} else {
			repository.setProperty(DATE_PATTERN_KEY, configuration.getDatePattern());
		}
		if (JiraLocalConfiguration.DEFAULT_DATE_TIME_PATTERN.equals(configuration.getDateTimePattern())) {
			repository.removeProperty(DATE_TIME_PATTERN_KEY);
		} else {
			repository.setProperty(DATE_TIME_PATTERN_KEY, configuration.getDateTimePattern());
		}
		if (JiraLocalConfiguration.DEFAULT_LOCALE.equals(configuration.getLocale())) {
			repository.removeProperty(LOCALE_KEY);
		} else {
			repository.setProperty(LOCALE_KEY, configuration.getLocale().toString());
		}
		repository.setProperty(FOLLOW_REDIRECTS_KEY, Boolean.toString(configuration.getFollowRedirects()));
	}

	public static boolean isCustomDateTimeAttribute(TaskAttribute attribute) {
		if (attribute.getId().startsWith(IJiraConstants.ATTRIBUTE_CUSTOM_PREFIX)) {
			String metaType = attribute.getMetaData().getValue(IJiraConstants.META_TYPE);
			if (JiraFieldType.DATETIME.getKey().equals(metaType)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isCustomDateAttribute(TaskAttribute attribute) {
		if (attribute.getId().startsWith(IJiraConstants.ATTRIBUTE_CUSTOM_PREFIX)) {
			String metaType = attribute.getMetaData().getValue(IJiraConstants.META_TYPE);
			if (JiraFieldType.DATE.getKey().equals(metaType)) {
				return true;
			}
		}
		return false;
	}

}
