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
package me.glindholm.connector.commons.jira.soap;

import org.apache.axis.client.Call;
import org.apache.axis.client.Stub;

import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholm.theplugin.commons.cfg.UserCfg;
import me.glindholm.theplugin.commons.remoteapi.ServerData;

import java.rmi.Remote;

/**
 * @autrhor pmaruszak
 * @date Mar 23, 2010
 */
public class AxisSessionCallbackImpl implements AxisSessionCallback {
    public void configureRemoteService(Remote remote, ConnectionCfg connectionCfg) {
               if (connectionCfg instanceof ServerData && ((ServerData) connectionCfg).isUseBasicUser()) {
            UserCfg basicUser = ((ServerData) connectionCfg).getBasicUser();
            if (basicUser != null) {
                ((Stub) remote)._setProperty(Call.USERNAME_PROPERTY, basicUser.getUsername());
                ((Stub) remote)._setProperty(Call.PASSWORD_PROPERTY, basicUser.getPassword());
            }
        }
    }
}
