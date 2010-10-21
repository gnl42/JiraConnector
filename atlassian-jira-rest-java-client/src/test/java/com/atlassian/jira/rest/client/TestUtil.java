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

package com.atlassian.jira.rest.client;

import com.sun.jersey.api.client.UniformInterfaceException;
import junit.framework.Assert;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

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
		assertErrorCode(errorCode, null, runnable);
	}


	public static void assertErrorCode(Response.Status status, String message, Runnable runnable) {
		assertErrorCode(status.getStatusCode(), message, runnable);
	}

	public static void assertErrorCode(Response.Status status, Runnable runnable) {
		assertErrorCode(status.getStatusCode(), null, runnable);
	}

	public static void assertErrorCode(int errorCode, String message, Runnable runnable) {
		try {
			runnable.run();
			Assert.fail(UniformInterfaceException.class + " exception expected");
		} catch (UniformInterfaceException e) {
			Assert.assertEquals(errorCode, e.getResponse().getStatus());
		} catch (RestClientException e) {
			if (message != null) {
				Assert.assertEquals(message, e.getMessage());
			}
			Assert.assertTrue(e.getCause() instanceof UniformInterfaceException);
			Assert.assertEquals(errorCode, ((UniformInterfaceException) e.getCause()).getResponse().getStatus());
		}
	}


	public static String getLastPathSegment(URI uri) {
		final String path = uri.getPath();
		final int index = path.lastIndexOf('/');
		if (index == -1) {
			return path;
		}
		if (index == path.length()) {
			return "";
		}
		return path.substring(index + 1);
	}

	public static <E> void assertEqualsSymmetrical(E a, E b) {
		Assert.assertEquals(a, b);
		Assert.assertEquals(b, a);
	}

	public static <E> void assertNotEquals(E a, E b) {
		if (a == null) {
			Assert.assertFalse("[" + a + "] not equals [" + b + "]", b.equals(a));
		} else if (b == null) {
			Assert.assertFalse("[" + a + "] not equals [" + b + "]", a.equals(b));
		} else if (a.equals(b) || b.equals(a)) {
			Assert.fail("[" + a + "] not equals [" + b + "]");
		}
	}
}
