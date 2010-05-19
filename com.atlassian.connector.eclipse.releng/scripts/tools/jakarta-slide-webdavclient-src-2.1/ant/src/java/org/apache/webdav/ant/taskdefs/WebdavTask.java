// vi: set ts=3 sw=3:
/* 
 * $Header: /home/cvs/jakarta-slide/webdavclient/ant/src/java/org/apache/webdav/ant/taskdefs/WebdavTask.java,v 1.4.2.1 2004/08/15 13:01:15 luetzkendorf Exp $
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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.webdav.ant.Utils;

/**
 * Base class of all WebDAV tasks. Provides common attributes and the creation
 * of the HttpClient.
 * 
 * <p>TODO enable proxies?
 */
public abstract class WebdavTask extends Task {
   private HttpURL url = null;
   private String userid = "";
   private String password = "";
   private HttpClient httpClient = null;
   private boolean verbose = false;
   
   
   protected HttpClient getHttpClient() {
      try {
         if (this.httpClient == null) {
            this.httpClient = new HttpClient();
            if (this.userid.length() > 0) {
               this.httpClient.getState().setCredentials(
                     null, 
                     this.url.getHost(),
                     new UsernamePasswordCredentials(this.userid, this.password));
            }
         }
         return this.httpClient;
      } catch (URIException e) {
         throw new BuildException("Can't create HttpClient.", e);
      }
   }
   
   protected void validate() {
      if (this.url == null) {
         throw new BuildException("Required attribute url missing!");
      }
   }
   
   /**
    * Sets the username for authentication at the WebDAV server.
    * @param userid
    */
   public void setUserid(String userid) {
      this.userid = userid;
   }
   protected String getUserid() {
      return this.userid;
   }
   /**
    * Sets the password for authentication at the WebDAV server.
    * @param password
    */
   public void setPassword(String password) {
      this.password = password;
   }
   protected String getPassword() {
      return this.password;
   }
   /**
    * Set the base URL.
    * @param url URL for the request.
    */
   public void setUrl(String url) {
       try {
          this.url = Utils.createHttpURL(url);
          // remove double slashes in url like /DAV/files//document.txt
          this.url.setPath(removeDoubleSlashes(this.url.getPath()));
       } catch (URIException e) {
          throw new BuildException("Invalid uri!", e);
       }
   }

   protected void setUrl(HttpURL url) {
      this.url = url;
   }
   protected HttpURL getUrl() {
      //return (HttpURL)this.url.clone(); // does not work, clone returns an URL
      return this.url;
   }
   
   public void setVerbose(boolean v) {
      this.verbose = v;
   }
   /**
    * Returns the INFO message level if the verbose attribute is set.
    * @return Project.MSG_VERBOSE or Project.MSG_INFO
    */
   protected int ifVerbose() {
      return this.verbose ? Project.MSG_INFO : Project.MSG_VERBOSE; 
   }
   
   public static HttpURL assureCollectionUrl(HttpURL url)
      throws URIException
   {
      if (url.getPath().endsWith("/")) {
         return url;
      } else {
         HttpURL coll = Utils.createHttpURL(url, "");
         coll.setPath(url.getPath() + "/");
         return coll;
      }
   }
   
   static String removeDoubleSlashes(String path) {
      if (path.indexOf("//") == -1) return path;
      
      StringBuffer r = new StringBuffer(path.length());
      for(int i = 0, l = path.length(); i < l; i++) {
         if (path.charAt(i) == '/') {
            if (!(i > 0 && path.charAt(i-1) == '/')) {
               r.append('/');
            }
         } else {
            r.append(path.charAt(i));
         }
      }
      return r.toString();
   }
}
