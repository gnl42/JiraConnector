package com.atlassian.theplugin.crucible.api.rest.cruciblemock;

import static junit.framework.Assert.assertTrue;
import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Wojciech Seliga
 */
public class GetMetricsCallback implements JettyMockServer.Callback {
	public void onExpectedRequest(String target,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		assertTrue(request.getPathInfo().endsWith("/rest-service/reviews-v1/metrics/1"));
		new CrucibleMockUtil().copyResource(response.getOutputStream(), "metricsResponse.xml");
		response.getOutputStream().flush();
	}

}
