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

import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import java.util.Comparator;
import java.util.Date;

/**
 * A comparator for sorting versioned comments
 * 
 * @author Shawn Minto
 */
public class VersionedCommentDateComparator implements Comparator<VersionedComment> {

	public int compare(VersionedComment o1, VersionedComment o2) {
		if (o1 != null && o2 != null) {
			int difference = 0;
			if (o1.isToLineInfo() && o2.isToLineInfo()) {
				Integer start1 = o1.getToStartLine();
				Integer start2 = o2.getToStartLine();

				difference = start1.compareTo(start2);
			} else if (o1.isToLineInfo()) {
				difference = 1;
			} else if (o2.isToLineInfo()) {
				difference = -1;
			}

			if (difference == 0) {
				Date d1 = o1.getCreateDate();
				Date d2 = o2.getCreateDate();
				difference = d1.compareTo(d2);
			}
			return difference;
		}
		return 0;
	}

}
