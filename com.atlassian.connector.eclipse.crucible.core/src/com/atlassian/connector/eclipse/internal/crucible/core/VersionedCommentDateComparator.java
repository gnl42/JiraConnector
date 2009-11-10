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

import com.atlassian.connector.commons.misc.IntRanges;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import java.util.Comparator;
import java.util.Date;
import java.util.Map;

/**
 * A comparator for sorting versioned comments
 * 
 * @author Shawn Minto
 */
public class VersionedCommentDateComparator implements Comparator<VersionedComment> {

	@SuppressWarnings("deprecation")
	private int getStartLine(VersionedComment vc) {
		final Map<String, IntRanges> lineRanges = vc.getLineRanges();
		if (lineRanges != null) {
			return getStartLine(lineRanges);
		}
		if (vc.isToLineInfo()) {
			return vc.getToLineRanges().getTotalMin();
		} else if (vc.isFromLineInfo()) {
			return vc.getFromLineRanges().getTotalMin();
		} else {
			return 0;
		}
	}

	public int getStartLine(Map<String, IntRanges> lineRanges) {
		boolean firstTime = true;
		int res = 0;
		for (IntRanges lines : lineRanges.values()) {
			final int totalMin = lines.getTotalMin();
			if (totalMin < res || firstTime) {
				firstTime = false;
				res = totalMin;
			}
		}
		return res;
	}

	public int compare(VersionedComment o1, VersionedComment o2) {
		if (o1 != null && o2 != null) {
			final Integer start1 = getStartLine(o1);
			final Integer start2 = getStartLine(o2);
			final int difference = start1.compareTo(start2);
			if (difference == 0) {
				Date d1 = o1.getCreateDate();
				Date d2 = o2.getCreateDate();
				return d1.compareTo(d2);
			} else {
				return difference;
			}
		}
		return 0;
	}
}
