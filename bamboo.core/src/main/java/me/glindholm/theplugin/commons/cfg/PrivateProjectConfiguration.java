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

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import me.glindholm.theplugin.commons.util.MiscUtil;

public class PrivateProjectConfiguration {
    public Collection<PrivateServerCfgInfo> getPrivateServerCfgInfos() {
        return privateServerCfgInfos;
    }

    private final Set<PrivateServerCfgInfo> privateServerCfgInfos = MiscUtil.buildHashSet();

    public void add(@NonNull final PrivateServerCfgInfo info) {
        privateServerCfgInfos.add(info);
    }

    public PrivateServerCfgInfo getPrivateServerCfgInfo(final ServerId serverId) {
        for (final PrivateServerCfgInfo privateServerCfgInfo : privateServerCfgInfos) {
            if (privateServerCfgInfo.getServerId().equals(serverId)) {
                return privateServerCfgInfo;
            }
        }
        return null;
    }
}
