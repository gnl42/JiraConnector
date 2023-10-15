package me.glindholm.theplugin.commons.jira;

import me.glindholm.theplugin.commons.cfg.Server;
import me.glindholm.theplugin.commons.cfg.UserCfg;
import me.glindholm.theplugin.commons.remoteapi.ServerData;

/**
 * User: kalamon Date: Aug 19, 2009 Time: 3:52:44 PM
 */
public class JiraServerData extends ServerData {

    public JiraServerData(final Server server) {
        super(server);
    }

    public JiraServerData(final Builder builder) {
        super(builder);
    }

    public JiraServerData(final Server server, final UserCfg defaultUser) {
        super(server, defaultUser);
    }

    public static class Builder extends ServerData.Builder {

        public Builder(final Server server) {
            super(server);
        }

        public Builder(final Server server, final UserCfg defaultUser) {
            super(server, defaultUser);
        }

        @Override
        public JiraServerData build() {
            return new JiraServerData(this);
        }
    }
}
