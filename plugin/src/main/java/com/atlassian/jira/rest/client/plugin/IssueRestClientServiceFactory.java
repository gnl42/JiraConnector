package com.atlassian.jira.rest.client.plugin;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousIssueRestClient;
import com.atlassian.plugin.remotable.api.annotation.PublicComponent;
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import javax.inject.Inject;
import java.net.URI;

import static com.atlassian.plugin.remotable.spi.util.OsgiServiceProxy.wrapService;

/**
 *
 */
@PublicComponent(IssueRestClient.class)
public class IssueRestClientServiceFactory implements ServiceFactory {
	private final MetadataRestClientServiceFactory metadataRestClient;
	private final SessionRestClientServiceFactory sessionRestClient;

	@Inject
	public IssueRestClientServiceFactory(MetadataRestClientServiceFactory metadataRestClient,
			SessionRestClientServiceFactory sessionRestClient) {
		this.metadataRestClient = metadataRestClient;
		this.sessionRestClient = sessionRestClient;
	}

	@Override
	public Object getService(Bundle bundle, ServiceRegistration registration) {
		final HostHttpClient hostHttpClient = wrapService(bundle.getBundleContext(), HostHttpClient.class,
				getClass().getClassLoader());
		return new AsynchronousIssueRestClient(
				URI.create("."), hostHttpClient,
				sessionRestClient.getService(bundle, hostHttpClient),
				metadataRestClient.getService(bundle, hostHttpClient));
	}

	@Override
	public void ungetService(Bundle bundle, ServiceRegistration registration, Object service
	) {
	}
}
