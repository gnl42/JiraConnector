package com.atlassian.jira.rest.client.plugin;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.ComponentRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousComponentRestClient;
import com.atlassian.plugin.remotable.api.annotation.PublicComponent;

import java.net.URI;

@PublicComponent(ComponentRestClient.class)
public final class ComponentRestClientServiceFactory extends AbstractRestClientServiceFactory<ComponentRestClient>
{
    @Override
    protected ComponentRestClient getService(URI baseUri, HttpClient httpClient)
    {
        return new AsynchronousComponentRestClient(baseUri, httpClient);
    }
}
