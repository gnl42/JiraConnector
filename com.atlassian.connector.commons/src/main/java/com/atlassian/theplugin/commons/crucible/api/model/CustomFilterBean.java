/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.commons.crucible.api.model;

import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import org.apache.commons.lang.StringUtils;


public class CustomFilterBean implements CustomFilter {
	private ServerIdImpl serverId;
	private String title = "";
	private State[] state = new State[0];
	private String author = "";
	private String moderator = "";
	private String creator = "";
	private String reviewer = "";
	private boolean orRoles;
	private Boolean complete;
	private Boolean allReviewersComplete;
	private String projectKey = "";
	private boolean enabled;
	private static final double ID_DISCRIMINATOR = 1002d;
	private static final int HASHCODE_CONSTANT = 31;
	private static final int SHIFT_32 = 32;
	public static final String FILTER_ID = "MANUAL_FILTER_ID";
	private final String filterName = "Custom";
    private boolean empty = false;

    @Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		CustomFilterBean that = (CustomFilterBean) o;

		if (uid != that.uid) {
			return false;
		}
		if (!filterName.equals(that.filterName)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		result = (filterName != null ? filterName.hashCode() : 0);
		result = HASHCODE_CONSTANT * result + (int) (uid ^ (uid >>> SHIFT_32));
		return result;
	}

	private transient long uid = System.currentTimeMillis() + (long) (Math.random() * ID_DISCRIMINATOR);

	public ServerIdImpl getServerId() {
		return serverId;
	}

	public void setServerId(ServerIdImpl serverId) {
		this.serverId = serverId;
	}

    public CustomFilterBean copy(CustomFilterBean bean) {
        this.serverId = bean.serverId;
        this.title = bean.title;
        this.state = bean.state;
        this.author = bean.author;
        this.moderator = bean.moderator;
        this.creator = bean.creator;
        this.reviewer = bean.reviewer;
        this.orRoles = bean.orRoles;
        this.complete = bean.complete;
        this.allReviewersComplete = bean.allReviewersComplete;
        this.projectKey = bean.projectKey;
        this.enabled = bean.enabled;
        this.empty = bean.empty;
        return this;
    }
    public CustomFilterBean(CustomFilterBean bean) {
          copy(bean);
    }



	public CustomFilterBean() {
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public State[] getState() {
		return state;
	}

	public void setState(State[] state) {

		this.state = state;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getModerator() {
		return moderator;
	}

	public void setModerator(String moderator) {
		this.moderator = moderator;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getReviewer() {
		return reviewer;
	}

	public void setReviewer(String reviewer) {
		this.reviewer = reviewer;
	}

	public Boolean isComplete() {
		return complete;
	}

	public void setComplete(Boolean complete) {
		this.complete = complete;
	}

	public Boolean isAllReviewersComplete() {
		return allReviewersComplete;
	}

	public void setAllReviewersComplete(Boolean allReviewersComplete) {
		this.allReviewersComplete = allReviewersComplete;
	}

	public String getProjectKey() {
		return projectKey;
	}

	public void setProjectKey(String projectKey) {
		this.projectKey = projectKey;
	}

	public boolean isOrRoles() {
		return orRoles;
	}

	public void setOrRoles(boolean orRoles) {
		this.orRoles = orRoles;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getId() {
		return FILTER_ID;
	}

	public String getFilterName() {
		return filterName;
	}

	public String getFilterUrl() {
		return prepareCustomFilterUrl();
	}

	private String prepareCustomFilterUrl() {
		StringBuilder url = new StringBuilder();

		addQueryParam(AUTHOR, getAuthor(), url);
		addQueryParam(CREATOR, getCreator(), url);
		addQueryParam(MODERATOR, getModerator(), url);
		addQueryParam(REVIEWER, getReviewer(), url);
		addQueryParam(PROJECT, getProjectKey(), url);
		String stateParam = getStates();
		addQueryParam(STATES, stateParam, url);

		if (isComplete() != null) {
			addQueryParam(COMPLETE, Boolean.toString(isComplete()), url);
		}
		addQueryParam(ORROLES, Boolean.toString(isOrRoles()), url);
		if (isAllReviewersComplete() != null) {
			addQueryParam(ALLCOMPLETE, Boolean.toString(isAllReviewersComplete()), url);
		}

		String urlString = url.toString();
		return urlString.equals("?") ? "" : urlString;
	}

	private void addQueryParam(String name, String value, StringBuilder builder) {
		if (!StringUtils.isEmpty(value)) {
			if (builder.length() > 0) {
				builder.append("&");
			}
			builder.append(name).append("=").append(value);
		}
	}

	public String getStates() {
		String stateParam = "";
		if (getState() != null) {
			for (State s : getState()) {
				if (stateParam.length() > 0) {
					stateParam += ",";
				}
				stateParam += s.value();
			}
		}
		return stateParam;
	}

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }
}
