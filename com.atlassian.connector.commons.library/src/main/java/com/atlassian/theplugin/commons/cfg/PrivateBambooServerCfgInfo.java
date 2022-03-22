package com.atlassian.theplugin.commons.cfg;

import com.atlassian.theplugin.commons.util.MiscUtil;

import java.util.ArrayList;
import java.util.Collection;

public class PrivateBambooServerCfgInfo extends PrivateServerCfgInfo {
    protected final int timezoneOffset;
    private Collection<SubscribedPlan> plans = MiscUtil.buildArrayList();
    private final boolean useFavourites;

    public PrivateBambooServerCfgInfo(final ServerIdImpl serverId, final boolean enabled, final boolean useDefaultCredentials,
                                      final String username,
                                      final String password, final int timezoneOffset,
                                      final boolean useSessionCookies,
                                      final boolean useHttpBasic,
                                      final String basicUsername,
                                      final String basicPassword,
                                      final boolean global, Collection<SubscribedPlan> plans, boolean useFavourites) {
        super(serverId, enabled, useDefaultCredentials, username, password, useSessionCookies, useHttpBasic, basicUsername, basicPassword,
                global);
        this.timezoneOffset = timezoneOffset;
        if (plans == null) {
            plans = new ArrayList<SubscribedPlan>();
        } else {
            this.plans = plans;
        }
        this.useFavourites = useFavourites;
    }

    public int getTimezoneOffset() {
        return timezoneOffset;
    }

    public Collection<SubscribedPlan> getPlans() {
        if (plans == null) {
            plans = new ArrayList<SubscribedPlan>();
        }
        return plans;
    }

    public void setPlans(Collection<SubscribedPlan> plans) {
        this.plans = plans;
    }

    public boolean isUseFavourites() {
        return useFavourites;
    }
}
