/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/UpdateMethod.java,v 1.6 2004/08/02 15:45:48 unico Exp $
 * $Revision: 1.6 $
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
import org.apache.webdav.lib.util.XMLPrinter;


/**
 * The Update method updates a version-controlled resource to a new version.
 * Two parameters are required, the path of the resource, and a URI identifying
 * the version from the history to which to update.
 *
 */
public class UpdateMethod extends XMLResponseMethodBase {


    // -------------------------------------------------------------- Constants



    // ----------------------------------------------------- Instance Variables

    private String target = null;


    // ----------------------------------------------------------- Constructors


    /**
     * Method constructor.
     */
    public UpdateMethod() {
    }


   /**
     * Method constructor.
     */
    public UpdateMethod(String path) {
        super(path);
    }



    /**
     * Method constructor.
     *
     * @param path
     * @param target
     */
    public UpdateMethod(String path, String target) {
        super(path);
        this.target = target;
    }


    public String getName() {
        return "UPDATE";
    }

    /**
     * DAV requests that contain a body must override this function to
     * generate that body.
     *
     * <p>The default behavior simply returns an empty body.</p>
     */
    protected String generateRequestBody() {
        if (target != null) {
            XMLPrinter printer = new XMLPrinter();


            printer.writeXMLHeader();
            printer.writeElement("D", "DAV:", "update", XMLPrinter.OPENING);
            printer.writeElement("D", "version", XMLPrinter.OPENING);
            printer.writeElement("D", "href", XMLPrinter.OPENING);
            printer.writeText(target);
            printer.writeElement("D", "href", XMLPrinter.CLOSING);
            printer.writeElement("D", "version", XMLPrinter.CLOSING);
            printer.writeElement("D", "update", XMLPrinter.CLOSING);

            return printer.toString();
        }
        else
            return "";
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


}
