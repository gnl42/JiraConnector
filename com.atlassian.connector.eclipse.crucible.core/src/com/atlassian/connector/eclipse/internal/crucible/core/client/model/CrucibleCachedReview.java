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

package com.atlassian.connector.eclipse.internal.crucible.core.client.model;

import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;
import com.atlassian.theplugin.commons.crucible.api.model.notification.ReviewDifferenceProducer;

import java.util.List;

/**
 * Class to represent a cached review and its new revision. This is used for determining changes for notifications and
 * the tasklist. This type is purely internal for the ReviewCache
 * 
 * @author Shawn Minto
 */
public class CrucibleCachedReview {

	private Review lastReadReview; // lastread

	private int lastReadRevision = 1;

	private Review serverReview; // repository

	private int serverRevision = 1;

	private List<CrucibleNotification> differences;

	CrucibleCachedReview(Review review) {
		this.lastReadReview = review;
	}

	synchronized Review getLastReadReview() {
		return lastReadReview;
	}

	synchronized Review getServerReview() {
		if (serverReview != null) {
			return serverReview;
		} else {
			return lastReadReview;
		}
	}

	synchronized Review getWorkingCopy() {
		if (serverRevision != lastReadRevision && serverReview != null) {
			lastReadReview = serverReview;
			serverReview = null;
			lastReadRevision = serverRevision;
			differences = null;
		}
		return lastReadReview;
	}

	synchronized boolean addReview(Review review) {
		ReviewDifferenceProducer differencer = new ReviewDifferenceProducer(lastReadReview, review);

		if (serverReview != null) {
			ReviewDifferenceProducer serverDifferencer = new ReviewDifferenceProducer(serverReview, review);
			List<CrucibleNotification> serverDiffs = serverDifferencer.getDiff();
			if ((serverDiffs == null || serverDiffs.size() == 0) && serverDifferencer.isShortEqual()
					&& serverDifferencer.isFilesEqual() && serverDifferencer.getCommentChangesCount() == 0) {
				return false;
			}
		}

		differences = differencer.getDiff();
		if (differences.size() > 0 || !differencer.isShortEqual() || !differencer.isFilesEqual()) {
			serverRevision++;
			serverReview = review;

			return true;
		} else {
			differences = null;
			return false;
		}
	}

	synchronized List<CrucibleNotification> getDifferences() {
		return differences; // TODO remove changes from me?
	}

}
