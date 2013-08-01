package com.atlassian.jira.rest.client.test.matchers;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.util.ErrorCollection;
import com.google.common.collect.ImmutableList;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

/**
 * Matchers for RestClientException
 *
 * @since v2.0
 */
public class RestClientExceptionMatchers {

	public static Matcher<RestClientException> rceWithSingleError(final Integer statusCode, final String expectedErrorMessage) {
		return new BaseMatcher<RestClientException>() {

			@Override
			public boolean matches(final Object item) {
				if (item instanceof RestClientException) {
					final RestClientException ex = (RestClientException) item;
					final Matcher<Iterable<? extends String>> errorMessageMatcher = Matchers
							.contains(expectedErrorMessage);
					return ex.getStatusCode().get().equals(statusCode)
							&& ex.getErrorCollections().size() == 1
							&& errorMessageMatcher.matches(ex.getErrorCollections().iterator().next().getErrorMessages());

				}
				return false;
			}

			@Override
			public void describeTo(final Description description) {
				final ErrorCollection expectedErrorCollection = ErrorCollection.builder()
						.errorMessage(expectedErrorMessage).status(statusCode) .build();

				final RestClientException expectedException = new RestClientException(
						ImmutableList.of(expectedErrorCollection), statusCode);

				description.appendText("<"+expectedException.toString()+">");
			}
		};
	}
}