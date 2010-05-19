/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/properties/GetLastModifiedProperty.java,v 1.4.2.1 2004/09/26 14:19:20 luetzkendorf Exp $
 * $Revision: 1.4.2.1 $
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

import org.apache.webdav.lib.ResponseEntity;
import org.w3c.dom.Element;

/**
 * This interface models the <code>&lt;D:getlastmodified&gt;</code> property,
 * which indicates the last time the resource was modified.  It does not, as
 * the name might misleadingly imply, have anything to do with getting the most
 * recently modified property.
 *
 * @version $Revision: 1.4.2.1 $
 */
public class GetLastModifiedProperty extends DateProperty {


    // -------------------------------------------------------------- Constants


    /**
     * The property name.
     */
    public static final String TAG_NAME = "getlastmodified";


    /**
     * The standard date format for the last modified date, as specified in
     * the HTTP 1.1 specification (RFC 2068).
     * @deprecated
     */
    public static final String DATE_FORMAT = "EEE, d MMM yyyy kk:mm:ss z";


    // ----------------------------------------------------------- Constructors


    /**
     * Default constructor for the property.
     */
    public GetLastModifiedProperty(ResponseEntity response, Element element) {
        super(response, element);
    }

}
