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

package com.atlassian.theplugin.crucible.api.rest.cruciblemock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import org.ddsteps.mock.httpserver.JettyMockServer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginCallback implements JettyMockServer.Callback {

	private final User expectedUser;
	private final String expectedPassword;
	private final boolean fail;
	private final boolean shouldExpectPost;

	public static final boolean ALWAYS_FAIL = true;
	public static final String AUTH_TOKEN = "authtokenstring";

	public LoginCallback(String expectedUserName, String expectedPassword) {
		this(expectedUserName, expectedPassword, false);
	}

	public LoginCallback(String expectedUserName, String expectedPassword, boolean alwaysFail) {
		this(expectedUserName, expectedPassword, alwaysFail, false);

	}

	public LoginCallback(String expectedUserName, String expectedPassword, boolean alwaysFail,
			boolean shouldExpectPost) {
		this.expectedUser = new User(expectedUserName);
		this.expectedPassword = expectedPassword;

		fail = alwaysFail;
		this.shouldExpectPost = shouldExpectPost;
	}

	public void onExpectedRequest(String target, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		assertEquals(shouldExpectPost ? "POST" : "GET", request.getMethod());
		assertTrue(request.getPathInfo().endsWith("/rest-service/auth-v1/login"));

		final String[] usernames = request.getParameterValues("userName");
		final String[] passwords = request.getParameterValues("password");

		assertEquals(1, usernames.length);
		assertEquals(1, passwords.length);

		final String username = usernames[0];
		final String password = passwords[0];

		assertNotNull(username);
		assertNotNull(password);

		assertEquals(expectedUser.getUsername(), username);
		assertEquals(expectedPassword, password);

		new CrucibleMockUtil().copyResource(response.getOutputStream(), fail ? "loginFailedResponse.xml" : "loginSuccessResponse.xml");
		response.getOutputStream().flush();

	}


}