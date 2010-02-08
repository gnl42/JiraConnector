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

package com.atlassian.connector.eclipse.internal.crucible.ui.dialogs;

import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.ui.dialogs.ProgressDialog;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AbstractCrucibleReviewActionDialog extends ProgressDialog {

	protected final Review review;
	protected Review updatedReview;
	protected final String userName;
	protected final TaskRepository taskRepository;
	protected final String taskKey;
	protected final String taskId;
	protected final CrucibleClient client;
	protected boolean discardDrafts = false;
	private final String actionText;

	public String getTaskKey() {
		return taskKey;
	}

	public AbstractCrucibleReviewActionDialog(Shell parentShell, Review review, String userName,
			TaskRepository taskRepository, String taskKey, String taskId, CrucibleClient client, String actionText) {
		super(parentShell);
		this.review = review;
		this.userName = userName;
		this.taskRepository = taskRepository;
		this.taskKey = taskKey;
		this.taskId = taskId;
		this.client = client;
		this.actionText = actionText;
	}

	public String getTaskId() {
		return taskId;
	}

	public TaskRepository getTaskRepository() {
		return taskRepository;
	}

	public Review getUpdatedReview() {
		return updatedReview;
	}

	public void handleUserDrafts(Composite draftComp) {
		boolean hasDrafts = checkForDrafts();
		if (hasDrafts) {
			GridDataFactory.fillDefaults().grab(true, false).applyTo(
					new Label(draftComp, SWT.SEPARATOR | SWT.HORIZONTAL));
			final Label draftComments = new Label(draftComp, SWT.NONE);
			final int numDraftComments = review.getNumberOfGeneralCommentsDrafts()
					+ review.getNumberOfVersionedCommentsDrafts();
			final String commentStr = numDraftComments == 1 ? "comment" : "comments";

			draftComments.setText("You have " + numDraftComments + " draft "
					+ commentStr + ". " + "Draft comments that aren't posted will be deleted.\n"
					+ "Please choose an action:");
			GridDataFactory.fillDefaults().span(2, 1).applyTo(draftComments);

			Button deleteDrafts = new Button(draftComp, SWT.RADIO);
			deleteDrafts.setText("Discard Drafts");
			deleteDrafts.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					discardDrafts = true;
				}
			});
			Button postDrafts = new Button(draftComp, SWT.RADIO);
			postDrafts.setText("Post Drafts");
			postDrafts.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					discardDrafts = false;
				}
			});
			if (discardDrafts) {
				deleteDrafts.setSelection(true);
			} else {
				postDrafts.setSelection(true);
			}
		}
	}

	private boolean checkForDrafts() {
		if ((review.getNumberOfGeneralCommentsDrafts() + review.getNumberOfVersionedCommentsDrafts()) > 0) {
			return true;
		}
		return false;
	}

	protected abstract void doAction();

	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		Button summarizeButton = createButton(parent, IDialogConstants.CLIENT_ID + 1, actionText, false);
		summarizeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doAction();
			}
		});

		Button cancelButton = createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cancelPressed();
			}
		});

	}

	protected Reviewer getReviewer(User author) {
		for (Reviewer reviewer : review.getReviewers()) {
			if (reviewer.getUsername().equals(author.getUsername())) {
				return reviewer;
			}
		}
		return null;
	}

	protected Set<Reviewer> getOpenReviewers() {
		Set<Reviewer> openReviewers = new LinkedHashSet<Reviewer>();
		for (Reviewer reviewer : review.getReviewers()) {
			if (!reviewer.isCompleted()) {
				openReviewers.add(reviewer);
			}
		}
		return openReviewers;
	}

	protected Set<Reviewer> getCompletedReviewers() {
		Set<Reviewer> completedReviewers = new LinkedHashSet<Reviewer>();
		for (Reviewer reviewer : review.getReviewers()) {
			if (reviewer.isCompleted()) {
				completedReviewers.add(reviewer);
			}
		}
		return completedReviewers;
	}

}