/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package me.glindholm.connector.commons.api;

import java.util.Base64;
import java.util.Objects;

import me.glindholm.bamboo.invoker.ApiClient;

public class ConnectionCfg {
    private final String url;
    private final String username;
    private final String password;
    private final String id;
    private final ApiClient apiClient;

    public ConnectionCfg(final String id, final String url, final String username, final String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.id = id;

        apiClient = new ApiClient();
        apiClient.updateBaseUri(url + "/rest");
        apiClient.setRequestInterceptor(authorize -> authorize.header("Authorization", basicAuth(username, password)));
    }

    private void connect() {
    }

    private static String basicAuth(final String username, final String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getId() {
        return id;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiClient, id, password, url, username);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ConnectionCfg)) {
            return false;
        }
        final ConnectionCfg other = (ConnectionCfg) obj;
        return Objects.equals(apiClient, other.apiClient) && Objects.equals(id, other.id) && Objects.equals(password, other.password)
                && Objects.equals(url, other.url) && Objects.equals(username, other.username);
    }

}
