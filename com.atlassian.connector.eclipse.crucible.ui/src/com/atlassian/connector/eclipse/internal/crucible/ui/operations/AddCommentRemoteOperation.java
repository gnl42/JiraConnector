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

package com.atlassian.connector.eclipse.internal.crucible.ui.operations;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CustomField;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralCommentBean;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PermIdBean;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.UserBean;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedCommentBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import java.util.HashMap;

/**
 * Operation for adding a comment to a review
 * 
 * @author Shawn Minto
 */
public final class AddCommentRemoteOperation extends CrucibleClient.RemoteOperation<Comment> {

	private HashMap<String, CustomField> customFields = null;

	private Comment parentComment = null;

	private final CrucibleClient client;

	private LineRange commentLines = null;

	private boolean isDraft = false;

	private final CrucibleFile reviewItem;

	private final String message;

	private boolean isDefect = false;

	private final Review review;

	public AddCommentRemoteOperation(TaskRepository repository, Review review, CrucibleClient client,
			CrucibleFile reviewItem, String newComment, IProgressMonitor monitor) {
		super(monitor, repository);
		this.review = review;
		this.client = client;
		this.reviewItem = reviewItem;
		this.message = newComment;
		this.customFields = new HashMap<String, CustomField>();
	}

	public void setDefect(boolean newIsDefect) {
		this.isDefect = newIsDefect;
	}

	public void setDraft(boolean newIsDraft) {
		this.isDraft = newIsDraft;
	}

	public void setCustomFields(HashMap<String, CustomField> customFields) {
		this.customFields = customFields;
	}

	public void setParentComment(Comment parentComment) {
		this.parentComment = parentComment;
	}

	public void setCommentLines(LineRange commentLines) {
		this.commentLines = commentLines;
	}

	private String getTaskId() {
		if (review == null) {
			return null;
		}
		return CrucibleUtil.getTaskIdFromPermId(review.getPermId().getId());
	}

	@Override
	public Comment run(CrucibleServerFacade server, ServerData serverCfg, IProgressMonitor monitor)
			throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {

		if (reviewItem != null) {
			String permId = CrucibleUtil.getPermIdFromTaskId(getTaskId());
			PermId riId = reviewItem.getCrucibleFileInfo().getPermId();
			VersionedCommentBean newComment = createNewVersionedComment();
			newComment.setDefectRaised(isDefect);
			newComment.setDraft(isDraft);
			newComment.getCustomFields().putAll(customFields);

			if (parentComment != null && newComment.isReply()) {
				return server.addVersionedCommentReply(serverCfg, new PermIdBean(permId), parentComment.getPermId(),
						newComment);
			} else {
				return server.addVersionedComment(serverCfg, new PermIdBean(permId), riId, newComment);
			}
		} else {
			GeneralCommentBean newComment = createNewGeneralComment();
			newComment.setDefectRaised(isDefect);
			newComment.setDraft(isDraft);
			newComment.getCustomFields().putAll(customFields);
			String permId = CrucibleUtil.getPermIdFromTaskId(getTaskId());

			if (parentComment != null && newComment.isReply()) {
				return server.addGeneralCommentReply(serverCfg, new PermIdBean(permId), parentComment.getPermId(),
						newComment);
			} else {
				return server.addGeneralComment(serverCfg, new PermIdBean(permId), newComment);
			}
		}
	}

	private GeneralCommentBean createNewGeneralComment() {
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

	private VersionedCommentBean createNewVersionedComment() {
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

		newComment.setAuthor(new UserBean(client.getUserName()));
		newComment.setDraft(isDraft);
		newComment.setMessage(message);
		if (parentComment != null && parentComment instanceof VersionedComment) {
			newComment.setReply(true);
		} else {
			newComment.setReply(false);
		}

		return newComment;
	}
}