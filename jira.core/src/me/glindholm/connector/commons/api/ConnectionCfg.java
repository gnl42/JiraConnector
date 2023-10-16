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

import java.util.Objects;

public class ConnectionCfg {
    private final String url;
    private final String username;
    private final String password;
    private final String id;

    public ConnectionCfg(final String id, final String url, final String username, final String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.id = id;
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

    @Override
    public int hashCode() {
        return Objects.hash(id, password, url, username);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final ConnectionCfg other = (ConnectionCfg) obj;
        if (!Objects.equals(id, other.id)) {
            return false;
        }
        if (!Objects.equals(password, other.password)) {
            return false;
        }
        if (!Objects.equals(url, other.url)) {
            return false;
        }
        if (!Objects.equals(username, other.username)) {
            return false;
        }
        return true;
    }

}
