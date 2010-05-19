/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/properties/LockDiscoveryProperty.java,v 1.4.2.1 2004/10/11 08:17:20 luetzkendorf Exp $
 * $Revision: 1.4.2.1 $
 * $Date: 2004/10/11 08:17:20 $
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
import org.apache.webdav.lib.BaseProperty;
import org.apache.webdav.lib.Lock;
import org.apache.webdav.lib.ResponseEntity;
import org.apache.webdav.lib.methods.DepthSupport;
import org.apache.webdav.lib.util.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class represents a listing of who has lock, what type of lock he has,
 * the timeout type and the time remaining on the timeout, and the associated
 * lock token.  The server is free to withhold any or all of this information
 * if the requesting principal does not have sufficient access rights to see
 * the requested data.
 *
 * <!ELEMENT lockdiscovery (activelock)* >
 *
 * @version $Revision: 1.4.2.1 $
 */
public class LockDiscoveryProperty extends BaseProperty {


    // -------------------------------------------------------------- Constants


    /**
     * The property name.
     */
    public static final String TAG_NAME = "lockdiscovery";


    // ----------------------------------------------------------- Constructors


    /**
     * Default constructor for the property.
     */
    public LockDiscoveryProperty(ResponseEntity response, Element element) {
        super(response, element);
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Get the activelock in this lockdiscovery property.
     *
     * @return Lock[] A lock array or null when there is no lock.
     */
    public Lock[] getActiveLocks() {
        NodeList children = element.getChildNodes();
        if (children == null || children.getLength() == 0)
            return null;
        ArrayList locks = new ArrayList();
        for (int i = 0; i < children.getLength(); i++) {
            try {
                Element child = (Element) children.item(i);
                String namespace = DOMUtils.getElementNamespaceURI(child);
                if (namespace != null && namespace.equals("DAV:")) {
                    String localName = DOMUtils.getElementLocalName(child);
                    if ("activelock".equals(localName)) {
                        locks.add(parseLock(child));
                    }
                }
            } catch (ClassCastException e) {
            }
        }
        return (Lock[]) locks.toArray(new Lock[locks.size()]);
    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Parse a lock.
     */
    protected Lock parseLock(Element element) {

        int ls = -1;
        Element child = DOMUtils.getFirstElement(element, "DAV:", "lockscope");
        if (child != null) {
            Element lockScope =
                DOMUtils.getFirstElement(child, "DAV:", "exclusive");
            if (lockScope != null) {
                ls = Lock.SCOPE_EXCLUSIVE;
            }
            lockScope = DOMUtils.getFirstElement(child, "DAV:", "shared");
            if (lockScope != null) {
                ls = Lock.SCOPE_SHARED;
            }
        }

        int lt = -1;
        child = DOMUtils.getFirstElement(element, "DAV:", "locktype");
        if (child != null) {
            Element lockType = DOMUtils.getFirstElement(child, "DAV:", "write");
            if (lockType != null) {
                lt = Lock.TYPE_WRITE;
            } else {
                lockType = DOMUtils.getFirstElement(child, "DAV:", "transaction");
                if (lockType != null) {
                    lt = Lock.TYPE_TRANSACTION;
                }
            }
        }

        int d = -1;
        child = DOMUtils.getFirstElement(element, "DAV:", "depth");
        if (child != null) {
            String depth = DOMUtils.getTextValue(child);
            if (depth != null) {
                if ("0".equals(depth)) {
                    d = DepthSupport.DEPTH_0;
                } else if ("1".equals(depth)) {
                    d = DepthSupport.DEPTH_1;
                } else if ("infinity".equalsIgnoreCase(depth)) {
                    d = DepthSupport.DEPTH_INFINITY;
                } else {
                    try {
                        d = Integer.parseInt(depth);
                        if (d<0) {
                            d = -1; // unknown
                        }
                    } catch (NumberFormatException ex) {
                        d = -1; // unknown
                    }
                }
            }
        }

        String owner = null;
        child = DOMUtils.getFirstElement(element, "DAV:", "owner");
        owner = DOMUtils.getTextValue(child);

        int t = -1;
        child = DOMUtils.getFirstElement(element, "DAV:", "timeout");
        if (child != null) {
            String timeout = DOMUtils.getTextValue(child);
            int at = timeout.indexOf('-');
            if (at > 0) {
                try {
                    t = Integer.parseInt(timeout.substring(at + 1));
                } catch (NumberFormatException e) {
                }
            }
        }

        String lockToken = null;
        child = DOMUtils.getFirstElement(element, "DAV:", "locktoken");
        if (child != null) {
            Element href = DOMUtils.getFirstElement(child, "DAV:", "href");
            if (href != null) {
                lockToken = DOMUtils.getTextValue(href);
            }
        }
        
        String principalUrl = null;
        child = DOMUtils.getFirstElement(element, "DAV:", "principal-URL");
        if (child != null) {
            Element href = DOMUtils.getFirstElement(child, "DAV:", "href");
            if (href != null) {
                principalUrl = DOMUtils.getTextValue(href);
            }
        } 

        return new Lock(ls, lt, d, owner, t, lockToken, principalUrl);

    }

    public String getPropertyAsString() {
        Lock[] locks = getActiveLocks();

        if ((locks==null) || (locks.length==0))
            return "";

        StringBuffer tmp = new StringBuffer(locks[0].toString());
        for (int i=1; i<locks.length ; i++) {
            tmp.append(", ");
            tmp.append(locks[i].toString());
        }

        return tmp.toString();
    }

}

