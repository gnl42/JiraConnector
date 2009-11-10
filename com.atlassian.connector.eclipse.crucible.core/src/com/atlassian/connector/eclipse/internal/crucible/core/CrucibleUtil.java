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

package com.atlassian.connector.eclipse.internal.crucible.core;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CustomField;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilterBean;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.util.MiscUtil;

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

	private static final int FALSE_HASH_MAGIC = 1237;

	private static final int TRUE_HASH_MAGIC = 1231;

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

	public static int createHash(Review review) {

		try {
			final int prime = 31;
			int result = 1;

			result = prime * result + (review.isAllowReviewerToJoin() ? TRUE_HASH_MAGIC : FALSE_HASH_MAGIC);
			result = prime * result + ((review.getAuthor() == null) ? 0 : review.getAuthor().getUsername().hashCode());
			result = prime * result + ((review.getCloseDate() == null) ? 0 : review.getCloseDate().hashCode());
			result = prime * result + ((review.getCreateDate() == null) ? 0 : review.getCreateDate().hashCode());
			result = prime * result
					+ ((review.getCreator() == null) ? 0 : review.getCreator().getUsername().hashCode());
			result = prime * result + ((review.getProjectKey() == null) ? 0 : review.getProjectKey().hashCode());
			result = prime * result + ((review.getDescription() == null) ? 0 : review.getDescription().hashCode());

			int miniResult = 0;
			for (CrucibleFileInfo file : review.getFiles()) {
				miniResult += ((file.getFileDescriptor() == null) ? 0 : file.getFileDescriptor().getUrl().hashCode());
				for (VersionedComment comment : file.getVersionedComments()) {
					miniResult = createHashForVersionedComment(miniResult, comment);
				}
			}
			result = prime * result + miniResult;

			miniResult = 0;
			for (GeneralComment comment : review.getGeneralComments()) {
				miniResult = createHashForGeneralComment(miniResult, comment);
			}
			result = prime * result + miniResult;

			result = prime * result
					+ ((review.getModerator() == null) ? 0 : review.getModerator().getUsername().hashCode());
			result = prime * result + ((review.getName() == null) ? 0 : review.getName().hashCode());
			result = prime * result
					+ ((review.getParentReview() == null) ? 0 : review.getParentReview().getId().hashCode());
			result = prime * result + ((review.getPermId() == null) ? 0 : review.getPermId().getId().hashCode());
			result = prime * result + ((review.getProjectKey() == null) ? 0 : review.getProjectKey().hashCode());
			result = prime * result + ((review.getRepoName() == null) ? 0 : review.getRepoName().hashCode());

			miniResult = 0;
			for (Reviewer reviewer : review.getReviewers()) {
				miniResult += (reviewer.getUsername().hashCode());
				miniResult += (reviewer.isCompleted() ? TRUE_HASH_MAGIC : FALSE_HASH_MAGIC);
			}
			result = prime * result + miniResult;

			result = prime * result + ((review.getState() == null) ? 0 : review.getState().name().hashCode());
			result = prime * result + ((review.getSummary() == null) ? 0 : review.getSummary().hashCode());

			return result;
		} catch (ValueNotYetInitialized e) {
			//ignore
		}

		return -1;

	}

	private static int createHashForGeneralComment(int result, GeneralComment comment) {

		result += (comment.isDraft() ? TRUE_HASH_MAGIC : FALSE_HASH_MAGIC);
		result += ((comment.getMessage() == null) ? 0 : comment.getMessage().hashCode());
		result += ((comment.getAuthor() == null) ? 0 : comment.getAuthor().getUsername().hashCode());
		result += ((comment.getCreateDate() == null) ? 0 : comment.getCreateDate().hashCode());
		result += ((comment.getPermId() == null) ? 0 : comment.getPermId().getId().hashCode());

		for (CustomField customValue : comment.getCustomFields().values()) {
			result += ((customValue == null) ? 0 : customValue.getValue().hashCode());
			result += ((customValue == null) ? 0 : customValue.getConfigVersion());
		}

		for (GeneralComment reply : comment.getReplies2()) {
			result = createHashForGeneralComment(result, reply);
		}

		return result;
	}

	private static int createHashForVersionedComment(int result, VersionedComment comment) {

		result += comment.getFromEndLine();
		result += comment.getFromStartLine();
		result += comment.getToEndLine();
		result += comment.getToStartLine();
		result += comment.getLineRanges() != null ? comment.getLineRanges().hashCode() : 0;
		result += (comment.isDraft() ? TRUE_HASH_MAGIC : FALSE_HASH_MAGIC);
		result += ((comment.getMessage() == null) ? 0 : comment.getMessage().hashCode());
		result += ((comment.getAuthor() == null) ? 0 : comment.getAuthor().getUsername().hashCode());
		result += ((comment.getCreateDate() == null) ? 0 : comment.getCreateDate().hashCode());
		result += ((comment.getPermId() == null) ? 0 : comment.getPermId().getId().hashCode());

		for (CustomField customValue : comment.getCustomFields().values()) {
			result += ((customValue == null) ? 0 : customValue.getValue().hashCode());
			result += ((customValue == null) ? 0 : customValue.getConfigVersion());
		}

		for (VersionedComment reply : comment.getReplies2()) {
			result = createHashForVersionedComment(result, reply);
		}

		return result;
	}

	public static boolean canAddCommentToReview(Review review) {
		if (review != null) {
			try {
				return review.getActions().contains(CrucibleAction.COMMENT);
			} catch (ValueNotYetInitialized e) {
				StatusHandler.log(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID, e.getMessage(), e));
			}
		}
		return false;
	}

	public static boolean isCompleted(Review review) {
		State state = review.getState();
		return state == State.ABANDONED || state == State.CLOSED || state == State.DEAD || state == State.REJECTED;
	}

	public static boolean isUserCompleted(String userName, Review review) {
		try {
			for (Reviewer reviewer : review.getReviewers()) {
				if (reviewer.getUsername().equals(userName)) {
					return reviewer.isCompleted();
				}
			}
		} catch (ValueNotYetInitialized e) {
			// ignore
		}
		return false;
	}

	// TODO add a param for whether it should be a deep comaparison?
	public static boolean areVersionedCommentsDeepEquals(VersionedComment c1, VersionedComment c2) {
		if (c1 == c2) {
			return true;
		}

		if (!areCommentsEqual(c1, c2)) {
			return false;
		}

		if (!MiscUtil.isEqual(c1.getLineRanges(), c2.getLineRanges())) {
			return false;
		}

		if (c1.getFromEndLine() != c2.getFromEndLine()) {
			return false;
		}
		if (c1.isFromLineInfo() != c2.isFromLineInfo()) {
			return false;
		}
		if (c1.getFromStartLine() != c2.getFromStartLine()) {
			return false;
		}
		if (c1.getToEndLine() != c2.getToEndLine()) {
			return false;
		}
		if (c1.isToLineInfo() != c2.isToLineInfo()) {
			return false;
		}
		if (c1.getToStartLine() != c2.getToStartLine()) {
			return false;
		}
		if (c1.getReplies() != null ? !c1.getReplies().equals(c2.getReplies()) : c2.getReplies() != null) {
			return false;
		}

		if (c1.getReplies().size() != c2.getReplies().size()) {
			return false;
		}

		for (VersionedComment vc1 : c1.getReplies2()) {
			boolean found = false;
			for (VersionedComment vc2 : c2.getReplies2()) {
				if (vc1.getPermId() == vc2.getPermId() && areVersionedCommentsDeepEquals(vc1, vc2)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}

		return true;
	}

	private static boolean areCommentsEqual(Comment c1, Comment c2) {
		if (c1.isDefectApproved() != c2.isDefectApproved()) {
			return false;
		}
		if (c1.isDefectRaised() != c2.isDefectRaised()) {
			return false;
		}
		if (c1.isDeleted() != c2.isDeleted()) {
			return false;
		}
		if (c1.isDraft() != c2.isDraft()) {
			return false;
		}
		if (c1.isReply() != c2.isReply()) {
			return false;
		}
		if (c1.getAuthor() != null ? !c1.getAuthor().equals(c2.getAuthor()) : c2.getAuthor() != null) {
			return false;
		}
		if (c1.getCreateDate() != null ? !c1.getCreateDate().equals(c2.getCreateDate()) : c2.getCreateDate() != null) {
			return false;
		}
		if (c1.getCustomFields() != null ? !c1.getCustomFields().equals(c2.getCustomFields())
				: c2.getCustomFields() != null) {
			return false;
		}
		if (c1.getMessage() != null ? !c1.getMessage().equals(c2.getMessage()) : c2.getMessage() != null) {
			return false;
		}
		if (c1.getPermId() != null ? !c1.getPermId().equals(c2.getPermId()) : c2.getPermId() != null) {
			return false;
		}
		return true;
	}

	// TODO add a param for whether it should be a deep comaparison?
	public static boolean areCrucibleFilesDeepEqual(CrucibleFileInfo file, CrucibleFileInfo file2) {
		if (file.getPermId() != null ? file.getPermId().getId().equals(file2.getPermId().getId())
				: file2.getPermId() != null) {
			return false;
		}

		if (file.getNumberOfComments() != file2.getNumberOfComments()) {
			return false;
		}
		for (VersionedComment comment : file.getVersionedComments()) {
			boolean found = false;
			for (VersionedComment comment2 : file2.getVersionedComments()) {
				if (CrucibleUtil.areVersionedCommentsDeepEquals(comment, comment2)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}

		return true;
	}

	// TODO add a param for whether it should be a deep comaparison?
	public static boolean areGeneralCommentsDeepEquals(GeneralComment c1, GeneralComment c2) {
		if (c1 == c2) {
			return true;
		}

		if (!areCommentsEqual(c1, c2)) {
			return false;
		}

		if (c1.getReplies() != null ? !c1.getReplies().equals(c2.getReplies()) : c2.getReplies() != null) {
			return false;
		}

		if (c1.getReplies().size() != c2.getReplies().size()) {
			return false;
		}

		for (GeneralComment vc1 : c1.getReplies2()) {
			boolean found = false;
			for (GeneralComment vc2 : c2.getReplies2()) {
				if (vc1.getPermId() == vc2.getPermId() && areGeneralCommentsDeepEquals(vc1, vc2)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}

		return true;
	}

}
