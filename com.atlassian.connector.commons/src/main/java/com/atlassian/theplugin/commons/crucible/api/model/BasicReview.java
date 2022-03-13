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

package com.atlassian.theplugin.commons.crucible.api.model;

import com.atlassian.theplugin.commons.util.MiscUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * Contains almost all review data which can be transferred quite cheaply by Crucible for (e.g. while doing queries returning
 * many reviews).
 *
 * The only thing which is not returned (as of Crucible 1.6.x) are files ({@link CrucibleFileInfo}).
 *
 * @author wseliga
 */
public class BasicReview {
	private Set<Reviewer> reviewers;
	private Set<CrucibleAction> transitions = MiscUtil.buildHashSet();
	private Set<CrucibleAction> actions = MiscUtil.buildHashSet();
	@NotNull
	private User author;
	private User creator;
	private String description;
	@Nullable
	private User moderator;
	private String name;
	/** this field seems to be not initialized by ACC at all */
	@Nullable
	private PermId parentReview;
	private PermId permId;
	@NotNull
	private String projectKey;
	private String repoName;
	private State state;
	private boolean allowReviewerToJoin;
	private int metricsVersion;
	private Date createDate;
	private Date closeDate;
	@Nullable
	private DateTime dueDate;
	private String summary;
	private final String serverUrl;
	private final ReviewType type;

	public BasicReview(@NotNull ReviewType type, @NotNull String serverUrl, @NotNull String projectKey, @NotNull User author,
			@Nullable User moderator) {
		this.type = type;
		this.serverUrl = serverUrl;
		this.projectKey = projectKey;
		this.author = author;
		this.moderator = moderator;
	}

	public ReviewType getType() {
		return type;
	}

	public void setReviewers(Set<Reviewer> reviewers) {
		this.reviewers = reviewers;
	}

	public void setTransitions(@NotNull Collection<CrucibleAction> transitions) {
		this.transitions = MiscUtil.buildHashSet(transitions);
	}

	public void setActions(@NotNull Set<CrucibleAction> actions) {
		this.actions = MiscUtil.buildHashSet(actions);
	}

	public void setAuthor(@NotNull final User author) {
		this.author = author;
	}

	@NotNull
	public String getServerUrl() {
		return serverUrl;
	}

	public Set<Reviewer> getReviewers() {
		return reviewers;
	}

	public Set<CrucibleAction> getTransitions() {
		return transitions;
	}

	@NotNull
	public Set<CrucibleAction> getActions() {
		return actions;
	}

	public boolean isCompleted() {

		for (Reviewer reviewer : reviewers) {
			if (!reviewer.isCompleted()) {
				return false;
			}
		}
		return true;
	}

	@NotNull
	public User getAuthor() {
		return author;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User value) {
		this.creator = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String value) {
		this.description = value;
	}

	public User getModerator() {
		return moderator;
	}

	public void setModerator(User value) {
		this.moderator = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		this.name = value;
	}

	@Nullable
	public PermId getParentReview() {
		return parentReview;
	}

	public void setParentReview(PermId value) {
		this.parentReview = value;
	}

	@Nullable
	public PermId getPermId() {
		return permId;
	}

	public void setPermId(PermId value) {
		this.permId = value;
	}

	@NotNull
	public String getProjectKey() {
		return projectKey;
	}

	public void setProjectKey(@NotNull String value) {
		this.projectKey = value;
	}

	@Nullable
	public String getRepoName() {
		return repoName;
	}

	public void setRepoName(String value) {
		this.repoName = value;
	}

	@Nullable
	public State getState() {
		return state;
	}

	public void setState(State value) {
		this.state = value;
	}

	public boolean isAllowReviewerToJoin() {
		return allowReviewerToJoin;
	}

	public void setAllowReviewerToJoin(boolean allowReviewerToJoin) {
		this.allowReviewerToJoin = allowReviewerToJoin;
	}

	public int getMetricsVersion() {
		return metricsVersion;
	}

	public void setMetricsVersion(int metricsVersion) {
		this.metricsVersion = metricsVersion;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(Date closeDate) {
		this.closeDate = closeDate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		BasicReview that = (BasicReview) o;

		return !(permId != null ? !permId.equals(that.permId) : that.permId != null);
	}

	@Override
	public int hashCode() {
		int result;
		result = (permId != null ? permId.hashCode() : 0);
		return result;
	}

	public String getSummary() {
		return this.summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public void setDueDate(DateTime dueDate) {
		this.dueDate = dueDate;
	}

	public DateTime getDueDate() {
		return dueDate;
	}

}