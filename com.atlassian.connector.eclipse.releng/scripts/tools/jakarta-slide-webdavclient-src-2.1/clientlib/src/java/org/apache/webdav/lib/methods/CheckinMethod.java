/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/CheckinMethod.java,v 1.4 2004/08/02 15:45:48 unico Exp $
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
 * The CHECKIN method can be applied to a checked-out version-controlled
 * resource to produce a new version whose content and dead properties are
 * copied from the checked-out resource.
 *
 * <p>   This implementation of a CHECKIN client method does support a
 * a request body.</p>
 *
 * <p>   If a CHECKIN request fails, the server state preceding the request
 * MUST be restored. The request body MUST be a DAV:checkin XML element with
 * at most one DAV:keep-checked-out or DAV:fork-ok.</p>
 *
 *
 * <h3>Example Request</h3>
 * <pre>
 * CHECKIN /foo.html HTTP/1.1
 * Host: www.server.org
 * Content-type: text/xml; charset="utf-8"
 * Content-Length: xx
 * </pre>
 *
 * <h3>Example Response</h3>
 * <pre>
 * HTTP/1.1 201 Created
 * Location: http://server.org/history/1/1.1
 * Content-type: text/xml; charset="utf-8"
 * </pre>
 *
 */
public class CheckinMethod
    extends XMLResponseMethodBase {


    // -------------------------------------------------------------- Constants



    // ----------------------------------------------------- Instance Variables


    // ----------------------------------------------------------- Constructors


    /**
     * Method constructor.
     */
    public CheckinMethod() {
    }


    /**
     * Method constructor.
     */
    public CheckinMethod(String path) {
        super(path);
    }





    // ------------------------------------------------------------- Properties

    // --------------------------------------------------- WebdavMethod Methods

    /**
     * Parse response.
     *
     * @param input Input stream
     */
    public void parseResponse(InputStream input, HttpState state, HttpConnection conn)
        throws IOException, HttpException {
        try
        {
            if (getStatusLine().getStatusCode() == WebdavStatus.SC_CONFLICT     ||
                getStatusLine().getStatusCode() == WebdavStatus.SC_FORBIDDEN ) {
                parseXMLResponse(input);
            }
        }
        catch (IOException e) {
                // FIX ME:  provide a way to deliver non xml data
        }
    }

    public String getName() {
        return "CHECKIN";
    }
}
