/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.jira.core.IJiraConstants;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraFieldType;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.JiraTimeFormat;
import org.eclipse.mylyn.internal.jira.core.model.JiraConfiguration;
import org.eclipse.mylyn.internal.jira.core.model.JiraFilter;
import org.eclipse.mylyn.internal.jira.core.model.NamedFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;

/**
 * @author Steffen Pingel
 */
public class JiraUtil {

	private static final String CHARACTER_ENCODING_VALIDATED = "jira.characterEncodingValidated";

	private static final String COMPRESSION_KEY = "compression";

	private static final String DATE_PATTERN_KEY = "jira.datePattern";

	private static final String DATE_TIME_PATTERN_KEY = "jira.dateTimePattern";

	private static final String KEY_FILTER_CUSTOM_URL = "FilterCustomUrl";

	private static final String KEY_FILTER_ID = "FilterID";

	private static final String KEY_FILTER_NAME = "FilterName";

	private static final String REFRESH_CONFIGURATION_KEY = "refreshConfiguration";

	private static final String REPOSITORY_UPDATE_TIME_STAMP = "jira.lastIssueUpdate";

	private static final String COMPLETED_BASED_ON_RESOLUTION = "jira.completedBasedOnResolution";

	private static final String WORK_HOURS_PER_DAY = "jira.workHoursPerDay";

	private static final String WORK_DAYS_PER_WEEK = "jira.workDaysPerWeek";

	private static final String MAX_SEARCH_RESULTS = "jira.maxSearchResults";

	public static final int DEFAULT_MAX_SEARCH_RESULTS = TaskDataCollector.MAX_HITS;

	private static final boolean TRACE_ENABLED = Boolean.valueOf(Platform.getDebugOption("org.eclipse.mylyn.jira.core/general"));

	private static final String LINKED_TASKS_AS_SUBTASKS = "jira.linkedTasksAsSubtasks";

	private static final String LOCALE_KEY = "jira.locale";

	public static String dateToString(Date date) {
		if (date == null) {
			return "";
		} else {
			return date.getTime() + "";
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
				StatusHandler.log(new Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN, 0, "Could not encode text \""
						+ text + "\"", e));
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

	public static boolean getCompletedBasedOnResolution(TaskRepository taskRepository) {
		return Boolean.parseBoolean(taskRepository.getProperty(COMPLETED_BASED_ON_RESOLUTION));
	}

	public static boolean getCompression(TaskRepository taskRepository) {
		return Boolean.parseBoolean(taskRepository.getProperty(COMPRESSION_KEY));
	}

	public static FilterDefinition getFilterDefinition(TaskRepository taskRepository, JiraClient client,
			IRepositoryQuery query, boolean validate) {
		String customUrl = query.getAttribute(KEY_FILTER_CUSTOM_URL);
		if (customUrl != null && customUrl.length() > 0) {
			FilterDefinitionConverter converter = new FilterDefinitionConverter(taskRepository.getCharacterEncoding());
			return converter.toFilter(client, customUrl, validate);
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
		if (value <= 0) {
			return -1;
		}
		return value;
	}

	public static NamedFilter getNamedFilter(IRepositoryQuery query) {
		String id = query.getAttribute(KEY_FILTER_ID);
		if (id != null) {
			NamedFilter namedFilter = new NamedFilter();
			namedFilter.setId(id);
			namedFilter.setName(query.getAttribute(KEY_FILTER_NAME));
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

	public static JiraTimeFormat getTimeFormat(TaskRepository taskRepository) {
		return new JiraTimeFormat(JiraUtil.getWorkDaysPerWeek(taskRepository),
				JiraUtil.getWorkHoursPerDay(taskRepository));
	}

	public static int getWorkDaysPerWeek(TaskRepository repository) {
		int value = getInteger(repository, WORK_DAYS_PER_WEEK, JiraTimeFormat.DEFAULT_WORK_DAYS_PER_WEEK);
		if (value < 1) {
			return 1;
		}
		if (value > 7) {
			return 7;
		}
		return value;
	}

	public static int getWorkHoursPerDay(TaskRepository repository) {
		int value = getInteger(repository, WORK_HOURS_PER_DAY, JiraTimeFormat.DEFAULT_WORK_HOURS_PER_DAY);
		if (value < 1) {
			return 1;
		}
		if (value > 24) {
			return 24;
		}
		return value;
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

	public static void setCompletedBasedOnResolution(TaskRepository taskRepository, boolean completion) {
		taskRepository.setProperty(COMPLETED_BASED_ON_RESOLUTION, String.valueOf(completion));
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
			query.setUrl(taskRepository.getRepositoryUrl() + JiraRepositoryConnector.FILTER_URL_PREFIX + "&requestId="
					+ namedFilter.getId());
		} else if (filter instanceof FilterDefinition) {
			FilterDefinitionConverter converter = new FilterDefinitionConverter(taskRepository.getCharacterEncoding());
			String url = converter.toUrl(taskRepository.getRepositoryUrl(), (FilterDefinition) filter);
			query.setAttribute(KEY_FILTER_CUSTOM_URL, url);
			query.setUrl(url);
		}
	}

	public static void setWorkDaysPerWeek(TaskRepository repository, int workDaysPerWeek) {
		repository.setProperty(WORK_DAYS_PER_WEEK, String.valueOf(workDaysPerWeek));
	}

	public static void setWorkHoursPerDay(TaskRepository repository, int workHoursPerDay) {
		repository.setProperty(WORK_HOURS_PER_DAY, String.valueOf(workHoursPerDay));
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
				trace(new Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN, 0, "Error while parsing date string "
						+ dateString, e));
				return null;
			}
		}
	}

	public static void trace(IStatus status) {
		if (TRACE_ENABLED) {
			JiraCorePlugin.getDefault().getLog().log(status);
		}
	}

	public static JiraConfiguration getConfiguration(TaskRepository repository) {
		JiraConfiguration configuration = new JiraConfiguration();
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
		return configuration;
	}

	public static void setConfiguration(TaskRepository repository, JiraConfiguration configuration) {
		if (JiraConfiguration.DEFAULT_DATE_PATTERN.equals(configuration.getDatePattern())) {
			repository.removeProperty(DATE_PATTERN_KEY);
		} else {
			repository.setProperty(DATE_PATTERN_KEY, configuration.getDatePattern());
		}
		if (JiraConfiguration.DEFAULT_DATE_TIME_PATTERN.equals(configuration.getDateTimePattern())) {
			repository.removeProperty(DATE_TIME_PATTERN_KEY);
		} else {
			repository.setProperty(DATE_TIME_PATTERN_KEY, configuration.getDateTimePattern());
		}
		if (JiraConfiguration.DEFAULT_LOCALE.equals(configuration.getLocale())) {
			repository.removeProperty(LOCALE_KEY);
		} else {
			repository.setProperty(LOCALE_KEY, configuration.getLocale().toString());
		}
	}

	public static boolean isCustomDateTimeAttribute(TaskAttribute attribute) {
		if (attribute.getId().startsWith(IJiraConstants.ATTRIBUTE_CUSTOM_PREFIX)) {
			String metaType = attribute.getMetaData().getValue(IJiraConstants.META_TYPE);
			if (JiraFieldType.DATETIME.getKey().equals(metaType) || JiraFieldType.DATE.getKey().equals(metaType)) {
				return true;
			}
		}
		return false;
	}

}
