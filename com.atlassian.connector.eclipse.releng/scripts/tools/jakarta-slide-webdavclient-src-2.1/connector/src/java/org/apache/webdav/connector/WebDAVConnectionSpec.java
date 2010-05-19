/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/connector/src/java/org/apache/webdav/connector/WebDAVConnectionSpec.java,v 1.3 2004/07/15 12:37:36 ozeigermann Exp $
 * $Revision: 1.3 $
 * $Date: 2004/07/15 12:37:36 $
 *
 * ====================================================================
 *
 * Copyright 2004 The Apache Software Foundation 
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

package org.apache.webdav.connector;

import javax.resource.cci.ConnectionSpec;
import javax.resource.spi.ConnectionRequestInfo;

import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.URIException;

/**
 * Specifies where the {@link WebDAVConnection} shall go to. 
 *  
 * @version $Revision: 1.3 $
 * 
 */
public class WebDAVConnectionSpec implements ConnectionSpec, ConnectionRequestInfo {
    
    /** The http URL on the client connection. */
    protected HttpURL httpURL;
    protected int timeout;
    
    /**
     * Creates a specification where the {@link WebDAVConnection} shall go to.
     * 
     * @param httpURL complete url of the Slide (WebDAV) server including user and password
     * @param timeout timeout of the externally controlled transaction 
     */
    public WebDAVConnectionSpec(HttpURL httpURL, int timeout) {
        this.httpURL = httpURL;
        this.timeout = timeout; 
    
    }

    /**
     * Creates a specification where the {@link WebDAVConnection} shall go to.
     * 
     * @param url url string of the Slide (WebDAV) server
     * @param userName user name for login to the Slide (WebDAV) server
     * @param password password for login to the Slide (WebDAV) server
     * @param timeout timeout of the externally controlled transaction 
     * @throws URIException if the given uri is not a valid one
     */
    public WebDAVConnectionSpec(String url, String userName, String password, int timeout) throws URIException {
        this.httpURL = url.startsWith("https") ? new HttpsURL(url) : new HttpURL(url);
        this.httpURL.setUserinfo(userName, password);
        this.timeout = timeout; 
    }

    protected HttpURL getHttpURL() {
        return httpURL;
    }

    protected int getTimeout() {
        return timeout;
    }
}
