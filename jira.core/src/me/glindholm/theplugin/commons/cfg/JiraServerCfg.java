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
package me.glindholm.theplugin.commons.cfg;

import me.glindholm.theplugin.commons.ServerType;

public class JiraServerCfg extends ServerCfg {
    private static final int HASHCODE_MAGIC = 31;
    private boolean dontUseBasicAuth = true;
    private UserCfg basicHttpUser;
    private boolean useSessionCookies = false;

    public JiraServerCfg(final String name, final ServerIdImpl serverId, final boolean dontUseBasicAuth) {
        super(true, name, serverId);
        this.dontUseBasicAuth = dontUseBasicAuth;
    }

    public JiraServerCfg(final JiraServerCfg other) {
        super(other);
        dontUseBasicAuth = other.dontUseBasicAuth;
        basicHttpUser = other.basicHttpUser;
        useSessionCookies = other.useSessionCookies;
    }

    public JiraServerCfg(final boolean enabled, final String name, final ServerIdImpl serverId, final boolean dontUseBasicAuth,
            final boolean useSessionCookies) {
        super(enabled, name, serverId);
        this.dontUseBasicAuth = dontUseBasicAuth;
        this.useSessionCookies = useSessionCookies;
    }

    public JiraServerCfg(final boolean enabled, final String name, final String url, final ServerIdImpl serverId, final boolean dontUseBasicAuth,
            final boolean useSessionCookies) {
        super(enabled, name, url, serverId);
        this.dontUseBasicAuth = dontUseBasicAuth;
        this.useSessionCookies = useSessionCookies;
    }

    @Override
    public ServerType getServerType() {
        return ServerType.JIRA_SERVER;
    }

    @Override
    public JiraServerCfg getClone() {
        return new JiraServerCfg(this);
    }

    @Override
    public boolean isDontUseBasicAuth() {
        return dontUseBasicAuth;
    }

    public void setDontUseBasicAuth(final boolean dontUseBasicAuth) {
        this.dontUseBasicAuth = dontUseBasicAuth;
    }

    public void setBasicHttpUser(final UserCfg userCfg) {
        basicHttpUser = userCfg;
    }

    @Override
    public UserCfg getBasicHttpUser() {
        return basicHttpUser;
    }

    @Override
    public boolean isUseSessionCookies() {
        return useSessionCookies;
    }

    public void setUseSessionCookies(final boolean useSessionCookies) {
        this.useSessionCookies = useSessionCookies;
    }

    @Override
    public PrivateServerCfgInfo createPrivateProjectConfiguration() {
        return new PrivateServerCfgInfo(getServerId(), isEnabled(), isUseDefaultCredentials(), getUsername(), isPasswordStored() ? getPassword() : null,
                useSessionCookies, !dontUseBasicAuth, basicHttpUser != null ? basicHttpUser.getUsername() : "",
                basicHttpUser != null ? basicHttpUser.getPassword() : "", isShared());

    }

    @Override
    public void mergePrivateConfiguration(final PrivateServerCfgInfo psci) {
        super.mergePrivateConfiguration(psci);
        if (psci != null) {
            setUseSessionCookies(psci.isUseSessionCookies());
            setDontUseBasicAuth(!psci.isUseHttpBasic());
            setBasicHttpUser(new UserCfg(psci.getBasicUsername(), psci.getBasicPassword()));
        } else {
            setUseSessionCookies(false);
            setDontUseBasicAuth(true);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }

        final JiraServerCfg that = (JiraServerCfg) o;

        if ((useSessionCookies != that.useSessionCookies) || (dontUseBasicAuth != that.dontUseBasicAuth) || (basicHttpUser != null ? !basicHttpUser.equals(that.basicHttpUser) : that.basicHttpUser != null)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (useSessionCookies ? 1 : 0);
        result = 31 * result + (dontUseBasicAuth ? 1 : 0);
        result = 31 * result + (basicHttpUser != null ? basicHttpUser.hashCode() : 0);
        return result;
    }
}
