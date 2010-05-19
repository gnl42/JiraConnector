/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/properties/SupportedLockProperty.java,v 1.4 2004/08/02 15:45:50 unico Exp $
 * $Revision: 1.4 $
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
import org.apache.webdav.lib.BaseProperty;
import org.apache.webdav.lib.Lock;
import org.apache.webdav.lib.ResponseEntity;
import org.apache.webdav.lib.util.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @version $Revision: 1.4 $
 */
public class SupportedLockProperty extends BaseProperty {


    // -------------------------------------------------------------- Constants


    /**
     * The property name.
     */
    public static final String TAG_NAME = "supportedlock";


    // ----------------------------------------------------------- Constructors


    /**
     * Default constructor for the property.
     */
    public SupportedLockProperty(ResponseEntity response, Element element) {
        super(response, element);
    }


    // --------------------------------------------------------- Public Methods



    /**
     * Get the lockentry in this supportedlock property.
     *
     * @return Lock[] A lock array or null when there is no lock.
     */
    public Lock[] getLockEntries() {
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
                    if ("lockentry".equals(localName)) {
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
            Element lockType =
                DOMUtils.getFirstElement(child, "DAV:", "write");
            if (lockType != null) {
                lt = Lock.TYPE_WRITE;
            }
        }

        return new Lock(ls, lt);
    }

    public String getPropertyAsString() {
        Lock[] locks = getLockEntries();

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


