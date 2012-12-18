package com.atlassian.jira.rest.client.plugin;

import com.atlassian.jira.rest.client.api.ComponentRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousComponentRestClient;
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
@PublicComponent(ComponentRestClient.class)
public class ComponentRestClientServiceFactory implements ServiceFactory {
	@Override
	public Object getService(Bundle bundle, ServiceRegistration registration) {
		return new AsynchronousComponentRestClient(URI.create("."),
				wrapService(bundle.getBundleContext(), HostHttpClient.class, getClass().getClassLoader()));
	}

	@Override
	public void ungetService(Bundle bundle, ServiceRegistration registration, Object service
	) {
	}
}
