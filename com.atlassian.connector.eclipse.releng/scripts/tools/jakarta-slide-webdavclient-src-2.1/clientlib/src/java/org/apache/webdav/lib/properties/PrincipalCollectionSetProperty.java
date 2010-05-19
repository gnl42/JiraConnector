/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/properties/PrincipalCollectionSetProperty.java,v 1.4 2004/08/02 15:45:50 unico Exp $
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

import java.util.Vector;
import org.apache.webdav.lib.BaseProperty;
import org.apache.webdav.lib.ResponseEntity;
import org.apache.webdav.lib.util.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class models the <code>&lt;D:principal-collection-set&gt;</code> property, which is
 * defined in the WebDAV Access Control Protocol specification.
 *
 * @version $Revision: 1.4 $
 */
public class PrincipalCollectionSetProperty extends BaseProperty {

    /**
     * The property name.
     */
    public static final String TAG_NAME = "principal-collection-set";

    // ----------------------------------------------------------- Constructors

    /**
     * Default constructor for the property.
     */
    public PrincipalCollectionSetProperty(ResponseEntity response, Element element) {
        super(response, element);
    }


    // --------------------------------------------------------- Public Methods

    private String[] hrefs=null;

    private void init()
    {
        // check if already initialized
        if (this.hrefs!=null)
            return;

        Vector hrefVector = new Vector();
        NodeList hrefNodes = DOMUtils.getElementsByTagNameNS(element, "href", "DAV:");
        if (hrefNodes!=null)
        {
            for (int i = 0; i < hrefNodes.getLength(); i++)
            {
                Node hrefNode = hrefNodes.item(i);
                String href = DOMUtils.getTextValue(hrefNode);
                if ((href!=null) && (href.length()>0))
                    hrefVector.add(href);
            }
        }

        this.hrefs=(String[]) hrefVector.toArray(new String[hrefVector.size()]);
    }

    /**
     * Returns the Hrefs present in this principal-collection-set property.
     *
     * @return String[] A href array or null when there are no href.
     */
    public String[] getHrefs()
    {
        init();
        return this.hrefs;
    }

    public String getPropertyAsString() {
        String[] hrefs = getHrefs();

        if ((hrefs==null) || (hrefs.length==0))
            return "";

        StringBuffer tmp = new StringBuffer(hrefs[0]);
        for (int i=1; i<hrefs.length ; i++) {
            tmp.append(", ");
            tmp.append(hrefs[i]);
        }

        return tmp.toString();
    }

}
