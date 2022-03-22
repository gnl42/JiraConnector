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

public class BuildDetailsResultCallback27 implements Callback {

	private final String responsePath;
	private final String responseFile;

	public BuildDetailsResultCallback27(String responsePath, String responseFile) {
		this.responsePath = responsePath;
		this.responseFile = responseFile;
	}

	public void onExpectedRequest(String target, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Util.copyResourceWithFullPath(response.getOutputStream(), responsePath + responseFile);
		response.getOutputStream().flush();
	}

}
