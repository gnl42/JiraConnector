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
import java.time.Duration;

import me.glindholm.jira.rest.client.api.AuthenticationHandler;

/**
 * Factory for asynchronous http clients using native Java 21 HttpClient.
 *
 * @since v2.0
 */
public class AsynchronousHttpClientFactory {
    public DisposableHttpClient createUrlValidationClient(final URI serverUri) {
        final HttpClient httpClient = createHttpClient(Duration.ofMillis(500));
        return createDisposableClient(httpClient, null);
    }


    public DisposableHttpClient createClient(final URI serverUri, final AuthenticationHandler authenticationHandler) {
        final HttpClient httpClient = createHttpClient(Duration.ofSeconds(30));
        return createDisposableClient(httpClient, authenticationHandler);
    }

    private HttpClient createHttpClient(Duration timeout) {
         return HttpClient.newBuilder()
                .connectTimeout(timeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
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