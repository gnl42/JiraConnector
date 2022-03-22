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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ResourceUtil {
	public static void copyResource(final OutputStream outputStream, final Class<?> clazz, final String resource) {
		InputStream inputStream = clazz.getResourceAsStream(resource);
		if (inputStream == null) {
			throw new NullPointerException();
		}

		BufferedInputStream is = new BufferedInputStream(inputStream);
		int c;
		try {
			while ((c = is.read()) != -1) {
				outputStream.write(c);
			}
			outputStream.close();
		} catch (IOException e) {
			junit.framework.Assert.fail(e.getMessage());
		}
	}
}
