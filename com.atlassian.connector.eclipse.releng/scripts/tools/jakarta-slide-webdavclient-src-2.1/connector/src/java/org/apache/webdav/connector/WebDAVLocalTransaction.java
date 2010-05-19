/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/connector/src/java/org/apache/webdav/connector/WebDAVLocalTransaction.java,v 1.2 2004/07/15 12:37:36 ozeigermann Exp $
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

import java.io.IOException;

import javax.resource.ResourceException;
import org.apache.webdav.lib.WebdavResource;

/**
 * 
 * @version $Revision: 1.2 $
 * 
 */
public class WebDAVLocalTransaction implements javax.resource.spi.LocalTransaction, javax.resource.cci.LocalTransaction {

    protected WebdavResource webdavResource;
    protected String owner;
    protected int timeout;

    public WebDAVLocalTransaction(WebdavResource webdavResource, String owner, int timeout) {
        this.webdavResource = webdavResource;
        this.owner = owner;
        this.timeout = timeout;
    }

    public void begin() throws ResourceException {
        try {
            webdavResource.startTransaction(owner, timeout);
        } catch (IOException e) {
            throw new ResourceException("Could not start transaction", e);
        }
    }

    public void commit() throws ResourceException {
        try {
            if (!webdavResource.commitTransaction()) {
                throw new ResourceException("Could not commit transaction");
            }
        } catch (IOException e) {
            throw new ResourceException("Could not commit transaction", e);
        }
    }

    public void rollback() throws ResourceException {
        try {
            if (!webdavResource.abortTransaction()) {
                throw new ResourceException("Could not roll back transaction");
            }
        } catch (IOException e) {
            throw new ResourceException("Could not roll back transaction", e);
        }
    }

}
