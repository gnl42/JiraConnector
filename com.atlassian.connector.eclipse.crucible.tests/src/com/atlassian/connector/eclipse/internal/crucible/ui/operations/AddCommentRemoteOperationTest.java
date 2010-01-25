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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import com.atlassian.connector.eclipse.internal.core.client.HttpSessionCallbackImpl;
import com.atlassian.connector.eclipse.internal.core.client.RemoteOperation;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.CrucibleSession;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleUserCacheImpl;
import com.atlassian.theplugin.commons.crucible.api.model.CustomField;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldBean;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;

import org.easymock.EasyMock;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class AddCommentRemoteOperationTest extends TestCase {

	private static final User VALID_LOGIN = new User("validLogin");

	private static final String VALID_PASSWORD = "validPassword";

	private static final String VALID_URL = "http://localhost:9001";

	private CrucibleServerFacade2 facade;

	private CrucibleSession crucibleSessionMock;

	private TaskRepository repository;

	public static final String INVALID_PROJECT_KEY = "INVALID project key";

	@SuppressWarnings("unchecked")
	@Override
	protected void setUp() throws Exception {
		ConfigurationFactory.setConfiguration(new PluginConfigurationBean());

		crucibleSessionMock = createMock(CrucibleSession.class);

		facade = new CrucibleServerFacadeImpl(new CrucibleUserCacheImpl(), new HttpSessionCallbackImpl());

		try {
			Field f = CrucibleServerFacadeImpl.class.getDeclaredField("sessions");
			f.setAccessible(true);

			((Map<String, CrucibleSession>) f.get(facade)).put(VALID_URL + VALID_LOGIN.getUsername() + VALID_PASSWORD,
					crucibleSessionMock);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testRunAddGeneralComment() throws Exception {
		crucibleSessionMock.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);
		try {
			crucibleSessionMock.login();
		} catch (RemoteApiLoginException e) {
			fail("recording mock failed for login");
		}

		ConnectionCfg serverCfg = prepareServerBean();
		Review review = new Review(serverCfg.getUrl());
		PermId permId = new PermId("1");
		User user = new User("user");
		review.setPermId(permId);
		MockCrucibleClient client = getMockClient(serverCfg);

		crucibleSessionMock.addGeneralComment(review, EasyMock.isA(Comment.class));
		GeneralComment result = new GeneralComment(review);
		result.setAuthor(user);
		result.setMessage("resultMsg");
		result.setPermId(permId);
		EasyMock.expectLastCall().andReturn(result);
		replay(crucibleSessionMock);

		AddCommentRemoteOperation remoteOperation = new AddCommentRemoteOperation(repository, review, client, null,
				"message", new NullProgressMonitor());

		try {
			Comment comment = client.execute(remoteOperation);
			assertSame(result, comment);
		} catch (CoreException e) {
			fail("Failed to add comment");
		}
	}

	public void testRunAddGeneralCommentReply() throws Exception {
		crucibleSessionMock.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);
		try {
			crucibleSessionMock.login();
		} catch (RemoteApiLoginException e) {
			fail("recording mock failed for login");
		}

		ConnectionCfg serverCfg = prepareServerBean();
		Review review = new Review(serverCfg.getUrl());
		PermId permId = new PermId("1");
		User user = new User("user");
		review.setPermId(permId);
		MockCrucibleClient client = getMockClient(serverCfg);

		crucibleSessionMock.addGeneralCommentReply(review, EasyMock.isA(PermId.class), EasyMock.isA(Comment.class));
		GeneralComment result = new GeneralComment(review);
		result.setAuthor(user);
		result.setMessage("resultMsg");
		result.setPermId(permId);
		GeneralComment parentComment = new GeneralComment(review);
		parentComment.setPermId(new PermId("2"));
		parentComment.setAuthor(user);
		parentComment.addReply(result);
		EasyMock.expectLastCall().andReturn(result);
		replay(crucibleSessionMock);

		AddCommentRemoteOperation remoteOperation = new AddCommentRemoteOperation(repository, review, client, null,
				"message", new NullProgressMonitor());
		remoteOperation.setParentComment(parentComment);

		try {
			Comment comment = client.execute(remoteOperation);
			assertSame(result, comment);
			assertEquals(1, parentComment.getReplies().size());
			assertSame(parentComment.getReplies().get(0), comment);
		} catch (CoreException e) {
			fail("Failed to add comment");
		}
	}

	public void testRunAddGeneralVersionedCommentReply() throws Exception {
		crucibleSessionMock.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);
		try {
			crucibleSessionMock.login();
		} catch (RemoteApiLoginException e) {
			fail("recording mock failed for login");
		}

		ConnectionCfg serverCfg = prepareServerBean();
		Review review = new Review(serverCfg.getUrl());
		PermId permId = new PermId("1");
		User user = new User("user");
		review.setPermId(permId);
		MockCrucibleClient client = getMockClient(serverCfg);

		CrucibleFile reviewFile = getMockReviewItem(false);
		crucibleSessionMock.addVersionedComment(review, EasyMock.isA(PermId.class),
				EasyMock.isA(VersionedComment.class));
		VersionedComment result = new VersionedComment(review);
		result.setAuthor(user);
		result.setMessage("resultMsg");
		result.setPermId(permId);
		result.setReviewItemId(reviewFile.getCrucibleFileInfo().getPermId());
		VersionedComment parent = new VersionedComment(review);
		parent.setPermId(new PermId("2"));
		parent.setAuthor(user);
		parent.addReply(result);
		EasyMock.expectLastCall().andReturn(result);
		replay(crucibleSessionMock);

		AddCommentRemoteOperation remoteOperation = new AddCommentRemoteOperation(repository, review, client,
				reviewFile, "message", new NullProgressMonitor());

		try {
			Comment comment = client.execute(remoteOperation);
			assertSame(result, comment);
			assertEquals(1, parent.getReplies().size());
			assertSame(parent.getReplies().get(0), comment);
		} catch (CoreException e) {
			fail("Failed to add comment");
		}
	}

	public void testRunAddGeneralVersionedComment() throws Exception {
		crucibleSessionMock.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);
		try {
			crucibleSessionMock.login();
		} catch (RemoteApiLoginException e) {
			fail("recording mock failed for login");
		}

		ConnectionCfg serverCfg = prepareServerBean();
		Review review = new Review(serverCfg.getUrl());
		PermId permId = new PermId("1");
		User user = new User("user");
		review.setPermId(permId);
		MockCrucibleClient client = getMockClient(serverCfg);

		CrucibleFile reviewFile = getMockReviewItem(false);
		crucibleSessionMock.addVersionedComment(review, EasyMock.isA(PermId.class),
				EasyMock.isA(VersionedComment.class));
		VersionedComment result = new VersionedComment(review);
		result.setAuthor(user);
		result.setMessage("resultMsg");
		result.setPermId(permId);
		result.setReviewItemId(reviewFile.getCrucibleFileInfo().getPermId());
		EasyMock.expectLastCall().andReturn(result);
		replay(crucibleSessionMock);

		AddCommentRemoteOperation remoteOperation = new AddCommentRemoteOperation(repository, review, client,
				reviewFile, "message", new NullProgressMonitor());

		try {
			Comment comment = client.execute(remoteOperation);
			assertSame(result, comment);
		} catch (CoreException e) {
			fail("Failed to add comment");
		}
	}

	public void testRunAddLineVersionedComment() throws Exception {
		crucibleSessionMock.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);
		try {
			crucibleSessionMock.login();
		} catch (RemoteApiLoginException e) {
			fail("recording mock failed for login");
		}

		ConnectionCfg serverCfg = prepareServerBean();
		Review review = new Review(serverCfg.getUrl());
		PermId permId = new PermId("1");
		User user = new User("user");
		review.setPermId(permId);
		MockCrucibleClient client = getMockClient(serverCfg);

		CrucibleFile reviewFile = getMockReviewItem(false);
		crucibleSessionMock.addVersionedComment(review, EasyMock.isA(PermId.class),
				EasyMock.isA(VersionedComment.class));
		VersionedComment result = new VersionedComment(review);
		result.setAuthor(user);
		result.setMessage("resultMsg");
		result.setPermId(permId);
		result.setReviewItemId(reviewFile.getCrucibleFileInfo().getPermId());
		LineRange lineRange = new LineRange(1, 20);
		result.setFromLineInfo(false);
		result.setToLineInfo(true);
		result.setToStartLine(1);
		result.setToEndLine(21);
		EasyMock.expectLastCall().andReturn(result);
		replay(crucibleSessionMock);

		AddCommentRemoteOperation remoteOperation = new AddCommentRemoteOperation(repository, review, client,
				reviewFile, "message", new NullProgressMonitor());
		remoteOperation.setCommentLines(lineRange);

		try {
			Comment comment = client.execute(remoteOperation);
			assertSame(result, comment);
			VersionedComment versComment = (VersionedComment) comment;
			assertTrue(versComment.isToLineInfo());
			assertFalse(versComment.isFromLineInfo());
			assertEquals(lineRange.getStartLine(), versComment.getToStartLine());
			assertEquals(lineRange.getNumberOfLines(), versComment.getToEndLine() - versComment.getToStartLine());
		} catch (CoreException e) {
			fail("Failed to add comment");
		}
	}

	public void testRunAddLineVersionedCommentReply() throws Exception {
		crucibleSessionMock.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);
		try {
			crucibleSessionMock.login();
		} catch (RemoteApiLoginException e) {
			fail("recording mock failed for login");
		}

		ConnectionCfg serverCfg = prepareServerBean();
		Review review = new Review(serverCfg.getUrl());
		PermId permId = new PermId("1");
		User user = new User("user");
		review.setPermId(permId);
		MockCrucibleClient client = getMockClient(serverCfg);

		CrucibleFile reviewFile = getMockReviewItem(false);
		crucibleSessionMock.addVersionedComment(review, EasyMock.isA(PermId.class),
				EasyMock.isA(VersionedComment.class));
		VersionedComment result = new VersionedComment(review);
		result.setAuthor(user);
		result.setMessage("resultMsg");
		result.setPermId(permId);
		result.setReviewItemId(reviewFile.getCrucibleFileInfo().getPermId());
		LineRange lineRange = new LineRange(1, 20);
		result.setFromLineInfo(false);
		result.setToLineInfo(true);
		result.setToStartLine(1);
		result.setToEndLine(21);
		VersionedComment parent = new VersionedComment(review);
		parent.setPermId(new PermId("2"));
		parent.setAuthor(user);
		parent.addReply(result);
		EasyMock.expectLastCall().andReturn(result);
		replay(crucibleSessionMock);

		AddCommentRemoteOperation remoteOperation = new AddCommentRemoteOperation(repository, review, client,
				reviewFile, "message", new NullProgressMonitor());
		remoteOperation.setCommentLines(lineRange);

		try {
			Comment comment = client.execute(remoteOperation);
			assertSame(result, comment);
			VersionedComment versComment = (VersionedComment) comment;
			assertTrue(versComment.isToLineInfo());
			assertFalse(versComment.isFromLineInfo());
			assertEquals(lineRange.getStartLine(), versComment.getToStartLine());
			assertEquals(lineRange.getNumberOfLines(), versComment.getToEndLine() - versComment.getToStartLine());
			assertEquals(1, parent.getReplies().size());
			assertSame(parent.getReplies().get(0), comment);
		} catch (CoreException e) {
			fail("Failed to add comment");
		}
	}

	public void testSetDefect() throws Exception {
		crucibleSessionMock.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);
		try {
			crucibleSessionMock.login();
		} catch (RemoteApiLoginException e) {
			fail("recording mock failed for login");
		}

		ConnectionCfg serverCfg = prepareServerBean();
		Review review = new Review(serverCfg.getUrl());
		PermId permId = new PermId("1");
		User user = new User("user");
		review.setPermId(permId);
		MockCrucibleClient client = getMockClient(serverCfg);

		crucibleSessionMock.addGeneralComment(review, EasyMock.isA(Comment.class));
		GeneralComment result = new GeneralComment(review);
		result.setAuthor(user);
		result.setMessage("resultMsg");
		result.setPermId(permId);
		result.setDefectRaised(true);
		EasyMock.expectLastCall().andReturn(result);
		replay(crucibleSessionMock);

		AddCommentRemoteOperation remoteOperation = new AddCommentRemoteOperation(repository, review, client, null,
				"message", new NullProgressMonitor());
		remoteOperation.setDefect(true);

		try {
			Comment comment = client.execute(remoteOperation);
			assertSame(result, comment);
			assertEquals(true, comment.isDefectRaised());
		} catch (CoreException e) {
			fail("Failed to add comment");
		}
	}

	public void testSetDraft() throws Exception {
		crucibleSessionMock.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);
		try {
			crucibleSessionMock.login();
		} catch (RemoteApiLoginException e) {
			fail("recording mock failed for login");
		}

		ConnectionCfg serverCfg = prepareServerBean();
		Review review = new Review(serverCfg.getUrl());
		PermId permId = new PermId("1");
		User user = new User("user");
		review.setPermId(permId);
		MockCrucibleClient client = getMockClient(serverCfg);

		crucibleSessionMock.addGeneralComment(review, EasyMock.isA(Comment.class));
		GeneralComment result = new GeneralComment(review);
		result.setAuthor(user);
		result.setMessage("resultMsg");
		result.setPermId(permId);
		result.setDraft(true);
		EasyMock.expectLastCall().andReturn(result);
		replay(crucibleSessionMock);

		AddCommentRemoteOperation remoteOperation = new AddCommentRemoteOperation(repository, review, client, null,
				"message", new NullProgressMonitor());
		remoteOperation.setDraft(true);

		try {
			Comment comment = client.execute(remoteOperation);
			assertSame(result, comment);
			assertEquals(true, comment.isDraft());
		} catch (CoreException e) {
			fail("Failed to add comment");
		}
	}

	public void testSetCustomFields() throws Exception {
		crucibleSessionMock.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);
		try {
			crucibleSessionMock.login();
		} catch (RemoteApiLoginException e) {
			fail("recording mock failed for login");
		}

		ConnectionCfg serverCfg = prepareServerBean();
		Review review = new Review(serverCfg.getUrl());
		PermId permId = new PermId("1");
		User user = new User("user");
		review.setPermId(permId);
		MockCrucibleClient client = getMockClient(serverCfg);

		crucibleSessionMock.addGeneralComment(review, EasyMock.isA(Comment.class));
		GeneralComment result = new GeneralComment(review);
		result.setAuthor(user);
		result.setMessage("resultMsg");
		result.setPermId(permId);
		result.setDefectRaised(true);
		HashMap<String, CustomField> customFields = new HashMap<String, CustomField>();
		CustomField field = new CustomFieldBean();
		customFields.put("1", field);
		result.getCustomFields().put("1", field);
		EasyMock.expectLastCall().andReturn(result);
		replay(crucibleSessionMock);

		AddCommentRemoteOperation remoteOperation = new AddCommentRemoteOperation(repository, review, client, null,
				"message", new NullProgressMonitor());
		remoteOperation.setDefect(true);

		try {
			Comment comment = client.execute(remoteOperation);
			assertSame(result, comment);
			assertEquals(1, comment.getCustomFields().size());
			assertSame(field, comment.getCustomFields().get("1"));
		} catch (CoreException e) {
			fail("Failed to add comment");
		}
	}

	public void testSetParentComment() throws Exception {
		crucibleSessionMock.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);
		try {
			crucibleSessionMock.login();
		} catch (RemoteApiLoginException e) {
			fail("recording mock failed for login");
		}

		ConnectionCfg serverCfg = prepareServerBean();
		Review review = new Review(serverCfg.getUrl());
		PermId permId = new PermId("1");
		User user = new User("user");
		review.setPermId(permId);
		MockCrucibleClient client = getMockClient(serverCfg);

		crucibleSessionMock.addGeneralComment(review, EasyMock.isA(Comment.class));
		GeneralComment result = new GeneralComment(review);
		result.setAuthor(user);
		result.setMessage("resultMsg");
		result.setPermId(permId);
		GeneralComment parentComment = new GeneralComment(review);
		parentComment.setPermId(new PermId("2"));
		parentComment.setAuthor(user);
		parentComment.addReply(result);
		EasyMock.expectLastCall().andReturn(result);
		replay(crucibleSessionMock);

		AddCommentRemoteOperation remoteOperation = new AddCommentRemoteOperation(repository, review, client, null,
				"message", new NullProgressMonitor());
		remoteOperation.setDraft(true);

		try {
			Comment comment = client.execute(remoteOperation);
			assertSame(result, comment);
			assertEquals(1, parentComment.getReplies().size());
			assertSame(parentComment.getReplies().get(0), comment);
		} catch (CoreException e) {
			fail("Failed to add comment");
		}
	}

	public void testSetCommentLines() throws Exception {
		crucibleSessionMock.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);
		try {
			crucibleSessionMock.login();
		} catch (RemoteApiLoginException e) {
			fail("recording mock failed for login");
		}

		ConnectionCfg serverCfg = prepareServerBean();
		Review review = new Review(serverCfg.getUrl());
		PermId permId = new PermId("1");
		User user = new User("user");
		review.setPermId(permId);
		MockCrucibleClient client = getMockClient(serverCfg);

		crucibleSessionMock.addVersionedComment(review, EasyMock.isA(PermId.class),
				EasyMock.isA(VersionedComment.class));
		VersionedComment result = new VersionedComment(review);
		result.setAuthor(user);
		result.setMessage("resultMsg");
		result.setPermId(permId);
		LineRange lineRange = new LineRange(1, 20);
		result.setFromLineInfo(false);
		result.setToLineInfo(true);
		result.setToStartLine(1);
		result.setToEndLine(21);
		EasyMock.expectLastCall().andReturn(result);
		replay(crucibleSessionMock);

		AddCommentRemoteOperation remoteOperation = new AddCommentRemoteOperation(repository, review, client,
				getMockReviewItem(false), "message", new NullProgressMonitor());
		remoteOperation.setDraft(true);

		try {
			Comment comment = client.execute(remoteOperation);
			assertSame(result, comment);
			VersionedComment versComment = (VersionedComment) comment;
			assertTrue(versComment.isToLineInfo());
			assertFalse(versComment.isFromLineInfo());
			assertEquals(lineRange.getStartLine(), versComment.getToStartLine());
			assertEquals(lineRange.getNumberOfLines(), versComment.getToEndLine() - versComment.getToStartLine());
		} catch (CoreException e) {
			fail("Failed to add comment");
		}
	}

	private class MockCrucibleClient extends CrucibleClient {

		private final ConnectionCfg serverCfg;

		private final CrucibleServerFacade2 facade;

		public MockCrucibleClient(CrucibleServerFacade2 facade, ConnectionCfg serverCfg) {
			super(null, serverCfg, facade, null, null, new HttpSessionCallbackImpl());
			this.serverCfg = serverCfg;
			this.facade = facade;
		}

		@Override
		public <T> T execute(RemoteOperation<T, CrucibleServerFacade2> op) throws CoreException {
			try {
				return op.run(facade, serverCfg, op.getMonitor());
			} catch (CrucibleLoginException e) {
				fail("Executing RemoteOperation failed");
			} catch (RemoteApiException e) {
				fail("Executing RemoteOperation failed");
			} catch (ServerPasswordNotProvidedException e) {
				fail("Executing RemoteOperation failed");
			}
			throw new CoreException(new Status(IStatus.ERROR, "com.atlassian.connector.exlipse.crucible.tests",
					"Executing RemoteOperation failed"));
		}

		@Override
		public String getUsername() {
			return "user";
		}
	}

	private CrucibleFile getMockReviewItem(boolean isOldFIle) {
		return new CrucibleFile(new CrucibleFileInfo(new VersionedVirtualFile("path", "1.0"),
				new VersionedVirtualFile("path", "0.9"), new PermId("permID")), isOldFIle);
	}

	private MockCrucibleClient getMockClient(ConnectionCfg serverCfg) {
		// ignore
		return new MockCrucibleClient(facade, serverCfg);
	}

	private ConnectionCfg prepareServerBean() {
		repository = new TaskRepository(CrucibleCorePlugin.CONNECTOR_KIND, VALID_URL);
		return new ConnectionCfg("myname", VALID_URL, VALID_LOGIN.getUsername(), VALID_PASSWORD);
	}

}
