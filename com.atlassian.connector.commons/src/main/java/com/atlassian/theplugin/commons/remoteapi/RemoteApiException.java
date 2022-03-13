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

package com.atlassian.theplugin.commons.remoteapi;

import org.jetbrains.annotations.Nullable;

/**
 * Generic exception related to a remote session.
 */
public class RemoteApiException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -8518437777396192588L;

	@Nullable
	public String getServerStackTrace() {
		return serverStackTrace;
	}

	private final String serverStackTrace;

	public RemoteApiException(String message) {
		super(message);
		serverStackTrace = null;
	}

	public RemoteApiException(String message, @Nullable String serverStackTrace) {
		super(message);
		this.serverStackTrace = serverStackTrace;
	}

	public RemoteApiException(Throwable throwable) {
		super(throwable);
		serverStackTrace = null;
	}

	public RemoteApiException(String message, Throwable throwable) {
		super(message, throwable);
		serverStackTrace = null;
	}
}