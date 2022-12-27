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

package me.glindholm.theplugin.commons.exception;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Apr 30, 2008
 * Time: 3:20:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class HttpProxySettingsException extends Exception {
		/**
	 * 
	 */
	private static final long serialVersionUID = 2091368578815429676L;

		public HttpProxySettingsException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public HttpProxySettingsException(String message) {
		super(message);
	}
}
