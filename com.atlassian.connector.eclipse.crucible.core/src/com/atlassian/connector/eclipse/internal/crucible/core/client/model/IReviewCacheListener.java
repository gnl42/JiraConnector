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

import java.util.List;

/**
 * Listener for cache model changes
 * 
 * @author Shawn Minto
 */
public interface IReviewCacheListener {

	void reviewUpdated(String repositoryUrl, String taskId, Review review, List<CrucibleNotification> differences);

	void reviewAdded(String repositoryUrl, String taskId, Review review);

}
