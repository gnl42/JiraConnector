package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

public abstract class AbstractCommentNotification extends AbstractReviewNotification {
	private final Comment comment;

	public AbstractCommentNotification(final Review review, final Comment comment) {
		super(review);

		this.comment = comment;
	}

	public Comment getComment() {
		return comment;
	}

}
