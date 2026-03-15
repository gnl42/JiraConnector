package me.glindholm.jira.rest.client.internal.async;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Native Java 21 HTTP client with destroy exposed.
 */
public interface DisposableHttpClient {

    /**
     * Creates a new request builder for the given URI, with authentication pre-applied.
     */
    HttpRequest.Builder newRequest(URI uri);

    /**
     * Executes a request asynchronously and returns the response.
     */
    CompletableFuture<HttpResponse<String>> execute(HttpRequest request);

    /**
     * Executes a request for binary content (attachments).
     */
    CompletableFuture<HttpResponse<byte[]>> executeForBytes(HttpRequest request);

    void destroy() throws Exception;
}
