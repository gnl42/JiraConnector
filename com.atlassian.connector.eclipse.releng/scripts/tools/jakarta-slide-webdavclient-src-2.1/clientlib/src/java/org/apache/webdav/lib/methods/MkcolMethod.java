/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/MkcolMethod.java,v 1.3 2004/07/28 09:30:40 ib Exp $
 * $Revision: 1.3 $
 * $Date: 2004/07/28 09:30:40 $
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
import java.io.InputStream;
import org.apache.commons.httpclient.HttpMethodBase;

/**
 * The MKCOL method is used to create a new collection. All DAV compliant
 * resources must support the MKCOL method.  Collections are merely
 * the HTTP name for structures like directories or folders (and, in
 * fact, often map directly to a folder or directory on the web server.
 *
 * <p>   This implementation of a MKCOL client method does not support a
 * a request body, and the newly created web collection should therefore
 * have no members.
 *
 * <p>   MKCOL creates a new collection resource at the location specified by
 * the Request-URI. If the resource identified by the Request-URI already
 * exists on the server then the MKCOL will fail.  During MKCOL processing,
 * a server will make the Request-URI a member of the URI's parent collection
 * (unless the Request-URI is "/").  If no parent collection exists, the method
 * will fail.  Thus, for example, if a request to create collection
 * <code>/a/b/c/d/</code> is made, and neither <code>/a/b/</code> nor
 * <code>/a/b/c/</code> exists, the request will fail.
 *
 * <p>   MKCOL is not idempotent (that is to say, each MKCOL request should
 * be handled by the web server, and the results of a MKCOL request should
 * not be cached).
 *
 * <h3>Example Request</h3>
 * <pre>
 * MKCOL /webdisc/xfiles/ HTTP/1.1
 * Host: www.server.org
 * </pre>
 *
 * <h3>Example Response</h3>
 * <pre>
 * HTTP/1.1 201 Created
 * </pre>
 *
 */
public class MkcolMethod
    extends HttpMethodBase {


    // ----------------------------------------------------------- Constructors


    /**
     * Method constructor.
     */
    public MkcolMethod() {
    }


    /**
     * Method constructor.
     */
    public MkcolMethod(String path) {
        super(path);
    }


    // --------------------------------------------------- WebdavMethod Methods


    /**
     * Parse the response body.  The MKCOL method does not receive a response
     * body.
     *
     * @param is Input stream
     */
    public void parseResponse(InputStream is)
        throws IOException {
    }

    public String getName() {
        return "MKCOL";
    }


}
