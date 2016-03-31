package com.atlassian.jira.rest.client.internal.async;

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
		final Request.Builder builder = httpClient.newRequest();
		configureAuthentication(builder);
		return builder;
	}

	public Request.Builder newRequest(URI uri) {
		final Request.Builder builder = httpClient.newRequest(uri);
		configureAuthentication(builder);
		return builder;
	}

	public Request.Builder newRequest(URI uri, String contentType, String entity) {
		final Request.Builder builder = httpClient.newRequest(uri, contentType, entity);
		configureAuthentication(builder);
		return builder;
	}

	public Request.Builder newRequest(String uri) {
		final Request.Builder builder = httpClient.newRequest(uri);
		configureAuthentication(builder);
		return builder;
	}

	public Request.Builder newRequest(String uri, String contentType, String entity) {
		final Request.Builder builder = httpClient.newRequest(uri, contentType, entity);
		configureAuthentication(builder);
		return builder;
	}

	private void configureAuthentication(Request.Builder builder) {
		if(authenticationHandler != null) {
			authenticationHandler.configure(builder);
		}
	}

	@Override
	public <A> ResponseTransformation.Builder<A> transformation() {
		return httpClient.transformation();
	}

	@Override
	public ResponsePromise execute(Request request) {
		return httpClient.execute(request);
	}
}
