/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/properties/HrefValuedProperty.java,v 1.1.2.1 2004/09/26 14:19:20 luetzkendorf Exp $
 * $Revision: 1.1.2.1 $
 * $Date: 2004/09/26 14:19:20 $
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

import org.apache.webdav.lib.BaseProperty;
import org.apache.webdav.lib.ResponseEntity;
import org.apache.webdav.lib.util.DOMUtils;
import org.w3c.dom.Element;

/**
 * Base classe for properties with a single <code>href</code> value.
 */
public class HrefValuedProperty extends BaseProperty {


    // -------------------------------------------------------------- Constants

    // ----------------------------------------------------------- Constructors

    /**
     * Default constructor for the property.
     */
    public HrefValuedProperty(ResponseEntity response, Element element) {
        super(response, element);
    }


    // --------------------------------------------------------- Public Methods

    /**
     * Returns the value of the href element.
     */
    public String getHref() {
        String hrefValue="";
        Element href = DOMUtils.getFirstElement(element, "DAV:", "href");
        if (href!=null)
        {
            hrefValue = DOMUtils.getTextValue(href);
        }
        return hrefValue;
    }

    public String getPropertyAsString() {
        return getHref();
    }

    public String toString() {
        return getHref();
    }

}
