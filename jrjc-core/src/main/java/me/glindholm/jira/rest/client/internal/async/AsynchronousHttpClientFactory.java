/*
 * Copyright (C) 2012 Atlassian
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
package me.glindholm.jira.rest.client.internal.async;

import java.net.URI;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import me.glindholm.jira.rest.client.api.AuthenticationHandler;

/**
 * Factory for asynchronous http clients using native Java 21 HttpClient.
 *
 * @since v2.0
 */
public class AsynchronousHttpClientFactory {

    /**
     * Creates a default SSLContext initialised with the JVM's default trust store (cacerts),
     * so that self-signed certificates imported into cacerts are accepted.
     */
    private static SSLContext buildDefaultSslContext() {
        try {
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null); // null → use the JVM default trust store (cacerts)
            final SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, tmf.getTrustManagers(), null);
            return ctx;
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            // Fall back to platform default — better than completely failing
            try {
                return SSLContext.getDefault();
            } catch (final NoSuchAlgorithmException ex) {
                throw new IllegalStateException("Cannot initialise SSLContext", ex);
            }
        }
    }

    public DisposableHttpClient createClient(final URI serverUri, final AuthenticationHandler authenticationHandler) {
        final HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .sslContext(buildDefaultSslContext())
                .build();
        return createDisposableClient(httpClient, authenticationHandler);
    }

    public DisposableHttpClient createClient(final URI serverUri, final AuthenticationHandler authenticationHandler, final TrustManager[] trustManagers) {
        if (trustManagers == null) {
            return createClient(serverUri, authenticationHandler);
        }
        try {
            final SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, trustManagers, null);
            final HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .sslContext(ctx)
                    .build();
            return createDisposableClient(httpClient, authenticationHandler);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            // Fall back to default SSL context
            return createClient(serverUri, authenticationHandler);
        }
    }

    public DisposableHttpClient createClient(final URI serverUri, final AuthenticationHandler authenticationHandler, final HttpClient httpClient) {
        return createDisposableClient(httpClient, authenticationHandler);
    }

    public DisposableHttpClient createClient(final HttpClient client) {
        return createDisposableClient(client, null);
    }

    private DisposableHttpClient createDisposableClient(final HttpClient httpClient, final AuthenticationHandler authenticationHandler) {
        return new AtlassianHttpClientDecorator(httpClient, authenticationHandler) {
            @Override
            public void destroy() throws Exception {
                // Java 21 HttpClient manages its own lifecycle; nothing to dispose explicitly.
            }
        };
    }
}