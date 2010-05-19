// vi: set ts=3 sw=3:
/* 
 * $Header: /home/cvs/jakarta-slide/webdavclient/ant/src/java/org/apache/webdav/ant/taskdefs/Proppatch.java,v 1.4.2.1 2004/08/15 13:01:15 luetzkendorf Exp $
 * $Revision: 1.4.2.1 $
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
package org.apache.webdav.ant.taskdefs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpURL;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.webdav.ant.CollectionScanner;
import org.apache.webdav.ant.Utils;
import org.apache.webdav.ant.WebdavFileSet;
import org.apache.webdav.lib.Constants;
import org.apache.webdav.lib.ResponseEntity;
import org.apache.webdav.lib.methods.PropPatchMethod;
import org.apache.webdav.lib.util.WebdavStatus;

/**
 * WebDAV task for editing resource properties.
 * 
 * <p>TODO: Howto set Properties with XML values?
 */
public class Proppatch extends WebdavMatchingTask
{
   private String locktoken = null;
   private List toSet = new ArrayList();
   private List toRemove = new ArrayList();
   private int count = 0;
   
   public void execute() throws BuildException {
      validate();
      try {
         if (!getFileSets().hasNext()) {
            // delete the resource given by url
            log(getUrl().getURI(), Project.MSG_INFO);
            proppatch(getUrl(), getUrl().getURI());
         } else {
            log("at: " + getUrl(), ifVerbose());
            // URL must be a collection
            if (!getUrl().getPath().endsWith("/")) {
               getUrl().setPath(getUrl().getPath() + "/");
            }
            for(Iterator i = getFileSets(); i.hasNext(); ) {
               proppatch((WebdavFileSet)i.next());
            }
            log("Properties set on " + this.count 
                  + (this.count == 1 ? " resource" : " resources")
                  + " at " + getUrl(),
                  this.count > 0  ? Project.MSG_INFO : ifVerbose());
         }
      }
      catch (IOException e) {
         throw Utils.makeBuildException("Can't proppatch!", e);
      }
   }
   
   protected void validate() {
      super.validate();
      for(Iterator i = this.toSet.iterator(); i.hasNext();) {
         Set a = (Set)i.next();
         if (a.name == null) {
            throw new BuildException("Add must have name attribute.");
         }
      }
      for(Iterator i = this.toRemove.iterator(); i.hasNext();) {
         Remove r = (Remove)i.next();
         if (r.name == null) {
            throw new BuildException("Remove must have name attribute.");
         }
      }
   }
   
   protected void proppatch(HttpURL url, String logName)
      throws IOException, HttpException
   {
      log(logName, ifVerbose());
      PropPatchMethod propPatch = new PropPatchMethod(url.getURI());
      if (this.locktoken != null) {
         Utils.generateIfHeader(propPatch, this.locktoken);
      }
      
      int c = 1;
      for(Iterator i = toRemove.iterator(); i.hasNext(); ) {
         Remove r = (Remove)i.next();
         propPatch.addPropertyToRemove(r.name, 
               r.abbrev != null ? r.abbrev : "NS"+(c++), 
               r.namespace);
      }
      for(Iterator i = toSet.iterator(); i.hasNext(); ) {
         Set a = (Set)i.next();
         propPatch.addPropertyToSet(a.name, 
               a.getValue(), 
               a.abbrev != null ? a.abbrev : "NS"+(c++), 
               a.namespace);
      }
      
      int status = getHttpClient().executeMethod(propPatch);
      count++;
      
      switch (status) {
         case WebdavStatus.SC_OK:
            // ok
            break;
         case WebdavStatus.SC_MULTI_STATUS:
            for(Enumeration e = propPatch.getResponses(); e.hasMoreElements();) {
               ResponseEntity response = (ResponseEntity)e.nextElement();
               
               if (response.getStatusCode() > 400) {
                  throw Utils.makeBuildException("Error while PROPPATCH",
                        propPatch.getResponses());
               }
            }
            break;
            
         default:
            HttpException ex = new HttpException();
            ex.setReasonCode(status);
            throw ex;
      }
   }
   
   protected void proppatch(WebdavFileSet fileSet)
      throws IOException, HttpException
   {
      CollectionScanner scanner = 
         fileSet.getCollectionScanner(getProject(), getHttpClient(), getUrl());
      HttpURL baseUrl = scanner.getBaseURL();
     
      String[] files = scanner.getIncludedFiles();
      for (int i = 0; i < files.length; i++) {
         HttpURL url = Utils.createHttpURL(baseUrl, files[i]);
         proppatch(url, files[i]);
      }
      String[] colls = scanner.getIncludedDirectories();
      for (int i = 0; i < colls.length; i++) {
         HttpURL url = Utils.createHttpURL(baseUrl, colls[i]);
         proppatch(url, colls[i]);
      }
   }

   public void setLocktoken(String token) {
      this.locktoken = token;
      if (!this.locktoken.startsWith("opaquelocktoken:")) {
         throw new BuildException("Invalid locktoken: " + token);
      }
   }
   public Set createSet() {
      Set add = new Set();
      this.toSet.add(add);
      return add;
   }
   public Remove createRemove() {
      Remove remove = new Remove();
      this.toRemove.add(remove);
      return remove;
   }
   
   public class Set {
      String name;
      String namespace;
      String abbrev;
      String value;
      StringBuffer text = null;
      Set() {
         this.name = null;
         this.namespace = Constants.DAV;
         this.abbrev = null;
         this.value = null;
      }
      public void setName(String name) {
         this.name = name;
      }
      public void setNamespace(String namespace) {
         this.namespace = namespace;
      }
      public void setNamespaceprefix(String pfx) {
         this.abbrev = pfx;
      }
      public void setValue(String value) {
         this.value = value;
      }
      public void addText(String text) {
         if (this.value != null) {
            throw new BuildException("Only one of nested text or value attribute is allowed.");
         }
         if (this.text == null) this.text = new StringBuffer();
         this.text.append(getProject().replaceProperties(text));
      }
      String getValue() {
         if (this.value != null) {
            return this.value;
         }
         if (this.text != null) {
            return this.text.toString();
         }
         throw new BuildException("Either one of nested text or value attribute must be set.");
      }
   }
   public static class Remove {
      String name;
      String namespace;
      String abbrev;
      Remove() {
         this.name = null;
         this.name = Constants.DAV;
         this.abbrev = null;
      }
      public void setName(String name) {
         this.name = name;
      }
      public void setNamespace(String namespace) {
         this.namespace = namespace;
      }
      public void setNamespaceprefix(String pfx) {
         this.abbrev = pfx;
      }
   }
}
