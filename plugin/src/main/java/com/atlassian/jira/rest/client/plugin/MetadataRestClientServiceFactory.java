package com.atlassian.jira.rest.client.plugin;

import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousMetadataRestClient;
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
@PublicComponent(MetadataRestClient.class)
public class MetadataRestClientServiceFactory implements ServiceFactory {
	@Override
	public Object getService(Bundle bundle, ServiceRegistration registration) {
		return getService(bundle, wrapService(bundle.getBundleContext(), HostHttpClient.class, getClass().getClassLoader()));
	}

	MetadataRestClient getService(Bundle bundle, HostHttpClient hostHttpClient) {
		return new AsynchronousMetadataRestClient(URI.create("."), hostHttpClient);
	}

	@Override
	public void ungetService(Bundle bundle, ServiceRegistration registration, Object service
	) {
	}
}
