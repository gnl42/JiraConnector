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

package me.glindholm.jira.rest.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Assert;

import me.glindholm.jira.rest.client.api.RestClientException;
import me.glindholm.jira.rest.client.api.domain.OperationGroup;
import me.glindholm.jira.rest.client.api.domain.OperationLink;
import me.glindholm.jira.rest.client.api.domain.Transition;
import me.glindholm.jira.rest.client.api.domain.util.ErrorCollection;

public class TestUtil {
    // private static DateTimeFormatter universalOffsetDateTimeParser =
    // ISOOffsetDateTimeFormat.dateTimeParser();
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    public static List<OperationGroup> EMPTY_GROUPS = Collections.emptyList();
    public static List<OperationLink> EMPTY_LINKS = Collections.emptyList();

    public static URI toUri(final String str) {
        try {
            return new URIBuilder(str).build();
        } catch (final URISyntaxException e) {
            return null;
        }
    }

    // public static OffsetDateTime toOffsetDateTime(String isoOffsetDateTimeSt) {
    // return
    // universalOffsetDateTimeParser.parseOffsetDateTime(isoOffsetDateTimeSt);
    // }

    public static OffsetDateTime toOffsetDateTime(final String isoOffsetDateTimeSt) {
        return OffsetDateTime.parse(isoOffsetDateTimeSt, formatter);
        // formatter.withZone(zone).parseOffsetDateTime(isoOffsetDateTimeSt);
    }

    public static OffsetDateTime toOffsetDateTimeFromIsoDate(final String isoDate) {
        final LocalDate date = LocalDate.parse(isoDate, dateFormatter);
        return OffsetDateTime.of(date, LocalTime.MIDNIGHT, ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS);
    }

    public static void assertErrorCode(final int errorCode, final Runnable runnable) {
        assertErrorCode(errorCode, StringUtils.EMPTY, runnable);
    }

    @SuppressWarnings("unused")
    public static <T extends Throwable> void assertThrows(final Class<T> clazz, final String regexp, final Runnable runnable) {
        try {
            runnable.run();
            Assert.fail(clazz.getName() + " exception expected");
        } catch (final Throwable e) {
            Assert.assertTrue("Expected exception of class " + clazz.getName() + " but was caught " + e.getClass().getName(), clazz.isInstance(e));
            if (e.getMessage() == null && regexp != null) {
                Assert.fail("Exception with no message caught, while expected regexp [" + regexp + "]");
            }
            if (regexp != null && e.getMessage() != null) {
                Assert.assertTrue("Message [" + e.getMessage() + "] does not match regexp [" + regexp + "]", e.getMessage().matches(regexp));
            }
        }

    }

    public static void assertExpectedErrorCollection(final List<ErrorCollection> errors, final Runnable runnable) {
        assertExpectedErrors(errors, runnable);
    }

    // public static void assertErrorCode(Response.Status status, String message,
    // Runnable runnable) {
    // assertErrorCode(status.getStatusCode(), message, runnable);
    // }
    //
    // public static void assertErrorCodeWithRegexp(Response.Status status, String
    // regexp, Runnable runnable) {
    // assertErrorCodeWithRegexp(status.getStatusCode(), regexp, runnable);
    // }
    //
    // public static void assertErrorCode(Response.Status status, Runnable runnable)
    // {
    // assertErrorCode(status.getStatusCode(), null, runnable);
    // }

    public static void assertErrorCode(final int errorCode, final String message, final Runnable runnable) {
        try {
            runnable.run();
            Assert.fail(RestClientException.class + " exception expected");
        } catch (final me.glindholm.jira.rest.client.api.RestClientException e) {
            Assert.assertTrue(e.getStatusCode().isPresent());
            Assert.assertEquals(errorCode, e.getStatusCode().get().intValue());
            if (!StringUtils.isEmpty(message)) {
                // We expect a single error message. Either error or error message.
                Assert.assertEquals(1, e.getErrorCollections().size());
                if (e.getErrorCollections().get(0).getErrorMessages().size() > 0) {
                    Assert.assertEquals(e.getErrorCollections().get(0).getErrors().values(), message);
                } else if (e.getErrorCollections().get(0).getErrors().size() > 0) {
                    Assert.assertEquals(e.getErrorCollections().get(0).getErrors().values(), message);
                } else {
                    Assert.fail("Expected an error message.");
                }
            }
        }
    }

    public static void assertErrorCodeWithRegexp(final int errorCode, final String regExp, final Runnable runnable) {
        try {
            runnable.run();
            Assert.fail(RestClientException.class + " exception expected");
        } catch (final me.glindholm.jira.rest.client.api.RestClientException ex) {
            final ErrorCollection errorElement = ex.getErrorCollections().get(0);
            final String errorMessage = errorElement.getErrorMessages().get(0);
            Assert.assertTrue("'" + ex.getMessage() + "' does not match regexp '" + regExp + "'", errorMessage.matches(regExp));
            Assert.assertTrue(ex.getStatusCode().isPresent());
            Assert.assertEquals(errorCode, ex.getStatusCode().get().intValue());
        }
    }

    public static String getLastPathSegment(final URI uri) {
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

    public static <E> void assertEqualsSymmetrical(final E a, final E b) {
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);
    }

    public static <E> void assertNotEquals(final E a, final E b) {
        if (a == null) {
            Assert.assertFalse("[" + a + "] not equals [" + b + "]", b.equals(a));
        } else if (b == null) {
            Assert.assertFalse("[" + a + "] not equals [" + b + "]", a.equals(b));
        } else if (a.equals(b) || b.equals(a)) {
            Assert.fail("[" + a + "] not equals [" + b + "]");
        }
    }

    @Nullable
    public static Transition getTransitionByName(final List<Transition> transitions, final String transitionName) {
        Transition transitionFound = null;
        for (final Transition transition : transitions) {
            if (transition.getName().equals(transitionName)) {
                transitionFound = transition;
                break;
            }
        }
        return transitionFound;
    }

    private static void assertExpectedErrors(final List<ErrorCollection> expectedErrors, final Runnable runnable) {
        try {
            runnable.run();
            Assert.fail(RestClientException.class + " exception expected");
        } catch (final me.glindholm.jira.rest.client.api.RestClientException e) {
            Assert.assertEquals(e.getErrorCollections(), expectedErrors);
        }
    }

    public static <K> void assertEmptyList(final List<K> iterable) {
        Assert.assertEquals(iterable, Collections.emptyList());
    }

    public static <K> void assertEmptySet(final Set<K> iterable) {
        Assert.assertEquals(iterable, Collections.emptySet());
    }
}
