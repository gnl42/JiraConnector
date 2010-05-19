/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/connector/src/java/org/apache/webdav/connector/WebDAVManagedConnectionFactory.java,v 1.4 2004/07/15 12:37:36 ozeigermann Exp $
 * $Revision: 1.4 $
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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

import org.apache.commons.httpclient.HttpException;

/**
 * 
 * @version $Revision: 1.4 $
 *  
 */
public class WebDAVManagedConnectionFactory implements ManagedConnectionFactory {

    protected PrintWriter writer;

    /**
     * @see ManagedConnectionFactory#createConnectionFactory(ConnectionManager)
     */
    public Object createConnectionFactory(ConnectionManager cm) throws ResourceException {

        return new WebDAVConnectionFactory(this, cm);
    }

    /**
     * @see ManagedConnectionFactory#createConnectionFactory()
     */
    public Object createConnectionFactory() throws ResourceException {

        return new WebDAVConnectionFactory(this, null);
    }

    /**
     * @see ManagedConnectionFactory#createManagedConnection(Subject,
     *      ConnectionRequestInfo)
     */
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo)
            throws ResourceException {

        try {
            return new WebDAVManagedConnection(cxRequestInfo);
        } catch (HttpException e) {
            if (writer != null) {
                writer.println("Exception: " + e);
                e.printStackTrace(writer);
            }
            // XXX only in 1.4
//            throw new ResourceException("Could not create managed connection", e);
            throw new ResourceException("Could not create managed connection", e.toString());
        } catch (IOException e) {
            if (writer != null) {
                writer.println("Exception: " + e);
                e.printStackTrace(writer);
            }
            // XXX only in 1.4
//          throw new ResourceException("Could not create managed connection", e);
            throw new ResourceException("Could not create managed connection", e.toString());
        }
    }

    /**
     * @see ManagedConnectionFactory#matchManagedConnections(Set, Subject,
     *      ConnectionRequestInfo)
     */
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject,
            ConnectionRequestInfo cxRequestInfo) throws ResourceException {

        ManagedConnection match = null;
        Iterator iterator = connectionSet.iterator();
        if (iterator.hasNext()) {
            match = (ManagedConnection) iterator.next();
        }

        return match;
    }

    /**
     * @see ManagedConnectionFactory#setLogWriter(PrintWriter)
     */
    public void setLogWriter(PrintWriter writer) throws ResourceException {

        this.writer = writer;
    }

    /**
     * @see ManagedConnectionFactory#getLogWriter()
     */
    public PrintWriter getLogWriter() throws ResourceException {

        return writer;
    }

    public boolean equals(Object other) {

        if (other instanceof WebDAVManagedConnectionFactory) {
            return true;
        }
        return false;
    }

    public int hashCode() {

        return 0;
    }
}