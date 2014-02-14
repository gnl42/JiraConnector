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

package com.atlassian.connector.eclipse.internal.jira.core.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
 */
public class JiraWorkLog implements Serializable {

	public enum AdjustEstimateMethod {
		AUTO("AUTO"), //$NON-NLS-1$
		LEAVE("LEAVE"), //$NON-NLS-1$
		SET("SET"), //$NON-NLS-1$
		REDUCE("REDUCE"); //$NON-NLS-1$

		private final String value;

		AdjustEstimateMethod(String value) {
			this.value = value;
		}

		public String value() {
			return value;
		}

		public static AdjustEstimateMethod fromValue(String v) {
			for (AdjustEstimateMethod c : AdjustEstimateMethod.values()) {
				if (c.value.equals(v)) {
					return c;
				}
			}
			throw new IllegalArgumentException(v);
		}
	}

	private static final long serialVersionUID = 1L;

	private String author;

	private String comment;

	private Date created;

	private String groupLevel;

	private String id;

	private String roleLevelId;

	private Date startDate;

	private long timeSpent;

	private String updateAuthor;

	private Date updated;

	private long newRemainingEstimate;

	private AdjustEstimateMethod adjustEstimate = AdjustEstimateMethod.AUTO;

	public JiraWorkLog() {
	}

	public AdjustEstimateMethod getAdjustEstimate() {
		return adjustEstimate;
	}

	public String getAuthor() {
		return author;
	}

	public String getComment() {
		return comment;
	}

	public Date getCreated() {
		return created;
	}

	public String getGroupLevel() {
		return groupLevel;
	}

	public String getId() {
		return id;
	}

	public String getRoleLevelId() {
		return roleLevelId;
	}

	public Date getStartDate() {
		return startDate;
	}

	/**
	 * Returns the time spent in seconds.
	 */
	public long getTimeSpent() {
		return timeSpent;
	}

	public String getUpdateAuthor() {
		return updateAuthor;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setAdjustEstimate(AdjustEstimateMethod method) {
		this.adjustEstimate = method;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public void setGroupLevel(String groupLevel) {
		this.groupLevel = groupLevel;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setRoleLevelId(String roleLevelId) {
		this.roleLevelId = roleLevelId;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @param timeSpent
	 *            seconds
	 */
	public void setTimeSpent(long timeSpent) {
		this.timeSpent = timeSpent;
	}

	public void setUpdateAuthor(String updateAuthor) {
		this.updateAuthor = updateAuthor;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((author == null) ? 0 : author.hashCode());
		result = prime * result + ((adjustEstimate == null) ? 0 : adjustEstimate.hashCode());
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result + ((groupLevel == null) ? 0 : groupLevel.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((roleLevelId == null) ? 0 : roleLevelId.hashCode());
		result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
		result = prime * result + (int) (timeSpent ^ (timeSpent >>> 32));
		result = prime * result + ((updateAuthor == null) ? 0 : updateAuthor.hashCode());
		result = prime * result + ((updated == null) ? 0 : updated.hashCode());
		result = prime * result + (int) (newRemainingEstimate ^ (newRemainingEstimate >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		JiraWorkLog other = (JiraWorkLog) obj;
		if (author == null) {
			if (other.author != null) {
				return false;
			}
		} else if (!author.equals(other.author)) {
			return false;
		}
		if (adjustEstimate != other.adjustEstimate) {
			return false;
		}
		if (comment == null) {
			if (other.comment != null) {
				return false;
			}
		} else if (!comment.equals(other.comment)) {
			return false;
		}
		if (created == null) {
			if (other.created != null) {
				return false;
			}
		} else if (!created.equals(other.created)) {
			return false;
		}
		if (groupLevel == null) {
			if (other.groupLevel != null) {
				return false;
			}
		} else if (!groupLevel.equals(other.groupLevel)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (roleLevelId == null) {
			if (other.roleLevelId != null) {
				return false;
			}
		} else if (!roleLevelId.equals(other.roleLevelId)) {
			return false;
		}
		if (startDate == null) {
			if (other.startDate != null) {
				return false;
			}
		} else if (!startDate.equals(other.startDate)) {
			return false;
		}
		if (timeSpent != other.timeSpent) {
			return false;
		}
		if (updateAuthor == null) {
			if (other.updateAuthor != null) {
				return false;
			}
		} else if (!updateAuthor.equals(other.updateAuthor)) {
			return false;
		}
		if (updated == null) {
			if (other.updated != null) {
				return false;
			}
		} else if (!updated.equals(other.updated)) {
			return false;
		}
		if (newRemainingEstimate != other.newRemainingEstimate) {
			return false;
		}
		return true;
	}

	public void setNewRemainingEstimate(long newRemainingEstimate) {
		this.newRemainingEstimate = newRemainingEstimate;
	}

	public long getNewRemainingEstimate() {
		return newRemainingEstimate;
	}

	public boolean isAutoAdjustEstimate() {
		return adjustEstimate == AdjustEstimateMethod.AUTO;
	}
}
