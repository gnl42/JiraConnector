/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/BaseProperty.java,v 1.5 2004/08/02 15:45:49 unico Exp $
 * $Revision: 1.5 $
 * $Date: 2004/08/02 15:45:49 $
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

import java.io.StringWriter;
import java.util.List;

import org.apache.webdav.lib.util.DOMUtils;
import org.apache.webdav.lib.util.PropertyWriter;
import org.jdom.input.DOMBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Element;

/**
 * This interface models a DAV property.
 *
 * @version $Revision: 1.5 $
 */
public class BaseProperty implements Property {


    // ----------------------------------------------------------- Constructors


    /**
     * Default constructor for the property.
     */
    public BaseProperty(ResponseEntity response, Element element) {
        this.element = element;
        this.response = response;
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Associated response entity.
     */
    protected ResponseEntity response;


    /**
     * Associated node element.
     */
    protected Element element;


    // --------------------------------------------------------- Public Methods


    /**
     * This method returns the full name of the property.  Thus, for example,
     * calling this method on a property such as
     * <code>&lt;D:getlastmodified&gt;Tue, 05 Dec 2000
     * 05:25:02&lt;/D:getlastmodified&gt;</code> returns
     * <code>D:getlastmodified</code>.
     */
    public String getName() {
        return element.getTagName();
    }


    /**
     * This method returns the local name of the property.  Thus, for example,
     * calling this method on a property such as
     * <code>&lt;D:getlastmodified&gt;Tue, 05 Dec 2000
     * 05:25:02&lt;/D:getlastmodified&gt;</code> returns
     * <code>getlastmodified</code>.
     */
    public String getLocalName() {
        return DOMUtils.getElementLocalName(element);
    }


    /**
     * This method returns the namespace of the property.  Thus, for example,
     * calling this method on a property such as
     * <code>&lt;D:getlastmodified&gt;Tue, 05 Dec 2000
     * 05:25:02&lt;/D:getlastmodified&gt;</code> returns
     * <code>DAV:</code>.
     */
    public String getNamespaceURI() {
        return DOMUtils.getElementNamespaceURI(element);
    }


    /**
     * This method returns the property as a DOM Element.
     */
    public Element getElement() {
        return element;
    }


    /**
     * This method returns the value of the property.  Thus, for example,
     * calling this method on a property such as
     * <code>&lt;D:getlastmodified&gt;Tue, 05 Dec 2000
     * 05:25:02&lt;/D:getlastmodified&gt;</code> returns
     * <code>Tue, 05 Dec 2000 05:25:02</code>.<br/>
     * Note: Mixed content (text and xml together) will not be returned
     * accurately.
     */
    public String getPropertyAsString() {
    	StringBuffer text = new StringBuffer();
        DOMBuilder builder = new DOMBuilder();
        XMLOutputter outputter = new XMLOutputter( Format.getPrettyFormat() );
        org.jdom.Element e = builder.build( element );
        List children = e.getChildren();
        if ( children.size() > 0 ) {
        	text.append( outputter.outputString( children ) );
        }
        text.append( e.getTextTrim() );
        return text.toString();
    }


    /**
     * This method returns the status code associated with the property.
     */
    public int getStatusCode() {
        // A response can have multiple propstat elements each with
        // their own status, return the (mandatory) status before asking
        // the resoponse for its status

        //  <multistatus xmlns=\DAV:\>
        //    <response>
        //      <href>/slide/files/</href>
        //      <propstat>
        //        <prop><displayname>files</displayname></prop>
        //        <status>HTTP/1.1 200 OK</status>
        //      </propstat>
        //      <propstat>
        //        <prop><displayname>files</displayname></prop>
        //        <status>HTTP/1.1 200 OK</status>
        //      </propstat>
        //    </response>
        //  </multistatus>

        Element status = DOMUtils.getFirstElement(element.getParentNode().getParentNode(),"DAV:", "status");
        if (status != null) {
            return DOMUtils.parseStatus(DOMUtils.getTextValue(status));
        }

        return response.getStatusCode();
    }


    /**
     * This method returns URL file path of the resource to which this
     * property belongs.
     */
    public String getOwningURL() {
        return response.getHref();
    }


    /**
     * Get a String representation of the property.
     */
    public String toString () {
        StringWriter tmp = new StringWriter();
        PropertyWriter propertyWriter = new PropertyWriter(tmp, true);
        propertyWriter.print(element);
        return tmp.getBuffer().toString();
    }


}
