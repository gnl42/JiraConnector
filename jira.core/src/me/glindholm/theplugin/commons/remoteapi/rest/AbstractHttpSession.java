/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.glindholm.theplugin.commons.remoteapi.rest;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.jdt.annotation.NonNull;

import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholm.theplugin.commons.util.UrlUtil;

/**
 * Stub retained for API compatibility. The ThreadLocal URL tracking is preserved for use by
 * ConnectorTrustManager. All HTTP communication is now handled via java.net.http.HttpClient.
 */
public abstract class AbstractHttpSession {

    @NonNull
    protected final HttpSessionCallback callback;

    @NonNull
    private final ConnectionCfg server;

    private static ThreadLocal<URL> url = new ThreadLocal<>();

    @NonNull
    protected ConnectionCfg getServer() {
        return server;
    }

    protected String getUsername() {
        return server.getUsername();
    }

    protected String getPassword() {
        return server.getPassword();
    }

    public static URL getUrl() {
        return url.get();
    }

    public static void setUrl(final URL urlString) {
        url.set(urlString);
    }

    public static void setUrl(final String urlString) throws MalformedURLException, URISyntaxException {
        url.set(new URI(urlString).toURL());
    }

    protected String getBaseUrl() {
        return UrlUtil.removeUrlTrailingSlashes(server.getUrl());
    }

    public AbstractHttpSession(@NonNull final ConnectionCfg server, @NonNull final HttpSessionCallback callback) {
        this.server = server;
        this.callback = callback;
    }

    public static String getServerNameFromUrl(String urlString) {
        if (urlString == null || urlString.isEmpty()) {
            return "";
        }
        try {
            return new URI(urlString).getHost();
        } catch (final URISyntaxException e) {
            return urlString;
        }
    }
}