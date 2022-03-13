package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

public abstract class AbstractUpdatedCommentNotification extends AbstractCommentNotification {
	private final boolean wasDraft;

	public AbstractUpdatedCommentNotification(final Review review, final Comment comment, final boolean wasDraft) {
		super(review, comment);
		this.wasDraft = wasDraft;
	}

	public boolean wasDraft() {
		return wasDraft;
	}
}
