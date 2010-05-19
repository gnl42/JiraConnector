/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/connector/src/java/org/apache/webdav/connector/WebDAVXAResource.java,v 1.3 2004/07/27 15:27:23 luetzkendorf Exp $
 * $Revision: 1.3 $
 * $Date: 2004/07/27 15:27:23 $
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

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.commons.transaction.util.LoggerFacade;
import org.apache.commons.transaction.util.PrintWriterLogger;
import org.apache.commons.transaction.util.xa.AbstractTransactionalResource;
import org.apache.commons.transaction.util.xa.AbstractXAResource;
import org.apache.commons.transaction.util.xa.TransactionalResource;
import org.apache.webdav.lib.WebdavResource;

/**
 * 
 * @version $Revision: 1.3 $
 *  
 */
public class WebDAVXAResource extends AbstractXAResource {

    protected WebdavResource webdavResource;

    protected String owner;

    protected int timeout = 10;

    protected LoggerFacade loggerFacade;

    public WebDAVXAResource(WebdavResource webdavResource, String owner) {
        this.webdavResource = webdavResource;
        this.owner = owner;
        // log important stuff to standard out as long as nothing else is configured
        this.loggerFacade = new PrintWriterLogger(new PrintWriter(System.out), "WebDAVXAResource", false);
    }

    protected LoggerFacade getLoggerFacade() {
        return loggerFacade;
    }

    protected void setLoggerFacade(PrintWriter out) {
        loggerFacade = new PrintWriterLogger(out, "WebDAVXAResource", true);
    }

    public int getTransactionTimeout() throws XAException {
        return timeout;
    }

    public boolean setTransactionTimeout(int seconds) throws XAException {
        timeout = seconds;
        return true;
    }

    public boolean isSameRM(XAResource xares) throws XAException {
        return (xares != null && xares instanceof WebDAVXAResource && webdavResource
                .equals(((WebDAVXAResource) xares).webdavResource));
    }

    public Xid[] recover(int flag) throws XAException {
        // FIXME no idea how to recover anything here
        return null;
    }

    protected TransactionalResource createTransactionResource(Xid xid) throws Exception {
        return new TransactionalWebDAVResource(xid, webdavResource, owner, timeout, getLoggerFacade());
    }

    protected boolean includeBranchInXid() {
        return true;
    }

    protected static class TransactionalWebDAVResource extends AbstractTransactionalResource {

        WebdavResource webdavResource;
        LoggerFacade loggerFacade;
        
        public TransactionalWebDAVResource(Xid xid, WebdavResource webdavResource, String owner, int timeout, LoggerFacade loggerFacade) throws IOException {
            super(xid);
            this.webdavResource = webdavResource;
            System.out.println("Statring "+webdavResource);
            webdavResource.startTransaction(owner, timeout);
            this.loggerFacade = loggerFacade;  
        }

        public void commit() throws XAException {
            try {
                webdavResource.commitTransaction();
            } catch (IOException e) {
                loggerFacade.logWarning("Could not commit transaction", e);
                throw new XAException("Could not commit transaction");
            }
        }

        public void rollback() throws XAException {
            try {
                webdavResource.abortTransaction();
            } catch (IOException e) {
                loggerFacade.logWarning("Could not roll back transaction", e);
                throw new XAException("Could not roll back transaction");
            }
        }

        public int prepare() throws XAException {
            return XA_OK;
        }
        
        public void begin() throws XAException {
        }

        public void resume() throws XAException {
        }
        
        public void suspend() throws XAException {
        }
    }
}