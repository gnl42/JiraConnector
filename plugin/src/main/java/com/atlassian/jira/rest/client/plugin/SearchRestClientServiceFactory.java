package com.atlassian.jira.rest.client.plugin;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousSearchRestClient;
import com.atlassian.plugin.remotable.api.annotation.PublicComponent;

import java.net.URI;

@PublicComponent(SearchRestClient.class)
public final class SearchRestClientServiceFactory extends AbstractRestClientServiceFactory<SearchRestClient>
{
    @Override
    protected SearchRestClient getService(URI baseUri, HttpClient httpClient)
    {
        return new AsynchronousSearchRestClient(baseUri, httpClient);
    }
}
