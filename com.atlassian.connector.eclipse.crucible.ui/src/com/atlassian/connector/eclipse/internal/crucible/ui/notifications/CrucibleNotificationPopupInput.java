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

package com.atlassian.connector.eclipse.internal.crucible.ui.notifications;

import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;

import org.eclipse.mylyn.tasks.ui.TasksUiUtil;

import java.util.List;

/**
 * Input for the crucible change notification
 * 
 * @author Shawn Minto
 */
public class CrucibleNotificationPopupInput {

	private final List<CrucibleNotification> differences;

	private final Review review;

	private final String repositoryUrl;

	private final String taskId;

	private boolean isNew;

	public CrucibleNotificationPopupInput(String repositoryUrl, String taskId, Review review,
			List<CrucibleNotification> differences) {
		this.differences = differences;
		this.review = review;
		this.repositoryUrl = repositoryUrl;
		this.taskId = taskId;
	}

	public CrucibleNotificationPopupInput(String repositoryUrl, String taskId, Review review) {
		this(repositoryUrl, taskId, review, null);
		this.isNew = true;
	}

	public boolean isNew() {
		return isNew;
	}

	public String getDescription() {
		if (isNew()) {
			return "New Review";
		} else {
			if (differences == null || differences.size() == 0) {
				return "Review Details Changed";
			} else {
				String changedText = "";
				for (CrucibleNotification notification : differences) {
					changedText += notification.getPresentationMessage() + "\n";
				}
				return changedText;
			}
		}
	}

	public Review getReview() {
		return review;
	}

	public String getLabel() {
		return review.getPermId().getId() + " " + review.getName();
	}

	public void open() {
		TasksUiUtil.openTask(repositoryUrl, taskId, null);
	}

}
