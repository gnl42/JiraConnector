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
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import java.net.URI;

import static com.atlassian.plugin.remotable.spi.util.OsgiServiceProxy.wrapService;

abstract class AbstractRestClientServiceFactory<T> implements ServiceFactory
{
    private static final URI BASE_URI = URI.create("/rest/api/latest");

    @Override
    public final Object getService(Bundle bundle, ServiceRegistration registration)
    {
        return getService(BASE_URI, getHttpClient(bundle));
    }

    /**
     * No-op.
     */
    @Override
    public final void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
    }

    protected abstract T getService(URI baseUri, HttpClient httpClient);

    protected final HttpClient getHttpClient(Bundle bundle)
    {
        return wrapService(bundle.getBundleContext(), HostHttpClient.class, getClass().getClassLoader());
    }
}
