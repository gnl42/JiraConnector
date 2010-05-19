// vi: set ts=3 sw=3:
/* 
 * $Header: /home/cvs/jakarta-slide/webdavclient/ant/src/java/org/apache/webdav/ant/Utils.java,v 1.7.2.2 2004/08/22 10:36:47 luetzkendorf Exp $
 * $Revision: 1.7.2.2 $
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
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PutMethod;

import org.apache.tools.ant.BuildException;
import org.apache.webdav.lib.Property;
import org.apache.webdav.lib.PropertyName;
import org.apache.webdav.lib.ResponseEntity;
import org.apache.webdav.lib.WebdavException;
import org.apache.webdav.lib.methods.CopyMethod;
import org.apache.webdav.lib.methods.LockMethod;
import org.apache.webdav.lib.methods.MkcolMethod;
import org.apache.webdav.lib.methods.MoveMethod;
import org.apache.webdav.lib.methods.PropFindMethod;
import org.apache.webdav.lib.methods.UnlockMethod;
import org.apache.webdav.lib.properties.ResourceTypeProperty;
import org.apache.webdav.lib.util.WebdavStatus;

/**
 */
public class Utils {
   public static final String DAV_NAMESPACE= "DAV:";
   
   public static final PropertyName DISPLAYNAME = new PropertyName(
         DAV_NAMESPACE, "displayname"); 

   public static final PropertyName GETLASTMODIFIED = new PropertyName(
         DAV_NAMESPACE, "getlastmodified"); 
   
   public static final DateFormat GETLASTMODIFIED_FORMAT = 
            new SimpleDateFormat("EEE, d MMM yyyy kk:mm:ss z", Locale.US);

   public static final PropertyName RESOURCETYPE = new PropertyName(
         DAV_NAMESPACE, "resourcetype"); 

   /**
    * Lookup for given property in a propfind response.
    * @param propFind the method, must always be rxecuted
    * @param name
    * @param path path of the item for which the property is searched for
    * @return the property of <code>null</code> if not found
    */
   public static Property findProperty(PropFindMethod propFind, 
                                        PropertyName name,
                                        String path) 
   {
      Enumeration e = propFind.getResponseProperties(path);
      Property p = findProperty(e, name);
      // a collection requested as /a/col/path/ may be as 
      // /a/col/path in the response
      if (p == null && path.endsWith("/")) {
         e = propFind.getResponseProperties(path.substring(0, path.length()-1));
         p = findProperty(e, name);
      }
      return p;
   }
   
   /**
    * Searches in the enumeration of Propery objects for a property with the 
    * given name. 
    * @param e enumeration of Property objects.
    * @param name
    * @return the property searched for of <code>null</code> if not found.
    */
   public static Property findProperty(Enumeration e, PropertyName name) {
      while (e.hasMoreElements()) {
         Property p = (Property)e.nextElement();
         
         if (p.getNamespaceURI().equals(name.getNamespaceURI()) &&
             p.getLocalName().equals(name.getLocalName())) 
         {
            return p;
         }
      }
      return null;
   }
   
   /**
    * Returns <code>true</code> if the resource given as URL does exist.
    * @param client
    * @param httpURL
    * @return <code>true</code>if the resource exists
    * @throws IOException
    * @throws HttpException
    */
   public static boolean resourceExists(HttpClient client, HttpURL httpURL)
      throws IOException, HttpException
   {
      HeadMethod head = new HeadMethod(httpURL.getURI());
      head.setFollowRedirects(true);
      int status = client.executeMethod(head);
      
      switch (status) {
         case WebdavStatus.SC_OK:
            return true;
         case WebdavStatus.SC_NOT_FOUND:
            return false;
         default:
            HttpException ex = new HttpException();
            ex.setReasonCode(status);
            ex.setReason(head.getStatusText());
            throw ex;
      }
   }
   
   public static boolean collectionExists(HttpClient client, HttpURL httpURL)
      throws IOException, HttpException
   {
      Vector props = new Vector(1);
      props.add(RESOURCETYPE);
      PropFindMethod propFind = new PropFindMethod(httpURL.getURI(), 
                                                    0, PropFindMethod.BY_NAME);
      propFind.setFollowRedirects(true);
      propFind.setPropertyNames(props.elements());
      int status = client.executeMethod(propFind);
      switch (status) {
         case WebdavStatus.SC_MULTI_STATUS:
            Property p = findProperty(propFind, RESOURCETYPE, httpURL.getPath());
            if (p instanceof ResourceTypeProperty) {
               return ((ResourceTypeProperty)p).isCollection();
            } else {
               throw new WebdavException("PROPFFIND does not return resourcetype");
            }
         case WebdavStatus.SC_NOT_FOUND:
            return false;
         default:
            HttpException ex = new HttpException();
            ex.setReasonCode(status);
            ex.setReason(propFind.getStatusText());
            throw ex;
      }
   }
      
   public static long getLastModified(HttpClient client, HttpURL url)
      throws IOException, HttpException
   {
      Vector props = new Vector(1);
      props.add(GETLASTMODIFIED);
      PropFindMethod propFind = new PropFindMethod(url.getURI(), 0);
      propFind.setPropertyNames(props.elements());
      propFind.setFollowRedirects(true);
      
      int status = client.executeMethod(propFind);
      switch (status) {
         case WebdavStatus.SC_MULTI_STATUS:
            Property p = findProperty(propFind, GETLASTMODIFIED, url.getPath());
            if (p != null) {
               try {
                  Date d = GETLASTMODIFIED_FORMAT.parse(p.getPropertyAsString());
                  return d.getTime();
               } 
               catch (ParseException e) {
                  throw new HttpException("Invalid lastmodified property: " +
                        p.getPropertyAsString());
               }
            } 
            throw new HttpException("PROPFIND does not return lastmodified.");
         default:
            HttpException ex = new HttpException();
            ex.setReasonCode(status);
            ex.setReason(propFind.getStatusText());
            throw ex;
      }
   }
   
   /**
    * 
    * @param client
    * @param httpURL
    * @param lockToken the locktoken to be used or <code>null</code> if 
    *         none is to be used
    * @throws IOException
    * @throws HttpException
    */
   public static boolean assureExistingCollection(HttpClient client, 
                                                  HttpURL httpURL,
                                                  String lockToken)
      throws IOException, HttpException
   {
      String path = httpURL.getPath();
      if (!path.endsWith("/")) {
         path = path + "/";
      }
      Stack toBeCreated = new Stack();
      
      while (!path.equals("/")) {
         HttpURL parent = Utils.createHttpURL(httpURL, path);
         if (!collectionExists(client, parent)) {
            toBeCreated.push(path);
            path = path.substring(0, path.lastIndexOf("/", path.length()-2)+1);
         } else {
            break;
         }
      }

      boolean created = !toBeCreated.empty(); 
      while(!toBeCreated.empty()) {
         HttpURL newColl = Utils.createHttpURL(httpURL, (String)toBeCreated.pop());
         MkcolMethod mkcol = new MkcolMethod(newColl.getURI());
         mkcol.setFollowRedirects(true);
         generateIfHeader(mkcol, lockToken);
         int status = client.executeMethod(mkcol);
         if (status != WebdavStatus.SC_CREATED) {
            HttpException ex = new HttpException("Can't create collection " + 
                                                 newColl);
            ex.setReasonCode(status);
            ex.setReason(mkcol.getStatusText());
            throw ex;
         }
      }
      return created;
   }
   
   public static void putFile(HttpClient client, 
                              HttpURL url, 
                              InputStream is,
                              String contentType,
                              String lockToken)
      throws IOException, HttpException
   {
      PutMethod put = new PutMethod(url.getURI());
      generateIfHeader(put, lockToken);
      put.setRequestHeader("Content-Type", contentType);
      put.setRequestBody(is);
      put.setFollowRedirects(true);
      int status = client.executeMethod(put);
      switch (status) {
         case WebdavStatus.SC_OK:
         case WebdavStatus.SC_CREATED:
         case WebdavStatus.SC_NO_CONTENT:
            return;
         default:
            HttpException ex = new HttpException();
            ex.setReason(put.getStatusText());
            ex.setReasonCode(status);
            throw ex;
      }
   }
   
   public static InputStream getFile(HttpClient client, HttpURL url)
      throws IOException, HttpException
   {
      GetMethod get = new GetMethod(url.toString());
      get.setFollowRedirects(true);
      int status = client.executeMethod(get);
      
      switch (status) {
         case WebdavStatus.SC_OK:
            return get.getResponseBodyAsStream();
         default:
            HttpException ex = new HttpException();
            ex.setReason(get.getStatusText());
            ex.setReasonCode(status);
            throw ex;
      }
      
   }
   
   public static void generateIfHeader(HttpMethod method, String lockToken) {
      if (lockToken != null) {
         Header ifHeader = new Header();
         ifHeader.setName("If");
         ifHeader.setValue("<> (<" + lockToken + ">)");
         method.addRequestHeader(ifHeader);
      }
   }

   public static String lockResource(HttpClient client, HttpURL url, 
                                     String ownerInfo, int depth, int timeout)
      throws IOException, HttpException
   {
      LockMethod lock = new LockMethod(url.getURI());
      lock.setDepth(depth);
      lock.setTimeout(timeout);
      lock.setOwner(ownerInfo);
      //lock.setDebug(1);
      lock.setFollowRedirects(true);
      int status = client.executeMethod(lock);
      if (status == WebdavStatus.SC_OK) {
         Header header = lock.getResponseHeader("Lock-Token");
         if (header != null) {
            String l = header.getValue();
            return l.substring(1, l.length()-1);
         } else {
            String l = lock.getLockToken();
            if (l != null) {
               return l;
            }
            throw new WebdavException("LOCK does not provide a lock token.");
         }
      } else if (status == WebdavStatus.SC_MULTI_STATUS) {
         throw Utils.makeBuildException("Can't lock", lock.getResponses());
      } else {
         throw Utils.makeBuildException("Can't lock", status, lock.getStatusText());
      }
   }
   
   public static void unlockResource(HttpClient client, HttpURL url, 
                                     String lockToken)
      throws IOException, HttpException
   {
      UnlockMethod unlock = new UnlockMethod(url.getURI(), lockToken);
      unlock.setFollowRedirects(true);
      int status = client.executeMethod(unlock);
      
      switch (status) {
         case WebdavStatus.SC_OK:
         case WebdavStatus.SC_NO_CONTENT:
            return;
         
         default:
            HttpException ex = new HttpException();
            ex.setReasonCode(status);
            ex.setReason(unlock.getStatusText());
            throw ex;
      }
   }

   public static void copyResource(HttpClient client, HttpURL url, 
                                   String destination, int depth, boolean overwrite)
      throws IOException, HttpException 
   {
      CopyMethod copy = new CopyMethod(
              url.getURI(), 
              destination, 
              overwrite, 
              depth);
      copy.setFollowRedirects(true);
      int status = client.executeMethod(copy);
      switch (status) {
         case WebdavStatus.SC_OK:
         case WebdavStatus.SC_CREATED:
         case WebdavStatus.SC_NO_CONTENT:
             return;
         
         default:
             HttpException ex = new HttpException();
             ex.setReasonCode(status);
             ex.setReason(copy.getStatusText());
             throw ex;
      }
   }

   public static void moveResource(HttpClient client, HttpURL url, 
                                   String destination, boolean overwrite)
      throws IOException, HttpException 
   {
      MoveMethod move = new MoveMethod(url.getURI(), destination, overwrite);
      move.setFollowRedirects(true);
      int status = client.executeMethod(move);
      switch (status) {
         case WebdavStatus.SC_OK:
         case WebdavStatus.SC_CREATED:
         case WebdavStatus.SC_NO_CONTENT:
             return;

         default:
             HttpException ex = new HttpException();
             ex.setReasonCode(status);
             ex.setReason(move.getStatusText());
             throw ex;
   }
}

   public static BuildException makeBuildException(String msg, Exception e) {
      if (e instanceof HttpException) {
         HttpException he = (HttpException)e; 
         return new BuildException(
               msg + " " + e.getMessage() + " (" + 
               (he.getReason() != null 
                     ? he.getReason() 
                     : HttpStatus.getStatusText(he.getReasonCode())) + 
               ")");
         
      } else {
         return new BuildException(msg + " (" + e.toString() + ")", e);
      }
   }

   public static BuildException makeBuildException(String msg, int status) {
      return new BuildException(msg + " (" + 
                                HttpStatus.getStatusText(status) +
                                ")");
   }
   public static BuildException makeBuildException(String msg, 
         int status, String statusText) 
   {
      return new BuildException(msg + " (" + 
                                status + ", " + statusText + ")");
   }
   
   public static BuildException makeBuildException(
         String msg,
         Enumeration enumOfResponseEntities)
   {
      StringBuffer b = new StringBuffer();
      
      b.append(msg).append("\n");
      
      for(;enumOfResponseEntities.hasMoreElements();) {
         ResponseEntity e = (ResponseEntity)enumOfResponseEntities.nextElement();
         
         b.append(e.getHref())
          .append(" ")
          .append(HttpStatus.getStatusText(e.getStatusCode()))
          .append("\n");
      }
      
      return new BuildException(b.toString());
   }
   
   
   public static HttpURL createHttpURL(HttpURL base, String relative) 
       throws URIException
   {
     if (base instanceof HttpsURL) {
        return new HttpsURL((HttpsURL)base, relative);
     } else {
        return new HttpURL(base, relative);
     }
   }
    
   public static HttpURL createHttpURL(String url) throws URIException
   {
      if (url.startsWith("https://")) {
         return new HttpsURL(url);
      } else {
         return new HttpURL(url);
      }
   }


}
