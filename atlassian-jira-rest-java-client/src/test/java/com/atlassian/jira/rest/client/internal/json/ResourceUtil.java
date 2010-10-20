/*
 * Copyright (C) 2010 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.jira.rest.client.internal.json;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class ResourceUtil {
	public static JSONObject getJsonObjectFromResource(String resourcePath) {
		final String s = getStringFromResource(resourcePath);
		try {
			return new JSONObject(s);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}

	}

	public static String getStringFromResource(String resourcePath) {
		final String s;
		try {
            final InputStream is = ResourceUtil.class.getResourceAsStream(resourcePath);
            if (is == null) {
                throw new IOException("Cannot open resource [" + resourcePath + "]");
            }
            s = IOUtils.toString(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return s;
	}
}
