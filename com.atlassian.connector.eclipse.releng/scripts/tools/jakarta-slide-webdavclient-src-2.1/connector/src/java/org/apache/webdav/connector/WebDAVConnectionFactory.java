/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/connector/src/java/org/apache/webdav/connector/WebDAVConnectionFactory.java,v 1.2 2004/07/15 12:37:36 ozeigermann Exp $
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

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;

/**
 *   
 * @version $Revision: 1.2 $
 * 
 */
public class WebDAVConnectionFactory implements ConnectionFactory {

    protected Reference reference;
    protected ConnectionManager cm;
    protected ManagedConnectionFactory mcf;

    public WebDAVConnectionFactory(ManagedConnectionFactory mcf, ConnectionManager cm) {
        System.out.println("MCF Init with mcf " + mcf + " cm " + cm);
        this.mcf = mcf;
        this.cm = cm;
    }

    public Connection getConnection() throws ResourceException {
        throw new NotSupportedException(
                "Need a WebDAVConnectionSpec to create a connection. Call getConnection(ConnectionSpec spec) instead!");
    }

    public Connection getConnection(ConnectionSpec spec) throws ResourceException {
        if (!(spec instanceof WebDAVConnectionSpec)) {
            throw new NotSupportedException("Need a WebDAVConnectionSpec to create a connection!");
        }
        System.out.println("Getting connection with spec "+spec);
        return (Connection) cm.allocateConnection(mcf, (WebDAVConnectionSpec)spec);
    }

    public RecordFactory getRecordFactory() throws ResourceException {
        return null;
    }

    public ResourceAdapterMetaData getMetaData() throws ResourceException {
        return null;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public Reference getReference() throws NamingException {
        return reference;
    }

}
