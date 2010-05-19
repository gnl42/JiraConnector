/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/Property.java,v 1.3 2004/07/28 09:31:39 ib Exp $
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

import org.w3c.dom.Element;

/**
 * This interface models a DAV property.
 *
 * @version $Revision: 1.3 $
 */
public interface Property {

    /**
     * This method returns the full name of the property.  Thus, for example,
     * calling this method on a property such as
     * <code>&lt;D:getlastmodified&gt;Tue, 05 Dec 2000
     * 05:25:02&lt;/D:getlastmodified&gt;</code> returns
     * <code>D:getlastmodified</code>.
     */
    public String getName();

    /**
     * This method returns the local name of the property.  Thus, for example,
     * calling this method on a property such as
     * <code>&lt;D:getlastmodified&gt;Tue, 05 Dec 2000
     * 05:25:02&lt;/D:getlastmodified&gt;</code> returns
     * <code>getlastmodified</code>.
     */
    public String getLocalName();

    /**
     * This method returns the namespace of the property.  Thus, for example,
     * calling this method on a property such as
     * <code>&lt;D:getlastmodified&gt;Tue, 05 Dec 2000
     * 05:25:02&lt;/D:getlastmodified&gt;</code> returns
     * <code>DAV:</code>.
     */
    public String getNamespaceURI();

    /**
     * This method returns the property as a DOM Element.
     */
    public Element getElement();

    /**
     * This method returns the namespace of the property.  Thus, for example,
     * calling this method on a property such as
     * <code>&lt;D:getlastmodified&gt;Tue, 05 Dec 2000
     * 05:25:02&lt;/D:getlastmodified&gt;</code> returns
     * <code>Tue, 05 Dec 2000 05:25:02</code>.
     */
    public String getPropertyAsString();

    /**
     * This method returns the status code associated with the property.
     */
    public int getStatusCode();

    /**
     * This method returns URL file path of the resource to which this
     * property belongs.
     */
    public String getOwningURL();
}
