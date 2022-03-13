package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewTestUtil;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.util.MiscUtil;
import org.joda.time.DateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import junit.framework.TestCase;

public class ReviewDifferenceProducerTest extends TestCase {

	final PermId reviewId1 = new PermId("CR-1");

	final PermId newItem1 = new PermId("CRF:11");

	final PermId newCommentId1 = new PermId("CMT:11");

	final PermId newVCommentId1 = new PermId("CMT:12");

	final PermId reviewId2 = new PermId("CR-2");

	final PermId newItem2 = new PermId("CRF:21");

	final PermId newCommentId2 = new PermId("CMT:21");

	final PermId newVCommentId2 = new PermId("CMT:22");

	final Reviewer reviewer3 = prepareReviewer("scott", "Scott", false);

	final Reviewer reviewer4 = prepareReviewer("alice", "Alice", false);

	private final Review review = ReviewTestUtil.createReview("myurl.com");

	private Review prepareReview() {
		final Review res = new Review("http://bogus", "TEST", reviewer3, reviewer4);
		res.setFiles(Collections.<CrucibleFileInfo> emptySet());
		res.setGeneralComments(Collections.<Comment> emptyList());
		return res;
	}

	private Reviewer prepareReviewer(String userName, String displayName, boolean completed) {
		return new Reviewer(userName, displayName, completed);
	}

	private Comment prepareGeneralComment(Comment parentComment, final String message, final PermId permId, final Date date,
			final Comment reply) {
		GeneralComment bean = new GeneralComment(review, parentComment);
		bean.setMessage(message);
		bean.setPermId(permId);
		bean.setCreateDate(date);
		if (reply != null) {
			bean.getReplies().add(reply);
		}

		return bean;
	}

	private VersionedComment prepareVersionedComment(final String message, final PermId permId, final Date date,
			final VersionedComment reply, CrucibleFileInfo parentFileInfo) {
		VersionedComment bean = new VersionedComment(review, parentFileInfo);
		bean.setMessage(message);
		bean.setPermId(permId);
		bean.setCreateDate(date);
		if (reply != null) {
			bean.getReplies().add(reply);
		}

		return bean;
	}

	private CrucibleFileInfo prepareCrucibleFileInfo(final PermId permId, final Date date) {
		VersionedVirtualFile oldFileInfo = new VersionedVirtualFile("http://old.file", "1.1");
		VersionedVirtualFile newFileInfo = new VersionedVirtualFile("http://new.file", "1.3");
		CrucibleFileInfo bean = new CrucibleFileInfo(newFileInfo, oldFileInfo, permId);
		bean.setCommitDate(date);
		return bean;
	}

	private Review prepareReview1(State state, Date commentsDate) {
		Review review1 = prepareReview();
		review1.setGeneralComments(new ArrayList<Comment>());
		review1.setPermId(reviewId1);
		review1.setState(state);
		review1.setReviewers(MiscUtil.<Reviewer> buildHashSet(prepareReviewer("bob", "Bob", false), prepareReviewer(
				"alice", "Alice", false)));
		review1.getGeneralComments().add(prepareGeneralComment(null, "message", newCommentId1, commentsDate, null));
		review1.getGeneralComments().add(prepareGeneralComment(null, "message2", newCommentId2, commentsDate, null));

		CrucibleFileInfo file1 = prepareCrucibleFileInfo(newItem1, commentsDate);
		CrucibleFileInfo file2 = prepareCrucibleFileInfo(newItem2, commentsDate);

		List<VersionedComment> vComments = new ArrayList<VersionedComment>();
		vComments.add(prepareVersionedComment("versionedMessage", newVCommentId1, commentsDate, null, file1));
		vComments.add(prepareVersionedComment("versionedMessage2", newVCommentId2, commentsDate, null, file1));
		file1.setVersionedComments(vComments);

		List<VersionedComment> vComments2 = new ArrayList<VersionedComment>();
		vComments2.add(prepareVersionedComment("versionedMessage", newVCommentId1, commentsDate, null, file2));
		vComments2.add(prepareVersionedComment("versionedMessage2", newVCommentId2, commentsDate, null, file2));
		file2.setVersionedComments(vComments2);

		Set<CrucibleFileInfo> files1 = new HashSet<CrucibleFileInfo>();
		files1.add(file1);
		files1.add(file2);

		review1.setFilesAndVersionedComments(files1, null);

		return review1;
		// return new ReviewAdapter(review1, new ServerData(
		// new CrucibleServerCfg("Name", new ServerIdImpl()), new UserCfg("", "", false)));
	}

	public void testSameReviewsWithoutFiles() {
		Date commentDate = new Date();
		Review review = prepareReview1(State.REVIEW, commentDate);
		review.getFiles().clear();

		Review review1 = prepareReview1(State.REVIEW, commentDate);

		// test the same review - empty files collection
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(0, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());

		// test the same review - no files and versioned comments
		review.setFilesAndVersionedComments(Collections.<CrucibleFileInfo> emptyList(),
				Collections.<VersionedComment> emptyList());
		p = new ReviewDifferenceProducer(review, review);
		notifications = p.getDiff();

		assertEquals(0, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());

		// test the same content - one review empty collection and the second empty collection too
		review1.getFiles().clear();

		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();
		assertEquals(0, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
	}

	public void testSameReviews() {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		Review review = prepareReview1(State.REVIEW, commentDate);

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(0, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
	}

	public void testSameGeneralComments() {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		Review review = prepareReview1(State.REVIEW, commentDate);

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(0, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
	}

	public void testEmptyGeneralComments() {
		Date commentDate = new Date();
		Review review = prepareReview1(State.REVIEW, commentDate);
		Review review1 = prepareReview1(State.REVIEW, commentDate);
		review.setGeneralComments(Collections.<Comment> emptyList());

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(2, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());

		review1.setGeneralComments(Collections.<Comment> emptyList());

		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();

		assertEquals(0, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
	}

	public void testAddedGeneralComment() {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		Review review = prepareReview1(State.REVIEW, commentDate);
		Review review1 = prepareReview1(State.REVIEW, commentDate);

		// reset general for first review
		review.getGeneralComments().clear();
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(2, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(2, p.getCommentChangesCount());
		assertEquals(CrucibleNotificationType.NEW_COMMENT, notifications.get(0).getType());
		assertEquals(CrucibleNotificationType.NEW_COMMENT, notifications.get(1).getType());
	}

	public void testReviewItemAdded() {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		Review review = prepareReview1(State.REVIEW, commentDate);
		Review review1 = prepareReview1(State.REVIEW, commentDate);
		review.getFiles().clear();

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(6, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.NEW_REVIEW_ITEM, notifications.get(0).getType());
		assertEquals(CrucibleNotificationType.NEW_REVIEW_ITEM, notifications.get(1).getType());
		assertEquals(CrucibleNotificationType.NEW_COMMENT, notifications.get(2).getType());
		assertEquals(CrucibleNotificationType.NEW_COMMENT, notifications.get(3).getType());
		assertEquals(CrucibleNotificationType.NEW_COMMENT, notifications.get(4).getType());
		assertEquals(CrucibleNotificationType.NEW_COMMENT, notifications.get(5).getType());
	}

	public void testReviewItemRemoved() {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		Review review = prepareReview1(State.REVIEW, commentDate);
		Review review1 = prepareReview1(State.REVIEW, commentDate);
		review1.getFiles().clear();

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(2, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REMOVED_REVIEW_ITEM, notifications.get(0).getType());
		assertEquals(CrucibleNotificationType.REMOVED_REVIEW_ITEM, notifications.get(1).getType());
	}

	public void testAddedGeneralCommentReply() {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		Review review = prepareReview1(State.REVIEW, commentDate);
		Review review1 = prepareReview1(State.REVIEW, commentDate);
		review1.getGeneralComments().get(0).getReplies().add(
				prepareGeneralComment(review1.getGeneralComments().get(0), "reply", new PermId("CMT:41"), new Date(), null));

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.NEW_COMMENT, notifications.get(0).getType());
	}

	public void testUpdatedGeneralCommentReply() {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		Review review1 = prepareReview1(State.REVIEW, commentDate);
		Review review2 = prepareReview1(State.REVIEW, commentDate);
		Date replyDate = new Date();
		final Comment comment1 = review1.getGeneralComments().get(0);
		comment1.getReplies().add(prepareGeneralComment(comment1, "reply", new PermId("CMT:41"), replyDate, null));
		comment1.getReplies().add(prepareGeneralComment(comment1, "reply2", new PermId("CMT:42"), replyDate, null));
		final Comment comment2 = review2.getGeneralComments().get(0);
		comment2.getReplies().add(prepareGeneralComment(comment2, "reply", new PermId("CMT:41"), replyDate, null));
		comment2.getReplies().add(prepareGeneralComment(comment2, "reply2", new PermId("CMT:42"), replyDate, null));

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review1, review2);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(0, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());

		((GeneralComment) comment2.getReplies().get(0)).setMessage("new reply message");

		p = new ReviewDifferenceProducer(review1, review2);
		notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.UPDATED_COMMENT, notifications.get(0).getType());
	}

	public void testRemovedGeneralCommentReply() {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		Review review = prepareReview1(State.REVIEW, commentDate);
		Review review1 = prepareReview1(State.REVIEW, commentDate);
		final Comment comment = review.getGeneralComments().get(0);
		comment.getReplies().add(prepareGeneralComment(comment, "reply", new PermId("CMT:41"), new Date(), null));

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REMOVED_COMMENT, notifications.get(0).getType());
	}

	public void testAddedGeneralCommentReplyToReply() {
		// test same review - fields and versioned comments not empty
		final Date commentDate = new Date();
		final Review review1 = prepareReview1(State.REVIEW, commentDate);
		final Review review2 = prepareReview1(State.REVIEW, commentDate);

		final Comment comment1 = review1.getGeneralComments().get(0);
		comment1.getReplies().add(
				prepareGeneralComment(comment1, "reply", new PermId("CMT:41"), commentDate, null));

		final Comment comment2 = review2.getGeneralComments().get(0);
		comment2.getReplies().add(
				prepareGeneralComment(comment2, "reply", new PermId("CMT:41"), commentDate, null));

		comment2.getReplies().get(0).getReplies().add(
				prepareGeneralComment(comment2, "reply", new PermId("CMT:42"), commentDate, null));

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review1, review2);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.NEW_COMMENT, notifications.get(0).getType());
	}

	public void testUpdatedGeneralCommentReplyToReply() {
		// test same review - fields and versioned comments not empty
		Date commentDate = new Date();
		Review review1 = prepareReview1(State.REVIEW, commentDate);
		Review review2 = prepareReview1(State.REVIEW, commentDate);

		final Comment comment1 = review1.getGeneralComments().get(0);
		comment1.getReplies().add(prepareGeneralComment(comment1, "reply", new PermId("CMT:41"), commentDate, null));

		comment1.getReplies().get(0).getReplies().add(
				prepareGeneralComment(comment1, "reply", new PermId("CMT:42"), commentDate, null));

		final Comment comment2 = review2.getGeneralComments().get(0);
		comment2.getReplies().add(prepareGeneralComment(comment2, "reply", new PermId("CMT:41"), commentDate, null));

		comment2.getReplies().get(0).getReplies().add(
				prepareGeneralComment(comment2, "reply 2 updated", new PermId("CMT:42"), new Date(), null));

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review1, review2);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.UPDATED_COMMENT, notifications.get(0).getType());
	}

	public void testRemovedGeneralCommentReplyToReply() {
		// test same review - fields and versioned comments not empty
		Date commentDate = new Date();
		Review review = prepareReview1(State.REVIEW, commentDate);
		Review review1 = prepareReview1(State.REVIEW, commentDate);

		final Comment comment1 = review.getGeneralComments().get(0);
		comment1.getReplies().add(prepareGeneralComment(comment1, "reply", new PermId("CMT:41"), commentDate, null));

		comment1.getReplies().get(0).getReplies().add(
				prepareGeneralComment(comment1, "reply", new PermId("CMT:42"), commentDate, null));

		final Comment comment2 = review1.getGeneralComments().get(0);
		comment2.getReplies().add(prepareGeneralComment(comment2, "reply", new PermId("CMT:41"), commentDate, null));

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REMOVED_COMMENT, notifications.get(0).getType());
	}

	public void testEditedGeneralComment() {
		// test same review - fields and versioned comments not empty
		Date commentDate = new Date();
		Review review = prepareReview1(State.REVIEW, commentDate);
		Review review1 = prepareReview1(State.REVIEW, commentDate);

		// change general for second review
		((GeneralComment) review1.getGeneralComments().get(0)).setMessage("new message");
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.UPDATED_COMMENT, notifications.get(0).getType());
	}

	public void testRemovedGeneralComment() {
		// test same review - fields and versioned comments not empty
		Date commentDate = new Date();
		Review review = prepareReview1(State.REVIEW, commentDate);
		Review review1 = prepareReview1(State.REVIEW, commentDate);

		// reset general for second review
		review1.getGeneralComments().clear();
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(2, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REMOVED_COMMENT, notifications.get(0).getType());
		assertEquals(CrucibleNotificationType.REMOVED_COMMENT, notifications.get(1).getType());
	}

	public void testAddedVersionedComment() {
		// test same review - fields and versioned comments not empty
		Date commentDate = new Date();
		Review review = prepareReview1(State.REVIEW, commentDate);
		Review review1 = prepareReview1(State.REVIEW, commentDate);

		// reset versioned for first file review
		Iterator<CrucibleFileInfo> iter = review.getFiles().iterator();
		iter.next().getVersionedComments().clear();
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(2, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.NEW_COMMENT, notifications.get(0).getType());
		assertEquals(CrucibleNotificationType.NEW_COMMENT, notifications.get(1).getType());

		iter.next().getVersionedComments().clear();
		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();

		assertEquals(4, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.NEW_COMMENT, notifications.get(0).getType());
		assertEquals(CrucibleNotificationType.NEW_COMMENT, notifications.get(1).getType());
		assertEquals(CrucibleNotificationType.NEW_COMMENT, notifications.get(2).getType());
		assertEquals(CrucibleNotificationType.NEW_COMMENT, notifications.get(3).getType());
	}

	public void testEditedVersionedComment() {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		Review review = prepareReview1(State.REVIEW, commentDate);
		Review review1 = prepareReview1(State.REVIEW, commentDate);

		Iterator<CrucibleFileInfo> iter = review.getFiles().iterator();

		(iter.next().getVersionedComments().get(0)).setMessage("new message");
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.UPDATED_COMMENT, notifications.get(0).getType());

		(iter.next().getVersionedComments().get(0)).setMessage("new message");
		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();

		assertEquals(2, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.UPDATED_COMMENT, notifications.get(0).getType());
		assertEquals(CrucibleNotificationType.UPDATED_COMMENT, notifications.get(1).getType());
	}

	public void testRemovedVersionedComment() {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		Review review = prepareReview1(State.REVIEW, commentDate);
		Review review1 = prepareReview1(State.REVIEW, commentDate);

		// reset versioned for first file review
		Iterator<CrucibleFileInfo> iter = review1.getFiles().iterator();
		iter.next().getVersionedComments().clear();
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(2, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REMOVED_COMMENT, notifications.get(0).getType());
		assertEquals(CrucibleNotificationType.REMOVED_COMMENT, notifications.get(1).getType());

		iter.next().getVersionedComments().clear();
		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();

		assertEquals(4, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REMOVED_COMMENT, notifications.get(0).getType());
		assertEquals(CrucibleNotificationType.REMOVED_COMMENT, notifications.get(1).getType());
		assertEquals(CrucibleNotificationType.REMOVED_COMMENT, notifications.get(2).getType());
		assertEquals(CrucibleNotificationType.REMOVED_COMMENT, notifications.get(3).getType());
	}

	public void testAddedVersionedCommentReply() {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		Review review = prepareReview1(State.REVIEW, commentDate);
		Review review1 = prepareReview1(State.REVIEW, commentDate);

		Iterator<CrucibleFileInfo> iter = review1.getFiles().iterator();
		final VersionedComment versionedComment = iter.next().getVersionedComments().get(0);
		versionedComment.getReplies().add(
				prepareVersionedComment("reply", new PermId("CMT:41"), commentDate, null, versionedComment
						.getCrucibleFileInfo()));
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.NEW_COMMENT, notifications.get(0).getType());
	}

	public void testUpdatedVersionedCommentReply() {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		Review review = prepareReview1(State.REVIEW, commentDate);
		Review review1 = prepareReview1(State.REVIEW, commentDate);

		Iterator<CrucibleFileInfo> iter = review.getFiles().iterator();
		final VersionedComment versionedComment = iter.next().getVersionedComments().get(0);
		versionedComment.getReplies().add(
				prepareVersionedComment("reply", new PermId("CMT:41"), commentDate, null, versionedComment
						.getCrucibleFileInfo()));

		Iterator<CrucibleFileInfo> iter1 = review1.getFiles().iterator();
		final VersionedComment versionedComment2 = iter1.next().getVersionedComments().get(0);
		versionedComment2.getReplies().add(
				prepareVersionedComment("updated reply", new PermId("CMT:41"), commentDate, null, versionedComment2
						.getCrucibleFileInfo()));
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.UPDATED_COMMENT, notifications.get(0).getType());
	}

	public void testRemovedVersionedCommentReply() {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		Review review = prepareReview1(State.REVIEW, commentDate);
		Review review1 = prepareReview1(State.REVIEW, commentDate);

		Iterator<CrucibleFileInfo> iter = review.getFiles().iterator();
		final CrucibleFileInfo cfi = iter.next();
		cfi.getVersionedComments().get(0).getReplies().add(
				prepareVersionedComment("reply", new PermId("CMT:41"), commentDate, null, cfi));
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REMOVED_COMMENT, notifications.get(0).getType());
	}

	public void testAddedVersionedCommentReplyToReply() {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		Review review = prepareReview1(State.REVIEW, commentDate);
		Review review1 = prepareReview1(State.REVIEW, commentDate);

		Iterator<CrucibleFileInfo> iter;

		iter = review.getFiles().iterator();
		assertTrue(iter.hasNext());
		final CrucibleFileInfo cfi = iter.next();
		cfi.getVersionedComments().get(0).getReplies().add(
				prepareVersionedComment("reply", new PermId("CMT:41"), commentDate, null, cfi));

		iter = review1.getFiles().iterator();
		assertTrue(iter.hasNext());
		final CrucibleFileInfo cfi1 = iter.next();
		cfi1.getVersionedComments().get(0).getReplies().add(
				prepareVersionedComment("reply", new PermId("CMT:41"), commentDate, null, cfi1));

		iter = review1.getFiles().iterator();
		assertTrue(iter.hasNext());
		cfi1.getVersionedComments().get(0).getReplies().get(0).getReplies().add(
				prepareVersionedComment("reply", new PermId("CMT:42"), commentDate, null, cfi1));

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.NEW_COMMENT, notifications.get(0).getType());
	}

	public void testUpdatedVersionedCommentReplyToReply() {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		Review review = prepareReview1(State.REVIEW, commentDate);
		Review review1 = prepareReview1(State.REVIEW, commentDate);

		Iterator<CrucibleFileInfo> iter;

		iter = review.getFiles().iterator();
		assertTrue(iter.hasNext());
		final CrucibleFileInfo cfi = iter.next();
		cfi.getVersionedComments().get(0).getReplies().add(
				prepareVersionedComment("reply", new PermId("CMT:41"), commentDate, null, cfi));

		iter = review.getFiles().iterator();
		assertTrue(iter.hasNext());
		final CrucibleFileInfo cfi1 = iter.next();
		cfi1.getVersionedComments().get(0).getReplies().get(0).getReplies().add(
				prepareVersionedComment("reply 2", new PermId("CMT:42"), commentDate, null, cfi1));

		iter = review1.getFiles().iterator();
		assertTrue(iter.hasNext());
		final CrucibleFileInfo cfi2 = iter.next();
		cfi2.getVersionedComments().get(0).getReplies().add(
				prepareVersionedComment("reply", new PermId("CMT:41"), commentDate, null, cfi2));

		iter = review1.getFiles().iterator();
		assertTrue(iter.hasNext());
		final CrucibleFileInfo cfi3 = iter.next();
		cfi3.getVersionedComments().get(0).getReplies().get(0).getReplies().add(
				prepareVersionedComment("reply 2 updated", new PermId("CMT:42"), commentDate, null, cfi3));

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.UPDATED_COMMENT, notifications.get(0).getType());
	}

	public void testRemovedVersionedCommentReplyToReply() {
		// test same review - fields and versioned comments not empty
		Date commentDate = new Date();
		Review review = prepareReview1(State.REVIEW, commentDate);
		Review review1 = prepareReview1(State.REVIEW, commentDate);


		final CrucibleFileInfo cfi = review.getFiles().iterator().next();
		cfi.getVersionedComments().get(0).getReplies().add(
				prepareVersionedComment("reply", new PermId("CMT:41"), commentDate, null, cfi));

		cfi.getVersionedComments().get(0).getReplies().get(0).getReplies().add(
				prepareVersionedComment("reply 2", new PermId("CMT:42"), commentDate, null, cfi));

		final CrucibleFileInfo cfi1 = review1.getFiles().iterator().next();
		cfi1.getVersionedComments().get(0).getReplies().add(
				prepareVersionedComment("reply", new PermId("CMT:41"), commentDate, null, cfi1));

		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();

		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REMOVED_COMMENT, notifications.get(0).getType());
	}

	public void testStateChanges() {
		// test same review - fiels and versioned comments not empty
		Date commentDate = new Date();
		Review review = prepareReview1(State.DRAFT, commentDate);
		Review review1 = prepareReview1(State.REVIEW, commentDate);
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();
		assertEquals(2, notifications.size());
		assertFalse(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		CrucibleNotification n1 = notifications.get(0);
		assertEquals(CrucibleNotificationType.REVIEW_STATE_CHANGED, n1.getType());
		assertTrue(n1.getPresentationMessage().contains(State.REVIEW.getDisplayName()));

		review = prepareReview1(State.REVIEW, commentDate);
		review1 = prepareReview1(State.REVIEW, commentDate);
		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();
		assertEquals(0, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());

		review1 = prepareReview1(State.CLOSED, commentDate);
		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();
		assertEquals(2, notifications.size());
		assertFalse(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		n1 = notifications.get(0);
		assertEquals(CrucibleNotificationType.REVIEW_STATE_CHANGED, n1.getType());
		assertTrue(n1.getPresentationMessage().contains(State.CLOSED.getDisplayName()));
	}

	public void testReviewersChanges() {
		// test same review - fields and versioned comments not empty
		Date commentDate = new Date();
		Review review = prepareReview1(State.REVIEW, commentDate);
		Review review1 = prepareReview1(State.REVIEW, commentDate);

		// just add reviewer - no notification ???
		review1.getReviewers().add(reviewer3);
		ReviewDifferenceProducer p = new ReviewDifferenceProducer(review, review1);
		List<CrucibleNotification> notifications = p.getDiff();
		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());

		// just remove reviewer - no notification ???
		review1.getReviewers().remove(reviewer3);
		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();
		assertEquals(0, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());

		// complete reviewer1
		Iterator<Reviewer> iter = review1.getReviewers().iterator();
		Reviewer reviewer = iter.next();
		review1.getReviewers().remove(reviewer);
		review1.getReviewers().add(new Reviewer(reviewer.getUsername(), reviewer.getDisplayName(), true));

		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();
		assertEquals(1, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REVIEWER_COMPLETED, notifications.get(0).getType());

		// complete all reviewers
		Reviewer[] reviewers = review1.getReviewers().toArray(new Reviewer[review1.getReviewers().size()]);
		review1.getReviewers().clear();
		for (Reviewer r : reviewers) {
			review1.getReviewers().add(new Reviewer(r.getUsername(), r.getDisplayName(), true));
		}

		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();
		assertEquals(3, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.REVIEWER_COMPLETED, notifications.get(0).getType());
		assertEquals(CrucibleNotificationType.REVIEWER_COMPLETED, notifications.get(1).getType());
		assertEquals(CrucibleNotificationType.REVIEW_COMPLETED, notifications.get(2).getType());

		review.getReviewers().clear();
		review1.getReviewers().clear();
		p = new ReviewDifferenceProducer(review, review1);
		notifications = p.getDiff();
		assertEquals(0, notifications.size());
		assertTrue(p.isShortEqual());
		assertTrue(p.isFilesEqual());
	}

	private interface MyCallback {
		void handle(Review r1, Review r2, String s1, String s2);
	}

	private final String[][] stringPairs = { { "abc", "bcde" }, { "", "abc" }, { "abc", "" }, { "abc", "abc" },
			{ "", "" }, };

	private void testHelper(final CrucibleNotificationType notificationType, MyCallback myCallback) {
		final Review r1 = prepareReview();
		final Review r2 = prepareReview();
		final ReviewDifferenceProducer p = new ReviewDifferenceProducer(r1, r2);

		for (String[] stringPair : stringPairs) {
			final String s1 = stringPair[0];
			final String s2 = stringPair[1];
			myCallback.handle(r1, r2, s1, s2);
			final List<CrucibleNotification> diff = p.getDiff();
			final String msg = "Checking " + s1 + " vs " + s2;
			if (MiscUtil.isEqual(s1, s2)) {
				assertEquals(msg, 0, diff.size());
			} else {
				assertEquals(msg, 1, diff.size());
				assertEquals(msg, notificationType, diff.get(0).getType());
			}
		}
	}

	public void testStatementOfObjectivesChanged() {
		testHelper(CrucibleNotificationType.STATEMENT_OF_OBJECTIVES_CHANGED, new MyCallback() {
			public void handle(final Review r1, final Review r2, final String s1, final String s2) {
				r1.setDescription(s1);
				r2.setDescription(s2);
			}
		});

	}

	public void testNameChanged() {
		testHelper(CrucibleNotificationType.NAME_CHANGED, new MyCallback() {
			public void handle(final Review r1, final Review r2, final String s1, final String s2) {
				r1.setName(s1);
				r2.setName(s2);
			}
		});
	}

	public void testModeratorChanged() {
		testHelper(CrucibleNotificationType.MODERATOR_CHANGED, new MyCallback() {
			public void handle(final Review r1, final Review r2, final String s1, final String s2) {
				r1.setModerator(new User(s1));
				r2.setModerator(new User(s2));
			}
		});
	}

	public void testAuthorChanged() {
		testHelper(CrucibleNotificationType.AUTHOR_CHANGED, new MyCallback() {
			public void handle(final Review r1, final Review r2, final String s1, final String s2) {
				r1.setAuthor(new User(s1));
				r2.setAuthor(new User(s2));
			}
		});
	}

	public void testSummmaryChanged() {
		testHelper(CrucibleNotificationType.SUMMARY_CHANGED, new MyCallback() {
			public void handle(final Review r1, final Review r2, final String s1, final String s2) {
				r1.setSummary(s1);
				r2.setSummary(s2);
			}
		});
	}

	public void testProjectChanged() {
		testHelper(CrucibleNotificationType.PROJECT_CHANGED, new MyCallback() {
			public void handle(final Review r1, final Review r2, final String s1, final String s2) {
				r1.setProjectKey(s1);
				r2.setProjectKey(s2);
			}
		});
	}

	public void testDueDateChanged() {
		Review review1 = prepareReview();
		Review review2 = prepareReview();
		review1.setDueDate(new DateTime());
		ReviewDifferenceProducer rdp = new ReviewDifferenceProducer(review1, review2);
		List<CrucibleNotification> diff = rdp.getDiff();
		assertEquals(1, diff.size());
		assertEquals(CrucibleNotificationType.DUE_DATE_CHANGED, diff.get(0).getType());

		review2.setDueDate(review1.getDueDate().minusDays(1));
		rdp = new ReviewDifferenceProducer(review1, review2);
		diff = rdp.getDiff();
		assertEquals(1, diff.size());
		assertEquals(CrucibleNotificationType.DUE_DATE_CHANGED, diff.get(0).getType());

		review2.setDueDate(review1.getDueDate());
		rdp = new ReviewDifferenceProducer(review1, review2);
		diff = rdp.getDiff();
		assertEquals(0, diff.size());

	}

	private static class Pair<T, E> {
		private final T first;

		private final E second;

		public Pair(final T first, final E second) {
			this.first = first;
			this.second = second;
		}
	}

	@SuppressWarnings("unchecked")
	public void testReviewersChanged() {
		final Review r1 = prepareReview();
		final Review r2 = prepareReview();
		final Reviewer rv1 = new Reviewer("user1", true);
		final Reviewer rv2 = new Reviewer("user2", true);
		final Reviewer rv3 = new Reviewer("user3", true);

		Collection<Pair<Set<Reviewer>, Set<Reviewer>>> reviewers = MiscUtil.buildArrayList(
				new Pair<Set<Reviewer>, Set<Reviewer>>(MiscUtil.buildHashSet(rv1), MiscUtil.<Reviewer> buildHashSet()),
				new Pair<Set<Reviewer>, Set<Reviewer>>(MiscUtil.buildHashSet(rv1), MiscUtil.buildHashSet(rv1)),
				new Pair<Set<Reviewer>, Set<Reviewer>>(MiscUtil.buildHashSet(rv1), MiscUtil.buildHashSet(rv2)),
				new Pair<Set<Reviewer>, Set<Reviewer>>(MiscUtil.buildHashSet(rv1, rv2), MiscUtil.buildHashSet(rv1)),
				new Pair<Set<Reviewer>, Set<Reviewer>>(MiscUtil.buildHashSet(rv1, rv2), MiscUtil.buildHashSet(rv2, rv3)),
				new Pair<Set<Reviewer>, Set<Reviewer>>(MiscUtil.buildHashSet(rv1, rv2, rv3), MiscUtil.buildHashSet(rv2,
						rv3, rv1)));

		final ReviewDifferenceProducer p = new ReviewDifferenceProducer(r1, r2);
		for (Pair<Set<Reviewer>, Set<Reviewer>> reviewersPair : reviewers) {
			r1.setReviewers(reviewersPair.first);
			r2.setReviewers(reviewersPair.second);
			final List<CrucibleNotification> diff = p.getDiff();
			final String msg = "Checking " + reviewersPair.first + " vs " + reviewersPair.second;
			if (MiscUtil.isEqual(reviewersPair.first, reviewersPair.second)) {
				assertEquals(msg, 0, diff.size());
			} else {
				assertEquals(msg, 1, diff.size());
				assertEquals(msg, CrucibleNotificationType.REVIEWERS_CHANGED, diff.get(0).getType());
			}
		}
	}

	/**
	 * Both reviews are the same, except on has file with different revisions
 	 */
	public void testRevisionChanged() {
		final Date dt = new Date();
		final Review r1 = prepareReview1(State.REVIEW, dt);
		final Review r2 = prepareReview1(State.REVIEW, dt);
		for (CrucibleFileInfo file : r1.getFiles()) {
			file.getOldFileDescriptor().setRevision(
					file.getOldFileDescriptor().getRevision() + ".23");
		}

		final ReviewDifferenceProducer p = new ReviewDifferenceProducer(r1, r2);
		final List<CrucibleNotification> diff = p.getDiff();
		assertNotNull(diff);
		assertEquals(4, diff.size());
		assertTrue(p.isShortEqual());
		assertFalse(p.isFilesEqual());
		assertEquals(CrucibleNotificationType.NEW_REVIEW_ITEM, diff.get(0).getType());
		assertEquals(CrucibleNotificationType.NEW_REVIEW_ITEM, diff.get(1).getType());
		assertEquals(CrucibleNotificationType.REMOVED_REVIEW_ITEM, diff.get(2).getType());
		assertEquals(CrucibleNotificationType.REMOVED_REVIEW_ITEM, diff.get(3).getType());
	}
}
