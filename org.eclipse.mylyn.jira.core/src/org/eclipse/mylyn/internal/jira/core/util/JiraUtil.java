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
import org.eclipse.mylyn.internal.jira.core.JiraAttributeFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * @author Steffen Pingel
 */
public class JiraUtil {

	private static final boolean TRACE_ENABLED = Boolean.valueOf(Platform.getDebugOption("org.eclipse.mylyn.jira.core/general"));

	private static final String REPOSITORY_UPDATE_TIME_STAMP = "jira.lastIssueUpdate";

	private static final String CHARACTER_ENCODING_VALIDATED = "jira.characterEncodingValidated";

	private static final String COMPRESSION_KEY = "compression";

	private static final String REFRESH_CONFIGURATION_KEY = "refreshConfiguration";

	public static void setLastUpdate(TaskRepository repository, Date date) {
		repository.setProperty(REPOSITORY_UPDATE_TIME_STAMP, JiraUtil.dateToString(date));
	}

	public static Date getLastUpdate(TaskRepository repository) {
		return JiraUtil.stringToDate(repository.getProperty(REPOSITORY_UPDATE_TIME_STAMP));
	}

	public static void setCompression(TaskRepository taskRepository, boolean compression) {
		taskRepository.setProperty(COMPRESSION_KEY, String.valueOf(compression));
	}

	public static boolean getCompression(TaskRepository taskRepository) {
		return Boolean.parseBoolean(taskRepository.getProperty(COMPRESSION_KEY));
	}

	public static void setCharacterEncodingValidated(TaskRepository taskRepository, boolean validated) {
		taskRepository.setProperty(CHARACTER_ENCODING_VALIDATED, String.valueOf(validated));
	}

	public static boolean getCharacterEncodingValidated(TaskRepository taskRepository) {
		return Boolean.parseBoolean(taskRepository.getProperty(CHARACTER_ENCODING_VALIDATED));
	}

	public static String dateToString(Date date) {
		if (date == null) {
			return "";
		} else {
			return date.getTime() + "";
		}
	}

	public static Date stringToDate(String dateString) {
		if (dateString == null || dateString.length() == 0) {
			return null;
		}
		try {
			return new Date(Long.parseLong(dateString));
		} catch (NumberFormatException nfe) {
			try {
				return new SimpleDateFormat(JiraAttributeFactory.JIRA_DATE_FORMAT, Locale.US).parse(dateString);
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

	public static boolean getAutoRefreshConfiguration(TaskRepository repository) {
		return Boolean.parseBoolean(repository.getProperty(REFRESH_CONFIGURATION_KEY));
	}

	public static void setAutoRefreshConfiguration(TaskRepository repository, boolean autoRefreshConfiguration) {
		repository.setProperty(REFRESH_CONFIGURATION_KEY, String.valueOf(autoRefreshConfiguration));
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

}
