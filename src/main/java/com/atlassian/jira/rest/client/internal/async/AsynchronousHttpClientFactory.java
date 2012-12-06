package com.atlassian.jira.rest.client.internal.async;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.apache.httpcomponents.DefaultHttpClient;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.httpclient.spi.ThreadLocalContextManagers;
import com.atlassian.jira.rest.client.AuthenticationHandler;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.util.concurrent.Effect;

import java.io.File;
import java.net.URI;
import java.util.Date;

/**
 * Factory for asynchronous http clients.
 *
 * @since v2.0
 */
public class AsynchronousHttpClientFactory {

    private static class NoOpEventPublisher implements EventPublisher {
        @Override
        public void publish(Object o) {
        }
        @Override
        public void register(Object o) {
        }
        @Override
        public void unregister(Object o) {
        }
        @Override
        public void unregisterAll() {
        }
    }

    /**
     * These properties are used to present JRJC as a User-Agent during http requests.
     */
    @SuppressWarnings("deprecation")
    private static class RestClientApplicationProperties implements ApplicationProperties {

        private RestClientApplicationProperties(URI jiraURI) {
            this.baseUrl = jiraURI.getPath();
        }

        private final String baseUrl;

        @Override
        public String getBaseUrl() {
            return baseUrl;
        }

        @Override
        public String getDisplayName() {
            return "Atlassian JIRA Rest Java Client";
        }

        @Override
        public String getVersion() {
            // TODO implement using MavenUtils
            return "2.0.0";
        }

        @Override
        public Date getBuildDate() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getBuildNumber() {
            // TODO implement using MavenUtils
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
    }

    @SuppressWarnings("unchecked")
    public HttpClient client(final URI serverUri, final AuthenticationHandler authenticationHandler) {
        HttpClientOptions options = new HttpClientOptions();
        options.setRequestPreparer(new Effect<Request>() {
            @Override
            public void apply(final Request request) {
                authenticationHandler.configure(request);
            }
        });
        return new DefaultHttpClient(new NoOpEventPublisher(),
                new RestClientApplicationProperties(serverUri),
                ThreadLocalContextManagers.noop(), options);
    }
}
