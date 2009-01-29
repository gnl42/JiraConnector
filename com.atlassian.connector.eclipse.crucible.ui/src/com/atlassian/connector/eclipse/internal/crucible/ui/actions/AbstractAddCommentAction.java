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

import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.dialogs.CrucibleReviewReplyDialog;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewChangeJob;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.AddCommentRemoteOperation;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CustomField;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.window.Window;

import java.util.HashMap;

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

		final LineRange commentLines = getSelectedRange();
		final CrucibleFile reviewItem = getCrucibleFile();
		final Comment parentComment = getParentComment();

		CrucibleReviewReplyDialog commentDialog = new CrucibleReviewReplyDialog(null, getDialogTitle(), review,
				reviewItem, parentComment, commentLines);
		if (commentDialog.open() == Window.OK) {
			if (commentDialog.getValue().length() > 0) {
				final String message = commentDialog.getValue();
				final boolean isDraft = commentDialog.isDraft();
				final boolean isDefect = commentDialog.isDefect();
				final HashMap<String, CustomField> customFields = commentDialog.getCustomFieldSelections();

				CrucibleReviewChangeJob job = new CrucibleReviewChangeJob("Submitting comment" + getTaskKey(),
						getTaskRepository()) {
					@Override
					protected IStatus execute(final CrucibleClient client, IProgressMonitor monitor)
							throws CoreException {
						AddCommentRemoteOperation operation = new AddCommentRemoteOperation(monitor, review, client,
								reviewItem, message);
						operation.setDefect(isDefect);
						operation.setDraft(isDraft);
						operation.setCustomFields(customFields);
						operation.setParentComment(parentComment);

						client.execute(operation);
						client.getReview(getTaskRepository(), getTaskId(), true, monitor);

						return new Status(IStatus.OK, CrucibleUiPlugin.PLUGIN_ID, "Comment was submitted.");
					}

				};
				job.schedule(0L);
			}
		}
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

}
