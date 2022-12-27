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
package me.glindholm.theplugin.commons.bamboo;

import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholm.theplugin.commons.bamboo.api.BambooSession;
import me.glindholm.theplugin.commons.remoteapi.ProductSession;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import me.glindholm.theplugin.commons.remoteapi.rest.HttpSessionCallback;

public interface BambooSessionFactory {
	BambooSession createSession(ConnectionCfg serverData, HttpSessionCallback callback) throws RemoteApiException;

	ProductSession createLoginSession(final ConnectionCfg serverData, final HttpSessionCallback callback)
			throws RemoteApiMalformedUrlException;
}
