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
package com.atlassian.theplugin.bamboo.api.bamboomock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStreamWriter;
import java.util.Properties;

public class LatestBuildResultVelocityCallback implements JettyMockServer.Callback {

	private final String buildKey;

	private final int buildNumber;

	public LatestBuildResultVelocityCallback(final String buildKey, int buildNumber) {
		this.buildKey = buildKey;
		this.buildNumber = buildNumber;
	}

	public void onExpectedRequest(String target, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		assertTrue(request.getPathInfo().endsWith("/api/rest/getLatestBuildResults.action"));

		final String[] authTokens = request.getParameterValues("auth");
		final String[] buildKeys = request.getParameterValues("buildKey");

		assertEquals(1, authTokens.length);
		assertEquals(1, buildKeys.length);

		final String authToken = authTokens[0];
		final String myBuildKey = buildKeys[0];

		assertEquals(LoginCallback.AUTH_TOKEN, authToken);
		assertEquals(buildKey, myBuildKey);

		VelocityEngine velocityEngine = new VelocityEngine();
		Properties props = new Properties();
		props.setProperty("resource.loader", "class");
		props.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
		velocityEngine.init(props);
		final VelocityContext context = new VelocityContext();
		context.put("buildKey", buildKey);
		context.put("buildNumber", buildNumber);

		final OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
		velocityEngine.mergeTemplate("/mock/bamboo/1_2_4/api/rest/latestBuildResultResponse.vm", "UTF-8", context,
				writer);
		writer.flush();
		response.getOutputStream().flush();

	}

}
