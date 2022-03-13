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

import static junit.framework.Assert.assertTrue;
import org.ddsteps.mock.httpserver.JettyMockServer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author jjaroczynski@atlassian.com
 */
public class BamboBuildNumberCalbackHttp500 implements JettyMockServer.Callback {
    private String fullPath;

    public BamboBuildNumberCalbackHttp500() {
    }

    public BamboBuildNumberCalbackHttp500(String fullPath) {
        this.fullPath = fullPath;
    }

    public void onExpectedRequest(String target, HttpServletRequest request, HttpServletResponse response) throws Exception {
		assertTrue(request.getPathInfo().endsWith("/api/rest/getBambooBuildNumber.action"));

		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		// if (fullPath == null) {
		// Util.copyResource(response.getOutputStream(), "bambooBuildNumberResponse.xml");
		// } else {
		// Util.copyResourceWithFullPath(response.getOutputStream(), fullPath);
		// }

		// response.getOutputStream().flush();
	}
}
