/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/UnlockMethod.java,v 1.6 2004/07/28 09:30:37 ib Exp $
 * $Revision: 1.6 $
 * $Date: 2004/07/28 09:30:37 $
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

package org.apache.webdav.lib.methods;

import java.io.IOException;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;

import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.webdav.lib.WebdavState;

/**
 * UNLOCK Method.
 *
 */
public class UnlockMethod
    extends XMLResponseMethodBase {

    public final static int NO_TRANSACTION = -1;
    public final static int ABORT_TRANSACTION = 0;
    public final static int COMMIT_TRANSACTION = 1;
    
    // ----------------------------------------------------- Instance Variables


    private String lockToken = null;

    private int transactionStatus = NO_TRANSACTION;

    // ----------------------------------------------------------- Constructors

    /**
     * Creates an unlock method that <em>ends a transaction</em> when server supports
     * them in a 
     * <a href="http://msdn.microsoft.com/library/default.asp?url=/library/en-us/wss/wss/_webdav_lock.asp">MS like style</a>.
     * The transacion handle of transaction is stored as the lock token.   
     * <br><br>
     * To start a transaction
     * use {@link LockMethod}. 

     * @param path any path inside Slide's scope
     * @param txHandle lock token specifying transaction handle
     * @param transactionStatus status of transaction as described in {@link #setTransactionStatus(int)}
     * 
     */
    public UnlockMethod(String path, String txHandle, int transactionStatus) {
        this(path);
        setLockToken(txHandle);
        setTransactionStatus(transactionStatus);
    }
    
    /**
     * Method constructor.
     */
    public UnlockMethod() {
    }


    /**
     * Method constructor.
     */
    public UnlockMethod(String path) {
        super(path);
    }


    /**
     * Method constructor.
     */
    public UnlockMethod(String path, String lockToken) {
        this(path);
        setLockToken(lockToken);
    }


    // ------------------------------------------------------------- Properties


    public void setLockToken(String lockToken) {
        checkNotUsed();
        this.lockToken = lockToken;
    }

    /**
     * Gets the parameter described in {@link #setTransactionStatus(int)}.
     * 
     * @return either {@link UnlockMethod#COMMIT_TRANSACTION} or {@link UnlockMethod#ABORT_TRANSACTION} as the real
     * transaction status or {@link UnlockMethod#NO_TRANSACTION} to indicate this method is not used for
     * transaction control 
     */
    public int getTransactionStatus() {
        return transactionStatus;
    }

    /**
     * Sets the transaction status of this method when it is used to end a externally controlled
     * transaction.
     * 
     * @param transactionStatus {@link UnlockMethod#COMMIT_TRANSACTION} to set the status to successful commit or
     * {@link UnlockMethod#ABORT_TRANSACTION} to let the transaction abort discarding all changes associated to it. 
     * 
     */
    public void setTransactionStatus(int transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    // --------------------------------------------------- WebdavMethod Methods

    public String getName() {
        return "UNLOCK";
    }

    public void recycle() {
        this.transactionStatus = NO_TRANSACTION;
    }

    /**
     * Set header, handling the special case of the lock-token header so
     * that it calls {@link #setLockToken} instead.
     *
     * @param headerName Header name
     * @param headerValue Header value
     */
    public void setRequestHeader(String headerName, String headerValue) {
        if (headerName.equalsIgnoreCase("Lock-Token")){
            setLockToken(headerValue);
        }
        else{
            super.setRequestHeader(headerName, headerValue);
        }
    }

    /**
     * Generate additional headers needed by the request.
     *
     * @param state HttpState token
     * @param conn The connection being used to send the request.
     */
    public void addRequestHeaders(HttpState state, HttpConnection conn)
    throws IOException, HttpException {

        super.addRequestHeaders(state, conn);

        super.setRequestHeader("Lock-Token", "<" + lockToken + ">");

    }

    protected String generateRequestBody() {
        if (getTransactionStatus() == NO_TRANSACTION) {
            return "";
        } else {
            return "<D:transactioninfo xmlns:D='DAV:'>\n  <D:transactionstatus>"
                    + (getTransactionStatus() == ABORT_TRANSACTION ? "<D:abort/>" : "<D:commit/>")
                    + "</D:transactionstatus>\n</D:transactioninfo>";
        }
    }
    
    protected void processResponseBody(HttpState state, HttpConnection conn) {
        if ((getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) &&
            (state instanceof WebdavState)) {
            ((WebdavState) state).removeLock(getPath(), lockToken);
        }
    }
}
