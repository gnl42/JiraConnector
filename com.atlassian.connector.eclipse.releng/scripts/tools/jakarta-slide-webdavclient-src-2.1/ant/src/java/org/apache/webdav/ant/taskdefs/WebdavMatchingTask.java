/* 
 * $Header: /home/cvs/jakarta-slide/webdavclient/ant/src/java/org/apache/webdav/ant/taskdefs/WebdavMatchingTask.java,v 1.3 2004/07/28 09:31:47 ib Exp $
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.webdav.ant.WebdavFileSet;

/**
 * Baseclass of all WebDAV tasks that work on sets of WebDAV resources.
 * 
 * <p>Provides nested {@link org.apache.webdav.ant.WebdavFileSet}s.
 * 
 */
public abstract class WebdavMatchingTask extends WebdavTask { 

   protected List filesets = new ArrayList(); 

   public void addDavfileset(WebdavFileSet set) {
      filesets.add(set);
   }
   protected Iterator getFileSets() {
      return this.filesets.iterator();
   }
}
