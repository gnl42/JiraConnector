/*
 * Copyright (C) 2013 Atlassian
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

package com.atlassian.jira.rest.client.test.matchers;

import org.hamcrest.Description;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.net.URI;
import java.util.regex.Pattern;

public class UriRegularExpressionMatcher extends TypeSafeMatcher<URI> {
	private final URI baseUri;
	private final Pattern pattern;

	public UriRegularExpressionMatcher(final URI baseUri, final Pattern pattern) {
		this.baseUri = baseUri;
		this.pattern = pattern;
	}

	public static UriRegularExpressionMatcher uriMatchesRegexp(final URI baseUri, final Pattern pattern) {
		return new UriRegularExpressionMatcher(baseUri, pattern);
	}

	public static UriRegularExpressionMatcher uriMatchesRegexp(final URI baseUri, final String pattern) {
		return new UriRegularExpressionMatcher(baseUri, Pattern.compile(pattern));
	}

	@Override
	public boolean matchesSafely(URI given) {
		return checkBaseUri(given) && checkPattern(given);
	}

	private boolean checkPattern(URI given) {
		final URI relativeUrl = baseUri.relativize(given);
		return pattern.matcher(relativeUrl.toString()).matches();
	}

	private boolean checkBaseUri(URI given) {
		return given.toString().startsWith(baseUri.toString());
	}

	@Override
	public void describeTo(Description description) {
		description.appendText(String.format("url with base %s and matching regular expression %s", baseUri, pattern));
	}
}
