package com.atlassian.theplugin.commons.fisheye.api.rest.mock;

import com.atlassian.theplugin.commons.util.ResourceUtil;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User: pmaruszak
 */
public class FishEyeLogoutCallback implements JettyMockServer.Callback {

	private final String expectedToken;

	public FishEyeLogoutCallback() {
		this(FishEyeLoginCallback.AUTH_TOKEN);
	}

	public FishEyeLogoutCallback(String expectedToken) {
		this.expectedToken = expectedToken;
	}


	public void onExpectedRequest(String target,
								  HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		assertTrue(request.getPathInfo().endsWith("/api/rest/logout"));

		final String[] authTokens = request.getParameterValues("auth");
		assertEquals(1, authTokens.length);

		final String authToken = authTokens[0];
		assertEquals(expectedToken, authToken);

		ResourceUtil.copyResource(response.getOutputStream(), FishEyeLogoutCallback.class, "logoutResponse.xml");
		response.getOutputStream().flush();

	}

}
