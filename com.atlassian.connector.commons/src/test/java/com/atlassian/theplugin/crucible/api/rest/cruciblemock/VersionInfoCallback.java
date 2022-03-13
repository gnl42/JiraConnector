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

import static junit.framework.Assert.assertTrue;
import org.ddsteps.mock.httpserver.JettyMockServer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class VersionInfoCallback implements JettyMockServer.Callback {
	private final boolean cru16;

	public VersionInfoCallback(boolean cru16) {
		this.cru16 = cru16;
	}

	public void onExpectedRequest(String target, HttpServletRequest request, HttpServletResponse response) throws Exception {
		assertTrue(request.getPathInfo().endsWith("/rest-service/reviews-v1/versionInfo"));

		if (cru16) {
			new CrucibleMockUtil().copyResource(response.getOutputStream(), "versionInfoSuccessResponse.xml");
		} else {
			new CrucibleMockUtil().copyResource(response.getOutputStream(), "versionInfoFailureResponse.xml");
			response.setStatus(500);
		}
		response.getOutputStream().flush();

	}
}
