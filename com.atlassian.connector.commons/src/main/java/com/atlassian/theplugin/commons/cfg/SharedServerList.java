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

import java.util.*;

/**
 * @autrhor pmaruszak
 * @date Jun 11, 2010
 */
public class SharedServerList extends ArrayList<ServerCfg> {
	public static SharedServerList merge(SharedServerList currentConfig, SharedServerList loadedFromFile, Collection<ServerCfg> allServers) {
		LinkedList<ServerCfg> sharedList = new LinkedList<ServerCfg>();
		HashSet<String> storedIds = new HashSet<String>();
        HashSet<String> deletedIds = new HashSet<String>();
		SharedServerList newList = new SharedServerList();

        // it may happen that the user made a server no longer shared. In this case it must not go on the resulting shared server list
        Set<String> nonShared = new HashSet<String>();
        for (ServerCfg s : allServers) {
            if (!s.isShared()) {
                nonShared.add(s.getServerId().getId());
            }
        }

        //current config are priority cfg
        sharedList.addAll(currentConfig);
        sharedList.addAll(loadedFromFile);

		for (ServerCfg server : sharedList) {
            String uuid = server.getServerId().toString();
            if (server.getUrl() != null && server.getUrl().length() > 0	&& !storedIds.contains(uuid) && !deletedIds.contains(uuid)) {
                if (server.isDeleted()) {
                    deletedIds.add(uuid);
                } else if (!nonShared.contains(server.getServerId().getId())) {
                    newList.add(server);
                    storedIds.add(uuid);
                }
			}
		}
		return newList;
	}
}
