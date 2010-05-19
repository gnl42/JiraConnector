/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/OptionsMethod.java,v 1.6 2004/08/02 15:45:48 unico Exp $
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.webdav.lib.util.WebdavStatus;
import org.apache.webdav.lib.util.XMLPrinter;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * OPTIONS Method.
 *
 */
public class OptionsMethod
    extends XMLResponseMethodBase {


    // -------------------------------------------------------------- Constants


    /**
     * DAV level 1. Mandatory.
     */
    public static final String DAV_LEVEL1 = "1";


    /**
     * DAV level 2.
     */
    public static final String DAV_LEVEL2 = "2";


    /**
     * Advanced collections.
     */
    public static final String ADVANCED_COLLECTIONS = "3";


    /**
     * Delta V.
     */
    public static final String DELTAV = "4";


    /**
     * ACL.
     */
    public static final String ACL = "5";


    /**
     * DASL.
     */
    public static final String DASL = "6";

    /**
     *
     */
    public static final int OPTIONS_WORKSPACE = 8;

    /**
     *
     */
    public static final int OPTIONS_VERSION_HISTORY = 9;



    // ----------------------------------------------------------- Constructors


    /**
     * Method constructor.
     */
    public OptionsMethod() {
    }


    /**
     * Method constructor.
     */
    public OptionsMethod(String path) {
        super(path);
    }

    /**
     * Method constructor.
     */
    public OptionsMethod(String path, int type) {
        super(path);
        this.type = type;
    }

    // ----------------------------------------------------- Instance Variables
    /**
     * DAV Capabilities.
     */
    private Vector davCapabilities = new Vector();


    /**
     * Methods allowed.
     */
    private Vector methodsAllowed = new Vector();

    private int type = 0;

    private boolean hasXMLBody = false;


    // --------------------------------------------------------- Public Methods


    /**
     * Is the specified method allowed ?
     */
    public boolean isAllowed(String method) {
        checkUsed();
        return methodsAllowed.contains(method);
    }


    /**
     * Get a list of allowed methods.
     */
    public Enumeration getAllowedMethods() {
        checkUsed();
        return methodsAllowed.elements();
    }


    /**
     * Is DAV capability supported ?
     */
    public boolean isSupported(String capability) {
        checkUsed();
        return davCapabilities.contains(capability);
    }


    /**
     * Get a list of supported DAV capabilities.
     */
    public Enumeration getDavCapabilities() {
        checkUsed();
        return davCapabilities.elements();
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
            if (getStatusLine().getStatusCode() == WebdavStatus.SC_OK && hasXMLBody) {
                parseXMLResponse(input);
            }
        }
        catch (IOException e) {
                // FIX ME:  provide a way to deliver non xml data
        }
    }


    // --------------------------------------------------- WebdavMethod Methods





    /**
     * Process response headers. The contract of this method is that it only
     * parses the response headers.
     *
     * @param state the state
     * @param conn the connection
     */
    public void processResponseHeaders(HttpState state,
        HttpConnection conn) {

        Header davHeader = getResponseHeader("dav");
        if (davHeader != null) {
            String davHeaderValue = davHeader.getValue();
            StringTokenizer tokenizer =
                new StringTokenizer(davHeaderValue, ",");
            while (tokenizer.hasMoreElements()) {
                String davCapability = tokenizer.nextToken().trim();
                davCapabilities.addElement(davCapability);
            }
        }

        Header allowHeader = getResponseHeader("allow");
        if (allowHeader != null) {
            String allowHeaderValue = allowHeader.getValue();
            StringTokenizer tokenizer =
                new StringTokenizer(allowHeaderValue, ",");
            while (tokenizer.hasMoreElements()) {
                String methodAllowed =
                    tokenizer.nextToken().trim().toUpperCase();
                methodsAllowed.addElement(methodAllowed);
            }
        }

        Header lengthHeader = getResponseHeader("content-length");
        Header typeHeader = getResponseHeader("content-type");
        if(
                (lengthHeader != null &&
                 Integer.parseInt(lengthHeader.getValue()) > 0) ||
                (typeHeader != null &&
                 typeHeader.getValue().startsWith("text/xml")))
            hasXMLBody = true;

        super.processResponseHeaders(state, conn);
    }

    /**
     * DAV requests that contain a body must override this function to
     * generate that body.
     *
     * <p>The default behavior simply returns an empty body.</p>
     */
    protected String generateRequestBody() {

        if (type != 0){
            XMLPrinter printer = new XMLPrinter();

            printer.writeXMLHeader();
            //System.out.println(printer.toString());
            printer.writeElement("D", "DAV:", "options",
                                 XMLPrinter.OPENING);

            if (type == OPTIONS_VERSION_HISTORY)
                printer.writeElement("D", "version-history-collection-set", XMLPrinter.NO_CONTENT);
            if (type == OPTIONS_WORKSPACE)
                printer.writeElement("D", "workspace-collection-set", XMLPrinter.NO_CONTENT);

            printer.writeElement("D", "options", XMLPrinter.CLOSING);

            return printer.toString();
        }

        return null;
    }

    public String getName() {
        return "OPTIONS";
    }

        //get and set header
     public void addRequestHeaders(HttpState state, HttpConnection conn)
     throws IOException, HttpException {

         if (type!= 0){
            // set the default utf-8 encoding, if not already present
            if (getRequestHeader("Content-Type") == null ) super.setRequestHeader("Content-Type", "text/xml; charset=utf-8");
         }

         super.addRequestHeaders(state, conn);
     }

/**
     * This method returns an enumeration of URL paths.  If the PropFindMethod
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

    public Enumeration getResponseProperties(){
        Vector result = new Vector();
        return (Enumeration) result;
    }


    protected Document parseResponseContent(InputStream is)
        throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();

        byte[] chunk;
        byte[] all;
        int chunkLen;
        int allLen;
        List chunks;
        int i;
        int max;
        int ofs;

        allLen = 0;
        chunk = new byte[1024*4];
        chunkLen = is.read(chunk);
        chunks = new ArrayList();
        while (chunkLen != -1) {
            chunks.add(new Integer(chunkLen));
            chunks.add(chunk);
            allLen += chunkLen;
            chunk = new byte[1024*4];
            chunkLen = is.read(chunk);
        }

        all = new byte[allLen];
        ofs = 0;
        max = chunks.size();
        for (i = 0; i < max; i += 2) {
            chunkLen = ((Integer) chunks.get(i)).intValue();
            chunk = (byte[]) chunks.get(i + 1);
            System.arraycopy(chunk, 0, all, ofs, chunkLen);
            ofs += chunkLen;
        }

        if (all.length == 0) return null;
        return builder.parse(new InputSource(new ByteArrayInputStream(all)));

    }

}




