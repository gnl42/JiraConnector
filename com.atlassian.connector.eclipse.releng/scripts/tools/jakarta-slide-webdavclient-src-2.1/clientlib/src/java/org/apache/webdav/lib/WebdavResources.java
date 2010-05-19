/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/WebdavResources.java,v 1.5 2004/07/28 09:31:38 ib Exp $
 * $Revision: 1.5 $
 * $Date: 2004/07/28 09:31:38 $
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
import java.util.Hashtable;

/**
 * This WebdavResources class represents a set of {@link WebdavResource WebDAV resources }.
 *
 * Two WebdavResource instances are considered equal, if there {@link WebdavResource#getName()
 * getName()} method returns the same string.
 *
 */

public class WebdavResources {

    // --------------------------------------------------------- Constructors

    /**
     * Default constuctor.
     */
    public WebdavResources() {
    }


    /**
     * This constuctor.
     *
     * @param resource A resource to add.
     */
    public WebdavResources(WebdavResource resource) {
        addResource(resource);
    }


    // --------------------------------------------------- Instance Variables


    /**
     * The resources for each href and its properties.
     */
    Hashtable hrefTable = new Hashtable();


    // ------------------------------------------------------- Public Methods


    /**
     * Test if there is no resource.
     *
     * @return true if it's empty.
     */
    public boolean isEmpty() {
        return hrefTable.isEmpty();
    }


    /**
     * Test if there is a resource called the specified resource name.
     *
     * @param resourceName The resource name to check.
     */
    public boolean isThereResourceName(String resourceName) {
        return hrefTable.containsKey(resourceName);
    }


    /**
     * Test if there is a resource.
     *
     * @param resource The specified resource.
     * @return true if it exists.
     */
    public boolean isThereResource(WebdavResource resource) {
        return hrefTable.contains(resource);
    }


    /**
     * Get an enumeration of the resource names.
     *
     * @return An enumeration of the resource names.
     */
    public Enumeration getResourceNames() {
        return hrefTable.keys();
    }


    /**
     * Get an enumeration of the resources.
     *
     * @return An enumeration of resources.
     */
    public Enumeration getResources() {
        return hrefTable.elements();
    }


    /**
     * Get an array of resource names.
     *
     * @return An array of resource names.
     */
    public String[] list() {

        synchronized (hrefTable) {
            int num = hrefTable.size();
            String resourceNames[] = new String[num];

            Enumeration resources = getResourceNames();
            int i = 0;
            while (resources.hasMoreElements()) {
                resourceNames[i++] = (String) resources.nextElement();
            }

            return resourceNames;
        }
    }


    /**
     * Get an arraay of resources.
     *
     * @return An array of resources.
     */
    public WebdavResource[] listResources() {

        synchronized (hrefTable) {
            int num = hrefTable.size();
            WebdavResource WebdavResources[] = new WebdavResource[num];

            Enumeration resources = getResources();
            int i = 0;
            while (resources.hasMoreElements()) {
                WebdavResources[i++] =
                    (WebdavResource) resources.nextElement();
            }

            return WebdavResources;
        }
    }


    /**
     * Get an resource.
     *
     * @param resourceName The resource name.
     * @return The wanted resource if it exists.
     */
    public WebdavResource getResource(String resourceName) {
        return (WebdavResource) hrefTable.get(resourceName);
    }


    /**
     * Maps the resource name to its resource.
     * The resource name could be different from the displayname property.
     * It's useful for representing itself or parent collection.
     *
     * @param resourceName The resource name.
     * @param resource The resource.
     * @see #addResource(WebdavResource)
     */
    public void addResource(String resourceName, WebdavResource resource) {
        hrefTable.put(resourceName, resource);
    }


    /**
     * Add the specified resource.
     *
     * @param resource The resource to add.
     */
    public void addResource(WebdavResource resource) {
        hrefTable.put(resource.getName(), resource);
    }


    /**
     * Remove the specified resource name.
     *
     * @param resourceName The specified resource name.
     * @return The wanted resource.
     */
    public WebdavResource removeResource(String resourceName) {
        return (WebdavResource) hrefTable.remove(resourceName);
    }


    /**
     * Remove all resources.
     */
    public void removeAll() {
        hrefTable.clear();
    }


    /**
     * Return the string for this class.
     */
    public String toString() {
        return hrefTable.toString();
    }

}






