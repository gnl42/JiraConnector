/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/connector/src/java/org/apache/webdav/connector/WebDAVConnection.java,v 1.2 2004/07/15 12:37:36 ozeigermann Exp $
 * $Revision: 1.2 $
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

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.Interaction;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.ResultSetInfo;
import javax.resource.spi.ManagedConnection;

import org.apache.webdav.lib.WebdavResource;

/**
 * Abstraction of a connection to a Slide (WebDAV) server. All actual requests are made via the
 * {@link WebdavResource} the user gets with {@link #getWebdavResource()}.
 * 
 * @version $Revision: 1.2 $
 * 
 */
public class WebDAVConnection implements Connection {

    protected WebDAVManagedConnection mc;
    
    public WebDAVConnection(ManagedConnection mc) {
        this.mc = (WebDAVManagedConnection) mc;
    }

    /**
     * Returns the {@link WebdavResource} that has been associated to this WebDAV connection. All actual requests
     * to the WebDAV server are done with this{@link WebdavResource}.  
     * 
     * @return the {@link WebdavResource} associated to this connection 
     */
    public WebdavResource getWebdavResource() {
        return mc.getWebdavResource();
    }
    
    public void close() throws ResourceException {
        mc.close();
    }

    public Interaction createInteraction() throws ResourceException {
        return null;
    }

    public LocalTransaction getLocalTransaction() throws ResourceException {
        return (LocalTransaction)mc.getLocalTransaction();
    }

    public ConnectionMetaData getMetaData() throws ResourceException {
        return null;
    }

    public ResultSetInfo getResultSetInfo() throws ResourceException {
        return null;
    }

    void invalidate() {
        mc = null;
    }

}
