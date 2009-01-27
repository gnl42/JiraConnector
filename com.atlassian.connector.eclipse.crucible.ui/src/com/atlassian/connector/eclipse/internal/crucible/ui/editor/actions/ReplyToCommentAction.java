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

package com.atlassian.connector.eclipse.internal.crucible.ui.editor.actions;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient.RemoteOperation;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewChangeJob;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralCommentBean;
import com.atlassian.theplugin.commons.crucible.api.model.PermIdBean;
import com.atlassian.theplugin.commons.crucible.api.model.UserBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;

/**
 * Action to reply to a comment
 * 
 * @author Shawn Minto
 * @author Thomas Ehrnhoefer
 */
public class ReplyToCommentAction extends Action {
	private final Comment comment;

	private final ITask task;

	private final TaskRepository taskRepository;

	private String reply;

	public ReplyToCommentAction(Comment comment, ITask task, TaskRepository taskRepository) {
		this.comment = comment;
		this.task = task;
		this.taskRepository = taskRepository;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return TasksUiImages.COMMENT_REPLY;
	}

	@Override
	public String getToolTipText() {
		return "Reply";
	}

	@Override
	public void run() {
		InputDialog replyDialog = new InputDialog(null, "Reply to Comment", "Reply to " + "\n\"" + comment + "\'",
				reply == null ? "" : reply, null);
		if (replyDialog.open() == Window.OK) {
			reply = replyDialog.getValue();
			if (reply.length() > 0) {
				CrucibleReviewChangeJob job = new CrucibleReviewChangeJob("Reply to Comment " + comment.getPermId(),
						taskRepository) {
					@Override
					protected IStatus execute(final CrucibleClient client, IProgressMonitor monitor)
							throws CoreException {
						client.execute(new RemoteOperation<GeneralComment>(monitor) {
							@Override
							public GeneralComment run(CrucibleServerFacade server, CrucibleServerCfg serverCfg,
									IProgressMonitor monitor) throws CrucibleLoginException, RemoteApiException,
									ServerPasswordNotProvidedException {
								GeneralCommentBean replyBean = new GeneralCommentBean();
								replyBean.setMessage(reply);
								replyBean.setReply(true);
								replyBean.setAuthor(new UserBean(client.getUserName()));
								String permId = CrucibleUtil.getPermIdFromTaskId(task.getTaskId());
								return server.addGeneralCommentReply(serverCfg, new PermIdBean(permId),
										comment.getPermId(), replyBean);
							}

						});
						//TE: this triggers the editor ("incoming changes") display...not sure if that's the way to go though
						client.getReview(taskRepository, task.getTaskId(), true, monitor);
						return new Status(IStatus.OK, CrucibleUiPlugin.PLUGIN_ID, "General Comment was added");
					}
				};
				job.schedule();
			}
		}

	}
}