// vi: set ts=3 sw=3:
/* 
 * $Header: /home/cvs/jakarta-slide/webdavclient/ant/src/java/org/apache/webdav/ant/WebdavFileSet.java,v 1.3.2.1 2004/08/15 13:01:15 luetzkendorf Exp $
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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.URIException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.PatternSet;

/**
 */
public class WebdavFileSet {
   private PatternSet patterns = new PatternSet();
   private List patternSets = new ArrayList();
   
   private String directory = null;
   private boolean isCaseSensitive = true;
   
   private static final String[] DEFAULT_INCLUDES = {
         "**/*"
   };
   
   public WebdavFileSet() {
   }
   
   public CollectionScanner getCollectionScanner(
         Project project,
         HttpClient httpClient,
         HttpURL baseUrl)
   {
      validate();
      
      CollectionScanner scanner = new CollectionScanner();
      
      try {
         scanner.setBaseURL(Utils.createHttpURL(baseUrl, directory));
      } catch (URIException e) {
         throw new BuildException("Invalid URL. " + e.toString(), e);
      }
      scanner.setHttpClient(httpClient);
      
      scanner.setCaseSensitive(this.isCaseSensitive);
      
      if (this.patterns.getExcludePatterns(project) == null &&
          this.patterns.getIncludePatterns(project) == null &&
          this.patternSets.size() == 0) {
         scanner.setIncludes(DEFAULT_INCLUDES);
      } else {
         scanner.setExcludes(this.patterns.getExcludePatterns(project));
         scanner.setIncludes(this.patterns.getIncludePatterns(project));         
         for (Iterator i = this.patternSets.iterator(); i.hasNext();) {
            PatternSet patternSet = (PatternSet)i.next();
            scanner.addExcludes(patternSet.getExcludePatterns(project));
            scanner.addIncludes(patternSet.getIncludePatterns(project));
         }
      }
      scanner.scan();
      return scanner;
   }
   
   protected void validate() {
      if (this.directory == null) this.directory = "";
   }
   
   /**
    * Sets the <code>dir</code> attribute.
    * @param dir
    */
   public void setDir(String dir) {
      this.directory = dir;
      if (!this.directory.endsWith("/")) {
         this.directory += "/";
      }
      if (this.directory.startsWith("/")) {
         this.directory = this.directory.substring(1);
      }
   }

   /**
    * Sets the <code>casesensitive</code> attribute.
    * @param b
    */
   public void setCasesensitive(boolean b) {
      this.isCaseSensitive = b;
   }
   
   /**
    * Creates nested include and adds it to the patterns.
    */
   public PatternSet.NameEntry createInclude() {
       return this.patterns.createInclude();
   }

   /**
    * Creates nested includesfile and adds it to the patterns.
    */
   public PatternSet.NameEntry createIncludesFile() {
       return this.patterns.createIncludesFile();
   }

   /**
    * Creates nested exclude and adds it to the patterns.
    */
   public PatternSet.NameEntry createExclude() {
       return this.patterns.createExclude();
   }

   /**
    * Creates nested excludesfile and adds it to the patterns.
    */
   public PatternSet.NameEntry createExcludesFile() {
       return this.patterns.createExcludesFile();
   }
   
   /**
    * Creates a nested patternset.
    */
   public PatternSet createPatternSet() {
       PatternSet patterns = new PatternSet();
       this.patternSets.add(patterns);
       return patterns;
   }
}
