/* 
 * $Header: /home/cvs/jakarta-slide/webdavclient/ant/src/java/org/apache/webdav/ant/CollectionScanner.java,v 1.3.2.2 2004/08/22 10:36:47 luetzkendorf Exp $
 * $Revision: 1.3.2.2 $
 * $Date: 2004/08/22 10:36:47 $
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.URIException;

import org.apache.tools.ant.BuildException;
import org.apache.webdav.lib.PropertyName;
import org.apache.webdav.lib.methods.DepthSupport;
import org.apache.webdav.lib.methods.PropFindMethod;
import org.apache.webdav.lib.properties.ResourceTypeProperty;

/**
 * Scan a collection of WebDAV resources to find ones that match a 
 * specified pattern.
 *
 */
public class CollectionScanner extends Scanner {

    private HttpURL baseURL = null;
    private HttpClient client = null;
    private ResourceProperties properties = null;

    private Vector propertyNames = new Vector();

    public CollectionScanner() {
       propertyNames.add(Utils.RESOURCETYPE);
       propertyNames.add(Utils.GETLASTMODIFIED);
    }

    /**
     * Scans the base URL for resources that match at least one include
     * pattern, and don't match any exclude patterns.
     * 
     * For each resource the properties are stored and may be used after
     * scanning - may be for further selecting. (see {@link #getProperties()}
     * and {@link #addProperty(PropertyName)}).
     *
     * @exception IllegalStateException when baseurl was set incorrecly
     * @exception ScanException when a WebDAV or other error occurs
     */
    public void scan() {

        if (baseURL == null) {
           throw new IllegalStateException(
                 "BaseURL must be set before calling the scan() method");
        }

        // initialize member variables
        filesIncluded = new ArrayList();
        filesExcluded = new ArrayList();
        filesNotIncluded = new ArrayList();
        dirsIncluded = new ArrayList();
        dirsExcluded = new ArrayList();
        dirsNotIncluded = new ArrayList();
        this.properties = new ResourceProperties();
        
        try {
           readCollection(baseURL);
        } 
//        catch (IOException e) {
//            throw new ScanException(e.getMessage(), e);
//        }
        catch (Exception e) {
           e.printStackTrace();
        }
    }

   protected void readCollection(HttpURL collURL) 
         throws URIException
   {
      if (!collURL.getPath().endsWith(SEPARATOR)) {
         collURL = Utils.createHttpURL(collURL, "");
         collURL.setPath(collURL.getPath() + SEPARATOR);
      }
      
      // get a list of all resources from the given URL
      PropFindMethod propFind = new PropFindMethod(collURL.getURI(),
                                                   DepthSupport.DEPTH_1,
                                                   PropFindMethod.BY_NAME);
      propFind.setPropertyNames(propertyNames.elements());
      propFind.setFollowRedirects(true);
      try {
         this.client.executeMethod(propFind);
      } 
      catch (IOException e) {
         Utils.makeBuildException("Can't read collection content!", e);
      }
      
      List subCollections = new ArrayList();
      this.properties.storeProperties(propFind);
      
      // this collection
      addResource(collURL.getPath(), true);
      
      // for each content element, check resource type and classify
      for (Enumeration e = propFind.getAllResponseURLs(); e.hasMoreElements(); ) 
      {
         String href = (String) e.nextElement();
         
         ResourceTypeProperty property = 
                                this.properties.getResourceType(collURL, href);
         
         if (property != null) {
            if (property.isCollection()) {
               if (!href.endsWith(SEPARATOR)) href = href + SEPARATOR;
               // the collection URL itself may be in the list of 
               // response URL; filter them out to avoid recursion 
               HttpURL sub = Utils.createHttpURL(collURL, href);
               if (!sub.equals(collURL)) {
                  subCollections.add(Utils.createHttpURL(collURL, href));
               }
            } else {
               addResource(href, false);
            }
         } else {
            throw new BuildException("Can't determine resourcetype.");
         }
      }
      
      // read all sub collections
      for(Iterator i = subCollections.iterator(); i.hasNext();) {
         readCollection((HttpURL)i.next());
      }
   }

   protected void addResource(String href, boolean isCollection)
            throws ScanException 
   {
      try {
         String path = (Utils.createHttpURL(getBaseURL(), href)).getPath();
         String relPath = path.substring(getBaseURL().getPath().length());
         if (relPath.startsWith(SEPARATOR)) {
            relPath = relPath.substring(1);
         }
         if (isCollection) {
            if (isIncluded(relPath)) {
               if (isExcluded(relPath)) {
                  dirsExcluded.add(relPath);
               } else {
                  dirsIncluded.add(relPath);
               }
            } else {
               dirsNotIncluded.add(relPath);
            }
         } else {
            if (isIncluded(relPath)) {
               if (isExcluded(relPath)) {
                  filesExcluded.add(relPath);
               } else {
                  filesIncluded.add(relPath);
               }
           } else {
               filesNotIncluded.add(relPath);
           }
         }
       } 
       catch (URIException e) {
          throw new ScanException(
             "The XML response returned an invalid URL: " + e.getMessage(), e);
       }
    }

    public HttpURL getBaseURL() {
        return this.baseURL;
    }

    public void setBaseURL(HttpURL baseURL) {
        this.baseURL = baseURL;
    }

    public void setHttpClient(HttpClient client) {
        this.client = client;
    }
    
    public ResourceProperties getProperties()
    {
       return this.properties;
    }

    /**
     * Adds a property which the scanner retrieves while scanning.
     * 
     * @param property Name of the property to be retrieved.
     */
    public void addProperty(PropertyName property) {
       if (property == null) throw new NullPointerException();
       this.propertyNames.add(property);
    }
}
