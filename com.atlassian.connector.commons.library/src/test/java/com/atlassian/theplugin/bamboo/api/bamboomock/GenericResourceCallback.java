/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.theplugin.bamboo.api.bamboomock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.ddsteps.mock.httpserver.JettyMockServer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GenericResourceCallback implements JettyMockServer.Callback {

	private final String resource;
	private final String expectedUri;

	public GenericResourceCallback(String resourcePath, String expectedUri) {
		this.resource = resourcePath;
		this.expectedUri = expectedUri;
	}

	public void onExpectedRequest(String target, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		assertTrue(request.getPathInfo().contains(expectedUri));

		final String[] authTokens = request.getParameterValues("auth");

		assertEquals(1, authTokens.length);

		final String authToken = authTokens[0];

		assertEquals(LoginCallback.AUTH_TOKEN, authToken);

        Util.copyResourceWithFullPath(response.getOutputStream(), resource);
		response.getOutputStream().flush();
	}
}
