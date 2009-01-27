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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	private final Set<IReviewCacheListener> cacheListeners;

	private final Map<Integer, List<CustomFieldDef>> metricsMap;

	public ReviewCache() {
		cachedReviews = new HashMap<String, Map<String, CrucibleCachedReview>>();
		cacheListeners = new HashSet<IReviewCacheListener>();
		metricsMap = new HashMap<Integer, List<CustomFieldDef>>();
	}

	/**
	 * @return true if there are changes from the last review
	 */
	public synchronized boolean updateCachedReview(String repositoryUrl, String taskId, Review review) {
		if (CrucibleUtil.isPartialReview(review)) {
			StatusHandler.log(new Status(IStatus.WARNING, CrucibleCorePlugin.PLUGIN_ID, "Cannot cache partial review",
					new Exception()));
			return false;
		}

		Map<String, CrucibleCachedReview> taskIdToReviewMap = cachedReviews.get(repositoryUrl);
		if (taskIdToReviewMap == null) {
			taskIdToReviewMap = new HashMap<String, CrucibleCachedReview>();
			cachedReviews.put(repositoryUrl, taskIdToReviewMap);
		}

		CrucibleCachedReview cachedReview = taskIdToReviewMap.get(taskId);
		if (cachedReview == null) {
			taskIdToReviewMap.put(taskId, new CrucibleCachedReview(review));
			fireReviewAdded(repositoryUrl, taskId, review);
			// TODO deal with the offline case here potentially
			return false;
		} else {
			boolean changed = cachedReview.addReview(review);
			if (changed) {
				fireReviewUpdated(repositoryUrl, taskId, review, cachedReview.getDifferences());
			}
			return changed;
		}
	}

	private void fireReviewUpdated(String repositoryUrl, String taskId, Review review,
			List<CrucibleNotification> differences) {
		for (IReviewCacheListener cacheListener : cacheListeners) {
			cacheListener.reviewUpdated(repositoryUrl, taskId, review, differences);
		}
	}

	private void fireReviewAdded(String repositoryUrl, String taskId, Review review) {
		for (IReviewCacheListener cacheListener : cacheListeners) {
			cacheListener.reviewAdded(repositoryUrl, taskId, review);
		}
	}

	public void addCacheChangedListener(IReviewCacheListener cacheListener) {
		cacheListeners.add(cacheListener);
	}

	public void removeCacheChangedListener(IReviewCacheListener cacheListener) {
		cacheListeners.remove(cacheListener);
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

	public synchronized boolean hasMetrics(int version) {
		return getMetrics(version) != null;
	}

	public synchronized List<CustomFieldDef> getMetrics(int version) {
		return metricsMap.get(version);
	}

	public synchronized void setMetrics(int version, List<CustomFieldDef> metrics) {
		metricsMap.put(version, metrics);
	}
}
