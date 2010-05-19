/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/WebdavState.java,v 1.4 2004/07/28 09:31:38 ib Exp $
 * $Revision: 1.4 $
 * $Date: 2004/07/28 09:31:38 $
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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import org.apache.commons.httpclient.HttpState;

/**
 * Session state.
 *
 */
public class WebdavState extends HttpState {


    // -------------------------------------------------------------- Constants


    private static final String[] EMPTY_ARRAY = new String[0];


    // ----------------------------------------------------- Instance Variables


    /**
     * Lock tokens.
     */
    protected HashMap locks = new HashMap();


    /**
     * Lock list.
     */
    protected ArrayList lockTokens = new ArrayList();

    /**
     * Transaction handle of current session of <code>null</code> if not inside of transaction.
     */
    protected String transactionHandle = null;

    // ------------------------------------------------------------- Properties


    /**
     * Add a lock token.
     *
     * @param uri Uri
     * @param value Lock token value
     */
    public void addLock(String uri, String value) {

        if (value == null)
            return;

        if (lockTokens.contains(value))
            return;

        locks.put(uri, value);
        lockTokens.add(value);

    }


    /**
     * Remove a lock.
     *
     * @param uri Uri
     * @param value LockToken value
     */
    public void removeLock(String uri, String value) {

        locks.remove(uri);
        int i = lockTokens.indexOf(value);
        if (i != -1)
            lockTokens.remove(i);

    }


    /**
     * Remove locks.
     *
     * @param uri Uri
     */
    public void removeLocks(String uri) {

        String result = (String) locks.remove(uri);
        if (result != null) {
            int i = lockTokens.indexOf(result);
            if (i != -1)
                lockTokens.remove(i);
        }

    }


    /**
     * Get lock
     *
     * @param uri Uri
     */
    public String getLock(String uri) {

        return (String) locks.get(uri);

    }


    /**
     * Get locks
     *
     * @param uri Uri
     * @return Enumeration of lock tokens
     * @deprecated
     */
    public Enumeration getLocks(String uri) {

        Vector result = new Vector();
        String lockToken = getLock(uri);
        if (lockToken != null)
            result.addElement(lockToken);
        return result.elements();

    }


    /**
     * Get all locks scoped to that uri.
     *
     * @param uri Uri
     * @return Iterator of lock tokens
     */
    public String[] getAllLocks(String uri) {

        return (String[]) lockTokens.toArray(EMPTY_ARRAY);

    }

    public String getTransactionHandle() {
        return transactionHandle;
    }

    public void setTransactionHandle(String transactionHandle) {
        this.transactionHandle = transactionHandle;
    }
}
