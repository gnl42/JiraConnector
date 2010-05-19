/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/PropertyBodyHelper.java,v 1.4 2004/08/02 15:45:48 unico Exp $
 * $Revision: 1.4 $
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

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import org.apache.webdav.lib.PropertyName;
import org.apache.webdav.lib.util.XMLPrinter;

/**
 * This class manages an array of PropertyNames.
 * It is used to generate a <prop> tag section
 * in the body of a WebDAV method.
 *
 */
public class PropertyBodyHelper {

    protected PropertyName[] propertyNames;

    /**
     * Property names setter.
     * The enumeration may contain strings with or without a namespace prefix
     * but the preferred way is to provide PropertyName objects.
     *
     * @param propertyNames List of the property names
     */
    protected void setPropertyNames(Collection propertyNames) {
        Vector list = new Vector();
        Iterator propertyIterator = propertyNames.iterator();
        while (propertyIterator.hasNext()) {
            Object item = propertyIterator.next();

            if (item instanceof PropertyName) {
                list.add(item);

            } else if (item instanceof String) {
                String propertyName = (String) item;
                int length = propertyName.length();
                boolean found = false;
                int i = 1;
                while (!found && (i <= length)) {
                    char chr = propertyName.charAt(length - i);
                    if (!Character.isUnicodeIdentifierPart(chr)
                        && chr != '-'
                        && chr != '_'
                        && chr != '.') {
                        found = true;
                    } else {
                        i++;
                    }
                }
                if ((i == 1) || (i >= length)) {
                    list.add(new PropertyName("DAV:", propertyName));
                } else {
                    String namespace = propertyName.substring(0, length + 1 - i);
                    String localName = propertyName.substring(length + 1 - i);
                    list.add(new PropertyName(namespace, localName));
                }
            } else {
                // unknown type
                // ignore
            }
        }
        this.propertyNames =
            (PropertyName[]) list.toArray(new PropertyName[list.size()]);
    }

    /**
     * Writes the <D:prop> element to a XMLPrinter.
     * The element contains all properties from the
     * propertyNames array. Result is:
     * <D:prop>
     *   <D:displayname/>
     *   <D:creationdate/>
     *   ...
     * </D:prop>
     */
    protected void wirtePropElement(XMLPrinter printer) {
        if (propertyNames != null) {
            printer.writeElement("D", "prop", XMLPrinter.OPENING);
            for (int i = 0; i < propertyNames.length; i++) {
                String namespace = propertyNames[i].getNamespaceURI();
                String localname = propertyNames[i].getLocalName();
                if ("DAV:".equals(namespace)) {
                    printer.writeElement("D", localname, XMLPrinter.NO_CONTENT);
                } else {
                    printer.writeElement("ZZ", namespace, localname, XMLPrinter.NO_CONTENT);
                }
            }
            printer.writeElement("D", "prop", XMLPrinter.CLOSING);
        }
    }
}
