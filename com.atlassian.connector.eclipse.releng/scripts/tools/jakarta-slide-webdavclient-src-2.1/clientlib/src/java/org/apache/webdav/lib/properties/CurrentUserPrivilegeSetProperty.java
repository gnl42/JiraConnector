/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/properties/CurrentUserPrivilegeSetProperty.java,v 1.4 2004/08/02 15:45:50 unico Exp $
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

import org.apache.webdav.lib.BaseProperty;
import org.apache.webdav.lib.ResponseEntity;
import org.apache.webdav.lib.util.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Title:        CurrentUserPrivilegeSetProperty.java
 * Description:
 * Company:      SpeedLegal Holdings Inc.
 * @version 1.0
 */


public class CurrentUserPrivilegeSetProperty extends BaseProperty {


    // -------------------------------------------------------------- Constants


    /**
     * The property name.
     */
    public static final String TAG_NAME = "current-user-privilege-set";


    // ----------------------------------------------------------- Constructors


    /**
     * Default constructor for the property.
     */
    public CurrentUserPrivilegeSetProperty
        (ResponseEntity response, Element element) {
        super(response, element);
    }


    // --------------------------------------------------------- Public Methods


    public boolean hasReadAccess()  {
        NodeList readPrivilege = DOMUtils.getElementsByTagNameNS(getElement(), "read", "DAV:");
        return (readPrivilege.getLength() == 1);
    }

    public boolean hasWriteAccess()  {
        NodeList writePrivilege = DOMUtils.getElementsByTagNameNS(getElement(), "write", "DAV:");

        return (writePrivilege.getLength() == 1);
    }

    public boolean hasReadWriteAccess() {
        return (hasReadAccess() && hasWriteAccess());
    }


    public String getPropertyAsString() {
        String theResult="";
        theResult = (hasReadAccess()) ? "Read" : theResult;
        theResult = (hasWriteAccess()) ? theResult+" Write" : theResult;
        return theResult.trim();
    }
}




