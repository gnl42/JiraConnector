// vi: set ts=3 sw=3:
/* 
 * $Header: /home/cvs/jakarta-slide/webdavclient/ant/src/java/org/apache/webdav/ant/taskdefs/Lock.java,v 1.3 2004/07/28 09:31:47 ib Exp $
 * $Revision: 1.3 $
 * $Date: 2004/07/28 09:31:47 $
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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.webdav.ant.Utils;
import org.apache.webdav.lib.methods.DepthSupport;

/**
 * WebDAV task for locking resources.
 * 
 * @see <a href="../doc-files/tasks.htm#lock">Task documentation</a>
 */
public class Lock extends WebdavTask {
   private String lockTokenProperty = null;
   private int timeout = 3600;
   private String ownerInfo = null;
   private int depth = DepthSupport.DEPTH_INFINITY;
   
   /* 
    * @see org.apache.tools.ant.Task#execute()
    */
   public void execute() throws BuildException {
      validate();
      try {
         log("Locking " + getUrl(), Project.MSG_INFO);
         String locktoken = Utils.lockResource(
               getHttpClient(), 
               getUrl(),
               this.ownerInfo,
               this.depth,
               this.timeout
         );
         getProject().setProperty(this.lockTokenProperty, locktoken);
      } 
      catch (IOException e) {
         throw Utils.makeBuildException("Can't lock!", e);
      }
   }
   
   protected void validate() throws BuildException
   {
      super.validate();
      
      if (this.lockTokenProperty == null) {
         throw new BuildException("Attribute property required!");
      }
      if (this.ownerInfo == null) {
         this.ownerInfo =  "ant-webdav " + getUserid();
      }
   }
   
   
   public void setProperty (String name) {
      this.lockTokenProperty = name;
   }
   public void setTimeout(int value) {
      if (value > 0) {
         this.timeout = value;
      } else {
         throw new BuildException("Invalid timeout value (Must be " +
               "positive integer)");
      }
   }
   public void setOwnerinfo(String value) {
      this.ownerInfo = value;
   }
   public void setDepth(String value) {
      if ("0".trim().equals(value)) {
         this.depth = DepthSupport.DEPTH_0;
      } 
      else if ("infinity".trim().toLowerCase().equals(value)) {
         this.depth = DepthSupport.DEPTH_INFINITY;
      }
      else {
         throw new BuildException("Invalid value of depth attribute." 
               + " (One of '0' or 'infinity' exprected)");
      }
   }
}
