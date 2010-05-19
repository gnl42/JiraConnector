/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/XMLResponseMethodBase.java,v 1.14.2.2 2004/11/17 14:00:44 luetzkendorf Exp $
 * $Revision: 1.14.2.2 $
 * $Date: 2004/11/17 14:00:44 $
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
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;

import org.apache.webdav.lib.Property;
import org.apache.webdav.lib.ResponseEntity;
import org.apache.webdav.lib.properties.PropertyFactory;
import org.apache.webdav.lib.util.DOMUtils;
import org.apache.webdav.lib.util.DOMWriter;
import org.apache.webdav.lib.util.WebdavStatus;
import org.apache.webdav.lib.util.XMLDebugOutputer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Utility class for XML response parsing.
 *
 */
public abstract class XMLResponseMethodBase
    extends HttpRequestBodyMethodBase {

    //static private final Log log = LogSource.getInstance(XMLResponseMethodBase.class.getName());
    
    
    // debug level 
    private int debug = 0;


    // ----------------------------------------------------------- Constructors


    /**
     * Method constructor.
     */
    public XMLResponseMethodBase() {
        super();
    }


    /**
     * Method constructor.
     *
     * @param uri the URI to request
     */
    public XMLResponseMethodBase(String uri) {
        super(uri);
        
    }


    // ----------------------------------------------------- Instance Variables
    
    /**
     * XML Debug Outputer
     */
    private XMLDebugOutputer xo = new XMLDebugOutputer();


    /**
     * Response document.
     */
    private Document responseDocument = null;


    /**
     * Document builder.
     */
    protected DocumentBuilder builder = null;


    /**
     * Hashtable of response nodes
     */
    private Hashtable responseHashtable = null;

    /**
     * Vector of response nodes, to keep track of insertion order
     * FIXME: the above Hashtable and this Vector should be ported
     * to plain Collections
     */
    protected Vector responseURLs = null;

    protected String decodeResponseHrefs = null;

    // ------------------------------------------------------------- Properties


    /**
     * Response document getter.
     *
     * @return Document response document
     */
    public Document getResponseDocument() {
        return this.responseDocument;
    }


    /**
     * Return an enumeration containing the responses.
     *
     * @return An enumeration containing objects implementing the
     * ResponseEntity interface
     */
    public Enumeration getResponses() {
        return getResponseHashtable().elements();
    }


    // --------------------------------------------------- WebdavMethod Methods

    /**
     * Debug property setter.
     *
     * @param int Debug
     */ 
    public void setDebug(int debug) {
        this.debug = debug;
        
        xo.setDebug((debug > 0));
    }
    
	/**
	 * Debug property getter.
	 *
	 */ 
	public int getDebug() {
		return this.debug;
	}

	/**
	 * Sets whether the href in responses are decoded, as early as possible.
	 * The <code>href</code> data in responses is often url-encoded, but not 
	 * alwyas in a comparable way. Set this to a non-null value to decode the 
	 * hrefs as early as possible.
	 * @param encoding The encoding used in while decoding (UTF-8 is recommended)
	 */
	public void setDecodeResponseHrefs(String encoding) {
	    this.decodeResponseHrefs = encoding;
	}

    /**
     * Reset the State of the class to its initial state, so that it can be
     * used again.
     */
    public void recycle() {
        super.recycle();
        responseHashtable = null;
        responseURLs = null;
    }

    protected void readResponseBody(HttpState state, HttpConnection conn)
    throws IOException, HttpException {

        super.readResponseBody(state, conn);
        InputStream inStream = getResponseBodyAsStream();
        if (inStream != null) {
            parseResponse(inStream, state, conn);
            inStream.close();
        }
    }

    /**
     * Return the length (in bytes) of my request body, suitable for use in a
     * <tt>Content-Length</tt> header.
     *
     * <p>
     * Return <tt>-1</tt> when the content-length is unknown.
     * </p>
     *
     * <p>
     * This implementation returns <tt>0</tt>, indicating that the request has
     * no body.
     * </p>
     *
     * @return <tt>0</tt>, indicating that the request has no body.
     */
    protected int getRequestContentLength() {
        if (!isRequestContentAlreadySet()) {
            String contents = generateRequestBody();
            // be nice - allow overriding functions to return null or empty
            // strings for no content.
            if (contents == null)
                contents = "";

            setRequestBody(contents);
            

            if (debug > 0) {
                System.out.println("\n>>>>>>>  to  server  ---------------------------------------------------");
				System.out.println(getName() + " " +
				   getPath() + (getQueryString() != null ? "?" + getQueryString() : "") + " " + "HTTP/1.1");
        
				   Header[] headers = getRequestHeaders();
				   for (int i = 0; i < headers.length; i++) {
					   Header header = headers[i];
					   System.out.print(header.toString());
				   }
				System.out.println("Content-Length: "+super.getRequestContentLength());
				   
				if (this instanceof DepthSupport) {
					System.out.println("Depth: "+((DepthSupport)this).getDepth());
				}

                System.out.println();
                xo.print(contents);
                System.out.println("------------------------------------------------------------------------");
            }

        }

        return super.getRequestContentLength();
    }




    /**
     * DAV requests that contain a body must override this function to
     * generate that body.
     *
     * <p>The default behavior simply returns an empty body.</p>
     */
    protected String generateRequestBody() {
        return "";
    }

    /**
     * Write the request body to the given {@link HttpConnection}.
     *
     * <p>
     * This implementation writes any computed body and returns <tt>true</tt>.
     * </p>
     *
     * @param state the client state
     * @param conn the connection to write to
     *
     * @return <tt>true</tt>
     * @throws IOException when i/o errors occur reading the response
     * @throws HttpException when a protocol error occurs or state is invalid
     */
    protected boolean writeRequestBody(HttpState state, HttpConnection conn)
            throws IOException, HttpException {

        if (getRequestContentLength() > 0) {
            return super.writeRequestBody(state, conn);
        }
        return true;
    }

    /**
     * Parse response.
     *
     * @param input Input stream
     */
    public void parseResponse(InputStream input, HttpState state, HttpConnection conn)
        throws IOException, HttpException {
        // Also accept OK sent by buggy servers in reply to a PROPFIND or
        // REPORT (Xythos, Catacomb, ...?).
        if (getStatusCode() == WebdavStatus.SC_MULTI_STATUS
            || (this instanceof PropFindMethod || this instanceof ReportMethod)
                && getStatusCode() == HttpStatus.SC_OK) {
            try {
                parseXMLResponse(input);
            } catch (IOException e) {
                // FIX ME:  provide a way to deliver non xml data
            }
        }
    }


    protected void parseXMLResponse(InputStream input)
        throws IOException, HttpException {

        if (builder == null) {
            try {
                // TODO: avoid the newInstance call for each method instance for performance reasons.
                DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new HttpException
                    ("XML Parser Configuration error: " + e.getMessage());
            }
        }

        try {

            // avoid ugly printlns from the default error handler.
            builder.setErrorHandler(new DummyErrorHandler());
            responseDocument = builder.parse(new InputSource(input));
            
            if (debug > 0) {
               System.out.println("\n<<<<<<< from server  ---------------------------------------------------");
               System.out.println(getStatusLine());
               
               Header[] headers = getResponseHeaders();
               for (int i = 0; i < headers.length; i++) {
                  Header header = headers[i];
                  System.out.print(header.toString());
               }
               
               System.out.println();
               
               xo.print(responseDocument);
               System.out.println("------------------------------------------------------------------------");
            }


        } catch (Exception e) {
            throw new IOException
                ("XML parsing error; response stream is not valid XML: "
                 + e.getMessage());
        }


        // init the response table to display the responses during debugging
        /*if (debug > 10) {
            //if (log.isDebugEnabled()) {
            initResponseHashtable();
        }*/

    }


    protected Hashtable getResponseHashtable() {
        checkUsed();
        if (responseHashtable == null) {
            initResponseHashtable();
        }
        return responseHashtable;
    }

    protected Vector getResponseURLs() {
        checkUsed();
        if (responseHashtable == null) {
            initResponseHashtable();
        }
        return responseURLs;
    }

    private synchronized void initResponseHashtable() {
        if (responseHashtable == null) {

            responseHashtable = new Hashtable();
            responseURLs = new Vector();
            int status = getStatusLine().getStatusCode();
           
            // Also accept OK sent by buggy servers in reply to a PROPFIND
            // or REPORT (Xythos, Catacomb, ...?).
            if (status == WebdavStatus.SC_MULTI_STATUS
                || (this instanceof PropFindMethod
                    || this instanceof ReportMethod)
                    && status == HttpStatus.SC_OK) {


                Document rdoc = getResponseDocument();

                NodeList list = null;
                if (rdoc != null) {
                    Element multistatus = getResponseDocument().getDocumentElement();
                    list = multistatus.getChildNodes();
                }

                if (list != null) {
                    for (int i = 0; i < list.getLength(); i++) {
                        try {
                            Element child = (Element) list.item(i);
                            String name = DOMUtils.getElementLocalName(child);
                            String namespace = DOMUtils.getElementNamespaceURI
                                (child);
                            if (Response.TAG_NAME.equals(name) &&
                                "DAV:".equals(namespace)) {
                                Response response =
                                    new ResponseWithinMultistatus(child);
                                String href = getHref(response);
                                responseHashtable.put(href,response);
                                responseURLs.add(href);
                            }
                        } catch (ClassCastException e) {
                        }
                    }
                }
            } else if (responseDocument != null) {
                Response response = new SingleResponse(responseDocument,
                    getPath(), status);
                String href = getHref(response);
                responseHashtable.put(href, response);
                responseURLs.add(href);
            }
        }
    }


    private String getHref(Response response) {
        String href = response.getHref();
        if (this.decodeResponseHrefs != null) {
            try {
                href = URIUtil.decode(href, this.decodeResponseHrefs);
            }
            catch (URIException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        return href;
    }


    /**
     * This method creates a property implementation from an element.
     * It treats known properties (i.e., the DAV properties) specially.
     * These properties are instantiated as an implementation from the
     * <code>org.apache.webdav.lib.properties</code> package.
     */
    protected static Property convertElementToProperty(
        Response response, Element element) {

        return PropertyFactory.create(response, element);
        
    }


    // ---------------------------------------------------------- Inner Classes


    /**
     * An abstract class that models a DAV:response.
     */
    public abstract class Response implements ResponseEntity {

        protected Node node = null;

        Response(Node node) {
            this.node = node;
        }

        public static final String TAG_NAME = "response";
        public abstract int getStatusCode();
        public abstract String getHref();

        public Enumeration getHistories(){
            Vector result = new Vector();
            return result.elements();
        }
        public Enumeration getWorkspaces(){
            Vector result = new Vector();
            return result.elements();
        }
        public Enumeration getProperties() {
            NodeList list =
                DOMUtils.getElementsByTagNameNS(node, "prop", "DAV:");
            Vector vector = new Vector();
            for (int i = 0; list != null && i < list.getLength(); i++ ) {
                Element element = (Element) list.item(i);
                NodeList children = element.getChildNodes();
                for (int j = 0; children != null && j < children.getLength();
                     j++) {
                    try {
                        Element child = (Element) children.item(j);
                        vector.addElement(XMLResponseMethodBase.
                            convertElementToProperty(this, child));
                    } catch (ClassCastException e) {
                    }
                }
            }
            return vector.elements();
        }

        public String toString () {
            StringWriter tmp = new StringWriter();
            DOMWriter domWriter = new DOMWriter(tmp, true);
            domWriter.print(node);
            return tmp.getBuffer().toString();
        }
    }


    /**
     * A class that models the DAV:response element within a multistatus.
     */
    class ResponseWithinMultistatus extends Response {

        public ResponseWithinMultistatus(Element element) {
            super(element);
        }

        public int getStatusCode() {
            // The status element for the response can be inside the propstat element
            // or directly inside the response element.

            //  <multistatus xmlns=\DAV:\>
            //    <response>
            //      <href>/slide/files/</href>
            //      <propstat>
            //        <prop><displayname>files</displayname></prop>
            //        <status>HTTP/1.1 200 OK</status>
            //      </propstat>
            //    </response>
            //  </multistatus>
            Element propstat = getFirstElement("DAV:", "propstat");
            if (propstat != null ) {
                Element status = DOMUtils.getFirstElement(propstat,"DAV:", "status");
                if (status != null) {
                    return DOMUtils.parseStatus(DOMUtils.getTextValue(status));
                }
            }

            //  <multistatus xmlns=\DAV:\>
            //    <response>
            //      <href>/slide/files/</href>
            //      <href>/slide/files/a</href>
            //      <status>HTTP/1.1 200 OK</status>
            //    </response>
            //  </multistatus>
            Element status = getFirstElement("DAV:", "status");
            if (status != null) {
                return DOMUtils.parseStatus(DOMUtils.getTextValue(status));
            }

            return -1;
        }

        public String getHref() {
            Element href = getFirstElement("DAV:", "href");
            if (href != null) {
                return DOMUtils.getTextValue(href);
            } else {
                return "";
            }

        }

        protected Element getFirstElement(String namespace, String name) {
            return DOMUtils.getFirstElement(this.node, namespace, name);
        }
    }

    class SingleResponse extends Response {

        private int statusCode = -1;
        private String href = null;

        SingleResponse(Document document, String href, int statusCode) {
            super(document);
            this.statusCode = statusCode;
            this.href = href;
        }

        public int getStatusCode() {
            return this.statusCode;
        }

        public String getHref() {
            return this.href;
        }
    }

        class OptionsResponse extends SingleResponse{

            OptionsResponse(Document document, String href, int statusCode) {
                super(document, href, statusCode);

            }


            public Enumeration getWorkspaces(){


                Node root = responseDocument.cloneNode(true).getFirstChild();
                //System.out.println("Rootnode ws: "+ root.getNodeName());

                String sPrefix = root.getPrefix();
                Vector result = new Vector();

                Node child = root.getFirstChild();
                while (child!=null && !child.getNodeName().equalsIgnoreCase(sPrefix+":workspace-collection-set")){
                    child = child.getNextSibling();
                }

                if (child!=null && child.getNodeName().equalsIgnoreCase(sPrefix+":workspace-collection-set")){
                    child = child.getFirstChild();
                    while (child!=null){
                        result.add(child.getFirstChild().getNodeValue());
                        child = child.getNextSibling();
                    }
                }

                return result.elements();
            }

            public Enumeration getHistories(){
                Node root = responseDocument.cloneNode(true).getFirstChild();
                //System.out.println("Rootnode vh : " + root.getNodeName());

                String sPrefix = root.getPrefix();
                Vector result = new Vector();

                //System.out.println("Prefix : " + sPrefix);

                Node child = root.getFirstChild();
                while (child!=null && !child.getNodeName().equalsIgnoreCase(sPrefix+":version-history-collection-set")){
                    child = child.getNextSibling();
                }

                if (child!=null && child.getNodeName().equalsIgnoreCase(sPrefix+":version-history-collection-set")){
                    child = child.getFirstChild();
                    while (child!=null){
                        result.add(child.getFirstChild().getNodeValue());
                        child = child.getNextSibling();
                    }
                }

                return result.elements();
            }

        }
        protected void setDocument(Document doc){
            responseDocument = doc;
        }
        protected void setResponseHashtable(Hashtable h){
            responseHashtable = h;
        }




private static class DummyErrorHandler implements ErrorHandler {


    /**
     * Receive notification of a warning.
     *
     * <p>SAX parsers will use this method to report conditions that
     * are not errors or fatal errors as defined by the XML 1.0
     * recommendation.  The default behaviour is to take no action.</p>
     *
     * <p>The SAX parser must continue to provide normal parsing events
     * after invoking this method: it should still be possible for the
     * application to process the document through to the end.</p>
     *
     * <p>Filters may use this method to report other, non-XML warnings
     * as well.</p>
     *
     * @param exception The warning information encapsulated in a
     *                  SAX parse exception.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.SAXParseException
     */
    public void warning(SAXParseException exception) throws SAXException
    {
        // System.out.println("warning: " + exception.getMessage());
    }

    /**
     * Receive notification of a recoverable error.
     *
     * <p>This corresponds to the definition of "error" in section 1.2
     * of the W3C XML 1.0 Recommendation.  For example, a validating
     * parser would use this callback to report the violation of a
     * validity constraint.  The default behaviour is to take no
     * action.</p>
     *
     * <p>The SAX parser must continue to provide normal parsing events
     * after invoking this method: it should still be possible for the
     * application to process the document through to the end.  If the
     * application cannot do so, then the parser should report a fatal
     * error even if the XML 1.0 recommendation does not require it to
     * do so.</p>
     *
     * <p>Filters may use this method to report other, non-XML errors
     * as well.</p>
     *
     * @param exception The error information encapsulated in a
     *                  SAX parse exception.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.SAXParseException
     */
    public void error(SAXParseException exception) throws SAXException
    {
        // System.out.println("error: " + exception.getMessage());
    }

    /**
     * Receive notification of a non-recoverable error.
     *
     * <p>This corresponds to the definition of "fatal error" in
     * section 1.2 of the W3C XML 1.0 Recommendation.  For example, a
     * parser would use this callback to report the violation of a
     * well-formedness constraint.</p>
     *
     * <p>The application must assume that the document is unusable
     * after the parser has invoked this method, and should continue
     * (if at all) only for the sake of collecting addition error
     * messages: in fact, SAX parsers are free to stop reporting any
     * other events once this method has been invoked.</p>
     *
     * @param exception The error information encapsulated in a
     *                  SAX parse exception.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.SAXParseException
     */
    public void fatalError(SAXParseException exception) throws SAXException
    {
         // System.out.println("fatal: " + exception.getMessage());
    }

}




}

