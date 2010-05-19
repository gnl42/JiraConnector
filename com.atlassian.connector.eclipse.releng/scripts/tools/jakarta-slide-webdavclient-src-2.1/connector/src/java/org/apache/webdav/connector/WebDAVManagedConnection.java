/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/connector/src/java/org/apache/webdav/connector/WebDAVManagedConnection.java,v 1.2.2.1 2004/09/20 08:07:29 ozeigermann Exp $
 * $Revision: 1.2.2.1 $
 * $Date: 2004/09/20 08:07:29 $
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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.URIException;
import org.apache.webdav.lib.WebdavResource;

/**
 * 
 * @version $Revision: 1.2.2.1 $
 *  
 */
public class WebDAVManagedConnection implements ManagedConnection {

    protected WebDAVXAResource xares = null;

    protected WebDAVLocalTransaction tx = null;

    protected String name = null;

    protected WebdavResource webdavResource;

    protected WebDAVConnectionSpec webDAVConnectionSpec;

    protected WebDAVConnection connection = null;

    protected List listeners = new ArrayList();

    protected PrintWriter out;

    public WebDAVManagedConnection(ConnectionRequestInfo cxRequestInfo) throws HttpException, IOException {
        open((WebDAVConnectionSpec) cxRequestInfo);
    }

    public WebdavResource getWebdavResource() {
        return webdavResource;
    }

    public void close() {
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
        event.setConnectionHandle(connection);
        for (Iterator it = listeners.iterator(); it.hasNext();) {
            ((ConnectionEventListener) it.next()).connectionClosed(event);
        }
    }

    /**
     * @see ManagedConnection#getConnection(Subject, ConnectionRequestInfo)
     */
    public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {

        if (connection == null) {
            connection = new WebDAVConnection(this);
        }
        return connection;
    }

    /**
     * @see ManagedConnection#destroy()
     */
    public void destroy() throws ResourceException {

        if (connection != null) {
            connection.invalidate();
            connection = null;
        }

        listeners = null;
        name = null;
        xares = null;
        tx = null;
        try {
            webdavResource.close();
        } catch (IOException e) {
            throw new ResourceException(e);
        }
    }

    /**
     * @see ManagedConnection#cleanup()
     */
    public void cleanup() throws ResourceException {
        // XXX We should only reset internal state to put our
        // physical connection back to the pool. As I have
        // no idea how to recycle a WebdavResource a have to
        // fully destroy it (Olli Z.)

        if (connection != null) {
            connection.invalidate();
            connection = null;
        }

        name = null;
        xares = null;
        tx = null;
        try {
            webdavResource.close();
        } catch (IOException e) {
            throw new ResourceException(e);
        }
    }

    /**
     * @see ManagedConnection#associateConnection(Object)
     */
    public void associateConnection(Object connection) throws ResourceException {
        if (!(connection instanceof WebDAVConnection)) {
            throw new ResourceException("Connection is not of type WebDAVConnection");
        }

        this.connection = (WebDAVConnection) connection;
        try {
            open(this.connection.mc.webDAVConnectionSpec);
        } catch (URIException e) {
            throw new ResourceException("Could not associate connection", e);
        } catch (IOException e) {
            throw new ResourceException("Could not associate connection", e);
        }
        this.connection.mc = this;
    }

    /**
     * @see ManagedConnection#addConnectionEventListener(ConnectionEventListener)
     */
    public void addConnectionEventListener(ConnectionEventListener listener) {

        listeners.add(listener);
    }

    /**
     * @see ManagedConnection#removeConnectionEventListener(ConnectionEventListener)
     */
    public void removeConnectionEventListener(ConnectionEventListener listener) {

        listeners.remove(listener);
    }

    /**
     * @see ManagedConnection#getXAResource()
     */
    public XAResource getXAResource() throws ResourceException {
        return xares;
    }

    /**
     * @see ManagedConnection#getLocalTransaction()
     */
    public LocalTransaction getLocalTransaction() throws ResourceException {
        return tx;
    }

    /**
     * @see ManagedConnection#getMetaData()
     */
    public ManagedConnectionMetaData getMetaData() throws ResourceException {

        return null;
    }

    /**
     * @see ManagedConnection#setLogWriter(PrintWriter)
     */
    public void setLogWriter(PrintWriter out) throws ResourceException {
        this.out = out;
        xares.setLoggerFacade(out);
    }

    /**
     * @see ManagedConnection#getLogWriter()
     */
    public PrintWriter getLogWriter() throws ResourceException {

        return out;
    }

    protected void open(WebDAVConnectionSpec webDAVConnectionSpec) throws IOException, URIException {
        this.webDAVConnectionSpec = webDAVConnectionSpec;
        System.out.println("Opening: "+webDAVConnectionSpec.getHttpURL()); // FIXME
        webdavResource = new WebdavResource(webDAVConnectionSpec.getHttpURL());
        System.out.println("Opened"); // FIXME
        String owner = webDAVConnectionSpec.getHttpURL().getUser();
        if (owner == null)
            owner = "WebDAV Connector";

        tx = new WebDAVLocalTransaction(webdavResource, owner, webDAVConnectionSpec.getTimeout());
        xares = new WebDAVXAResource(webdavResource, owner);
    }

}