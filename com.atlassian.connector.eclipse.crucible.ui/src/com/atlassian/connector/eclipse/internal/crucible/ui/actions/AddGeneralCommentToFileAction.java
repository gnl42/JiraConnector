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

import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewActionListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewAction;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;

/**
 * Action to add a general file comment to the active review
 * 
 * @author Shawn Minto
 * @author Thomas Ehrnhoefer
 */
public class AddGeneralCommentToFileAction extends AbstractAddCommentAction implements IReviewAction {

	private final CrucibleFile crucibleFile;

	private IReviewActionListener actionListener;

	public AddGeneralCommentToFileAction(CrucibleFile file, Review review) {
		super("Create General File Comment");
		super.review = review;
		this.crucibleFile = file;
	}

	@Override
	protected String getDialogTitle() {
		return getText();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return TasksUiImages.COMMENT;
	}

	@Override
	public final void run() {
		super.run();
		if (actionListener != null) {
			actionListener.actionRan(this);
		}
	}

	@Override
	public String getToolTipText() {
		return "Add General File Comment";
	}

	@Override
	protected Review getReview() {
		return review;
	}

	@Override
	protected CrucibleFile getCrucibleFile() {
		return crucibleFile;
	}

	public void setActionListener(IReviewActionListener listener) {
		this.actionListener = listener;
	}

}
