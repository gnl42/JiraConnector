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

package com.atlassian.connector.eclipse.internal.crucible.core.client.model;

import com.atlassian.theplugin.commons.crucible.api.model.Review;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to manage cached reviews for access by models other than the editor (i.e. annotations)
 * 
 * @author Shawn Minto
 */
public class ReviewCache {

	/**
	 * map of repository -> task id -> cached review
	 */
	private final Map<String, Map<String, CrucibleCachedReview>> cachedReviews;

	public ReviewCache() {
		cachedReviews = new HashMap<String, Map<String, CrucibleCachedReview>>();
	}

	/**
	 * @return true if there are changes from the last review
	 */
	public synchronized boolean updateCachedReview(String repositoryUrl, String taskId, Review review) {

		Map<String, CrucibleCachedReview> taskIdToReviewMap = cachedReviews.get(repositoryUrl);
		if (taskIdToReviewMap == null) {
			taskIdToReviewMap = new HashMap<String, CrucibleCachedReview>();
			cachedReviews.put(repositoryUrl, taskIdToReviewMap);
		}

		CrucibleCachedReview cachedReview = taskIdToReviewMap.get(taskId);
		if (cachedReview == null) {
			taskIdToReviewMap.put(taskId, new CrucibleCachedReview(review));
			return false;
		} else {
			return cachedReview.addReview(review);
		}
	}

	public synchronized Review getLastReadReview(String repositoryUrl, String taskId) {
		CrucibleCachedReview cachedReview = getCachedReview(repositoryUrl, taskId);
		if (cachedReview != null) {
			return cachedReview.getLastReadReview();
		}
		return null;
	}

	public synchronized Review getServerReview(String repositoryUrl, String taskId) {
		CrucibleCachedReview cachedReview = getCachedReview(repositoryUrl, taskId);
		if (cachedReview != null) {
			return cachedReview.getServerReview();
		}
		return null;
	}

	public synchronized Review getWorkingCopyReview(String repositoryUrl, String taskId) {
		CrucibleCachedReview cachedReview = getCachedReview(repositoryUrl, taskId);
		if (cachedReview != null) {
			return cachedReview.getWorkingCopy();
		}
		return null;
	}

	private synchronized CrucibleCachedReview getCachedReview(String repositoryUrl, String taskId) {
		Map<String, CrucibleCachedReview> taskIdToReviewMap = cachedReviews.get(repositoryUrl);
		if (taskIdToReviewMap != null) {
			return taskIdToReviewMap.get(taskId);
		}

		return null;
	}
}
