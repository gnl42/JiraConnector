/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.crucible;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import com.atlassian.connector.commons.misc.ErrorResponse;
import com.atlassian.connector.commons.remoteapi.TestHttpSessionCallbackImpl;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.CrucibleSession;
import com.atlassian.theplugin.commons.crucible.api.model.BasicProject;
import com.atlassian.theplugin.commons.crucible.api.model.BasicReview;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleUserCacheImpl;
import com.atlassian.theplugin.commons.crucible.api.model.ExtendedCrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewTestUtil;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.crucible.api.rest.cruciblemock.LoginCallback;
import com.atlassian.theplugin.crucible.api.rest.cruciblemock.VersionInfoCallback;
import com.spartez.util.junit3.IAction;
import com.spartez.util.junit3.TestUtil;
import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpStatus;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.easymock.EasyMock;
import org.mortbay.jetty.Server;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;

public class CrucibleServerFacadeTest extends TestCase {
	private static final User VALID_LOGIN = new User("validLogin");

	private static final String VALID_PASSWORD = "validPassword";

	private static final String VALID_URL = "http://localhost:9001";

	private CrucibleServerFacade2 facade;

	private CrucibleSession crucibleSessionMock;

	public static final String INVALID_PROJECT_KEY = "INVALID project key";

	private CrucibleServerCfg crucibleServerCfg;

	@Override
	@SuppressWarnings("unchecked")
	protected void setUp() {
		ConfigurationFactory.setConfiguration(new PluginConfigurationBean());

		crucibleSessionMock = createMock(CrucibleSession.class);
		crucibleServerCfg = new CrucibleServerCfg("mycrucible", new ServerIdImpl());
		crucibleServerCfg.setUrl(VALID_URL);
		crucibleServerCfg.setPassword(VALID_PASSWORD);
		crucibleServerCfg.setUsername(VALID_LOGIN.getUsername());

		facade = new CrucibleServerFacadeImpl(LoggerImpl.getInstance(), new CrucibleUserCacheImpl(),
				new TestHttpSessionCallbackImpl());

		try {
			Field f = CrucibleServerFacadeImpl.class.getDeclaredField("sessions");
			f.setAccessible(true);

			((Map<String, CrucibleSession>) f.get(facade)).put(VALID_URL + VALID_LOGIN.getUsername() + VALID_PASSWORD,
					crucibleSessionMock);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public void testConnectionTestFailedBadPassword() throws Exception {

		Server server = new Server(0);
		server.start();

		crucibleServerCfg.setUrl("http://localhost:" + server.getConnectors()[0].getLocalPort());
		JettyMockServer mockServer = new JettyMockServer(server);
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(VALID_LOGIN.getUsername(), VALID_PASSWORD,
				LoginCallback.ALWAYS_FAIL));

		TestUtil.assertThrows(RemoteApiException.class, new IAction() {

			public void run() throws Throwable {
				facade.testServerConnection(getServerData(crucibleServerCfg));
			}
		});

		mockServer.verify();
		server.stop();
	}

	public void testConnectionTestFailedCru15() throws Exception {
		Server server = new Server(0);
		server.start();

		crucibleServerCfg.setUrl("http://localhost:" + server.getConnectors()[0].getLocalPort());
		JettyMockServer mockServer = new JettyMockServer(server);

		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(false));

		try {
			facade.testServerConnection(getServerData(crucibleServerCfg));
			fail("testServerConnection failed");
		} catch (RemoteApiException e) {
			// ok
		}

		mockServer.verify();
		server.stop();
	}

	/**
	 * Regression test for https://studio.atlassian.com/browse/PLE-514
	 *
	 * @throws Exception
	 */
	public void testConnectionTestFailedHttp404() throws Exception {
		Server server = new Server(0);
		server.start();

		crucibleServerCfg.setUrl("http://localhost:" + server.getConnectors()[0].getLocalPort());
		JettyMockServer mockServer = new JettyMockServer(server);

		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new ErrorResponse(404, "Not Found"));

		try {
			facade.testServerConnection(getServerData(crucibleServerCfg));
			fail("testServerConnection failed");
		} catch (RemoteApiException e) {
			assertTrue("Must include HTTP 404", e.getMessage().contains("HTTP 404"));
			assertTrue("Must contain " + HttpStatus.getStatusText(HttpStatus.SC_NOT_FOUND), e.getMessage().contains(
					HttpStatus.getStatusText(HttpStatus.SC_NOT_FOUND)));
			assertFalse("Should not include \\n", e.getMessage().contains("\n"));
		}

		mockServer.verify();
		server.stop();
	}

	/**
	 * Regression for https://studio.atlassian.com/browse/ACC-40
	 *
	 * @throws Exception
	 */
	public void testConnectionTestInvalidUrlIncludesPassword() throws Exception {
		crucibleServerCfg.setUrl("http://invalid url");
		try {
			facade.testServerConnection(getServerData(crucibleServerCfg));
			fail("Should throw RemoteApiLoginException");
		} catch (RemoteApiException e) {
			assertFalse("Message should not include users's password", e.getMessage().contains(VALID_PASSWORD));
		}
	}

	public void testConnectionTestSucceed() throws Exception {
		Server server = new Server(0);
		server.start();

		crucibleServerCfg.setUrl("http://localhost:" + server.getConnectors()[0].getLocalPort());
		JettyMockServer mockServer = new JettyMockServer(server);

		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(VALID_LOGIN.getUsername(), VALID_PASSWORD,
				false));
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));

		try {
			facade.testServerConnection(getServerData(crucibleServerCfg));
		} catch (RemoteApiException e) {
			fail("testServerConnection failed: " + e.getMessage());
		}

		mockServer.verify();
		server.stop();
	}

	@SuppressWarnings("unchecked")
	Map<String, CrucibleSession> getSessionsFromFacade() {
		Field f;
		try {
			f = CrucibleServerFacadeImpl.class.getDeclaredField("sessions");
			f.setAccessible(true);
			return (Map<String, CrucibleSession>) f.get(facade);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public void testChangedCredentials() throws Exception {
		User validLogin2 = new User(VALID_LOGIN.getUsername() + 2);
		String validPassword2 = VALID_PASSWORD + 2;
		getSessionsFromFacade().put(VALID_URL + validLogin2.getUsername() + validPassword2, crucibleSessionMock);

//		Disabled temporarily as to fix ACC-31
//		crucibleSessionMock.isLoggedIn();
//		EasyMock.expectLastCall().andReturn(false);
//		try {
//			crucibleSessionMock.login();
//		} catch (RemoteApiLoginException e) {
//			fail("recording mock failed for login");
//		}

		PermId permId = new PermId("permId");

		Review review = prepareReviewData(VALID_LOGIN, "name", State.DRAFT, permId);

		crucibleSessionMock.getReviewsForFilter(PredefinedFilter.Drafts);
		EasyMock.expectLastCall().andReturn(Arrays.asList(review, review));

//		crucibleSessionMock.isLoggedIn();
//		EasyMock.expectLastCall().andReturn(false);
//		try {
//			crucibleSessionMock.login();
//		} catch (RemoteApiLoginException e) {
//			fail("recording mock failed for login");
//		}

		Review review2 = prepareReviewData(validLogin2, "name", State.DRAFT, permId);
		crucibleSessionMock.getReviewsForFilter(PredefinedFilter.Drafts);
		EasyMock.expectLastCall().andReturn(Arrays.asList(review2));

		replay(crucibleSessionMock);

		CrucibleServerCfg server = prepareServerBean();
		List<BasicReview> ret = facade.getReviewsForFilter(getServerData(server), PredefinedFilter.Drafts);
		assertEquals(2, ret.size());
		assertEquals(permId.getId(), ret.get(0).getPermId().getId());
		assertEquals("name", ret.get(0).getName());
		assertEquals(VALID_LOGIN, ret.get(0).getAuthor());
		assertEquals(VALID_LOGIN, ret.get(0).getCreator());
		assertEquals("Test description", ret.get(0).getDescription());
		assertEquals(VALID_LOGIN, ret.get(0).getModerator());
		assertEquals("TEST", ret.get(0).getProjectKey());
		assertEquals(null, ret.get(0).getRepoName());
		assertSame(State.DRAFT, ret.get(0).getState());
		assertNull(ret.get(0).getParentReview());

		server.setUsername(validLogin2.getUsername());
		server.setPassword(validPassword2);
		ret = facade.getReviewsForFilter(getServerData(server), PredefinedFilter.Drafts);
		assertEquals(1, ret.size());
		assertEquals(permId.getId(), ret.get(0).getPermId().getId());
		assertEquals("name", ret.get(0).getName());
		assertEquals(validLogin2, ret.get(0).getAuthor());
		assertEquals(validLogin2, ret.get(0).getCreator());
		assertEquals("Test description", ret.get(0).getDescription());
		assertEquals(validLogin2, ret.get(0).getModerator());
		assertEquals("TEST", ret.get(0).getProjectKey());
		assertEquals(null, ret.get(0).getRepoName());
		assertSame(State.DRAFT, ret.get(0).getState());
		assertNull(ret.get(0).getParentReview());

		EasyMock.verify(crucibleSessionMock);
	}

	public void testCreateReview() throws Exception {
//		Disabled temporarily as to fix ACC-31
//		crucibleSessionMock.isLoggedIn();
//		EasyMock.expectLastCall().andReturn(false);
//		try {
//			crucibleSessionMock.login();
//		} catch (RemoteApiLoginException e) {
//			fail("recording mock failed for login");
//		}

		crucibleSessionMock.createReview(EasyMock.isA(Review.class));
		CrucibleServerCfg server = prepareServerBean();
		Review response = ReviewTestUtil.createReview(server.getUrl());
		final PermId permId = new PermId("p1");
		response.setPermId(permId);

		EasyMock.expectLastCall().andReturn(response);
		// actually for getReview a full review object would be normally returned, but for this test it doesn't matter
		EasyMock.expect(crucibleSessionMock.getReview(permId)).andReturn(response);

		replay(crucibleSessionMock);

		Review review = prepareReviewData("name", State.DRAFT);

		// test call
		Review ret = facade.createReview(getServerData(server), review);
		assertSame(response, ret);

		EasyMock.verify(crucibleSessionMock);

	}

	public void testCreateReviewWithInvalidProjectKey() throws Exception {
//		Disabled temporarily as to fix ACC-31
//		crucibleSessionMock.isLoggedIn();
//		EasyMock.expectLastCall().andReturn(false);
//		try {
//			crucibleSessionMock.login();
//		} catch (RemoteApiLoginException e) {
//			fail("recording mock failed for login");
//		}

		crucibleSessionMock.createReview(EasyMock.isA(Review.class));

		EasyMock.expectLastCall().andThrow(new RemoteApiException("test"));

		replay(crucibleSessionMock);

		CrucibleServerCfg server = prepareServerBean();
		Review review = prepareReviewData("name", State.DRAFT);

		try {
			// test call
			facade.createReview(getServerData(server), review);
			fail("creating review with invalid key should throw an CrucibleException()");
		} catch (RemoteApiException e) {

		} finally {
			EasyMock.verify(crucibleSessionMock);
		}

	}

	public void testCreateReviewFromPatch() throws ServerPasswordNotProvidedException, RemoteApiException {
//		Disabled temporarily as to fix ACC-31
//		crucibleSessionMock.isLoggedIn();
//		EasyMock.expectLastCall().andReturn(false);
//		try {
//			crucibleSessionMock.login();
//		} catch (RemoteApiLoginException e) {
//			fail("recording mock failed for login");
//		}

		crucibleSessionMock.createReviewFromPatch(EasyMock.isA(Review.class), EasyMock.eq("some patch"));
		CrucibleServerCfg server = prepareServerBean();
		Review response = ReviewTestUtil.createReview(server.getUrl());
		final PermId permId = new PermId("p1");
		response.setPermId(permId);
		EasyMock.expectLastCall().andReturn(response);

		// actually for getReview a full review object would be normally returned, but for this test it doesn't matter
		EasyMock.expect(crucibleSessionMock.getReview(permId)).andReturn(response);

		replay(crucibleSessionMock);

		Review review = prepareReviewData("name", State.DRAFT);

		String patch = "some patch";

		// test call
		Review ret = facade.createReviewFromPatch(getServerData(server), review, patch);
		assertSame(response, ret);

		EasyMock.verify(crucibleSessionMock);
	}

	public void testCreateReviewFromPatchWithInvalidProjectKey() throws Exception {
//		Disabled temporarily as to fix ACC-31
//		crucibleSessionMock.isLoggedIn();
//		EasyMock.expectLastCall().andReturn(false);
//		try {
//			crucibleSessionMock.login();
//		} catch (RemoteApiLoginException e) {
//			fail("recording mock failed for login");
//		}

		crucibleSessionMock.createReviewFromPatch(EasyMock.isA(Review.class), EasyMock.eq("some patch"));
		EasyMock.expectLastCall().andThrow(new RemoteApiException("test"));

		replay(crucibleSessionMock);

		CrucibleServerCfg server = prepareServerBean();
		Review review = prepareReviewData("name", State.DRAFT);

		String patch = "some patch";

		try {
			facade.createReviewFromPatch(getServerData(server), review, patch);
			fail("creating review with patch with invalid key should throw an RemoteApiException()");
		} catch (RemoteApiException e) {
			// ignored by design
		} finally {
			EasyMock.verify(crucibleSessionMock);
		}
	}


	public void testGetProjects() throws ServerPasswordNotProvidedException, RemoteApiException {
//		Disabled temporarily as to fix ACC-31
//		crucibleSessionMock.isLoggedIn();
//		EasyMock.expectLastCall().andReturn(false);
//		try {
//			crucibleSessionMock.login();
//		} catch (RemoteApiLoginException e) {
//			fail("recording mock failed for login");
//		}
		crucibleSessionMock.getProjects();
		EasyMock.expectLastCall().andReturn(Arrays.asList(prepareProjectData(0), prepareProjectData(1)));
		EasyMock.expect(crucibleSessionMock.getProjects()).andReturn(Arrays.asList(prepareProjectData(1)));
		replay(crucibleSessionMock);

		CrucibleServerCfg server = prepareServerBean();
		// test call
		List<BasicProject> ret = facade.getProjects(getServerData(server));
		assertEquals(2, ret.size());
		for (int i = 0; i < 2; i++) {
			String id = Integer.toString(i);
			assertEquals(id, ret.get(i).getId());
			assertEquals("CR" + id, ret.get(i).getKey());
			assertEquals("Name" + id, ret.get(i).getName());
		}

		// second call - now only one project (as we do not cache here)
		ret = facade.getProjects(getServerData(server));
		assertEquals(1, ret.size());
		assertEquals(prepareProjectData(1), ret.get(0));
		EasyMock.verify(crucibleSessionMock);
	}

	public void testGetRepositories() throws ServerPasswordNotProvidedException, RemoteApiException {
//		Disabled temporarily as to fix ACC-31
//		crucibleSessionMock.isLoggedIn();
//		EasyMock.expectLastCall().andReturn(false);
//		try {
//			crucibleSessionMock.login();
//		} catch (RemoteApiLoginException e) {
//			fail("recording mock failed for login");
//		}
		crucibleSessionMock.getRepositories();
		EasyMock.expectLastCall().andReturn(Arrays.asList(prepareRepositoryData(0), prepareRepositoryData(1)));
		replay(crucibleSessionMock);

		CrucibleServerCfg server = prepareServerBean();
		// test call
		List<Repository> ret = facade.getRepositories(getServerData(server));
		assertEquals(2, ret.size());
		for (int i = 0; i < 2; i++) {
			String id = Integer.toString(i);
			assertEquals("RepoName" + id, ret.get(i).getName());
		}
		EasyMock.verify(crucibleSessionMock);
	}

	private Review prepareReviewData(final String name, final State state) {
		return new Review("http://bogus.server", "TEST", VALID_LOGIN, VALID_LOGIN) {
			@Override
			public User getCreator() {
				return VALID_LOGIN;
			}

			@Override
			public String getDescription() {
				return "Test description";
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public PermId getParentReview() {
				return null;
			}

			@Override
			public PermId getPermId() {
				return new PermId("permId");
			}

			@Override
			public String getRepoName() {
				return null;
			}

			@Override
			public State getState() {
				return state;
			}

			@Override
			public boolean isAllowReviewerToJoin() {
				return false;
			}

			@Override
			public int getMetricsVersion() {
				return 0;
			}

			@Override
			public Date getCreateDate() {
				return null;
			}

			@Override
			public Date getCloseDate() {
				return null;
			}

			@Override
			public String getSummary() {
				return null;
			}

			@Override
			public Set<Reviewer> getReviewers() {
				return null;
			}

			@Override
			public List<Comment> getGeneralComments() {
				return null;
			}

			@Override
			public Set<CrucibleFileInfo> getFiles() {
				return null;
			}

			@Override
			public Set<CrucibleAction> getTransitions() {
				return null;
			}

			@Override
			public Set<CrucibleAction> getActions() {
				return null;
			}

			@Override
			public void setGeneralComments(final List<Comment> generalComments) {
				// not implemented
			}

			@Override
			public void removeGeneralComment(final Comment comment) {
				// not implemented
			}

			@Override
			public void removeVersionedComment(final VersionedComment vComment, final CrucibleFileInfo file) {
				// not implemented
			}

			@Override
			public void setFilesAndVersionedComments(final Collection<CrucibleFileInfo> files,
					final List<VersionedComment> commentList) {
			}

			@Override
			public CrucibleFileInfo getFileByPermId(PermId id) {
				return null;
			}
		};
	}

	private Review prepareReviewData(final User user, final String name, final State state, final PermId permId) {
		return new Review("http://bogus.server", "TEST", user, user) {
			@Override
			public User getCreator() {
				return user;
			}

			@Override
			public String getDescription() {
				return "Test description";
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public PermId getParentReview() {
				return null;
			}

			@Override
			public PermId getPermId() {
				return permId;
			}

			@Override
			public String getRepoName() {
				return null;
			}

			@Override
			public State getState() {
				return state;
			}

			@Override
			public boolean isAllowReviewerToJoin() {
				return false;
			}

			@Override
			public int getMetricsVersion() {
				return 0;
			}

			@Override
			public Date getCreateDate() {
				return null;
			}

			@Override
			public Date getCloseDate() {
				return null;
			}

			@Override
			public String getSummary() {
				return null;
			}

			@Override
			public void setGeneralComments(final List<Comment> generalComments) {
				// not implemented
			}

			@Override
			public void removeGeneralComment(final Comment comment) {
				// not implemented
			}

			@Override
			public void removeVersionedComment(final VersionedComment vComment, final CrucibleFileInfo file) {
				// not implemented
			}

			@Override
			public void setFilesAndVersionedComments(final Collection<CrucibleFileInfo> files,
					final List<VersionedComment> commentList) {
			}

			@Override
			public Set<Reviewer> getReviewers() {
				return null;
			}

			@Override
			public List<Comment> getGeneralComments() {
				return null;
			}

			@Override
			public Set<CrucibleFileInfo> getFiles() {
				return null;
			}

			@Override
			public Set<CrucibleAction> getTransitions() {
				return null;
			}

			@Override
			public Set<CrucibleAction> getActions() {
				return null;
			}

			@Override
			public CrucibleFileInfo getFileByPermId(PermId id) {
				return null;
			}
		};
	}

	private CrucibleServerCfg prepareServerBean() {
		CrucibleServerCfg server = new CrucibleServerCfg("myname", new ServerIdImpl());
		server.setUrl(VALID_URL);
		server.setUsername(VALID_LOGIN.getUsername());
		server.setPassword(VALID_PASSWORD);
		server.setPasswordStored(false);
		return server;
	}

	private BasicProject prepareProjectData(final int i) {
        Collection<String> usersNames = new ArrayList<String>();
        usersNames.add("Ala");
        usersNames.add("Zosia");

		return new ExtendedCrucibleProject(Integer.toString(i),
                "CR" + Integer.toString(i), "Name" + Integer.toString(i), usersNames);
	}

	private Repository prepareRepositoryData(final int i) {
		return new Repository("RepoName" + Integer.toString(i), "svn", false);
	}

	public void _testCreateReviewHardcoded() throws ServerPasswordNotProvidedException {

		//facade.setCrucibleSession(null);

		CrucibleServerCfg server = prepareCrucibleServerCfg();

		Review review = prepareReviewData("test", State.DRAFT);

		Review ret;

		try {
			ret = facade.createReview(getServerData(server), review);
			assertNotNull(ret);
			assertNotNull(ret.getPermId());
			assertNotNull(ret.getPermId().getId());
			assertTrue(ret.getPermId().getId().length() > 0);
		} catch (RemoteApiException e) {
			fail(e.getMessage());
		}
	}

	private CrucibleServerCfg prepareCrucibleServerCfg() {
		CrucibleServerCfg server = new CrucibleServerCfg("mycrucible", new ServerIdImpl());
		server.setUrl("http://lech.atlassian.pl:8060");
		server.setUsername("test");
		server.setPassword("test");
		server.setPasswordStored(false);
		return server;
	}


	private ConnectionCfg getServerData(final ServerCfg serverCfg) {
		return new ConnectionCfg(serverCfg.getServerId().getId(), serverCfg.getUrl(), serverCfg.getUsername(), serverCfg
				.getPassword());
	}
}
