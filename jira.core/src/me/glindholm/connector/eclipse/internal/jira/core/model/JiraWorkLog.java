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

package me.glindholm.connector.eclipse.internal.jira.core.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

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

    private Instant created;

    private String groupLevel;

    private String id;

    private String roleLevelId;

    private Instant startDate;

    private long timeSpent;

    private String updateAuthor;

    private Instant updated;

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

    public Instant getCreated() {
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

    public Instant getStartDate() {
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

    public Instant getUpdated() {
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

    public void setCreated(Instant created) {
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

    public void setStartDate(Instant startDate) {
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

    public void setUpdated(Instant updated) {
        this.updated = updated;
    }

    @Override
    public int hashCode() {
        return Objects.hash(adjustEstimate, author, comment, created, groupLevel, id, newRemainingEstimate, roleLevelId, startDate, timeSpent, updateAuthor,
                updated);
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
        return adjustEstimate == other.adjustEstimate && Objects.equals(author, other.author) && Objects.equals(comment, other.comment)
                && Objects.equals(created, other.created) && Objects.equals(groupLevel, other.groupLevel) && Objects.equals(id, other.id)
                && newRemainingEstimate == other.newRemainingEstimate && Objects.equals(roleLevelId, other.roleLevelId)
                && Objects.equals(startDate, other.startDate) && timeSpent == other.timeSpent && Objects.equals(updateAuthor, other.updateAuthor)
                && Objects.equals(updated, other.updated);
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
