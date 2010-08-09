package com.atlassian.jira.restjavaclient;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
* TODO: Document this class / interface here
*
* @since v0.1
*/
public class IsIterableOf<T> extends TypeSafeMatcher<Iterable<T>> {
    private final Iterable<T> expected;

    public IsIterableOf(Iterable<T> expected) {
        this.expected = expected;
    }


    public static <T> IsIterableOf<T> hasOnlyElements(T... elements) {
        return new IsIterableOf<T>(Arrays.asList(elements));
    }

    public boolean matchesSafely(Iterable<T> given) {
        final Set<T> s = asSet(expected);


        for (T t : given) {
            if (!s.remove(t)) {
                return false;
            }
        }
        return s.isEmpty();
    }

    private Set<T> asSet(Iterable<T> iterable) {
        final Set<T> s = new HashSet<T>();

        for (T t : iterable) {
            s.add(t);
        }
        return s;
    }

    public static <T> Matcher<Iterable<T>> ofItems(T... items) {
        return new IsIterableOf<T>(Arrays.asList(items));
    }

    public void describeTo(Description description) {
        description.appendText("an iterable containing just elements " + asSet(expected));
    }
}
