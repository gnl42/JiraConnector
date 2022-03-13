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
import com.atlassian.theplugin.commons.util.MiscUtil;

import java.util.Collection;

public class BambooServerCfg extends ServerCfg {

	private boolean isUseFavourites;
    private boolean showBranches;
    private boolean myBranchesOnly;
	private boolean isBamboo2;

	private Collection<SubscribedPlan> plans = MiscUtil.buildArrayList();
	private int timezoneOffset;

    public BambooServerCfg(final String name, final ServerIdImpl serverId) {
		super(true, name, serverId);
	}

	public BambooServerCfg(final String name, final String url, final ServerIdImpl serverId) {
		super(true, name, url, serverId);
	}

	public BambooServerCfg(final boolean enabled, final String name, final ServerIdImpl serverId) {
		super(enabled, name, serverId);
        showBranches = true;
	}

	public BambooServerCfg(final BambooServerCfg other) {
		super(other);
		isUseFavourites = other.isUseFavourites();
        showBranches = other.isShowBranches();
        myBranchesOnly = other.isMyBranchesOnly();
		isBamboo2 = other.isBamboo2();
		// shallow copy of SubscribedPlan objects is enough as they are immutable
		plans = MiscUtil.buildArrayList(other.getPlans());
		timezoneOffset = other.timezoneOffset;        
	}

	@Override
	public ServerType getServerType() {
		return ServerType.BAMBOO_SERVER;
	}

    public boolean isDontUseBasicAuth() {
        return true;
    }

    public boolean isUseSessionCookies() {
        return false;
    }

    public UserCfg getBasicHttpUser() {
        return null;
    }

    public boolean isUseFavourites() {
		return isUseFavourites;
	}

	public Collection<SubscribedPlan> getSubscribedPlans() {
		return plans;
	}

	public void clearSubscribedPlans() {
		plans.clear();
	}

	public void setUseFavourites(final boolean useFavourites) {
		isUseFavourites = useFavourites;
	}

    public void setShowBranches(boolean showBranches) {
        this.showBranches = showBranches;
    }

    public void setMyBranchesOnly(boolean myBranchesOnly) {
        this.myBranchesOnly = myBranchesOnly;
    }

    public int getTimezoneOffset() {
		return timezoneOffset;
	}

	public void setTimezoneOffset(int timezoneOffset) {
		this.timezoneOffset = timezoneOffset;
	}

	@Override
	public boolean equals(final Object o) {
		if (super.equals(o) == false) {
			return false;
		}

		if (this == o) {
			return true;
		}
		if (!(o instanceof BambooServerCfg)) {
			return false;
		}

		final BambooServerCfg that = (BambooServerCfg) o;

		if (isUseFavourites != that.isUseFavourites) {
			return false;
		}

        if (showBranches != that.showBranches) {
            return false;
        }

        if (myBranchesOnly != that.myBranchesOnly) {
            return false;
        }

        if (plans.equals(that.plans) == false) {
			return false;
		}

		if (timezoneOffset != that.timezoneOffset) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (isUseFavourites ? 1 : 0);
        result = 31 * result + (showBranches ? 1 : 0);
        result = 31 * result + (myBranchesOnly ? 1 : 0);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Bamboo Server [");
		builder.append(super.toString());
		builder.append("]");
		return builder.toString();
	}

	public void setIsBamboo2(final boolean b) {
		isBamboo2 = b;
	}

	public boolean isBamboo2() {
		return isBamboo2;
	}

    public boolean isShowBranches() {
        return showBranches;
    }

    public boolean isMyBranchesOnly() {
        return myBranchesOnly;
    }

    public Collection<SubscribedPlan> getPlans() {
		return plans;
	}

	public void setPlans(final Collection<SubscribedPlan> plans) {
		this.plans = plans;
	}

	@Override
	public BambooServerCfg getClone() {
		return new BambooServerCfg(this);
	}

	// this method is used by XStream - do not remove!!!
	@Override
	protected Object readResolve() {
		super.readResolve();
		if (plans == null) {
			plans = MiscUtil.buildArrayList();
		}
		return this;
	}

	@Override
	public PrivateServerCfgInfo createPrivateProjectConfiguration() {
		return new PrivateBambooServerCfgInfo(getServerId(), isEnabled(), isUseDefaultCredentials(),
				getUsername(), isPasswordStored() ? getPassword() : null, getTimezoneOffset(), false, false, "", "", isShared(),
                getPlans(), isUseFavourites);
	}

	@Override
	public void mergePrivateConfiguration(PrivateServerCfgInfo psci) {
		super.mergePrivateConfiguration(psci);
		if (psci != null) {
			try {
				setTimezoneOffset(((PrivateBambooServerCfgInfo) psci).getTimezoneOffset());
                setPlans(((PrivateBambooServerCfgInfo) psci).getPlans());
                isUseFavourites = ((PrivateBambooServerCfgInfo) psci).isUseFavourites();
			} catch (ClassCastException e) {
				// Echo Whisky Tango Foxtrot?
			}
		}

	}
}
