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

package com.atlassian.theplugin.commons.exception;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Mar 3, 2008
 * Time: 4:50:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class IncorrectVersionException extends ThePluginException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7685605767646337323L;

	public IncorrectVersionException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public IncorrectVersionException(String message) {
		super(message);
	}
}
