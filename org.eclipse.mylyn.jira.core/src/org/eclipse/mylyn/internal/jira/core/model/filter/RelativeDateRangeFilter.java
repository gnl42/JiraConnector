/*******************************************************************************
 * Copyright (c) 2007 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.model.filter;

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
	public RelativeDateRangeFilter(RangeType rangeType, long count) {
		this(rangeType, count, RangeType.NONE, 0);
	}

	public RelativeDateRangeFilter(RangeType previousRangeType, long previousCount, RangeType nextRangeType,
			long nextCount) {
		this.previousRangeType = previousRangeType;
		this.previousCount = previousCount;
		this.nextRangeType = nextRangeType;
		this.nextCount = nextCount;
	}

	public long previousMilliseconds() {
		return Math.abs(this.previousRangeType.getMultiplier() * previousCount);
	}

	public long nextMilliseconds() {
		return Math.abs(this.nextRangeType.getMultiplier() * nextCount);
	}

	public long getNextCount() {
		return this.nextCount;
	}

	public RangeType getNextRangeType() {
		return this.nextRangeType;
	}

	public long getPreviousCount() {
		return this.previousCount;
	}

	public RangeType getPreviousRangeType() {
		return this.previousRangeType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("from "); //$NON-NLS-1$
		if (RangeType.NONE == this.previousRangeType) {
			sb.append("whenever"); //$NON-NLS-1$
		} else {
			sb.append(this.previousCount).append(this.previousRangeType);
		}
		sb.append(" to "); //$NON-NLS-1$
		if (RangeType.NONE == this.nextRangeType) {
			sb.append("whenever"); //$NON-NLS-1$
		} else {
			sb.append(this.nextCount).append(this.nextRangeType);
		}
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.model.filter.DateFilter#copy()
	 */
	@Override
	DateFilter copy() {
		return new RelativeDateRangeFilter(this.previousRangeType, previousCount, nextRangeType, nextCount);
	}

	public static class RangeType {
		public static final RangeType NONE = new RangeType(0);

		public static final RangeType MINUTE = new RangeType(1000 * 60);

		public static final RangeType HOUR = new RangeType(1000 * 60 * 60);

		public static final RangeType DAY = new RangeType(1000 * 60 * 60 * 24);

		public static final RangeType WEEK = new RangeType(1000 * 60 * 60 * 24 * 7);

		private final long multiplier;

		private RangeType(long multiplier) {
			this.multiplier = multiplier;
		}

		public long getMultiplier() {
			return this.multiplier;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			if (HOUR.equals(this)) {
				return "h"; //$NON-NLS-1$
			} else if (DAY.equals(this)) {
				return "d"; //$NON-NLS-1$
			} else if (WEEK.equals(this)) {
				return "w"; //$NON-NLS-1$
			} else if (MINUTE.equals(this)) {
				return "m"; //$NON-NLS-1$
			} else {
				return "none"; //$NON-NLS-1$
			}
		}

	}

}
