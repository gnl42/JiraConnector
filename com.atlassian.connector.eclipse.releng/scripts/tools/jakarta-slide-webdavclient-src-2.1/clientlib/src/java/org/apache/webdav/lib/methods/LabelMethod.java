/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/LabelMethod.java,v 1.6 2004/08/02 15:45:47 unico Exp $
 * $Revision: 1.6 $
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
import java.io.InputStream;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.webdav.lib.util.WebdavStatus;
import org.apache.webdav.lib.util.XMLPrinter;


/**
 * The Label method is used to manipulate labels on resources on the server.
 *
 * <h3>Example Request</h3>
 * <pre>
 * LABEL /files/testfile.xml HTTP/1.1
 * Host: www.webdav.org
 * Content-Type: text/xml; charset="utf-8"
 *
 * <?xml version="1.0" encoding="utf-8"?>
 * <D:label xmlns:D="DAV:">
 *   <D:set>
 *     <D:label-name>newlabel</D:label-name>
 *   </D:set>
 * </D:label>
 * </pre>
 *
 * <h3>Example Response</h3>
 * <pre>
 * HTTP/1.1 200 OK
 * Cache-Control: no-cache
 * </pre>
 *
 */
public class LabelMethod extends XMLResponseMethodBase {

    // ----------------------------------------------------- Instance Variables

    /**
     * The constant for setting a label.
     */
    public static final int LABEL_SET = 1;


    /**
     * The constant for adding a label.
     */
    public static final int LABEL_ADD = 2;


    /**
     * The constant for removing a label.
     */
    public static final int LABEL_REMOVE = 3;


    /**
     * The label name.
     */
    private String labelName = null;


    /**
     * The lable type.
     */
    private int type = 0;

    // ----------------------------------------------------------- Constructors

    /**
     * The default constructor.
     */
    public LabelMethod() {
    }


    /**
     * The label method constructor.
     *
     * @param path the path
     * @param action the action
     * @param labelName the label name
     */
    public LabelMethod(String path, int action, String labelName) {
        super(path);
        this.labelName = labelName;
        this.type = action;
    }


    /**
     * Set the type of label action to take.
     *
     * @param type the type of the label action
     */
    public void setType(int type) {
        this.type = type;
    }


    /**
     * Get the label type which has been set.
     *
     * @return the type
     */
    public int getType() {
        return type;
    }


    /**
     * Set the label-name this action will manipulate.
     *
     * @param labelName the label name
     */
    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }


    /**
     * Get the label-name this action will manipulate.
     *
     * @return the label-name
     */
    public String getLabelName() {
        return labelName;
    }


    /**
     * Generate the protocol headers.
     *
     * @param state the state
     * @param conn the connection
     */
    public void addRequestHeaders(HttpState state, HttpConnection conn)
    throws IOException, HttpException {

        // set the default utf-8 encoding, if not already present
        if (getRequestHeader("Content-Type") == null ) super.setRequestHeader("Content-Type", "text/xml; charset=utf-8");
        super.addRequestHeaders(state, conn);
    }

    /**
     * DAV requests that contain a body must override this function to
     * generate that body.
     *
     * <p>The default behavior simply returns an empty body.</p>
     */
    protected String generateRequestBody() {

        if (type <= 0 || labelName == null)
            throw new IllegalStateException
                ("Action type and label name must be set before " +
                 "calling this function");

        XMLPrinter printer = new XMLPrinter();
 
        printer.writeXMLHeader();
        printer.writeElement("D", "DAV:", "label", XMLPrinter.OPENING);

        String typeElement = null;
        switch (type) {
            case LABEL_SET:
                typeElement = "set";
                break;
            case LABEL_REMOVE:
                typeElement = "remove";
                break;
            case LABEL_ADD:
                typeElement = "add";
                break;
        }

        printer.writeElement("D", typeElement, XMLPrinter.OPENING);
        printer.writeElement("D", "label-name", XMLPrinter.OPENING);
        printer.writeText(labelName);
        printer.writeElement("D", "label-name", XMLPrinter.CLOSING);
        printer.writeElement("D", typeElement, XMLPrinter.CLOSING);
        printer.writeElement("D", "label", XMLPrinter.CLOSING);

        return printer.toString();
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
        return "LABEL";
    }
}


