package com.atlassian.theplugin.bamboo.api.bamboomock;

import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static junit.framework.Assert.assertTrue;

/**
 * User: kalamon
 * Date: Jul 6, 2009
 * Time: 1:03:23 PM
 */
public class BuildForPlanAndNumberCallback implements JettyMockServer.Callback {

    private final String resource;
    private final String expectedPath;

    public BuildForPlanAndNumberCallback(String resourcePath, String expectedPath) {
        this.expectedPath = expectedPath;
        resource = resourcePath + "getBuildForPlanAndNumberResponse.xml";
    }

    public void onExpectedRequest(String target, HttpServletRequest request, HttpServletResponse response)
            throws Exception {


        assertTrue(request.getPathInfo().contains(expectedPath));

		// final String[] authTokens = request.getParameterValues("auth");
		//
		// assertEquals(1, authTokens.length);
		//
		// final String authToken = authTokens[0];
		//
		// assertEquals(LoginCallback.AUTH_TOKEN, authToken);

        Util.copyResourceWithFullPath(response.getOutputStream(), resource);
        response.getOutputStream().flush();
    }
}
