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

public class PrivateServerCfgInfo {
	protected final ServerIdImpl serverId;
	protected final boolean isEnabled;
	protected final String username;
	protected final String password;
    protected boolean useHttpBasic = false;
    protected boolean useSessionCookies = false;
    protected final String basicUsername;
    protected final String basicPassword;
    protected final boolean useDefaultCredentials;
	private static final int HASHCODE_MAGIC = 31;
    protected boolean shared;

    public PrivateServerCfgInfo(final ServerIdImpl serverId, final boolean enabled, final boolean useDefaultCredentials,
			final String username, final String password, final boolean useSessionCookies, final boolean useHttpBasic, final String basicUsername,
            final String basicPassword, final boolean shared) {
		this.serverId = serverId;
		isEnabled = enabled;
		this.useDefaultCredentials = useDefaultCredentials;
		this.username = username;
		this.password = password;

        this.useSessionCookies = useSessionCookies;
        this.useHttpBasic = useHttpBasic;
        this.basicUsername = basicUsername;
        this.basicPassword = basicPassword;
        this.shared = shared;
    }

	public ServerId getServerId() {
		return serverId;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

    public boolean isUseSessionCookies() {
        return useSessionCookies;
    }

    public boolean isUseHttpBasic() {
        return useHttpBasic;
    }

    public String getBasicUsername() {
        return basicUsername;
    }

    public String getBasicPassword() {
        return basicPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PrivateServerCfgInfo that = (PrivateServerCfgInfo) o;
       
        if (serverId != null ? !serverId.equals(that.serverId) : that.serverId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = serverId != null ? serverId.hashCode() : 0;
        result = 31 * result + (isEnabled ? 1 : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (useSessionCookies ? 1 : 0);
        result = 31 * result + (useHttpBasic ? 1 : 0);
        result = 31 * result + (basicUsername != null ? basicUsername.hashCode() : 0);
        result = 31 * result + (basicPassword != null ? basicPassword.hashCode() : 0);
        result = 31 * result + (useDefaultCredentials ? 1 : 0);
        return result;
    }

    public boolean isEnabled() {
		return isEnabled;
	}

	public boolean isUseDefaultCredentials() {
		return useDefaultCredentials;
	}

    public boolean isShared() {
        return shared;
    }
}
