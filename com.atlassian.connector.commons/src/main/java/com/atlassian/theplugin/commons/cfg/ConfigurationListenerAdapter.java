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

/**
 * @author Jacek Jaroczynski
 */
public abstract class ConfigurationListenerAdapter implements ConfigurationListener {

	public void configurationUpdated(ProjectConfiguration aProjectConfiguration) {
	}

	public void projectUnregistered() {
	}

	public void serverDataChanged(ServerData serverData) {
	}

	public void serverConnectionDataChanged(ServerId serverId) {
	}

	public void serverNameChanged(ServerId serverId) {
	}

//	public void serverAdded(ServerCfg newServer) {
//	}

	public void serverAdded(final ServerData serverData) {
	}

//	public void serverRemoved(ServerCfg oldServer) {
//	}

	public void serverRemoved(final ServerData serverData) {
	}

	public void serverEnabled(ServerData serverData) {
	}

	public void serverDisabled(ServerId serverId) {
	}

	public void jiraServersChanged(ProjectConfiguration newConfiguration) {
	}

	public void bambooServersChanged(ProjectConfiguration newConfiguration) {
	}

	public void crucibleServersChanged(ProjectConfiguration newConfiguration) {
	}

	public void fisheyeServersChanged(ProjectConfiguration newConfiguration) {
	}
}
