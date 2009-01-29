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
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient.RemoteOperation;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.dialogs.CrucibleSummarizeReviewDialog;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewChangeJob;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

/**
 * @author Thomas Ehrnhoefer
 */
public class SummarizeReviewAction extends BaseSelectionListenerAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow workbenchWindow;

	private Review review;

	public SummarizeReviewAction(Review review, String text) {
		super(text);
		this.review = review;
	}

	public void dispose() {
		// ignore
	}

	public void init(IWorkbenchWindow window) {
		this.workbenchWindow = window;
	}

	@Override
	public void run() {
		run(this);
	}

	public void run(IAction action) {
		if (review == null) {
			return;
		}

		CrucibleRepositoryConnector connector = CrucibleCorePlugin.getRepositoryConnector();
		CrucibleClient client = connector.getClientManager().getClient(getTaskRepository());
		if (client == null) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
					"Unable to get client, please try to refresh"));
			return;
		}

		CrucibleSummarizeReviewDialog summarizeDialog = new CrucibleSummarizeReviewDialog(null, review,
				client.getUserName());
		if (summarizeDialog.open() == Window.OK) {
			if (summarizeDialog.getSummarizeText().length() > 0) {
				//store own drafts
				final boolean discardDrafts = summarizeDialog.isDiscardDrafts();
				final String summarizeText = summarizeDialog.getSummarizeText();
				CrucibleReviewChangeJob job = new CrucibleReviewChangeJob("Summarize Review" + getTaskKey(),
						getTaskRepository()) {

					@Override
					protected IStatus execute(final CrucibleClient client, IProgressMonitor monitor)
							throws CoreException {
						if (!discardDrafts) {
							//post all drafts
							RemoteOperation<Object> publishDraftsOp = new RemoteOperation<Object>(monitor) {
								@Override
								public Object run(CrucibleServerFacade server, CrucibleServerCfg serverCfg,
										IProgressMonitor monitor) throws CrucibleLoginException, RemoteApiException,
										ServerPasswordNotProvidedException {
									server.publishAllCommentsForReview(serverCfg, review.getPermId());
									return null;
								}
							};
							client.execute(publishDraftsOp);
							review = client.getReview(getTaskRepository(), getTaskKey(), true, monitor);
						}
						//summarize
						RemoteOperation<Object> summarizeOp = new RemoteOperation<Object>(monitor) {
							@Override
							public Object run(CrucibleServerFacade server, CrucibleServerCfg serverCfg,
									IProgressMonitor monitor) throws CrucibleLoginException, RemoteApiException,
									ServerPasswordNotProvidedException {
								// ignore
								server.summarizeReview(serverCfg, review.getPermId());
								server.closeReview(serverCfg, review.getPermId(), summarizeText);
								return null;
							}
						};
						client.execute(summarizeOp);
						client.getReview(getTaskRepository(), getTaskKey(), true, monitor);
						return new Status(IStatus.OK, CrucibleUiPlugin.PLUGIN_ID, "Review was summarized.");
					}
				};
				job.schedule(0L);
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (review != null) {
			action.setEnabled(true);
			setEnabled(true);
		} else {
			action.setEnabled(false);
			setEnabled(false);
		}
	}

	protected IEditorPart getActiveEditor() {
		IWorkbenchWindow window = workbenchWindow;
		if (window == null) {
			window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		}
		if (window != null && window.getActivePage() != null) {
			return window.getActivePage().getActiveEditor();
		}
		return null;
	}

	protected IEditorInput getEditorInputFromSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = ((IStructuredSelection) selection);
			if (structuredSelection.getFirstElement() instanceof IEditorInput) {
				return (IEditorInput) structuredSelection.getFirstElement();
			}
		}
		return null;
	}

	private String getTaskKey() {
		if (review == null) {
			return null;
		}
		return review.getPermId().getId();
	}

	private TaskRepository getTaskRepository() {
		if (review == null) {
			return null;
		}
		ReviewBean activeReviewBean = (ReviewBean) review;
		String serverUrl = activeReviewBean.getServerUrl();

		return TasksUi.getRepositoryManager().getRepository(CrucibleCorePlugin.CONNECTOR_KIND, serverUrl);
	}

}
