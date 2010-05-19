/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/AclReportMethod.java,v 1.6 2004/08/02 15:45:48 unico Exp $
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
import java.util.Collection;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.webdav.lib.util.XMLPrinter;

/**
 * WebDAV Report method
 * This class is used to send an report
 * from the ACL specification.
 * In this version only the principal-property-search is supported.
 *
 */
public class AclReportMethod extends XMLResponseMethodBase implements DepthSupport {

    //supported method types
    public final static int PRINCIPAL_PROPERTY_SEARCH = 1;

    private PropertyBodyHelper propertyBodyHelper = null;
    private int depth = DepthSupport.DEPTH_INFINITY;
    private int reportType = 0;

    /**
     * @param path
     * @param propertyNames requested properties
     * @param depth
     * @param reportType - one of the supported report types
     */
    public AclReportMethod(
        String path,
        Collection propertyNames,
        int depth,
        int reportType) {

        super(path);
        setDepth(depth);
        this.reportType = reportType;
        propertyBodyHelper = new PropertyBodyHelper();
        propertyBodyHelper.setPropertyNames(propertyNames);
    }

    /**
     * @see org.apache.commons.httpclient.HttpMethod#getName()
     */
    public String getName() {
        return "REPORT";
    }

    /**
     * @see org.apache.webdav.lib.methods.DepthSupport#setDepth(int)
     */
    public void setDepth(int depth) {
        checkNotUsed();
        this.depth = depth;
    }

    /**
     * @see org.apache.webdav.lib.methods.DepthSupport#getDepth()
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Set a header value, redirecting the special case of header "Depth" to
     * {@link #setDepth} as appropriate.
     *
     * @param headerName Header name
     * @param headerValue Header value
     */
    public void setRequestHeader(String headerName, String headerValue) {
        if (headerName.equalsIgnoreCase("Depth")) {
            int depth = -1;
            if (headerValue.equals("0")) {
                depth = DEPTH_0;
            }
            if (headerValue.equals("1")) {
                depth = DEPTH_1;
            } else if (headerValue.equalsIgnoreCase("infinity")) {
                depth = DEPTH_INFINITY;
            }
            setDepth(depth);
        } else {
            super.setRequestHeader(headerName, headerValue);
        }
    }

    /**
     * Generate additional headers needed by the request.
     *
     * @param state State token
     * @param conn The connection being used for the request.
     */
    public void addRequestHeaders(HttpState state, HttpConnection conn)
        throws IOException, HttpException {

        super.addRequestHeaders(state, conn);

        // set the default utf-8 encoding, if not already present
        if (getRequestHeader("Content-Type") == null ) super.setRequestHeader("Content-Type", "text/xml; charset=utf-8");

        switch (getDepth()) {
            case DEPTH_0 :
                super.setRequestHeader("Depth", "0");
                break;
            case DEPTH_1 :
                super.setRequestHeader("Depth", "1");
                break;
            case DEPTH_INFINITY :
                super.setRequestHeader("Depth", "infinity");
                break;
        }
    }

    /**
     * @see org.apache.webdav.lib.methods.XMLResponseMethodBase#generateRequestBody()
     */
    protected String generateRequestBody() {
        XMLPrinter printer = new XMLPrinter();

        printer.writeXMLHeader();

        switch (reportType) {
            case PRINCIPAL_PROPERTY_SEARCH :
                generatePrincipalPropertySearchBody(printer);
                break;
            default :
                System.err.println("AclReportMethod: type not supported " + reportType);
        }
        return printer.toString();
    }

    /**
     * Generate the body for a principal-property-search report.
     * Currently the <property-search> section is constant.
     *
     */
    private void generatePrincipalPropertySearchBody(XMLPrinter printer) {
        printer.writeElement(
            "D",
            "DAV:",
            "principal-property-search",
            XMLPrinter.OPENING);

        //property-search
        printer.writeElement("D", "property-search", XMLPrinter.OPENING);
        printer.writeElement("D", "prop", XMLPrinter.OPENING);
        printer.writeElement("D", "displayname", XMLPrinter.NO_CONTENT);
        printer.writeElement("D", "prop", XMLPrinter.CLOSING);
        printer.writeElement("D", "caseless-substring", XMLPrinter.NO_CONTENT);
        printer.writeElement("D", "property-search", XMLPrinter.CLOSING);

        //resulting properties
        propertyBodyHelper.wirtePropElement(printer);

        printer.writeElement("D", "principal-property-search", XMLPrinter.CLOSING);
    }

}
