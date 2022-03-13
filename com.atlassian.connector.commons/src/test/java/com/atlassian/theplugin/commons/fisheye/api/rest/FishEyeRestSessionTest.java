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
package com.atlassian.theplugin.commons.fisheye.api.rest;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.remoteapi.TestHttpSessionCallbackImpl;
import com.atlassian.theplugin.api.AbstractSessionTest;
import com.atlassian.theplugin.commons.fisheye.api.model.FisheyePathHistoryItem;
import com.atlassian.theplugin.commons.fisheye.api.model.changeset.Changeset;
import com.atlassian.theplugin.commons.fisheye.api.model.changeset.ChangesetIdList;
import com.atlassian.theplugin.commons.fisheye.api.rest.mock.FishEyeLoginCallback;
import com.atlassian.theplugin.commons.fisheye.api.rest.mock.FishEyeLogoutCallback;
import com.atlassian.theplugin.commons.fisheye.api.rest.mock.FisheyeMockUtil;
import com.atlassian.theplugin.commons.remoteapi.ProductSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import org.ddsteps.mock.httpserver.JettyMockServer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * FishEyeRestSession Tester.
 *
 * @author wseliga
 */
public class FishEyeRestSessionTest extends AbstractSessionTest {

	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Override
	protected String getLoginUrl() {
		return "/api/rest/login";
	}

	@Override
	protected ProductSession getProductSession(final String url) throws RemoteApiMalformedUrlException {
		return createSession(url);
	}

	@Override
	protected JettyMockServer.Callback getLoginCallback(final boolean isFail) {
		return new FishEyeLoginCallback(USER_NAME, PASSWORD, isFail);
	}

	public void xtestAdjustHttpHeader() {
		//TODO: wseliga implement it
		fail("unimplemented");
	}

	public void xtestPreprocessResult() {
		//TODO: wseliga implement it
		fail("unimplemented");
	}

	public void xtestGetDocument() {
		//TODO: wseliga implement it
		fail("unimplemented");
	}

	public void xtestGetLastModified() {
		//TODO: wseliga implement it
		fail("unimplemented");
	}

	public void xtestGetEtag() {
		//TODO: wseliga implement it
		fail("unimplemented");
	}

	public void testSuccessLoginURLWithSlash() throws Exception {
		mockServer.expect(FishEyeRestSession.LOGIN_ACTION, new FishEyeLoginCallback(USER_NAME, PASSWORD));
		mockServer.expect(FishEyeRestSession.LOGOUT_ACTION, new FishEyeLogoutCallback(FishEyeLoginCallback.AUTH_TOKEN));

		FishEyeRestSession apiHandler = createSession(mockBaseUrl + "/");
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		assertTrue(apiHandler.isLoggedIn());
		apiHandler.logout();
		assertFalse(apiHandler.isLoggedIn());

		mockServer.verify();
	}

	public void testNullParamsLogin() throws Exception {
		try {
			FishEyeRestSession apiHandler = createSession(null);
			apiHandler.login(null, null);
			fail();
		} catch (RemoteApiException ex) {
		}
	}

	public void testWrongParamsLogin() throws Exception {
		try {
			FishEyeRestSession apiHandler = createSession("");
			apiHandler.login("", "".toCharArray());
			fail();
		} catch (RemoteApiException ex) {
		}
	}

    public void testGetPathHistory() throws Exception {
        mockServer.expect(FishEyeRestSession.LOGIN_ACTION, new FishEyeLoginCallback(USER_NAME, PASSWORD));
        mockServer.expect(FishEyeRestSession.LIST_HISTORY_ACTION + "a",
                new JettyMockServer.Callback() {
                    public void onExpectedRequest(String target, HttpServletRequest request,
                                                  HttpServletResponse response) throws Exception {
                        assertEquals("b", request.getParameter("path"));
                        new FisheyeMockUtil().copyResource(response.getOutputStream(), "pathHistorySuccessResponse.xml");
                        response.getOutputStream().flush();
                    }
                });
        mockServer.expect(FishEyeRestSession.LOGOUT_ACTION, new FishEyeLogoutCallback(FishEyeLoginCallback.AUTH_TOKEN));

        FishEyeRestSession apiHandler = createSession(mockBaseUrl + "/");
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        Collection<FisheyePathHistoryItem> history = apiHandler.getPathHistory("a", "b");
        apiHandler.logout();

        assertNotNull(history);
        assertEquals(132, history.size());
        for (FisheyePathHistoryItem item : history) {
            assertNotNull(item.getPath());
            assertNotNull(item.getAuthor());
            assertNotNull(item.getRev());
            assertNotNull(item.getAncestor());
        }

        mockServer.verify();
    }

    public void testGetPathHistoryFailure() throws Exception {
        mockServer.expect(FishEyeRestSession.LOGIN_ACTION, new FishEyeLoginCallback(USER_NAME, PASSWORD));
        mockServer.expect(FishEyeRestSession.LIST_HISTORY_ACTION + "a",
                new JettyMockServer.Callback() {
                    public void onExpectedRequest(String target, HttpServletRequest request,
                                                  HttpServletResponse response) throws Exception {
                        assertEquals("b", request.getParameter("path"));
                        new FisheyeMockUtil().copyResource(response.getOutputStream(), "pathHistoryErrorResponse.xml");
                        response.getOutputStream().flush();
                    }
                });
        mockServer.expect(FishEyeRestSession.LOGOUT_ACTION, new FishEyeLogoutCallback(FishEyeLoginCallback.AUTH_TOKEN));

        try {
            FishEyeRestSession apiHandler = createSession(mockBaseUrl + "/");
            apiHandler.login(USER_NAME, PASSWORD.toCharArray());
            apiHandler.getPathHistory("a", "b");
            apiHandler.logout();

            mockServer.verify();
            fail();
        } catch (RemoteApiException e) {
            assertTrue(e.getMessage().endsWith(FishEyeRestSession.SERVER_RETURNED_MALFORMED_RESPONSE));
        }
    }

    public void testGetPathHistoryBogusResponse() throws Exception {
        mockServer.expect(FishEyeRestSession.LOGIN_ACTION, new FishEyeLoginCallback(USER_NAME, PASSWORD));
        mockServer.expect(FishEyeRestSession.LIST_HISTORY_ACTION + "a",
                new JettyMockServer.Callback() {
                    public void onExpectedRequest(String target, HttpServletRequest request,
                                                  HttpServletResponse response) throws Exception {
                        assertEquals("b", request.getParameter("path"));
                        new FisheyeMockUtil().copyResource(response.getOutputStream(), "corruptResponse.xml");
                        response.getOutputStream().flush();
                    }
                });
        mockServer.expect(FishEyeRestSession.LOGOUT_ACTION, new FishEyeLogoutCallback(FishEyeLoginCallback.AUTH_TOKEN));

        try {
            FishEyeRestSession apiHandler = createSession(mockBaseUrl + "/");
            apiHandler.login(USER_NAME, PASSWORD.toCharArray());
            apiHandler.getPathHistory("a", "b");
            apiHandler.logout();

            mockServer.verify();
            fail();
        } catch (RemoteApiException e) {
            assertTrue(e.getMessage().endsWith(FishEyeRestSession.SERVER_RETURNED_MALFORMED_RESPONSE));
        }
    }

	private FishEyeRestSession createSession(String url) throws RemoteApiMalformedUrlException {
		return new FishEyeRestSession(new ConnectionCfg("id", url, "", ""), new TestHttpSessionCallbackImpl());
	}

	public void testGetChangesetList() throws Exception {
		mockServer.expect(FishEyeRestSession.LOGIN_ACTION, new FishEyeLoginCallback(USER_NAME, PASSWORD));
		mockServer.expect(FishEyeRestSession.CHANGESET_LIST_ACTION + "a", new JettyMockServer.Callback() {
			public void onExpectedRequest(String target, HttpServletRequest request, HttpServletResponse response)
					throws Exception {
				new FisheyeMockUtil().copyResource(response.getOutputStream(), "changesetList.xml");
				response.getOutputStream().flush();
			}
		});
		mockServer.expect(FishEyeRestSession.LOGOUT_ACTION, new FishEyeLogoutCallback(FishEyeLoginCallback.AUTH_TOKEN));

		FishEyeRestSession apiHandler = createSession(mockBaseUrl + "/");
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		ChangesetIdList history = apiHandler.getChangesetList("a", null, null, null, null);
		apiHandler.logout();

		assertNotNull(history);
		assertEquals(3000, history.getCsids().size());

		mockServer.verify();
	}

	public void testGetChangeset() throws Exception {
		mockServer.expect(FishEyeRestSession.LOGIN_ACTION, new FishEyeLoginCallback(USER_NAME, PASSWORD));
		mockServer.expect(FishEyeRestSession.CHANGESET_ACTION + "a/5", new JettyMockServer.Callback() {
			public void onExpectedRequest(String target, HttpServletRequest request, HttpServletResponse response)
					throws Exception {
				new FisheyeMockUtil().copyResource(response.getOutputStream(), "changeset.xml");
				response.getOutputStream().flush();
			}
		});
		mockServer.expect(FishEyeRestSession.LOGOUT_ACTION, new FishEyeLogoutCallback(FishEyeLoginCallback.AUTH_TOKEN));

		FishEyeRestSession apiHandler = createSession(mockBaseUrl + "/");
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		Changeset history = apiHandler.getChangeset("a", "5");
		apiHandler.logout();

		assertNotNull(history);
		assertEquals(4, history.getRevisionKeys().size());

		mockServer.verify();
	}
}
