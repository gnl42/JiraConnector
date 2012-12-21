package com.atlassian.jira.rest.client.plugin;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.SessionRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousSessionRestClient;
import com.atlassian.plugin.remotable.api.annotation.PublicComponent;

import java.net.URI;

@PublicComponent(SessionRestClient.class)
public final class SessionRestClientServiceFactory extends AbstractRestClientServiceFactory<SessionRestClient>
{
    @Override
    protected SessionRestClient getService(URI baseUri, HttpClient httpClient)
    {
        return new AsynchronousSessionRestClient(baseUri, httpClient);
    }
}
