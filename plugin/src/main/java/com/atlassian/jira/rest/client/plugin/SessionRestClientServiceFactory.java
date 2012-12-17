package com.atlassian.jira.rest.client.plugin;

import com.atlassian.jira.rest.client.api.SessionRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousSessionRestClient;
import com.atlassian.plugin.remotable.api.annotation.PublicComponent;
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import java.net.URI;

import static com.atlassian.plugin.remotable.spi.util.OsgiServiceProxy.wrapService;

/**
 *
 */
@PublicComponent(SessionRestClient.class)
public class SessionRestClientServiceFactory implements ServiceFactory
{
    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        return getService(bundle, wrapService(bundle.getBundleContext(), HostHttpClient.class, getClass().getClassLoader()));
    }

    SessionRestClient getService(Bundle bundle, HostHttpClient hostHttpClient)
    {
        return new AsynchronousSessionRestClient(URI.create("."), hostHttpClient);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service
    )
    {
    }
}
