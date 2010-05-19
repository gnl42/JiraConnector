/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/ReportMethod.java,v 1.6 2004/08/02 15:45:48 unico Exp $
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
import java.util.Enumeration;
import java.util.Vector;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.webdav.lib.PropertyName;
import org.apache.webdav.lib.util.XMLPrinter;

/**
 * This class implements the WebDAV REPORT Method.
 *
 * <P>     The REPORT method retrieves properties defined on the resource
 * identified by the Request-URI, if the resource does not have any internal
 * members, or on the resource identified by the Request-URI and potentially
 * its member resources, if the resource is a collection that has internal
 * member URIs.
 *
 * <P>     A typical request looks like this:
 *
 */
public class ReportMethod extends XMLResponseMethodBase
    implements DepthSupport {


    // -------------------------------------------------------------- Constants


    /**
     * Request specified properties.
     */
    public static final int SUB_SET = 0;


    /**
     * Request of all properties name and value.
     */
    public static final int ALL = 1;

    public static final int LOCATE_HISTORY = 2;

    public String sVersionHistory ="";

    private String preloadedQuery = null;

    // ----------------------------------------------------------- Constructors


    /**
     * Method constructor.
     */
    public ReportMethod() {
    }


    /**
     * Method constructor.
     */
    public ReportMethod(String path) {
        super(path);
    }


    /**
     * Method constructor.
     */
    public ReportMethod(String path, int depth) {
        this(path);
        setDepth(depth);
    }

    /**
     * Method constructor.
     */
    public ReportMethod(String path, Enumeration propertyNames) {
        this(path);
        setDepth(1);
        setPropertyNames(propertyNames);
        setType(SUB_SET);
    }

/**
     * Method constructor.
     */
    public ReportMethod(String path, int depth, Enumeration propertyNames, Enumeration histUrl) {
        this(path);
        setDepth(depth);
        setPropertyNames(propertyNames);
        setHistoryURLs(histUrl);
        setType(LOCATE_HISTORY);
    }

    /**
     * Method constructor.
     */
    public ReportMethod(String path, int depth, Enumeration propertyNames) {
        this(path);
        setDepth(depth);
        setPropertyNames(propertyNames);
        setType(SUB_SET);
    }



    public ReportMethod(String path, int depth, String sBody) {
        this(path);
        setDepth(depth);
        setType(SUB_SET);
        preloadedQuery = sBody;
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Type of the Propfind.
     */
    protected int type = ALL;


    /**
     * Property name list.
     */
    protected PropertyName[] propertyNames;

    /**
     * Depth.
     */
    protected int depth = DEPTH_INFINITY;


    /**
     * The namespace abbreviation that prefixes DAV tags
     */
    protected String prefix = null;


    // ------------------------------------------------------------- Properties




    /**
     * Set a header value, redirecting attempts to set the "Depth" header to
     * a {@link #setDepth} call.
     *
     * @param headerName Header name
     * @param headerValue Header value
     */
    public void setRequestHeader(String headerName, String headerValue) {
        if (headerName.equalsIgnoreCase("Depth")){
            int depth = -1;
            if (headerValue.equals("0")){
                depth = DEPTH_0;
            }
            if (headerValue.equals("1")){
                depth = DEPTH_1;
            }
            else if (headerValue.equalsIgnoreCase("infinity")){
                depth = DEPTH_INFINITY;
            }
            setDepth(depth);
        }
        else{
            super.setRequestHeader(headerName, headerValue);
        }
    }


    /**
     * Type setter.
     *
     * @param type New type value
     */
    public void setType(int type) {
        checkNotUsed();
        this.type = type;
    }


    /**
     * Type getter.
     *
     * @return int type value
     */
    public int getType() {
        return type;
    }


    /**
     * Depth setter.
     *
     * @param depth New depth value
     */
    public void setDepth(int depth) {
        checkNotUsed();
        this.depth = depth;
    }


    /**
     * Depth getter.
     *
     * @return int depth value
     */
    public int getDepth() {
        return depth;
    }


    /**
     * Property names setter.
     * The enumeration may contain strings with or without a namespace prefix
     * but the preferred way is to provide PropertyName objects.
     *
     * @param propertyNames List of the property names
     */
    public void setPropertyNames(Enumeration propertyNames) {
        checkNotUsed();

        Vector list = new Vector();
        while (propertyNames.hasMoreElements()) {

            Object item = propertyNames.nextElement();

            if (item instanceof PropertyName)
            {
                list.add(item);
            }
            else if (item instanceof String)
            {
                String propertyName = (String) item;

                int length = propertyName.length();
                boolean found = false;
                int i = 1;
                while (!found && (i <= length)) {
                    char chr = propertyName.charAt(length - i);
                    if (!Character.isUnicodeIdentifierPart(chr)
                        && chr!='-' && chr!='_' && chr!='.') {
                        found = true;
                    } else {
                        i++;
                    }
                }
                if ((i == 1) || (i >= length)) {
                    list.add(new PropertyName("DAV:",propertyName));
                } else {
                    String namespace = propertyName.substring(0, length + 1 - i);
                    String localName = propertyName.substring(length + 1 - i);
                    list.add(new PropertyName(namespace,localName));
                }
            }
            else
            {
                // unknown type
                // ignore
            }
        }

        this.propertyNames = (PropertyName[])list.toArray(new PropertyName[list.size()]);
    }

    /**
     * sets History URL for locate by history Report
     */
    public void setHistoryURLs(Enumeration historyURLs) {

        sVersionHistory = "<D:version-history-set>";

        while (historyURLs.hasMoreElements())
        {
            sVersionHistory += "<D:href>"+ historyURLs.nextElement().toString() + "</D:href>";
        }

        sVersionHistory += "</D:version-history-set>";
    }


    // --------------------------------------------------- WebdavMethod Methods

    public String getName() {
        return "REPORT";
    }

    public void recycle() {
        super.recycle();
        prefix = null;
    }

    /**
     * Generate additional headers needed by the request.
     *
     * @param state State token
     * @param conn The connection being used to make the request.
     */
    public void addRequestHeaders(HttpState state, HttpConnection conn)
    throws IOException, HttpException {

        // set the default utf-8 encoding, if not already present
        if (getRequestHeader("Content-Type") == null ) super.setRequestHeader("Content-Type", "text/xml; charset=utf-8");
        super.addRequestHeaders(state, conn);

        switch (depth) {
        case DEPTH_0:
            super.setRequestHeader("Depth", "0");
            break;
        case DEPTH_1:
            super.setRequestHeader("Depth", "1");
            break;
        case DEPTH_INFINITY:
            super.setRequestHeader("Depth", "infinity");
            break;
        }

    }

    /**
     * DAV requests that contain a body must override this function to
     * generate that body.
     *
     * <p>The default behavior simply returns an empty body.</p>
     */
    protected String generateRequestBody() {

        if (preloadedQuery != null)
            return preloadedQuery;

        XMLPrinter printer = new XMLPrinter();


        printer.writeXMLHeader();
        if (type!= LOCATE_HISTORY)
            printer.writeElement("D", "DAV:", "version-tree",
                             XMLPrinter.OPENING);

        switch (type) {
        case ALL:
            printer.writeElement("D", "allprop", XMLPrinter.NO_CONTENT);
            printer.writeElement("D", "version-tree", XMLPrinter.CLOSING);
            break;

        case SUB_SET:
            printer.writeElement("D", "prop", XMLPrinter.OPENING);
            for (int i=0 ; i<propertyNames.length ; i++)
            {
                String namespace = propertyNames[i].getNamespaceURI();
                String localname = propertyNames[i].getLocalName();
                if ("DAV:".equals(namespace)) {
                    printer.writeElement("D", localname, XMLPrinter.NO_CONTENT);
                } else {
                    printer.writeElement("ZZ", namespace,localname , XMLPrinter.NO_CONTENT);
                }
            }
            printer.writeElement("D", "prop", XMLPrinter.CLOSING);
            printer.writeElement("D", "version-tree", XMLPrinter.CLOSING);
            break;

        case LOCATE_HISTORY:
            printer.writeElement("D", "DAV:", "locate-by-history", XMLPrinter.OPENING);

            printer.writeText(sVersionHistory);

            printer.writeElement("D", "prop", XMLPrinter.OPENING);
            for (int i=0 ; i<propertyNames.length ; i++)
            {
                String namespace = propertyNames[i].getNamespaceURI();
                String localname = propertyNames[i].getLocalName();
                if ("DAV:".equals(namespace)) {
                    printer.writeElement("D", localname, XMLPrinter.NO_CONTENT);
                } else {
                    printer.writeElement("ZZ", namespace,localname , XMLPrinter.NO_CONTENT);
                }
            }
            printer.writeElement("D", "prop", XMLPrinter.CLOSING);
            printer.writeElement("D", "locate-by-history", XMLPrinter.CLOSING);
            break;

        }

        //System.out.println(query);
        return printer.toString();

    }

    /**
     * This method returns an enumeration of URL paths.  If the ReportMethod
     * was sent to the URL of a collection, then there will be multiple URLs.
     * The URLs are picked out of the <code>&lt;D:href&gt;</code> elements
     * of the response.
     *
     * @return an enumeration of URL paths as Strings
     */
    public Enumeration getAllResponseURLs() {
        checkUsed();
        return getResponseURLs().elements();
    }


    /**
     * Returns an enumeration of <code>Property</code> objects.
     */
            public Enumeration getResponseProperties(String urlPath) {
        checkUsed();

        Response response = (Response) getResponseHashtable().get(urlPath);
        if (response != null) {
            return response.getProperties();
        } else {
            return (new Vector()).elements();
        }

         }
}

