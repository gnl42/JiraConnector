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

package com.atlassian.connector.commons.misc;

import org.ddsteps.mock.httpserver.JettyMockServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ErrorResponse implements JettyMockServer.Callback {
	private final int errorCode;

	private final static String ERROR_PREFIX = "HTTP ";
	private final static String ERROR_MESSAGE = "error text";
	private String errorDescription;

	public ErrorResponse(int error, String errorDescription) {
		this.errorCode = error;
		this.errorDescription = errorDescription;
	}

	public void onExpectedRequest(String target, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.sendError(errorCode, ERROR_MESSAGE);
	}

	public String getErrorMessage() {
		return ERROR_PREFIX + errorCode + " (" + errorDescription + ")" + " \n" + ERROR_MESSAGE;
	}

	public static String getStaticErrorMessage(int error, String errorDesc) {
		return ERROR_PREFIX + error + " (" + errorDesc + ") \n" + ERROR_MESSAGE;
	}
}
