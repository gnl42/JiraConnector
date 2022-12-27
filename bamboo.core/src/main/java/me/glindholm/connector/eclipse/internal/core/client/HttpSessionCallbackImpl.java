/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package me.glindholm.connector.eclipse.internal.core.client;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.WebUtil;

import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholm.connector.eclipse.internal.core.JiraConnectorCorePlugin;
import me.glindholm.theplugin.commons.exception.HttpProxySettingsException;
import me.glindholm.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import me.glindholm.theplugin.commons.remoteapi.rest.HttpSessionCallback;

/**
 * An implementation of HttpSessionCallback that can handle setting the
 * HttpClient information on a per-server basis
 *
 * @author Shawn Minto
 * @author Wojciech Seliga
 * @author Jacek Jaroczynski
 */
public class HttpSessionCallbackImpl implements HttpSessionCallback {
    public static final String PRODUCT_NAME = "Eclipse Mylyn JiraConnector for  Atlassian's Jira/Bamboo";

    private final String userAgent;

    /** synchronized on this HttpSessionCallbackImpl */
    private final Map<ConnectionCfg, HttpClient> httpClients = new HashMap<>();

    private final Map<String, ConnectionCfg> locations = new HashMap<>();

    public HttpSessionCallbackImpl() {
        userAgent = PRODUCT_NAME + "/" + JiraConnectorCorePlugin.getDefault().getVersion();
    }

    @Override
    public synchronized HttpClient getHttpClient(final ConnectionCfg server) throws HttpProxySettingsException {
        final HttpClient httpClient = httpClients.get(server);

        // TODO handle the case where we dont have a client initialized
        assert httpClient != null;

        httpClient.getParams().setParameter(HttpMethodParams.USER_AGENT, userAgent);
        return httpClient;
    }

    @Override
    public void configureHttpMethod(final AbstractHttpSession session, final HttpMethod method) {
        // nothing to do here
    }

    public synchronized void removeClient(final ConnectionCfg server) {
        final HttpClient client = httpClients.remove(server);
        if (client != null) {
            shutdown(client);
        }
    }

    public synchronized void removeClient(final AbstractWebLocation location) {
        final ConnectionCfg server = locations.remove(location.getUrl());
        if (server != null) {
            removeClient(server);
        }
    }

    public synchronized void updateHostConfiguration(final AbstractWebLocation location, final ConnectionCfg serverCfg) {
        HttpClient httpClient = httpClients.get(serverCfg);
        if (httpClient == null) {
            httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
            httpClients.put(serverCfg, httpClient);
            locations.put(location.getUrl(), serverCfg);
            WebUtil.addConnectionManager(httpClient.getHttpConnectionManager());
        }
        setupHttpClient(location, httpClient);
    }

    private void setupHttpClient(final AbstractWebLocation location, final HttpClient httpClient) {
        final HostConfiguration hostConfiguration = WebUtil.createHostConfiguration(httpClient, location, new NullProgressMonitor());
        httpClient.setHostConfiguration(hostConfiguration);

        final AuthenticationCredentials proxyCredentials = location.getCredentials(AuthenticationType.PROXY);

        // check for domain name slash \ in the proxy user
        if (proxyCredentials != null && proxyCredentials.getUserName() != null && proxyCredentials.getUserName().contains("\\")) {
            // NTLM proxy detected - disable preemptive auth (httpClient limitation -
            // preemptive auth does not work with NTLM)
            httpClient.getParams().setAuthenticationPreemptive(false);
            StatusHandler.log(new Status(IStatus.INFO, JiraConnectorCorePlugin.PLUGIN_ID, "NTLM proxy detected. Preemptive authentication disabled."));
        } else {
            httpClient.getParams().setAuthenticationPreemptive(true);
        }

    }

    @Override
    protected void finalize() throws Throwable {
        for (final HttpClient httpClient : httpClients.values()) {
            shutdown(httpClient);
        }
        httpClients.clear();
    }

    public void shutdown(final HttpClient httpClient) {
        final HttpConnectionManager mgr = httpClient.getHttpConnectionManager();
        WebUtil.removeConnectionManager(mgr);
        ((MultiThreadedHttpConnectionManager) mgr).shutdown();
    }

    public void clear() {
        locations.clear();
        httpClients.clear();
    }

    @Override
    public void disposeClient(final ConnectionCfg server) {
        removeClient(server);
    }

    @Override
    public Cookie[] getCookiesHeaders(final ConnectionCfg server) {
        try {
            return getHttpClient(server).getState().getCookies();
        } catch (final HttpProxySettingsException e) {
            return new Cookie[0];
        }
    }

}
