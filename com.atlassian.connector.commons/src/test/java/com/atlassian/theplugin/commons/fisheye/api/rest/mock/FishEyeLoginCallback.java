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
package com.atlassian.theplugin.commons.fisheye.api.rest.mock;

import com.atlassian.theplugin.commons.util.ResourceUtil;
import static junit.framework.Assert.*;
import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FishEyeLoginCallback implements JettyMockServer.Callback {

	private final String expectedUsername;
	private final String expectedPassword;
	private final boolean fail;

	public static final boolean ALWAYS_FAIL = true;
	public static final String AUTH_TOKEN = "username:559:eb9118f34d38fac5946953220fac7fff";

	public FishEyeLoginCallback(String expectedUsername, String expectedPassword) {
		this(expectedUsername, expectedPassword, false);
	}

	public FishEyeLoginCallback(String expectedUsername, String expectedPassword, boolean alwaysFail) {
		this.expectedUsername = expectedUsername;
		this.expectedPassword = expectedPassword;

		fail = alwaysFail;
	}

	public void onExpectedRequest(String target,
								  HttpServletRequest request, HttpServletResponse response)
			throws Exception {


		assertTrue(request.getPathInfo().endsWith("/api/rest/login"));

		final String[] usernames = request.getParameterValues("username");
		final String[] passwords = request.getParameterValues("password");

		assertEquals(1, usernames.length);
		assertEquals(1, passwords.length);

		final String username = usernames[0];
		final String password = passwords[0];

		assertNotNull(username);
		assertNotNull(password);

		assertEquals(expectedUsername, username);
		assertEquals(expectedPassword, password);

		ResourceUtil.copyResource(response.getOutputStream(), FishEyeLoginCallback.class,
				fail ? "loginFailedResponse.xml" : "loginSuccessResponse.xml");
		response.getOutputStream().flush();

	}


}

