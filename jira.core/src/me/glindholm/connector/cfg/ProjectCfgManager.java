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

package me.glindholm.connector.cfg;

import java.util.Collection;

import org.eclipse.jdt.annotation.Nullable;

import me.glindholm.theplugin.commons.ServerType;
import me.glindholm.theplugin.commons.cfg.ConfigurationListener;
import me.glindholm.theplugin.commons.cfg.ServerCfg;
import me.glindholm.theplugin.commons.cfg.ServerId;
import me.glindholm.theplugin.commons.jira.JiraServerData;
import me.glindholm.theplugin.commons.remoteapi.ServerData;

public interface ProjectCfgManager {

    @Nullable
    JiraServerData getDefaultJiraServer();

    /**
     * Finds server with specified url in collection of servers (exact String match). It tries to find
     * enabled server. If not found then tries to find disabled server. If the above failed then it
     * tries to compare host, port and path (skips protocol and query string)
     *
     * @param serverUrl url of server
     * @param servers   collection of servers
     * @return ServerData or null if not found
     */
    ServerData findServer(final String serverUrl, final Collection<ServerData> servers);

    @Deprecated
    ServerCfg getServer(ServerId serverId);

    void addProjectConfigurationListener(ConfigurationListener configurationListener);

    boolean removeProjectConfigurationListener(ConfigurationListener configurationListener);

    boolean isDefaultJiraServerValid();

    Collection<ServerData> getAllEnabledServerss(final ServerType serverType);

    Collection<ServerData> getAllServerss(final ServerType serverType);

    JiraServerData getJiraServerr(ServerId serverId);

    ServerData getServerr(ServerId serverId);

    @Nullable
    ServerData getEnabledServerr(ServerId serverId);

    JiraServerData getEnabledJiraServerr(ServerId serverId);

    Collection<ServerData> getAllServerss();

    Collection<ServerData> getAllJiraServerss();

    Collection<JiraServerData> getAllEnabledJiraServerss();

    Collection<ServerData> getAllEnabledServerss();

    @Deprecated
    Collection<ServerCfg> getAllEnabledServers(ServerType serverType);

}
