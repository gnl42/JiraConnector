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
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient.RemoteOperation;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewChangeJob;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PermIdBean;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewBean;
import com.atlassian.theplugin.commons.crucible.api.model.UserBean;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedCommentBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

/**
 * Abstract class to deal with adding comments to a review
 * 
 * @author Shawn Minto
 */
public abstract class AbstractAddCommentToFileAction extends BaseSelectionListenerAction implements
		IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow workbenchWindow;

	private Review activeReview;

	protected AbstractAddCommentToFileAction(String text) {
		super(text);
	}

	public void dispose() {
		// ignore

	}

	public void init(IWorkbenchWindow window) {
		this.workbenchWindow = window;

	}

	public void run(IAction action) {

		if (activeReview == null) {
			return;
		}

		final LineRange commentLines = getSelectedRange();
		final CrucibleFile reviewItem = getCrucibleFile();

		InputDialog replyDialog = new InputDialog(null, "Add Comment", "", "", null);
		if (replyDialog.open() == Window.OK) {
			if (replyDialog.getValue().length() > 0) {
				final String message = replyDialog.getValue();
				final boolean isDraft = false;

				CrucibleReviewChangeJob job = new CrucibleReviewChangeJob("Submitting comment" + getTaskKey(),
						getTaskRepository()) {
					@Override
					protected IStatus execute(final CrucibleClient client, IProgressMonitor monitor)
							throws CoreException {
						client.execute(new RemoteOperation<VersionedComment>(monitor) {
							@Override
							public VersionedComment run(CrucibleServerFacade server, CrucibleServerCfg serverCfg,
									IProgressMonitor monitor) throws CrucibleLoginException, RemoteApiException,
									ServerPasswordNotProvidedException {

								String permId = CrucibleUtil.getPermIdFromTaskId(getTaskId());

								PermId riId = reviewItem.getCrucibleFileInfo().getPermId();

								VersionedCommentBean newComment = createNewVersionedComment(commentLines, reviewItem,
										message, isDraft, client.getUserName());

								return server.addVersionedComment(serverCfg, new PermIdBean(permId), riId, newComment);

							}

						});

						client.getReview(getTaskRepository(), getTaskId(), true, monitor);

						return new Status(IStatus.OK, CrucibleUiPlugin.PLUGIN_ID, "Review was summarized.");
					}
				};
				job.schedule(0L);
			}
		}
	}

	private VersionedCommentBean createNewVersionedComment(LineRange commentLines, CrucibleFile reviewItem,
			String message, boolean isDraft, String userName) {
		VersionedCommentBean newComment = new VersionedCommentBean();

		if (commentLines != null) {
			if (reviewItem.isOldFile()) {
				newComment.setFromStartLine(commentLines.getStartLine());
				newComment.setFromEndLine(commentLines.getStartLine() + commentLines.getNumberOfLines());
				newComment.setFromLineInfo(true);
				newComment.setToLineInfo(false);
			} else {
				newComment.setToStartLine(commentLines.getStartLine());
				newComment.setToEndLine(commentLines.getStartLine() + commentLines.getNumberOfLines());
				newComment.setFromLineInfo(false);
				newComment.setToLineInfo(true);
			}
		} else {
			newComment.setFromLineInfo(false);
			newComment.setToLineInfo(false);
		}

		newComment.setAuthor(new UserBean(userName));
		newComment.setDraft(isDraft);
		newComment.setMessage(message);
		newComment.setReply(false);

		return newComment;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		activeReview = CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
		if (activeReview != null) {
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

	protected abstract CrucibleFile getCrucibleFile();

	protected abstract LineRange getSelectedRange();

	private TaskRepository getTaskRepository() {
		if (activeReview == null) {
			return null;
		}
		ReviewBean activeReviewBean = (ReviewBean) activeReview;
		String serverUrl = activeReviewBean.getServerUrl();

		return TasksUi.getRepositoryManager().getRepository(CrucibleCorePlugin.CONNECTOR_KIND, serverUrl);
	}

	private String getTaskId() {
		if (activeReview == null) {
			return null;
		}
		return CrucibleUtil.getTaskIdFromPermId(activeReview.getPermId().getId());
	}

	private String getTaskKey() {
		if (activeReview == null) {
			return null;
		}
		return activeReview.getPermId().getId();
	}
}
