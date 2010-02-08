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
import com.atlassian.connector.commons.crucible.api.model.ByReviewerDisplayNameComparator;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleRemoteOperation;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.MiscUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

public class CrucibleCompleteReviewDialog extends AbstractCrucibleReviewActionDialog {

	private final class PublishAllDraftsReviewRunnable implements IRunnableWithProgress {
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

			monitor.beginTask("Complete Review", IProgressMonitor.UNKNOWN);

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
				}
				// complete
				final CrucibleRemoteOperation<Object> completeOp = new CrucibleRemoteOperation<Object>(monitor,
						getTaskRepository()) {
					@Override
					public Object run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
							throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
						server.completeReview(serverCfg, review.getPermId(), true);
						return null;
					}
				};
				client.execute(completeOp);

				updatedReview = client.getReview(getTaskRepository(), getTaskId(), true, monitor);
				TasksUiPlugin.getTaskJobFactory().createSynchronizeRepositoriesJob(
						Collections.singleton(taskRepository)).schedule();
			} catch (CoreException e) {
				throw new InvocationTargetException(e);

			}
		}
	}

	private static final String INCOMPLETED_REVIEWERS_WARNING = "Reviewers not yet finished: ";

	private static final String COMPLETED_REVIEWERS_INFO = "Reviewers that have finished this review:";

	public CrucibleCompleteReviewDialog(Shell parentShell, Review review, String userName, String taskKey,
			String taskId, TaskRepository taskRepository, CrucibleClient client) {
		super(parentShell, review, userName, taskRepository, taskKey, taskId, client, "&Complete");
	}

	@Override
	protected Control createPageControls(Composite parent) {
		getShell().setText("Complete Review");
		setTitle("Complete Review");
		// setMessage("Provide an optional comment.");

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

		handleOpenReviewsAndDrafts(composite);

		return composite;
	}

	private String getSortedReviewersAsString(Set<Reviewer> reviewers) {
		final ArrayList<Reviewer> res = MiscUtil.buildArrayList(reviewers);
		Collections.sort(res, new ByReviewerDisplayNameComparator());
		StringBuilder builder = new StringBuilder();
		for (Iterator<Reviewer> it = res.iterator(); it.hasNext();) {
			final Reviewer reviewer = it.next();
			builder.append(reviewer.getDisplayName());
			if (it.hasNext()) {
				builder.append(", ");
			}
		}
		return builder.toString();
	}

	private void handleOpenReviewsAndDrafts(Composite composite) {
		Set<Reviewer> openReviewers = getOpenReviewers();
		Set<Reviewer> completedReviewers = getCompletedReviewers();
		String currentUser = CrucibleUiUtil.getCurrentUsername(review);
		// no point to list myself in open reviewers list
		for (Iterator<Reviewer> it = openReviewers.iterator(); it.hasNext();) {
			final Reviewer reviewer = it.next();
			if (currentUser.equals(reviewer.getUsername())) {
				it.remove();
			}
		}

		Composite draftComp = new Composite(composite, SWT.NONE);
		GridLayout draftCompLayout = new GridLayout(1, false);
		draftCompLayout.horizontalSpacing = 0;
		draftCompLayout.marginWidth = 0;
		draftComp.setLayout(draftCompLayout);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(draftComp);

		final boolean hasCompletedReviewers = completedReviewers.size() > 0;
		if (hasCompletedReviewers) {
			final Label label = new Label(draftComp, SWT.NONE);
			label.setText(COMPLETED_REVIEWERS_INFO);
			final Label label2 = new Label(draftComp, SWT.WRAP);
			GridDataFactory.fillDefaults().grab(true, false).hint(600, SWT.DEFAULT).applyTo(label2);
			label2.setText(getSortedReviewersAsString(completedReviewers));
		}

		if (openReviewers.size() > 0) {
			if (hasCompletedReviewers) {
				GridDataFactory.fillDefaults().grab(true, false).applyTo(
						new Label(draftComp, SWT.SEPARATOR | SWT.HORIZONTAL));
			}
			final Label label = new Label(draftComp, SWT.NONE);
			label.setText(INCOMPLETED_REVIEWERS_WARNING);
			final Label label2 = new Label(draftComp, SWT.WRAP);
			GridDataFactory.fillDefaults().grab(true, false).hint(600, SWT.DEFAULT).applyTo(label2);
			label2.setText(getSortedReviewersAsString(openReviewers));
		}
		handleUserDrafts(draftComp);
	}

	@Override
	protected void doAction() {
		try {
			setMessage("");
			run(true, false, new PublishAllDraftsReviewRunnable());
		} catch (InvocationTargetException e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
			setErrorMessage("Unable to complete the review");
			return;
		} catch (InterruptedException e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
			setErrorMessage("Unable to complete the review");
			return;
		}

		setReturnCode(Window.OK);
		close();
	}
}
