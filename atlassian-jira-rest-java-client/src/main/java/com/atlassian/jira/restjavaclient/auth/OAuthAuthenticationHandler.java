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

package com.atlassian.jira.restjavaclient.auth;

import com.atlassian.jira.restjavaclient.AuthenticationHandler;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.Filterable;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.oauth.client.OAuthClientFilter;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;

/**
 * TODO: Document this class / interface here
 * This class is still under construction. Do not use.
 *
 * @since v0.1
 */
public class OAuthAuthenticationHandler implements AuthenticationHandler {

    private final OAuthParameters oAuthParameters;
    private final OAuthSecrets oAuthSecrets;

    public OAuthAuthenticationHandler(OAuthParameters oAuthParameters, OAuthSecrets oAuthSecrets) {
        this.oAuthParameters = oAuthParameters;
        this.oAuthSecrets = oAuthSecrets;
    }

    @Override
    public void configure(ApacheHttpClientConfig apacheHttpClientConfig) {
    }

    @Override
    public void configure(Filterable filterable, Client client) {
        OAuthClientFilter filter = new OAuthClientFilter(client.getProviders(), oAuthParameters, oAuthSecrets);
        filterable.addFilter(filter);
    }
}
