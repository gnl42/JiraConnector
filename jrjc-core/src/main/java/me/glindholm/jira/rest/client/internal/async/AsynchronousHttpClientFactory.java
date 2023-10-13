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

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.apache.httpcomponents.DefaultHttpClientFactory;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.executor.ThreadLocalContextManager;

import me.glindholm.jira.rest.client.api.AuthenticationHandler;

/**
 * Factory for asynchronous http clients.
 *
 * @since v2.0
 */
public class AsynchronousHttpClientFactory {
    private static final Logger log = LoggerFactory.getLogger(AsynchronousHttpClientFactory.class);

    private static String libraryVersion = "unknown";
    private static Instant libraryDate = Instant.now();

    static {
        final URL url = Thread.currentThread().getContextClassLoader().getResource("META-INF/MANIFEST.MF");
        try {
            try (InputStream is = url.openStream()) {
                final Manifest mf = new Manifest(is);
                final Attributes attrs = mf.getMainAttributes();
                final String versionStr = attrs.getValue("Bundle-Version");
                final String[] parts = versionStr.split("\\.");
                libraryVersion = parts[0] + "." + parts[1] + "." + parts[2];
                final DateTimeFormatter parse = DateTimeFormatter.ofPattern("yyyyMMddHHmmSS");
                if (parts[3].equals("qualifier")) {
                    libraryDate = Instant.now();
                } else {
                    libraryDate = LocalDateTime.parse(parts[4], parse).atOffset(ZoneOffset.UTC).toInstant();
                }
            }
        } catch (final Exception e) {
            log.debug("", e);
        }
    }

    public DisposableHttpClient createClient(final URI serverUri, final AuthenticationHandler authenticationHandler) {
        return createClient(serverUri, authenticationHandler, new HttpClientOptions());
    }

    public DisposableHttpClient createClient(final URI serverUri, final AuthenticationHandler authenticationHandler, final HttpClientOptions options) {
        final DefaultHttpClientFactory defaultHttpClientFactory = new DefaultHttpClientFactory(new NoOpEventPublisher(),
                new RestClientApplicationProperties(serverUri), new ThreadLocalContextManager<Object>() {
                    @Override
                    public Object getThreadLocalContext() {
                        return null;
                    }

                    @Override
                    public void setThreadLocalContext(final Object context) {
                    }

                    @Override
                    public void clearThreadLocalContext() {
                    }
                });

        final HttpClient httpClient = defaultHttpClientFactory.create(options);

        return new AtlassianHttpClientDecorator(httpClient, authenticationHandler) {
            @Override
            public void destroy() throws Exception {
                defaultHttpClientFactory.dispose(httpClient);
            }
        };
    }

    public DisposableHttpClient createClient(final HttpClient client) {
        return new AtlassianHttpClientDecorator(client, null) {

            @Override
            public void destroy() throws Exception {
                // This should never be implemented. This is simply creation of a wrapper
                // for AtlassianHttpClient which is extended by a destroy method.
                // Destroy method should never be called for AtlassianHttpClient coming from
                // a client! Imagine you create a RestClient, pass your own HttpClient there
                // and it gets destroy.
            }
        };
    }

    private static class NoOpEventPublisher implements EventPublisher {
        @Override
        public void publish(final Object o) {
        }

        @Override
        public void register(final Object o) {
        }

        @Override
        public void unregister(final Object o) {
        }

        @Override
        public void unregisterAll() {
        }
    }

    /**
     * These properties are used to present JRJC as a User-Agent during http
     * requests.
     */
    private static class RestClientApplicationProperties implements ApplicationProperties {

        private final String baseUrl;

        private RestClientApplicationProperties(final URI jiraURI) {
            baseUrl = jiraURI.getPath();
        }

        @Override
        public String getBaseUrl() {
            return baseUrl;
        }

        /**
         * We'll always have an absolute URL as a client.
         */
        @NonNull
        @Override
        public String getBaseUrl(final UrlMode urlMode) {
            return baseUrl;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "JIRA Rest Java Client";
        }

        @NonNull
        @Override
        public String getPlatformId() {
            return ApplicationProperties.PLATFORM_JIRA;
        }

        @NonNull
        @Override
        public String getVersion() {
            return libraryVersion;
        }

        @NonNull
        @Override
        public Date getBuildDate() {
            return Date.from(libraryDate);
        }

        @NonNull
        @Override
        public String getBuildNumber() {
            // TODO implement using MavenUtils, JRJC-123
            return String.valueOf(0);
        }

        @Override
        public File getHomeDirectory() {
            return new File(".");
        }

        @Override
        public String getPropertyValue(final String s) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @NonNull
        @Override
        public String getApplicationFileEncoding() {
            return StandardCharsets.UTF_8.name();
        }

        @NonNull
        @Override
        public Optional<Path> getLocalHomeDirectory() {
            return Optional.of(getHomeDirectory().toPath());
        }

        @NonNull
        @Override
        public Optional<Path> getSharedHomeDirectory() {
            return getLocalHomeDirectory();
        }
    }
}
