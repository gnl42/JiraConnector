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

package com.atlassian.theplugin.crucible.api.rest;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.misc.ErrorResponse;
import com.atlassian.connector.commons.misc.IntRange;
import com.atlassian.connector.commons.misc.IntRanges;
import com.atlassian.connector.commons.remoteapi.TestHttpSessionCallbackImpl;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.crucible.api.CrucibleSession;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.crucible.api.model.changes.Changes;
import com.atlassian.theplugin.commons.crucible.api.rest.CrucibleSessionImpl;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.crucible.api.rest.cruciblemock.*;
import com.spartez.util.junit3.TestUtil;
import junit.framework.TestCase;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.mortbay.jetty.Server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Test case for {#link BambooSessionImpl}
 */
public class CrucibleSessionTest extends TestCase {
	private static final String USER_NAME = "someUser";

	private static final String PASSWORD = "somePassword";

	private Server server;

	private JettyMockServer mockServer;

	private String mockBaseUrl;

	@Override
	protected void setUp() throws Exception {
		ConfigurationFactory.setConfiguration(new PluginConfigurationBean());

		server = new Server(0);
		server.start();

		mockBaseUrl = "http://localhost:" + server.getConnectors()[0].getLocalPort();

		mockServer = new JettyMockServer(server);
	}

	@Override
	protected void tearDown() throws Exception {
		mockServer.verify();
		mockServer = null;
		mockBaseUrl = null;
		server.stop();
	}

	public void testSuccessCrucibleLogin() throws Exception {
		testCrucibleLoginImpl(new VersionInfoCallback(true), false);
	}

	private void testCrucibleLoginImpl(JettyMockServer.Callback versionInfoCallback, boolean shouldExpectPost)
			throws RemoteApiException, RemoteApiLoginException {
		String[] usernames = { "user", "+-=&;<>", "", "a;&username=other", "!@#$%^&*()_-+=T " };
		String[] passwords = { "password", "+-=&;<>", "", "&password=other", ",./';[]\t\\ |}{\":><?" };

		for (int i = 0; i < usernames.length; ++i) {
			mockServer.expect("/rest-service/reviews-v1/versionInfo", versionInfoCallback);
			mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(usernames[i], passwords[i], false,
					shouldExpectPost));
			final CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, usernames[i], passwords[i]);

			apiHandler.login();
			assertTrue(apiHandler.isLoggedIn());
			apiHandler.logout();
			assertFalse(apiHandler.isLoggedIn());
		}
		mockServer.verify();
	}

	public void testSuccessLoginForCrucible2x4() throws Exception {
		testCrucibleLoginImpl(new Version2x4InfoCallback(), true);
	}

	public void testLoginMalformedResponse() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new MalformedResponseCallback());
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		try {
			apiHandler.login();
			apiHandler.isLoggedIn();
			fail();
		} catch (RemoteApiException e) {
		}
		mockServer.verify();
	}

	public void testLoginInternalErrorResponse() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new ErrorResponse(500, ""));
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		try {
			apiHandler.login();
			apiHandler.isLoggedIn();
			fail();
		} catch (RemoteApiException e) {
			// expected
		}
		mockServer.verify();
	}

	public void testSuccessCrucibleLoginURLWithSlash() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));

		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl + "/", USER_NAME, PASSWORD);
		apiHandler.login();
		assertTrue(apiHandler.isLoggedIn());
		apiHandler.logout();
		assertFalse(apiHandler.isLoggedIn());

		mockServer.verify();
	}

	public void testNullParamsLogin() throws Exception {
		try {
			CrucibleSession apiHandler = createCrucibleSession(null, null, null);
			apiHandler.login();
			fail();
		} catch (RemoteApiException ex) {
			// OK
		}
	}

	public void testNullLoginLogin() throws Exception {
		try {
			CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, null, null);
			apiHandler.login();
			apiHandler.isLoggedIn();
			fail();
		} catch (RemoteApiLoginException ex) {
			// OK
		}
	}

	@SuppressWarnings("null")
	public void testWrongUrlCrucibleLogin() throws Exception {
		ErrorResponse error = new ErrorResponse(400, "Bad Request/reason phrase");
		mockServer.expect("/wrongurl/rest-service/reviews-v1/versionInfo", error); // logging in first looks for cru version
		RemoteApiLoginException exception = null;

		try {
			CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl + "/wrongurl", USER_NAME, PASSWORD);
			apiHandler.login();
			apiHandler.isLoggedIn();
		} catch (RemoteApiLoginException ex) {
			exception = ex;
		}
		mockServer.verify();

		assertNotNull("Exception expected", exception);
		final Throwable cause = exception.getCause();
		assertNotNull("Exception should have a cause", cause);
		assertSame(RemoteApiException.class, cause.getClass());
		final Throwable nestedCause = cause.getCause();
		assertNotNull("The Cause should have another cause", nestedCause);
		assertSame(IOException.class, nestedCause.getClass());
		// Regression test for https://studio.atlassian.com/browse/PLE-514
		// exception should not include Reason Phrase - it goes to log
		assertFalse(exception.getMessage().contains(error.getErrorMessage()));
	}

	@SuppressWarnings("null")
	public void testNonExistingServerCrucibleLogin() throws Exception {
		RemoteApiLoginException exception = null;

		try {
			CrucibleSession apiHandler = createCrucibleSession("http://non.existing.server.utest", USER_NAME, PASSWORD);
			apiHandler.login();
			apiHandler.isLoggedIn();
		} catch (RemoteApiLoginException ex) {
			exception = ex;
		}

		assertNotNull("Exception expected", exception);
		final Throwable cause = exception.getCause();
		assertNotNull("Exception should have a cause", cause);
		assertSame("RemoteApiException expected", RemoteApiException.class, cause.getClass());

		final Throwable innecCause = cause.getCause();
		assertNotNull("Exception cause should have nested cause", innecCause);
		assertSame("UnknownHostException expected", UnknownHostException.class, innecCause.getClass());
		assertEquals("Checking exception message failed", "Unknown host: non.existing.server.utest", exception.getMessage());
	}

	public void testMalformedUrlCrucibleLogin() {
		tryMalformedUrl("noprotocol.url/path");
		tryMalformedUrl("http:localhost/path");
		tryMalformedUrl("http:/localhost/path");
		tryMalformedUrl("http:///localhost/path");
		tryMalformedUrl("http:localhost");
		tryMalformedUrl("http:/localhost");
		tryMalformedUrl("http:///localhost");
		tryMalformedUrl("http://");
		tryMalformedUrl("ncxvx:/localhost/path");
		tryMalformedUrl("ncxvx:///localhost/path");
		tryMalformedUrl("ncxvx://localhost/path");
		tryMalformedUrl("ncxvx:///localhost/path");
		tryMalformedUrl("https:localhost/path");
		tryMalformedUrl("https:/localhost/path");
		tryMalformedUrl("https:///localhost/path");
		tryMalformedUrl("https:localhost");
		tryMalformedUrl("https:/localhost");
		tryMalformedUrl("https:///localhost");
		tryMalformedUrl("https://");
		tryMalformedUrl("http::localhost/path");
		tryMalformedUrl("http://loca:lhost/path");
	}

	@SuppressWarnings("null")
	private void tryMalformedUrl(final String url) {
		RemoteApiException exception = null;
		try {
			CrucibleSession apiHandler = createCrucibleSession(url, USER_NAME, PASSWORD);
			apiHandler.login();
		} catch (RemoteApiLoginException e) {
			exception = e;
		} catch (RemoteApiException e) {
			exception = e;
		}

		assertNotNull("Exception expected", exception);
		assertNotNull("Exception should have a cause", exception.getCause());
		assertTrue("MalformedURLException expected", exception.getCause() instanceof MalformedURLException);
		assertEquals("Malformed server URL: " + url, exception.getMessage());
	}

	@SuppressWarnings("null")
	public void testOutOfRangePort() {
		String url = "http://localhost:80808";
		RemoteApiException exception = null;
		try {
			CrucibleSession apiHandler = createCrucibleSession(url, USER_NAME, PASSWORD);
			apiHandler.login();
		} catch (RemoteApiException e) {
			exception = e;
		}

		assertNotNull("Exception expected", exception);
		assertNotNull("Exception should have a cause", exception.getCause());
		assertTrue("MalformedURLException expected", exception.getCause() instanceof IOException);
	}

	public void testWrongUserCrucibleLogin() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD,
				LoginCallback.ALWAYS_FAIL));

		try {
			CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);
			apiHandler.login(); // mock will fail this
			apiHandler.isLoggedIn();
			fail();
		} catch (RemoteApiLoginException ex) {
			// expected
		}

		mockServer.verify();
	}

	public void testWrongParamsCrucibleLogin() throws Exception {
		try {
			CrucibleSession apiHandler = createCrucibleSession("", "", "");
			apiHandler.login();
			fail();
		} catch (RemoteApiException ex) {
			// expected
		}
	}

	public void testSuccessCrucibleLogout() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));

		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		assertTrue(apiHandler.isLoggedIn());

		apiHandler.logout();
		apiHandler.logout();

		CrucibleSession apiHandler2 = createCrucibleSession(mockBaseUrl);
		apiHandler2.logout();

		mockServer.verify();
	}

	public void testFailedCrucibleLogin() throws RemoteApiException {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD,
				LoginCallback.ALWAYS_FAIL));
		try {
			createCrucibleSession(mockBaseUrl, null, null);
		} catch (RemoteApiException e) {
			fail();
		}

		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);
		try {
			apiHandler.login();
			apiHandler.isLoggedIn();
			fail("Login succeeded while expected failure.");
		} catch (RemoteApiLoginException e) {
			// expected
		}

		apiHandler = createCrucibleSession(mockBaseUrl, null, PASSWORD);
		try {
			apiHandler.login();
			apiHandler.isLoggedIn();
			fail("Login succeeded while expected failure.");
		} catch (RemoteApiLoginException e) {
			// expected
		}

		apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, null);
		try {
			apiHandler.login();
			apiHandler.isLoggedIn();
			fail("Login succeeded while expected failure.");
		} catch (RemoteApiLoginException e) {
			// expected
		}

		mockServer.verify();
	}

	public void testSuccessCrucibleDoubleLogin() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		apiHandler.isLoggedIn();
		apiHandler.login();
		apiHandler.isLoggedIn();

		mockServer.verify();
	}

	public void testMethodCallWithoutLogin() throws Exception {
		CrucibleSession crucibleSession = createCrucibleSession(mockBaseUrl);
		List<State> states = new ArrayList<State>();
		try {
			crucibleSession.getReviewsInStates(states);
			fail();
		} catch (IllegalStateException e) {
			//expected
		}
		try {
			crucibleSession.getReviewsInStates(null);
			fail();
		} catch (IllegalStateException e) {
			//expected
		}
		try {
			crucibleSession.getReviewers(null);
			fail();
		} catch (IllegalStateException e) {
			//expected
		}

		try {
			crucibleSession.createReview(null);
			fail();
		} catch (IllegalStateException e) {
			//expected
		}

		try {
			crucibleSession.createReviewFromPatch(null, "patch");
			fail();
		} catch (IllegalStateException e) {
			//expected
		}
	}

//	public void testGetAllTypeReviews() throws Exception {
//		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
//		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
//		List<State> states = Arrays.asList(State.values());
//		mockServer.expect("/rest-service/reviews-v1/details", new GetReviewsCallback(states));
//		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
//		mockServer.expect("/rest-service/reviews-v1/metrics/1", new GetMetricsCallback());
//		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);
//
//		apiHandler.login();
//		List<BasicReview> reviews = apiHandler.getReviewsInStates(null);
//		assertEquals(states.size(), reviews.size());
//		int i = 0;
//		for (BasicReview review : reviews) {
//			assertEquals(review.getState(), states.get(i++));
//		}
//		mockServer.verify();
//	}

	public void testGetEmptyReviews() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		List<State> states = new ArrayList<State>();
		mockServer.expect("/rest-service/reviews-v1/details", new GetReviewsCallback(states));
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		List<BasicReview> reviews = apiHandler.getAllReviews();
		assertEquals(states.size(), reviews.size());
		assertTrue(reviews.isEmpty());
		mockServer.verify();
	}

	public void testGetEmptyReviewsForType() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		List<State> states = new ArrayList<State>();
		mockServer.expect("/rest-service/reviews-v1/details", new GetReviewsCallback(states));
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		List<BasicReview> reviews = apiHandler.getAllReviews();
		assertEquals(states.size(), reviews.size());
		assertTrue(reviews.isEmpty());
		mockServer.verify();
	}

//	public void testGetReviewsInStates() throws Exception {
//		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
//		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
//		List<State> states = Arrays.asList(State.REVIEW, State.DRAFT);
//		mockServer.expect("/rest-service/reviews-v1/details", new GetReviewsCallback(states));
//		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
//		mockServer.expect("/rest-service/reviews-v1/metrics/1", new GetMetricsCallback());
//		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);
//
//		apiHandler.login();
//		List<BasicReview> reviews = apiHandler.getReviewsInStates(states);
//		assertEquals(states.size(), reviews.size());
//		assertTrue(!reviews.isEmpty());
//		mockServer.verify();
//	}

	public void testGetMissingReviewsInStates() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		List<State> states = Arrays.asList(State.REVIEW, State.DRAFT);
		mockServer.expect("/rest-service/reviews-v1/details", new GetReviewsCallback(states));
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		List<State> req = Arrays.asList(State.CLOSED);
		List<BasicReview> reviews = apiHandler.getReviewsInStates(req);
		assertTrue(reviews.isEmpty());
		mockServer.verify();
	}

//	public void testGetEmptyRequestReviewsInStates() throws Exception {
//		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
//		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
//		List<State> states = Arrays.asList(State.REVIEW, State.DRAFT);
//		mockServer.expect("/rest-service/reviews-v1/details", new GetReviewsCallback(states));
//		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
//		mockServer.expect("/rest-service/reviews-v1/metrics/1", new GetMetricsCallback());
//		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);
//
//		apiHandler.login();
//		List<State> req = new ArrayList<State>();
//		List<BasicReview> reviews = apiHandler.getReviewsInStates(req);
//		assertEquals(states.size(), reviews.size());
//		assertTrue(!reviews.isEmpty());
//		mockServer.verify();
//	}

	public void testGetAllReviewsMalformedResponse() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1/details", new MalformedResponseCallback());
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		try {
			apiHandler.getAllReviews();
			fail();
		} catch (RemoteApiException e) {
			// expected
		}
		mockServer.verify();
	}

	public void testGetReviewsInStatesMalformedResponse() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1/details", new MalformedResponseCallback());
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		try {
			List<State> states = Arrays.asList(State.REVIEW, State.DRAFT);
			apiHandler.getReviewsInStates(states);
			fail();
		} catch (RemoteApiException e) {
			// expected
		}
		mockServer.verify();
	}

	public void testGetEmptyReviewers() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1/PR-1/reviewers", new GetReviewersCallback(new User[] {}));
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		PermId permId = new PermId("PR-1");
		List<Reviewer> reviewers = apiHandler.getReviewers(permId);
		assertEquals(0, reviewers.size());
		mockServer.verify();
	}

//	public void testGetReviewers() throws Exception {
//		User[] reviewers = new User[3];
//		reviewers[0] = new User("bob");
//		reviewers[1] = new User("alice");
//		reviewers[2] = new User("steve");
//
//		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
//		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
//		mockServer.expect("/rest-service/reviews-v1/PR-1/reviewers", new GetReviewersCallback(reviewers));
//		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);
//
//		apiHandler.login();
//		PermId permId = new PermId("PR-1");
//		List<Reviewer> result = apiHandler.getReviewers(permId);
//		assertEquals(3, result.size());
//		assertEquals(result.get(0).getUsername(), "bob");
//		assertEquals(result.get(1).getUsername(), "alice");
//		assertEquals(result.get(2).getUsername(), "steve");
//		mockServer.verify();
//	}

	public void testGetReviewersInvalidId() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1/PR-2/reviewers", new ErrorResponse(500, ""));
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		PermId permId = new PermId("PR-2");
		try {
			apiHandler.getReviewers(permId);
			fail();
		} catch (RemoteApiException e) {
			// expected
		}

		mockServer.verify();
	}

	public void testGetReviewersMalformedResponse() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1/PR-1/reviewers", new MalformedResponseCallback());
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		PermId permId = new PermId("PR-1");
		try {
			apiHandler.getReviewers(permId);
			fail();
		} catch (RemoteApiException e) {
			// expected
		}

		mockServer.verify();
	}

	public void testCreateReview() throws Exception {
		Review review = createReviewRequest();

		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new CreateReviewCallback());
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		BasicReview response = apiHandler.createReview(review);
		assertEquals(review.getAuthor(), response.getAuthor());
		assertEquals(review.getCreator(), response.getCreator());
		assertEquals(review.getDescription(), response.getDescription());
		assertEquals(review.getModerator(), response.getModerator());
		assertEquals(review.getName(), response.getName());
		assertEquals(review.getProjectKey(), response.getProjectKey());
		assertEquals(State.DRAFT, response.getState());
		assertEquals(CreateReviewCallback.PERM_ID, response.getPermId().getId());

		mockServer.verify();
	}

	public void testCreateReviewMalformedResponse() throws Exception {
		Review review = createReviewRequest();

		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new MalformedResponseCallback());
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		try {
			apiHandler.createReview(review);
			fail();
		} catch (RemoteApiException e) {
			// expected
		}
		mockServer.verify();
	}

	public void testCreateReviewErrorResponse() throws Exception {
		Review review = createReviewRequest();

		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new ErrorResponse(500, ""));
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		try {
			apiHandler.createReview(review);
			fail();
		} catch (RemoteApiException e) {
			// expected
		}

		mockServer.verify();
	}


	public void testCreateReviewFromPatch240() throws Exception {

        //http://localhost:54080/rest-service/reviews-v1

		mockServer.expect("/rest-service/reviews-v1/versionInfo", new Version2x4InfoCallback());
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD, false, true));
		mockServer.expect("/rest-service/reviews-v1", new CreateReviewCallback24());
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);
		PatchAnchorData patchAnchorData = new PatchAnchorDataBean("repoName", "path", "stripCount");

		apiHandler.login();
		Review review = createReviewRequest();
		BasicReview response = apiHandler.createReviewFromPatch(review, "patch text", patchAnchorData);
		assertEquals(review.getAuthor(), response.getAuthor());
		assertEquals(review.getCreator(), response.getCreator());
		assertEquals(review.getDescription(), response.getDescription());
		assertEquals(review.getModerator(), response.getModerator());
		assertEquals(review.getName(), response.getName());
		assertEquals(review.getProjectKey(), response.getProjectKey());
		assertEquals(State.DRAFT, response.getState());
		assertEquals(CreateReviewCallback.PERM_ID, response.getPermId().getId());

		mockServer.verify();
	}

	public void testCreateReviewFromPatch() throws Exception {

        //http://localhost:54080/rest-service/reviews-v1

		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new CreateReviewCallback());
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		Review review = createReviewRequest();
		BasicReview response = apiHandler.createReviewFromPatch(review, "patch text");
		assertEquals(review.getAuthor(), response.getAuthor());
		assertEquals(review.getCreator(), response.getCreator());
		assertEquals(review.getDescription(), response.getDescription());
		assertEquals(review.getModerator(), response.getModerator());
		assertEquals(review.getName(), response.getName());
		assertEquals(review.getProjectKey(), response.getProjectKey());
		assertEquals(State.DRAFT, response.getState());
		assertEquals(CreateReviewCallback.PERM_ID, response.getPermId().getId());

		mockServer.verify();
	}

	public void testCreateReviewFromNullPatch() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new CreateReviewCallback());
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		Review review = createReviewRequest();
		BasicReview response = apiHandler.createReviewFromPatch(review, null);
		assertEquals(review.getAuthor(), response.getAuthor());
		assertEquals(review.getCreator(), response.getCreator());
		assertEquals(review.getDescription(), response.getDescription());
		assertEquals(review.getModerator(), response.getModerator());
		assertEquals(review.getName(), response.getName());
		assertEquals(review.getProjectKey(), response.getProjectKey());
		assertEquals(State.DRAFT, response.getState());
		assertEquals(CreateReviewCallback.PERM_ID, response.getPermId().getId());

		mockServer.verify();
	}

	public void testCreateReviewFromEmptyPatch() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new CreateReviewCallback());
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		Review review = createReviewRequest();
		BasicReview response = apiHandler.createReviewFromPatch(review, "");
		assertEquals(review.getAuthor(), response.getAuthor());
		assertEquals(review.getCreator(), response.getCreator());
		assertEquals(review.getDescription(), response.getDescription());
		assertEquals(review.getModerator(), response.getModerator());
		assertEquals(review.getName(), response.getName());
		assertEquals(review.getProjectKey(), response.getProjectKey());
		assertEquals(State.DRAFT, response.getState());
		assertEquals(CreateReviewCallback.PERM_ID, response.getPermId().getId());

		mockServer.verify();
	}

	public void testCreateReviewFromPatchMalformedResponse() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1", new MalformedResponseCallback());
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		try {
			Review review = createReviewRequest();
			apiHandler.createReviewFromPatch(review, "patch text");
			fail();
		} catch (RemoteApiException e) {
			// expected
		}

		mockServer.verify();
	}

	public void testGetProjectsCrucible1_6() throws Exception {
		int size = 4;

		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
        mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/projects-v1", new GetProjectsCallback(size, false));
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		List<BasicProject> project = apiHandler.getProjects();
		assertEquals(size, project.size());
		for (int i = 0; i < size; i++) {
			String id = Integer.toString(i);
			assertEquals(id, project.get(i).getId());
			assertEquals("ProjectName" + id, project.get(i).getName());
			assertEquals("CR" + id, project.get(i).getKey());
		}
		mockServer.verify();
	}

    public void testGetProjectsCrucible2_0() throws Exception {
		int size = 4;

		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/projects-v1", new GetProjectsCallback(size, true));
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

        apiHandler.login();
		List<BasicProject> project = apiHandler.getProjects();
		assertEquals(size, project.size());
		for (int i = 0; i < size; i++) {
			String id = Integer.toString(i);
			assertEquals(id, project.get(i).getId());
			assertEquals("ProjectName" + id, project.get(i).getName());
			assertEquals("CR" + id, project.get(i).getKey());
        }
		mockServer.verify();
	}

	public void testGetProjects2_0() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/projects-v1", new ResourceCallback(
				"projects2_0.xml"));

		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		List<BasicProject> projects = apiHandler.getProjects();
		assertEquals(98, projects.size());

		BasicProject project = projects.get(12);
		assertEquals("CR-UI", project.getKey());
		assertFalse(project.isJoiningAllowed());
		assertTrue(project.isModeratorEnabled());
		assertEquals(0, project.getDefaultReviewers().size());
		assertEquals("UI", project.getDefaultRepository());

		project = projects.get(3);
		assertEquals("CR-JST", project.getKey());
		assertEquals(5, project.getDefaultReviewers().size());

		mockServer.verify();
	}

	public void testGetProjectCrDemo() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/projects-v1/CR-DEMO", new ResourceCallback(
				"project-CR-DEMO.xml"));

		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		ExtendedCrucibleProject project = apiHandler.getProject("CR-DEMO");
		assertNotNull(project);

		assertEquals("CR-DEMO", project.getKey());
		assertFalse(project.isJoiningAllowed());
		assertTrue(project.isModeratorEnabled());
		assertEquals(1, project.getAllowedReviewers().size());
		assertEquals(0, project.getDefaultReviewers().size());

		mockServer.verify();
	}

	public void testGetProjectsEmpty() throws Exception {
		int size = 0;
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
        mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/projects-v1", new GetProjectsCallback(size, false));
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		List<BasicProject> project = apiHandler.getProjects();
		assertEquals(size, project.size());
		for (int i = 0; i < size; i++) {
			String id = Integer.toString(i);
			assertEquals(id, project.get(i).getId());
			assertEquals("ProjectName" + id, project.get(i).getName());
			assertEquals("CR" + id, project.get(i).getKey());
		}
		mockServer.verify();
	}

	public void testGetRepositories() throws Exception {
		int size = 4;

		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/repositories-v1", new GetRepositoriesCallback(size));
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		List<Repository> repositories = apiHandler.getRepositories();
		assertEquals(size, repositories.size());
		for (int i = 0; i < size; i++) {
			String id = Integer.toString(i);
			assertEquals("RepoName" + id, repositories.get(i).getName());
		}
		mockServer.verify();
	}

	public void testGetRepositoriesEmpty() throws Exception {
		int size = 0;

		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/repositories-v1", new GetRepositoriesCallback(size));
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);
		apiHandler.login();
		List<Repository> repositories = apiHandler.getRepositories();
		assertEquals(size, repositories.size());
		for (int i = 0; i < size; i++) {
			String id = Integer.toString(i);
			assertEquals("RepoName" + id, repositories.get(i).getName());
		}
		mockServer.verify();
	}

	private Review createReviewRequest() {
		Review review = new Review(mockBaseUrl, "PR", new User("author", ""), new User("moderator", ""));
		review.setCreator(new User("creator", ""));
		review.setDescription("description");
		review.setName("name");
		review.setProjectKey("PR");
		return review;
	}

	private CrucibleSessionImpl createCrucibleSession(String url) throws RemoteApiException {
		CrucibleServerCfg serverCfg = new CrucibleServerCfg(url, new ServerIdImpl());
		serverCfg.setUrl(url);
		return new CrucibleSessionImpl(createServerData(serverCfg), new TestHttpSessionCallbackImpl(),
				LoggerImpl.getInstance());
	}

	private CrucibleSessionImpl createCrucibleSession(String url, String username, String password)
			throws RemoteApiException {
		final CrucibleServerCfg serverCfg = new CrucibleServerCfg("mockcrucibleservercfg", new ServerIdImpl());
		serverCfg.setUrl(url);
		serverCfg.setUsername(username);
		serverCfg.setPassword(password);
		return new CrucibleSessionImpl(createServerData(serverCfg), new TestHttpSessionCallbackImpl(),
				LoggerImpl.getInstance());
	}

	public void testGetReviewDetailsWithAddedFile() throws Exception {
		PermId permId = new PermId("CR-4");

		final Review review = getReview(permId, "reviewDetailsResponse-withAddedFile.xml", 2);
		assertNotNull(review);
		assertTrue(review.isAllowReviewerToJoin());
		assertEquals(permId, review.getPermId());
		assertEquals("CR", review.getProjectKey());
		final Set<CrucibleFileInfo> files = review.getFiles();
		assertEquals(3, files.size());
		CrucibleFileInfo fileInfo = null;
		for (CrucibleFileInfo crucibleFileInfo : files) {
			if (crucibleFileInfo.getFileDescriptor().getName().contains("AdjustedBuildStatus.java")) {
				fileInfo = crucibleFileInfo;
				break;
			}
		}
		assertNotNull(fileInfo);
		final List<VersionedComment> vcs = fileInfo.getVersionedComments();
		assertNotNull(vcs);
		assertEquals(2, vcs.size());

		//		<toLineRange>14, 17-19, 25-35</toLineRange>

		final VersionedComment vc1 = vcs.get(0);
		assertEquals("CMT:910", vc1.getPermId().getId());
		assertEquals(0, vc1.getFromStartLine());
		assertEquals(0, vc1.getFromEndLine());
		assertFalse(vc1.isFromLineInfo());
		assertTrue(vc1.isToLineInfo());
		assertEquals(14, vc1.getToStartLine());
		assertEquals(35, vc1.getToEndLine());
		assertEquals(new IntRanges(new IntRange(14), new IntRange(17, 19), new IntRange(25, 35)), vc1.getToLineRanges());
		assertNull(vc1.getFromLineRanges());

		//		<toLineRange>3-5</toLineRange>

		final VersionedComment vc2 = vcs.get(1);
		assertEquals("CMT:909", vc2.getPermId().getId());
		assertFalse(vc2.isFromLineInfo());
		assertEquals(0, vc2.getFromStartLine());
		assertEquals(0, vc2.getFromEndLine());
		assertTrue(vc2.isToLineInfo());
		assertEquals(3, vc2.getToStartLine());
		assertEquals(5, vc2.getToEndLine());
		assertNull(vc2.getFromLineRanges());
		assertEquals(new IntRanges(new IntRange(3, 5)), vc2.getToLineRanges());
	}

	public void testGetReviewDetailsWithoutModerator() throws Exception {
		PermId permId = new PermId("TST-9");

		final Review review = getReview(permId, "reviewDetailsResponse-withoutModerator.xml", 1);
		assertNotNull(review);
		assertNull(review.getModerator());
	}

	public void testGetReviewDetailsWithReplies() throws Exception {
		PermId permId = new PermId("TST-9");

		final Review review = getReview(permId, "reviewDetailsResponse-withReplies.xml", 1);
		assertNotNull(review);
		assertEquals("wseliga", review.getModerator().getUsername());
		assertEquals("Wojciech Seliga", review.getModerator().getDisplayName());
		assertEquals(3, review.getFiles().size());
		final CrucibleFileInfo reviewItem = review.getFileByPermId(new PermId("CFR-45"));
		assertNotNull(reviewItem);
		assertEquals(1, reviewItem.getVersionedComments().size());
		final VersionedComment versionedComment = reviewItem.getVersionedComments().get(0);
		assertEquals("a constructor here", versionedComment.getMessage());
		assertEquals(1, versionedComment.getReplies().size());
		final Comment reply = versionedComment.getReplies().get(0);
		assertEquals("xx", reply.getMessage());
	}

	public void testGetReviewDetails() throws Exception {
		PermId permId = new PermId("TST-9");

		final Review review = getReview(permId, "reviewDetailsResponse.xml", 1);
		assertNotNull(review);
		assertEquals(ReviewType.REVIEW, review.getType());
		assertFalse(review.isAllowReviewerToJoin());
		assertEquals(permId, review.getPermId());
		assertEquals("TST", review.getProjectKey());
		assertNotNull(review.getModerator());
		final Set<CrucibleFileInfo> files = review.getFiles();
		assertEquals(1, files.size());
		final CrucibleFileInfo fileInfo = files.iterator().next();
		final List<VersionedComment> vcs = fileInfo.getVersionedComments();
		assertEquals(2, vcs.size());

		//		<fromLineRange>61, 65-66, 77, 79-80</fromLineRange>
		//		<toLineRange>61, 66-67, 78, 80-82</toLineRange>

		final VersionedComment vc1 = vcs.get(0);
		assertEquals("CMT:908", vc1.getPermId().getId());
		assertEquals(61, vc1.getFromStartLine());
		assertEquals(80, vc1.getFromEndLine());
		assertEquals(61, vc1.getToStartLine());
		assertEquals(82, vc1.getToEndLine());
		assertEquals(new IntRanges(new IntRange(61), new IntRange(65, 66), new IntRange(77), new IntRange(79, 80)),
				vc1.getFromLineRanges());
		assertEquals(new IntRanges(new IntRange(61), new IntRange(66, 67), new IntRange(78), new IntRange(80, 82)),
				vc1.getToLineRanges());

		//		<fromLineRange>64-65, 79-80</fromLineRange>
		//		<toLineRange>65-66, 80, 82</toLineRange>

		final VersionedComment vc2 = vcs.get(1);
		assertEquals("CMT:907", vc2.getPermId().getId());
		assertEquals(64, vc2.getFromStartLine());
		assertEquals(80, vc2.getFromEndLine());
		assertEquals(65, vc2.getToStartLine());
		assertEquals(82, vc2.getToEndLine());
		assertEquals(new IntRanges(new IntRange(64, 65), new IntRange(79, 80)), vc2.getFromLineRanges());
		assertEquals(new IntRanges(new IntRange(65, 66), new IntRange(80), new IntRange(82)), vc2.getToLineRanges());
	}

	private static class ResourceCallback implements JettyMockServer.Callback {
		private final String resourcePath;

		ResourceCallback(String resourcePath) {
			this.resourcePath = resourcePath;
		}

		public void onExpectedRequest(String arg0, HttpServletRequest request, HttpServletResponse response)
				throws Exception {
			new CrucibleMockUtil().copyResource(response.getOutputStream(), resourcePath);
			response.getOutputStream().flush();
		}

	}

	public void testGetReviewsForFilter() throws RemoteApiException {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1/filter/open/details", new ResourceCallback(
				"open-reviews-filter-results-cru-1.6.xml"));
		CrucibleSession session = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);
		session.login();
		final List<BasicReview> reviews = session.getReviewsForFilter(PredefinedFilter.Open);
		mockServer.verify();
		assertEquals(46, reviews.size());

		BasicReview lastReview = reviews.get(reviews.size() - 1);

		final User user = new User("wseliga", "Wojtek Seliga");
		assertEquals(user, lastReview.getAuthor());
		assertEquals(user, lastReview.getModerator());
		assertEquals(user, lastReview.getCreator());
		assertEquals("CR-ACC", lastReview.getProjectKey());
		final Reviewer r1 = new Reviewer("mwent", "Marek Went", false);
		final Reviewer r2 = new Reviewer("spingel", "Steffen Pingel", true);
		final Reviewer r3 = new Reviewer("thomas.ehrnhoefer@tasktop.com", "Thomas Ehrnhoefer", true);
		final Reviewer r4 = new Reviewer("sminto", "Shawn Minto", true);

		TestUtil.assertHasOnlyElements(lastReview.getReviewers(), r1, r2, r3, r4);
		assertFalse(lastReview.isAllowReviewerToJoin());
		assertEquals(1, lastReview.getMetricsVersion());
		assertEquals(State.REVIEW, lastReview.getState());
		assertEquals("CR-ACC-1", lastReview.getPermId().getId());
		assertEquals("ACC-27: Eclipse compile error in BambooSessionImpl - stupid mistake?", lastReview.getName());
		assertTrue(lastReview.getDescription().startsWith("line 378: if (elements"));
		assertEquals(MiscUtil.buildHashSet(CrucibleAction.VIEW, CrucibleAction.RECOVER, CrucibleAction.SUBMIT,
				CrucibleAction.ABANDON, CrucibleAction.MODIFY_FILES, CrucibleAction.APPROVE, CrucibleAction.REJECT,
				CrucibleAction.COMMENT, CrucibleAction.CREATE, CrucibleAction.SUMMARIZE, CrucibleAction.CLOSE,
				CrucibleAction.REOPEN), lastReview.getActions());
	}

	private Review getReview(final PermId permId, final String resource, int numRepos) throws RemoteApiException {
		final int size = 4;
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1/" + permId.getId() + "/details", new JettyMockServer.Callback() {
			public void onExpectedRequest(final String target, final HttpServletRequest request,
					final HttpServletResponse response) throws Exception {
				new CrucibleMockUtil().copyResource(response.getOutputStream(), resource);
				response.getOutputStream().flush();
			}
		});
		for (int i = 0; i < numRepos; i++) {
			mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
			mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
			mockServer.expect("/rest-service/repositories-v1", new GetRepositoriesCallback(size));
		}
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		final Review review = apiHandler.getReview(permId);
		mockServer.verify();
		return review;
	}

	private ConnectionCfg createServerData(ServerCfg serverCfg) {
		return new ConnectionCfg(serverCfg.getServerId().getId(), serverCfg.getUrl(), serverCfg.getUsername(), serverCfg
				.getPassword());
	}

	public void testGetChanges() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/repositories-v1/changes/PLE/", new JettyMockServer.Callback() {
			public void onExpectedRequest(String arg0, HttpServletRequest request, HttpServletResponse response)
					throws Exception {
				assertTrue(request.getPathInfo().endsWith("/rest-service/repositories-v1/changes/PLE/"));

				new CrucibleMockUtil().copyResource(response.getOutputStream(), "changes.xml");
				response.getOutputStream().flush();
			}
		});
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		Changes changes = apiHandler.getChanges("PLE", null, false, null, false, null);
		assertNotNull(changes.getChanges());

		mockServer.verify();
	}

	public void testGetChangesForMissingRepository() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/repositories-v1/changes/PLE/", new JettyMockServer.Callback() {
			public void onExpectedRequest(String arg0, HttpServletRequest request, HttpServletResponse response)
					throws Exception {
				assertTrue(request.getPathInfo().endsWith("/rest-service/repositories-v1/changes/PLE/"));

				new CrucibleMockUtil().copyResource(response.getOutputStream(), "changes-repository-not-found.xml");
				response.getOutputStream().flush();
			}
		});
		CrucibleSession apiHandler = createCrucibleSession(mockBaseUrl, USER_NAME, PASSWORD);

		apiHandler.login();
		try {
			apiHandler.getChanges("PLE", null, false, null, false, null);
			fail();
		} catch (RemoteApiException e) {
		}

		mockServer.verify();
	}
}