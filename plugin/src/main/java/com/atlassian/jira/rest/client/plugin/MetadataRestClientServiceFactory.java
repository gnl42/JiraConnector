package com.atlassian.jira.rest.client.plugin;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousMetadataRestClient;
import com.atlassian.plugin.remotable.api.annotation.PublicComponent;

import java.net.URI;

@PublicComponent(MetadataRestClient.class)
public final class MetadataRestClientServiceFactory extends AbstractRestClientServiceFactory<MetadataRestClient>
{
    @Override
    protected MetadataRestClient getService(URI baseUri, HttpClient httpClient)
    {
        return new AsynchronousMetadataRestClient(baseUri, httpClient);
    }
}
