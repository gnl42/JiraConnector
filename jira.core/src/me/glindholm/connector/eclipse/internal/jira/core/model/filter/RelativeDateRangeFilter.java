/*******************************************************************************
 * Copyright (c) 2004, 2008 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core.model.filter;

/**
 * @author Brock Janiczak
 */
public class RelativeDateRangeFilter extends DateFilter {
    private static final long serialVersionUID = 1L;

    private final RangeType previousRangeType;

    private final long previousCount;

    private final RangeType nextRangeType;

    private final long nextCount;

    /**
     * Creates a date range from now back to the specified range
     *
     * @param rangeType
     *            Unit of measure
     * @param count
     *            Number of units
     */
    public RelativeDateRangeFilter(final RangeType rangeType, final long count) {
        this(rangeType, count, RangeType.NONE, 0);
    }

    public RelativeDateRangeFilter(final RangeType previousRangeType, final long previousCount, final RangeType nextRangeType,
            final long nextCount) {
        this.previousRangeType = previousRangeType;
        this.previousCount = previousCount;
        this.nextRangeType = nextRangeType;
        this.nextCount = nextCount;
    }

    public long previousMilliseconds() {
        return Math.abs(previousRangeType.getMultiplier() * previousCount);
    }

    public long nextMilliseconds() {
        return Math.abs(nextRangeType.getMultiplier() * nextCount);
    }

    public long getNextCount() {
        return nextCount;
    }

    public RangeType getNextRangeType() {
        return nextRangeType;
    }

    public long getPreviousCount() {
        return previousCount;
    }

    public RangeType getPreviousRangeType() {
        return previousRangeType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("from "); //$NON-NLS-1$
        if (RangeType.NONE == previousRangeType) {
            sb.append("whenever"); //$NON-NLS-1$
        } else {
            sb.append(previousCount).append(previousRangeType);
        }
        sb.append(" to "); //$NON-NLS-1$
        if (RangeType.NONE == nextRangeType) {
            sb.append("whenever"); //$NON-NLS-1$
        } else {
            sb.append(nextCount).append(nextRangeType);
        }
        return sb.toString();
    }

    @Override
    DateFilter copy() {
        return new RelativeDateRangeFilter(previousRangeType, previousCount, nextRangeType, nextCount);
    }

    public static class RangeType {
        public static final RangeType NONE = new RangeType(0);

        public static final RangeType MINUTE = new RangeType(1000 * 60);

        public static final RangeType HOUR = new RangeType(1000 * 60 * 60);

        public static final RangeType DAY = new RangeType(1000 * 60 * 60 * 24);

        public static final RangeType WEEK = new RangeType(1000 * 60 * 60 * 24 * 7);

        private final long multiplier;

        private RangeType(final long multiplier) {
            this.multiplier = multiplier;
        }

        public long getMultiplier() {
            return multiplier;
        }

        @Override
        public String toString() {
            if (this.equals(HOUR)) {
                return "h"; //$NON-NLS-1$
            } else if (this.equals(DAY)) {
                return "d"; //$NON-NLS-1$
            } else if (this.equals(WEEK)) {
                return "w"; //$NON-NLS-1$
            } else if (this.equals(MINUTE)) {
                return "m"; //$NON-NLS-1$
            } else {
                return "none"; //$NON-NLS-1$
            }
        }

    }

}
