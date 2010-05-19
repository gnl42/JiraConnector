/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/PropPatchMethod.java,v 1.6 2004/08/02 15:45:48 unico Exp $
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
import java.util.Enumeration;
import java.util.Hashtable;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.webdav.lib.util.WebdavStatus;
import org.apache.webdav.lib.util.XMLPrinter;


/**
 * PROPPATCH Method.
 *
 */
public class PropPatchMethod
    extends XMLResponseMethodBase {


    // ----------------------------------------------------------- Constructors


    /**
     * Method constructor.
     */
    public PropPatchMethod() {
    }


    /**
     * Method constructor.
     */
    public PropPatchMethod(String path) {
        super(path);
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Hashtable of the properties to set.
     */
    protected Hashtable toSet = new Hashtable();


    /**
     * Hashtable of the properties to remove.
     */
    protected Hashtable toRemove = new Hashtable();


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new property to set.
     *
     * @param name Property name
     * @param value Property value
     */
    public void addPropertyToSet(String name, String value) {
        checkNotUsed();
        Property propertyToSet = new Property();
        if (name != null) {
            propertyToSet.name = name;
            if (value != null)
                propertyToSet.value = value;
            else
                propertyToSet.value = "";
            toSet.put(name, propertyToSet);
        }
    }


    /**
     * Add a new property to set.
     *
     * @param name Property name
     * @param value Property value
     * @param namespace Namespace abbreviation
     * @param namespaceInfo Namespace information
     */
    public void addPropertyToSet(String name, String value, String namespace,
                                 String namespaceInfo) {
        checkNotUsed();
        Property propertyToSet = new Property();
        if (name != null) {
            propertyToSet.name = name;
            if (value != null)
                propertyToSet.value = value;
            else
                propertyToSet.value = "";
            propertyToSet.namespace = namespace;
            propertyToSet.namespaceInfo = namespaceInfo;
            toSet.put(namespace + name, propertyToSet);
        }
    }


    /**
     * Add property to remove.
     *
     * @param name Property name
     */
    public void addPropertyToRemove(String name) {
        checkNotUsed();
        Property propertyToRemove = new Property();
        if (name != null) {
            propertyToRemove.name = name;
            toRemove.put(name, propertyToRemove);
        }
    }


    /**
     * Add property to remove.
     *
     * @param name Property name
     * @param namespace Namespace abbreviation
     * @param namespaceInfo Namespace information
     */
    public void addPropertyToRemove(String name, String namespace,
                                    String namespaceInfo) {
        checkNotUsed();
        Property propertyToRemove = new Property();
        if (name != null) {
            propertyToRemove.name = name;
            propertyToRemove.namespace = namespace;
            propertyToRemove.namespaceInfo = namespaceInfo;
            toRemove.put(name, propertyToRemove);
        }
    }


    // --------------------------------------------------- WebdavMethod Methods


    public String getName() {
        return "PROPPATCH";
    }

    /**
     * Generate additional headers needed by the request.
     *
     * @param state State token
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
        XMLPrinter printer = new XMLPrinter();


        printer.writeXMLHeader();
        printer.writeElement("D", "DAV:", "propertyupdate",
                             XMLPrinter.OPENING);

        if (toSet.size() > 0) {

            printer.writeElement("D", null, "set", XMLPrinter.OPENING);

            Enumeration toSetList = toSet.elements();
            printer.writeElement("D", null, "prop", XMLPrinter.OPENING);
            while (toSetList.hasMoreElements()) {
                Property current = (Property) toSetList.nextElement();
                if ("DAV:".equals(current.namespaceInfo)) {
                    printer.writeProperty("D", null, current.name, current.value);
                }
                else {
                    printer.writeProperty(current.namespace, current.namespaceInfo,
                                      current.name, current.value);
                }
            }
            printer.writeElement("D", null, "prop", XMLPrinter.CLOSING);

            printer.writeElement("D", null, "set", XMLPrinter.CLOSING);

        }

        if (toRemove.size() > 0) {

            printer.writeElement("D", null, "remove", XMLPrinter.OPENING);

            Enumeration toRemoveList = toRemove.elements();
            printer.writeElement("D", null, "prop", XMLPrinter.OPENING);
            while (toRemoveList.hasMoreElements()) {
                Property current = (Property) toRemoveList.nextElement();
                printer.writeElement(current.namespace, current.namespaceInfo,
                                     current.name, XMLPrinter.NO_CONTENT);
            }
            printer.writeElement("D", null, "prop", XMLPrinter.CLOSING);

            printer.writeElement("D", null, "remove", XMLPrinter.CLOSING);

        }

        printer.writeElement("D", "propertyupdate", XMLPrinter.CLOSING);

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


    // --------------------------------------------------- Property Inner Class


    private class Property {

        public String name = "";
        public String namespace;
        public String namespaceInfo;
        public String value;

    }


}
