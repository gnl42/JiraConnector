package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.util.MiscUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.theplugin.commons.util.MiscUtil.isModified;

/**
 * This class is NOT thread-safe!
 */
public class ReviewDifferenceProducer {
	private final Review oldReview;
	private final Review newReview;
	private final List<CrucibleNotification> notifications = new ArrayList<CrucibleNotification>();
	private boolean shortEqual;
	private boolean filesEqual;
	private int changes;

	public ReviewDifferenceProducer(@NotNull final Review oldReview, @NotNull final Review newReview) {
		this.oldReview = oldReview;
		this.newReview = newReview;
	}

	public boolean isShortEqual() {
		return shortEqual;
	}

	public boolean isFilesEqual() {
		return filesEqual;
	}

	public int getCommentChangesCount() {
		return changes;
	}

	public List<CrucibleNotification> getDiff() {
		notifications.clear();
		if (isModified(oldReview.getDescription(), newReview.getDescription())) {
			notifications.add(new BasisReviewDetailsChangedNotification(newReview,
					CrucibleNotificationType.STATEMENT_OF_OBJECTIVES_CHANGED, "Statement of Objectives has been changed"));
		}
		if (isModified(oldReview.getName(), newReview.getName())) {
			notifications.add(new BasisReviewDetailsChangedNotification(newReview, CrucibleNotificationType.NAME_CHANGED,
					"Review name has been changed"));
		}

		if (isModified(oldReview.getModerator(), newReview.getModerator())) {
			notifications.add(new BasisReviewDetailsChangedNotification(newReview, CrucibleNotificationType.MODERATOR_CHANGED,
					"Moderator has changed"));
		}

		if (isModified(oldReview.getAuthor(), newReview.getAuthor())) {
			notifications.add(new BasisReviewDetailsChangedNotification(newReview, CrucibleNotificationType.AUTHOR_CHANGED,
					"Author has changed"));
		}

		if (isModified(oldReview.getSummary(), newReview.getSummary())) {
			notifications.add(new BasisReviewDetailsChangedNotification(newReview, CrucibleNotificationType.SUMMARY_CHANGED,
					"Summary has been changed"));
		}

		if (isModified(oldReview.getProjectKey(), newReview.getProjectKey())) {
			notifications.add(new BasisReviewDetailsChangedNotification(newReview, CrucibleNotificationType.PROJECT_CHANGED,
					"Project has been changed"));
		}

		if (isModified(oldReview.getDueDate(), newReview.getDueDate())) {
			notifications.add(new BasisReviewDetailsChangedNotification(newReview, CrucibleNotificationType.DUE_DATE_CHANGED,
					"Due date has been changed"));
		}

		processReviewers();

		shortEqual = isShortContentEqual();
		if (!shortEqual) {
			notifications.add(new ReviewDataChangedNotification(newReview));
		}
		filesEqual = areFilesEqual();
		// check comments status
			changes = checkComments(oldReview, newReview, true);

		return notifications;
	}

	private boolean isShortContentEqual() {
		return !stateChanged()
				&& areActionsEqual()
				&& oldReview.isAllowReviewerToJoin() == newReview.isAllowReviewerToJoin()
				&& oldReview.getMetricsVersion() == newReview.getMetricsVersion()
				&& areObjectsEqual(oldReview.getCloseDate(), newReview.getCloseDate())
				&& areObjectsEqual(oldReview.getCreateDate(), newReview.getCreateDate())
				&& areObjectsEqual(oldReview.getCreator(), newReview.getCreator())
				&& areObjectsEqual(oldReview.getParentReview(), newReview.getParentReview())
				&& areObjectsEqual(oldReview.getRepoName(), newReview.getRepoName())
				&& areTransitionsEqual();
	}

	private boolean areFilesEqual() {
		Set<CrucibleFileInfo> l = oldReview.getFiles();
		Set<CrucibleFileInfo> r = newReview.getFiles();

		if (l == null && r == null) {
			return true;
		}
		if (l == null || r == null) {
			return false;
		}
		boolean areFilesEqual = l.equals(r);
		if (!areFilesEqual) {
			for (CrucibleFileInfo crucibleFileInfo : r) {
				if (!l.contains(crucibleFileInfo)) {
					notifications.add(new NewReviewItemNotification(newReview));
				}
			}
			for (CrucibleFileInfo crucibleFileInfo : l) {
				if (!r.contains(crucibleFileInfo)) {
					notifications.add(new RemovedReviewItemNotification(oldReview));
				}
			}
		}
		return areFilesEqual;
	}

	private boolean areActionsEqual() {
		Set<CrucibleAction> l = oldReview.getActions();
		Set<CrucibleAction> r = newReview.getActions();
		return areObjectsEqual(l, r);
	}

	private boolean areTransitionsEqual() {
		Set<CrucibleAction> l = oldReview.getTransitions();
		Set<CrucibleAction> r = newReview.getTransitions();
		return areObjectsEqual(l, r);
	}

	private static <T> boolean areObjectsEqual(T oldReview, T newReview) {
		return MiscUtil.isEqual(oldReview, newReview);
	}

	private boolean stateChanged() {
		if (!MiscUtil.isEqual(oldReview.getState(), newReview.getState())) {
			notifications.add(new ReviewStateChangedNotification(newReview, oldReview.getState()));
			return true;
		}
		return false;
	}

	@Nullable
	private Collection<String> buildReviewerSet(@Nullable Set<Reviewer> reviewers) {
		if (reviewers == null) {
			return null;
		}
		final Set<String> res = new HashSet<String>(reviewers.size() * 2);
		for (Reviewer reviewer : reviewers) {
			res.add(reviewer.getUsername());
		}
		return res;
	}

	private void processReviewers() {
		boolean allCompleted = true;
		boolean atLeastOneChanged = false;

		Set<Reviewer> oldReviewers = oldReview.getReviewers();
		Set<Reviewer> newReviewers = newReview.getReviewers();

		final Collection<String> oldR = buildReviewerSet(oldReviewers);
		final Collection<String> newR = buildReviewerSet(newReviewers);

		if (MiscUtil.isModified(oldR, newR)) {
			notifications.add(new BasisReviewDetailsChangedNotification(newReview, CrucibleNotificationType.REVIEWERS_CHANGED,
					"Reviewers have been changed"));
		}

		if (oldReviewers == null || newReviewers == null) {
			return;
		}

		for (Reviewer reviewer : newReviewers) {
			for (Reviewer oldReviewer : oldReviewers) {
				if (reviewer.getUsername().equals(oldReviewer.getUsername())) {
					if (reviewer.isCompleted() != oldReviewer.isCompleted()) {
						notifications.add(new ReviewerCompletedNotification(newReview, reviewer));
						atLeastOneChanged = true;
					}
				}
			}
			if (!reviewer.isCompleted()) {
				allCompleted = false;
			}
		}
		if (allCompleted && atLeastOneChanged) {
			notifications.add(new ReviewCompletedNotification(newReview));
		}
	}

	private int checkComments(final Review anOldReview, final Review aNewReview,
			final boolean checkFiles) {
		int commentChanges = 0;

		Set<Comment> allOldComments = getAllCommentsRecursively(anOldReview.getGeneralComments());

		Set<Comment> allNewComments = getAllCommentsRecursively(aNewReview.getGeneralComments());

		for (Comment comment : allNewComments) {
			Comment existing = null;
			for (Comment oldComment : allOldComments) {
				if (comment.getPermId().getId().equals(oldComment.getPermId().getId())) {
					existing = oldComment;
					break;
				}
			}

			if ((existing == null)
					|| commentContentsDiffer(existing, comment)
                    || existing.getReadState() != comment.getReadState()) {
                commentChanges++;
				if (existing == null) {
					notifications.add(new NewCommentNotification(aNewReview, comment));
				} else {
                    if (commentContentsDiffer(existing, comment)) {
						notifications.add(new UpdatedCommentNotification(aNewReview, comment, existing.isDraft()));
                    }
                    checkAndNotifyReadUnreadStateChange(aNewReview, comment, existing);
                }
			}
		}

		Set<Comment> deletedGen = getDeletedComments(allOldComments, allNewComments);
		for (Comment gc : deletedGen) {
			commentChanges++;
			notifications.add(new RemovedCommentNotification(aNewReview, gc));
		}

		if (checkFiles) {
			int versionedChanges = 0;
			for (CrucibleFileInfo fileInfo : aNewReview.getFiles()) {
				allNewComments = getAllCommentsRecursively(fileInfo.getVersionedComments());
				for (Comment comment : allNewComments) {
					// PL-2047:
					if (comment == null) {
						continue;
					}
					Comment existing = null;
					for (CrucibleFileInfo oldFile : anOldReview.getFiles()) {
						if (oldFile.getPermId().equals(fileInfo.getPermId())) {
							allOldComments = getAllCommentsRecursively(oldFile.getVersionedComments());
							for (Comment oldComment : allOldComments) {
								// PL-2047: //pstefaniak - I've got no idea why NPE could be thrown here :(
								if (comment.getPermId() == null || oldComment == null || oldComment.getPermId() == null) {
									continue;
								}
								if (comment.getPermId().getId().equals(oldComment.getPermId().getId())) {
									existing = oldComment;
									break;
								}
							}
						}
					}
					if ((existing == null)
							|| commentContentsDiffer(existing, comment)
                            || existing.getReadState() != comment.getReadState()) {
						versionedChanges++;
						if (existing == null) {
							notifications.add(new NewCommentNotification(aNewReview, comment));
						} else {
                            if (commentContentsDiffer(existing, comment)) {
								notifications.add(new UpdatedCommentNotification(aNewReview, comment, existing.isDraft()));
                            }
                            checkAndNotifyReadUnreadStateChange(aNewReview, comment, existing);
                        }
					}
				}
			}

			for (CrucibleFileInfo oldFile : anOldReview.getFiles()) {
				for (CrucibleFileInfo newFile : aNewReview.getFiles()) {
					if (oldFile.getPermId().equals(newFile.getPermId())) {
						Set<Comment> oldVersionedComments = getAllCommentsRecursively(oldFile.getVersionedComments());
						Set<Comment> newVersionedComments = getAllCommentsRecursively(newFile.getVersionedComments());
						Set<Comment> deletedVcs = getDeletedComments(oldVersionedComments, newVersionedComments);
						for (Comment vc : deletedVcs) {
							versionedChanges++;
							notifications.add(new RemovedCommentNotification(aNewReview, vc));
						}
					}
				}
			}

			if (versionedChanges > 0) {
				commentChanges += versionedChanges;
				filesEqual = false;
			}
		}
		return commentChanges;
	}

	@NotNull
	private <T extends Comment> Set<Comment> getAllCommentsRecursively(@NotNull List<T> generalComments) {
		Set<Comment> result = MiscUtil.buildHashSet();
		for (T c : generalComments) {
			result.add(c);
			List<Comment> replies = c.getReplies();
			if (replies != null && replies.size() > 0) {
				result.addAll(getAllCommentsRecursively(replies));
			}
		}
		return result;
	}

	private void checkAndNotifyReadUnreadStateChange(Review aNewReview, Comment comment, Comment existing) {
        if (existing.getReadState() != comment.getReadState()) {
			notifications.add(new CommentReadUnreadStateChangedNotification(aNewReview, comment));
        }
    }

    private boolean commentContentsDiffer(Comment existing, Comment comment) {
        return !existing.getMessage().equals(comment.getMessage())
                || existing.isDefectRaised() != comment.isDefectRaised()
				|| existing.isDraft() != comment.isDraft();
    }

	private <T extends Comment> Set<T> getDeletedComments(Collection<T> org, Collection<T> modified) {
		final Set<T> deletedList = MiscUtil.buildHashSet();

		for (T corg : org) {
			boolean found = false;
			for (T cnew : modified) {
				if (cnew != null && cnew.getPermId() != null && corg != null
                        && cnew.getPermId().equals(corg.getPermId())) {
					found = true;
					break;
				}
			}
			if (!found) {
				deletedList.add(corg);
			}
		}

		return deletedList;
	}
}
