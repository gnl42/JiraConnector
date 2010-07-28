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

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleRemoteOperation;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CustomField;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import java.util.HashMap;

/**
 * Operation for adding a comment to a review
 * 
 * @author Shawn Minto
 */
public final class AddCommentRemoteOperation extends CrucibleRemoteOperation<Comment> {

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

	@Override
	public Comment run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
			throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {

		if (parentComment != null) {
			// replies are always general comments
			GeneralComment newComment = createNewGeneralComment(parentComment);
			newComment.setDefectRaised(isDefect);
			newComment.setDraft(isDraft);
			newComment.getCustomFields().putAll(customFields);

			return server.addReply(serverCfg, newComment);
		} else if (reviewItem != null) {
			PermId riId = reviewItem.getCrucibleFileInfo().getPermId();
			VersionedComment newComment = createNewVersionedComment(reviewItem.getCrucibleFileInfo());
			newComment.setDefectRaised(isDefect);
			newComment.setDraft(isDraft);
			newComment.getCustomFields().putAll(customFields);

			if (parentComment != null && newComment.isReply()) {
				return server.addReply(serverCfg, newComment);
			} else {
				return server.addVersionedComment(serverCfg, review, riId, newComment);
			}
		} else {
			GeneralComment newComment = createNewGeneralComment(null);
			newComment.setDefectRaised(isDefect);
			newComment.setDraft(isDraft);
			newComment.getCustomFields().putAll(customFields);
			return server.addGeneralComment(serverCfg, review, newComment);
		}
	}

	private GeneralComment createNewGeneralComment(Comment aParentComment) {
		GeneralComment newComment = new GeneralComment(review, aParentComment);
		newComment.setMessage(message);
		if (aParentComment != null && aParentComment instanceof Comment) {
			newComment.setReply(true);
		} else {
			newComment.setReply(false);
		}
		newComment.setAuthor(new User(client.getUsername()));
		return newComment;
	}

	private VersionedComment createNewVersionedComment(CrucibleFileInfo crucibleFileInfo) {
		VersionedComment newComment = new VersionedComment(review, crucibleFileInfo);

		if (commentLines != null) {
			if (reviewItem.isOldFile()) {
				newComment.setFromStartLine(commentLines.getStartLine());
				newComment.setFromEndLine(commentLines.getStartLine() + commentLines.getNumberOfLines() - 1);
				newComment.setFromLineInfo(true);
				newComment.setToLineInfo(false);
			} else {
				newComment.setToStartLine(commentLines.getStartLine());
				newComment.setToEndLine(commentLines.getStartLine() + commentLines.getNumberOfLines() - 1);
				newComment.setFromLineInfo(false);
				newComment.setToLineInfo(true);
			}
		} else {
			newComment.setFromLineInfo(false);
			newComment.setToLineInfo(false);
		}

		newComment.setAuthor(new User(client.getUsername()));
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