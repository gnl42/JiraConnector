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
package me.glindholm.theplugin.commons.bamboo;

import org.jetbrains.annotations.NotNull;

import me.glindholm.theplugin.commons.cfg.BambooServerCfg;
import me.glindholm.theplugin.commons.cfg.Server;
import me.glindholm.theplugin.commons.cfg.SubscribedPlan;
import me.glindholm.theplugin.commons.cfg.UserCfg;
import me.glindholm.theplugin.commons.remoteapi.ServerData;

import java.util.Collection;

/**
 * @author Jacek Jaroczynski
 */
public class BambooServerData extends ServerData {
	public BambooServerData(@NotNull BambooServerCfg server) {
		super(server);
	}

    public BambooServerData(Builder builder) {
        super(builder);

    }

    public BambooServerData(BambooServerCfg server, UserCfg defaultUser) {
        super(server, defaultUser);
    }

    public static class Builder extends ServerData.Builder {

        public Builder(Server server) {
            super(server);
        }

		public Builder(Server server, UserCfg defaultUser) {
			super(server, defaultUser);
		}

		@Override
        public BambooServerData build() {
            return new BambooServerData(this);
        }

        @Override
        protected Server getServer() {
            return super.getServer();
        }
    }	
	public Collection<SubscribedPlan> getPlans() {
		return getServer().getPlans();
	}

	public boolean isUseFavourites() {
		return getServer().isUseFavourites();
}

	public int getTimezoneOffset() {
		return getServer().getTimezoneOffset();
	}

	public boolean isBamboo2() {
		return getServer().isBamboo2();
	}

    public boolean isShowBranches() {
        return getServer().isShowBranches();
    }

    public boolean isMyBranchesOnly() {
        return getServer().isMyBranchesOnly();
    }

	@Override
	protected BambooServerCfg getServer() {
		return (BambooServerCfg) super.getServer();
	}
}
