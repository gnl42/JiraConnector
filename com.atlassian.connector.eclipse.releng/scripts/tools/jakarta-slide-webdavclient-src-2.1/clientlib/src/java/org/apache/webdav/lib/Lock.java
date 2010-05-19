/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/Lock.java,v 1.3.2.1 2004/10/11 08:17:19 luetzkendorf Exp $
 * $Revision: 1.3.2.1 $
 * $Date: 2004/10/11 08:17:19 $
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

import org.apache.webdav.lib.methods.DepthSupport;

/**
 * This class represents a lock on a resource.
 *
 * @version $Revision: 1.3.2.1 $
 */
public class Lock {


    // -------------------------------------------------------------- Constants


    /**
     * The property name.
     */
    public static final String TAG_NAME = "activelock";


    /**
     * The write constant in the locktype.
     */
    public static final int TYPE_WRITE = 0;
    
    /**
     * Type indicating lock is a transaction lock.
     */
    public static final int TYPE_TRANSACTION = 1;


    /**
     * The exclusive constant in the lockscope.
     */
    public static final int SCOPE_EXCLUSIVE = 0;


    /**
     * The shared constant in the lockscope.
     */
    public static final int SCOPE_SHARED = 1;


    // ----------------------------------------------------------- Constructors


    /**
     * Default constructor for the lockentry.
     */
    public Lock(int lockScope, int lockType) {
        this.lockScope = lockScope;
        this.lockType = lockType;
    }


    /**
     * Default constructor for the activelock.
     */
    public Lock(int lockScope, int lockType, int depth, String owner,
                int timeout, String lockToken) {
        this.lockScope = lockScope;
        this.lockType = lockType;
        this.depth = depth;
        this.owner = owner;
        this.timeout = timeout;
        this.lockToken = lockToken;
    }

    public Lock(int lockScope, int lockType, int depth, String owner,
            int timeout, String lockToken, String principalUrl) {
	    this.lockScope = lockScope;
	    this.lockType = lockType;
	    this.depth = depth;
	    this.owner = owner;
	    this.timeout = timeout;
	    this.lockToken = lockToken;
	    this.principalUrl = principalUrl;
	}

    /**
     * Default constructor for the activelock.
     * @deprecated The timeout value MUST NOT be greater than 2^32-1.
     */
    public Lock(int lockScope, int lockType, int depth, String owner,
                long timeout, String lockToken) {
        this(lockScope, lockType, depth, owner, (int) timeout, lockToken);
    }


    // ------------------------------------------------------ Instance Variable


    protected int lockScope = -1;


    protected int lockType = -1;


    protected int depth = -1;


    protected String owner = null;


    protected int timeout = -1;


    protected String lockToken = null;
    
    protected String principalUrl = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Get whether a lock is an exclusive lock, or a shared lock.
     *
     * @return The lock scope.  If it's not set, it could be -1.
     */
    public int getLockScope() {
        return lockScope;
    }


    /**
     * Get the access type of a lock.
     *
     * @return The lock type. If it's not set, it could be -1.
     */
    public int getLockType() {
        return lockType;
    }


    /**
     * Get the value of the depth.
     *
     * @return The depth vlaue. If it's not set, it could be -1.
     */
    public int getDepth() {
        return depth;
    }


    /**
     * Get information about the principal taking out a lock.
     *
     * @return The owner.
     */
    public String getOwner() {
        return owner;
    }
    
    /**
     * Get the <code>principal-URL</code> property of the lock, if one.
     * @return an URL as String
     */
    public String getPrincipalUrl() {
        return principalUrl;
    }


    /**
     * Get the timeout associated with a lock.
     *
     * @return The timeout vlaue. If it's not set, it could be -1.
     */
    public int getTimeout() {
        return timeout;
    }


    /**
     * Get the access type of a lock.
     *
     * @return The lock token.
     */
    public String getLockToken() {
        return lockToken;
    }

    public String toString() {
        StringBuffer tmp=new StringBuffer();

        if (lockScope==Lock.SCOPE_EXCLUSIVE) {
            tmp.append("Exclusive");
        }
        else if (lockScope==Lock.SCOPE_SHARED) {
            tmp.append("Shared");
        }

        if (lockType==Lock.TYPE_WRITE) {
            tmp.append(" write lock");
        }

        if (depth==DepthSupport.DEPTH_INFINITY) {
            tmp.append(" depth:infinity");
        }
        else if (depth==-1) {
            // unknown
        }
        else {
            tmp.append(" depth:" + depth);
        }

        if (owner!=null)
            tmp.append(" owner:" + owner);

        if (timeout!=-1)
            tmp.append(" timeout:" + timeout);

        if (lockToken!=null)
            tmp.append(" token:" + lockToken);

        return tmp.toString();
    }

}
