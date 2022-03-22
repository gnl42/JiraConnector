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

public abstract class ServerCfg implements Server {
	private static final int HASHCODE_MAGIC = 31;

	public ServerCfg(final boolean enabled, final String name, final ServerIdImpl serverId) {
		this(enabled, name, "", serverId);
	}

	public ServerCfg(final boolean enabled, final String name, final String url, final ServerIdImpl serverId) {
		isEnabled = enabled;
		this.name = name;
		this.serverId = serverId;
		this.url = url;
	}

	// private boolean isShargetPassed;
	private ServerIdImpl serverId;
	private boolean isEnabled;
    private boolean deleted;
	private String name;
	private String url = "";
	private String username = "";
	private String password = "";
	private boolean isPasswordStored;
	private boolean useDefaultCredentials;

    private boolean shared = false;


	protected ServerCfg(final ServerCfg other) {
		serverId = other.getServerId(); // shallow copy here as it's immutable
		isEnabled = other.isEnabled();
		name = other.getName();
		url = other.getUrl();
		username = other.getUsername();
		password = other.getPassword();
		isPasswordStored = other.isPasswordStored();
		useDefaultCredentials = other.useDefaultCredentials;
        shared = other.shared;
        deleted = other.deleted;
	}

	public boolean isUseDefaultCredentials() {
		return useDefaultCredentials;
	}

	public void setUseDefaultCredentials(final boolean useDefaultCredentials) {
		this.useDefaultCredentials = useDefaultCredentials;
	}

	// this method is used by XStream - do not remove!!!
	protected Object readResolve() {
		if (username == null) {
			username = "";
		}
		if (password == null) {
			password = "";
		}
		return this;
	}


	public String getPassword() {
		return password;
	}

	public abstract ServerType getServerType();


	public String getUsername() {
		return username;
	}

	public void setEnabled(final boolean enabled) {
		isEnabled = enabled && !deleted;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setServerId(final ServerIdImpl serverId) {
		this.serverId = serverId;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public void setPasswordStored(final boolean passwordStored) {
		isPasswordStored = passwordStored;
	}

	public boolean isEnabled() {
		return isEnabled && !deleted;
	}

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getName() {
		return name;
	}

	public ServerIdImpl getServerId() {
		return serverId;
	}

	public String getUrl() {
		return url;
	}

	public boolean isPasswordStored() {
		return isPasswordStored;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ServerCfg)) {
			return false;
		}

		final ServerCfg serverCfg = (ServerCfg) o;

		if (isEnabled != serverCfg.isEnabled) {
			return false;
		}

        if (deleted != serverCfg.deleted) {
            return false;
        }

        if (isPasswordStored != serverCfg.isPasswordStored) {
			return false;
		}

        if (shared != serverCfg.shared) {
			return false;
		}

		if (useDefaultCredentials != serverCfg.useDefaultCredentials) {
			return false;
		}
		if (name != null ? !name.equals(serverCfg.name) : serverCfg.name != null) {
			return false;
		}
		if (password != null ? !password.equals(serverCfg.password) : serverCfg.password != null) {
			return false;
		}
		if (serverId != null ? !serverId.equals(serverCfg.serverId) : serverCfg.serverId != null) {
			return false;
		}
		if (url != null ? !url.equals(serverCfg.url) : serverCfg.url != null) {
			return false;
		}
		if (username != null ? !username.equals(serverCfg.username) : serverCfg.username != null) {
			return false;
		}



		return true;
	}

	@Override
	public int hashCode() {
		int result;
		result = (isEnabled ? 1 : 0);
		result = HASHCODE_MAGIC * result + (name != null ? name.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (serverId != null ? serverId.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (url != null ? url.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (username != null ? username.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (password != null ? password.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (isPasswordStored ? 1 : 0);
        result = HASHCODE_MAGIC * result + (shared ? 1 : 0);
        result = HASHCODE_MAGIC * result + (deleted ? 1 : 0);
		result = HASHCODE_MAGIC * result + (useDefaultCredentials ? 1 : 0);
		return result;
	}

	/**
	 * Returns real clone (deep copy of this object). It intentionally does not use standard Java Cloneable as it sucks
	 * terriblyk
	 *
	 * @return deep copy
	 */
	public abstract ServerCfg getClone();

	public boolean isComplete() {
		return getPassword() != null && getPassword().length() != 0;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("name = ");
		builder.append(getName());
		builder.append(", url = ");
		builder.append(getUrl());
		return builder.toString();
	}

	public PrivateServerCfgInfo createPrivateProjectConfiguration() {
		return new PrivateServerCfgInfo(getServerId(), isEnabled(), isUseDefaultCredentials(),
				getUsername(), isPasswordStored() ? getPassword() : null,
                isUseSessionCookies(), false, null, null, shared);
	}


	public void mergePrivateConfiguration(PrivateServerCfgInfo psci) {
		if (psci != null) {
			setUsername(psci.getUsername());
			setEnabled(psci.isEnabled());
			setUseDefaultCredentials(psci.isUseDefaultCredentials());
            setShared(psci.isShared());
			final String pwd = psci.getPassword();
			if (pwd != null) {
				setPassword(pwd);
				setPasswordStored(true);
			} else {
				setPasswordStored(false);
			}
		} else {
			setPasswordStored(false);
			setEnabled(true); // new servers (for which there was no private info yet) are enabled by default
		}
	}

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
	}
}
