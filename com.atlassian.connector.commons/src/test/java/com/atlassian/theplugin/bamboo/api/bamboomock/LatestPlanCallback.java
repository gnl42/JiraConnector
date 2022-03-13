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

import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static junit.framework.Assert.assertTrue;

public class LatestPlanCallback implements JettyMockServer.Callback {

	private int errorReason = NON_ERROR;
    private final String resourcePrefixPath;
    private final String file;
	private final String planKey;

	public static final int NON_ERROR = 0;
	public static final int NON_EXIST_FAIL = 1;

	public LatestPlanCallback() {
		this("ECL-DPL", "planResponse.xml", NON_ERROR, Util.RESOURCE_BASE_2_3);
	}

	public LatestPlanCallback(String planKey, String file, int reason, String resourcePrefixPath) {
		this.planKey = planKey;
		this.file = file;
		this.errorReason = reason;
        this.resourcePrefixPath = resourcePrefixPath;
    }

	public LatestPlanCallback(String file) {
		this("ECL-DPL", file, NON_ERROR, Util.RESOURCE_BASE_2_3);
	}

	public LatestPlanCallback(int reason) {
		this("ECL-DPL", "planResponse.xml", reason, Util.RESOURCE_BASE_2_3);
	}

	public void onExpectedRequest(String target,
								  HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		assertTrue(request.getPathInfo().endsWith("/rest/api/latest/plan/" + planKey));

		// final String[] authTokens = request.getParameterValues("auth");
		//
		// assertEquals(1, authTokens.length);
		//
		// final String authToken = authTokens[0];
		//
		// assertEquals(LoginCallback.AUTH_TOKEN, authToken);

		switch (errorReason) {
			case NON_ERROR:
				Util.copyResourceWithFullPath(response.getOutputStream(), resourcePrefixPath + file);
				break;
			case NON_EXIST_FAIL:
				Util.copyResourceWithFullPath(response.getOutputStream(), resourcePrefixPath + "planNotFoundResponse.xml");
				break;
		}
		response.getOutputStream().flush();
	}

}