package com.atlassian.jira.rest.client.internal.async;

import com.atlassian.httpclient.apache.httpcomponents.DefaultRequest;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.httpclient.api.ResponseTransformation;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;

import java.net.URI;
import java.util.regex.Pattern;

/**
 * Abstract wrapper for an Atlassian HttpClient.
 */
public abstract class AtlassianHttpClientDecorator implements DisposableHttpClient {

	private final HttpClient httpClient;
	private final AuthenticationHandler authenticationHandler;

	public AtlassianHttpClientDecorator(HttpClient httpClient, AuthenticationHandler authenticationHandler) {
		this.httpClient = httpClient;
		this.authenticationHandler = authenticationHandler;
	}

	public void flushCacheByUriPattern(Pattern urlPattern) {
		httpClient.flushCacheByUriPattern(urlPattern);
	}

	public Request.Builder newRequest() {
		return new AuthenticatedRequestBuilder();
	}

	public Request.Builder newRequest(URI uri) {
		final Request.Builder builder = new AuthenticatedRequestBuilder();
		builder.setUri(uri);
		return builder;
	}

	public Request.Builder newRequest(URI uri, String contentType, String entity) {
		final Request.Builder builder = new AuthenticatedRequestBuilder();
		builder.setUri(uri);
		builder.setContentType(contentType);
		builder.setEntity(entity);
		return builder;
	}

	public Request.Builder newRequest(String uri) {
		final Request.Builder builder = new AuthenticatedRequestBuilder();
		builder.setUri(URI.create(uri));
		return builder;
	}

	public Request.Builder newRequest(String uri, String contentType, String entity) {
		final Request.Builder builder = new AuthenticatedRequestBuilder();
		builder.setUri(URI.create(uri));
		builder.setContentType(contentType);
		builder.setEntity(entity);
		return builder;
	}

	@Override
	public <A> ResponseTransformation.Builder<A> transformation() {
		return httpClient.transformation();
	}

	@Override
	public ResponsePromise execute(Request request) {
		return httpClient.execute(request);
	}

	private class AuthenticatedRequestBuilder extends DefaultRequest.DefaultRequestBuilder {
		public AuthenticatedRequestBuilder() {
			super(httpClient);
		}

		@Override
		public ResponsePromise execute(Request.Method method) {
			if(authenticationHandler != null) {
				this.setMethod(method);
				authenticationHandler.configure(this);
			}
			return super.execute(method);
		}
	}
}
