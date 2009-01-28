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

package com.atlassian.connector.eclipse.internal.crucible.ui.editor.actions;

import com.atlassian.connector.eclipse.internal.crucible.ui.actions.AbstractAddCommentAction;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;

/**
 * @author thomas
 */
public class AddGeneralReviewCommentAction extends AbstractAddCommentAction {

	private final Review review;

	public AddGeneralReviewCommentAction(Review review) {
		super("Add General Review Comment");
		this.review = review;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return TasksUiImages.COMMENT;
	}

	@Override
	public String getToolTipText() {
		return "Add General Review Comment";
	}

	@Override
	protected Review getReview() {
		return review;
	}
}
