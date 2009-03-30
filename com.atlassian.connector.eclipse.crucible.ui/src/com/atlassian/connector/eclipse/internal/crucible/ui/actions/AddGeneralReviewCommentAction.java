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

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

/**
 * Action for adding a general review comment
 * 
 * @author Thomas Ehrnhoefer
 */
public class AddGeneralReviewCommentAction extends AbstractAddCommentAction {

	public AddGeneralReviewCommentAction(Review review) {
		super("Add General Review Comment");
		setImageDescriptor(CrucibleImages.ADD_COMMENT);
		setToolTipText("Add General Review Comment");
		super.review = review;
	}

	@Override
	protected String getDialogTitle() {
		return getText();
	}

	@Override
	protected Review getReview() {
		return review;
	}
}
