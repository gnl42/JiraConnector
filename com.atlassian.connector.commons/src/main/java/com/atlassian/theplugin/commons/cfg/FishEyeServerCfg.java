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

public class FishEyeServerCfg extends ServerCfg implements FishEyeServer {
	public FishEyeServerCfg(final boolean enabled, final String name, final ServerIdImpl serverId) {
		super(enabled, name, serverId);
	}


	protected FishEyeServerCfg(final FishEyeServerCfg other) {
		super(other);
	}

	public FishEyeServerCfg(final String name, final ServerIdImpl serverId) {
		this(true, name, serverId);
	}

	@Override
	public ServerType getServerType() {
		return ServerType.FISHEYE_SERVER;
	}

    public boolean isDontUseBasicAuth() {
        return false;
    }

    public boolean isUseSessionCookies() {
        return false;
    }

    public UserCfg getBasicHttpUser() {
        return null;  
    }

    @Override
	public FishEyeServerCfg getClone() {
		return new FishEyeServerCfg(this);
	}

	@Override
	public FishEyeServer asFishEyeServer() {
		return this;
	}

	public ProjectId getProjectId() {
		return null;
	}
}
