/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/WebdavSession.java,v 1.7 2004/07/30 13:20:48 ib Exp $
 * $Revision: 1.7 $
 * $Date: 2004/07/30 13:20:48 $
 *
 * ====================================================================
 *
 * Copyright 1999-2002 The Apache Software Foundation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.webdav.lib;

import java.io.IOException;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.HttpURL;

/**
 * This WebdavSession class is for the session management of WebDAV clients.
 * This class saves and restores the requested client.
 *
 * Although this class is thread safe, it should only be accessed by one
 * concurrent thread, since the underlying protocol, HTTP, is not multiplexed.
 * If simultaneous operations are needed, it is recommended to create
 * additional threads, each having its own associated WebDAV client.
 *
 * Clients that use persistent connections SHOULD limit the number of
 * simultaneous connections that they maintain to a given server. A
 * single-user client SHOULD NOT maintain more than 2 connections with
 * any server or proxy. A proxy SHOULD use up to 2*N connections to
 * another server or proxy, where N is the number of simultaneously
 * active users. These guidelines are intended to improve HTTP response
 * times and avoid congestion.
 *
 */
public abstract class WebdavSession {


    // -------------------------------------------------------  Constructors


    /**
     * Default constructor.
     */
    public WebdavSession() {
        super();
    }


    // ---------------------------------------------------- Instance Variables


    /**
     * The Http client instance.
     */
    protected HttpClient client;

    /**
     * Credentials to use for authentication
     */
    protected Credentials hostCredentials = null;

    /**
     * The hostname to use for the proxy, if any
     */
    protected String proxyHost = null;

    /**
     * Port number to use for proxy, if any
     */
    protected int proxyPort = -1;

    /**
     * Credentials to use for an authenticating proxy
     */
    protected Credentials proxyCredentials = null;


    /**
     * Debug level.
     */
    protected int debug = 0;


    // ------------------------------------------------------------- Properties


    /**
     * Set debug level.
     */
    public void setDebug(int debug) {
        this.debug = debug;
    }


    // ------------------------------------------------------  Public methods


    /**
     * Get a <code>HttpClient</code> instance.
     * This method returns a new client instance for the first time.
     * And it is saved util it's closed or reset.
     *
     * @param httpURL The http URL to connect.  only used the authority part.
     * @return An instance of <code>HttpClient</code>.
     * @exception IOException
     */
    public HttpClient getSessionInstance(HttpURL httpURL)
        throws IOException {

        return getSessionInstance(httpURL, false);
    }


    /**
     * Get a <code>HttpClient</code> instance.
     * This method returns a new client instance, when reset is true.
     *
     * @param httpURL The http URL to connect.  only used the authority part.
     * @param reset The reset flag to represent whether the saved information
     *              is used or not.
     * @return An instance of <code>HttpClient</code>.
     * @exception IOException
     */
    public HttpClient getSessionInstance(HttpURL httpURL, boolean reset)
        throws IOException {

        if (reset || client == null) {
            client = new HttpClient();
            // Set a state which allows lock tracking
            client.setState(new WebdavState());
            HostConfiguration hostConfig = client.getHostConfiguration();
            hostConfig.setHost(httpURL);
            if (proxyHost != null && proxyPort > 0)
                hostConfig.setProxy(proxyHost, proxyPort);

            if (hostCredentials == null) {
                String userName = httpURL.getUser();
                if (userName != null && userName.length() > 0) {
                    hostCredentials =
                        new UsernamePasswordCredentials(userName,
                                                        httpURL.getPassword());
                }
            }

            if (hostCredentials != null) {
                HttpState clientState = client.getState();
                clientState.setCredentials(null, httpURL.getHost(),
                                           hostCredentials);
                clientState.setAuthenticationPreemptive(true);
            }

            if (proxyCredentials != null) {
                client.getState().setProxyCredentials(null, proxyHost,
                                                      proxyCredentials);
            }
        }

        return client;
    }

    /**
     * Set credentials for authentication.
     *
     * @param credentials The credentials to use for authentication.
     */
    public void setCredentials(Credentials credentials) {
        hostCredentials = credentials;
    }

    /** Set proxy info, to use proxying.
     */
    public void setProxy(String host, int port)
    {
        this.proxyHost = host;
        this.proxyPort = port;
    }

    /**
     * Set credentials for authenticating against a proxy.
     *
     * @param credentials The credentials to use for authentication.
     */
    public void setProxyCredentials(Credentials credentials) {
        proxyCredentials = credentials;
    }

    /**
     * Close an session and delete the connection information.
     *
     * @exception IOException Error in closing socket.
     */
    public void closeSession()
        throws IOException {
        if (client != null) {
            client.getHttpConnectionManager().getConnection(
                client.getHostConfiguration()).close();
            client = null;
        }
    }


    /**
     * Close an session and delete the connection information.
     *
     * @param client The HttpClient instance.
     * @exception IOException Error in closing socket.
     * @deprecated Replaced by closeSession()
     */
    public synchronized void closeSession(HttpClient client)
        throws IOException {
        closeSession();
    }


    /**
     * Progressing by the progress event.
     *
     * @param pe The progress event.
     */
    /*
    public void progressing(ProgressEvent pe) {
        if (debug > 3)
            System.err.println("[EVENT/WebdavSession] " +
                               "action:" + pe.getAction() +
                               ((pe.getResourceName() == null) ? "" :
                               ", resource:" + pe.getResourceName()) +
                               ", soMany:" + pe.getSoMany() +
                               ", remained:" + pe.getRemainedSize() +
                               ", total:" + pe.getTotalSize());
        getProgressUtil().fireProgress(pe);
    }
    */


    /**
     * Get the utility of this progress event and listener.
     *
     * @return ProgressUtil
     */
    /*
    public ProgressUtil getProgressUtil() {
        return sessionProgress;
    }
    */
}
