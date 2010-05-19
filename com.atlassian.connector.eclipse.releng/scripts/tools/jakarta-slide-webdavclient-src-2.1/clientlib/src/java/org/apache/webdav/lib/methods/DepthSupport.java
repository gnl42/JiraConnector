/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/DepthSupport.java,v 1.3 2004/07/28 09:30:47 ib Exp $
 * $Revision: 1.3 $
 * $Date: 2004/07/28 09:30:47 $
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

/**
 * Methods that can act on collections (for example, DELETE, LOCK, PROPFIND,
 * etc.) support a depth header.  The depth header indicates that the
 * method applies to either:
 * <ol>
 * <li>the collection (depth 0);
 * <li>the collection and its immediate contents (depth 1); or
 * <li>the collection, its contents and all subcollections (depth infinity).
 * </ol>
 *
 */
public interface DepthSupport {


    // -------------------------------------------------------------- Constants


    /**
     * Request with depth 0.
     */
    public static final int DEPTH_0 = 0;


    /**
     * Request with depth 1.
     */
    public static final int DEPTH_1 = 1;


    /**
     * Request with depth infinity.
     */
    public static final int DEPTH_INFINITY = Integer.MAX_VALUE;


    // ------------------------------------------------------------- Properties


    /**
     * Depth setter.
     *
     * @param depth New depth value
     */
    public void setDepth(int depth);


    /**
     * Depth getter.
     *
     * @return int depth value
     */
    public int getDepth();
}
