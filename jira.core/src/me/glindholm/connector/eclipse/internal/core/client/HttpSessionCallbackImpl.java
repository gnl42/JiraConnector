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

import org.eclipse.mylyn.commons.net.AbstractWebLocation;

import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholm.theplugin.commons.remoteapi.rest.HttpSessionCallback;

/**
 * An implementation of HttpSessionCallback. Retained for API compatibility.
 * Actual HTTP is now handled via java.net.http.HttpClient in JiraRestClientAdapter.
 *
 * @author Shawn Minto
 * @author Wojciech Seliga
 * @author Jacek Jaroczynski
 */
public class HttpSessionCallbackImpl implements HttpSessionCallback {

    private final Map<String, ConnectionCfg> locations = new HashMap<>();

    public synchronized void removeClient(final ConnectionCfg server) {
        locations.values().remove(server);
    }

    public synchronized void removeClient(final AbstractWebLocation location) {
        locations.remove(location.getUrl());
    }

    public synchronized void updateHostConfiguration(final AbstractWebLocation location, final ConnectionCfg serverCfg) {
        locations.put(location.getUrl(), serverCfg);
    }

    public void clear() {
        locations.clear();
    }

    @Override
    public void disposeClient(final ConnectionCfg server) {
        removeClient(server);
    }
}