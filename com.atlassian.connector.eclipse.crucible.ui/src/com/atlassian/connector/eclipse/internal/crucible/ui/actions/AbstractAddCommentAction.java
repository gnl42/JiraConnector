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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.dialogs.CrucibleAddCommentDialog;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;

/**
 * Abstract class to deal with adding comments to a review
 * 
 * @author Shawn Minto
 * @author Thomas Ehrnhoefer
 */
public abstract class AbstractAddCommentAction extends AbstractReviewAction {

	protected AbstractAddCommentAction(String text) {
		super(text);
	}

	public void run(IAction action) {

		if (review == null) {
			return;
		}

		LineRange commentLines = getSelectedRange();
		CrucibleFile reviewItem = getCrucibleFile();
		Comment parentComment = getParentComment();

		CrucibleRepositoryConnector connector = CrucibleCorePlugin.getRepositoryConnector();
		CrucibleClient client = connector.getClientManager().getClient(getTaskRepository());
		if (client == null) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
					"Unable to get client, please try to refresh"));
			return;
		}

		CrucibleAddCommentDialog commentDialog = new CrucibleAddCommentDialog(WorkbenchUtil.getShell(),
				getDialogTitle(), review, reviewItem, parentComment, commentLines, getTaskKey(), getTaskId(),
				getTaskRepository(), client);
		commentDialog.open();
	}

	protected abstract String getDialogTitle();

	protected CrucibleFile getCrucibleFile() {
		return null;
	}

	protected LineRange getSelectedRange() {
		return null;
	}

	protected Comment getParentComment() {
		return null;
	}

	@Override
	public boolean isEnabled() {
		Review myReview = getReview();
		return super.isEnabled() && (myReview != null && CrucibleUtil.canAddCommentToReview(getReview()));
	}

}
