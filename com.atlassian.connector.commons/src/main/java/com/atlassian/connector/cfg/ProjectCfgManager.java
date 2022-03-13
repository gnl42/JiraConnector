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

package com.atlassian.connector.cfg;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.bamboo.BambooServerData;
import com.atlassian.theplugin.commons.cfg.ConfigurationListener;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface ProjectCfgManager {


	@Nullable
	JiraServerData getDefaultJiraServer();

	@Nullable
	ServerData getDefaultCrucibleServer();

	@Nullable
	ServerData getDefaultFishEyeServer();

	String getDefaultCrucibleRepo();

	String getDefaultCrucibleProject();

	String getDefaultFishEyeRepo();

	String getFishEyeProjectPath();

	/**
	 * Finds server with specified url in collection of servers (exact String match).
	 * It tries to find enabled server. If not found then tries to find disabled server.
	 * If the above failed then it tries to compare host, port and path (skips protocol and query string)
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

	Collection<ServerData> getAllFishEyeServerss();

	Collection<ServerData> getAllEnabledServerss(final ServerType serverType);

	Collection<ServerData> getAllServerss(final ServerType serverType);

	JiraServerData getJiraServerr(ServerId serverId);

	Collection<BambooServerData> getAllEnabledBambooServerss();

	ServerData getServerr(ServerId serverId);

	@Nullable
	ServerData getEnabledServerr(ServerId serverId);

	JiraServerData getEnabledJiraServerr(ServerId serverId);

	ServerData getCrucibleServerr(ServerId serverId);

	ServerData getEnabledCrucibleServerr(ServerId serverId);

	Collection<ServerData> getAllServerss();

	Collection<ServerData> getAllJiraServerss();

	Collection<ServerData> getAllCrucibleServerss();

	Collection<JiraServerData> getAllEnabledJiraServerss();

	Collection<ServerData> getAllEnabledCrucibleServerss();

	Collection<ServerData> getAllEnabledServerss();

	Collection<BambooServerData> getAllBambooServerss();

	@Deprecated
	Collection<ServerCfg> getAllEnabledServers(ServerType serverType);

    Collection<ServerData> getAllEnabledCrucibleServersContainingFisheye();

}
