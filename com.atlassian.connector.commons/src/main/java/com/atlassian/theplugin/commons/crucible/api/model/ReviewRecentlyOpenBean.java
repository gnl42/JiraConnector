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

import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;

/**
 * @author Jacek Jaroczynski
 */
public class ReviewRecentlyOpenBean {
	private ServerIdImpl serverId;
	private String reviewId;

	public ReviewRecentlyOpenBean() {
	}

	public ReviewRecentlyOpenBean(final ServerId serverId, final String reviewId) {
		if (serverId instanceof ServerIdImpl) {
			this.serverId = (ServerIdImpl) serverId;
		}
		this.reviewId = reviewId;
	}

	public ServerIdImpl getServerId() {
		return serverId;
	}

	public void setServerId(final ServerIdImpl serverId) {
		this.serverId = serverId;
	}

	public String getReviewId() {
		return reviewId;
	}

	public void setReviewId(final String reviewId) {
		this.reviewId = reviewId;
	}

	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final ReviewRecentlyOpenBean that = (ReviewRecentlyOpenBean) o;

		if (reviewId != null ? !reviewId.equals(that.reviewId) : that.reviewId != null) {
			return false;
		}
		if (serverId != null ? !serverId.equals(that.serverId) : that.serverId != null) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		int result;
		result = (serverId != null ? serverId.hashCode() : 0);
		result = 31 * result + (reviewId != null ? reviewId.hashCode() : 0);
		return result;
	}

}
