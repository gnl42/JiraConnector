/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/DeleteMethod.java,v 1.4 2004/08/02 15:45:48 unico Exp $
 * $Revision: 1.4 $
 * $Date: 2004/08/02 15:45:48 $
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
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.webdav.lib.util.WebdavStatus;

/**
 * DELETE Method.  The delete method can be sent to either a collection or
 * non-collection resource.  If a delete is sent to a collection, then all
 * members of that collection are deleted.
 *
 * <p>   Deletes can fail because of permission problems or if a resource is
 * currently locked.
 *
 * <p>   A typical request/response pair might look like this:
 *
 * <h3>Request</h3>
 * <pre>
 * DELETE /container/ HTTP/1.1
 * Host: www.foo.bar
 * </pre>
 *
 * <h3>Response</h3>
 * <pre>
 * HTTP/1.1 207 Multi-Status
 * Content-Type: text/xml; charset="utf-8"
 * Content-Length: xxxx
 * &lt;?xml version="1.0" encoding="utf-8" ?&gt;
 *   &lt;d:multistatus xmlns:d="DAV:"&gt;
 *   &lt;d:response&gt;
 *     &lt;d:href&gt;http://www.foo.bar/container/resource3&lt;/d:href&gt;
 *     &lt;d:status&gt;HTTP/1.1 423 Locked&lt;/d:status&gt;
 *   &lt;/d:response&gt;
 * &lt;/d:multistatus&gt;
 * </pre>
 *
 * <p>   In this example, the delete failed because one of the members was
 * locked.
 *
 */
public class DeleteMethod
    extends XMLResponseMethodBase {


    // ----------------------------------------------------------- Constructors


    /**
     * Method constructor.
     */
    public DeleteMethod() {
    }


    /**
     * Method constructor.
     */
    public DeleteMethod(String path) {
        super(path);
    }

    /**
     * Parse response.
     *
     * @param input Input stream
     */
    public void parseResponse(InputStream input, HttpState state, HttpConnection conn)
        throws IOException, HttpException {
        try
        {
            int code = getStatusLine().getStatusCode();
            if (code == WebdavStatus.SC_CONFLICT     ||
                code == WebdavStatus.SC_MULTI_STATUS ||
                code == WebdavStatus.SC_FORBIDDEN ) {
                parseXMLResponse(input);
            }
        }
        catch (IOException e) {
                // FIX ME:  provide a way to deliver non xml data
        }
    }

    public String getName() {
        return "DELETE";
    }

    // --------------------------------------------------- WebdavMethod Methods

}
