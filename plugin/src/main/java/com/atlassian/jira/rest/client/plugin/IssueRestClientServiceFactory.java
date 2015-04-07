/*
 * Copyright (C) 2010 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    private final MetadataRestClientServiceFactory metadataRestClientServiceFactory;
    private final SessionRestClientServiceFactory sessionRestClientServiceFactory;

    @Inject
    public IssueRestClientServiceFactory(MetadataRestClientServiceFactory metadataRestClientServiceFactory, SessionRestClientServiceFactory sessionRestClientServiceFactory)
    {
        this.metadataRestClientServiceFactory = checkNotNull(metadataRestClientServiceFactory);
        this.sessionRestClientServiceFactory = checkNotNull(sessionRestClientServiceFactory);
    }

    @Override
    protected IssueRestClient getService(URI baseUri, HttpClient httpClient)
    {
        return new AsynchronousIssueRestClient(
                baseUri,
                httpClient,
                sessionRestClientServiceFactory.getService(baseUri, httpClient),
                metadataRestClientServiceFactory.getService(baseUri, httpClient));
    }
}
