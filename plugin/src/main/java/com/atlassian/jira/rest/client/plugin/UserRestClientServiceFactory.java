package com.atlassian.jira.rest.client.plugin;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.UserRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousUserRestClient;
import com.atlassian.plugin.remotable.api.annotation.PublicComponent;

import java.net.URI;

@PublicComponent(UserRestClient.class)
public class UserRestClientServiceFactory extends AbstractRestClientServiceFactory<UserRestClient>
{
    @Override
    protected UserRestClient getService(URI baseUri, HttpClient httpClient)
    {
        return new AsynchronousUserRestClient(baseUri, httpClient);
    }
}
