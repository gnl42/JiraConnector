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

package com.atlassian.theplugin.eclipse.view.popup;

/**
 * @author Mik Kersten
 */
public class RepositoryTaskHandleUtil {

	public static final String HANDLE_DELIM = "-";

	private static final String MISSING_REPOSITORY = "norepository";

	public static String getHandle(String repositoryUrl, String taskId) {
		if (!isValidTaskId(taskId)) {
			throw new RuntimeException("invalid handle for task, can not contain: " + HANDLE_DELIM + ", was: " + taskId);
		}

		if (repositoryUrl == null) {
			return MISSING_REPOSITORY + HANDLE_DELIM + taskId;
		} else {
			return repositoryUrl + HANDLE_DELIM + taskId;
		}
	}

	public static String getRepositoryUrl(String taskHandle) {
		int index = taskHandle.lastIndexOf(RepositoryTaskHandleUtil.HANDLE_DELIM);
		String url = null;
		if (index != -1) {
			url = taskHandle.substring(0, index);
		}
		return url;
	}

	public static String getTaskId(String taskHandle) {
		int index = taskHandle.lastIndexOf(HANDLE_DELIM);
		if (index != -1) {
			String id = taskHandle.substring(index + 1);
			return id;
		}
		return null;
	}

	public static boolean isValidTaskId(String taskId) {
		return !taskId.contains(HANDLE_DELIM);
	}

}
