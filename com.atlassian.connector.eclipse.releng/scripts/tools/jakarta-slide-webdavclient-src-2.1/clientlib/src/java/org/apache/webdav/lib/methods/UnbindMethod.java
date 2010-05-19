/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/UnbindMethod.java,v 1.5 2004/08/02 15:45:48 unico Exp $
 * $Revision: 1.5 $
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

import org.apache.webdav.lib.util.XMLPrinter;

/**
 * The UNBIND method modifies the collection identified by the Request-URI,
 * by removing the binding identified by the segment specified in the UNBIND
 * body.
 *
 * UNBIND Method Example:
 * >> Request:
 *  UNBIND /CollX HTTP/1.1
 *  Host: www.example.com
 *  Content-Type: text/xml; charset="utf-8"
 *  Content-Length: xxx
 *  <?xml version="1.0" encoding="utf-8" ?>
 *  <D:unbind xmlns:D="DAV:">
 *      <D:segment>foo.html</D:segment>
 *  </D:unbind>
 *
 * >> Response:
 *  HTTP/1.1 200 OK
 * The server removed the binding named "foo.html" from the collection,
 * "http://www.example.com/CollX". A request to the resource named
 * "http://www.example.com/CollX/foo.html" will return a 404 (Not Found)
 * response.
 *
 */
public class UnbindMethod
    extends XMLResponseMethodBase {


    public static final String NAME = "UNBIND";

    private String segment = null;

    // ----------------------------------------------------------- Constructors


    /**
     * Method constructor.
     */
    public UnbindMethod() {
    }

    public UnbindMethod(String binding) {
        super(binding.substring(0, binding.lastIndexOf('/')));
        this.segment = binding.substring(binding.lastIndexOf('/') + 1);
    }

    public String getName() {
        return NAME;
    }

    /**
     * DAV requests that contain a body must override this function to
     * generate that body.
     *
     * <p>The default behavior simply returns an empty body.</p>
     */
    protected String generateRequestBody() {

        if (segment == null)
            throw new IllegalStateException
                ("Segment must be set before calling this function.");

        XMLPrinter printer = new XMLPrinter();

        printer.writeXMLHeader();
        printer.writeElement("D", "DAV:", "unbind", XMLPrinter.OPENING);
        printer.writeElement("D", "segment", XMLPrinter.OPENING);
        printer.writeText(segment);
        printer.writeElement("D", "segment", XMLPrinter.CLOSING);
        printer.writeElement("D", "unbind", XMLPrinter.CLOSING);

        return printer.toString();
    }

    /**
     * @return resource name to be unbound
     */
    public String getSegment() {
        return segment;
    }

    /**
     * @param segment resource name to be unbound
     */
    public void setSegment(String segment) {
        this.segment = segment;
    }

}

