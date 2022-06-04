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

import static com.google.common.collect.Iterators.getOnlyElement;

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

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.hamcrest.Matchers;
import org.junit.Assert;

import com.google.common.collect.Iterators;

import me.glindholm.jira.rest.client.api.RestClientException;
import me.glindholm.jira.rest.client.api.domain.OperationGroup;
import me.glindholm.jira.rest.client.api.domain.OperationLink;
import me.glindholm.jira.rest.client.api.domain.Transition;
import me.glindholm.jira.rest.client.api.domain.util.ErrorCollection;

public class TestUtil {
    //    private static DateTimeFormatter universalOffsetDateTimeParser = ISOOffsetDateTimeFormat.dateTimeParser();
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    public static Iterable<OperationGroup> EMPTY_GROUPS = Collections.emptyList();
    public static Iterable<OperationLink> EMPTY_LINKS = Collections.emptyList();

    public static URI toUri(String str) {
        try {
            return new URIBuilder(str).build();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    //    public static OffsetDateTime toOffsetDateTime(String isoOffsetDateTimeSt) {
    //        return universalOffsetDateTimeParser.parseOffsetDateTime(isoOffsetDateTimeSt);
    //    }

    public static OffsetDateTime toOffsetDateTime(String isoOffsetDateTimeSt) {
        return OffsetDateTime.parse(isoOffsetDateTimeSt, formatter);
        //         formatter.withZone(zone).parseOffsetDateTime(isoOffsetDateTimeSt);
    }

    public static OffsetDateTime toOffsetDateTimeFromIsoDate(String isoDate) {
        final LocalDate date = LocalDate.parse(isoDate, dateFormatter);
        return OffsetDateTime.of(date, LocalTime.MIDNIGHT, ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS);
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

    public static void assertExpectedErrorCollection(final List<ErrorCollection> errors, final Runnable runnable) {
        assertExpectedErrors(errors, runnable);
    }

    //    public static void assertErrorCode(Response.Status status, String message, Runnable runnable) {
    //        assertErrorCode(status.getStatusCode(), message, runnable);
    //    }
    //
    //   public static void assertErrorCodeWithRegexp(Response.Status status, String regexp, Runnable runnable) {
    //        assertErrorCodeWithRegexp(status.getStatusCode(), regexp, runnable);
    //    }
    //
    //    public static void assertErrorCode(Response.Status status, Runnable runnable) {
    //        assertErrorCode(status.getStatusCode(), null, runnable);
    //    }

    public static void assertErrorCode(int errorCode, String message, Runnable runnable) {
        try {
            runnable.run();
            Assert.fail(RestClientException.class + " exception expected");
        } catch (me.glindholm.jira.rest.client.api.RestClientException e) {
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
        } catch (me.glindholm.jira.rest.client.api.RestClientException ex) {
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

    private static void assertExpectedErrors(final List<ErrorCollection> expectedErrors, final Runnable runnable) {
        try {
            runnable.run();
            Assert.fail(RestClientException.class + " exception expected");
        } catch (me.glindholm.jira.rest.client.api.RestClientException e) {
            Assert.assertEquals(e.getErrorCollections(), expectedErrors);
        }
    }

    public static <K> void assertEmptyIterable(Iterable<K> iterable) {
        Assert.assertThat(iterable, Matchers.<K>emptyIterable());
    }
}
