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

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleRemoteOperation;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.parts.CrucibleParticipantUiUtil;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
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
public class CrucibleSummarizeReviewDialog extends AbstractCrucibleReviewActionDialog {

	private final class SummarizeReviewRunnable implements IRunnableWithProgress {
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

			monitor.beginTask("Summarize and Close Review", IProgressMonitor.UNKNOWN);

			try {
				if (!discardDrafts) {
					// post all drafts
					CrucibleRemoteOperation<Object> publishDraftsOp = new CrucibleRemoteOperation<Object>(monitor,
							getTaskRepository()) {
						@Override
						public Object run(CrucibleServerFacade2 server, ConnectionCfg serverCfg,
								IProgressMonitor monitor) throws CrucibleLoginException, RemoteApiException,
								ServerPasswordNotProvidedException {
							server.publishAllCommentsForReview(serverCfg, review.getPermId());
							return null;
						}
					};
					client.execute(publishDraftsOp);
					updatedReview = client.getReview(getTaskRepository(), getTaskId(), true, monitor);
				}
				// summarize
				CrucibleRemoteOperation<Object> summarizeOp = new CrucibleRemoteOperation<Object>(monitor,
						getTaskRepository()) {
					@Override
					public Object run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
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

	private Text summaryText;

	private String summaryString = "";

	private ImageRegistry imageRegistry;

	public CrucibleSummarizeReviewDialog(Shell parentShell, Review review, String userName, String taskKey,
			String taskId, TaskRepository taskRepository, CrucibleClient client) {
		super(parentShell, review, userName, taskRepository, taskKey, taskId, client, "&Summarize and Close");
	}

	@Override
	protected Control createPageControls(Composite parent) {
		getShell().setText("Summarize and Close");
		setTitle("Summarize and Close Review");
		setMessage("Provide an optional comment.");
		imageRegistry = new ImageRegistry();
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				imageRegistry.dispose();
			}
		});

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
		boolean hasOthersDrafts = checkForOthersDrafts();
		Set<Reviewer> openReviewers = getOpenReviewers();
		Set<Reviewer> completedReviewers = getCompletedReviewers();

		if (openReviewers.size() > 0) {
			final Composite draftsWarning = new Composite(composite, SWT.NONE);
			draftsWarning.setLayout(new RowLayout());
			Label imageControl = new Label(draftsWarning, SWT.NONE);
			imageControl.setImage(JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING));
			Label labelControl = new Label(draftsWarning, SWT.WRAP);
			labelControl.setText(OTHER_DRAFTS_WARNING);
		}

		Composite draftComp = new Composite(composite, SWT.NONE);
		GridLayout draftCompLayout = new GridLayout(1, false);
		draftCompLayout.horizontalSpacing = 0;
		draftCompLayout.marginWidth = 0;
		draftComp.setLayout(draftCompLayout);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(draftComp);

		boolean hasCompletedReviewers = false;
		if (completedReviewers.size() > 0) {
			new Label(draftComp, SWT.NONE).setText(COMPLETED_REVIEWS_INFO);
			CrucibleParticipantUiUtil.createReviewersListComposite(null, draftComp, completedReviewers, imageRegistry, null);
			hasCompletedReviewers = true;
		}

		if (openReviewers.size() > 0) {
			if (hasCompletedReviewers) {
				GridDataFactory.fillDefaults().grab(true, false).applyTo(
						new Label(draftComp, SWT.SEPARATOR | SWT.HORIZONTAL));
			}

			new Label(draftComp, SWT.NONE).setText(OPEN_REVIEWS_WARNING);
			CrucibleParticipantUiUtil.createReviewersListComposite(null, draftComp, openReviewers, imageRegistry, null);

			if (hasOthersDrafts) {
				final Set<Reviewer> othersDrafts = getOthersDrafts();
				new Label(draftComp, SWT.NONE).setText("Reviewers with draft comments:");
				CrucibleParticipantUiUtil.createReviewersListComposite(null, draftComp, othersDrafts, imageRegistry, null);
			}
		}

		handleUserDrafts(draftComp);
	}

	private Set<Reviewer> getOthersDrafts() {
		Set<Reviewer> othersDrafts = new LinkedHashSet<Reviewer>();
		for (Comment comment : review.getGeneralComments()) {
			checkCommentForDraft(comment, othersDrafts);
		}
		for (CrucibleFileInfo file : review.getFiles()) {
			for (VersionedComment comment : file.getVersionedComments()) {
				checkCommentForDraft(comment, othersDrafts);
			}
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

	/**
	 * Wont work yet since API does not seem to make other's drafts available
	 * 
	 * @return
	 */
	private boolean checkForOthersDrafts() {
		int totalDrafts = review.getNumberOfGeneralCommentsDrafts() + review.getNumberOfVersionedCommentsDrafts();
		int myDrafts = review.getNumberOfGeneralCommentsDrafts(userName)
				+ review.getNumberOfVersionedCommentsDrafts(userName);
		if (totalDrafts > myDrafts) {
			return true;
		}
		return false;
	}

	@Override
	protected void doAction() {

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

}
