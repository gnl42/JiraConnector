// vi: set ts=3 sw=3:
/* 
 * $Header: /home/cvs/jakarta-slide/webdavclient/ant/src/java/org/apache/webdav/ant/taskdefs/Mkcol.java,v 1.4 2004/07/28 09:31:47 ib Exp $
 * $Revision: 1.4 $
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

/**
 * WebDAV task for creating collections.
 * 
 * @see <a href="../doc-files/tasks.htm#mkcol">Task documentation</a> 
 */
public class Mkcol extends WebdavTask {
   private String locktoken = null;
   
   /* 
    * @see org.apache.tools.ant.Task#execute()
    */
   public void execute() throws BuildException {
      validate();
      try {
         boolean created = Utils.assureExistingCollection(
                                    getHttpClient(), 
                                    getUrl(), 
                                    this.locktoken);
         if (created) {
            log("Created collection " + getUrl(), Project.MSG_INFO);
         }
      } 
      catch (IOException e) {
         throw Utils.makeBuildException("Can't make collection!", e);
      }
   }
   

   public void setLocktoken(String token) {
      this.locktoken = token;
      if (!this.locktoken.startsWith("opaquelocktoken:")) {
         throw new BuildException("Invalid locktoken: " + token);
      }
   }
}
