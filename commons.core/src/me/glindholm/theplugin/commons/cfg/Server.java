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

import me.glindholm.theplugin.commons.ServerType;

public interface Server {

	ServerIdImpl getServerId();

	String getName();

	String getUrl();

//	void setPassword(final String password);

	boolean isEnabled();

	boolean isUseDefaultCredentials();

	String getUsername();

	String getPassword();

	ServerType getServerType();

    boolean isDontUseBasicAuth();

    UserCfg getBasicHttpUser();

    boolean isShared();

    void setShared(boolean global);

    boolean isUseSessionCookies();
}
