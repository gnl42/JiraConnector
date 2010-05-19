// vi: set ts=3 sw=3:
/* 
 * $Header: /home/cvs/jakarta-slide/webdavclient/ant/src/java/org/apache/webdav/ant/taskdefs/Delete.java,v 1.5.2.2 2004/08/22 10:36:47 luetzkendorf Exp $
 * $Revision: 1.5.2.2 $
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
package org.apache.webdav.ant.taskdefs;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpURL;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.webdav.ant.CollectionScanner;
import org.apache.webdav.ant.Utils;
import org.apache.webdav.ant.WebdavFileSet;
import org.apache.webdav.lib.methods.DeleteMethod;
import org.apache.webdav.lib.util.WebdavStatus;

/**
 * WebDAV task for deleting resources and collections.
 * 
 * @see <a href="../doc-files/tasks.htm#davdelete">Task documentation</a>
 */
public class Delete extends WebdavMatchingTask
{
   private String locktoken = null;
   private int count = 0;
   
   public void execute() throws BuildException {
      try {
         if (!getFileSets().hasNext()) {
            // delete the resource given by url
            log("Deleting: " + getUrl(), Project.MSG_INFO);
            delete(getUrl(), getUrl().getURI());
         } else {
            // delete all resources in file sets
            log("Deleting at: " + getUrl(), ifVerbose());
            // URL must be a collection
            if (!getUrl().getPath().endsWith("/")) {
               getUrl().setPath(getUrl().getPath() + "/");
            }
            for(Iterator i = getFileSets(); i.hasNext(); ) {
               deleteFileset((WebdavFileSet)i.next());
            }
            log("Deleted " + this.count 
                  + (this.count == 1 ? " resource" : " resources")
                  + " from " + getUrl(),
                  this.count > 0 
                     ? Project.MSG_INFO
                     : ifVerbose());
         }
      } 
      catch (IOException e) {
         throw Utils.makeBuildException("Can't delete!", e);
      }
   }
   
   
   private void delete(HttpURL url, String logName)
      throws IOException, HttpException
   {
      validate();
      log("Deleting " + logName, ifVerbose());
      DeleteMethod delete = new DeleteMethod(url.getURI());
      delete.setFollowRedirects(true);
      if (this.locktoken != null) {
         Utils.generateIfHeader(delete, this.locktoken);
      }
      int status = getHttpClient().executeMethod(delete);
      
      switch (status) {
         case WebdavStatus.SC_OK:
         case WebdavStatus.SC_NO_CONTENT:
         case WebdavStatus.SC_NOT_FOUND:
            // ok
            this.count++;
            break;
         default:
            HttpException ex = new HttpException();
            ex.setReasonCode(status);
            throw ex;
      }
   }
   
   private void deleteFileset(WebdavFileSet fileSet)
      throws IOException, HttpException
   {
      CollectionScanner scanner = 
         fileSet.getCollectionScanner(getProject(), getHttpClient(), getUrl());
      HttpURL baseUrl = scanner.getBaseURL();
     
      String[] files = scanner.getIncludedFiles();
      for (int i = 0; i < files.length; i++) {
         HttpURL url = Utils.createHttpURL(baseUrl, files[i]);
         delete(url, files[i]);
      }
      String[] colls = scanner.getIncludedDirectories();
      for (int i = 0; i < colls.length; i++) {
         HttpURL url = Utils.createHttpURL(baseUrl, colls[i]);
         delete(url, colls[i]);
      }
   }
   
   public void setLocktoken(String token) {
      this.locktoken = token;
      if (!this.locktoken.startsWith("opaquelocktoken:")) {
         throw new BuildException("Invalid locktoken: " + token);
      }
   }
}
