/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.core;

import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.State;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.tasks.core.TasksUtil;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods for dealing with Crucible
 * 
 * @author Shawn Minto
 */
public final class CrucibleUtil {

	public static final String KEY_FILTER_ID = "FilterId";

	private static final String CRUCIBLE_URL_START = "cru/";

	private static final String PREDEFINED_FILER_START = CRUCIBLE_URL_START + "?filter=";

	private CrucibleUtil() {
	}

	public static PredefinedFilter getPredefinedFilter(String filterUrl) {
		for (PredefinedFilter filter : PredefinedFilter.values()) {
			if (filter.getFilterUrl().equals(filterUrl)) {
				return filter;
			}
		}
		return null;
	}

	public static String getPermIdFromTaskId(String taskId) {
		if (!taskId.contains("%")) {
			// this means that it was already encoded
			return taskId;
		}
		return TasksUtil.decode(taskId);
	}

	public static String getTaskIdFromPermId(String permId) {
		if (permId.contains("%")) {
			// this means that it was already encoded
			return permId;
		}
		return TasksUtil.encode(permId);
	}

	public static String getPredefinedFilterWebUrl(String repositoryUrl, String filterId) {
		String url = addTrailingSlash(repositoryUrl);
		url += PREDEFINED_FILER_START + filterId;
		return url;
	}

	public static String addTrailingSlash(String repositoryUrl) {
		if (repositoryUrl.endsWith("/")) {
			return repositoryUrl;
		} else {
			return repositoryUrl + "/";
		}
	}

	public static String getReviewUrl(String repositoryUrl, String taskId) {
		// TODO handle both taskid and task key
		String url = addTrailingSlash(repositoryUrl);
		url += CRUCIBLE_URL_START + getPermIdFromTaskId(taskId);
		return url;
	}

	public static String getTaskIdFromUrl(String taskFullUrl) {
		int index = taskFullUrl.indexOf(CRUCIBLE_URL_START);
		if (index != -1 && index + CRUCIBLE_URL_START.length() < taskFullUrl.length()) {
			String permId = taskFullUrl.substring(index + CRUCIBLE_URL_START.length());
			if (permId.contains("/")) {
				// this isnt the url of the task
				return null;
			} else {
				return getTaskIdFromPermId(permId);
			}
		}
		return null;
	}

	public static String getRepositoryUrlFromUrl(String taskFullUrl) {
		int index = taskFullUrl.indexOf(CRUCIBLE_URL_START);
		if (index != -1) {
			return taskFullUrl.substring(0, index);
		}
		return null;
	}

	public static boolean isFilterDefinition(IRepositoryQuery query) {
		String filterId = query.getAttribute(KEY_FILTER_ID);
		return filterId == null || filterId.length() == 0;
	}

	public static State[] getStatesFromString(String statesString) {
		Set<State> states = new HashSet<State>();
		String[] statesArray = statesString.split(",");
		for (String stateString : statesArray) {
			if (stateString.trim().length() == 0) {
				continue;
			}
			try {
				State state = State.fromValue(stateString);
				if (state != null) {
					states.add(state);
				}
			} catch (IllegalArgumentException e) {
				StatusHandler.log(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID, e.getMessage(), e));
			}
		}
		return states.toArray(new State[0]);
	}
}
