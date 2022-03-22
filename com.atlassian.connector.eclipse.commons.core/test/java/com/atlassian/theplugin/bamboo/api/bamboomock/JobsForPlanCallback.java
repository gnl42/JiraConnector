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

import org.ddsteps.mock.httpserver.JettyMockServer.Callback;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JobsForPlanCallback implements Callback {

	private final String resourcePath;

	public JobsForPlanCallback(String resourcePath) {
		this.resourcePath = resourcePath + "getJobsForPlanResponse.xml";
	}

	public void onExpectedRequest(String target, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

        Util.copyResourceWithFullPath(response.getOutputStream(), resourcePath);
		response.getOutputStream().flush();
	}

}
