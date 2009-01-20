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

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilterBean;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.State;

import org.apache.commons.lang.StringUtils;
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
		url += CrucibleConstants.PREDEFINED_FILER_START + filterId;
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
		url += CrucibleConstants.CRUCIBLE_URL_START + getPermIdFromTaskId(taskId);
		return url;
	}

	public static String getTaskIdFromUrl(String taskFullUrl) {
		int index = taskFullUrl.indexOf(CrucibleConstants.CRUCIBLE_URL_START);
		if (index != -1 && index + CrucibleConstants.CRUCIBLE_URL_START.length() < taskFullUrl.length()) {
			String permId = taskFullUrl.substring(index + CrucibleConstants.CRUCIBLE_URL_START.length());
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
		int index = taskFullUrl.indexOf(CrucibleConstants.CRUCIBLE_URL_START);
		if (index != -1) {
			return taskFullUrl.substring(0, index);
		}
		return null;
	}

	public static boolean isFilterDefinition(IRepositoryQuery query) {
		String filterId = query.getAttribute(CrucibleConstants.KEY_FILTER_ID);
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

	public static CustomFilterBean createCustomFilterFromQuery(final IRepositoryQuery query) {
		String allComplete = query.getAttribute(CustomFilter.ALLCOMPLETE);
		String author = query.getAttribute(CustomFilter.AUTHOR);
		String complete = query.getAttribute(CustomFilter.COMPLETE);
		String creator = query.getAttribute(CustomFilter.CREATOR);
		String moderator = query.getAttribute(CustomFilter.MODERATOR);
		String orRoles = query.getAttribute(CustomFilter.ORROLES);
		String project = query.getAttribute(CustomFilter.PROJECT);
		String reviewer = query.getAttribute(CustomFilter.REVIEWER);
		String states = query.getAttribute(CustomFilter.STATES);

		CustomFilterBean customFilter = new CustomFilterBean();

		if (allComplete != null && allComplete.length() > 0) {
			if (!Boolean.parseBoolean(allComplete)) {
				customFilter.setAllReviewersComplete(null);
			} else {
				customFilter.setAllReviewersComplete(Boolean.parseBoolean(allComplete));
			}
		} else {
			customFilter.setAllReviewersComplete(null);
		}
		customFilter.setAuthor(author);
		if (complete != null && complete.length() > 0) {
			if (!Boolean.parseBoolean(complete)) {
				customFilter.setComplete(null);
			} else {
				customFilter.setComplete(Boolean.parseBoolean(complete));
			}
		} else {
			customFilter.setComplete(null);
		}
		customFilter.setCreator(creator);
		customFilter.setModerator(moderator);
		customFilter.setOrRoles(Boolean.parseBoolean(orRoles));
		customFilter.setProjectKey(project);
		customFilter.setReviewer(reviewer);
		customFilter.setState(getStatesFromString(states));
		return customFilter;
	}

	public static String createFilterWebUrl(String repositoryUrl, IRepositoryQuery query) {

		String allComplete = query.getAttribute(CustomFilter.ALLCOMPLETE);
		String author = query.getAttribute(CustomFilter.AUTHOR);
		String complete = query.getAttribute(CustomFilter.COMPLETE);
		String creator = query.getAttribute(CustomFilter.CREATOR);
		String moderator = query.getAttribute(CustomFilter.MODERATOR);
		String orRoles = query.getAttribute(CustomFilter.ORROLES);
		String project = query.getAttribute(CustomFilter.PROJECT);
		String reviewer = query.getAttribute(CustomFilter.REVIEWER);
		String states = query.getAttribute(CustomFilter.STATES);

		StringBuilder url = new StringBuilder(addTrailingSlash(repositoryUrl) + CrucibleConstants.CUSTOM_FILER_START);

		addQueryParam(CustomFilter.AUTHOR, author, url);
		addQueryParam(CustomFilter.CREATOR, creator, url);
		addQueryParam(CustomFilter.MODERATOR, moderator, url);
		addQueryParam(CustomFilter.REVIEWER, reviewer, url);
		addQueryParam(CustomFilter.PROJECT, project, url);
		for (State state : getStatesFromString(states)) {
			addQueryParam("state", state.value(), url);
		}

		if (complete != null && complete.length() > 0) {
			addQueryParam(CustomFilter.COMPLETE, complete, url);
		}

		if (orRoles != null && orRoles.length() > 0) {
			addQueryParam(CustomFilter.ORROLES, orRoles, url);
		}

		if (allComplete != null && allComplete.length() > 0) {
			addQueryParam(CustomFilter.ALLCOMPLETE, allComplete, url);
		}

		return url.toString();
	}

	private static void addQueryParam(String name, String value, StringBuilder builder) {
		if (!StringUtils.isEmpty(value)) {
			if (builder.length() > 0) {
				builder.append("&");
			}
			builder.append(name).append("=").append(value);
		}
	}

	public static String getTaskIdFromReview(Review review) {
		String key = review.getPermId().getId();
		return CrucibleUtil.getTaskIdFromPermId(key);
	}

	public static boolean isPartialReview(Review review) {
		try {
			review.getFiles();
		} catch (ValueNotYetInitialized e) {
	
			return true;
		}
		return false;
	}
}
