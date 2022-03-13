package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

public class UpdatedCommentNotification extends AbstractUpdatedCommentNotification {
	public UpdatedCommentNotification(final Review review, final Comment comment, final boolean wasDraft) {
		super(review, comment, wasDraft);
	}

	@Override
	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.UPDATED_COMMENT;
	}

	@Override
	public String getPresentationMessage() {
		return "Comment updated by " + getComment().getAuthor().getDisplayName();
	}
}
