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
package me.glindholm.theplugin.commons.cfg.xstream;

import java.util.Collection;

import me.glindholm.theplugin.commons.cfg.ServerCfg;
import me.glindholm.theplugin.commons.cfg.ServerCfgFactoryException;
import me.glindholm.theplugin.commons.cfg.SharedServerList;

/**
 * @autrhor pmaruszak
 * @date Jun 10, 2010
 */
public interface UserSharedConfigurationDao {
    void save(SharedServerList serversInfo, Collection<ServerCfg> allServers) throws ServerCfgFactoryException;
    SharedServerList load() throws ServerCfgFactoryException;
}
