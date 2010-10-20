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

package com.atlassian.jira.rest.restjavaclient.internal.jersey;

import com.atlassian.jira.rest.restjavaclient.ComponentRestClient;
import com.atlassian.jira.rest.restjavaclient.ProgressMonitor;
import com.atlassian.jira.rest.restjavaclient.domain.Component;
import com.atlassian.jira.rest.restjavaclient.internal.json.ComponentJsonParser;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache.ApacheHttpClient;
import org.codehaus.jettison.json.JSONObject;

import java.net.URI;
import java.util.concurrent.Callable;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class JerseyComponentRestClient extends AbstractJerseyRestClient implements ComponentRestClient {

	private final ComponentJsonParser componentJsonParser = new ComponentJsonParser();
	
	public JerseyComponentRestClient(URI baseUri, ApacheHttpClient client) {
		super(baseUri, client);
	}

	@Override
	public Component getComponent(final URI componentUri, ProgressMonitor progressMonitor) {
		return invoke(new Callable<Component>() {
			@Override
			public Component call() throws Exception {
				final WebResource componentResource = client.resource(componentUri);
				final JSONObject jsonObject = componentResource.get(JSONObject.class);
				return componentJsonParser.parse(jsonObject);
			}
		});
	}

}
