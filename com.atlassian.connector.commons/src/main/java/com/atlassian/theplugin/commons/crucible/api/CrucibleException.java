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

package com.atlassian.theplugin.commons.crucible.api;

import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-05
 * Time: 16:14:11
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleException extends RemoteApiException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7182791443222497268L;

	public CrucibleException(String message) {
		super(message);
	}

	public CrucibleException(Throwable throwable) {
		super(throwable);
	}

	public CrucibleException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
