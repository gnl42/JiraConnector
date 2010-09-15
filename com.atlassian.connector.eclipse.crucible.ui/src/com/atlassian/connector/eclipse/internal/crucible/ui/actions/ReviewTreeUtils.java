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

package com.atlassian.connector.eclipse.internal.crucible.ui.actions;

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import org.eclipse.mylyn.tasks.core.ITask;

public final class ReviewTreeUtils {

	private ReviewTreeUtils() {
	}

	public static Review getReview(CrucibleFileInfo fileInfo) {
		return CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
	}

	public static ITask getTask(CrucibleFileInfo fileInfo) {
		return CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveTask();
	}

}
