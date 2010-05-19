/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/ResponseEntity.java,v 1.3 2004/07/28 09:31:39 ib Exp $
 * $Revision: 1.3 $
 * $Date: 2004/07/28 09:31:39 $
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


package org.apache.webdav.lib;


import java.util.Enumeration;

/**
 * The interface for the response entity body formats that provide
 * operations for the XML response documents.
 *
 * @version $Revision: 1.3 $ $Date: 2004/07/28 09:31:39 $
 */

public interface ResponseEntity {

    /**
     * Get the href string in the response XML element.
     *
     * Each response XML element MUST contain an href XML element that gives
     * the URI of the resource on which the properties in the prop XML
     * element are defined.
     *
     * @return the href string.
     */
    public String getHref();


    /**
     * Get the status code for use with 207 (Multi-Status).
     *
     * Unless explicitly prohibited any 2/3/4/5xx series
     * response code may be used in a Multi-Status response.
     *
     * @return the status code.
     */
    public int getStatusCode();


    /**
     * Get the properties in the response XML element.
     *
     * @return the properties.
     */
    public Enumeration getProperties();

    /**
     * Get the properties in the response XML element.
     *
     * @return the properties.
     */
    public Enumeration getHistories();

    /**
     * Get the properties in the response XML element.
     *
     * @return the properties.
     */
    public Enumeration getWorkspaces();

}
