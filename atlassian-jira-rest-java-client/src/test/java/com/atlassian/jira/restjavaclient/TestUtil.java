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

package com.atlassian.jira.restjavaclient;

import com.sun.jersey.api.client.UniformInterfaceException;
import junit.framework.Assert;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class TestUtil {
	private static DateTimeFormatter formatter = ISODateTimeFormat.dateTime();

	public static URI toUri(String str) {
		try {
			return new URI(str);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public static DateTime toDateTime(String isoDateTimeSt) {
		return formatter.parseDateTime(isoDateTimeSt);
	}

	public static void assertErrorCode(int errorCode, Runnable runnable) {
		try {
			runnable.run();
			Assert.fail(UniformInterfaceException.class + " exception expected");
		} catch (UniformInterfaceException e) {
			Assert.assertEquals(errorCode, e.getResponse().getStatus());
		} catch (RestClientException e) {
			Assert.assertTrue(e.getCause() instanceof UniformInterfaceException);
			Assert.assertEquals(errorCode, ((UniformInterfaceException)e.getCause()).getResponse().getStatus());
		}

	}
}
