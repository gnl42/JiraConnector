package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

public class RemovedCommentNotification extends AbstractCommentNotification {

	public RemovedCommentNotification(final Review review, final Comment comment) {
		super(review, comment);
	}

	@Override
	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.REMOVED_COMMENT;
	}

	@Override
	public String getPresentationMessage() {
		return "Comment removed by " + getComment().getAuthor().getDisplayName();
	}
}
