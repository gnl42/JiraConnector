package com.atlassian.jira.restjavaclient;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public interface HttpClientConfigurer {
	void configure(HttpClient httpClient);
	void configureMethod(HttpRequestBase method);
	void handleLogin(HttpClient httpClient);
}
