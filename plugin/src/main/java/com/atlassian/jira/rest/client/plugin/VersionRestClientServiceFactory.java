package com.atlassian.jira.rest.client.plugin;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.VersionRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousVersionRestClient;
import com.atlassian.plugin.remotable.api.annotation.PublicComponent;

import java.net.URI;

@PublicComponent(VersionRestClient.class)
public final class VersionRestClientServiceFactory extends AbstractRestClientServiceFactory<VersionRestClient>
{
    @Override
    protected VersionRestClient getService(URI baseUri, HttpClient httpClient)
    {
        return new AsynchronousVersionRestClient(baseUri, httpClient);
    }
}
