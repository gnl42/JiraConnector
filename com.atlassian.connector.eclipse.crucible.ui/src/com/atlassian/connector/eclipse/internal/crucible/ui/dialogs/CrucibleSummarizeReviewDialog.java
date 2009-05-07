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
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient.RemoteOperation;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.parts.CrucibleReviewersPart;
import com.atlassian.connector.eclipse.ui.dialogs.ProgressDialog;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Dialog shown to the user when they summarize a review
 * 
 * @author Thomas Ehrnhoefer
 * @author Shawn Minto
 */
public class CrucibleSummarizeReviewDialog extends ProgressDialog {

	private final class SummarizeReviewRunnable implements IRunnableWithProgress {
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

			monitor.beginTask("Summarize and Close Review", IProgressMonitor.UNKNOWN);

			try {
				if (!discardDrafts) {
					//post all drafts
					RemoteOperation<Object> publishDraftsOp = new RemoteOperation<Object>(monitor, getTaskRepository()) {
						@Override
						public Object run(CrucibleServerFacade server, ServerData serverCfg, IProgressMonitor monitor)
								throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
							server.publishAllCommentsForReview(serverCfg, review.getPermId());
							return null;
						}
					};
					client.execute(publishDraftsOp);
					updatedReview = client.getReview(getTaskRepository(), getTaskId(), true, monitor);
				}
				//summarize
				RemoteOperation<Object> summarizeOp = new RemoteOperation<Object>(monitor, getTaskRepository()) {
					@Override
					public Object run(CrucibleServerFacade server, ServerData serverCfg, IProgressMonitor monitor)
							throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
						// ignore
						server.summarizeReview(serverCfg, review.getPermId());
						server.closeReview(serverCfg, review.getPermId(), summaryString);
						return null;
					}
				};
				client.execute(summarizeOp);
				client.getReview(getTaskRepository(), getTaskId(), true, monitor);
				TasksUiPlugin.getTaskJobFactory().createSynchronizeRepositoriesJob(
						Collections.singleton(taskRepository)).schedule();
			} catch (CoreException e) {
				throw new InvocationTargetException(e);

			}
		}
	}

	private static final String OTHER_DRAFTS_WARNING = "Warning - Other participants' draft comments will be discarded.";

	private static final String OPEN_REVIEWS_WARNING = "Reviewers not yet finished: ";

	private static final String COMPLETED_REVIEWS_INFO = "Reviewers that have finished this review:";

	private final Review review;

	private Review updatedReview;

	private final String userName;

	private final TaskRepository taskRepository;

	private final String taskKey;

	private final String taskId;

	private final CrucibleClient client;

	private Text summaryText;

	private String summaryString = "";

	private boolean discardDrafts = true;

	public CrucibleSummarizeReviewDialog(Shell parentShell, Review review, String userName, String taskKey,
			String taskId, TaskRepository taskRepository, CrucibleClient client) {
		super(parentShell);
		this.review = review;
		this.userName = userName;
		this.taskKey = taskKey;
		this.taskId = taskId;
		this.taskRepository = taskRepository;
		this.client = client;
	}

	@Override
	protected Control createPageControls(Composite parent) {
		getShell().setText("Summarize and Close");
		setTitle("Summarize and Close Review");
		setMessage("Provide an optional comment.");

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

		new Label(composite, SWT.NONE).setText("Summary Text (optional):");

		summaryText = new Text(composite, SWT.WRAP | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		GridData textGridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
		textGridData.heightHint = 120;
		textGridData.widthHint = 200;
		summaryText.setLayoutData(textGridData);

		handleOpenReviewsAndDrafts(composite);

		return composite;
	}

	private void handleOpenReviewsAndDrafts(Composite composite) {
		boolean hasDrafts = checkForDrafts();
		boolean hasOthersDrafts = checkForOthersDrafts();
		Set<Reviewer> openReviewers = getOpenReviewers();
		Set<Reviewer> completedReviewers = getCompletedReviewers();

		Composite draftComp = new Composite(composite, SWT.NONE);
		GridLayout draftCompLayout = new GridLayout(1, false);
		draftCompLayout.horizontalSpacing = 0;
		draftCompLayout.marginWidth = 0;
		draftComp.setLayout(draftCompLayout);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(draftComp);

		boolean hasCompletedReviewers = false;
		if (completedReviewers.size() > 0) {
			new CrucibleReviewersPart(completedReviewers).createControl(null, draftComp, COMPLETED_REVIEWS_INFO);
			hasCompletedReviewers = true;
		}

		if (openReviewers.size() > 0) {
			if (hasCompletedReviewers) {
				GridDataFactory.fillDefaults().grab(true, false).applyTo(
						new Label(draftComp, SWT.SEPARATOR | SWT.HORIZONTAL));
			}
			new CrucibleReviewersPart(openReviewers).createControl(null, draftComp, OPEN_REVIEWS_WARNING);
			Label labelControl = new Label(draftComp, SWT.WRAP);
			labelControl.setText(OTHER_DRAFTS_WARNING);
			if (hasOthersDrafts) {
				Set<Reviewer> othersDrafts = getOthersDrafts();
				new CrucibleReviewersPart(othersDrafts).createControl(null, draftComp, "Reviewers with draft comments:");
			}
		}

		if (hasDrafts) {
			GridDataFactory.fillDefaults().grab(true, false).applyTo(
					new Label(draftComp, SWT.SEPARATOR | SWT.HORIZONTAL));
			Label draftComments = new Label(draftComp, SWT.NONE);
			draftComments.setText("You have open drafts in this review. Please choose an action:");
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
			deleteDrafts.setSelection(discardDrafts);
		}
	}

	private boolean checkForDrafts() {
		try {
			if ((review.getNumberOfGeneralCommentsDrafts() + review.getNumberOfVersionedCommentsDrafts()) > 0) {
				return true;
			}
		} catch (ValueNotYetInitialized e) {
			return false;
		}
		return false;
	}

	private Set<Reviewer> getOthersDrafts() {
		Set<Reviewer> othersDrafts = new LinkedHashSet<Reviewer>();
		try {
			for (GeneralComment comment : review.getGeneralComments()) {
				checkCommentForDraft(comment, othersDrafts);
			}
		} catch (ValueNotYetInitialized e) {
			//
		}
		try {
			for (CrucibleFileInfo file : review.getFiles()) {
				for (VersionedComment comment : file.getVersionedComments()) {
					checkCommentForDraft(comment, othersDrafts);
				}
			}
		} catch (ValueNotYetInitialized e) {
			// 
		}
		if (othersDrafts.contains(null)) {
			othersDrafts.remove(null);
		}
		return othersDrafts;
	}

	private void checkCommentForDraft(Comment comment, Set<Reviewer> othersDrafts) {
		if (comment.isDraft()) {
			othersDrafts.add(getReviewer(comment.getAuthor()));
		}
		if (!comment.isReply()) {
			for (Comment reply : comment.getReplies()) {
				checkCommentForDraft(reply, othersDrafts);
			}
		}
	}

	private Reviewer getReviewer(User author) {
		try {
			for (Reviewer reviewer : review.getReviewers()) {
				if (reviewer.getUserName().equals(author.getUserName())) {
					return reviewer;
				}
			}
		} catch (ValueNotYetInitialized e) {
			// 
		}
		return null;
	}

	private Set<Reviewer> getOpenReviewers() {
		Set<Reviewer> openReviewers = new LinkedHashSet<Reviewer>();
		try {
			for (Reviewer reviewer : review.getReviewers()) {
				if (!reviewer.isCompleted()) {
					openReviewers.add(reviewer);
				}
			}
		} catch (ValueNotYetInitialized e) {
			//
		}
		return openReviewers;
	}

	private Set<Reviewer> getCompletedReviewers() {
		Set<Reviewer> completedReviewers = new LinkedHashSet<Reviewer>();
		try {
			for (Reviewer reviewer : review.getReviewers()) {
				if (reviewer.isCompleted()) {
					completedReviewers.add(reviewer);
				}
			}
		} catch (ValueNotYetInitialized e) {
			//
		}
		return completedReviewers;
	}

	/**
	 * Wont work yet since API does not seem to make other's drafts available
	 * 
	 * @return
	 */
	private boolean checkForOthersDrafts() {
		try {
			int totalDrafts = review.getNumberOfGeneralCommentsDrafts() + review.getNumberOfVersionedCommentsDrafts();
			int myDrafts = review.getNumberOfGeneralCommentsDrafts(userName)
					+ review.getNumberOfVersionedCommentsDrafts(userName);
			if (totalDrafts > myDrafts) {
				return true;
			}
		} catch (ValueNotYetInitialized e) {
			return false;
		}
		return false;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		Button summarizeButton = createButton(parent, IDialogConstants.CLIENT_ID + 1, "&Summarize and Close", false);
		summarizeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				summarizeReview();
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

	public void summarizeReview() {

		summaryString = summaryText.getText();
		try {
			setMessage("");
			run(true, false, new SummarizeReviewRunnable());
		} catch (InvocationTargetException e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
			setErrorMessage("Unable to summarize the review");
			return;
		} catch (InterruptedException e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
			setErrorMessage("Unable to summarize the review");
			return;
		}

		setReturnCode(Window.OK);
		close();
	}

	public String getTaskKey() {
		return taskKey;
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
}
