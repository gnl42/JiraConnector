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

package com.atlassian.jira.rest.restjavaclient.auth;

import com.atlassian.jira.rest.restjavaclient.AuthenticationHandler;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.Filterable;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;

/**
* Anonymous handler - no credentials passed to the server. Only anonymously accessible operations will be possible. 
*
* @since v0.1
*/
public class AnonymousAuthenticationHandler implements AuthenticationHandler {
	@Override
	public void configure(ApacheHttpClientConfig apacheHttpClientConfig) {
	}

	@Override
	public void configure(Filterable filterable, Client client) {
	}
}
