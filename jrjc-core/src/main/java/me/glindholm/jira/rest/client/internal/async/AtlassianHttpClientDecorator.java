package me.glindholm.jira.rest.client.internal.async;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import me.glindholm.jira.rest.client.api.AuthenticationHandler;

/**
 * Native Java 21 HTTP client wrapper that applies authentication to each request.
 */
public abstract class AtlassianHttpClientDecorator implements DisposableHttpClient {

    private final HttpClient httpClient;
    private final AuthenticationHandler authenticationHandler;

    public AtlassianHttpClientDecorator(final HttpClient httpClient, final AuthenticationHandler authenticationHandler) {
        this.httpClient = httpClient;
        this.authenticationHandler = authenticationHandler;
    }

    @Override
    public HttpRequest.Builder newRequest(final URI uri) {
        final HttpRequest.Builder builder = HttpRequest.newBuilder(uri);
        if (authenticationHandler != null) {
            authenticationHandler.configure(builder);
        }
        return builder;
    }

    @Override
    public CompletableFuture<HttpResponse<String>> execute(final HttpRequest request) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    @Override
    public CompletableFuture<HttpResponse<byte[]>> executeForBytes(final HttpRequest request) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray());
    }
}