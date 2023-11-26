package me.glindholm.jira.rest.client.internal.async;

import java.net.URI;
import java.util.regex.Pattern;

import com.atlassian.httpclient.apache.httpcomponents.DefaultRequest;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.httpclient.api.ResponseTransformation;

import me.glindholm.jira.rest.client.api.AuthenticationHandler;

/**
 * Abstract wrapper for an Atlassian HttpClient.
 */
public abstract class AtlassianHttpClientDecorator implements DisposableHttpClient {

    private final HttpClient httpClient;
    private final AuthenticationHandler authenticationHandler;

    public AtlassianHttpClientDecorator(final HttpClient httpClient, final AuthenticationHandler authenticationHandler) {
        this.httpClient = httpClient;
        this.authenticationHandler = authenticationHandler;
    }

    @Override
    public void flushCacheByUriPattern(final Pattern urlPattern) {
        httpClient.flushCacheByUriPattern(urlPattern);
    }

    @Override
    public Request.Builder newRequest() {
        return new AuthenticatedRequestBuilder();
    }

    @Override
    public Request.Builder newRequest(final URI uri) {
        final Request.Builder builder = new AuthenticatedRequestBuilder();
        builder.setUri(uri);
        return builder;
    }

    @Override
    public Request.Builder newRequest(final URI uri, final String contentType, final String entity) {
        final Request.Builder builder = new AuthenticatedRequestBuilder();
        builder.setUri(uri);
        builder.setContentType(contentType);
        builder.setEntity(entity);
        return builder;
    }

    @Override
    public Request.Builder newRequest(final String uri) {
        final Request.Builder builder = new AuthenticatedRequestBuilder();
        builder.setUri(URI.create(uri));
        return builder;
    }

    @Override
    public Request.Builder newRequest(final String uri, final String contentType, final String entity) {
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
    public ResponsePromise execute(final Request request) {
        return httpClient.execute(request);
    }

    private class AuthenticatedRequestBuilder extends DefaultRequest.DefaultRequestBuilder {
        public AuthenticatedRequestBuilder() {
            super(httpClient);
        }

        @Override
        public ResponsePromise execute(final Request.Method method) {
            if (authenticationHandler != null) {
                setMethod(method);
                authenticationHandler.configure(this);
            }
            return super.execute(method);
        }
    }
}
