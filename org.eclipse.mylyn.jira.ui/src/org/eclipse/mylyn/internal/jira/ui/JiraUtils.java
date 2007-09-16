/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.tasks.core.TaskRepository;

public class JiraUtils {

	private static final boolean TRACE_ENABLED = Boolean.valueOf(Platform.getDebugOption("org.eclipse.mylyn.internal.jira.ui/general"));

	private static final String REPOSITORY_UPDATE_TIME_STAMP = "jira.lastIssueUpdate";
	
	private static final String CHARACTER_ENCODING_VALIDATED = "jira.characterEncodingValidated";

	private static final String COMPRESSION_KEY = "compression";

	public static void setLastUpdate(TaskRepository repository, Date date) {
		repository.setProperty(REPOSITORY_UPDATE_TIME_STAMP, JiraUtils.dateToString(date));
	}

	public static Date getLastUpdate(TaskRepository repository) {
		return JiraUtils.stringToDate(repository.getProperty(REPOSITORY_UPDATE_TIME_STAMP));
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
			return new SimpleDateFormat(JiraAttributeFactory.JIRA_DATE_FORMAT, Locale.US).format(date);
		}
	}

	public static Date stringToDate(String dateString) {
		if (dateString == null || dateString.length() == 0) {
			return null;
		}
		try {
			return new SimpleDateFormat(JiraAttributeFactory.JIRA_DATE_FORMAT, Locale.US).parse(dateString);
		} catch (ParseException e) {
			trace(new Status(IStatus.WARNING, JiraUiPlugin.PLUGIN_ID, 0, "Error while parsing date string "
					+ dateString, e));
			return null;
		}
	}

	public static void trace(IStatus status) {
		if (TRACE_ENABLED) {
			JiraUiPlugin.getDefault().getLog().log(status);
		}
	}

}
