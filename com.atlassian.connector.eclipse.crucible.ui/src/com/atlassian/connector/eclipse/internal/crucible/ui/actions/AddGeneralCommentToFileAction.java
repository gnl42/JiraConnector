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

import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;

/**
 * Action to add a general file comment to the active review
 * 
 * @author Shawn Minto
 */
public class AddGeneralCommentToFileAction extends AbstractAddCommentAction {

	private final CrucibleFile crucibleFile;

	private final Review crucibleReview;

	public AddGeneralCommentToFileAction(CrucibleFile file, Review review) {
		super("Create General File Comment");
		this.crucibleReview = review;
		this.crucibleFile = file;
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
		return crucibleReview;
	}

	@Override
	protected CrucibleFile getCrucibleFile() {
		return crucibleFile;
	}
}
