package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

/**
 * User: kalamon
 * Date: Aug 7, 2009
 * Time: 11:24:31 AM
 */
public class CommentReadUnreadStateChangedNotification extends AbstractCommentNotification {

	public CommentReadUnreadStateChangedNotification(final Review review, final Comment comment) {
		super(review, comment);
    }

    @Override
	public CrucibleNotificationType getType() {
        return CrucibleNotificationType.COMMENT_READ_UNREAD_STATE_CHANGED;
    }

    @Override
	public String getPresentationMessage() {
		return "Comment state changed to " + getComment().getReadState();
    }
}
