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

import java.util.ArrayList;
import java.util.Collection;

import me.glindholm.theplugin.commons.ServerType;
import me.glindholm.theplugin.commons.util.MiscUtil;

public class ProjectConfiguration {
    private final Collection<ServerCfg> servers;

    private ServerIdImpl defaultJiraServerId;

    private static final int HASHCODE_MAGIC = 31;

    public ProjectConfiguration(final ProjectConfiguration other) {
        servers = cloneArrayList(other.getServers());
        defaultJiraServerId = other.defaultJiraServerId;
    }

    public static Collection<ServerCfg> cloneArrayList(final Collection<ServerCfg> collection) {
        final ArrayList<ServerCfg> res = new ArrayList<>(collection.size());
        for (final ServerCfg serverCfg : collection) {
            res.add(serverCfg.getClone());
        }
        return res;
    }

    public ProjectConfiguration(final Collection<ServerCfg> servers) {
        if (servers == null) {
            throw new NullPointerException("Servers cannot be null");
        }
        this.servers = servers;
    }

    public ProjectConfiguration() {
        servers = MiscUtil.buildArrayList();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        // noinspection RedundantIfStatement
        if (!(o instanceof final ProjectConfiguration that)
                || (defaultJiraServerId != null ? !defaultJiraServerId.equals(that.defaultJiraServerId) : that.defaultJiraServerId != null)
                || !servers.equals(that.servers)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = servers.hashCode();
        result = HASHCODE_MAGIC * result + (defaultJiraServerId != null ? defaultJiraServerId.hashCode() : 0);
        return result;
    }

    public Collection<ServerCfg> getServers() {
        return servers;
    }

    public ServerCfg getServerCfg(final ServerId serverId) {
        for (final ServerCfg serverCfg : servers) {
            if (serverId.equals(serverCfg.getServerId())) {
                return serverCfg;
            }
        }
        return null;
    }

    public static ProjectConfiguration emptyConfiguration() {
        return new ProjectConfiguration();
    }

    public ProjectConfiguration getClone() {
        return new ProjectConfiguration(this);
    }

    public ServerId getDefaultJiraServerId() {
        if (defaultJiraServerId == null && getAllJIRAServers().size() == 1) {
            final JiraServerCfg jiraServerCfg = getAllJIRAServers().iterator().next();
            if (jiraServerCfg.isEnabled()) {
                defaultJiraServerId = jiraServerCfg.getServerId();
            }
        }

        return defaultJiraServerId;
    }

    public JiraServerCfg getDefaultJiraServer() {
        if (getDefaultJiraServerId() == null) {
            return null;
        }

        final ServerCfg serverCfg = getServerCfg(getDefaultJiraServerId());

        // no additional check - let IDE handle such error in a standard way (error dialog)
        // in unlikely event of some fuck-up
        final JiraServerCfg jiraServerCfg = (JiraServerCfg) serverCfg;
        if (jiraServerCfg == null || !jiraServerCfg.isEnabled()) {
            return null;
        }
        return jiraServerCfg;
    }

    public void setDefaultJiraServerId(final ServerIdImpl defaultJiraServerId) {
        this.defaultJiraServerId = defaultJiraServerId;
    }

    public boolean isDefaultJiraServerValid() {
        if (getDefaultJiraServerId() == null) {
            return true;
        }

        final ServerCfg serverCfg = getServerCfg(getDefaultJiraServerId());

        // no additional check - let IDE handle such error in a standard way (error dialog)
        // in unlikely event of some fuck-up
        final JiraServerCfg jiraServerCfg = (JiraServerCfg) serverCfg;
        return jiraServerCfg != null && jiraServerCfg.isEnabled();
    }

    public Collection<JiraServerCfg> getAllJIRAServers() {
        final Collection<JiraServerCfg> jiraServers = MiscUtil.buildArrayList();

        for (final ServerCfg server : servers) {
            if (server.getServerType() == ServerType.JIRA_SERVER && server instanceof JiraServerCfg) {
                jiraServers.add((JiraServerCfg) server);
            }
        }

        return jiraServers;
    }

    public Collection<ServerCfg> getAllEnabledServersWithDefaultCredentials() {
        final Collection<ServerCfg> defServers = MiscUtil.buildArrayList();
        for (final ServerCfg server : servers) {
            if (server.isUseDefaultCredentials() && server.isEnabled()) {
                defServers.add(server);
            }
        }
        return defServers;
    }
}
