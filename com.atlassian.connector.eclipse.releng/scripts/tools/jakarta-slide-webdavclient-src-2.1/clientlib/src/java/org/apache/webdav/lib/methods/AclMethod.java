/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/AclMethod.java,v 1.8 2004/08/02 15:45:48 unico Exp $
 * $Revision: 1.8 $
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
import org.apache.webdav.lib.Ace;
import org.apache.webdav.lib.Privilege;
import org.apache.webdav.lib.PropertyName;
import org.apache.webdav.lib.util.XMLPrinter;

/**
 * ACL Method.
 *
 */
public class AclMethod
    extends XMLResponseMethodBase {


    // -------------------------------------------------------------- Constants


    // ----------------------------------------------------------- Constructors


    /**
     * Method constructor.
     */
    public AclMethod() {
    }


    /**
     * Method constructor.
     */
    public AclMethod(String path) {
        super(path);
    }


    // ----------------------------------------------------- Instance Variables


    protected Vector aces = new Vector();


    // --------------------------------------------------------- Public Methods


    /**
     * Add an ace to the ace list which will be set by the method.
     *
     * @param ace Access control entry
     */
    public void addAce(Ace ace) {
        checkNotUsed();
        aces.addElement(ace);
    }


    // --------------------------------------------------- WebdavMethod Methods


    public void recycle() {
        super.recycle();
        aces.clear();
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
        printer.writeElement("D", "DAV:", "acl",
                             XMLPrinter.OPENING);

        Enumeration aceList = aces.elements();

        while (aceList.hasMoreElements()) {
            Ace ace = (Ace) aceList.nextElement();

            if (ace.isInherited() || ace.isProtected()) {
                // draft-ietf-webdav-acl-06
                // do not submit inherited and protected aces
                // continue with next ace
                continue;
            }

            printer.writeElement("D", null, "ace",
                                 XMLPrinter.OPENING);

            printer.writeElement("D", null, "principal",
                                 XMLPrinter.OPENING);

            boolean found = false;
            String principal = ace.getPrincipal();
            String[] types = {"all", "authenticated", "unauthenticated",
                              "property", "self"};
            for (int i = 0; i < types.length && !found; i++) {
                if (types[i].equals(principal)) {
                    found = true;
                    if ("property".equals(principal)) {
                        printer.writeElement("D", null, principal,
                                             XMLPrinter.OPENING);
                        PropertyName property = ace.getProperty();
                        String nsURI = property.getNamespaceURI();
                        if ("DAV:".equals(nsURI)) {
                            printer.writeElement("D", null,
                                                 property.getLocalName(),
                                                 XMLPrinter.NO_CONTENT);
                        } else {
                            printer.writeElement("Z", nsURI,
                                                 property.getLocalName(),
                                                 XMLPrinter.NO_CONTENT);
                        }
                        printer.writeElement("D", null, principal,
                                             XMLPrinter.CLOSING);
                    } else {
                        printer.writeElement("D", null, principal,
                                             XMLPrinter.NO_CONTENT);
                    }
                }
            }
            if (!found) {
                printer.writeElement("D", null, "href", XMLPrinter.OPENING);
                printer.writeText(principal);
                printer.writeElement("D", null, "href", XMLPrinter.CLOSING);
            }

            printer.writeElement("D", null, "principal",
                                 XMLPrinter.CLOSING);

            String positive = (ace.isNegative()) ? "deny" : "grant";

            printer.writeElement("D", null, positive,
                                 XMLPrinter.OPENING);

            Enumeration privilegeList = ace.enumeratePrivileges();
            while (privilegeList.hasMoreElements()) {
                Privilege privilege = (Privilege) privilegeList.nextElement();
                printer.writeElement("D", null, "privilege",
                                     XMLPrinter.OPENING);
                String nsURI = privilege.getNamespace();
                if ("DAV:".equals(nsURI)) {
                    printer.writeElement("D", null, privilege.getName(),
                                         XMLPrinter.NO_CONTENT);
                } else {
                    printer.writeElement("Z", nsURI, privilege.getName(),
                                         XMLPrinter.NO_CONTENT);
                }
                printer.writeElement("D", null, "privilege",
                                     XMLPrinter.CLOSING);
            }

            printer.writeElement("D", null, positive,
                                 XMLPrinter.CLOSING);

            printer.writeElement("D", null, "ace",
                                 XMLPrinter.CLOSING);

        }

        printer.writeElement("D", "acl", XMLPrinter.CLOSING);

        return printer.toString();
    }

    public String getName() {
        return "ACL";
    }
}
