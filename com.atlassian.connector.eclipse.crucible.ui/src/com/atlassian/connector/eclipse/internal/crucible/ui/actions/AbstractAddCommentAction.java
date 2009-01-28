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
import com.atlassian.connector.eclipse.internal.crucible.ui.dialogs.CrucibleReviewReplyDialog;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewChangeJob;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CustomField;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralCommentBean;
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

import java.util.HashMap;

/**
 * Abstract class to deal with adding comments to a review
 * 
 * @author Shawn Minto
 */
public abstract class AbstractAddCommentAction extends BaseSelectionListenerAction implements
		IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow workbenchWindow;

	private Review review;

	protected AbstractAddCommentAction(String text) {
		super(text);
	}

	public void dispose() {
		// ignore

	}

	public void init(IWorkbenchWindow window) {
		this.workbenchWindow = window;

	}

	@Override
	public void run() {
		review = getReview();
		run(this);
	}

	public void run(IAction action) {

		if (review == null) {
			return;
		}

		final LineRange commentLines = getSelectedRange();
		final CrucibleFile reviewItem = getCrucibleFile();
		final Comment parentComment = getParentComment();

		CrucibleReviewReplyDialog commentDialog = new CrucibleReviewReplyDialog(null, review, reviewItem,
				parentComment, commentLines);
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
						submitNewComment(commentLines, reviewItem, parentComment, message, isDraft, isDefect,
								customFields, client, monitor);

						client.getReview(getTaskRepository(), getTaskId(), true, monitor);

						return new Status(IStatus.OK, CrucibleUiPlugin.PLUGIN_ID, "Review was summarized.");
					}

				};
				job.schedule(0L);
			}
		}
	}

	private void submitNewComment(final LineRange commentLines, final CrucibleFile reviewItem,
			final Comment parentComment, final String message, final boolean isDraft, final boolean isDefect,
			final HashMap<String, CustomField> customFields, final CrucibleClient client, IProgressMonitor monitor)
			throws CoreException {
		client.execute(new RemoteOperation<Comment>(monitor) {
			@Override
			public Comment run(CrucibleServerFacade server, CrucibleServerCfg serverCfg, IProgressMonitor monitor)
					throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {

				if (reviewItem != null) {
					String permId = CrucibleUtil.getPermIdFromTaskId(getTaskId());
					PermId riId = reviewItem.getCrucibleFileInfo().getPermId();
					VersionedCommentBean newComment = createNewVersionedComment(parentComment, commentLines,
							reviewItem, message, isDraft, client.getUserName());

					if (parentComment != null && newComment.isReply()) {
						return server.addVersionedCommentReply(serverCfg, new PermIdBean(permId),
								parentComment.getPermId(), newComment);
					} else {
						return server.addVersionedComment(serverCfg, new PermIdBean(permId), riId, newComment);
					}
				} else {
					GeneralCommentBean newComment = createNewGeneralComment(parentComment, message, client);
					newComment.setDefectRaised(isDefect);
					newComment.setDraft(isDraft);
					newComment.getCustomFields().putAll(customFields);
					String permId = CrucibleUtil.getPermIdFromTaskId(getTaskId());

					if (parentComment != null && newComment.isReply()) {
						return server.addGeneralCommentReply(serverCfg, new PermIdBean(permId),
								parentComment.getPermId(), newComment);
					} else {
						return server.addGeneralComment(serverCfg, new PermIdBean(permId), newComment);
					}
				}
			}

		});
	}

	private GeneralCommentBean createNewGeneralComment(final Comment parentComment, final String message,
			final CrucibleClient client) {
		GeneralCommentBean newComment = new GeneralCommentBean();
		newComment.setMessage(message);
		if (parentComment != null && parentComment instanceof GeneralComment) {
			newComment.setReply(true);
		} else {
			newComment.setReply(false);
		}
		newComment.setAuthor(new UserBean(client.getUserName()));
		return newComment;
	}

	private VersionedCommentBean createNewVersionedComment(Comment parentComment, LineRange commentLines,
			CrucibleFile reviewItem, String message, boolean isDraft, String userName) {
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
		if (parentComment != null && parentComment instanceof VersionedComment) {
			newComment.setReply(true);
		} else {
			newComment.setReply(false);
		}

		return newComment;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		review = getReview();
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

	protected abstract CrucibleFile getCrucibleFile();

	protected abstract LineRange getSelectedRange();

	protected abstract Review getReview();

	protected Comment getParentComment() {
		return null;
	}

	private TaskRepository getTaskRepository() {
		if (review == null) {
			return null;
		}
		ReviewBean activeReviewBean = (ReviewBean) review;
		String serverUrl = activeReviewBean.getServerUrl();

		return TasksUi.getRepositoryManager().getRepository(CrucibleCorePlugin.CONNECTOR_KIND, serverUrl);
	}

	private String getTaskId() {
		if (review == null) {
			return null;
		}
		return CrucibleUtil.getTaskIdFromPermId(review.getPermId().getId());
	}

	private String getTaskKey() {
		if (review == null) {
			return null;
		}
		return review.getPermId().getId();
	}
}
