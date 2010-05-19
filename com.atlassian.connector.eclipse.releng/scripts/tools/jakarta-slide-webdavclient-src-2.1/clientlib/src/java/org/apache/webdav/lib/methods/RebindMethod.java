/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/RebindMethod.java,v 1.5 2004/08/02 15:45:47 unico Exp $
 * $Revision: 1.5 $
 * $Date: 2004/08/02 15:45:47 $
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
import org.apache.webdav.lib.util.XMLPrinter;

/**
 * The REBIND method removes a binding to a resource from one collection,
 * and adds a binding to that resource into another collection. It is
 * effectively an atomic form of a MOVE request.
 *
 * REBIND Method Example:
 * >> Request:
 *  REBIND /CollX HTTP/1.1
 *  Host: www.example.com
 *  Content-Type: text/xml; charset="utf-8"
 *  Content-Length: xxx
 *  <?xml version="1.0" encoding="utf-8" ?>
 *  <D:rebind xmlns:D="DAV:">
 *      <D:segment>foo.html</D:segment>
 *      <D:href>http://www.example.com/CollY/bar.html</D:href>
 *  </D:rebind>
 *
 * >> Response:
 *  HTTP/1.1 200 OK
 * The server added a new binding to the collection,
 * "http://www.example.com/CollX", associating "foo.html" with the resource
 * identified by the URI "http://www.example.com/CollY/bar.html",
 * and removes the binding named "bar.html" from the collection identified
 * by the URI "http://www.example.com/CollY".
 * Clients can now use the URI "http://www.example.com/CollX/foo.html" to
 * submit requests to that resource, and requests on the URI
 * "http://www.example.com/CollY/bar.html" will fail with a 404 (Not Found)
 * response.
 *
 */
public class RebindMethod
    extends XMLResponseMethodBase {


    public static final String NAME = "REBIND";

    private boolean overwrite = true;
    private String segment = null;
    private String href = null;

    // ----------------------------------------------------------- Constructors


    /**
     * Method constructor.
     */
    public RebindMethod() {
    }

    public RebindMethod(String existingBinding, String newBinding) {
        super(newBinding.substring(0, newBinding.lastIndexOf('/')));
        this.href = existingBinding;
        this.segment = newBinding.substring(newBinding.lastIndexOf('/') + 1);
    }

    public String getName() {
        return NAME;
    }

    /**
     * By default, if there already is a binding for the specified segment
     * in the collection, the new binding replaces the existing binding.
     * This default binding replacement behavior can be overridden using
     * the Overwrite header.
     *
     * @return the current value of the overwrite flag
     */
    public boolean isOverwrite() {
        return overwrite;
    }


    /**
     * By default, if there already is a binding for the specified segment
     * in the collection, the new binding replaces the existing binding.
     * This default binding replacement behavior can be overridden using
     * the Overwrite header.
     *
     * @param overwrite New overwrite value
     */
    public void setOverwrite(boolean overwrite) {
        checkNotUsed();
        this.overwrite = overwrite;
    }


    /**
     * Generate additional headers needed by the request.
     *
     * @param state HttpState token
     * @param conn The connection being used for the request.
     */
    public void addRequestHeaders(HttpState state, HttpConnection conn)
        throws IOException, HttpException {

        super.addRequestHeaders(state, conn);

        if (!isOverwrite())
            super.setRequestHeader("Overwrite", "F");

    }

    /**
     * DAV requests that contain a body must override this function to
     * generate that body.
     *
     * <p>The default behavior simply returns an empty body.</p>
     */
    protected String generateRequestBody() {

        if (segment == null || href == null)
            throw new IllegalStateException
                ("Segment and Href must be set before " +
                 "calling this function.");

        XMLPrinter printer = new XMLPrinter();

        printer.writeXMLHeader();
        printer.writeElement("D", "DAV:", "rebind", XMLPrinter.OPENING);
        printer.writeElement("D", "segment", XMLPrinter.OPENING);
        printer.writeText(segment);
        printer.writeElement("D", "segment", XMLPrinter.CLOSING);
        printer.writeElement("D", "href", XMLPrinter.OPENING);
        printer.writeText(href);
        printer.writeElement("D", "href", XMLPrinter.CLOSING);
        printer.writeElement("D", "rebind", XMLPrinter.CLOSING);

        return printer.toString();
    }

    /**
     * @return path of the resource to be rebound
     */
    public String getHref() {
        return href;
    }

    /**
     * @return new resource name
     */
    public String getSegment() {
        return segment;
    }

    /**
     * @param href path of the resource to be rebound
     */
    public void setHref(String href) {
        this.href = href;
    }

    /**
     * @param segment new resource name
     */
    public void setSegment(String segment) {
        this.segment = segment;
    }

}

