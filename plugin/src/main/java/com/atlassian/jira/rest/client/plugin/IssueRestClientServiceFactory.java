package com.atlassian.jira.rest.client.plugin;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousIssueRestClient;
import com.atlassian.plugin.remotable.api.annotation.PublicComponent;

import javax.inject.Inject;
import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

@PublicComponent(IssueRestClient.class)
public final class IssueRestClientServiceFactory extends AbstractRestClientServiceFactory<IssueRestClient>
{
    private final MetadataRestClientServiceFactory metadataRestClient;
    private final SessionRestClientServiceFactory sessionRestClient;

    @Inject
    public IssueRestClientServiceFactory(MetadataRestClientServiceFactory metadataRestClient, SessionRestClientServiceFactory sessionRestClient)
    {
        this.metadataRestClient = checkNotNull(metadataRestClient);
        this.sessionRestClient = checkNotNull(sessionRestClient);
    }

    @Override
    protected IssueRestClient getService(URI baseUri, HttpClient httpClient)
    {
        return new AsynchronousIssueRestClient(
                baseUri,
                httpClient,
                sessionRestClient.getService(baseUri, httpClient),
                metadataRestClient.getService(baseUri, httpClient));
    }
}
