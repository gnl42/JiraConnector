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

package com.atlassian.theplugin.commons.util;

/**
 * @author Jacek
 * @date 2008-05-02
 */
public interface Logger {
	boolean isDebugEnabled();

	void error(String msg);

	void error(String msg, Throwable t);

	void error(Throwable t);

	void warn(String msg);

	void warn(String msg, Throwable t);

	void warn(Throwable t);

	void info(String msg);

	void info(String msg, Throwable t);

	void info(Throwable t);

	void verbose(String msg);

	void verbose(String msg, Throwable t);

	void verbose(Throwable t);

	void debug(String msg);

	void debug(String msg, Throwable t);

	void debug(Throwable t);

	void log(int level, String msg, Throwable t);
}
