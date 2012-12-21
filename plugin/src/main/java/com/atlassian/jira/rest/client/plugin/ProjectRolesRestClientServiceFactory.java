package com.atlassian.jira.rest.client.plugin;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.ProjectRolesRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousProjectRolesRestClient;
import com.atlassian.plugin.remotable.api.annotation.PublicComponent;

import java.net.URI;

@PublicComponent(ProjectRolesRestClient.class)
public final class ProjectRolesRestClientServiceFactory extends AbstractRestClientServiceFactory<ProjectRolesRestClient>
{
    @Override
    protected ProjectRolesRestClient getService(URI baseUri, HttpClient httpClient)
    {
        return new AsynchronousProjectRolesRestClient(baseUri, httpClient);
    }
}
