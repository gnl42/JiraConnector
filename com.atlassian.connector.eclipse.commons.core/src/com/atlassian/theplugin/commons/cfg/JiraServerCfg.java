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
package com.atlassian.theplugin.commons.cfg;

import com.atlassian.theplugin.commons.ServerType;

public class JiraServerCfg extends ServerCfg {
    private static final int HASHCODE_MAGIC = 31;
    private boolean dontUseBasicAuth = true;
    private UserCfg basicHttpUser;
    private boolean useSessionCookies = false;

    public JiraServerCfg(final String name, final ServerIdImpl serverId, boolean dontUseBasicAuth) {
		super(true, name, serverId);
        this.dontUseBasicAuth = dontUseBasicAuth;
    }

	public JiraServerCfg(final JiraServerCfg other) {
		super(other);
        this.dontUseBasicAuth = other.dontUseBasicAuth;
        this.basicHttpUser = other.basicHttpUser;
        this.useSessionCookies = other.useSessionCookies;
	}

	public JiraServerCfg(boolean enabled, String name, ServerIdImpl serverId, boolean dontUseBasicAuth, boolean useSessionCookies) {
		super(enabled, name, serverId);
        this.dontUseBasicAuth = dontUseBasicAuth;
        this.useSessionCookies = useSessionCookies;
    }

    public JiraServerCfg(boolean enabled, String name, String url, ServerIdImpl serverId, boolean dontUseBasicAuth, boolean useSessionCookies) {
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

    public boolean isDontUseBasicAuth() {
        return dontUseBasicAuth;
    }

    
    public void setDontUseBasicAuth(boolean dontUseBasicAuth) {
        this.dontUseBasicAuth = dontUseBasicAuth;
    }

    public void setBasicHttpUser(UserCfg userCfg) {
        this.basicHttpUser = userCfg;
    }

    public UserCfg getBasicHttpUser() {
        return basicHttpUser;
    }

    public boolean isUseSessionCookies() {
        return useSessionCookies;
    }

    public void setUseSessionCookies(boolean useSessionCookies) {
        this.useSessionCookies = useSessionCookies;
    }

    @Override
    public PrivateServerCfgInfo createPrivateProjectConfiguration() {
		return new PrivateServerCfgInfo(getServerId(), isEnabled(), isUseDefaultCredentials(),
				getUsername(), isPasswordStored() ? getPassword() : null,  useSessionCookies,
                !dontUseBasicAuth,
                basicHttpUser != null ? basicHttpUser.getUsername() : "",
                basicHttpUser != null ? basicHttpUser.getPassword() : "",
                isShared());

    }

    @Override
    public void mergePrivateConfiguration(PrivateServerCfgInfo psci) {
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        JiraServerCfg that = (JiraServerCfg) o;

        if (useSessionCookies != that.useSessionCookies) {
            return false;
        }
        if (dontUseBasicAuth != that.dontUseBasicAuth) {
            return false;
        }
        if (basicHttpUser != null ? !basicHttpUser.equals(that.basicHttpUser) : that.basicHttpUser != null) {
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
