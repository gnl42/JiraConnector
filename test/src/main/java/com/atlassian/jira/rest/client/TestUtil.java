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

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.OperationGroup;
import com.atlassian.jira.rest.client.api.domain.OperationLink;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.util.ErrorCollection;
import com.google.common.collect.Iterators;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import static com.google.common.collect.Iterators.getOnlyElement;

public class TestUtil {
	private static DateTimeFormatter universalDateTimeParser = ISODateTimeFormat.dateTimeParser();
	private static DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
	private static DateTimeFormatter dateFormatter = ISODateTimeFormat.date();
	public static Iterable<OperationGroup> EMPTY_GROUPS = Collections.emptyList();
	public static Iterable<OperationLink> EMPTY_LINKS = Collections.emptyList();

	public static URI toUri(String str) {
		return UriBuilder.fromUri(str).build();
	}

	public static DateTime toDateTime(String isoDateTimeSt) {
		return universalDateTimeParser.parseDateTime(isoDateTimeSt);
	}

	@SuppressWarnings("unused")
	public static DateTime toDateTime(String isoDateTimeSt, DateTimeZone zone) {
		return formatter.withZone(zone).parseDateTime(isoDateTimeSt);
	}

	public static DateTime toDateTimeFromIsoDate(String isoDate) {
		return dateFormatter.parseDateTime(isoDate);
	}

	public static void assertErrorCode(int errorCode, Runnable runnable) {
		assertErrorCode(errorCode, StringUtils.EMPTY, runnable);
	}

	@SuppressWarnings("unused")
	public static <T extends Throwable> void assertThrows(Class<T> clazz, String regexp, Runnable runnable) {
		try {
			runnable.run();
			Assert.fail(clazz.getName() + " exception expected");
		} catch (Throwable e) {
			Assert.assertTrue("Expected exception of class " + clazz.getName() + " but was caught " + e.getClass().getName(),
					clazz.isInstance(e));
			if (e.getMessage() == null && regexp != null) {
				Assert.fail("Exception with no message caught, while expected regexp [" + regexp + "]");
			}
			if (regexp != null && e.getMessage() != null) {
				Assert.assertTrue("Message [" + e.getMessage() + "] does not match regexp [" + regexp + "]", e.getMessage()
						.matches(regexp));
			}
		}

	}

	public static void assertErrorCode(Response.Status status, String message, Runnable runnable) {
		assertErrorCode(status.getStatusCode(), message, runnable);
	}

	public static void assertExpectedErrorCollection(final Collection<ErrorCollection> errors, final Runnable runnable) {
		assertExpectedErrors(errors, runnable);
	}

	public static void assertErrorCodeWithRegexp(Response.Status status, String regexp, Runnable runnable) {
		assertErrorCodeWithRegexp(status.getStatusCode(), regexp, runnable);
	}

	public static void assertErrorCode(Response.Status status, Runnable runnable) {
		assertErrorCode(status.getStatusCode(), null, runnable);
	}

	public static void assertErrorCode(int errorCode, String message, Runnable runnable) {
		try {
			runnable.run();
			Assert.fail(RestClientException.class + " exception expected");
		} catch (com.atlassian.jira.rest.client.api.RestClientException e) {
			Assert.assertTrue(e.getStatusCode().isPresent());
			Assert.assertEquals(errorCode, e.getStatusCode().get().intValue());
			if (!StringUtils.isEmpty(message)) {
				// We expect a single error message. Either error or error message.
				Assert.assertEquals(1, e.getErrorCollections().size());
				if (Iterators.getOnlyElement(e.getErrorCollections().iterator()).getErrorMessages().size() > 0) {
					Assert.assertEquals(getOnlyElement(getOnlyElement(e.getErrorCollections().iterator()).getErrorMessages()
							.iterator()), message);
				} else if (Iterators.getOnlyElement(e.getErrorCollections().iterator()).getErrors().size() > 0) {
					Assert.assertEquals(getOnlyElement(getOnlyElement(e.getErrorCollections().iterator()).getErrors().values()
							.iterator()), message);
				} else {
					Assert.fail("Expected an error message.");
				}
			}
		}
	}

	public static void assertErrorCodeWithRegexp(int errorCode, String regExp, Runnable runnable) {
		try {
			runnable.run();
			Assert.fail(RestClientException.class + " exception expected");
		} catch (com.atlassian.jira.rest.client.api.RestClientException ex) {
			final ErrorCollection errorElement = getOnlyElement(ex.getErrorCollections().iterator());
			final String errorMessage = getOnlyElement(errorElement.getErrorMessages().iterator());
			Assert.assertTrue("'" + ex.getMessage() + "' does not match regexp '" + regExp + "'", errorMessage.matches(regExp));
			Assert.assertTrue(ex.getStatusCode().isPresent());
			Assert.assertEquals(errorCode, ex.getStatusCode().get().intValue());
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

	@Nullable
	public static Transition getTransitionByName(Iterable<Transition> transitions, String transitionName) {
		Transition transitionFound = null;
		for (Transition transition : transitions) {
			if (transition.getName().equals(transitionName)) {
				transitionFound = transition;
				break;
			}
		}
		return transitionFound;
	}

	private static void assertExpectedErrors(final Collection<ErrorCollection> expectedErrors, final Runnable runnable) {
		try {
			runnable.run();
			Assert.fail(RestClientException.class + " exception expected");
		} catch (com.atlassian.jira.rest.client.api.RestClientException e) {
			Assert.assertEquals(e.getErrorCollections(), expectedErrors);
		}
	}

	public static <K> void assertEmptyIterable(Iterable<K> iterable) {
		org.junit.Assert.assertThat(iterable, Matchers.<K>emptyIterable());
	}
}
