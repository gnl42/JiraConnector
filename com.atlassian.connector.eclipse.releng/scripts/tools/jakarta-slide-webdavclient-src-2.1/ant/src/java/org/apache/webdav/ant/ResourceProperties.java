// vi: set ts=3 sw=3:
/* 
 * $Header: /home/cvs/jakarta-slide/webdavclient/ant/src/java/org/apache/webdav/ant/ResourceProperties.java,v 1.3.2.1 2004/08/15 13:01:15 luetzkendorf Exp $
 * $Revision: 1.3.2.1 $
 * $Date: 2004/08/15 13:01:15 $
 * ========================================================================
 * Copyright 2004 The Apache Software Foundation
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
 * ========================================================================
 */
package org.apache.webdav.ant;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;

import org.apache.webdav.lib.Property;
import org.apache.webdav.lib.PropertyName;
import org.apache.webdav.lib.methods.PropFindMethod;
import org.apache.webdav.lib.properties.GetLastModifiedProperty;
import org.apache.webdav.lib.properties.ResourceTypeProperty;

/**
 * Helper for holding properies of WebDAV resources retrieved by PROPFIND
 * requests.
 * 
 */
public class ResourceProperties {
   /** Maps resource URLs to lists of properties. */
   private Map resourceMap = new HashMap();
   
   
   public void storeProperties(PropFindMethod propFind)
      throws URIException
   {
      // for each content element, check resource type and classify
      for (Enumeration e = propFind.getAllResponseURLs(); e.hasMoreElements(); ) 
      {
         String href = (String) e.nextElement();
         URI uri = new URI(propFind.getURI(), href);
         
         String key = uri.toString();
         List properties = (List)this.resourceMap.get(key);
         if (properties == null) {
            properties = new ArrayList();
            this.resourceMap.put(key, properties);
         }
         for(Enumeration f = propFind.getResponseProperties(href); 
             f.hasMoreElements();) 
         {
            properties.add((Property)f.nextElement());
         }
      }
   }
   
   public Property getProperty(HttpURL baseUrl, 
                               String relative, 
                               PropertyName propertyName)
      throws URIException
   {
      HttpURL url = Utils.createHttpURL(baseUrl, relative);
      return getProperty(url.getURI(), propertyName);
   }
   
   public Property getProperty(String uri, PropertyName propertyName) 
   {
      List properties = (List)this.resourceMap.get(uri);
      if (properties != null) {
         for(Iterator i = properties.iterator(); i.hasNext();) {
            Property p = (Property)i.next();
            if (p.getLocalName().equals(propertyName.getLocalName()) &&
                p.getNamespaceURI().equals(propertyName.getNamespaceURI()))
            {
               return p;
            }
         }
      }
      return null;
   }

   public long getLastModified(String uri) {
      GetLastModifiedProperty p = 
            (GetLastModifiedProperty)getProperty(uri, Utils.GETLASTMODIFIED);
      if (p != null) {
         return p.getDate().getTime();
      } else {
         return 0;
      }
   }
   
   public ResourceTypeProperty getResourceType(HttpURL baseUrl, String relative)
      throws URIException
   {
      HttpURL url = Utils.createHttpURL(baseUrl, relative);
      return getResourceType(url.toString());
   }
   
   public ResourceTypeProperty getResourceType(String uri) {
      ResourceTypeProperty p = 
            (ResourceTypeProperty)getProperty(uri, Utils.RESOURCETYPE);
      return p; 
   }
}
