package com.atlassian.jira.rest.client;

import com.sun.istack.Nullable;

import java.util.Collections;
import java.util.Iterator;

/**
 * Represent iterable which is optional (for example due to lack of field in old REST API version).<br/>
 * This iterable is intended to be not null, so you can always get items by using iterator. If you need
 * to check if value was actually provided then use {@link com.atlassian.jira.rest.client.OptionalIterable#isPresent()}.
 *
 * @since v1.0
 */
public class OptionalIterable<T> implements Iterable<T> {

	@SuppressWarnings("unchecked")
	private static final OptionalIterable absentInstance = new OptionalIterable(null);

	@Nullable
	private final Iterable<T> iterable;

	@SuppressWarnings("unchecked")
	public static <T> OptionalIterable<T> absent() {
		return absentInstance;
	}

	public OptionalIterable(@Nullable Iterable<T> iterable) {
		this.iterable = iterable;
	}

	/**
	 * @return iterator for original iterable if {@link OptionalIterable#isPresent()} is true,
	 *         or empty iterator in other case.
	 */
	@Override
	public Iterator<T> iterator() {
		return isPresent()
				? iterable.iterator()
				: Collections.<T>emptyList().iterator();
	}

	public boolean isPresent() {
		return iterable != null;
	}
}
