package com.atlassian.jira.rest.client.plugin;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousProjectRestClient;
import com.atlassian.plugin.remotable.api.annotation.PublicComponent;

import java.net.URI;

@PublicComponent(ProjectRestClient.class)
public final class ProjectRestClientServiceFactory extends AbstractRestClientServiceFactory<ProjectRestClient>
{
    @Override
    protected ProjectRestClient getService(URI baseUri, HttpClient httpClient)
    {
        return new AsynchronousProjectRestClient(baseUri, httpClient);
    }
}
