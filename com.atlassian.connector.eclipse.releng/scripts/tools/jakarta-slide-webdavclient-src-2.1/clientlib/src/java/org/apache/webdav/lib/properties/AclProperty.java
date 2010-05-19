/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/properties/AclProperty.java,v 1.5 2004/08/02 15:45:50 unico Exp $
 * $Revision: 1.5 $
 * $Date: 2004/08/02 15:45:50 $
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

package org.apache.webdav.lib.properties;

import java.util.ArrayList;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.webdav.lib.Ace;
import org.apache.webdav.lib.BaseProperty;
import org.apache.webdav.lib.Privilege;
import org.apache.webdav.lib.ResponseEntity;
import org.apache.webdav.lib.util.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This interface models the <code>&lt;D:acl&gt;</code> property, which is
 * defined in the WebDAV Access Control Protocol specification.
 *
 * @version $Revision: 1.5 $
 */
public class AclProperty extends BaseProperty {


    // -------------------------------------------------------------- Constants


    /**
     * The property name.
     */
    public static final String TAG_NAME = "acl";


    // ----------------------------------------------------------- Constructors


    /**
     * Default constructor for the property.
     */
    public AclProperty(ResponseEntity response, Element element) {
        super(response, element);
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Returns the Aces present in this acl property.
     *
     * @return Ace[] An ace array or null when there is no ace.
     */
    public Ace[] getAces() {
        NodeList children = element.getChildNodes();
        if (children == null || children.getLength() == 0)
            return null;
        ArrayList aces = new ArrayList();
        for (int i = 0; i < children.getLength(); i++) {
            try {
                Element child = (Element) children.item(i);
                String namespace = DOMUtils.getElementNamespaceURI(child);
                if (namespace != null && namespace.equals("DAV:")) {
                    String localName = DOMUtils.getElementLocalName(child);
                    if ("ace".equals(localName)) {
                        aces.add(parseAce(child));
                    }
                }
            } catch (ClassCastException e) {
            }
        }
        return (Ace[]) aces.toArray(new Ace[aces.size()]);
    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Parse an ace.
     */
    protected Ace parseAce(Element element) {

        String principal = null;
        Element child = DOMUtils.getFirstElement(element, "DAV:", "principal");
        if (child == null) {
            System.err.println("Error: mandatory element <principal> is missing !");
            System.err.println("element: " + element);
            return null;
        }

        Element href = DOMUtils.getFirstElement(child, "DAV:", "href");
        if (href != null) {
            principal = DOMUtils.getTextValue(href);
            try {
                principal = URIUtil.decode(principal);
            } catch (URIException e) {
                System.err.println("Warning: decoding href element failed!");
                System.err.println("reason: " + e.getReason());
            }
        }

        String[] types={"all","authenticated","unauthenticated","property","self"};
        for (int i=0 ; i<types.length && principal==null ; i++)
        {
            Element type = DOMUtils.getFirstElement(child, "DAV:", types[i]);
            if (type!=null)
            {
                principal=types[i];
            }
        }

        if (principal==null)
        {
            System.err.println("Error: unknown type of principal");
            System.err.println("element: " + element);
            return null;
        }

        Ace ace = new Ace(principal);

        child = DOMUtils.getFirstElement(element, "DAV:", "grant");
        if (child == null) {
            child = DOMUtils.getFirstElement(element, "DAV:", "deny");
            ace.setNegative(true);
         }
        if (child != null) {
            NodeList privilegeElements = child.getElementsByTagNameNS("DAV:", "privilege");
            for (int i = 0; i < privilegeElements.getLength(); i++) {
                Element privilegeElement = (Element) privilegeElements.item(i);
                NodeList privileges = privilegeElement.getElementsByTagName("*");
                for (int j=0 ; j<privileges.getLength() ; j++)
                {
                    Element privilege = (Element) privileges.item(j);
                    ace.addPrivilege(parsePrivilege(privilege));
                }
            }
        }

        child = DOMUtils.getFirstElement(element, "DAV:", "inherited");
        if (child != null) {
            href = DOMUtils.getFirstElement(child, "DAV:", "href");
            String shref = null;
            if (href != null)
            {
                shref = DOMUtils.getTextValue(href);
                if (!shref.equals(response.getHref())) {
                    ace.setInherited(true);
                    ace.setInheritedFrom(shref);
                }
            }
            else
            {
                System.err.println("Error: mandatory element <href> is missing !");
                return null;
            }
        }

        child = DOMUtils.getFirstElement(element, "DAV:", "protected");
        if (child != null) {
            ace.setProtected(true);
        }

        return ace;

    }


    /**
     * Parse a privilege element.
     */
    protected Privilege parsePrivilege(Element privilegeElement) {
        return new Privilege(privilegeElement.getNamespaceURI(),
                             privilegeElement.getLocalName(), null);
    }

    public String getPropertyAsString() {
        Ace[] aces = getAces();

        if ((aces==null) || (aces.length==0))
            return "";

        StringBuffer tmp = new StringBuffer(aces[0].toString());
        for (int i=1; i<aces.length ; i++) {
            tmp.append(", ");
            tmp.append(aces[i].toString());
        }

        return tmp.toString();
    }

}
