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
package me.glindholm.theplugin.commons.remoteapi;

import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholm.theplugin.commons.ServerType;
import me.glindholm.theplugin.commons.cfg.Server;
import me.glindholm.theplugin.commons.cfg.ServerId;
import me.glindholm.theplugin.commons.cfg.UserCfg;

/**
 * @author pmaruszak
 */
public class ServerData extends ConnectionCfg {
    private final Server server;
    private boolean useProxy = false;
    private UserCfg basicUser;
    private UserCfg proxyUser;
    private boolean useBasicUser = false;
    private boolean useSessionCookies = false;

//    private boolean serverResponding = false;

    public ServerData(final Builder builder) {
        super(builder.server != null && builder.server.getServerId() != null ? builder.server.getServerId().getId() : "",
                builder.server != null ? builder.server.getUrl() : "",
                builder.useDefaultUser ? builder.defaultUser.getUsername() : builder.server != null ? builder.server.getUsername() : "",
                builder.useDefaultUser ? builder.defaultUser.getPassword() : builder.server != null ? builder.server.getPassword() : "");
        server = builder.server;
        basicUser = builder.basicUser;
        useProxy = builder.useProxyUser;
        proxyUser = builder.proxyUser;
        useBasicUser = builder.useBasicUser;
        useSessionCookies = builder.useSessionCookies;
    }

    private ServerData(final ServerData that) {
        super(that.getId(), that.getUrl(), that.getUsername(), that.getPassword());
        server = that.server;
        useProxy = that.useProxy;
        basicUser = that.basicUser;
        proxyUser = that.proxyUser;
        useBasicUser = that.useBasicUser;
        useSessionCookies = that.useSessionCookies;
    }

    public static class Builder {
        // required params
        protected final Server server;
        // optional params
        protected UserCfg defaultUser;
        protected UserCfg basicUser;
        protected UserCfg proxyUser;
        protected boolean useDefaultUser = false;
        protected boolean useProxyUser = false;
        private boolean useBasicUser;
        private boolean useSessionCookies = false;

        public Builder(final Server server) {
            this.server = server;
            basicUser = server.getBasicHttpUser();
            useBasicUser = !server.isDontUseBasicAuth();
            useDefaultUser = server.isUseDefaultCredentials();
            useSessionCookies = server.isUseSessionCookies();
        }

        public Builder(final Server server, final UserCfg defaultUser) {
            this(server);
            this.defaultUser = defaultUser;
        }

        protected Server getServer() {
            return server;
        }

        public void basicUser(final UserCfg basicUsr) {
            basicUser = basicUsr;
        }

        public void defaultUser(final UserCfg defaultUsr) {
            defaultUser = defaultUsr;
        }

        public void proxyUser(final UserCfg proxyUsr) {
            proxyUser = proxyUsr;
        }

        public void useDefaultUser(final boolean useDefault) {
            useDefaultUser = useDefault;
        }

        public void useProxyUser(final boolean useProxy) {
            useProxyUser = useProxy;
        }

        public void useBasicUser(final boolean useBasic) {
            useBasicUser = useBasic;
        }

        public ServerData build() {
            return new ServerData(this);
        }

        public void setUseSessionCookies(final boolean useSessionCookies) {
            this.useSessionCookies = useSessionCookies;
        }
    }

    public ServerData(final Server server) {
        super(server != null ? server.getServerId() != null ? server.getServerId().getId() : "" : "", server != null ? server.getUrl() : "",
                server != null ? server.getUsername() : "", server != null ? server.getPassword() : "");
        this.server = server;
        if (server != null) {
            useBasicUser = !server.isDontUseBasicAuth();
            basicUser = server.getBasicHttpUser();
        }
    }

    public ServerData(final Server server, final UserCfg defaultUser) {
        super(server != null && server.getServerId() != null ? server.getServerId().getId() : "", server != null ? server.getUrl() : "",
                defaultUser != null ? defaultUser.getUsername() : server != null ? server.getUsername() : "",
                defaultUser != null ? defaultUser.getPassword() : server != null ? server.getPassword() : "");
        this.server = server;

    }

    protected Server getServer() {
        return server;
    }

    @Override
    public String getUsername() {
        return super.getUsername();
    }

    @Override
    public String getPassword() {
        return super.getPassword();
    }

    @Override
    public String getUrl() {
        return server.getUrl();
    }

    public ServerId getServerId() {
        return server.getServerId();
    }

    public String getName() {
        return server.getName();
    }

    public boolean isEnabled() {
        return server.isEnabled();
    }

    public ServerType getServerType() {
        return server.getServerType();
    }

    public boolean isUseProxy() {
        return useProxy;
    }

    public UserCfg getBasicUser() {
        return basicUser;
    }

    public UserCfg getProxyUser() {
        return proxyUser;
    }

    public boolean isUseBasicUser() {
        return useBasicUser;
    }

    public boolean isUseSessionCookies() {
        return useSessionCookies;
    }

    // public boolean isServerResponding() {
//        return serverResponding;
//    }

//    public void setServerResponding(boolean serverResponding) {
//        this.serverResponding = serverResponding;
//    }

    @Override
    /**
     * Beware when overriding this method. It uses instanceof instead of getClass(). Remember to keep
     * 'symmetry'
     */
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof final ServerData that)) { // getClass() != o.getClass()) {
            return false;
        }

        if ((getServerId() != null ? !getServerId().equals(that.getServerId()) : that.getServerId() != null) || (getUrl() != null ? !getUrl().equals(that.getUrl()) : that.getUrl() != null)) {
            return false;
        }

        if (getPassword() == null) {
            if (that.getPassword() != null) {
                return false;
            }
        } else if (!getPassword().equals(that.getPassword())) {
            return false;
        }

        if (getUsername() == null) {
            if (that.getUsername() != null) {
                return false;
            }
        } else if (!getUsername().equals(that.getUsername())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result;
        // todo do we want to use name for hashCode and Equals???
//		result = (name != null ? name.hashCode() : 0);
        result = getServerId() != null ? getServerId().hashCode() : 0;
        result = 31 * result + super.hashCode();
        result = 31 * result + (getUrl() != null ? getUrl().hashCode() : 0);
        return result;
    }

    public ConnectionCfg toConnectionCfg() {
        return new ServerData(this);
    }

}
