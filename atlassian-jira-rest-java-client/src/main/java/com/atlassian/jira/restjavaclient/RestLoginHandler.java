package com.atlassian.jira.restjavaclient;

import org.apache.http.client.HttpClient;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public interface RestLoginHandler {
    void handleLogin(HttpClient httpClient);
}
