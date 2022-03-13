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

import com.atlassian.theplugin.commons.remoteapi.ServerData;

public interface ConfigurationListener {
	void configurationUpdated(final ProjectConfiguration aProjectConfiguration);

	void projectUnregistered();

	/**
	 * Called in case connections data (url, username, password) has changed
	 *
	 * @param serverId id of modified server
	 */
	void serverConnectionDataChanged(ServerId serverId);

	/**
	 * Called in case server name (label) has changed
	 *
	 * @param serverId id of modified server
	 */
	void serverNameChanged(ServerId serverId);

	/**
	 * Called in case new server has been added.
	 * It notifies also about DISABLED servers.
	 *
	 * @param newServer added server
	 */
//	void serverAdded(ServerCfg newServer);

	/**
	 * Called in case new server has been added.
	 * It notifies also about DISABLED servers.
	 *
	 * @param serverData added server
	 */
	void serverAdded(final ServerData serverData);

	/**
	 * Called in case server has been removed from configuration.
	 * It notifies also about DISABLED servers.
	 *
	 * @param oldServer removed server
	 */
//	void serverRemoved(ServerCfg oldServer);

	/**
	 * Called in case server has been removed from configuration.
	 * It notifies also about DISABLED servers.
	 *
	 * @param serverData removed server
	 */
	void serverRemoved(final ServerData serverData);

	/**
	 * Called in case server has been enabled
	 *
	 * @param serverData enabled server
	 */
	void serverEnabled(ServerData serverData);

	/**
	 * Called in case server has been disabled
	 *
	 * @param serverId id of disabled server
	 */
	void serverDisabled(ServerId serverId);

	/**
	 * Called in case server has been changed
	 * ServerCfg.equals is used to determine the change
	 *
	 * @param serverData changed server
	 */
	void serverDataChanged(ServerData serverData);

	/**
	 * Called in case something in the JIRA servers list has been changed
	 *
	 * @param newConfiguration fresh configuration
	 */
	void jiraServersChanged(ProjectConfiguration newConfiguration);

	/**
	 * Called in case something in the Bamboo servers list has been changed
	 *
	 * @param newConfiguration fresh configuration
	 */
	void bambooServersChanged(ProjectConfiguration newConfiguration);

	/**
	 * Called in case something in the Crucible servers list has been changed
	 *
	 * @param newConfiguration fresh configuration
	 */
	void crucibleServersChanged(ProjectConfiguration newConfiguration);

	/**
	 * Called in case something in the Fisheye servers list has been changed
	 *
	 * @param newConfiguration fresh configuration
	 */
	void fisheyeServersChanged(ProjectConfiguration newConfiguration);
}
