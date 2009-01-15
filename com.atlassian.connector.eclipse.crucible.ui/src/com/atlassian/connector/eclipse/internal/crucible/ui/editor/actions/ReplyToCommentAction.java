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

package com.atlassian.connector.eclipse.internal.crucible.ui.editor.actions;

import com.atlassian.theplugin.commons.crucible.api.model.Comment;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;

/**
 * Action to reply to a comment
 * 
 * @author Shawn Minto
 */
public class ReplyToCommentAction extends Action {
	private final Comment comment;

	public ReplyToCommentAction(Comment comment) {
		this.comment = comment;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return TasksUiImages.COMMENT_REPLY;
	}

	@Override
	public String getToolTipText() {
		return "Reply";
	}

	@Override
	public void run() {
		MessageDialog.openInformation(null, "Unsupported Operation", "This operation is currently unsupported");

	}
}