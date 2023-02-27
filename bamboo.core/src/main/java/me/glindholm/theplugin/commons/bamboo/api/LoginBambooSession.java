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
package me.glindholm.theplugin.commons.bamboo.api;

import org.apache.commons.httpclient.HttpMethod;
import org.jdom2.Document;
import org.jdom2.JDOMException;

import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholm.theplugin.commons.remoteapi.ProductSession;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiLoginException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiSessionExpiredException;
import me.glindholm.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import me.glindholm.theplugin.commons.remoteapi.rest.HttpSessionCallback;

/**
 * @author Jacek Jaroczynski
 */
public class LoginBambooSession extends AbstractHttpSession implements ProductSession {

    public LoginBambooSession(final ConnectionCfg serverData, final HttpSessionCallback callback) throws RemoteApiMalformedUrlException {
        super(serverData, callback);
    }

    /**
     * Connects to Bamboo server instance. On successful login authentication token
     * is returned from server and stored in Bamboo session for subsequent calls.
     * <p/>
     * The exception returned may have the getCause() examined for to get the actual
     * exception reason.<br>
     * If the exception is caused by a valid error response from the server (no
     * IOEXception, UnknownHostException, MalformedURLException or JDOMException),
     * the
     * {@link me.glindholm.theplugin.commons.remoteapi.RemoteApiLoginFailedException}
     * is actually thrown. This may be used as a hint that the password is invalid.
     *
     * @param name      username defined on Bamboo server instance
     * @param aPassword for username
     * @throws me.glindholm.theplugin.commons.remoteapi.RemoteApiLoginException on
     *                                                                          connection
     *                                                                          or
     *                                                                          authentication
     *                                                                          errors
     */
    @Override
    @Deprecated
    public void login(final String name, final char[] aPassword) throws RemoteApiLoginException {
        throw new RemoteApiLoginException("Removed");
    }

    @Override
    @Deprecated
    public void logout() {
    }

    @Override
    public boolean isLoggedIn() {
        return true;
    }

    @Override
    protected void preprocessResult(final Document doc) throws JDOMException, RemoteApiSessionExpiredException {
        throw new RemoteApiSessionExpiredException("Removed");
    }

    @Override
    protected void preprocessMethodResult(final HttpMethod method) {
    }

    protected static String getExceptionMessages(final Document doc) throws JDOMException {
        throw new JDOMException("Removed");
    }

    @Override
    protected void adjustHttpHeader(final HttpMethod method) {
        // do not do it here it's forbidden and breakes ACE
        // method.addRequestHeader(new Header("Authorization", getAuthHeaderValue()));
    }

//	private String getAuthHeaderValue() {
//		return "Basic " + StringUtil.encode(getUsername() + ":" + getPassword());
//	}
}
