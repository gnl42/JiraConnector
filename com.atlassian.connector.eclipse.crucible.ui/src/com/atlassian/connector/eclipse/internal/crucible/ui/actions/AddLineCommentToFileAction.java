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
import com.atlassian.connector.eclipse.ui.team.TeamUiUtils;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PermIdBean;
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
 * Action for adding a comment to a line in the active review
 * 
 * @author Shawn Minto
 */
public class AddLineCommentToFileAction extends BaseSelectionListenerAction implements IWorkbenchWindowActionDelegate {

	private LineRange selectedRange = null;

	private CrucibleFile crucibleFile = null;

	private IWorkbenchWindow workbenchWindow;

	public AddLineCommentToFileAction() {
		super("Create Comment");
	}

	public void dispose() {
		// ignore

	}

	public void init(IWorkbenchWindow window) {
		this.workbenchWindow = window;

	}

	public void run(IAction action) {
		final LineRange commentLines = selectedRange;
		final CrucibleFile reviewItem = crucibleFile;

		InputDialog replyDialog = new InputDialog(null, "Add Comment", "", "", null);
		if (replyDialog.open() == Window.OK) {
			if (replyDialog.getValue().length() > 0) {
				final String message = replyDialog.getValue();
				final boolean isDraft = false;

				CrucibleReviewChangeJob job = new CrucibleReviewChangeJob(
						"Summarizing Crucible Review " + getTaskKey(), getTaskRepository()) {
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

		newComment.setAuthor(new UserBean(userName));
		newComment.setDraft(isDraft);
		newComment.setMessage(message);
		newComment.setReply(false);

		return newComment;
	}

	private TaskRepository getTaskRepository() {
		ReviewBean activeReview = (ReviewBean) CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
		String serverUrl = activeReview.getServerUrl();

		return TasksUi.getRepositoryManager().getRepository(CrucibleCorePlugin.CONNECTOR_KIND, serverUrl);
	}

	private String getTaskId() {
		// ignore
		return CrucibleUtil.getTaskIdFromPermId(CrucibleUiPlugin.getDefault()
				.getActiveReviewManager()
				.getActiveReview()
				.getPermId()
				.getId());
	}

	private String getTaskKey() {
		// ignore
		return CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview().getPermId().getId();
	}

	public void selectionChanged(IAction action, ISelection selection) {

		IEditorPart editorPart = getActiveEditor();
		IEditorInput editorInput = getEditorInputFromSelection(selection);
		if (editorInput != null && editorPart != null) {
			selectedRange = TeamUiUtils.getSelectedLineNumberRangeFromEditorInput(editorPart, editorInput);

			if (selectedRange != null) {
				crucibleFile = TeamUiUtils.getCorrespondingCrucibleFileFromEditorInput(editorInput,
						CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview());
				if (crucibleFile != null) {
					action.setEnabled(true);
					setEnabled(true);
					return;
				}
			}
		}
		action.setEnabled(false);
		setEnabled(false);
		selectedRange = null;
		crucibleFile = null;

	}

	private IEditorPart getActiveEditor() {
		IWorkbenchWindow window = workbenchWindow;
		if (window == null) {
			window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		}
		if (window != null && window.getActivePage() != null) {
			return window.getActivePage().getActiveEditor();
		}
		return null;
	}

	private IEditorInput getEditorInputFromSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = ((IStructuredSelection) selection);
			if (structuredSelection.getFirstElement() instanceof IEditorInput) {
				return (IEditorInput) structuredSelection.getFirstElement();
			}
		}
		return null;
	}

}
