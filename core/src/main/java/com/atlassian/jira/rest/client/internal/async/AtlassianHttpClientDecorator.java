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
	private AuthenticationHandler authenticationHandler;

	public AtlassianHttpClientDecorator(HttpClient httpClient, AuthenticationHandler authenticationHandler) {
		this.httpClient = httpClient;
		this.authenticationHandler = authenticationHandler;
	}

	@Override
	public void flushCacheByUriPattern(Pattern urlPattern) {
		httpClient.flushCacheByUriPattern(urlPattern);
	}

	@Override
	public Request.Builder newRequest() {
		return authenticationHandler.configure(httpClient.newRequest());
	}

	@Override
	public Request.Builder newRequest(URI uri) {
		return authenticationHandler.configure(httpClient.newRequest(uri));
	}

	@Override
	public Request.Builder newRequest(URI uri, String contentType, String entity) {
		return authenticationHandler.configure(httpClient.newRequest(uri, contentType, entity));
	}

	@Override
	public Request.Builder newRequest(String uri) {
		return authenticationHandler.configure(httpClient.newRequest(uri));
	}

	@Override
	public Request.Builder newRequest(String uri, String contentType, String entity)
	{
		return authenticationHandler.configure(httpClient.newRequest(uri, contentType, entity));
	}

	@Override
	public <A> ResponseTransformation.Builder<A> transformation()
	{
		return httpClient.transformation();
	}

	@Override
	public ResponsePromise execute(final Request request)
	{
		return httpClient.execute(request);
	}
}
