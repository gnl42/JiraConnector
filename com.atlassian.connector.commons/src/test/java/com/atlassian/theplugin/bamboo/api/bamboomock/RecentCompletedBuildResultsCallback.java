package com.atlassian.theplugin.bamboo.api.bamboomock;

import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;

/**
 * User: kalamon
 * Date: Jul 6, 2009
 * Time: 12:42:39 PM
 */
public class RecentCompletedBuildResultsCallback implements JettyMockServer.Callback {

    private final String resource;

    // empty response by default
    public RecentCompletedBuildResultsCallback() {
        resource = Util.RESOURCE_BASE_2_1_5 + "getEmptyRecentCompletedBuildResultsResponse.xml";
    }

    public void onExpectedRequest(String target, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        assertTrue(request.getPathInfo().endsWith("/api/rest/getRecentlyCompletedBuildResultsForBuild.action"));

        final String[] authTokens = request.getParameterValues("auth");
        final String[] buildKeys = request.getParameterValues("buildKey");

        assertEquals(1, authTokens.length);
        assertEquals(1, buildKeys.length);

        final String authToken = authTokens[0];

        assertEquals(LoginCallback.AUTH_TOKEN, authToken);

        Util.copyResourceWithFullPath(response.getOutputStream(), resource);
        response.getOutputStream().flush();
    }
}
