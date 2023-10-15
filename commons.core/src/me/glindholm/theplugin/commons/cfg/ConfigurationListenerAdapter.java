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

import me.glindholm.theplugin.commons.remoteapi.ServerData;

/**
 * @author Jacek Jaroczynski
 */
public abstract class ConfigurationListenerAdapter implements ConfigurationListener {

    @Override
    public void configurationUpdated(final ProjectConfiguration aProjectConfiguration) {
    }

    @Override
    public void projectUnregistered() {
    }

    @Override
    public void serverDataChanged(final ServerData serverData) {
    }

    @Override
    public void serverConnectionDataChanged(final ServerId serverId) {
    }

    @Override
    public void serverNameChanged(final ServerId serverId) {
    }

//	public void serverAdded(ServerCfg newServer) {
//	}

    @Override
    public void serverAdded(final ServerData serverData) {
    }

//	public void serverRemoved(ServerCfg oldServer) {
//	}

    @Override
    public void serverRemoved(final ServerData serverData) {
    }

    @Override
    public void serverEnabled(final ServerData serverData) {
    }

    @Override
    public void serverDisabled(final ServerId serverId) {
    }

    @Override
    public void jiraServersChanged(final ProjectConfiguration newConfiguration) {
    }

}
